package com.banking.account_service.repository;

import com.banking.account_service.entity.Account;
import com.banking.account_service.enums.AccountStatus;
import com.banking.account_service.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Account Repository
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Find account by account number
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Check if account number exists
     */
    boolean existsByAccountNumber(String accountNumber);

    /**
     * Find all accounts for a user
     */
    List<Account> findByInternalUserId(String internalUserId);

    /**
     * Find accounts by user ID and status
     */
    List<Account> findByInternalUserIdAndStatus(String internalUserId, AccountStatus status);

    /**
     * Find accounts by user ID and type
     */
    List<Account> findByInternalUserIdAndAccountType(String internalUserId, AccountType accountType);

    /**
     * Count active accounts for a user
     */
    long countByInternalUserIdAndStatus(String internalUserId, AccountStatus status);

    /**
     * Count accounts by type for a user
     */
    long countByInternalUserIdAndAccountType(String internalUserId, AccountType accountType);

    /**
     * Find all active accounts
     */
    List<Account> findByStatus(AccountStatus status);

    /**
     * Find dormant accounts (no transaction in X days)
     */
    @Query("SELECT a FROM Account a WHERE a.status = 'ACTIVE' " +
            "AND a.lastTransactionDate < :cutoffDate")
    List<Account> findDormantAccounts(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);

    /**
     * Get total balance for a user (all active accounts)
     */
    @Query("SELECT SUM(a.balance) FROM Account a " +
            "WHERE a.internalUserId = :userId AND a.status = 'ACTIVE'")
    java.math.BigDecimal getTotalBalanceForUser(@Param("userId") String internalUserId);
}