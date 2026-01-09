package com.banking.account_service.util;

import com.banking.account_service.dto.AccountResponse;
import com.banking.account_service.entity.Account;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Account Mapper
 * Converts Account entity to AccountResponse DTO
 */
@Component
public class AccountMapper {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Map Account entity to AccountResponse DTO
     */
    public AccountResponse toResponse(Account account) {
        if (account == null) {
            return null;
        }

        return AccountResponse.builder()
                .accountId(account.getAccountId())
                .accountNumber(account.getAccountNumber())
                .internalUserId(account.getInternalUserId())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .status(account.getStatus())
                .ifscCode(account.getIfscCode())
                .branchCode(account.getBranchCode())
                .branchName(account.getBranchName())
                .currency(account.getCurrency())
                .createdDate(account.getCreatedDate() != null ?
                        account.getCreatedDate().format(DATE_FORMATTER) : null)
                .lastTransactionDate(account.getLastTransactionDate() != null ?
                        account.getLastTransactionDate().format(DATE_FORMATTER) : null)
                .build();
    }
}