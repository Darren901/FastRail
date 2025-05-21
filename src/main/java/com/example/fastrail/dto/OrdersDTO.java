package com.example.fastrail.dto;


import lombok.Data;

import java.time.LocalTime;

@Data
public class OrdersDTO {

    private Integer userId;
    private String trainNumber;
    private Integer departureStationId;
    private Integer arrivalStationId;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private String twId;
    private String clientOrderId;
}
