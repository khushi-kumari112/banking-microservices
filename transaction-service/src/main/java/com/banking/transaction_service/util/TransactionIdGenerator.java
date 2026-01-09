package com.banking.transaction_service.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class TransactionIdGenerator {

    private static final Random random = new Random();

    public static String generateTransactionId() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomNum = random.nextInt(9000) + 1000;
        return "TXN" + timestamp + randomNum;
    }

    public static String generateReferenceNumber() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomNum = random.nextInt(9000) + 1000;
        return "REF" + timestamp + randomNum;
    }
}