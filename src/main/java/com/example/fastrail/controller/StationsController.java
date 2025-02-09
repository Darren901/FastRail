package com.example.fastrail.controller;

import com.example.fastrail.dto.StationsDTO;
import com.example.fastrail.model.Stations;
import com.example.fastrail.service.StationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
public class StationsController {

    @Autowired
    private StationsService stationsService;

    @GetMapping
    public ResponseEntity<?> getAllStations(){
        List<Stations> allStations = stationsService.findAllStations();
        return ResponseEntity.ok(allStations);
    }

    @PostMapping
    public ResponseEntity<?> createStations(@RequestBody StationsDTO stationsDTO){
        Stations station = stationsService.createStation(stationsDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(station);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStations(@PathVariable Integer id,
                                            @RequestBody StationsDTO stationsDTO){
        stationsDTO.setId(id);
        Stations station = stationsService.updateStation(stationsDTO);
        return ResponseEntity.status(HttpStatus.OK).body(station);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStations(@PathVariable Integer id){
        stationsService.deleteStation(id);
        return ResponseEntity.noContent().build();
    }

}
