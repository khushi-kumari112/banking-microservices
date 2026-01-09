package com.banking.transaction_service.repository;

import com.banking.transaction_service.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionId(String transactionId);
    Optional<Transaction> findByReferenceNumber(String referenceNumber);
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    Page<Transaction> findByFromAccountIdOrToAccountIdOrderByCreatedDateDesc(
            Long fromAccountId, Long toAccountId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.totalAmount), 0) FROM Transaction t WHERE " +
            "t.fromAccountId = :accountId " +
            "AND t.status = 'SUCCESS' " +
            "AND t.createdDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateDailyDebitTotal(
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}