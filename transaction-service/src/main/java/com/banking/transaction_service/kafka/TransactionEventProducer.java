package com.banking.transaction_service.kafka;

import com.banking.transaction_service.entity.Transaction;
import com.banking.transaction_service.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.transaction-completed}")
    private String transactionCompletedTopic;

    @Value("${kafka.topics.transaction-failed}")
    private String transactionFailedTopic;

    @Value("${kafka.topics.transaction-reversed}")
    private String transactionReversedTopic;

    public void publishTransactionCompleted(Transaction transaction) {
        TransactionCompletedEvent event = TransactionCompletedEvent.builder()
                .transactionId(transaction.getTransactionId())
                .fromAccountId(transaction.getFromAccountId())
                .toAccountId(transaction.getToAccountId())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType().name())
                .referenceNumber(transaction.getReferenceNumber())
                .completedAt(transaction.getCompletedDate())
                .build();

        kafkaTemplate.send(transactionCompletedTopic, transaction.getTransactionId(), event);
        log.info(" Published completed event: {}", transaction.getTransactionId());
    }

    public void publishTransactionFailed(Transaction transaction) {
        TransactionFailedEvent event = TransactionFailedEvent.builder()
                .transactionId(transaction.getTransactionId())
                .fromAccountId(transaction.getFromAccountId())
                .toAccountId(transaction.getToAccountId())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType().name())
                .failureReason(transaction.getFailureReason())
                .failedAt(transaction.getCreatedDate())
                .build();

        kafkaTemplate.send(transactionFailedTopic, transaction.getTransactionId(), event);
        log.info(" Published failed event: {}", transaction.getTransactionId());
    }

    public void publishTransactionReversed(Transaction transaction) {
        kafkaTemplate.send(transactionReversedTopic, transaction.getTransactionId(), transaction);
        log.info(" Published reversed event: {}", transaction.getTransactionId());
    }
}