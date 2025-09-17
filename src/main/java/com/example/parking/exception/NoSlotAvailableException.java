package com.example.parking.exception;

public class NoSlotAvailableException extends RuntimeException {
	public NoSlotAvailableException(String message) { super(message); }
}
