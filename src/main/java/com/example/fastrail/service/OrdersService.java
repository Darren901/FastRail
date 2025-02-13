package com.example.fastrail.service;

import com.example.fastrail.dto.OrdersDTO;
import com.example.fastrail.model.*;
import com.example.fastrail.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

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

        return ordersRepo.findByUser(user);
    }

    @Transactional
    public Orders createOrder(OrdersDTO ordersDTO){
        Users user = usersRepo.findById(ordersDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("找不到該會員資訊"));

        Trains train = trainsRepo.findById(ordersDTO.getTrainId())
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
        orders.setSeatNumber(generateSeatNumber(train));
        orders.setTicketPrice(calculateTicketPrice(
                depStation,
                arrStation,
                depStopStation.getDepartureTime()
        ));
        orders.setOrderNumber(generateOrderNumber());
        orders.setOrderStatus(Orders.OrderStatus.PENDING);
        orders.setPaymentDeadline(LocalDateTime.now().plusMinutes(15));


        train.setAvailableSeats(train.getAvailableSeats() - 1);
        trainsRepo.save(train);

        return ordersRepo.save(orders);

    }

    @Transactional
    public Orders payOrder(String orderNumber){
        Orders order = ordersRepo.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("找不到可以付款的訂單"));
        if(LocalDateTime.now().isAfter(order.getPaymentDeadline())){
            throw new RuntimeException("已超過付款期限");
        }

        if(order.getOrderStatus() != Orders.OrderStatus.PENDING){
            throw new RuntimeException("訂單狀態不正確");
        }

        order.setOrderStatus(Orders.OrderStatus.PAID);
        return ordersRepo.save(order);
    }

    private String generateOrderNumber(){
        return "THR" +
                LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE) +
                String.format("%06d", new Random().nextInt(999999));
    }

    private String generateSeatNumber(Trains train){
        List<String> occupiedSeats = ordersRepo.findByTrain(train)
                .stream()
                .map(Orders::getSeatNumber)
                .toList();

        for(int car = 1; car <= 12; car++){
            for(int row = 1; row <= 15; row++){
                for(char col = 'A'; col <= 'E'; col++){
                   String seatNumber = String.format("%02d%02d%c", car, row, col);
                   if(!occupiedSeats.contains(seatNumber)){
                       return seatNumber;
                   }
                }
            }
        }

        throw new RuntimeException("沒有可用的座位");
    }

    private int calculateTicketPrice(Stations departure, Stations arrival, LocalTime departureTime){
        int distance = Math.abs(arrival.getId() - departure.getId());
        int basePrice = distance * 120;

        if(isPeakHour(departureTime)){
            basePrice = (int)(basePrice * 1.1);
        }

        return basePrice;
    }

    private boolean isPeakHour(LocalTime time) {
        return (time.isAfter(LocalTime.of(7, 0)) && time.isBefore(LocalTime.of(9, 0))) ||
                (time.isAfter(LocalTime.of(17, 0)) && time.isBefore(LocalTime.of(19, 0)));
    }
}
