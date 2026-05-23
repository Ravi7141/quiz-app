package com.example.quiz.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void sendEmail(String to, String subject, String body) {
        if (brevoApiKey == null || brevoApiKey.trim().isEmpty()) {
            log.warn("[MOCK EMAIL] Brevo API key not set. Logging email content instead.");
            log.info("\n----------------------------------------\n" +
                    "Sending Email To: {}\n" +
                    "Subject: {}\n" +
                    "Body:\n{}\n" +
                    "----------------------------------------", to, subject, body);
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey.trim());
            headers.set("accept", "application/json");

            Map<String, Object> request = new HashMap<>();
            
            Map<String, String> sender = new HashMap<>();
            sender.put("name", "AssessSphere");
            sender.put("email", fromEmail != null && !fromEmail.isEmpty() ? fromEmail : "noreply@assesssphere.com");
            request.put("sender", sender);
            
            List<Map<String, String>> toList = new ArrayList<>();
            Map<String, String> toMap = new HashMap<>();
            toMap.put("email", to);
            toList.add(toMap);
            request.put("to", toList);
            
            request.put("subject", subject);
            request.put("textContent", body);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            restTemplate.postForObject("https://api.brevo.com/v3/smtp/email", entity, String.class);
            log.info("Email sent successfully via Brevo API to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {} via Brevo: {}. Falling back to console log.", to, e.getMessage());
            log.info("\n----------------------------------------\n" +
                    "[FALLBACK EMAIL] To: {}\n" +
                    "Subject: {}\n" +
                    "Body:\n{}\n" +
                    "----------------------------------------", to, subject, body);
        }
    }
}
