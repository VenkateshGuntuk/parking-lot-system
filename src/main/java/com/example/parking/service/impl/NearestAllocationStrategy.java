package com.example.parking.service.impl;

import com.example.parking.dao.ParkingSlotDao;
import com.example.parking.entity.ParkingSlot;
import com.example.parking.entity.enums.SlotStatus;
import com.example.parking.entity.enums.SlotType;
import com.example.parking.service.interfaces.AllocationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service("nearestStrategy")
public class NearestAllocationStrategy implements AllocationService {
	private final ParkingSlotDao slotDao;

	public NearestAllocationStrategy(ParkingSlotDao slotDao) {
		this.slotDao = slotDao;
	}

	@Override
	@Transactional
	public Optional<ParkingSlot> allocate(Long parkingLotId, Long gateId, int gateFloor, SlotType type) {
        return slotDao.findNearestByGate(parkingLotId, gateId, gateFloor, type).stream().findFirst().flatMap(slot -> {
            int updated = slotDao.markOccupiedIfAvailable(slot.getId());
            if (updated == 1) {
                slot.setStatus(SlotStatus.OCCUPIED);
                return Optional.of(slot);
            }
            return Optional.empty();
        });
	}

	@Override
	@Transactional
	public void free(ParkingSlot slot) {
		slot.setStatus(SlotStatus.AVAILABLE);
		slotDao.save(slot);
	}
}
