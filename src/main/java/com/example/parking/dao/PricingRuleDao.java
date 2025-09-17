package com.example.parking.dao;

import com.example.parking.entity.PricingRule;
import com.example.parking.entity.enums.SlotType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PricingRuleDao extends JpaRepository<PricingRule, Long> {
	Optional<PricingRule> findByType(SlotType type);
}
