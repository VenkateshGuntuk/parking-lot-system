package com.example.parking.exception;

public class NoParkingLotAvailableException extends RuntimeException {
	public NoParkingLotAvailableException(String message) { super(message); }
}
