package com.example.fastrail.service;

import com.example.fastrail.dto.TrainStopsDTO;
import com.example.fastrail.model.Stations;
import com.example.fastrail.model.TrainStops;
import com.example.fastrail.model.Trains;
import com.example.fastrail.repository.StationsRepository;
import com.example.fastrail.repository.TrainStopsRepository;
import com.example.fastrail.repository.TrainsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TrainStopsService {

    @Autowired
    private TrainsRepository trainsRepo;

    @Autowired
    private StationsRepository stationsRepo;

    @Autowired
    private TrainStopsRepository trainStopsRepo;

    public List<TrainStops> addStopsByTrains(String trainNumber, List<TrainStopsDTO> dtoList){
        Trains train = trainsRepo.findByTrainNumberForUpdate(trainNumber)
                .orElseThrow(() -> new RuntimeException("找不到車次"));

        List<TrainStops> list = new ArrayList<>();
        for(TrainStopsDTO dto: dtoList){
            Stations station = stationsRepo.findById(dto.getStationId())
                    .orElseThrow(() -> new RuntimeException("找不到該停靠站資料"));
            TrainStops stop = new TrainStops();
            stop.setTrain(train);
            stop.setStation(station);
            stop.setArrivalTime(dto.getArrivalTime());
            stop.setDepartureTime(dto.getDepartureTime());
            stop.setStopSequence(dto.getStopSequence());

            list.add(trainStopsRepo.save(stop));
        }

        return list;
    }

    public List<TrainStops> getStopsByTrainNumber(String trainNumber){
        Trains train = trainsRepo.findByTrainNumberForUpdate(trainNumber)
                .orElseThrow(() -> new RuntimeException("找不到該車次"));

        return trainStopsRepo.findByTrainOrderByStopSequence(train);
    }
}
