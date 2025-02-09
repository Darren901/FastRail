package com.example.fastrail.repository;

import com.example.fastrail.model.Stations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StationsRepository extends JpaRepository<Stations, Integer> {

    boolean existsByStationCode(String stationCode);

    boolean existsByStationName(String stationName);

}
