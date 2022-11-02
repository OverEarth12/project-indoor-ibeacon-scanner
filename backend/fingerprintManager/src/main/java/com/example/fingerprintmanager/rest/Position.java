package com.example.fingerprintmanager.rest;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.GeneratedValue;
import java.util.List;

@Data
@Document("RadioMap")
public class Position {
    @Id
    @GeneratedValue
    private String _id;
    private String RoomId;
    private String scannerId;
    private Integer rssi;
    private List<Integer> pos;

    public Position(){}

    public Position(String roomId, String scannerId, Integer rssi, List<Integer> pos) {
        this.RoomId = roomId;
        this.scannerId = scannerId;
        this.rssi = rssi;
        this.pos = pos;
    }

    public Position(String _id, String roomId, String scannerId, Integer rssi, List<Integer> pos) {
        this._id = _id;
        this.RoomId = roomId;
        this.scannerId = scannerId;
        this.rssi = rssi;
        this.pos = pos;
    }

    public String getId() {
        return _id;
    }

    public String getRoomId() {
        return RoomId;
    }

    public String getScannerId() {
        return scannerId;
    }

    public Integer getRssi() {
        return rssi;
    }

    public List<Integer> getPos() {
        return pos;
    }
}
