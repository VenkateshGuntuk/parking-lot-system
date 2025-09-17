package com.example.parking.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ReceiptDto(Long ticketId, String plateNo, Long slotId, OffsetDateTime entry, OffsetDateTime exit, BigDecimal amount) {}
