package com.example.fingerprintmanager;

import com.example.fingerprintmanager.rest.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PositionService {
    @Autowired
    private PositionRepository repository;

    public PositionService(PositionRepository repository){
        this.repository = repository;
    }
    public List<Position> getAllInRoom(String roomId){return repository.findByRoom(roomId); }
    public Position savePosition(Position position){
        return repository.save(position);
    }
}
