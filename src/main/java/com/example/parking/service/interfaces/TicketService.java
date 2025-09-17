package com.example.parking.service.interfaces;

import com.example.parking.dto.EntryRequest;
import com.example.parking.dto.EntryResponse;
import com.example.parking.dto.ExitRequest;
import com.example.parking.dto.ExitResponse;
import com.example.parking.dto.ReceiptDto;

public interface TicketService {
	EntryResponse enter(EntryRequest request);
	ExitResponse payAndExit(ExitRequest request);
	ReceiptDto receipt(Long ticketId);
}
