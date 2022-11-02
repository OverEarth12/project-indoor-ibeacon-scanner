package com.example.fingerprintmanager.rest;

import com.example.fingerprintmanager.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/savepos")
public class PositionController {
    @Autowired
    private PositionService positionService;

    @PostMapping
    public String savePosition(@RequestBody Position position) {
        Position p = positionService.savePosition(position);
        return "saved from room: "+p;
    }

}
