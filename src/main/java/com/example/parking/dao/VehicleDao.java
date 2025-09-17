package com.example.parking.dao;

import com.example.parking.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleDao extends JpaRepository<Vehicle, Long> {
	Optional<Vehicle> findByPlateNo(String plateNo);
}
