package com.banking.loan_service.service;

import com.banking.loan_service.client.AccountServiceClient;
import com.banking.loan_service.client.UserServiceClient;
import com.banking.loan_service.dto.*;
import com.banking.loan_service.entity.EmiSchedule;
import com.banking.loan_service.entity.Loan;
import com.banking.loan_service.enums.EmiStatus;
import com.banking.loan_service.enums.LoanStatus;
import com.banking.loan_service.exception.LoanNotFoundException;
import com.banking.loan_service.exception.LoanOperationException;
import com.banking.loan_service.kafka.LoanEventProducer;
import com.banking.loan_service.repository.EmiScheduleRepository;
import com.banking.loan_service.repository.LoanRepository;
import com.banking.loan_service.service.LoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final EmiScheduleRepository emiScheduleRepository;
    private final AccountServiceClient accountServiceClient;
    private final UserServiceClient userServiceClient;
    private final LoanEventProducer loanEventProducer;

    private static final int MIN_CREDIT_SCORE = 650;
    private static final double MAX_INCOME_RATIO = 0.5;
    private static final BigDecimal PENALTY_AMOUNT = BigDecimal.valueOf(500);
    private static final BigDecimal FORECLOSURE_CHARGE_PERCENT = BigDecimal.valueOf(0.01);

    @Override
    @CacheEvict(value = "loans", key = "#request.internalUserId")
    public LoanResponse applyForLoan(LoanApplicationRequest request) {
        log.info("Applying for loan | User: {} | Type: {} | Amount: {}",
                request.getInternalUserId(), request.getLoanType(), request.getLoanAmount());

        UserResponse user = getUserProfile(request.getInternalUserId());
        validateAccount(request.getAccountNumber());

        String loanNumber = generateLoanNumber();
        BigDecimal emiAmount = calculateEmiAmount(request.getLoanAmount(),
                request.getInterestRate(), request.getTenureMonths());
        BigDecimal totalAmount = emiAmount.multiply(BigDecimal.valueOf(request.getTenureMonths()));
        String creditRating = determineCreditRating(
                request.getCreditScore() != null ? request.getCreditScore() : user.getCreditScore());

        Loan loan = new Loan();
        loan.setLoanNumber(loanNumber);
        loan.setInternalUserId(request.getInternalUserId());
        loan.setAccountNumber(request.getAccountNumber());
        loan.setLoanType(request.getLoanType());
        loan.setLoanAmount(request.getLoanAmount());
        loan.setInterestRate(BigDecimal.valueOf(request.getInterestRate())); // Convert Double to BigDecimal
        loan.setTenureMonths(request.getTenureMonths());
        loan.setEmiAmount(emiAmount);
        loan.setTotalAmount(totalAmount);
        loan.setRemainingAmount(totalAmount);
        loan.setPaidEmis(0);
        loan.setRemainingEmis(request.getTenureMonths());
        loan.setStatus(LoanStatus.PENDING);
        loan.setPurpose(request.getPurpose());
        loan.setMonthlyIncome(request.getMonthlyIncome());
        loan.setExistingEmi(request.getExistingEmi());
        loan.setEmploymentType(request.getEmploymentType());
        loan.setEmployerName(request.getEmployerName());
        loan.setCreditScore(request.getCreditScore() != null ? request.getCreditScore() : user.getCreditScore());
        loan.setCreditRating(creditRating);
        loan.setApplicationDate(LocalDate.now());
        loan.setIsDisbursed(false);
        loan.setCreatedBy(request.getInternalUserId());

        Loan savedLoan = loanRepository.save(loan);
        log.info("Loan application created: {}", loanNumber);

        loanEventProducer.publishLoanApplied(loanNumber, request.getInternalUserId(),
                request.getLoanType().toString(), request.getLoanAmount());

        return mapToResponse(savedLoan);
    }

    @Override
    @Cacheable(value = "loans", key = "#loanNumber")
    public LoanResponse getLoanByNumber(String loanNumber) {
        log.info("Fetching loan: {}", loanNumber);
        Loan loan = findLoanByNumber(loanNumber);
        return mapToResponse(loan);
    }

    @Override
    @Cacheable(value = "user-loans", key = "#internalUserId")
    public List<LoanResponse> getLoansByUserId(String internalUserId) {
        log.info("Fetching all loans for user: {}", internalUserId);
        List<Loan> loans = loanRepository.findByInternalUserId(internalUserId);
        return loans.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<LoanResponse> getActiveLoansByUserId(String internalUserId) {
        log.info("Fetching active loans for user: {}", internalUserId);
        List<LoanStatus> activeStatuses = Arrays.asList(LoanStatus.ACTIVE, LoanStatus.OVERDUE);
        List<Loan> loans = loanRepository.findByInternalUserIdAndStatusIn(internalUserId, activeStatuses);
        return loans.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public LoanSummaryResponse getLoanSummary(String internalUserId) {
        log.info("Generating loan summary for user: {}", internalUserId);

        List<Loan> allLoans = loanRepository.findByInternalUserId(internalUserId);

        long totalLoans = allLoans.size();
        long activeLoans = allLoans.stream().filter(l -> l.getStatus() == LoanStatus.ACTIVE).count();
        long closedLoans = allLoans.stream().filter(l -> l.getStatus() == LoanStatus.CLOSED).count();

        BigDecimal totalOutstanding = allLoans.stream()
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE || l.getStatus() == LoanStatus.OVERDUE)
                .map(Loan::getRemainingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDisbursed = allLoans.stream()
                .filter(Loan::getIsDisbursed)
                .map(Loan::getLoanAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthlyEmi = allLoans.stream()
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE)
                .map(Loan::getEmiAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int overdueCount = emiScheduleRepository.countOverdueEmisForUser(internalUserId);

        BigDecimal totalPenalty = allLoans.stream()
                .map(Loan::getPenaltyAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return LoanSummaryResponse.builder()
                .totalLoans(totalLoans)
                .activeLoans(activeLoans)
                .closedLoans(closedLoans)
                .totalOutstandingAmount(totalOutstanding)
                .totalDisbursedAmount(totalDisbursed)
                .monthlyEmiAmount(monthlyEmi)
                .overdueEmisCount(overdueCount)
                .totalPenaltyAmount(totalPenalty)
                .build();
    }

    @Override
    public Page<LoanResponse> getPendingLoans(Pageable pageable) {
        log.info("Fetching pending loans");
        Page<Loan> loansPage = loanRepository.findByStatus(LoanStatus.PENDING, pageable);
        List<LoanResponse> responses = loansPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, loansPage.getTotalElements());
    }

    @Override
    @CacheEvict(value = "loans", key = "#loanNumber")
    public LoanResponse approveLoan(String loanNumber, LoanApprovalRequest request) {
        log.info("Approving loan: {}", loanNumber);

        Loan loan = findLoanByNumber(loanNumber);

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new LoanOperationException("Only PENDING loans can be approved");
        }

        loan.setStatus(LoanStatus.APPROVED);
        loan.setApprovalDate(LocalDate.now());
        loan.setApprovedBy(request.getApprovedBy());
        loan.setApprovalRemarks(request.getRemarks());
        loan.setUpdatedBy(request.getApprovedBy());

        generateEmiSchedule(loan);

        Loan savedLoan = loanRepository.save(loan);
        log.info("Loan approved: {}", loanNumber);

        loanEventProducer.publishLoanApproved(loanNumber, loan.getInternalUserId(),
                loan.getLoanAmount(), request.getApprovedBy());

        return mapToResponse(savedLoan);
    }

    @Override
    @CacheEvict(value = "loans", key = "#loanNumber")
    public LoanResponse rejectLoan(String loanNumber, LoanRejectionRequest request) {
        log.warn("Rejecting loan: {}", loanNumber);

        Loan loan = findLoanByNumber(loanNumber);

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new LoanOperationException("Only PENDING loans can be rejected");
        }

        loan.setStatus(LoanStatus.REJECTED);
        loan.setRejectionReason(request.getReason());
        loan.setUpdatedBy(request.getRejectedBy());

        Loan savedLoan = loanRepository.save(loan);
        log.info("Loan rejected: {}", loanNumber);

        loanEventProducer.publishLoanRejected(loanNumber, loan.getInternalUserId(),
                request.getReason(), request.getRejectedBy());

        return mapToResponse(savedLoan);
    }

    @Override
    @CacheEvict(value = "loans", key = "#loanNumber")
    public LoanResponse disburseLoan(String loanNumber, String adminId) {
        log.info("Disbursing loan: {}", loanNumber);

        Loan loan = findLoanByNumber(loanNumber);

        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new LoanOperationException("Only APPROVED loans can be disbursed");
        }

        TransactionRequest transactionRequest = TransactionRequest.builder()
                .accountNumber(loan.getAccountNumber())
                .amount(loan.getLoanAmount())
                .transactionType("CREDIT")
                .description("Loan disbursement - " + loanNumber)
                .build();

        ApiResponse<TransactionResponse> transactionResponse =
                accountServiceClient.processTransaction(transactionRequest);

        if (!transactionResponse.isSuccess()) {
            throw new LoanOperationException("Failed to disburse loan: " + transactionResponse.getMessage());
        }

        loan.setStatus(LoanStatus.ACTIVE);
        loan.setDisbursementDate(LocalDate.now());
        loan.setIsDisbursed(true);
        loan.setDisbursementTransactionId(transactionResponse.getData().getTransactionId());
        loan.setFirstEmiDate(LocalDate.now().plusMonths(1));
        loan.setNextEmiDueDate(LocalDate.now().plusMonths(1));
        loan.setLastEmiDate(LocalDate.now().plusMonths(loan.getTenureMonths()));

        Loan savedLoan = loanRepository.save(loan);
        log.info("Loan disbursed: {} | Transaction: {}", loanNumber, transactionResponse.getData().getTransactionId());

        loanEventProducer.publishLoanDisbursed(loanNumber, loan.getInternalUserId(),
                loan.getAccountNumber(), loan.getLoanAmount(), transactionResponse.getData().getTransactionId());

        return mapToResponse(savedLoan);
    }

    @Override
    @CacheEvict(value = "loans", key = "#loanNumber")
    public EmiPaymentResponse payEmi(String loanNumber, EmiPaymentRequest request) {
        log.info("Processing EMI payment for loan: {}", loanNumber);

        Loan loan = findLoanByNumber(loanNumber);

        if (!loan.canRepay()) {
            throw new LoanOperationException("Loan cannot accept payments in current status: " + loan.getStatus());
        }

        Optional<EmiSchedule> nextEmiOpt = emiScheduleRepository.findNextPendingEmi(loanNumber);
        if (nextEmiOpt.isEmpty()) {
            throw new LoanOperationException("No pending EMI found for loan: " + loanNumber);
        }

        EmiSchedule nextEmi = nextEmiOpt.get();

        if (nextEmi.isOverdue()) {
            nextEmi.applyPenalty(PENALTY_AMOUNT);
            loan.applyPenalty(PENALTY_AMOUNT);
        }

        BigDecimal totalPayment = nextEmi.getEmiAmount().add(nextEmi.getPenaltyAmount());

        if (request.getAmount().compareTo(totalPayment) < 0) {
            throw new LoanOperationException("Payment amount insufficient. Required: " + totalPayment);
        }

        TransactionRequest transactionRequest = TransactionRequest.builder()
                .accountNumber(request.getFromAccountNumber())
                .amount(totalPayment)
                .transactionType("DEBIT")
                .description("EMI payment - " + loanNumber + " - EMI #" + nextEmi.getEmiNumber())
                .build();

        ApiResponse<TransactionResponse> transactionResponse =
                accountServiceClient.processTransaction(transactionRequest);

        if (!transactionResponse.isSuccess()) {
            throw new LoanOperationException("EMI payment failed: " + transactionResponse.getMessage());
        }

        nextEmi.markAsPaid(totalPayment, transactionResponse.getData().getTransactionId());
        emiScheduleRepository.save(nextEmi);

        loan.makePayment(nextEmi.getEmiAmount());

        if (loan.getRemainingEmis() > 0) {
            loan.setNextEmiDueDate(loan.getNextEmiDueDate().plusMonths(1));
        } else {
            loan.setNextEmiDueDate(null);
        }

        Loan savedLoan = loanRepository.save(loan);
        log.info("EMI paid: {} | EMI #{} | Transaction: {}",
                loanNumber, nextEmi.getEmiNumber(), transactionResponse.getData().getTransactionId());

        loanEventProducer.publishEmiPaid(loanNumber, loan.getInternalUserId(),
                nextEmi.getEmiNumber(), nextEmi.getEmiAmount(), transactionResponse.getData().getTransactionId());

        return EmiPaymentResponse.builder()
                .loanNumber(loanNumber)
                .emiNumber(nextEmi.getEmiNumber())
                .paidAmount(totalPayment)
                .emiAmount(nextEmi.getEmiAmount())
                .penaltyAmount(nextEmi.getPenaltyAmount())
                .transactionId(transactionResponse.getData().getTransactionId())
                .paidDate(LocalDate.now())
                .remainingAmount(savedLoan.getRemainingAmount())
                .remainingEmis(savedLoan.getRemainingEmis())
                .nextEmiDueDate(savedLoan.getNextEmiDueDate())
                .build();
    }

    @Override
    public List<EmiScheduleResponse> getEmiSchedule(String loanNumber) {
        log.info("Fetching EMI schedule for loan: {}", loanNumber);
        List<EmiSchedule> schedules = emiScheduleRepository.findByLoanNumberOrderByEmiNumberAsc(loanNumber);
        return schedules.stream().map(this::mapToEmiResponse).collect(Collectors.toList());
    }

    @Override
    public EmiScheduleResponse getNextEmi(String loanNumber) {
        log.info("Fetching next EMI for loan: {}", loanNumber);
        Optional<EmiSchedule> nextEmi = emiScheduleRepository.findNextPendingEmi(loanNumber);
        if (nextEmi.isEmpty()) {
            throw new LoanNotFoundException("No pending EMI found for loan: " + loanNumber);
        }
        return mapToEmiResponse(nextEmi.get());
    }

    @Override
    @CacheEvict(value = "loans", key = "#loanNumber")
    public LoanResponse forecloseLoan(String loanNumber, String internalUserId) {
        log.info("Processing loan foreclosure: {}", loanNumber);

        Loan loan = findLoanByNumber(loanNumber);

        if (!loan.canRepay()) {
            throw new LoanOperationException("Loan cannot be foreclosed in current status");
        }

        BigDecimal foreclosureCharge = loan.getRemainingAmount().multiply(FORECLOSURE_CHARGE_PERCENT);
        BigDecimal totalAmount = loan.getRemainingAmount().add(foreclosureCharge);

        loan.setStatus(LoanStatus.FORECLOSED);
        loan.setClosureDate(LocalDate.now());
        loan.setRemainingAmount(BigDecimal.ZERO);
        loan.setRemainingEmis(0);

        Loan savedLoan = loanRepository.save(loan);
        log.info("Loan foreclosed: {}", loanNumber);

        loanEventProducer.publishLoanForeclosed(loanNumber, internalUserId, totalAmount, foreclosureCharge);

        return mapToResponse(savedLoan);
    }

    @Override
    public List<EmiScheduleResponse> getOverdueEmis(String internalUserId) {
        log.warn("Fetching overdue EMIs for user: {}", internalUserId);
        List<EmiSchedule> overdueEmis = emiScheduleRepository.findOverdueEmisForUser(internalUserId);
        return overdueEmis.stream().map(this::mapToEmiResponse).collect(Collectors.toList());
    }

    @Override
    public EmiCalculationResponse calculateEmi(BigDecimal loanAmount, Double interestRate, Integer tenureMonths) {
        log.info("Calculating EMI: Amount={}, Rate={}, Tenure={}", loanAmount, interestRate, tenureMonths);

        BigDecimal emiAmount = calculateEmiAmount(loanAmount, interestRate, tenureMonths);
        BigDecimal totalAmount = emiAmount.multiply(BigDecimal.valueOf(tenureMonths));
        BigDecimal totalInterest = totalAmount.subtract(loanAmount);
        BigDecimal monthlyRate = BigDecimal.valueOf(interestRate).divide(
                BigDecimal.valueOf(12 * 100), 6, RoundingMode.HALF_UP); // Fixed division

        return EmiCalculationResponse.builder()
                .loanAmount(loanAmount)
                .interestRate(interestRate)
                .tenureMonths(tenureMonths)
                .emiAmount(emiAmount)
                .totalInterest(totalInterest)
                .totalAmount(totalAmount)
                .monthlyInterestRate(monthlyRate)
                .build();
    }

    @Override
    public LoanEligibilityResponse checkEligibility(LoanEligibilityRequest request) {
        log.info("Checking loan eligibility for user: {}", request.getInternalUserId());

        boolean eligible = true;
        StringBuilder reason = new StringBuilder();

        Integer creditScore = request.getCreditScore();
        if (creditScore == null) {
            UserResponse user = getUserProfile(request.getInternalUserId());
            creditScore = user.getCreditScore();
        }

        String creditRating = determineCreditRating(creditScore);

        if (creditScore < MIN_CREDIT_SCORE) {
            eligible = false;
            reason.append("Credit score below minimum requirement (").append(MIN_CREDIT_SCORE).append("); ");
        }

        // Use getLoanType() method that exists (changed from simple enum)
        BigDecimal proposedEmi = calculateEmiAmount(request.getLoanAmount(), 10.0, request.getTenureMonths());

        BigDecimal totalMonthlyEmi = request.getExistingEmi().add(proposedEmi);
        double incomeRatio = totalMonthlyEmi.doubleValue() / request.getMonthlyIncome().doubleValue();

        if (incomeRatio > MAX_INCOME_RATIO) {
            eligible = false;
            reason.append(String.format("Income ratio (%.2f%%) exceeds maximum (%.0f%%); ",
                    incomeRatio * 100, MAX_INCOME_RATIO * 100));
        }

        BigDecimal maxMonthlyEmi = request.getMonthlyIncome().multiply(BigDecimal.valueOf(MAX_INCOME_RATIO))
                .subtract(request.getExistingEmi());

        BigDecimal maxEligibleAmount = calculateMaxLoanAmount(maxMonthlyEmi, 10.0, request.getTenureMonths());

        List<LoanStatus> activeStatuses = Arrays.asList(LoanStatus.ACTIVE, LoanStatus.OVERDUE);
        long activeLoansCount = loanRepository.countActiveLoans(request.getInternalUserId(), activeStatuses);

        if (eligible) {
            reason.append("Meets all eligibility criteria");
        }

        return LoanEligibilityResponse.builder()
                .eligible(eligible)
                .reason(reason.toString().trim())
                .creditScore(creditScore)
                .creditRating(creditRating)
                .maxEligibleAmount(maxEligibleAmount)
                .proposedEmi(proposedEmi)
                .totalMonthlyEmi(totalMonthlyEmi)
                .incomeRatio(incomeRatio)
                .activeLoansCount((int) activeLoansCount)
                .build();
    }

    private Loan findLoanByNumber(String loanNumber) {
        return loanRepository.findByLoanNumber(loanNumber)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found: " + loanNumber));
    }

    private UserResponse getUserProfile(String internalUserId) {
        ApiResponse<UserResponse> response = userServiceClient.getUserProfile(internalUserId);
        if (!response.isSuccess() || response.getData() == null) {
            throw new LoanOperationException("Failed to fetch user profile");
        }
        return response.getData();
    }

    private void validateAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 14) {
            throw new LoanOperationException("Invalid account number");
        }
    }

    private String generateLoanNumber() {
        String loanNumber;
        int attempts = 0;
        do {
            long timestamp = System.currentTimeMillis() / 1000;
            int random = (int) (Math.random() * 900000) + 100000;
            loanNumber = "LN" + timestamp + random;
            attempts++;
        } while (loanRepository.existsByLoanNumber(loanNumber) && attempts < 5);

        if (attempts >= 5) {
            throw new LoanOperationException("Failed to generate unique loan number");
        }
        return loanNumber;
    }

    private BigDecimal calculateEmiAmount(BigDecimal principal, Double annualRate, Integer months) {
        if (annualRate == 0) {
            return principal.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        }

        double monthlyRate = annualRate / (12 * 100);
        double numerator = principal.doubleValue() * monthlyRate * Math.pow(1 + monthlyRate, months);
        double denominator = Math.pow(1 + monthlyRate, months) - 1;

        double emi = numerator / denominator;
        return BigDecimal.valueOf(emi).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMaxLoanAmount(BigDecimal emi, Double annualRate, Integer months) {
        if (annualRate == 0) {
            return emi.multiply(BigDecimal.valueOf(months));
        }

        double monthlyRate = annualRate / (12 * 100);
        double denominator = monthlyRate * Math.pow(1 + monthlyRate, months);
        double numerator = Math.pow(1 + monthlyRate, months) - 1;

        double principal = emi.doubleValue() * (numerator / denominator);
        return BigDecimal.valueOf(principal).setScale(2, RoundingMode.HALF_UP);
    }

    private String determineCreditRating(Integer creditScore) {
        if (creditScore == null || creditScore < 300) return "UNKNOWN";
        if (creditScore >= 750) return "EXCELLENT";
        if (creditScore >= 700) return "GOOD";
        if (creditScore >= 650) return "FAIR";
        return "POOR";
    }

    private void generateEmiSchedule(Loan loan) {
        log.info("Generating EMI schedule for loan: {}", loan.getLoanNumber());

        List<EmiSchedule> schedules = new ArrayList<>();
        LocalDate emiDate = loan.getApprovalDate().plusMonths(1);
        BigDecimal remainingPrincipal = loan.getLoanAmount();
        double monthlyRate = loan.getInterestRate().doubleValue() / (12 * 100); // Convert BigDecimal to double

        for (int i = 1; i <= loan.getTenureMonths(); i++) {
            BigDecimal interestAmount = remainingPrincipal.multiply(BigDecimal.valueOf(monthlyRate));
            BigDecimal principalAmount = loan.getEmiAmount().subtract(interestAmount);
            remainingPrincipal = remainingPrincipal.subtract(principalAmount);

            EmiSchedule schedule = new EmiSchedule();
            schedule.setLoanId(loan.getId());
            schedule.setLoanNumber(loan.getLoanNumber());
            schedule.setEmiNumber(i);
            schedule.setEmiAmount(loan.getEmiAmount());
            schedule.setPrincipalAmount(principalAmount);
            schedule.setInterestAmount(interestAmount);
            schedule.setDueDate(emiDate);
            schedule.setStatus(EmiStatus.PENDING);
            schedule.setOutstandingBalance(remainingPrincipal.max(BigDecimal.ZERO));

            schedules.add(schedule);
            emiDate = emiDate.plusMonths(1);
        }

        emiScheduleRepository.saveAll(schedules);
        log.info("Generated {} EMI schedules", schedules.size());
    }

    private LoanResponse mapToResponse(Loan loan) {
        return LoanResponse.builder()
                .id(loan.getId())
                .loanNumber(loan.getLoanNumber())
                .internalUserId(loan.getInternalUserId())
                .accountNumber(loan.getAccountNumber())
                .loanType(loan.getLoanType())
                .loanAmount(loan.getLoanAmount())
                .interestRate(loan.getInterestRate().doubleValue()) // Convert BigDecimal to Double
                .tenureMonths(loan.getTenureMonths())
                .emiAmount(loan.getEmiAmount())
                .totalAmount(loan.getTotalAmount())
                .remainingAmount(loan.getRemainingAmount())
                .paidEmis(loan.getPaidEmis())
                .remainingEmis(loan.getRemainingEmis())
                .status(loan.getStatus())
                .purpose(loan.getPurpose())
                .monthlyIncome(loan.getMonthlyIncome())
                .creditScore(loan.getCreditScore())
                .creditRating(loan.getCreditRating())
                .applicationDate(loan.getApplicationDate())
                .approvalDate(loan.getApprovalDate())
                .disbursementDate(loan.getDisbursementDate())
                .nextEmiDueDate(loan.getNextEmiDueDate())
                .firstEmiDate(loan.getFirstEmiDate())
                .lastEmiDate(loan.getLastEmiDate())
                .isDisbursed(loan.getIsDisbursed())
                .missedEmis(loan.getMissedEmis())
                .penaltyAmount(loan.getPenaltyAmount())
                .lastPaymentDate(loan.getLastPaymentDate())
                .createdAt(loan.getCreatedAt())
                .updatedAt(loan.getUpdatedAt())
                .build();
    }

    private EmiScheduleResponse mapToEmiResponse(EmiSchedule emi) {
        return EmiScheduleResponse.builder()
                .id(emi.getId())
                .loanNumber(emi.getLoanNumber())
                .emiNumber(emi.getEmiNumber())
                .emiAmount(emi.getEmiAmount())
                .principalAmount(emi.getPrincipalAmount())
                .interestAmount(emi.getInterestAmount())
                .dueDate(emi.getDueDate())
                .status(emi.getStatus())
                .paidDate(emi.getPaidDate())
                .paidAmount(emi.getPaidAmount())
                .penaltyAmount(emi.getPenaltyAmount())
                .daysOverdue(emi.getDaysOverdue())
                .outstandingBalance(emi.getOutstandingBalance())
                .build();
    }
}