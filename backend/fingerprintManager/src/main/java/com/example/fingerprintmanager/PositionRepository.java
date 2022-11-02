package com.example.fingerprintmanager;

import com.example.fingerprintmanager.rest.Position;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PositionRepository extends MongoRepository<Position, String> {
    @Query(value = "{roomId:'?0'}")
    public List<Position> findByRoom(String roomId);


}
