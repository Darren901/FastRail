package com.example.fastrail.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
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

    @Column(name = "train_type", nullable = false)
    private String trainType;

    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL)
    private List<TrainStops> trainStops = new ArrayList<>();

}
