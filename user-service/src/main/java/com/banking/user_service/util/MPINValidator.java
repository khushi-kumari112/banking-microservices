
package com.banking.user_service.util;

import com.banking.user_service.exception.custom.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@Slf4j
public class MPINValidator {

    private static final Pattern MPIN_PATTERN = Pattern.compile("^\\d{4}$|^\\d{6}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * Validate MPIN Format (4 or 6 digits)
     */
    public void validateMPIN(String mpin) {
        if (mpin == null || mpin.isEmpty()) {
            throw new InvalidInputException("MPIN", "MPIN cannot be empty");
        }

        if (!MPIN_PATTERN.matcher(mpin).matches()) {
            throw new InvalidInputException("MPIN", "MPIN must be 4 or 6 digits");
        }

        // Check for sequential numbers (e.g., 1234, 123456)
        if (isSequentialNumbers(mpin)) {
            throw new InvalidInputException("MPIN", "MPIN cannot be sequential numbers (e.g., 1234, 123456)");
        }

        // Check for repeated numbers (e.g., 1111, 000000)
        if (isRepeatedNumbers(mpin)) {
            throw new InvalidInputException("MPIN", "MPIN cannot be repeated numbers (e.g., 1111, 000000)");
        }
    }

    /**
     * Validate Phone Number (Indian format)
     */
    public void validatePhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            throw new InvalidInputException("Phone", "Phone number cannot be empty");
        }

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new InvalidInputException("Phone", "Invalid Indian phone number format");
        }
    }

    /**
     * Validate Email
     */
    public void validateEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new InvalidInputException("Email", "Email cannot be empty");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidInputException("Email", "Invalid email format");
        }
    }

    /**
     * Check if MPIN has sequential numbers
     */
    private boolean isSequentialNumbers(String mpin) {
        for (int i = 0; i < mpin.length() - 1; i++) {
            if (Integer.parseInt(String.valueOf(mpin.charAt(i + 1))) !=
                    Integer.parseInt(String.valueOf(mpin.charAt(i))) + 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if MPIN has all repeated numbers
     */
    private boolean isRepeatedNumbers(String mpin) {
        char firstChar = mpin.charAt(0);
        for (int i = 1; i < mpin.length(); i++) {
            if (mpin.charAt(i) != firstChar) {
                return false;
            }
        }
        return true;
    }
}
