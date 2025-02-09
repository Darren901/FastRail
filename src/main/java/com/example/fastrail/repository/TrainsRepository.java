package com.example.fastrail.repository;

import com.example.fastrail.model.Trains;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainsRepository extends JpaRepository<Trains, Integer> {

    boolean existsByTrainNumber(String trainNumber);

    Optional<Trains> findByTrainNumber(String trainNumber);

}
