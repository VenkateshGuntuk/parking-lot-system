package com.example.parking.entity;

import com.example.parking.entity.enums.SlotType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehicle", uniqueConstraints = {
		@UniqueConstraint(name = "uk_vehicle_plate", columnNames = {"plate_no"})
})
@Getter
@Setter
@NoArgsConstructor
public class Vehicle {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "plate_no", nullable = false)
	private String plateNo;

	@Enumerated(EnumType.STRING)
	private SlotType type;

	private String ownerEmail;
}
