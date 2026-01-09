package com.banking.loan_service.exception;

public class LoanOperationException extends RuntimeException {
    public LoanOperationException(String message) {
        super(message);
    }
}