package com.example.quiz.service;

import com.example.quiz.dto.request.AssessmentRequest;
import com.example.quiz.dto.response.*;
import com.example.quiz.entity.*;
import com.example.quiz.enums.AttemptStatus;
import com.example.quiz.enums.SectionType;
import com.example.quiz.exception.BadRequestException;
import com.example.quiz.exception.ResourceNotFoundException;
import com.example.quiz.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentSectionRepository sectionRepository;
    private final AssessmentAttemptRepository attemptRepository;
    private final QuizRepository quizRepository;
    private final CodingTestRepository codingTestRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final StudentCodingSubmissionRepository studentCodingSubmissionRepository;
    private final UserService userService;
    private final AuthService authService; // ← for admin-scoped filtering

    @Transactional
    public AssessmentResponse createAssessment(AssessmentRequest request) {
        User currentAdmin = authService.getCurrentUser(); // ← tag this assessment to the creating admin
        Assessment assessment = Assessment.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .durationMinutes(request.getDurationMinutes())
                .shareToken(UUID.randomUUID().toString())
                .scheduledFor(request.getScheduledFor())
                .validUntil(request.getValidUntil())
                .passingPercentage(request.getPassingPercentage())
                .active(true)
                .createdBy(currentAdmin)
                .build();

        Assessment saved = assessmentRepository.save(assessment);

        if (request.getSections() != null) {
            for (AssessmentRequest.SectionRequest secReq : request.getSections()) {
                AssessmentSection section = AssessmentSection.builder()
                        .assessment(saved)
                        .sectionType(secReq.getType())
                        .referenceId(secReq.getReferenceId())
                        .sectionOrder(secReq.getOrder())
                        .build();
                sectionRepository.save(section);
            }
        }

        return mapToResponse(saved);
    }

    public List<AssessmentResponse> getAllAssessments() {
        User currentAdmin = authService.getCurrentUser();
        List<Assessment> assessments;
        if (currentAdmin != null) {
            assessments = assessmentRepository.findByCreatedById(currentAdmin.getId());
        } else {
            assessments = assessmentRepository.findAll();
        }
        return assessments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AssessmentResponse getAssessmentById(Long id) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", id));
        return mapToResponse(assessment);
    }

    @Transactional
    public AssessmentResponse updateAssessment(Long id, AssessmentRequest request) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", id));

        if (request.getTitle() != null)           assessment.setTitle(request.getTitle());
        if (request.getDescription() != null)     assessment.setDescription(request.getDescription());
        if (request.getDurationMinutes() != null) assessment.setDurationMinutes(request.getDurationMinutes());
        if (request.getScheduledFor() != null)    assessment.setScheduledFor(request.getScheduledFor());
        if (request.getValidUntil() != null)      assessment.setValidUntil(request.getValidUntil());
        if (request.getPassingPercentage() != null) assessment.setPassingPercentage(request.getPassingPercentage());

        if (request.getSections() != null) {
            sectionRepository.deleteByAssessmentId(id);
            for (AssessmentRequest.SectionRequest secReq : request.getSections()) {
                AssessmentSection section = AssessmentSection.builder()
                        .assessment(assessment)
                        .sectionType(secReq.getType())
                        .referenceId(secReq.getReferenceId())
                        .sectionOrder(secReq.getOrder())
                        .build();
                sectionRepository.save(section);
            }
        }

        return mapToResponse(assessmentRepository.save(assessment));
    }

    @Transactional
    public AssessmentResponse regenerateShareToken(Long id) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", id));
        assessment.setShareToken(UUID.randomUUID().toString());
        return mapToResponse(assessmentRepository.save(assessment));
    }

    @Transactional
    public void deleteAssessment(Long id) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", id));

        // Delete all assessment attempts and their child records
        List<com.example.quiz.entity.AssessmentAttempt> attempts = attemptRepository.findByAssessmentId(id);
        for (com.example.quiz.entity.AssessmentAttempt aa : attempts) {
            // Delete quiz attempts (and student answers via cascade or manually)
            List<QuizAttempt> qas = quizAttemptRepository.findByAssessmentAttemptId(aa.getId());
            for (QuizAttempt qa : qas) {
                studentAnswerRepository.deleteByAttemptId(qa.getId());
                quizAttemptRepository.delete(qa);
            }
            // Delete coding submissions
            studentCodingSubmissionRepository.deleteByAssessmentAttemptId(aa.getId());
            attemptRepository.delete(aa);
        }

        // Delete sections and the assessment itself
        sectionRepository.deleteByAssessmentId(id);
        assessmentRepository.delete(assessment);
        log.info("deleteAssessment: deleted assessment {} and all associated data", id);
    }

    public AssessmentDetailsResponse getAssessmentByToken(String token) {
        Optional<Assessment> assessmentOpt = assessmentRepository.findByShareToken(token);
        if (assessmentOpt.isEmpty()) {
            try {
                Long id = Long.parseLong(token);
                assessmentOpt = assessmentRepository.findById(id);
            } catch (NumberFormatException ignored) {}
        }
        Assessment assessment = assessmentOpt
                .orElseThrow(() -> new ResourceNotFoundException("Assessment with token " + token, 0L));

        return buildDetailsResponse(assessment);
    }

    private AssessmentDetailsResponse buildDetailsResponse(Assessment assessment) {
        List<AssessmentSection> sections = sectionRepository
                .findByAssessmentIdOrderBySectionOrderAsc(assessment.getId());

        List<AssessmentDetailsResponse.SectionDetails> sectionDetailsList = new ArrayList<>();
        for (AssessmentSection section : sections) {
            String title = "";
            String desc = "";
            List<QuestionResponse> questions = null;
            CodingTestResponse codingTest = null;

            if (section.getSectionType() == SectionType.QUIZ) {
                Quiz quiz = quizRepository.findById(section.getReferenceId()).orElse(null);
                if (quiz != null) {
                    title = quiz.getTitle();
                    desc = quiz.getDescription() != null ? quiz.getDescription() : "";
                    questions = questionRepository.findByQuizId(quiz.getId()).stream()
                            .map(q -> QuestionResponse.builder()
                                    .id(q.getId())
                                    .questionText(q.getQuestionText())
                                    .optionA(q.getOptionA())
                                    .optionB(q.getOptionB())
                                    .optionC(q.getOptionC())
                                    .optionD(q.getOptionD())
                                    .marks(q.getMarks())
                                    .quizId(quiz.getId())
                                    .multiAnswer(q.getCorrectAnswer() != null && q.getCorrectAnswer().contains(","))
                                    .build())
                            .collect(Collectors.toList());
                }
            } else if (section.getSectionType() == SectionType.CODING) {
                CodingTest test = codingTestRepository.findById(section.getReferenceId()).orElse(null);
                if (test != null) {
                    title = test.getTitle();
                    desc = test.getDescription() != null ? test.getDescription() : "";
                    codingTest = CodingTestResponse.builder()
                            .id(test.getId())
                            .title(test.getTitle())
                            .description(test.getDescription())
                            .sampleInput(test.getSampleInput())
                            .sampleOutput(test.getSampleOutput())
                            .difficulty(test.getDifficulty())
                            .scheduledFor(test.getScheduledFor())
                            .validUntil(test.getValidUntil())
                            .createdAt(test.getCreatedAt())
                            .build();
                }
            }

            sectionDetailsList.add(AssessmentDetailsResponse.SectionDetails.builder()
                    .id(section.getId())
                    .type(section.getSectionType())
                    .referenceId(section.getReferenceId())
                    .order(section.getSectionOrder())
                    .title(title)
                    .description(desc)
                    .questions(questions)
                    .codingTest(codingTest)
                    .build());
        }

        return AssessmentDetailsResponse.builder()
                .id(assessment.getId())
                .title(assessment.getTitle())
                .description(assessment.getDescription())
                .durationMinutes(assessment.getDurationMinutes())
                .shareToken(assessment.getShareToken())
                .active(assessment.getActive())
                .scheduledFor(assessment.getScheduledFor())
                .validUntil(assessment.getValidUntil())
                .passingPercentage(assessment.getPassingPercentage())
                .sections(sectionDetailsList)
                .build();
    }

    @Transactional
    public AssessmentAttemptResponse startAttempt(Long assessmentId, Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", assessmentId));

        // Use List to avoid crash when student has multiple past attempts
        List<AssessmentAttempt> existing = attemptRepository.findByAssessmentId(assessmentId)
                .stream().filter(a -> a.getStudent().getId().equals(studentId))
                .collect(Collectors.toList());

        // If student already has an IN_PROGRESS attempt, resume it
        Optional<AssessmentAttempt> inProgress = existing.stream()
                .filter(a -> a.getStatus() == AttemptStatus.IN_PROGRESS)
                .findFirst();
        if (inProgress.isPresent()) {
            log.info("startAttempt: resuming IN_PROGRESS attempt {} for student {}", inProgress.get().getId(), studentId);
            return mapToAttemptResponse(inProgress.get());
        }

        // Create new assessment attempt
        AssessmentAttempt attempt = AssessmentAttempt.builder()
                .assessment(assessment)
                .student(student)
                .status(AttemptStatus.IN_PROGRESS)
                .violations(0)
                .build();
        AssessmentAttempt saved = attemptRepository.save(attempt);
        log.info("startAttempt: created new assessment attempt {} for student {}", saved.getId(), studentId);

        // Create quiz attempts for each quiz section
        List<AssessmentSection> sections = sectionRepository.findByAssessmentIdOrderBySectionOrderAsc(assessmentId);
        for (AssessmentSection section : sections) {
            if (section.getSectionType() == SectionType.QUIZ && section.getReferenceId() != null) {
                Quiz quiz = quizRepository.findById(section.getReferenceId()).orElse(null);
                if (quiz != null) {
                    QuizAttempt quizAttempt = QuizAttempt.builder()
                            .student(student)
                            .quiz(quiz)
                            .status(AttemptStatus.IN_PROGRESS)
                            .assessmentAttempt(saved)
                            .build();
                    QuizAttempt savedQa = quizAttemptRepository.save(quizAttempt);
                    log.info("  Created QuizAttempt {} for quiz {} linked to assessmentAttempt {}", savedQa.getId(), quiz.getId(), saved.getId());
                }
            }
        }

        return mapToAttemptResponse(saved);
    }

    @Transactional
    public AssessmentAttemptResponse submitAttempt(Long attemptId, java.util.Map<String, String> answersFromFrontend) {
        AssessmentAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("AssessmentAttempt", attemptId));

        if (attempt.getStatus() == AttemptStatus.SUBMITTED) {
            log.info("submitAttempt [id={}]: already SUBMITTED, returning cached result", attemptId);
            return mapToAttemptResponse(attempt);
        }

        List<AssessmentSection> sections = sectionRepository
                .findByAssessmentIdOrderBySectionOrderAsc(attempt.getAssessment().getId());
        List<QuizAttempt> quizAttempts = quizAttemptRepository.findByAssessmentAttemptId(attemptId);

        // Convert string keys from JSON to Long keys for lookup
        java.util.Map<Long, String> safe = new java.util.HashMap<>();
        if (answersFromFrontend != null) {
            for (java.util.Map.Entry<String, String> entry : answersFromFrontend.entrySet()) {
                try {
                    safe.put(Long.parseLong(entry.getKey()), entry.getValue());
                } catch (NumberFormatException e) {
                    log.warn("submitAttempt: skipping invalid questionId key '{}'", entry.getKey());
                }
            }
        }

        log.info("submitAttempt [id={}]: {} sections, {} quizAttempts, {} answers received",
                attemptId, sections.size(), quizAttempts.size(), safe.size());
        log.info("  Answers map: {}", safe);

        int totalScore = 0;
        int totalMaxScore = 0;

        for (AssessmentSection section : sections) {

            // ── QUIZ SECTION ─────────────────────────────────────────────────
            if (section.getSectionType() == SectionType.QUIZ) {
                if (section.getReferenceId() == null) continue;

                Quiz quiz = quizRepository.findById(section.getReferenceId()).orElse(null);
                if (quiz == null) {
                    log.warn("  Quiz {} not found, skipping", section.getReferenceId());
                    continue;
                }

                List<com.example.quiz.entity.Question> questions = questionRepository.findByQuizId(quiz.getId());

                // Calculate max marks for this quiz section
                int quizMax = (quiz.getTotalMarks() != null && quiz.getTotalMarks() > 0)
                        ? quiz.getTotalMarks()
                        : questions.stream().mapToInt(q -> q.getMarks() != null ? q.getMarks() : 1).sum();
                totalMaxScore += quizMax;

                // Find or create the quiz attempt for this section
                QuizAttempt qa = quizAttempts.stream()
                        .filter(a -> a.getQuiz() != null && a.getQuiz().getId().equals(section.getReferenceId()))
                        .findFirst()
                        .orElseGet(() -> {
                            log.warn("  No QuizAttempt found for quiz={}, creating one now", quiz.getId());
                            QuizAttempt newQa = QuizAttempt.builder()
                                    .student(attempt.getStudent())
                                    .quiz(quiz)
                                    .status(AttemptStatus.IN_PROGRESS)
                                    .assessmentAttempt(attempt)
                                    .build();
                            return quizAttemptRepository.save(newQa);
                        });

                // Score this section by iterating every question
                int sectionScore = 0;
                for (com.example.quiz.entity.Question q : questions) {
                    String selected = safe.get(q.getId());
                    boolean isCorrect = false;

                    if (selected != null && !selected.trim().isEmpty() && q.getCorrectAnswer() != null) {
                        java.util.Set<String> correctSet = new java.util.HashSet<>();
                        for (String c : q.getCorrectAnswer().split(",")) {
                            String trimmed = c.trim().toUpperCase();
                            if (!trimmed.isEmpty()) correctSet.add(trimmed);
                        }
                        java.util.Set<String> selectedSet = new java.util.HashSet<>();
                        for (String s : selected.split(",")) {
                            String trimmed = s.trim().toUpperCase();
                            if (!trimmed.isEmpty()) selectedSet.add(trimmed);
                        }
                        isCorrect = !correctSet.isEmpty() && correctSet.equals(selectedSet);
                    }

                    if (isCorrect) {
                        sectionScore += (q.getMarks() != null ? q.getMarks() : 1);
                    }

                    log.info("  Q[id={}] correct='{}' selected='{}' isCorrect={} marks={}",
                            q.getId(), q.getCorrectAnswer(), selected, isCorrect, q.getMarks());

                    // Persist individual answer record
                    if (selected != null) {
                        boolean finalIsCorrect = isCorrect;
                        StudentAnswer sa = studentAnswerRepository
                                .findByAttemptIdAndQuestionId(qa.getId(), q.getId())
                                .orElse(StudentAnswer.builder().attempt(qa).question(q).build());
                        sa.setSelectedOption(selected);
                        sa.setIsCorrect(finalIsCorrect);
                        studentAnswerRepository.save(sa);
                    }
                }

                int cappedScore = Math.min(sectionScore, quizMax);
                qa.setScore(cappedScore);
                qa.setStatus(AttemptStatus.SUBMITTED);
                qa.setSubmittedAt(LocalDateTime.now());
                quizAttemptRepository.save(qa);
                totalScore += cappedScore;

                log.info("  Quiz[{}] '{}': {} questions, sectionScore={}, cappedScore={}, max={}",
                        quiz.getId(), quiz.getTitle(), questions.size(), sectionScore, cappedScore, quizMax);

            // ── CODING SECTION ───────────────────────────────────────────────
            } else if (section.getSectionType() == SectionType.CODING) {
                if (section.getReferenceId() == null) continue;
                int codingMax = 20;
                totalMaxScore += codingMax;
                boolean passed = studentCodingSubmissionRepository
                        .findByAssessmentAttemptIdAndCodingTestId(attemptId, section.getReferenceId())
                        .stream().anyMatch(sub -> Boolean.TRUE.equals(sub.getPassed()));
                if (passed) totalScore += codingMax;
                log.info("  Coding[{}]: passed={}, marks={}", section.getReferenceId(), passed, passed ? codingMax : 0);
            }
        }

        // Finalise assessment attempt
        attempt.setStatus(AttemptStatus.SUBMITTED);
        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setScore(totalScore);

        double percentage = totalMaxScore > 0
                ? Math.round(((double) totalScore / totalMaxScore) * 10000.0) / 100.0
                : 100.0;
        attempt.setPercentage(percentage);

        Integer passingPct = attempt.getAssessment().getPassingPercentage();
        boolean passed = (passingPct == null) || (percentage >= passingPct);
        attempt.setPassed(passed);

        log.info("submitAttempt FINAL [id={}]: score={}/{}, percentage={}%, passed={}",
                attemptId, totalScore, totalMaxScore, percentage, passed);

        return mapToAttemptResponse(attemptRepository.save(attempt));
    }

    /** Extracted score calculation — no more duplication with AttemptService */
    private int calculateScore(List<StudentAnswer> answers) {
        return answers.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                .mapToInt(a -> a.getQuestion().getMarks() != null ? a.getQuestion().getMarks() : 1)
                .sum();
    }

    private AssessmentResponse mapToResponse(Assessment assessment) {
        return AssessmentResponse.builder()
                .id(assessment.getId())
                .title(assessment.getTitle())
                .description(assessment.getDescription())
                .durationMinutes(assessment.getDurationMinutes())
                .shareToken(assessment.getShareToken())
                .active(assessment.getActive())
                .scheduledFor(assessment.getScheduledFor())
                .validUntil(assessment.getValidUntil())
                .passingPercentage(assessment.getPassingPercentage())
                .createdAt(assessment.getCreatedAt())
                .build();
    }

    private AssessmentAttemptResponse mapToAttemptResponse(AssessmentAttempt attempt) {
        List<QuizAttempt> quizAttempts = quizAttemptRepository.findByAssessmentAttemptId(attempt.getId());
        java.util.Map<Long, Long> quizAttemptMap = quizAttempts.stream()
                .collect(Collectors.toMap(qa -> qa.getQuiz().getId(), QuizAttempt::getId, (a, b) -> a));

        return AssessmentAttemptResponse.builder()
                .attemptId(attempt.getId())
                .assessmentId(attempt.getAssessment().getId())
                .status(attempt.getStatus())
                .score(attempt.getScore())
                .passed(attempt.getPassed())
                .percentage(attempt.getPercentage())
                .startedAt(attempt.getStartedAt())
                .submittedAt(attempt.getSubmittedAt())
                .quizAttemptMap(quizAttemptMap)
                .build();
    }

    @Transactional
    public void saveCodingSubmission(Long assessmentAttemptId, Long codingTestId,
                                     String code, String language, Boolean passed) {
        AssessmentAttempt attempt = attemptRepository.findById(assessmentAttemptId)
                .orElseThrow(() -> new ResourceNotFoundException("AssessmentAttempt", assessmentAttemptId));

        // Gracefully handle race condition: if student submits code AFTER final assessment submit
        if (attempt.getStatus() == AttemptStatus.SUBMITTED) {
            log.warn("saveCodingSubmission: attempt {} already SUBMITTED — saving code anyway for record", assessmentAttemptId);
            // Still save the code submission for the record, don't throw
        }

        CodingTest codingTest = codingTestRepository.findById(codingTestId)
                .orElseThrow(() -> new ResourceNotFoundException("CodingTest", codingTestId));

        StudentCodingSubmission submission = studentCodingSubmissionRepository
                .findByAssessmentAttemptIdAndCodingTestId(assessmentAttemptId, codingTestId)
                .stream().findFirst()
                .orElse(StudentCodingSubmission.builder()
                        .assessmentAttempt(attempt)
                        .codingTest(codingTest)
                        .build());

        submission.setCode(code);
        submission.setLanguage(language);
        submission.setPassed(passed);

        studentCodingSubmissionRepository.save(submission);
    }

    /**
     * Enroll a student for an assessment (find or create account).
     * Uses centralised UserService — no more duplicate logic.
     */
    @Transactional
    public AuthResponse enrollStudent(String name, String email, String phone) {
        User student = userService.findOrCreateStudent(email, name, phone, null);
        return userService.mapToAuthResponse(student);
    }

    /** Debug: returns raw DB state for an assessment attempt */
    public java.util.Map<String, Object> debugAttempt(Long attemptId) {
        List<QuizAttempt> quizAttempts = quizAttemptRepository.findByAssessmentAttemptId(attemptId);
        java.util.List<java.util.Map<String, Object>> qaList = new java.util.ArrayList<>();
        for (QuizAttempt qa : quizAttempts) {
            List<com.example.quiz.entity.StudentAnswer> answers = studentAnswerRepository.findByAttemptId(qa.getId());
            java.util.Map<String, Object> qaInfo = new java.util.LinkedHashMap<>();
            qaInfo.put("quizAttemptId", qa.getId());
            qaInfo.put("quizId", qa.getQuiz() != null ? qa.getQuiz().getId() : null);
            qaInfo.put("quizTitle", qa.getQuiz() != null ? qa.getQuiz().getTitle() : null);
            qaInfo.put("status", qa.getStatus());
            qaInfo.put("score", qa.getScore());
            qaInfo.put("answersCount", answers.size());
            java.util.List<java.util.Map<String, Object>> answerList = new java.util.ArrayList<>();
            for (com.example.quiz.entity.StudentAnswer ans : answers) {
                java.util.Map<String, Object> a = new java.util.LinkedHashMap<>();
                a.put("questionId", ans.getQuestion().getId());
                a.put("selectedOption", ans.getSelectedOption());
                a.put("isCorrect", ans.getIsCorrect());
                a.put("marks", ans.getQuestion().getMarks());
                a.put("correctAnswer", ans.getQuestion().getCorrectAnswer());
                answerList.add(a);
            }
            qaInfo.put("answers", answerList);
            qaList.add(qaInfo);
        }
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("assessmentAttemptId", attemptId);
        result.put("quizAttempts", qaList);
        return result;
    }

    /** Debug: find latest assessment attempt for a student */
    public java.util.Map<String, Object> debugLatestAttemptForStudent(Long studentId) {
        List<com.example.quiz.entity.AssessmentAttempt> attempts = attemptRepository.findByStudentId(studentId);
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("studentId", studentId);
        result.put("totalAssessmentAttempts", attempts.size());
        if (attempts.isEmpty()) {
            result.put("message", "No assessment attempts found for this student");
            return result;
        }
        // Get the latest attempt
        com.example.quiz.entity.AssessmentAttempt latest = attempts.get(attempts.size() - 1);
        result.put("latestAttemptId", latest.getId());
        result.put("latestAttemptStatus", latest.getStatus());
        result.put("latestScore", latest.getScore());
        result.put("latestPercentage", latest.getPercentage());
        // Get quiz attempts linked to this assessment attempt
        List<QuizAttempt> qas = quizAttemptRepository.findByAssessmentAttemptId(latest.getId());
        result.put("quizAttemptsLinked", qas.size());
        java.util.List<java.util.Map<String, Object>> qaDetails = new java.util.ArrayList<>();
        for (QuizAttempt qa : qas) {
            List<com.example.quiz.entity.StudentAnswer> answers = studentAnswerRepository.findByAttemptId(qa.getId());
            java.util.Map<String, Object> qaInfo = new java.util.LinkedHashMap<>();
            qaInfo.put("quizAttemptId", qa.getId());
            qaInfo.put("quizId", qa.getQuiz() != null ? qa.getQuiz().getId() : null);
            qaInfo.put("quizTitle", qa.getQuiz() != null ? qa.getQuiz().getTitle() : null);
            qaInfo.put("status", qa.getStatus());
            qaInfo.put("score", qa.getScore());
            qaInfo.put("answersCount", answers.size());
            java.util.List<java.util.Map<String, Object>> answerDetails = new java.util.ArrayList<>();
            for (com.example.quiz.entity.StudentAnswer ans : answers) {
                java.util.Map<String, Object> a = new java.util.LinkedHashMap<>();
                a.put("questionId", ans.getQuestion().getId());
                a.put("selectedOption", ans.getSelectedOption());
                a.put("isCorrect", ans.getIsCorrect());
                a.put("correctAnswer", ans.getQuestion().getCorrectAnswer());
                answerDetails.add(a);
            }
            qaInfo.put("answers", answerDetails);
            qaDetails.add(qaInfo);
        }
        result.put("quizAttemptDetails", qaDetails);
        // Also check all exam_attempts for this student (not just assessment-linked)
        List<QuizAttempt> allQas = quizAttemptRepository.findByStudentId(studentId);
        result.put("allQuizAttemptsForStudent", allQas.stream().map(qa -> {
            java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", qa.getId());
            m.put("quizId", qa.getQuiz() != null ? qa.getQuiz().getId() : null);
            m.put("quizTitle", qa.getQuiz() != null ? qa.getQuiz().getTitle() : null);
            m.put("assessmentAttemptId", qa.getAssessmentAttempt() != null ? qa.getAssessmentAttempt().getId() : null);
            m.put("status", qa.getStatus());
            m.put("score", qa.getScore());
            return m;
        }).collect(java.util.stream.Collectors.toList()));
        return result;
    }
}
