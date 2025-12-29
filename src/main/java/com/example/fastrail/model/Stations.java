package com.example.fastrail.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "stations")
@Data
public class Stations {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "station_name", nullable = false)
    private String stationName;

    @Column(name = "station_code", nullable = false)
    private String stationCode;
}
