package com.example.parking.dao;

import com.example.parking.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentDao extends JpaRepository<Payment, Long> {
	Optional<Payment> findByTicketId(Long ticketId);
}
