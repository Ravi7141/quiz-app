package com.example.quiz.service;

import com.example.quiz.dto.request.TokenGenerateRequest;
import com.example.quiz.dto.response.TokenResponse;
import com.example.quiz.dto.response.TokenVerifyResponse;
import com.example.quiz.entity.CodingTest;
import com.example.quiz.entity.ExamToken;
import com.example.quiz.entity.Quiz;
import com.example.quiz.exception.ResourceNotFoundException;
import com.example.quiz.repository.CodingTestRepository;
import com.example.quiz.repository.ExamTokenRepository;
import com.example.quiz.repository.QuizRepository;
import com.example.quiz.repository.UserRepository;
import com.example.quiz.repository.QuestionRepository;
import com.example.quiz.entity.User;
import com.example.quiz.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamTokenService {

    private final ExamTokenRepository examTokenRepository;
    private final QuizRepository quizRepository;
    private final CodingTestRepository codingTestRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final AuthService authService;
    private final EmailService emailService;

    @Transactional
    public List<TokenResponse> generateTokens(TokenGenerateRequest request) {
        String examTitle = "";
        LocalDateTime validFrom = request.getValidFrom();
        LocalDateTime expiresAt = request.getValidUntil();

        if (validFrom == null) validFrom = LocalDateTime.now();

        if ("QUIZ".equalsIgnoreCase(request.getExamType())) {
            Quiz quiz = quizRepository.findById(request.getExamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Quiz", request.getExamId()));
            examTitle = quiz.getTitle();
            if (expiresAt == null) expiresAt = validFrom.plusDays(7); // Default 7 days
        } else if ("CODING".equalsIgnoreCase(request.getExamType())) {
            CodingTest test = codingTestRepository.findById(request.getExamId())
                    .orElseThrow(() -> new ResourceNotFoundException("CodingTest", request.getExamId()));
            examTitle = test.getTitle();
            if (expiresAt == null) expiresAt = validFrom.plusDays(7); // Default 7 days
        } else {
            throw new IllegalArgumentException("Invalid exam type. Must be QUIZ or CODING.");
        }

        List<TokenResponse> responses = new ArrayList<>();

        for (String raw : request.getEmails()) {
            if (raw == null || raw.trim().isEmpty()) continue;

            String email = "";
            String name = null;
            String phone = null;

            // Simple parser for "Name <email> phone" or "Name, email, phone"
            // We use regex to find the email first
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6})").matcher(raw);
            if (m.find()) {
                email = m.group(1);
                String remaining = raw.replace(email, " ").trim();
                
                // Try to find a phone number (digits, at least 10)
                java.util.regex.Matcher pm = java.util.regex.Pattern.compile("(\\+?\\d[\\d\\s-]{8,14}\\d)").matcher(remaining);
                if (pm.find()) {
                    phone = pm.group(1).replaceAll("\\s+", "");
                    remaining = remaining.replace(pm.group(1), " ").trim();
                }
                
                // Whatever is left is the name
                name = remaining.replaceAll("[,<>]", " ").replaceAll("\\s+", " ").trim();
                if (name.isEmpty()) name = null;
            } else {
                // If no email found, treat the whole string as email (legacy fallback)
                email = raw.trim();
            }

            // Ensure User exists for the student immediately on import/generation so they show up in Student list
            final String finalEmail = email;
            final String finalName = name;
            final String finalPhone = phone;
            final User currentAdmin = authService.getCurrentUser();
            userRepository.findByEmail(email).map(u -> {
                boolean changed = false;
                if ((u.getPhone() == null || u.getPhone().isEmpty()) && finalPhone != null) {
                    u.setPhone(finalPhone);
                    changed = true;
                }
                if ((u.getName() == null || u.getName().isEmpty() || u.getName().equals(finalEmail.split("@")[0])) && finalName != null) {
                    u.setName(finalName);
                    changed = true;
                }
                if (u.getCreatedBy() == null && currentAdmin != null) {
                    u.setCreatedBy(currentAdmin);
                    changed = true;
                }
                if (changed) {
                    userRepository.save(u);
                }
                return u;
            }).orElseGet(() -> {
                User newUser = User.builder()
                        .name(finalName != null ? finalName : finalEmail.split("@")[0])
                        .email(finalEmail)
                        .phone(finalPhone)
                        .password(UUID.randomUUID().toString())
                        .role(Role.STUDENT)
                        .createdBy(currentAdmin)
                        .build();
                return userRepository.save(newUser);
            });

            ExamToken token = ExamToken.builder()
                    .token(UUID.randomUUID().toString())
                    .examId(request.getExamId())
                    .examType(request.getExamType().toUpperCase())
                    .studentEmail(email)
                    .studentName(name)
                    .studentPhone(phone)
                    .isUsed(false)
                    .validFrom(validFrom)
                    .expiresAt(expiresAt)
                    .build();

            examTokenRepository.save(token);

            responses.add(TokenResponse.builder()
                    .token(token.getToken())
                    .examId(token.getExamId())
                    .examType(token.getExamType())
                    .studentEmail(token.getStudentEmail())
                    .studentName(token.getStudentName())
                    .studentPhone(token.getStudentPhone())
                    .isUsed(token.isUsed())
                    .validFrom(token.getValidFrom())
                    .expiresAt(token.getExpiresAt())
                    .examTitle(examTitle)
                    .build());
        }

        return responses;
    }

    public List<TokenResponse> getTokensForExam(String examType, Long examId) {
        String title = getExamTitle(examType, examId);
        return examTokenRepository.findByExamIdAndExamType(examId, examType.toUpperCase())
                .stream()
                .map(t -> TokenResponse.builder()
                        .token(t.getToken())
                        .examId(t.getExamId())
                        .examType(t.getExamType())
                        .studentEmail(t.getStudentEmail())
                        .studentName(t.getStudentName())
                        .studentPhone(t.getStudentPhone())
                        .isUsed(t.isUsed())
                        .validFrom(t.getValidFrom())
                        .expiresAt(t.getExpiresAt())
                        .examTitle(title)
                        .build())
                .collect(Collectors.toList());
    }

    public TokenVerifyResponse verifyToken(String tokenStr) {
        ExamToken token = examTokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token."));

        if (token.isUsed()) {
            throw new IllegalArgumentException("This token has already been used.");
        }
        // Token expiry is still checked, but start time is handled by Frontend
        if (token.getExpiresAt() != null && LocalDateTime.now().isAfter(token.getExpiresAt())) {
            throw new IllegalArgumentException("This token has expired.");
        }

        // Ensure user exists
        User student = userRepository.findByEmail(token.getStudentEmail()).map(u -> {
            // Update phone if it was missing but is now in token
            if ((u.getPhone() == null || u.getPhone().isEmpty()) && token.getStudentPhone() != null) {
                u.setPhone(token.getStudentPhone());
                userRepository.save(u);
            }
            return u;
        }).orElseGet(() -> {
            User newUser = User.builder()
                    .name(token.getStudentName() != null ? token.getStudentName() : token.getStudentEmail().split("@")[0])
                    .email(token.getStudentEmail())
                    .phone(token.getStudentPhone())
                    .password(UUID.randomUUID().toString())
                    .role(Role.STUDENT)
                    .build();
            return userRepository.save(newUser);
        });

        TokenVerifyResponse.TokenVerifyResponseBuilder builder = TokenVerifyResponse.builder()
                .token(token.getToken())
                .examId(token.getExamId())
                .examType(token.getExamType())
                .studentEmail(token.getStudentEmail())
                .studentName(token.getStudentName())
                .studentId(student.getId());

        if ("QUIZ".equals(token.getExamType())) {
            Quiz quiz = quizRepository.findById(token.getExamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Quiz", token.getExamId()));
            builder.examTitle(quiz.getTitle())
                   .description(quiz.getDescription())
                   .durationMinutes(quiz.getDurationMinutes())
                   .scheduledFor(quiz.getScheduledFor())
                   .validUntil(quiz.getValidUntil());
        } else if ("CODING".equals(token.getExamType())) {
            CodingTest test = codingTestRepository.findById(token.getExamId())
                    .orElseThrow(() -> new ResourceNotFoundException("CodingTest", token.getExamId()));
            builder.examTitle(test.getTitle())
                   .description(test.getDescription())
                   .difficulty(test.getDifficulty() != null ? test.getDifficulty() : "MEDIUM")
                   .scheduledFor(test.getScheduledFor())
                   .validUntil(test.getValidUntil());
        }

        return builder.build();
    }

    @Transactional
    public void consumeToken(String tokenStr) {
        ExamToken token = examTokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token."));
        if (token.isUsed()) {
            throw new IllegalArgumentException("Token already used.");
        }
        token.setUsed(true);
        examTokenRepository.save(token);
    }

    private String getExamTitle(String examType, Long examId) {
        if ("QUIZ".equalsIgnoreCase(examType)) {
            return quizRepository.findById(examId).map(Quiz::getTitle).orElse("Unknown Quiz");
        } else if ("CODING".equalsIgnoreCase(examType)) {
            return codingTestRepository.findById(examId).map(CodingTest::getTitle).orElse("Unknown Coding Test");
        }
        return "Unknown";
    }

    @Transactional(readOnly = true)
    public void emailAllTokens(String examType, Long examId, String baseUrl) {
        List<ExamToken> tokens = examTokenRepository.findByExamIdAndExamType(examId, examType.toUpperCase());

        List<ExamToken> activeTokens = tokens.stream()
                .filter(t -> !t.isUsed())
                .collect(Collectors.toList());

        if (activeTokens.isEmpty()) {
            throw new IllegalArgumentException("No available tokens to email.");
        }

        String examTitle = "";
        String duration = null;
        Integer totalQuestions = null;
        LocalDateTime scheduledFor = null;
        User creator = null;

        if ("QUIZ".equalsIgnoreCase(examType)) {
            Quiz quiz = quizRepository.findById(examId).orElse(null);
            if (quiz != null) {
                examTitle = quiz.getTitle();
                if (quiz.getDurationMinutes() != null) {
                    duration = quiz.getDurationMinutes() + " Minutes";
                }
                totalQuestions = questionRepository.countByQuizId(examId);
                scheduledFor = quiz.getScheduledFor();
                creator = quiz.getCreatedBy();
            }
        } else if ("CODING".equalsIgnoreCase(examType)) {
            CodingTest test = codingTestRepository.findById(examId).orElse(null);
            if (test != null) {
                examTitle = test.getTitle();
                scheduledFor = test.getScheduledFor();
                creator = test.getCreatedBy();
            }
        }

        String examDate = "Flexible / Anytime";
        String examTime = "Flexible / Anytime";
        if (scheduledFor != null) {
            try {
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                examDate = scheduledFor.format(dateFormatter);
                examTime = scheduledFor.format(timeFormatter);
            } catch (Exception e) {
                // Ignore formatting exception
            }
        }

        String company = "QuizVault Portal";
        String supportEmail = "support@example.com";
        if (creator != null) {
            if (creator.getName() != null && !creator.getName().isEmpty()) {
                company = creator.getName();
            }
            if (creator.getEmail() != null && !creator.getEmail().isEmpty()) {
                supportEmail = creator.getEmail();
            }
        }

        StringBuilder examDetails = new StringBuilder();
        examDetails.append("Exam Name: ").append(examTitle);
        examDetails.append("\nDate: ").append(examDate);
        examDetails.append("\nTime: ").append(examTime);
        if (duration != null) {
            examDetails.append("\nDuration: ").append(duration);
        }
        if (totalQuestions != null) {
            examDetails.append("\nTotal Questions: ").append(totalQuestions);
        }
        String examDetailsStr = examDetails.toString();

        List<String> failedEmails = new ArrayList<>();
        String subject = "Exam Invitation – Start Your Assessment";

        for (ExamToken t : activeTokens) {
            try {
                String link = baseUrl + "/exam/entry/" + t.getToken();
                String name = t.getStudentName() != null ? t.getStudentName() : t.getStudentEmail().split("@")[0];
                
                String body = String.format(
                        "Hello %s,\n\n" +
                        "You have been invited to attend the online examination.\n\n" +
                        "📘 Exam Details\n" +
                        "━━━━━━━━━━━━━━━━━━\n" +
                        "%s\n\n" +
                        "📝 Instructions\n" +
                        "━━━━━━━━━━━━━━━━━━\n" +
                        "• Ensure you have a stable internet connection.\n" +
                        "• Do not refresh or close the browser during the exam.\n" +
                        "• The exam will automatically submit once the timer ends.\n" +
                        "• Full-screen mode is required during the test.\n" +
                        "• Multiple attempts to exit full-screen may be marked as cheating.\n\n" +
                        "🔗 Start Exam\n" +
                        "━━━━━━━━━━━━━━━━━━\n" +
                        "Click the link below to begin your exam:\n\n" +
                        "%s\n\n" +
                        "If the button/link does not work, copy and paste the URL into your browser.\n\n" +
                        "Best of luck!\n\n" +
                        "Regards,\n" +
                        "%s\n" +
                        "%s",
                        name, examDetailsStr, link, company, supportEmail
                );
                
                emailService.sendEmail(t.getStudentEmail(), subject, body);
            } catch (Exception e) {
                failedEmails.add(t.getStudentEmail() + " (" + e.getMessage() + ")");
            }
        }

        if (!failedEmails.isEmpty()) {
            throw new RuntimeException("Failed to send emails to: " + String.join(", ", failedEmails));
        }
    }
}
