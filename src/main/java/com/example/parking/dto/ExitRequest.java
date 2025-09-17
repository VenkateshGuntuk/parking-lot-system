package com.example.parking.dto;

import jakarta.validation.constraints.NotNull;

public record ExitRequest(@NotNull Long ticketId) {}
