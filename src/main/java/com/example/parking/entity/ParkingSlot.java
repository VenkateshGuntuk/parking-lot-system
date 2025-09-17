package com.example.parking.entity;

import com.example.parking.entity.enums.SlotStatus;
import com.example.parking.entity.enums.SlotType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "parking_slot", indexes = {
		@Index(name = "idx_slot_floor_type_status", columnList = "floor,type,status")
})
@Getter
@Setter
@NoArgsConstructor
public class ParkingSlot {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Integer floor;

	private Integer number;

	@Enumerated(EnumType.STRING)
	private SlotType type;

	@Enumerated(EnumType.STRING)
	private SlotStatus status = SlotStatus.AVAILABLE;

	@Version
	private Long version;

    @ManyToOne(optional = false)
    private ParkingLot parkingLot;
}
