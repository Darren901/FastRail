package com.example.fastrail.service;

import com.example.fastrail.dto.StationsDTO;
import com.example.fastrail.model.Stations;
import com.example.fastrail.repository.StationsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StationsService {

    @Autowired
    private StationsRepository stationsRepo;

    public List<Stations> findAllStations(){
        return stationsRepo.findAll();
    }

    public Stations createStation(StationsDTO stationsDTO){
        if (stationsRepo.existsByStationName(stationsDTO.getStationName())) {
            throw new RuntimeException("站名已存在");
        }
        if (stationsRepo.existsByStationCode(stationsDTO.getStationCode())) {
            throw new RuntimeException("站名代號不能重複");
        }
        Stations station = new Stations();
        station.setStationName(stationsDTO.getStationName());
        station.setStationCode(stationsDTO.getStationCode());
        return stationsRepo.save(station);
    }

    public Stations updateStation(StationsDTO stationsDTO){
        Stations updateStation = stationsRepo.findById(stationsDTO.getId())
                .orElseThrow(() -> new RuntimeException("找不到此站名資訊"));

        if (!updateStation.getStationName().equals(stationsDTO.getStationName()) &&
                stationsRepo.existsByStationName(stationsDTO.getStationName())) {
            throw new RuntimeException("站名已存在");
        }
        if (!updateStation.getStationCode().equals(stationsDTO.getStationCode()) &&
                stationsRepo.existsByStationCode(stationsDTO.getStationCode())) {
            throw new RuntimeException("站名代號不能重複");
        }

        updateStation.setStationName(stationsDTO.getStationName());
        updateStation.setStationCode(stationsDTO.getStationCode());

        return stationsRepo.save(updateStation);

    }

    public void deleteStation(Integer id){
        Stations deleteStation = stationsRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到此站名資訊"));

        stationsRepo.delete(deleteStation);
    }
}
