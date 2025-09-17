package com.example.parking.controller;

import com.example.parking.dao.ParkingLotDao;
import com.example.parking.dao.PricingRuleDao;
import com.example.parking.dao.ParkingSlotDao;
import com.example.parking.entity.ParkingLot;
import com.example.parking.entity.ParkingSlot;
import com.example.parking.entity.PricingRule;
import com.example.parking.entity.enums.SlotStatus;
import com.example.parking.entity.enums.SlotType;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
	private final ParkingSlotDao slotDao;
	private final PricingRuleDao pricingRuleDao;

	private final ParkingLotDao parkingLotDao;

	public AdminController(ParkingSlotDao slotDao, PricingRuleDao pricingRuleDao, ParkingLotDao parkingLotDao) {
		this.slotDao = slotDao;
		this.pricingRuleDao = pricingRuleDao;
		this.parkingLotDao = parkingLotDao;
	}

	@PostMapping("/slots")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ParkingSlot> addSlot(@RequestParam @NotNull Long parkingLotId,
											   @RequestParam @NotNull Integer floor,
												 @RequestParam @NotNull Integer number,
												 @RequestParam @NotNull SlotType type) {
		ParkingLot parkingLot = parkingLotDao.findById(parkingLotId)
				.orElseThrow(() -> new com.example.parking.exception.NoParkingLotAvailableException(
						"No available parking lot with id : %d"
								.formatted(parkingLotId)));
		List<ParkingSlot> parkingSlots = slotDao.findSlotByParkingLotIdAndTypeAndNumber(parkingLotId, type, floor, number);
		if(parkingSlots.size() > 0){
			throw new com.example.parking.exception.NoSlotAvailableException(
					"No available slots for type %s in lot %d".formatted(type, parkingLotId));
		}
		ParkingSlot slot = new ParkingSlot();
		slot.setFloor(floor);
		slot.setNumber(number);
		slot.setType(type);
		slot.setStatus(SlotStatus.AVAILABLE);
		slot.setParkingLot(parkingLot);
		return ResponseEntity.ok(slotDao.save(slot));
	}

	@GetMapping("/slots")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<ParkingSlot>> listSlots() {
		return ResponseEntity.ok(slotDao.findAll());
	}

	@DeleteMapping("/slots/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> removeSlot(@PathVariable Long id) {
		slotDao.deleteById(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/pricing")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<PricingRule> upsertPricing(@RequestParam SlotType type,
													   @RequestParam Integer freeMinutes,
													   @RequestParam BigDecimal ratePerHour) {
		PricingRule rule = pricingRuleDao.findByType(type).orElse(new PricingRule());
		rule.setType(type);
		rule.setFreeMinutes(freeMinutes);
		rule.setRatePerHour(ratePerHour);
		return ResponseEntity.ok(pricingRuleDao.save(rule));
	}

	@GetMapping("/pricing")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<PricingRule>> listPricing() {
		return ResponseEntity.ok(pricingRuleDao.findAll());
	}
}
