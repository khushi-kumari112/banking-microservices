package com.banking.account_service.service;

import com.banking.account_service.entity.AccountAudit;
import com.banking.account_service.enums.AuditAction;
import com.banking.account_service.repository.AccountAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Account Audit Service
 * Logs all account operations for compliance
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountAuditService {

    private final AccountAuditRepository auditRepository;

    /**
     * Log account operation
     */
    @Transactional
    public void logAudit(
            String accountNumber,
            String internalUserId,
            AuditAction action,
            String description,
            String oldValue,
            String newValue,
            String performedBy,
            String ipAddress
    ) {
        try {
            AccountAudit audit = AccountAudit.builder()
                    .accountNumber(accountNumber)
                    .internalUserId(internalUserId)
                    .action(action)
                    .description(description)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .performedBy(performedBy != null ? performedBy : "SYSTEM")
                    .ipAddress(ipAddress)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditRepository.save(audit);
            log.info(" Audit logged: {} for account: {}", action, accountNumber);
        } catch (Exception e) {
            log.error(" Failed to log audit: {}", e.getMessage());
        }
    }

    /**
     * Log account creation
     */
    public void logAccountCreation(String accountNumber, String internalUserId, String accountType) {
        logAudit(
                accountNumber,
                internalUserId,
                AuditAction.CREATE_ACCOUNT,
                "Account created - Type: " + accountType,
                null,
                accountType,
                internalUserId,
                null
        );
    }

    /**
     * Log balance update
     */
    public void logBalanceUpdate(
            String accountNumber,
            String oldBalance,
            String newBalance,
            String operation
    ) {
        logAudit(
                accountNumber,
                null,
                AuditAction.UPDATE_BALANCE,
                "Balance " + operation,
                oldBalance,
                newBalance,
                "SYSTEM",
                null
        );
    }

    /**
     * Log status change
     */
    public void logStatusChange(
            String accountNumber,
            String oldStatus,
            String newStatus,
            String reason
    ) {
        logAudit(
                accountNumber,
                null,
                AuditAction.UPDATE_STATUS,
                "Status changed: " + reason,
                oldStatus,
                newStatus,
                "ADMIN",
                null
        );
    }
}

