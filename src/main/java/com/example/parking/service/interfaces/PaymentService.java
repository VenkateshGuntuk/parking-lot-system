package com.example.parking.service.interfaces;

import com.example.parking.entity.Payment;
import com.example.parking.entity.Ticket;

import java.math.BigDecimal;

public interface PaymentService {
	Payment initiate(Ticket ticket, BigDecimal amount);
}
