package com.banking.transaction_service.enums;

public enum TransferMode {
    IMPS,  // Immediate Payment Service (Instant)
    NEFT,  // National Electronic Funds Transfer (Within hours)
    RTGS   // Real Time Gross Settlement (Real-time, high value)
}