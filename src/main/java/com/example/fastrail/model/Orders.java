package com.example.fastrail.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "orders")
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trains_id")
    private Trains train;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departure_station_id")
    private Stations departureStation;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arrival_station_id")
    private Stations arrivalStation;

    @Column(name = "departure_time")
    private LocalTime departureTime;

    @Column(name = "arrival_time")
    private LocalTime arrivalTime;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    @Column(name = "ticket_price", nullable = false)
    private Integer ticketPrice;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_statis")
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Column(name = "payment_deadline")
    private LocalDateTime paymentDeadline;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum OrderStatus{
        PENDING,
        PAID,
        CANCELLED
    }

    @JsonProperty("userName")
    public String fetchUserName(){
        return user.getName();
    }

    @JsonProperty("trainNumber")
    public String fetchTrainNumber(){
        return train.getTrainNumber();
    }

    @JsonProperty("departureStationName")
    public String fetchDepartureStationName() {
        return departureStation.getStationName();
    }

    @JsonProperty("arrivalStationName")
    public String fetchArrivalStationName() {
        return arrivalStation.getStationName();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Trains getTrain() {
        return train;
    }

    public void setTrain(Trains train) {
        this.train = train;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Integer getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(Integer ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public LocalDateTime getPaymentDeadline() {
        return paymentDeadline;
    }

    public void setPaymentDeadline(LocalDateTime paymentDeadline) {
        this.paymentDeadline = paymentDeadline;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Stations getDepartureStation() {
        return departureStation;
    }

    public void setDepartureStation(Stations departureStation) {
        this.departureStation = departureStation;
    }

    public Stations getArrivalStation() {
        return arrivalStation;
    }

    public void setArrivalStation(Stations arrivalStation) {
        this.arrivalStation = arrivalStation;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
