package com.example.parking.service.interfaces;

import com.example.parking.entity.enums.SlotType;

import java.math.BigDecimal;
import java.time.Duration;

public interface PricingService {
	BigDecimal calculateAmount(SlotType type, Duration duration);
}
