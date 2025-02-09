package com.example.fastrail.model;

import jakarta.persistence.*;

@Entity
@Table(name = "stations")
public class Stations {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "station_name", nullable = false, unique = true)
    private String stationName;

    @Column(name = "station_code", nullable = false, unique = true)
    private String stationCode;

    public Stations() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getStationCode() {
        return stationCode;
    }

    public void setStationCode(String stationCode) {
        this.stationCode = stationCode;
    }
}
