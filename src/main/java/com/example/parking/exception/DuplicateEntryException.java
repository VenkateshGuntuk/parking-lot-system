package com.example.parking.exception;

public class DuplicateEntryException extends RuntimeException {
	public DuplicateEntryException(String message) { super(message); }
}
