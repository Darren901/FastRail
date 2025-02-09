package com.example.fastrail.controller;

import com.example.fastrail.service.TrainStopsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stops")
public class TrainStopsController {

    @Autowired
    private TrainStopsService trainStopsService;
}
