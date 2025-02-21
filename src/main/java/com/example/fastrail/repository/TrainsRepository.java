package com.example.fastrail.repository;

import com.example.fastrail.model.Stations;
import com.example.fastrail.model.Trains;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainsRepository extends JpaRepository<Trains, Integer> {

    boolean existsByTrainNumber(String trainNumber);

    Optional<Trains> findByTrainNumber(String trainNumber);

    @Query("SELECT t FROM Trains t " +
            "JOIN t.trainStops ts1 " +
            "JOIN t.trainStops ts2 " +
            "WHERE t.trainNumber = :trainNumber " +
            "AND ts1.station.id = :depStationId " +
            "AND ts2.station.id = :arrStationId " +
            "AND ts1.stopSequence < ts2.stopSequence " +
            "AND t.trainDate = :date")
    List<Trains> findByTrainNumberAndRoute(
            @Param("trainNumber") String trainNumber,
            @Param("depStationId") Integer depStationId,
            @Param("arrStationId") Integer arrStationId,
            @Param("date") LocalDate date
    );

    @Query(value = "SELECT DISTINCT t.* FROM trains t " +
            "INNER JOIN train_stops ts1 ON t.id = ts1.trains_id " +
            "INNER JOIN train_stops ts2 ON t.id = ts2.trains_id " +
            "WHERE ts1.station_id = :depStationId " +
            "AND ts2.station_id = :arrStationId " +
            "AND ts1.stop_sequence < ts2.stop_sequence " +
            "AND t.train_date = :date " +
            "AND CAST(CONVERT(VARCHAR, ts1.departure_time, 108) AS time) >= " +
            "CAST(CONVERT(VARCHAR, :time, 108) AS time)",
            nativeQuery = true)
    List<Trains> findTrains(
            @Param("depStationId") Integer depStationId,
            @Param("arrStationId") Integer arrStationId,
            @Param("date") LocalDate date,
            @Param("time") LocalTime time
    );




}
