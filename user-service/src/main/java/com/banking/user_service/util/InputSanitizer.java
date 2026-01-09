
package com.banking.user_service.util;

import org.springframework.stereotype.Component;

@Component
public class InputSanitizer {

    public String sanitizeInput(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Remove HTML tags
        String sanitized = input.replaceAll("<[^>]*>", "");

        // Remove SQL injection patterns
        sanitized = sanitized.replaceAll("(?i)(SELECT|INSERT|UPDATE|DELETE|DROP|CREATE|ALTER|EXEC|EXECUTE|SCRIPT)", "");

        // Remove XSS patterns
        sanitized = sanitized.replaceAll("(?i)(javascript:|onerror=|onload=)", "");

        // Trim whitespace
        return sanitized.trim();
    }

    public String sanitizeEmailInput(String email) {
        if (email == null) return null;
        return email.toLowerCase().trim();
    }

    public String sanitizePhoneInput(String phone) {
        if (phone == null) return null;
        // Remove all non-digit characters
        return phone.replaceAll("[^0-9]", "");
    }
}