package com.banking.user_service.repository;

import com.banking.user_service.entity.AuditLog;
import com.banking.user_service.enums.AuditAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByInternalUserId(String internalUserId);

    List<AuditLog> findByAction(AuditAction action);

    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<AuditLog> findByInternalUserIdAndTimestampBetween(String internalUserId, LocalDateTime start, LocalDateTime end);
}