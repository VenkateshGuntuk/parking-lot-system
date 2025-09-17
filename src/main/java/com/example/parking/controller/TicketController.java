package com.example.parking.controller;

import com.example.parking.dto.*;
import com.example.parking.service.interfaces.PricingService;
import com.example.parking.service.interfaces.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
	private final TicketService ticketService;
	private final PricingService pricingService;

	public TicketController(TicketService ticketService, PricingService pricingService) {
		this.ticketService = ticketService;
		this.pricingService = pricingService;
	}

	@PostMapping("/enter")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<EntryResponse> enter(@RequestBody @Valid EntryRequest request) {
		return ResponseEntity.ok(ticketService.enter(request));
	}

	@GetMapping("/amount/{ticketId}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<BigDecimal> preview(@PathVariable Long ticketId) {
		return ResponseEntity.ok(ticketService.receipt(ticketId).amount());
	}

	@PostMapping("/exit")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<ExitResponse> payAndExit(@RequestBody @Valid ExitRequest request) {
		return ResponseEntity.ok(ticketService.payAndExit(request));
	}

	@GetMapping("/receipt/{ticketId}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<ReceiptDto> receipt(@PathVariable Long ticketId) {
		return ResponseEntity.ok(ticketService.receipt(ticketId));
	}
}
