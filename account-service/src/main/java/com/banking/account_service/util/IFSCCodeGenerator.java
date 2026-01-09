// ═══════════════════════════════════════════════════════════════════════
// File: IFSCCodeGenerator.java
// ═══════════════════════════════════════════════════════════════════════
package com.banking.account_service.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * IFSC Code Generator
 *
 * Real Banking Standard: 11-character IFSC code
 * Format: BANK(4) + 0 + BRANCH(6)
 * Example: SBIN0001234, HDFC0000123
 *
 * IFSC = Indian Financial System Code
 */
@Component
@Slf4j
public class IFSCCodeGenerator {

    private static final String BANK_CODE = "BNKL"; // Your bank code
    private static final String FILLER = "0";
    private static final int IFSC_LENGTH = 11;

    /**
     * Generate IFSC code from branch code
     * Format: BNKL + 0 + BRANCH_CODE (padded to 6 digits)
     */
    public String generateIFSCCode(String branchCode) {
        if (branchCode == null || branchCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Branch code cannot be empty");
        }

        // Remove non-alphanumeric characters
        String cleanBranchCode = branchCode.replaceAll("[^A-Za-z0-9]", "");

        // Pad or trim to 6 characters
        String paddedBranchCode = String.format("%-6s", cleanBranchCode)
                .substring(0, 6)
                .replace(' ', '0')
                .toUpperCase();

        String ifscCode = BANK_CODE + FILLER + paddedBranchCode;

        log.debug("Generated IFSC code: {} for branch: {}", ifscCode, branchCode);
        return ifscCode;
    }

    /**
     * Validate IFSC code format
     */
    public boolean isValidIFSCCode(String ifscCode) {
        return ifscCode != null &&
                ifscCode.matches("[A-Z]{4}0[A-Z0-9]{6}") &&
                ifscCode.length() == IFSC_LENGTH;
    }

    /**
     * Extract branch code from IFSC
     */
    public String extractBranchCode(String ifscCode) {
        if (!isValidIFSCCode(ifscCode)) {
            return null;
        }
        return ifscCode.substring(5); // Skip BANK(4) + 0(1)
    }

    /**
     * Format IFSC code for display
     * Example: BNKL0001234 -> BNKL-0-001234
     */
    public String formatIFSCCode(String ifscCode) {
        if (!isValidIFSCCode(ifscCode)) {
            return ifscCode;
        }
        return ifscCode.substring(0, 4) + "-" +
                ifscCode.substring(4, 5) + "-" +
                ifscCode.substring(5);
    }
}

