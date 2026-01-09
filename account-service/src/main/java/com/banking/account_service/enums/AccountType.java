package com.banking.account_service.enums;

/**
 * Account Types - Real Banking Standards
 *
 * SAVINGS: Personal savings account (SBI, HDFC standard)
 * CURRENT: Business/commercial account (no interest, overdraft facility)
 * SALARY: Employee salary account (zero balance)
 * WALLET: Digital wallet (UPI, mobile banking)
 */
public enum AccountType {
    SAVINGS,    // Personal savings
    CURRENT,    // Business account
    SALARY,     // Salary account
    WALLET      // Digital wallet
}