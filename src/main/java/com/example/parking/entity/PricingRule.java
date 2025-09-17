package com.example.parking.entity;

import com.example.parking.entity.enums.SlotType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "pricing_rule")
@Getter
@Setter
@NoArgsConstructor
public class PricingRule {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private SlotType type;

	private Integer freeMinutes; // e.g., 120

	private BigDecimal ratePerHour; // e.g., 20.00
}
