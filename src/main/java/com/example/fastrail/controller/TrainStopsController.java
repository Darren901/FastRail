package com.example.fastrail.controller;

import com.example.fastrail.dto.TrainStopsDTO;
import com.example.fastrail.model.TrainStops;
import com.example.fastrail.service.TrainStopsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stops")
public class TrainStopsController {

    @Autowired
    private TrainStopsService trainStopsService;

    @PostMapping("/{trainNumber}")
    public ResponseEntity<?> addStopsByTrain(@PathVariable String trainNumber, @RequestBody List<TrainStopsDTO> dtoList){
        List<TrainStops> list = trainStopsService.addStopsByTrains(trainNumber, dtoList);
        return ResponseEntity.status(HttpStatus.CREATED).body(list);
    }

    @GetMapping("/{trainNumber}")
    public ResponseEntity<?> getStopsByTrain(@PathVariable String trainNumber) {
        List<TrainStops> stops = trainStopsService.getStopsByTrainNumber(trainNumber);
        return ResponseEntity.ok(stops);
    }

}
