package com.banking.loan_service.controller;

import com.banking.loan_service.dto.*;
import com.banking.loan_service.service.LoanService;
import com.banking.loan_service.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
@Slf4j
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/apply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LoanResponse>> applyForLoan(
            @RequestHeader("internal-user-id") String internalUserId,
            @Valid @RequestBody LoanApplicationRequest request) {

        log.info("Loan application | User: {} | Type: {} | Amount: {}",
                internalUserId, request.getLoanType(), request.getLoanAmount());

        request.setInternalUserId(internalUserId);
        LoanResponse response = loanService.applyForLoan(request);

        return ResponseUtil.success("Loan application submitted successfully", response, HttpStatus.CREATED);
    }

    @GetMapping("/{loanNumber}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LoanResponse>> getLoan(@PathVariable String loanNumber) {
        log.info("Fetching loan: {}", loanNumber);
        LoanResponse loan = loanService.getLoanByNumber(loanNumber);
        return ResponseUtil.success("Loan retrieved successfully", loan);
    }

    @GetMapping("/user/{internalUserId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getUserLoans(@PathVariable String internalUserId) {
        log.info("Fetching loans for user: {}", internalUserId);
        List<LoanResponse> loans = loanService.getLoansByUserId(internalUserId);
        return ResponseUtil.success(String.format("Found %d loan(s)", loans.size()), loans);
    }

    @GetMapping("/user/{internalUserId}/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getActiveLoans(@PathVariable String internalUserId) {
        log.info("Fetching active loans for user: {}", internalUserId);
        List<LoanResponse> loans = loanService.getActiveLoansByUserId(internalUserId);
        return ResponseUtil.success(String.format("Found %d active loan(s)", loans.size()), loans);
    }

    @GetMapping("/user/{internalUserId}/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LoanSummaryResponse>> getLoanSummary(@PathVariable String internalUserId) {
        log.info("Fetching loan summary for user: {}", internalUserId);
        LoanSummaryResponse summary = loanService.getLoanSummary(internalUserId);
        return ResponseUtil.success("Loan summary retrieved successfully", summary);
    }

    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<LoanResponse>>> getPendingLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Admin: Fetching pending loans | Page: {} | Size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<LoanResponse> loans = loanService.getPendingLoans(pageable);
        return ResponseUtil.success(String.format("Found %d pending loan(s)", loans.getTotalElements()), loans);
    }

    @PostMapping("/admin/{loanNumber}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LoanResponse>> approveLoan(
            @PathVariable String loanNumber,
            @Valid @RequestBody LoanApprovalRequest request,
            @RequestHeader("internal-user-id") String adminId) {

        log.info("Admin approval | Loan: {} | Admin: {}", loanNumber, adminId);
        request.setApprovedBy(adminId);
        LoanResponse loan = loanService.approveLoan(loanNumber, request);
        return ResponseUtil.success("Loan approved successfully", loan);
    }

    @PostMapping("/admin/{loanNumber}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LoanResponse>> rejectLoan(
            @PathVariable String loanNumber,
            @Valid @RequestBody LoanRejectionRequest request,
            @RequestHeader("internal-user-id") String adminId) {

        log.warn("Admin rejection | Loan: {} | Admin: {} | Reason: {}", loanNumber, adminId, request.getReason());
        request.setRejectedBy(adminId);
        LoanResponse loan = loanService.rejectLoan(loanNumber, request);
        return ResponseUtil.success("Loan rejected", loan);
    }

    @PostMapping("/admin/{loanNumber}/disburse")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LoanResponse>> disburseLoan(
            @PathVariable String loanNumber,
            @RequestHeader("internal-user-id") String adminId) {

        log.info("Disbursing loan: {} | Admin: {}", loanNumber, adminId);
        LoanResponse loan = loanService.disburseLoan(loanNumber, adminId);
        return ResponseUtil.success("Loan disbursed successfully", loan);
    }

    @PostMapping("/{loanNumber}/pay-emi")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EmiPaymentResponse>> payEmi(
            @PathVariable String loanNumber,
            @Valid @RequestBody EmiPaymentRequest request,
            @RequestHeader("internal-user-id") String internalUserId) {

        log.info("EMI payment | Loan: {} | User: {} | Amount: {}", loanNumber, internalUserId, request.getAmount());
        request.setInternalUserId(internalUserId);
        EmiPaymentResponse response = loanService.payEmi(loanNumber, request);
        return ResponseUtil.success("EMI paid successfully", response);
    }

    @GetMapping("/{loanNumber}/emi-schedule")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<EmiScheduleResponse>>> getEmiSchedule(@PathVariable String loanNumber) {
        log.info("Fetching EMI schedule for loan: {}", loanNumber);
        List<EmiScheduleResponse> schedule = loanService.getEmiSchedule(loanNumber);
        return ResponseUtil.success("EMI schedule retrieved successfully", schedule);
    }

    @GetMapping("/{loanNumber}/next-emi")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EmiScheduleResponse>> getNextEmi(@PathVariable String loanNumber) {
        log.info("Fetching next EMI for loan: {}", loanNumber);
        EmiScheduleResponse emi = loanService.getNextEmi(loanNumber);
        return ResponseUtil.success("Next EMI retrieved successfully", emi);
    }

    @PostMapping("/{loanNumber}/foreclose")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LoanResponse>> forecloseLoan(
            @PathVariable String loanNumber,
            @RequestHeader("internal-user-id") String internalUserId) {

        log.info("Loan foreclosure | Loan: {} | User: {}", loanNumber, internalUserId);
        LoanResponse loan = loanService.forecloseLoan(loanNumber, internalUserId);
        return ResponseUtil.success("Loan foreclosed successfully", loan);
    }

    @GetMapping("/calculate-emi")
    public ResponseEntity<ApiResponse<EmiCalculationResponse>> calculateEmi(
            @RequestParam BigDecimal loanAmount,
            @RequestParam Double interestRate,
            @RequestParam Integer tenureMonths) {

        log.info("EMI calculation | Amount: {} | Rate: {}% | Tenure: {} months",
                loanAmount, interestRate, tenureMonths);

        EmiCalculationResponse calculation = loanService.calculateEmi(loanAmount, interestRate, tenureMonths);
        return ResponseUtil.success("EMI calculated successfully", calculation);
    }

    @PostMapping("/check-eligibility")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LoanEligibilityResponse>> checkEligibility(
            @RequestHeader("internal-user-id") String internalUserId,
            @Valid @RequestBody LoanEligibilityRequest request) {

        log.info("Eligibility check | User: {} | Type: {} | Amount: {}",
                internalUserId, request.getLoanType(), request.getLoanAmount());

        request.setInternalUserId(internalUserId);
        LoanEligibilityResponse response = loanService.checkEligibility(request);
        return ResponseUtil.success("Eligibility checked successfully", response);
    }

    @GetMapping("/user/{internalUserId}/overdue")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<EmiScheduleResponse>>> getOverdueEmis(@PathVariable String internalUserId) {
        log.warn("Fetching overdue EMIs for user: {}", internalUserId);
        List<EmiScheduleResponse> overdueEmis = loanService.getOverdueEmis(internalUserId);
        return ResponseUtil.success(String.format("Found %d overdue EMI(s)", overdueEmis.size()), overdueEmis);
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseUtil.success("Loan Service is running", "Service is healthy", HttpStatus.OK);
    }
}
