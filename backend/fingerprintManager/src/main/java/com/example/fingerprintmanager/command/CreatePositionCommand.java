package com.example.fingerprintmanager.command;

import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.List;

@Builder
@Data
public class CreatePositionCommand {
    @TargetAggregateIdentifier
    private String RoomId;
    private String scannerId;
    private List<Integer> rssi;
    private List<Integer> pos;
}
