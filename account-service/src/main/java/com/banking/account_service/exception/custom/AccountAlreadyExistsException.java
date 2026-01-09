package com.banking.account_service.exception.custom;

/**
 * Exception thrown when account already exists
 */
public class AccountAlreadyExistsException extends RuntimeException {
    public AccountAlreadyExistsException(String message) {
        super(message);
    }
}