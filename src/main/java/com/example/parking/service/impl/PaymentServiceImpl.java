package com.example.parking.service.impl;

import com.example.parking.dao.PaymentDao;
import com.example.parking.entity.Payment;
import com.example.parking.entity.Ticket;
import com.example.parking.exception.PaymentFailedException;
import com.example.parking.service.interfaces.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
public class PaymentServiceImpl implements PaymentService {
	private final PaymentDao paymentDao;

	public PaymentServiceImpl(PaymentDao paymentDao) {
		this.paymentDao = paymentDao;
	}

	@Override
	@Transactional
    public Payment initiate(Ticket ticket, BigDecimal amount) {
        Payment p = paymentDao.findByTicketId(ticket.getId()).orElse(new Payment());
        p.setAmount(amount);
        p.setStatus("INITIATED");
        p.setTimestamp(OffsetDateTime.now());
        p.setTicket(ticket);
        p = paymentDao.save(p);

        // Simulated gateway call; replace with real integration
        boolean gatewaySuccess = simulateGatewayCharge(ticket.getId(), amount);
        if (!gatewaySuccess) {
            p.setStatus("FAILED");
            paymentDao.save(p);
            throw new PaymentFailedException("Payment declined by gateway");
        }

        p.setStatus("SUCCESS");
        return paymentDao.save(p);
    }

    private boolean simulateGatewayCharge(Long ticketId, BigDecimal amount) {
        return true; // flip to false to simulate failures in tests
    }
}
