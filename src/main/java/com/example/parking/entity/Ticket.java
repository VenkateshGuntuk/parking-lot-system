package com.example.parking.entity;

import com.example.parking.entity.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ticket", indexes = {
		@Index(name = "idx_ticket_vehicle_active", columnList = "vehicle_id,status")
})
@Getter
@Setter
@NoArgsConstructor
public class Ticket {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	private Vehicle vehicle;

	@ManyToOne(optional = false)
	private ParkingSlot slot;

	private OffsetDateTime entryTime;
	private OffsetDateTime exitTime;

	@Enumerated(EnumType.STRING)
	private TicketStatus status = TicketStatus.ACTIVE;

	@Version
	private Long version;
}
