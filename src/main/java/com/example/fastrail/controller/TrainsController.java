package com.example.fastrail.controller;

import com.example.fastrail.dto.TrainsListDTO;
import com.example.fastrail.model.Trains;
import com.example.fastrail.service.TrainsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/batch")
    public ResponseEntity<?> createTrainsBatch(@RequestBody TrainsListDTO trainsListDTO) {
        List<Trains> savedTrains = trainsService.createTrainsBatch(trainsListDTO.getTrainsList());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTrains);
    }
}
