package com.example.parking.dao;

import com.example.parking.entity.ParkingSlot;
import com.example.parking.entity.enums.SlotType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParkingSlotDao extends JpaRepository<ParkingSlot, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ParkingSlot s where s.parkingLot.id = :lotId and s.type = :type and s.status = 'AVAILABLE' order by abs(s.floor - :gateFloor) asc, s.floor asc, s.number asc")
    List<ParkingSlot> findNearestByGate(@Param("lotId") Long parkingLotId, @Param("gateId") Long gateId, @Param("gateFloor") int gateFloor, @Param("type") SlotType type);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ParkingSlot s where s.parkingLot.id = :lotId and s.type = :type and s.status = 'AVAILABLE' order by s.floor asc, s.number asc")
    List<ParkingSlot> findLevelWise(@Param("lotId") Long parkingLotId, @Param("type") SlotType type);

    @Query("select s from ParkingSlot s where s.parkingLot.id = :lotId and s.type = :type and s.number= :number and s.status = 'AVAILABLE'")
    List<ParkingSlot> findSlotByParkingLotIdAndTypeAndNumber(@Param("lotId") Long parkingLotId, @Param("type") SlotType type, @Param("floor") Integer floor, @Param("number") Integer number);

    @Modifying
    @Query("update ParkingSlot s set s.status = 'OCCUPIED' where s.id = :id and s.status = 'AVAILABLE'")
    int markOccupiedIfAvailable(@Param("id") Long id);
}
