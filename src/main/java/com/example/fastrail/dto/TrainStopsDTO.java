package com.example.fastrail.dto;

import java.time.LocalTime;

public class TrainStopsDTO {

    private Integer trainsId;
    private Integer stationId;
    private LocalTime arrivalTime;
    private LocalTime departureTime;
    private Integer stopSequence;

    public Integer getTrainsId() {
        return trainsId;
    }

    public void setTrainsId(Integer trainsId) {
        this.trainsId = trainsId;
    }

    public Integer getStationId() {
        return stationId;
    }

    public void setStationId(Integer stationId) {
        this.stationId = stationId;
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
