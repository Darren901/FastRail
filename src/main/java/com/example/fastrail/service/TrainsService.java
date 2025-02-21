package com.example.fastrail.service;

import com.example.fastrail.dto.TrainsDTO;
import com.example.fastrail.model.Stations;
import com.example.fastrail.model.Trains;
import com.example.fastrail.repository.StationsRepository;
import com.example.fastrail.repository.TrainsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TrainsService {

    @Autowired
    private TrainsRepository trainsRepo;
    @Autowired
    private StationsRepository stationsRepo;

    @Transactional
    public List<Trains> findTrains(TrainsDTO trainsDTO){

        return trainsRepo.findTrains(
                trainsDTO.getDepartureStationId(),
                trainsDTO.getArrivalStationId(),
                trainsDTO.getTrainDate(),
                trainsDTO.getDepartureTime());
    }

    public List<Trains> findByTrainNumberAndRoute(TrainsDTO trainsDTO){
        return trainsRepo.findByTrainNumberAndRoute(
                trainsDTO.getTrainNumber(),
                trainsDTO.getDepartureStationId(),
                trainsDTO.getArrivalStationId(),
                trainsDTO.getTrainDate()
        );
    }

    public Page<Trains> findTrainsByPage(Integer pageNumber){
        int pageNum = pageNumber != null ? pageNumber - 1 : 0;
        PageRequest pgr = PageRequest.of(pageNum, 10,
                Sort.by("trainDate")
                        .descending()
                        .and(Sort.by("departureTime")));
        return trainsRepo.findAll(pgr);
    }

    public Trains findByTrainNumber(String trainNumber){
        return trainsRepo.findByTrainNumber(trainNumber)
                .orElseThrow(() -> new RuntimeException("找不到車次資料"));
    }

    public Trains getTrainById(Integer id){
        return trainsRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到車次"));
    }

    public Trains createTrain(TrainsDTO trainsDTO){
        if(trainsRepo.existsByTrainNumber(trainsDTO.getTrainNumber())){
            throw new RuntimeException("列車編號不能重複");
        }
        Stations departureStation = stationsRepo.findById(trainsDTO.getDepartureStationId())
                .orElseThrow(() -> new RuntimeException("出發站不能為空或錯誤的車站編號"));
        Stations arrivalStation = stationsRepo.findById(trainsDTO.getArrivalStationId())
                .orElseThrow(() -> new RuntimeException("抵達站不能為空或錯誤的車站編號"));

        Trains trains = new Trains();
        updateTrainsFromDTO(trains, trainsDTO, departureStation, arrivalStation);

        return trainsRepo.save(trains);
    }

    public Trains updateTrain(TrainsDTO trainsDTO){
        Trains updateTrain = trainsRepo.findById(trainsDTO.getId())
                .orElseThrow(() -> new RuntimeException("找不到車次"));

        if (!updateTrain.getTrainNumber().equals(trainsDTO.getTrainNumber()) &&
                trainsRepo.existsByTrainNumber(trainsDTO.getTrainNumber())) {
            throw new RuntimeException("列車編號不能重複");
        }

        Stations departureStation = stationsRepo.findById(trainsDTO.getDepartureStationId())
                .orElseThrow(() -> new RuntimeException("出發站不能為空或錯誤的車站編號"));
        Stations arrivalStation = stationsRepo.findById(trainsDTO.getArrivalStationId())
                .orElseThrow(() -> new RuntimeException("抵達站不能為空或錯誤的車站編號"));

        updateTrainsFromDTO(updateTrain, trainsDTO, departureStation, arrivalStation);

        return trainsRepo.save(updateTrain);
    }

    public void deleteTrains(String trainNumber){
        Trains trains = trainsRepo.findByTrainNumber(trainNumber)
                .orElseThrow(() -> new RuntimeException("找不到車次編號"));
        trainsRepo.delete(trains);
    }

    public List<Trains> createTrainsBatch(List<TrainsDTO> trainsDTOList) {
        List<Trains> savedTrains = new ArrayList<>();

        for(TrainsDTO dto : trainsDTOList) {

            if(trainsRepo.existsByTrainNumber(dto.getTrainNumber())) {
                throw new RuntimeException("列車編號 " + dto.getTrainNumber() + " 不能重複");
            }

            Stations departureStation = stationsRepo.findById(dto.getDepartureStationId())
                    .orElseThrow(() -> new RuntimeException("出發站不能為空或錯誤的車站編號: " + dto.getDepartureStationId()));
            Stations arrivalStation = stationsRepo.findById(dto.getArrivalStationId())
                    .orElseThrow(() -> new RuntimeException("抵達站不能為空或錯誤的車站編號: " + dto.getArrivalStationId()));

            Trains trains = new Trains();
            updateTrainsFromDTO(trains, dto, departureStation, arrivalStation);

            savedTrains.add(trainsRepo.save(trains));
        }

        return savedTrains;
    }

    public void updateTrainsFromDTO(Trains trains, TrainsDTO dto, Stations departureStation, Stations arrivalStation){
        trains.setTrainNumber(dto.getTrainNumber());
        trains.setDepartureStation(departureStation);
        trains.setArrivalStation(arrivalStation);
        trains.setDepartureTime(dto.getDepartureTime());
        trains.setArrivalTime(dto.getArrivalTime());
        trains.setAvailableSeats(dto.getAvailableSeats());
        trains.setTicketPrice(dto.getTicketPrice());
        trains.setTrainDate(dto.getTrainDate());
    }
}
