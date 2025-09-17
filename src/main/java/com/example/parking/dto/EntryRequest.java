package com.example.parking.dto;

import com.example.parking.entity.enums.SlotType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EntryRequest(@NotBlank String plateNo,
                           @NotNull SlotType vehicleType,
                           @NotBlank String ownerEmail,
                           @NotNull Long parkingLotId,
                           @NotNull Long gateId,
                           @NotNull Integer gateFloor) {}
