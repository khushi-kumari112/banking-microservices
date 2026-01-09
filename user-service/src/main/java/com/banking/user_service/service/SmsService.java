package com.banking.user_service.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * SMS Service for Phone OTP
 * Real-world banking systems use this for phone verification
 */
@Service
@Slf4j
public class SmsService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    /**
     * Send Phone OTP via SMS
     * Real-world: Used by HDFC, ICICI, SBI for phone verification
     */
    @Async
    public void sendPhoneOtp(String toPhoneNumber, String otp, String name) {
        try {
            log.info(" Sending SMS OTP to: {}", toPhoneNumber);

            String messageBody = String.format(
                    "Dear %s, Your OTP for phone verification is %s. " +
                            "Valid for 5 minutes. Do not share with anyone. -Banking App",
                    name, otp
            );

            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(fromPhoneNumber),
                    messageBody
            ).create();

            log.info(" SMS OTP sent successfully. SID: {}", message.getSid());

        } catch (Exception e) {
            log.error(" Failed to send SMS OTP to: {}. Error: {}", toPhoneNumber, e.getMessage());
            throw new RuntimeException("Failed to send SMS OTP: " + e.getMessage());
        }
    }

    /**
     * Send MPIN Reset OTP via SMS
     */
    @Async
    public void sendMpinResetSms(String toPhoneNumber, String otp, String name) {
        try {
            log.info(" Sending MPIN reset OTP via SMS to: {}", toPhoneNumber);

            String messageBody = String.format(
                    "Dear %s, Your OTP for MPIN reset is %s. " +
                            "Valid for 5 minutes. If you did not request this, contact support immediately. -Banking App",
                    name, otp
            );

            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(fromPhoneNumber),
                    messageBody
            ).create();

            log.info(" MPIN reset SMS sent successfully. SID: {}", message.getSid());

        } catch (Exception e) {
            log.error(" Failed to send MPIN reset SMS: {}", e.getMessage());
        }
    }
}
