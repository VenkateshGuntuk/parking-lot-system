package com.example.parking.dao;

import com.example.parking.entity.Gate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GateDao extends JpaRepository<Gate, Long> {
}
