package com.example.fastrail.service;

import com.example.fastrail.common.APBusinessException;
import com.example.fastrail.common.Constant;
import com.example.fastrail.config.RabbitMQConfig;
import com.example.fastrail.dto.AuditPayload;
import com.example.fastrail.dto.OrdersDTO;
import com.example.fastrail.model.*;
import com.example.fastrail.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.hibernate.query.Order;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdersService {

    private final OrdersRepository ordersRepo;
    private final UsersRepository usersRepo;
    private final TrainsRepository trainsRepo;
    private final TrainStopsRepository trainStopsRepo;
    private final StationsRepository stationsRepo;
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public Orders findById(Integer id){
        Optional<Orders> op = ordersRepo.findById(id);
        return op.orElse(null);
    }

    public Page<Orders> findByPage(Integer pageNumber){
        int pageNum=  pageNumber != null ? pageNumber - 1 : 0;
        PageRequest pg = PageRequest
                .of(pageNum, 10, Sort.by("createdAt").descending());
        return ordersRepo.findAll(pg);
    }

    public void deleteById(Integer id){
        ordersRepo.deleteById(id);
    }

    public Orders findByOrderNumber(String orderNumber){
        return ordersRepo.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("找不到此車票編號"));
    }

    public List<Orders> findByUser(Integer userId){
        Users user = usersRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("找不到使用者"));

        return ordersRepo.findByUser(user).stream()
                .sorted(Comparator.comparing(Orders::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE_NAME)
    public void createOrder(OrdersDTO ordersDTO) throws JsonProcessingException {
        try{
        Users user = usersRepo.findById(ordersDTO.getUserId())
                .orElseThrow(() -> new APBusinessException(Constant.RCODE.NO_USER_INFO));

        if(!user.getTwId().equals(ordersDTO.getTwId()) ){
            throw new APBusinessException(Constant.RCODE.ERROR_IDENTIFIED_CODE);
        }


        Trains train = trainsRepo.findByTrainNumberForUpdate(ordersDTO.getTrainNumber())
                .orElseThrow(() -> new APBusinessException(Constant.RCODE.NO_TRAINS_INFO));

        if (train.getAvailableSeats() <= 0) {
            throw new APBusinessException(Constant.RCODE.NO_AVAILABLE_SEAT);
        }

        Stations depStation = stationsRepo.findById(ordersDTO.getDepartureStationId())
                .orElseThrow(() -> new APBusinessException(Constant.RCODE.NO_STATIONS_INFO));

        Stations arrStation = stationsRepo.findById(ordersDTO.getArrivalStationId())
                .orElseThrow(() -> new APBusinessException(Constant.RCODE.NO_STATIONS_INFO));

        TrainStops depStopStation = trainStopsRepo.findByTrainAndStation(train, depStation)
                .orElseThrow(() -> new APBusinessException(Constant.RCODE.NO_STATIONS_INFO));
        TrainStops arrStopStation = trainStopsRepo.findByTrainAndStation(train, arrStation)
                .orElseThrow(() -> new APBusinessException(Constant.RCODE.NO_STATIONS_INFO));

        if (depStopStation.getStopSequence() >= arrStopStation.getStopSequence()) {
            throw new APBusinessException(Constant.RCODE.NO_STATIONS_INFO);
        }

        Orders orders = createOrderEntity(user,
                train,
                depStation,
                arrStation,
                ordersDTO);

        train.setAvailableSeats(train.getAvailableSeats() - 1);
        trainsRepo.save(train);

        ordersRepo.save(orders);
        updateOrderResultInRedis(ordersDTO.getClientOrderId(),
                "SUCCESS",
                orders.getOrderNumber(),
                "");
        log.info("📬 已成功創建訂單: {}", orders.getOrderNumber());
        sendAuditLog("CREATE_ORDER", ordersDTO.getTwId(), "SUCCESS");
        }catch (APBusinessException ae){
            handleOrderProcessingFailure(ordersDTO.getClientOrderId(),
                    ordersDTO.getTwId(),
                    ae.getFullMessage(),
                    ae);
        }
        catch (Exception e) {
            handleOrderProcessingFailure(ordersDTO.getClientOrderId(),
                    ordersDTO.getTwId(),
                    e.getMessage(),
                    e);
        }
    }

    @Transactional
    public Orders payOrder(String orderNumber, String twId){
        Orders order = ordersRepo.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("找不到可以付款的訂單"));
        System.out.println(order.getUser().getTwId());
        System.out.println(twId);
        if(!order.getUser().getTwId().equals(twId)){
            throw new RuntimeException("取票識別碼錯誤");
        }
        if(LocalDateTime.now().isAfter(order.getPaymentDeadline())){
            throw new RuntimeException("已超過付款期限");
        }

        if(order.getOrderStatus() != Orders.OrderStatus.PENDING){
            throw new RuntimeException("訂單狀態不正確");
        }

        order.setOrderStatus(Orders.OrderStatus.PAID);
        return ordersRepo.save(order);
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cancelExpiredOrders(){
        List<Orders> expiredOrders = ordersRepo.findByOrderStatusAndPaymentDeadlineBefore(
                Orders.OrderStatus.PENDING,
                LocalDateTime.now()
        );

        for (Orders order : expiredOrders) {
            order.setOrderStatus(Orders.OrderStatus.CANCELLED);

            Trains train = order.getTrain();
            train.setAvailableSeats(train.getAvailableSeats() + 1);
            trainsRepo.save(train);
        }

        if (!expiredOrders.isEmpty()) {
            ordersRepo.saveAll(expiredOrders);
            System.out.println("已自動取消 " + expiredOrders.size() + " 筆過期訂單");
        }
    }

    private Orders createOrderEntity(Users user, Trains train, Stations depStation, Stations arrStation, OrdersDTO ordersDTO) {
        Orders orders = new Orders();
        orders.setUser(user);
        orders.setTrain(train);
        orders.setDepartureStation(depStation);
        orders.setArrivalStation(arrStation);
        orders.setDepartureTime(ordersDTO.getDepartureTime());
        orders.setArrivalTime(ordersDTO.getArrivalTime());
        orders.setSeatNumber(generateSeatNumber(train, depStation, arrStation));
        orders.setTicketPrice(calculateTicketPrice(depStation, arrStation));
        orders.setOrderNumber(generateOrderNumber());
        orders.setOrderStatus(Orders.OrderStatus.PENDING);
        orders.setPaymentDeadline(LocalDateTime.now().plusMinutes(60));

        // 設置行程類型
        orders.setTripType(depStation.getId() > arrStation.getId() ? "去程" : "回程");

        return orders;
    }

    // 更新 Redis 中的訂單結果
    private void updateOrderResultInRedis(String clientOrderId, String status, String orderNumber, String errorMessage) throws JsonProcessingException {
        Map<String, Object> resultMap;
        if ("SUCCESS".equals(status)) {
            resultMap = Map.of("status", status, "orderNumber", orderNumber);
        } else {
            resultMap = Map.of("status", status, "message", errorMessage);
        }

        redisTemplate.opsForValue().set(
                "clientOrder:" + clientOrderId,
                objectMapper.writeValueAsString(resultMap),
                5,
                TimeUnit.MINUTES
        );
    }

    // 發送審計日誌
    private void sendAuditLog(String action, String twId, String status) {
        AuditPayload auditPayload = new AuditPayload(action, twId, status, Instant.now());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.AUDIT_ROUTING_KEY,
                auditPayload
        );
    }

    // 處理訂單處理失敗
    private void handleOrderProcessingFailure(String clientOrderId, String twId, String errorMessage, Exception e) throws JsonProcessingException {
        updateOrderResultInRedis(clientOrderId, "FAILED", null, errorMessage);
        sendAuditLog("CREATE_ORDER", twId, "FAILED");
        log.info("❌ 創建訂單失敗 {}", errorMessage, e);
    }

    private String generateOrderNumber(){
        return "THR" +
                LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE) +
                String.format("%06d", new Random().nextInt(999999));
    }

    private String generateSeatNumber(Trains train, Stations departureStation, Stations arrivalStation) {
        List<Orders> existingOrders = ordersRepo.findByTrain(train);

        List<String> allPossibleSeats = new ArrayList<>();
        for (int car = 1; car <= 12; car++) {
            for (int row = 1; row <= 15; row++) {
                for (char col = 'A'; col <= 'E'; col++) {
                    allPossibleSeats.add(String.format("%02d%02d%c", car, row, col));
                }
            }
        }

        // 過濾掉與當前行程區間重疊的座位
        List<String> unavailableSeats = existingOrders.stream()
                .filter(order -> isRouteOverlapping(
                        order.getDepartureStation(), order.getArrivalStation(),
                        departureStation, arrivalStation))
                .map(Orders::getSeatNumber)
                .toList();

        // 找出可用座位
        for (String seat : allPossibleSeats) {
            if (!unavailableSeats.contains(seat)) {
                return seat;
            }
        }

        throw new RuntimeException("沒有可用的座位");
    }


    private boolean isRouteOverlapping(
            Stations existingDeparture, Stations existingArrival,
            Stations newDeparture, Stations newArrival) {

        int existingStart = existingDeparture.getId();
        int existingEnd = existingArrival.getId();
        int newStart = newDeparture.getId();
        int newEnd = newArrival.getId();


        if (existingStart > existingEnd) {
            int temp = existingStart;
            existingStart = existingEnd;
            existingEnd = temp;
        }

        if (newStart > newEnd) {
            int temp = newStart;
            newStart = newEnd;
            newEnd = temp;
        }

        return !(newEnd <= existingStart || newStart >= existingEnd);
    }

    private int calculateTicketPrice(Stations departure, Stations arrival){
        int distance = Math.abs(arrival.getId() - departure.getId());
        return  distance * 120;
    }
}
