package com.example.parking.dto;

import java.math.BigDecimal;

public record ExitResponse(Long ticketId, BigDecimal amount, String status) {}
