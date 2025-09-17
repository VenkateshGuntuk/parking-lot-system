package com.example.parking.service.impl;

import com.example.parking.dao.PricingRuleDao;
import com.example.parking.entity.PricingRule;
import com.example.parking.entity.enums.SlotType;
import com.example.parking.service.interfaces.PricingService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
		long billable = Math.max(0, totalMinutes - rule.getFreeMinutes());
		BigDecimal hours = new BigDecimal(billable).divide(new BigDecimal("60"), 2, java.math.RoundingMode.UP);
		return hours.multiply(rule.getRatePerHour());
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
