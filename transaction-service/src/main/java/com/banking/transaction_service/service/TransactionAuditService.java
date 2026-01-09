package com.banking.transaction_service.service;

import com.banking.transaction_service.entity.TransactionAudit;
import com.banking.transaction_service.enums.AuditAction;
import com.banking.transaction_service.repository.TransactionAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionAuditService {

    private final TransactionAuditRepository auditRepository;

    public void logTransactionInitiated(String transactionId, String performedBy) {
        createAuditLog(transactionId, AuditAction.TRANSACTION_INITIATED,
                "Transaction initiated", performedBy);
    }

    public void logTransactionCompleted(String transactionId, String performedBy) {
        createAuditLog(transactionId, AuditAction.TRANSACTION_COMPLETED,
                "Transaction completed", performedBy);
    }

    public void logTransactionFailed(String transactionId, String reason) {
        createAuditLog(transactionId, AuditAction.TRANSACTION_FAILED,
                "Failed: " + reason, "SYSTEM");
    }

    public void logTransactionReversed(String transactionId, String reason) {
        createAuditLog(transactionId, AuditAction.TRANSACTION_REVERSED,
                "Reversed: " + reason, "ADMIN");
    }

    private void createAuditLog(String transactionId, AuditAction action,
                                String description, String performedBy) {
        TransactionAudit audit = TransactionAudit.builder()
                .transactionId(transactionId)
                .action(action)
                .description(description)
                .performedBy(performedBy)
                .timestamp(LocalDateTime.now())
                .build();

        auditRepository.save(audit);
        log.debug(" Audit: {}", action);
    }
}
