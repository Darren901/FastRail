package com.example.fastrail.controller;

import com.example.fastrail.dto.StationsDTO;
import com.example.fastrail.model.Stations;
import com.example.fastrail.service.StationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class StationsController {

    private final StationsService stationsService;

    @GetMapping
    public ResponseEntity<?> getAllStations(){
        List<Stations> allStations = stationsService.findAllStations();
        return ResponseEntity.ok(allStations);
    }

}
