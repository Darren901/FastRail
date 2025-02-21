package com.example.fastrail.controller;

import com.example.fastrail.dto.TrainsDTO;
import com.example.fastrail.dto.TrainsListDTO;
import com.example.fastrail.model.Trains;
import com.example.fastrail.service.TrainsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/trains")
public class TrainsController {

    @Autowired
    TrainsService trainsService;

    @GetMapping("/page/{pageNum}")
    public ResponseEntity<?> findByPage(@PathVariable Integer pageNum){
        Page<Trains> page = trainsService.findTrainsByPage(pageNum);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/trains")
    public ResponseEntity<List<Trains>> findTrains(
            @RequestParam Integer departureStationId,
            @RequestParam Integer arrivalStationId,
            @RequestParam LocalDate trainDate,
            @RequestParam(required = false) LocalTime departureTime
    ) {
        TrainsDTO trainsDTO = new TrainsDTO();
        trainsDTO.setDepartureStationId(departureStationId);
        trainsDTO.setArrivalStationId(arrivalStationId);
        trainsDTO.setTrainDate(trainDate);
        trainsDTO.setDepartureTime(departureTime);

        List<Trains> trains = trainsService.findTrains(trainsDTO);
        return ResponseEntity.ok(trains);
    }

    @GetMapping("/trains/byNumberAndRoute")
    public ResponseEntity<List<Trains>> findTrainsByNumberAndRoute(
            @RequestParam String trainNumber,
            @RequestParam Integer departureStationId,
            @RequestParam Integer arrivalStationId,
            @RequestParam LocalDate trainDate) {

        TrainsDTO trainsDTO = new TrainsDTO();
        trainsDTO.setDepartureStationId(departureStationId);
        trainsDTO.setArrivalStationId(arrivalStationId);
        trainsDTO.setTrainDate(trainDate);
        trainsDTO.setTrainNumber(trainNumber);

        List<Trains> trains = trainsService.findByTrainNumberAndRoute(trainsDTO);
        return ResponseEntity.ok(trains);
    }

    @GetMapping("/{trainNumber}")
    public ResponseEntity<?> findByTrainNumber(@PathVariable String trainNumber){
        Trains train = trainsService.findByTrainNumber(trainNumber);
        return ResponseEntity.ok(train);
    }

    @PostMapping("/batch")
    public ResponseEntity<?> createTrainsBatch(@RequestBody TrainsListDTO trainsListDTO) {
        List<Trains> savedTrains = trainsService.createTrainsBatch(trainsListDTO.getTrainsList());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTrains);
    }
}
