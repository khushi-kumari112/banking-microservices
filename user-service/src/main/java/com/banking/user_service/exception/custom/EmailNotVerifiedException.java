package com.banking.user_service.exception.custom;

public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException() {
        super("Email must be verified before proceeding with this action.");
    }

    public EmailNotVerifiedException(String operation) {
        super(String.format("Email verification required to perform: %s", operation));
    }
}
