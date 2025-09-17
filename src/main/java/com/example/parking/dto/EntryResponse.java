package com.example.parking.dto;

import java.time.OffsetDateTime;

public record EntryResponse(Long ticketId, Long slotId, int floor, int number, String plateNo, OffsetDateTime entryTime) {}
