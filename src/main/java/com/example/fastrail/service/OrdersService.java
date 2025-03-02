package com.example.fastrail.service;

import com.example.fastrail.dto.OrdersDTO;
import com.example.fastrail.model.*;
import com.example.fastrail.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class OrdersService {

    @Autowired
    private OrdersRepository ordersRepo;
    @Autowired
    private UsersRepository usersRepo;
    @Autowired
    private TrainsRepository trainsRepo;
    @Autowired
    private TrainStopsRepository trainStopsRepo;
    @Autowired
    private StationsRepository stationsRepo;

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
    public Orders createOrder(OrdersDTO ordersDTO){
        Users user = usersRepo.findById(ordersDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("找不到該會員資訊"));

        if(!user.getTwId().equals(ordersDTO.getTwId()) ){
            throw  new RuntimeException("取票識別碼填寫錯誤");
        }

        Trains train = trainsRepo.findByTrainNumber(ordersDTO.getTrainNumber())
                .orElseThrow(() -> new RuntimeException("找不到車次資訊"));

        if (train.getAvailableSeats() <= 0) {
            throw new RuntimeException("該車次已無座位");
        }

        Stations depStation = stationsRepo.findById(ordersDTO.getDepartureStationId())
                .orElseThrow(() -> new RuntimeException("找不到起始站資訊"));

        Stations arrStation = stationsRepo.findById(ordersDTO.getArrivalStationId())
                .orElseThrow(() -> new RuntimeException("找不到抵達站資訊"));

        TrainStops depStopStation = trainStopsRepo.findByTrainAndStation(train, depStation)
                .orElseThrow(() -> new RuntimeException("此車次未停靠該起始站"));
        TrainStops arrStopStation = trainStopsRepo.findByTrainAndStation(train, arrStation)
                .orElseThrow(() -> new RuntimeException("此車次未停靠該終點站"));

        if (depStopStation.getStopSequence() >= arrStopStation.getStopSequence()) {
            throw new RuntimeException("不符合行車方向");
        }

        Orders orders = new Orders();
        orders.setUser(user);
        orders.setTrain(train);
        orders.setDepartureStation(depStation);
        orders.setArrivalStation(arrStation);
        orders.setDepartureTime(ordersDTO.getDepartureTime());
        orders.setArrivalTime(ordersDTO.getArrivalTime());
        orders.setSeatNumber(generateSeatNumber(train, depStation, arrStation));
        orders.setTicketPrice(calculateTicketPrice(
                depStation,
                arrStation
        ));
        orders.setOrderNumber(generateOrderNumber());
        orders.setOrderStatus(Orders.OrderStatus.PENDING);
        orders.setPaymentDeadline(LocalDateTime.now().plusMinutes(60));
        if(depStation.getId() > arrStation.getId()){
            orders.setTripType("去程");
        }else{
            orders.setTripType("回程");
        }


        train.setAvailableSeats(train.getAvailableSeats() - 1);
        trainsRepo.save(train);

        return ordersRepo.save(orders);

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
