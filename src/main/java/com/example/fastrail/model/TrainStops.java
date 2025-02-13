package com.example.fastrail.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "train_stops")
public class TrainStops {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "trains_id")
    @JsonIgnore
    private Trains train;

    @ManyToOne
    @JoinColumn(name = "station_id")
    @JsonIgnore
    private Stations station;

    @Column(name = "arrival_time")
    private LocalTime arrivalTime;

    @Column(name = "departure_time")
    private LocalTime departureTime;

    @Column(name = "stop_sequence", nullable = false)
    private Integer stopSequence;

    @JsonProperty("trainNumber")
    public String fetchTrainNumber(){
        return train.getTrainNumber();
    }

    @JsonProperty("stationName")
    public String fetchStationName(){
        return station.getStationName();
    }

    @JsonProperty("stationId")
    public Integer fetchStationId(){
        return station.getId();
    }

    public TrainStops(){}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Trains getTrain() {
        return train;
    }

    public void setTrain(Trains train) {
        this.train = train;
    }

    public Stations getStation() {
        return station;
    }

    public void setStation(Stations station) {
        this.station = station;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public Integer getStopSequence() {
        return stopSequence;
    }

    public void setStopSequence(Integer stopSequence) {
        this.stopSequence = stopSequence;
    }
}
