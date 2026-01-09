package com.banking.loan_service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String LOAN_TOPIC = "loan-events";

    public void publishLoanApplied(String loanNumber, String userId, String loanType, BigDecimal amount) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "LOAN_APPLIED");
        event.put("loanNumber", loanNumber);
        event.put("userId", userId);
        event.put("loanType", loanType);
        event.put("amount", amount);
        event.put("timestamp", LocalDateTime.now());

        kafkaTemplate.send(LOAN_TOPIC, loanNumber, event);
        log.info("📤 Published LOAN_APPLIED event: {}", loanNumber);
    }

    public void publishLoanApproved(String loanNumber, String userId, BigDecimal amount, String approvedBy) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "LOAN_APPROVED");
        event.put("loanNumber", loanNumber);
        event.put("userId", userId);
        event.put("amount", amount);
        event.put("approvedBy", approvedBy);
        event.put("timestamp", LocalDateTime.now());

        kafkaTemplate.send(LOAN_TOPIC, loanNumber, event);
        log.info("📤 Published LOAN_APPROVED event: {}", loanNumber);
    }

    public void publishLoanRejected(String loanNumber, String userId, String reason, String rejectedBy) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "LOAN_REJECTED");
        event.put("loanNumber", loanNumber);
        event.put("userId", userId);
        event.put("reason", reason);
        event.put("rejectedBy", rejectedBy);
        event.put("timestamp", LocalDateTime.now());

        kafkaTemplate.send(LOAN_TOPIC, loanNumber, event);
        log.info("📤 Published LOAN_REJECTED event: {}", loanNumber);
    }

    public void publishLoanDisbursed(String loanNumber, String userId, String accountNumber,
                                     BigDecimal amount, String transactionId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "LOAN_DISBURSED");
        event.put("loanNumber", loanNumber);
        event.put("userId", userId);
        event.put("accountNumber", accountNumber);
        event.put("amount", amount);
        event.put("transactionId", transactionId);
        event.put("timestamp", LocalDateTime.now());

        kafkaTemplate.send(LOAN_TOPIC, loanNumber, event);
        log.info("📤 Published LOAN_DISBURSED event: {}", loanNumber);
    }

    public void publishEmiPaid(String loanNumber, String userId, Integer emiNumber,
                               BigDecimal amount, String transactionId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "EMI_PAID");
        event.put("loanNumber", loanNumber);
        event.put("userId", userId);
        event.put("emiNumber", emiNumber);
        event.put("amount", amount);
        event.put("transactionId", transactionId);
        event.put("timestamp", LocalDateTime.now());

        kafkaTemplate.send(LOAN_TOPIC, loanNumber, event);
        log.info("📤 Published EMI_PAID event: {} - EMI #{}", loanNumber, emiNumber);
    }

    public void publishLoanForeclosed(String loanNumber, String userId, BigDecimal amount,
                                      BigDecimal foreclosureCharge) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "LOAN_FORECLOSED");
        event.put("loanNumber", loanNumber);
        event.put("userId", userId);
        event.put("amount", amount);
        event.put("foreclosureCharge", foreclosureCharge);
        event.put("timestamp", LocalDateTime.now());

        kafkaTemplate.send(LOAN_TOPIC, loanNumber, event);
        log.info("📤 Published LOAN_FORECLOSED event: {}", loanNumber);
    }
}
