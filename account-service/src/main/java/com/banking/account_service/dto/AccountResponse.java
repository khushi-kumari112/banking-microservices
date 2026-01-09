package com.banking.account_service.dto;

import com.banking.account_service.enums.AccountStatus;
import com.banking.account_service.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for account operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {
    private Long accountId;
    private String accountNumber;
    private String internalUserId;
    private AccountType accountType;
    private BigDecimal balance;
    private AccountStatus status;
    private String ifscCode;
    private String branchCode;
    private String branchName;
    private String currency;
    private String createdDate;
    private String lastTransactionDate;
}