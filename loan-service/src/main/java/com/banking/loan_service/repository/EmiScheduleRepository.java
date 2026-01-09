package com.banking.loan_service.repository;

import com.banking.loan_service.entity.EmiSchedule;
import com.banking.loan_service.enums.EmiStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmiScheduleRepository extends JpaRepository<EmiSchedule, Long> {

    List<EmiSchedule> findByLoanNumberOrderByEmiNumberAsc(String loanNumber);

    Optional<EmiSchedule> findByLoanNumberAndEmiNumber(String loanNumber, Integer emiNumber);

    List<EmiSchedule> findByLoanNumberAndStatus(String loanNumber, EmiStatus status);

    @Query("SELECT e FROM EmiSchedule e WHERE e.loanNumber = :loanNumber AND e.status = 'PENDING' ORDER BY e.emiNumber ASC")
    Optional<EmiSchedule> findNextPendingEmi(@Param("loanNumber") String loanNumber);

    @Query("SELECT e FROM EmiSchedule e WHERE e.loanId IN " +
            "(SELECT l.id FROM Loan l WHERE l.internalUserId = :userId) " +
            "AND e.status = 'OVERDUE' ORDER BY e.dueDate ASC")
    List<EmiSchedule> findOverdueEmisForUser(@Param("userId") String internalUserId);

    @Query("SELECT e FROM EmiSchedule e WHERE e.status = :status AND e.dueDate < :date")
    List<EmiSchedule> findOverdueEmis(@Param("status") EmiStatus status, @Param("date") LocalDate date);

    @Query("SELECT COUNT(e) FROM EmiSchedule e WHERE e.loanId IN " +
            "(SELECT l.id FROM Loan l WHERE l.internalUserId = :userId) AND e.status = 'OVERDUE'")
    int countOverdueEmisForUser(@Param("userId") String userId);
}