package com.banking.transaction_service.repository;

import com.banking.transaction_service.entity.TransactionAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionAuditRepository extends JpaRepository<TransactionAudit, Long> {
}