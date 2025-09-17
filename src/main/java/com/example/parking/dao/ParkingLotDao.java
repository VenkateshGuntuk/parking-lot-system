package com.example.parking.dao;

import com.example.parking.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkingLotDao extends JpaRepository<ParkingLot, Long> {
}
