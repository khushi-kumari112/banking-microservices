package com.banking.user_service.exception.custom;

public class InvalidEmailDomainException extends RuntimeException {
    public InvalidEmailDomainException(String message) {
        super(message);
    }

    public InvalidEmailDomainException(String email, String allowedDomains) {
        super(String.format("Email domain not allowed: %s. Allowed domains: %s", email, allowedDomains));
    }
}