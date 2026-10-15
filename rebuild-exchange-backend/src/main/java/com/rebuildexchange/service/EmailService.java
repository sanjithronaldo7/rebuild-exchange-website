package com.rebuildexchange.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@rebuild.exchange}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends an email OTP to the recipient.
     * If the SMTP server is not accessible (e.g. in local development without active credentials),
     * it logs a warning with the generated code to allow local verification while flagging SMTP delivery status.
     */
    public boolean sendOtpEmail(String toEmail, String otpCode, String purpose) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your ReBuild Exchange Verification Code: " + otpCode);
            message.setText("Hello from ReBuild Exchange,\n\n"
                    + "Your one-time verification code for " + purpose.toLowerCase() + " is:\n\n"
                    + "    " + otpCode + "\n\n"
                    + "This code is valid for 10 minutes. Please do not share this code with anyone.\n\n"
                    + "Best regards,\n"
                    + "The ReBuild Exchange Team\n"
                    + "hello@rebuild.exchange");

            mailSender.send(message);
            logger.info("Successfully dispatched OTP email to {}", toEmail);
            return true;
        } catch (Exception ex) {
            logger.warn("Could not dispatch email via SMTP server ({}). Developer fallback: OTP code for {} is [{}]",
                    ex.getMessage(), toEmail, otpCode);
            return false;
        }
    }
}
