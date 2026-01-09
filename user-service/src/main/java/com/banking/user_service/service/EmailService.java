package com.banking.user_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:Banking App <noreply@bankingapp.com>}")
    private String fromEmail;

    @Value("${notification.mode:CONSOLE}")
    private String notificationMode;

    @Value("${spring.mail.enabled:false}")
    private boolean mailEnabled;

    @Async
    public void sendOtpEmail(String toEmail, String otp, String name) {
        // Development Mode: Console logging
        if ("CONSOLE".equalsIgnoreCase(notificationMode) || !mailEnabled) {
            logOtpToConsole(toEmail, otp, name, "EMAIL VERIFICATION");
            return;
        }

        // Production Mode: Real email
        try {
            log.info(" Sending email verification OTP to: {}", toEmail);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Email Verification - Banking App");
            message.setText(buildOtpEmailText(name, otp, "Email Verification"));
            mailSender.send(message);
            log.info(" Email verification OTP sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error(" Failed to send email OTP to: {}. Error: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendMpinResetOtp(String toEmail, String otp, String name) {
        // Development Mode: Console logging
        if ("CONSOLE".equalsIgnoreCase(notificationMode) || !mailEnabled) {
            logOtpToConsole(toEmail, otp, name, "MPIN RESET");
            return;
        }

        // Production Mode: Real email
        try {
            log.info(" Sending MPIN reset OTP to: {}", toEmail);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("MPIN Reset Request - Banking App");
            message.setText(buildMpinResetEmailText(name, otp));
            mailSender.send(message);
            log.info(" MPIN reset OTP sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error(" Failed to send MPIN reset OTP to: {}. Error: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendPhoneOtp(String phoneNumber, String otp, String name) {
        // Development Mode: Console logging
        if ("CONSOLE".equalsIgnoreCase(notificationMode) || !mailEnabled) {
            logSmsOtpToConsole(phoneNumber, otp, name, "PHONE VERIFICATION");
            return;
        }

        // Production Mode: SMS gateway (placeholder)
        log.info(" Sending SMS OTP to: {}", phoneNumber);
        String smsContent = String.format(
                "Dear %s, Your OTP for phone verification is %s. " +
                        "Valid for 5 minutes. Do not share with anyone. -Banking App",
                name, otp
        );
        log.info(" SMS Content: {}", smsContent);
    }

    private void logOtpToConsole(String email, String otp, String name, String purpose) {
        String border = "═══════════════════════════════════════════════════════════";
        System.out.println("\n" + border);
        System.out.println("📧 EMAIL OTP NOTIFICATION - " + purpose);
        System.out.println(border);
        System.out.println("│ Recipient : " + email);
        System.out.println("│ Name      : " + name);
        System.out.println("│ OTP       : " + otp);
        System.out.println("│ Valid For : 5 minutes");
        System.out.println("│ Mode      : CONSOLE (Development/Testing)");
        System.out.println(border + "\n");
        log.info(" Console OTP logged for: {}", email);
    }

    private void logSmsOtpToConsole(String phone, String otp, String name, String purpose) {
        String border = "═══════════════════════════════════════════════════════════";
        System.out.println("\n" + border);
        System.out.println(" SMS OTP NOTIFICATION - " + purpose);
        System.out.println(border);
        System.out.println("│ Phone     : " + phone);
        System.out.println("│ Name      : " + name);
        System.out.println("│ OTP       : " + otp);
        System.out.println("│ Valid For : 5 minutes");
        System.out.println("│ Mode      : CONSOLE (Development/Testing)");
        System.out.println(border + "\n");
        log.info(" Console SMS OTP logged for: {}", phone);
    }

    private String buildOtpEmailText(String name, String otp, String purpose) {
        return String.format("""
            Dear %s,
            
            Your OTP for %s is: %s
            
            This OTP is valid for 5 minutes.
            
            SECURITY WARNING:
            - Never share this OTP with anyone
            - Banking staff will never ask for your OTP
            - If you did not request this, please contact support immediately
            
            Thank you for using Banking App!
            
            Regards,
            Banking Team
            
            ---
            This is an automated email. Please do not reply.
            Support: support@bankingapp.com | Phone: 1800-XXX-XXXX
            © 2025 Banking App. All rights reserved.
            """, name, purpose, otp);
    }

    private String buildMpinResetEmailText(String name, String otp) {
        return String.format("""
            Dear %s,
            
            SECURITY ALERT: MPIN Reset Request
            
            We received a request to reset your MPIN. Your verification code is: %s
            
            This OTP is valid for 5 minutes.
            
             IMPORTANT SECURITY NOTICE:
            - If you did NOT request this, contact support IMMEDIATELY
            - Someone may be trying to access your account
            - Never share OTP with anyone, including bank staff
            - Change your MPIN immediately if you suspect compromise
            
            What happens next?
            1. Enter this OTP on the reset screen
            2. Create a new MPIN (must be different from old one)
            3. Your account will be unlocked automatically
            
            Stay secure,
            Banking Security Team
            
            ---
            This is an automated security email. Please do not reply.
            Support: support@bankingapp.com | Phone: 1800-XXX-XXXX (24x7)
            © 2025 Banking App. All rights reserved.
            """, name, otp);
    }
}