package com.example.quiz.service;

import com.example.quiz.dto.request.TokenGenerateRequest;
import com.example.quiz.dto.response.TokenResponse;
import com.example.quiz.dto.response.TokenVerifyResponse;
import com.example.quiz.entity.CodingTest;
import com.example.quiz.entity.ExamToken;
import com.example.quiz.entity.Quiz;
import com.example.quiz.exception.BadRequestException;
import com.example.quiz.exception.ResourceNotFoundException;
import com.example.quiz.repository.CodingTestRepository;
import com.example.quiz.repository.ExamTokenRepository;
import com.example.quiz.entity.Assessment;
import com.example.quiz.repository.AssessmentRepository;
import com.example.quiz.repository.QuizRepository;
import com.example.quiz.repository.QuestionRepository;
import com.example.quiz.entity.User;
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
    private final AssessmentRepository assessmentRepository;
    private final QuestionRepository questionRepository;
    private final AuthService authService;
    private final EmailService emailService;
    private final UserService userService; // ← replaces duplicate user-create logic

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
            if (expiresAt == null) expiresAt = validFrom.plusDays(7);
        } else if ("CODING".equalsIgnoreCase(request.getExamType())) {
            CodingTest test = codingTestRepository.findById(request.getExamId())
                    .orElseThrow(() -> new ResourceNotFoundException("CodingTest", request.getExamId()));
            examTitle = test.getTitle();
            if (expiresAt == null) expiresAt = validFrom.plusDays(7);
        } else if ("ASSESSMENT".equalsIgnoreCase(request.getExamType())) {
            Assessment assessment = assessmentRepository.findById(request.getExamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assessment", request.getExamId()));
            examTitle = assessment.getTitle();
            if (expiresAt == null) expiresAt = validFrom.plusDays(7);
        } else {
            throw new BadRequestException("Invalid exam type. Must be QUIZ, CODING or ASSESSMENT.");
        }

        List<TokenResponse> responses = new ArrayList<>();
        User currentAdmin = authService.getCurrentUser();

        for (String raw : request.getEmails()) {
            if (raw == null || raw.trim().isEmpty()) continue;

            String email = "";
            String name = null;
            String phone = null;

            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6})")
                    .matcher(raw);
            if (m.find()) {
                email = m.group(1);
                String remaining = raw.replace(email, " ").trim();
                java.util.regex.Matcher pm = java.util.regex.Pattern
                        .compile("(\\+?\\d[\\d\\s-]{8,14}\\d)")
                        .matcher(remaining);
                if (pm.find()) {
                    phone = pm.group(1).replaceAll("\\s+", "");
                    remaining = remaining.replace(pm.group(1), " ").trim();
                }
                name = remaining.replaceAll("[,<>]", " ").replaceAll("\\s+", " ").trim();
                if (name.isEmpty()) name = null;
            } else {
                email = raw.trim();
            }

            // Use centralised UserService instead of inline duplication
            userService.findOrCreateStudent(email, name, phone, currentAdmin);

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

    public com.example.quiz.dto.response.PageData<TokenResponse> getTokensForExam(String examType, Long examId, org.springframework.data.domain.Pageable pageable) {
        String title = getExamTitle(examType, examId);
        org.springframework.data.domain.Page<ExamToken> page = examTokenRepository.findByExamIdAndExamType(examId, examType.toUpperCase(), pageable);
        org.springframework.data.domain.Page<TokenResponse> dtoPage = page.map(t -> TokenResponse.builder()
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
                .build());
        return com.example.quiz.dto.response.PageData.of(dtoPage);
    }

    public TokenVerifyResponse verifyToken(String tokenStr) {
        ExamToken token = examTokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new BadRequestException("Invalid token."));

        if (token.isUsed()) {
            throw new BadRequestException("This token has already been used.");
        }
        if (token.getExpiresAt() != null && LocalDateTime.now().isAfter(token.getExpiresAt())) {
            throw new BadRequestException("This token has expired.");
        }

        // Use centralised UserService for find-or-create
        User student = userService.findOrCreateStudent(
                token.getStudentEmail(), token.getStudentName(), token.getStudentPhone(), null);

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
                   .scheduledFor(null)
                   .validUntil(null);
        } else if ("CODING".equals(token.getExamType())) {
            CodingTest test = codingTestRepository.findById(token.getExamId())
                    .orElseThrow(() -> new ResourceNotFoundException("CodingTest", token.getExamId()));
            builder.examTitle(test.getTitle())
                   .description(test.getDescription())
                   .difficulty(test.getDifficulty() != null ? test.getDifficulty() : "MEDIUM")
                   .scheduledFor(null)
                   .validUntil(null);
        } else if ("ASSESSMENT".equals(token.getExamType())) {
            Assessment assessment = assessmentRepository.findById(token.getExamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assessment", token.getExamId()));
            builder.examTitle(assessment.getTitle())
                   .description(assessment.getDescription())
                   .durationMinutes(assessment.getDurationMinutes())
                   .shareToken(assessment.getShareToken())
                   .scheduledFor(assessment.getScheduledFor())
                   .validUntil(assessment.getValidUntil());
        }

        return builder.build();
    }

    @Transactional
    public void consumeToken(String tokenStr) {
        ExamToken token = examTokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new BadRequestException("Invalid token."));
        if (token.isUsed()) {
            throw new BadRequestException("Token already used.");
        }
        token.setUsed(true);
        examTokenRepository.save(token);
    }

    private String getExamTitle(String examType, Long examId) {
        if ("QUIZ".equalsIgnoreCase(examType)) {
            return quizRepository.findById(examId).map(Quiz::getTitle).orElse("Unknown Quiz");
        } else if ("CODING".equalsIgnoreCase(examType)) {
            return codingTestRepository.findById(examId).map(CodingTest::getTitle).orElse("Unknown Coding Test");
        } else if ("ASSESSMENT".equalsIgnoreCase(examType)) {
            return assessmentRepository.findById(examId).map(Assessment::getTitle).orElse("Unknown Assessment");
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
            throw new BadRequestException("No available tokens to email.");
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
                scheduledFor = null;
                creator = quiz.getCreatedBy();
            }
        } else if ("CODING".equalsIgnoreCase(examType)) {
            CodingTest test = codingTestRepository.findById(examId).orElse(null);
            if (test != null) {
                examTitle = test.getTitle();
                scheduledFor = null;
                creator = test.getCreatedBy();
            }
        } else if ("ASSESSMENT".equalsIgnoreCase(examType)) {
            Assessment assessment = assessmentRepository.findById(examId).orElse(null);
            if (assessment != null) {
                examTitle = assessment.getTitle();
                if (assessment.getDurationMinutes() != null) {
                    duration = assessment.getDurationMinutes() + " Minutes";
                }
                scheduledFor = assessment.getScheduledFor();
            }
        }

        String examDate = "Flexible / Anytime";
        String examTime = "Flexible / Anytime";
        if (scheduledFor != null) {
            try {
                examDate = scheduledFor.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                examTime = scheduledFor.format(DateTimeFormatter.ofPattern("HH:mm"));
            } catch (Exception e) {
                // ignore
            }
        }

        String company = "AssessSphere Portal";
        String supportEmail = "support@example.com";
        if (creator != null) {
            if (creator.getName() != null && !creator.getName().isEmpty()) company = creator.getName();
            if (creator.getEmail() != null && !creator.getEmail().isEmpty()) supportEmail = creator.getEmail();
        }

        StringBuilder examDetails = new StringBuilder();
        examDetails.append("Exam Name: ").append(examTitle);
        examDetails.append("\nDate: ").append(examDate);
        examDetails.append("\nTime: ").append(examTime);
        if (duration != null) examDetails.append("\nDuration: ").append(duration);
        if (totalQuestions != null) examDetails.append("\nTotal Questions: ").append(totalQuestions);
        String examDetailsStr = examDetails.toString();

        List<String> failedEmails = new ArrayList<>();
        String subject = "Exam Invitation – Start Your Assessment";

        for (ExamToken t : activeTokens) {
            try {
                String link = baseUrl + "/exam/entry/" + t.getToken();
                String name = t.getStudentName() != null ? t.getStudentName() : t.getStudentEmail().split("@")[0];
                String body = String.format(
                        "Hello %s,\n\nYou have been invited to attend the online examination.\n\n" +
                        "📘 Exam Details\n━━━━━━━━━━━━━━━━━━\n%s\n\n" +
                        "📝 Instructions\n━━━━━━━━━━━━━━━━━━\n" +
                        "• Ensure you have a stable internet connection.\n" +
                        "• Do not refresh or close the browser during the exam.\n" +
                        "• The exam will automatically submit once the timer ends.\n" +
                        "• Full-screen mode is required during the test.\n\n" +
                        "🔗 Start Exam\n━━━━━━━━━━━━━━━━━━\n%s\n\n" +
                        "Best of luck!\n\nRegards,\n%s\n%s",
                        name, examDetailsStr, link, company, supportEmail);
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
