package com.example.quiz.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String body) {
        if (fromEmail == null || fromEmail.trim().isEmpty() || fromEmail.contains("your-email")) {
            log.warn("[MOCK EMAIL] SMTP credentials not set. Logging email content instead.");
            log.info("\n----------------------------------------\n" +
                     "Sending Email To: {}\n" +
                     "Subject: {}\n" +
                     "Body:\n{}\n" +
                     "----------------------------------------", to, subject, body);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}. Falling back to console log.", to, e.getMessage());
            log.info("\n----------------------------------------\n" +
                     "[FALLBACK EMAIL] To: {}\n" +
                     "Subject: {}\n" +
                     "Body:\n{}\n" +
                     "----------------------------------------", to, subject, body);
        }
    }
}
