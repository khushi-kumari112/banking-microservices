package com.banking.user_service.exception.custom;

public class MPINMismatchException extends RuntimeException {

    public MPINMismatchException(String message) {
        super(message);
    }

    public MPINMismatchException() {
        super("MPIN and Confirm MPIN do not match");
    }
}
