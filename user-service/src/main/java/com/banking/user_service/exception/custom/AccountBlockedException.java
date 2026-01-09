package com.banking.user_service.exception.custom;

import com.banking.user_service.enums.UserStatus;

public class AccountBlockedException extends RuntimeException {

    public AccountBlockedException(String message) {
        super(message);
    }

    public AccountBlockedException(UserStatus status) {
        super(String.format("Account is %s. Please contact support.", status.name()));
    }
}

