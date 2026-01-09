package com.banking.user_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;

/**
 * OTP Message Service
 * Manages all OTP-related messages from application.properties
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OtpMessageService {

    @Value("${app.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    // Email Verification Messages
    @Value("${otp.email.verification.subject}")
    private String emailVerificationSubject;

    @Value("${otp.email.verification.body}")
    private String emailVerificationBody;

    // MPIN Reset Email Messages
    @Value("${otp.email.mpin.reset.subject}")
    private String mpinResetSubject;

    @Value("${otp.email.mpin.reset.body}")
    private String mpinResetBody;

    // SMS Messages
    @Value("${otp.sms.phone.verification}")
    private String smsPhoneVerification;

    @Value("${otp.sms.mpin.reset}")
    private String smsMpinReset;

    // Console Messages
    @Value("${otp.console.border}")
    private String consoleBorder;

    @Value("${otp.console.email.header}")
    private String consoleEmailHeader;

    @Value("${otp.console.sms.header}")
    private String consoleSmsHeader;

    @Value("${otp.console.recipient.label}")
    private String consoleRecipientLabel;

    @Value("${otp.console.phone.label}")
    private String consolePhoneLabel;

    @Value("${otp.console.name.label}")
    private String consoleNameLabel;

    @Value("${otp.console.otp.label}")
    private String consoleOtpLabel;

    @Value("${otp.console.validity.label}")
    private String consoleValidityLabel;

    @Value("${otp.console.validity.value}")
    private String consoleValidityValue;

    @Value("${otp.console.mode.label}")
    private String consoleModeLabel;

    @Value("${otp.console.mode.value}")
    private String consoleModeValue;

    /**
     * Get Email Verification Subject
     */
    public String getEmailVerificationSubject() {
        return emailVerificationSubject;
    }

    /**
     * Get Email Verification Body
     * Parameters: {0} = name, {1} = otp, {2} = validity minutes
     */
    public String getEmailVerificationBody(String name, String otp) {
        return MessageFormat.format(emailVerificationBody, name, otp, otpExpiryMinutes);
    }

    /**
     * Get MPIN Reset Email Subject
     */
    public String getMpinResetSubject() {
        return mpinResetSubject;
    }

    /**
     * Get MPIN Reset Email Body
     * Parameters: {0} = name, {1} = otp, {2} = validity minutes
     */
    public String getMpinResetBody(String name, String otp) {
        return MessageFormat.format(mpinResetBody, name, otp, otpExpiryMinutes);
    }

    /**
     * Get Phone Verification SMS Message
     * Parameters: {0} = name, {1} = otp, {2} = validity minutes
     */
    public String getPhoneVerificationSms(String name, String otp) {
        return MessageFormat.format(smsPhoneVerification, name, otp, otpExpiryMinutes);
    }

    /**
     * Get MPIN Reset SMS Message
     * Parameters: {0} = name, {1} = otp, {2} = validity minutes
     */
    public String getMpinResetSms(String name, String otp) {
        return MessageFormat.format(smsMpinReset, name, otp, otpExpiryMinutes);
    }

    /**
     * Build Console Email OTP Display
     */
    public String buildConsoleEmailOtp(String email, String name, String otp, String purpose) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(consoleBorder).append("\n");
        sb.append(MessageFormat.format(consoleEmailHeader, purpose)).append("\n");
        sb.append(consoleBorder).append("\n");
        sb.append(consoleRecipientLabel).append(" ").append(email).append("\n");
        sb.append(consoleNameLabel).append(" ").append(name).append("\n");
        sb.append(consoleOtpLabel).append(" ").append(otp).append("\n");
        sb.append(consoleValidityLabel).append(" ").append(MessageFormat.format(consoleValidityValue, otpExpiryMinutes)).append("\n");
        sb.append(consoleModeLabel).append(" ").append(consoleModeValue).append("\n");
        sb.append(consoleBorder).append("\n");
        return sb.toString();
    }

    /**
     * Build Console SMS OTP Display
     */
    public String buildConsoleSmsOtp(String phone, String name, String otp, String purpose) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(consoleBorder).append("\n");
        sb.append(MessageFormat.format(consoleSmsHeader, purpose)).append("\n");
        sb.append(consoleBorder).append("\n");
        sb.append(consolePhoneLabel).append(" ").append(phone).append("\n");
        sb.append(consoleNameLabel).append(" ").append(name).append("\n");
        sb.append(consoleOtpLabel).append(" ").append(otp).append("\n");
        sb.append(consoleValidityLabel).append(" ").append(MessageFormat.format(consoleValidityValue, otpExpiryMinutes)).append("\n");
        sb.append(consoleModeLabel).append(" ").append(consoleModeValue).append("\n");
        sb.append(consoleBorder).append("\n");
        return sb.toString();
    }
}