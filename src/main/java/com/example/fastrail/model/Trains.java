package com.example.fastrail.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trains")
public class Trains {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "train_number", nullable = false)
    private String trainNumber;

    @ManyToOne
    @JoinColumn(name = "departure_station_id", nullable = false)
    private Stations departureStation;

    @ManyToOne
    @JoinColumn(name = "arrival_station_id", nullable = false)
    private Stations arrivalStation;

    @Column(name = "departure_time", nullable = false)
    @Temporal(TemporalType.TIME)
    private LocalTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    @Temporal(TemporalType.TIME)
    private LocalTime arrivalTime;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Column(name = "ticket_price", nullable = false)
    private Integer ticketPrice;

    @Column(name = "train_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate trainDate;

    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL)
    private List<TrainStops> trainStops = new ArrayList<>();

    public Trains(){

    }

    public List<TrainStops> getTrainStops() {
        return trainStops;
    }

    public void setTrainStops(List<TrainStops> trainStops) {
        this.trainStops = trainStops;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
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

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    public Integer getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(Integer ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public LocalDate getTrainDate() {
        return trainDate;
    }

    public void setTrainDate(LocalDate trainDate) {
        this.trainDate = trainDate;
    }
}
