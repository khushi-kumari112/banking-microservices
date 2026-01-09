// ═══════════════════════════════════════════════════════════════════════
// File: AppProperties.java
// ═══════════════════════════════════════════════════════════════════════
package com.banking.account_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Application Properties Configuration
 * Maps all business rules from application.properties
 */
@Data
@Component
@ConfigurationProperties(prefix = "account")
public class AppProperties {

    // Account Number Config
    private NumberConfig number = new NumberConfig();

    // IFSC Config
    private IfscConfig ifsc = new IfscConfig();

    // Account Type Limits
    private AccountLimits savings = new AccountLimits();
    private AccountLimits current = new AccountLimits();
    private AccountLimits salary = new AccountLimits();
    private AccountLimits wallet = new AccountLimits();

    // Account Limits
    private int maxPerUser;
    private int maxSavingsPerUser;
    private int maxCurrentPerUser;

    @Data
    public static class NumberConfig {
        private String prefix;
        private int length;
    }

    @Data
    public static class IfscConfig {
        private String bankCode;
        private int length;
    }

    @Data
    public static class AccountLimits {
        private BigDecimal minBalance;
        private BigDecimal maxBalance;
        private BigDecimal dailyWithdrawalLimit;
        private BigDecimal dailyTransactionLimit;
        private Integer monthlyTransactionLimit;
    }
}
