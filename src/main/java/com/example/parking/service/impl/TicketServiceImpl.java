package com.example.parking.service.impl;

import com.example.parking.dao.TicketDao;
import com.example.parking.dao.VehicleDao;
import com.example.parking.dto.*;
import com.example.parking.entity.ParkingSlot;
import com.example.parking.entity.Ticket;
import com.example.parking.entity.Vehicle;
import com.example.parking.entity.enums.TicketStatus;
import com.example.parking.service.interfaces.AllocationService;
import com.example.parking.service.interfaces.PaymentService;
import com.example.parking.service.interfaces.PricingService;
import com.example.parking.service.interfaces.TicketService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
public class TicketServiceImpl implements TicketService {
	private final TicketDao ticketDao;
	private final VehicleDao vehicleDao;
    private final AllocationService allocationService;
	private final PricingService pricingService;
	private final PaymentService paymentService;

    public TicketServiceImpl(TicketDao ticketDao,
                             VehicleDao vehicleDao,
                             Map<String, AllocationService> allocationStrategies,
                             @Value("${app.allocation.strategy:nearestStrategy}") String strategyName,
                             PricingService pricingService,
                             PaymentService paymentService) {
        this.ticketDao = ticketDao;
        this.vehicleDao = vehicleDao;
        AllocationService chosen = allocationStrategies.get(strategyName);
        if (chosen == null) {
            // fallback to nearestStrategy if configured bean not found
            chosen = allocationStrategies.get("nearestStrategy");
        }
        if (chosen == null) {
            throw new IllegalStateException("No allocation strategy bean found for name: " + strategyName);
        }
        this.allocationService = chosen;
        this.pricingService = pricingService;
        this.paymentService = paymentService;
    }

	@Override
	@Transactional
	public EntryResponse enter(EntryRequest request) {
		List<Ticket> active = ticketDao.findActiveByPlate(request.plateNo());
		if (!active.isEmpty()) {
			throw new IllegalStateException("Vehicle already parked with active ticket");
		}
		Vehicle vehicle = vehicleDao.findByPlateNo(request.plateNo())
				.orElseGet(() -> {
					Vehicle v = new Vehicle();
					v.setPlateNo(request.plateNo());
					v.setType(request.vehicleType());
					v.setOwnerEmail(request.ownerEmail());
					return vehicleDao.save(v);
				});
        ParkingSlot slot = allocationService.allocate(request.parkingLotId(), request.gateId(), request.gateFloor(), request.vehicleType())
                .orElseThrow(() -> new com.example.parking.exception.NoSlotAvailableException(
                        "No available slots for type %s in lot %d".formatted(request.vehicleType(), request.parkingLotId())));
		Ticket t = new Ticket();
		t.setVehicle(vehicle);
		t.setSlot(slot);
		t.setEntryTime(OffsetDateTime.now());
		t.setStatus(TicketStatus.ACTIVE);
		t = ticketDao.save(t);
		return new EntryResponse(t.getId(), slot.getId(), slot.getFloor(), slot.getNumber(), vehicle.getPlateNo(), t.getEntryTime());
	}

	@Override
    @Transactional
    public ExitResponse payAndExit(ExitRequest request) {
        Ticket t = ticketDao.findById(request.ticketId()).orElseThrow(EntityNotFoundException::new);
        Duration duration = Duration.between(t.getEntryTime(), OffsetDateTime.now());
        BigDecimal amount = pricingService.calculateAmount(t.getVehicle().getType(), duration);
        try {
            paymentService.initiate(t, amount);
        } catch (com.example.parking.exception.PaymentFailedException ex) {
            return new ExitResponse(t.getId(), amount, "FAILED");
        }
        t.setStatus(TicketStatus.PAID);
        t.setExitTime(OffsetDateTime.now());
        ticketDao.save(t);
        allocationService.free(t.getSlot());
        return new ExitResponse(t.getId(), amount, "SUCCESS");
    }

	@Override
	@Transactional(readOnly = true)
	public ReceiptDto receipt(Long ticketId) {
		Ticket t = ticketDao.findById(ticketId).orElseThrow(EntityNotFoundException::new);
		Duration duration = Duration.between(t.getEntryTime(), t.getExitTime() == null ? OffsetDateTime.now() : t.getExitTime());
		BigDecimal amount = pricingService.calculateAmount(t.getVehicle().getType(), duration);
		return new ReceiptDto(t.getId(), t.getVehicle().getPlateNo(), t.getSlot().getId(), t.getEntryTime(), t.getExitTime(), amount);
	}
}
