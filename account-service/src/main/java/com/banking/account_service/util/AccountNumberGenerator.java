package com.banking.account_service.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Account Number Generator
 *
 * Real Banking Standard: 14-digit account number
 * Format: YYYYMMDD + 6 random digits
 * Example: 20251101123456
 *
 * Used by: SBI, HDFC, ICICI
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AccountNumberGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int RANDOM_DIGITS_LENGTH = 6;

    /**
     * Generate 14-digit account number
     * Format: YYYYMMDD (8) + Random (6) = 14 digits
     */
    public String generateAccountNumber() {
        // Get current date: YYYYMMDD (8 digits)
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Generate 6 random digits
        StringBuilder randomPart = new StringBuilder();
        for (int i = 0; i < RANDOM_DIGITS_LENGTH; i++) {
            randomPart.append(RANDOM.nextInt(10));
        }

        String accountNumber = datePart + randomPart.toString();

        log.debug("Generated account number: {}", accountNumber);
        return accountNumber;
    }

    /**
     * Validate account number format
     */
    public boolean isValidAccountNumber(String accountNumber) {
        return accountNumber != null &&
                accountNumber.matches("\\d{14}");
    }

    /**
     * Format account number for display
     * Example: 20251101123456 -> 2025-1101-123456
     */
    public String formatAccountNumber(String accountNumber) {
        if (!isValidAccountNumber(accountNumber)) {
            return accountNumber;
        }
        return accountNumber.substring(0, 4) + "-" +
                accountNumber.substring(4, 8) + "-" +
                accountNumber.substring(8);
    }
}

