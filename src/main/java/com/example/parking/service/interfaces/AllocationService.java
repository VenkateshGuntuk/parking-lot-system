package com.example.parking.service.interfaces;

import com.example.parking.entity.ParkingSlot;
import com.example.parking.entity.enums.SlotType;

import java.util.Optional;

public interface AllocationService {
    Optional<ParkingSlot> allocate(Long parkingLotId, Long gateId, int gateFloor, SlotType type);
    void free(ParkingSlot slot);
}
