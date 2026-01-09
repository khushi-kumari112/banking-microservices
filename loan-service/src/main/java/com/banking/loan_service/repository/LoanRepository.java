package com.banking.loan_service.repository;

import com.banking.loan_service.entity.Loan;
import com.banking.loan_service.enums.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    Optional<Loan> findByLoanNumber(String loanNumber);

    List<Loan> findByInternalUserId(String internalUserId);

    List<Loan> findByInternalUserIdAndStatus(String internalUserId, LoanStatus status);

    List<Loan> findByInternalUserIdAndStatusIn(String internalUserId, List<LoanStatus> statuses);

    Page<Loan> findByStatus(LoanStatus status, Pageable pageable);

    long countByInternalUserIdAndStatusIn(String internalUserId, List<LoanStatus> statuses);

    boolean existsByLoanNumber(String loanNumber);

    @Query("SELECT SUM(l.remainingAmount) FROM Loan l WHERE l.internalUserId = :userId AND l.status IN :statuses")
    BigDecimal getTotalOutstandingAmount(@Param("userId") String userId, @Param("statuses") List<LoanStatus> statuses);

    @Query("SELECT SUM(l.emiAmount) FROM Loan l WHERE l.internalUserId = :userId AND l.status = 'ACTIVE'")
    BigDecimal getTotalMonthlyEmi(@Param("userId") String userId);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.internalUserId = :userId AND l.status IN :statuses")
    long countActiveLoans(@Param("userId") String userId, @Param("statuses") List<LoanStatus> statuses);
}