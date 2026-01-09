package com.banking.user_service.exception.custom;

public class InvalidMPINException extends RuntimeException {

    public InvalidMPINException(String message) {
        super(message);
    }

    public InvalidMPINException() {
        super("Invalid MPIN provided. Please try again.");
    }

    public InvalidMPINException(int remainingAttempts) {
        super(String.format("Invalid MPIN. You have %d attempt(s) remaining.", remainingAttempts));
    }
}
