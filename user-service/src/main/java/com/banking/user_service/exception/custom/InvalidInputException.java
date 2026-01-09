package com.banking.user_service.exception.custom;

public class InvalidInputException extends RuntimeException {

    public InvalidInputException(String message) {
        super(message);
    }

    public InvalidInputException(String field, String reason) {
        super(String.format("Invalid %s: %s", field, reason));
    }
}
