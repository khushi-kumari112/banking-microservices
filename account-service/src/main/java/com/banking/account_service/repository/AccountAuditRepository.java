package com.banking.account_service.repository;

import com.banking.account_service.entity.AccountAudit;
import com.banking.account_service.enums.AuditAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Account Audit Repository
 */
@Repository
public interface AccountAuditRepository extends JpaRepository<AccountAudit, Long> {

    /**
     * Find all audits for an account
     */
    List<AccountAudit> findByAccountNumberOrderByTimestampDesc(String accountNumber);

    /**
     * Find all audits for a user
     */
    List<AccountAudit> findByInternalUserIdOrderByTimestampDesc(String internalUserId);

    /**
     * Find audits by action type
     */
    List<AccountAudit> findByAction(AuditAction action);

    /**
     * Find audits in date range
     */
    List<AccountAudit> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Find audits by IP address (fraud detection)
     */
    List<AccountAudit> findByIpAddress(String ipAddress);
}
