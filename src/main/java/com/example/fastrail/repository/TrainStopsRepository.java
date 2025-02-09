package com.example.fastrail.repository;

import com.example.fastrail.model.Stations;
import com.example.fastrail.model.TrainStops;
import com.example.fastrail.model.Trains;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainStopsRepository extends JpaRepository<TrainStops, Integer> {

    List<TrainStops> findByTrainOrderByStopSequence(Trains train);

    boolean existsByTrainAndStation(Trains train, Stations station);
}
