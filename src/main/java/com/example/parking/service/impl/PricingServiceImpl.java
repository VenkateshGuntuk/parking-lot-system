package com.example.parking.service.impl;

import com.example.parking.dao.PricingRuleDao;
import com.example.parking.entity.PricingRule;
import com.example.parking.entity.enums.SlotType;
import com.example.parking.service.interfaces.PricingService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

@Service
public class PricingServiceImpl implements PricingService {
	private final PricingRuleDao pricingRuleDao;

	public PricingServiceImpl(PricingRuleDao pricingRuleDao) {
		this.pricingRuleDao = pricingRuleDao;
	}

	@Override
	public BigDecimal calculateAmount(SlotType type, Duration duration) {
		PricingRule rule = pricingRuleDao.findByType(type).orElseGet(() -> defaultRule(type));
		long totalMinutes = Math.max(0, duration.toMinutes());
		long billableMinutes = Math.max(0, totalMinutes - rule.getFreeMinutes());
		long billableHours = (long) Math.ceil(billableMinutes / 60.0);
		return rule.getRatePerHour().multiply(BigDecimal.valueOf(billableHours)).setScale(2, RoundingMode.HALF_UP);
	}

	private PricingRule defaultRule(SlotType type) {
		PricingRule r = new PricingRule();
		r.setType(type);
		r.setFreeMinutes(120);
		switch (type) {
			case BIKE -> r.setRatePerHour(new BigDecimal("10"));
			case CAR -> r.setRatePerHour(new BigDecimal("20"));
			case TRUCK -> r.setRatePerHour(new BigDecimal("40"));
		}
		return r;
	}
}
