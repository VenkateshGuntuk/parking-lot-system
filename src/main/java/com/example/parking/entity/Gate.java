package com.example.parking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "gate")
@Getter
@Setter
@NoArgsConstructor
public class Gate {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	private ParkingLot parkingLot;

	private Integer floor; // floor where this gate is located (0-based)

	@Column(length = 16)
	private String type; // ENTRY or EXIT (optional for now)
}
