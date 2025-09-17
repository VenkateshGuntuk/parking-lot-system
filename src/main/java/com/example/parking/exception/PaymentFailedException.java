package com.example.parking.exception;

public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String message) { super(message); }
}

