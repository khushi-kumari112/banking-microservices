package com.banking.loan_service.service;

import com.banking.loan_service.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * Loan Service Interface
 * Defines all loan-related business operations
 */
public interface LoanService {

    // Loan Application & Management
    LoanResponse applyForLoan(LoanApplicationRequest request);

    LoanResponse getLoanByNumber(String loanNumber);

    List<LoanResponse> getLoansByUserId(String internalUserId);

    List<LoanResponse> getActiveLoansByUserId(String internalUserId);

    LoanSummaryResponse getLoanSummary(String internalUserId);

    // Admin Operations
    Page<LoanResponse> getPendingLoans(Pageable pageable);

    LoanResponse approveLoan(String loanNumber, LoanApprovalRequest request);

    LoanResponse rejectLoan(String loanNumber, LoanRejectionRequest request);

    LoanResponse disburseLoan(String loanNumber, String adminId);

    // EMI Operations
    EmiPaymentResponse payEmi(String loanNumber, EmiPaymentRequest request);

    List<EmiScheduleResponse> getEmiSchedule(String loanNumber);

    EmiScheduleResponse getNextEmi(String loanNumber);

    LoanResponse forecloseLoan(String loanNumber, String internalUserId);

    List<EmiScheduleResponse> getOverdueEmis(String internalUserId);

    // Calculations & Eligibility
    EmiCalculationResponse calculateEmi(BigDecimal loanAmount, Double interestRate, Integer tenureMonths);

    LoanEligibilityResponse checkEligibility(LoanEligibilityRequest request);
}