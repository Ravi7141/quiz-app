package com.example.quiz.service;

import com.example.quiz.dto.request.AssessmentRequest;
import com.example.quiz.dto.response.*;
import com.example.quiz.entity.*;
import com.example.quiz.enums.AttemptStatus;
import com.example.quiz.enums.SectionType;
import com.example.quiz.exception.ResourceNotFoundException;
import com.example.quiz.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Transactional
    public AssessmentResponse createAssessment(AssessmentRequest request) {
        Assessment assessment = Assessment.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .durationMinutes(request.getDurationMinutes())
                .shareToken(UUID.randomUUID().toString())
                .active(true)
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
        return assessmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AssessmentResponse regenerateShareToken(Long id) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", id));
        assessment.setShareToken(UUID.randomUUID().toString());
        return mapToResponse(assessmentRepository.save(assessment));
    }

    public AssessmentDetailsResponse getAssessmentByToken(String token) {
        Optional<Assessment> optAssessment = assessmentRepository.findByShareToken(token);
        
        if (optAssessment.isEmpty()) {
            try {
                Long numericId = Long.parseLong(token);
                // 1. Try to find by Assessment ID directly
                optAssessment = assessmentRepository.findById(numericId);
                
                // 2. If not found, try to find an Assessment containing this Quiz as a section
                if (optAssessment.isEmpty()) {
                    List<AssessmentSection> quizSections = sectionRepository.findAll().stream()
                            .filter(s -> s.getSectionType() == SectionType.QUIZ && s.getReferenceId().equals(numericId))
                            .collect(Collectors.toList());
                    if (!quizSections.isEmpty()) {
                        optAssessment = Optional.of(quizSections.get(0).getAssessment());
                    }
                }
                
                // 3. If not found, try to find an Assessment containing this Coding Test as a section
                if (optAssessment.isEmpty()) {
                    List<AssessmentSection> codingSections = sectionRepository.findAll().stream()
                            .filter(s -> s.getSectionType() == SectionType.CODING && s.getReferenceId().equals(numericId))
                            .collect(Collectors.toList());
                    if (!codingSections.isEmpty()) {
                        optAssessment = Optional.of(codingSections.get(0).getAssessment());
                    }
                }

                // 4. If still not found, dynamically create a fallback Assessment for this Quiz or Coding Test
                if (optAssessment.isEmpty()) {
                    Quiz quiz = quizRepository.findById(numericId).orElse(null);
                    if (quiz != null) {
                        Assessment fallback = Assessment.builder()
                                .title(quiz.getTitle())
                                .description(quiz.getDescription())
                                .durationMinutes(quiz.getDurationMinutes())
                                .shareToken("fallback-quiz-" + numericId)
                                .active(true)
                                .build();
                        Assessment saved = assessmentRepository.save(fallback);
                        AssessmentSection section = AssessmentSection.builder()
                                .assessment(saved)
                                .sectionType(SectionType.QUIZ)
                                .referenceId(numericId)
                                .sectionOrder(1)
                                .build();
                        sectionRepository.save(section);
                        optAssessment = Optional.of(saved);
                    } else {
                        CodingTest codingTest = codingTestRepository.findById(numericId).orElse(null);
                        if (codingTest != null) {
                            Assessment fallback = Assessment.builder()
                                    .title(codingTest.getTitle())
                                    .description(codingTest.getDescription())
                                    .durationMinutes(30) // default 30 mins
                                    .shareToken("fallback-coding-" + numericId)
                                    .active(true)
                                    .build();
                            Assessment saved = assessmentRepository.save(fallback);
                            AssessmentSection section = AssessmentSection.builder()
                                    .assessment(saved)
                                    .sectionType(SectionType.CODING)
                                    .referenceId(numericId)
                                    .sectionOrder(1)
                                    .build();
                            sectionRepository.save(section);
                            optAssessment = Optional.of(saved);
                        }
                    }
                }
            } catch (NumberFormatException e) {
                // Ignore and proceed to throw exception
            }
        }

        Assessment assessment = optAssessment
                .orElseThrow(() -> new ResourceNotFoundException("Assessment with token " + token, 0L));

        List<AssessmentSection> sections = sectionRepository.findByAssessmentIdOrderBySectionOrderAsc(assessment.getId());

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
                    desc = quiz.getDescription();
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
                                    .build())
                            .collect(Collectors.toList());
                }
            } else if (section.getSectionType() == SectionType.CODING) {
                CodingTest test = codingTestRepository.findById(section.getReferenceId()).orElse(null);
                if (test != null) {
                    title = test.getTitle();
                    desc = test.getDescription();
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
                .sections(sectionDetailsList)
                .build();
    }

    @Transactional
    public AssessmentAttemptResponse startAttempt(Long assessmentId, Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", assessmentId));

        // If an attempt is already IN_PROGRESS, return that
        Optional<AssessmentAttempt> existing = attemptRepository.findByAssessmentIdAndStudentId(assessmentId, studentId);
        if (existing.isPresent() && existing.get().getStatus() == AttemptStatus.IN_PROGRESS) {
            return mapToAttemptResponse(existing.get());
        }

        AssessmentAttempt attempt = AssessmentAttempt.builder()
                .assessment(assessment)
                .student(student)
                .status(AttemptStatus.IN_PROGRESS)
                .violations(0)
                .build();

        AssessmentAttempt saved = attemptRepository.save(attempt);

        // Pre-create child Quiz attempts if any QUIZ sections exist
        List<AssessmentSection> sections = sectionRepository.findByAssessmentIdOrderBySectionOrderAsc(assessmentId);
        for (AssessmentSection section : sections) {
            if (section.getSectionType() == SectionType.QUIZ) {
                Quiz quiz = quizRepository.findById(section.getReferenceId()).orElse(null);
                if (quiz != null) {
                    QuizAttempt quizAttempt = QuizAttempt.builder()
                            .student(student)
                            .quiz(quiz)
                            .status(AttemptStatus.IN_PROGRESS)
                            .assessmentAttempt(saved)
                            .build();
                    quizAttemptRepository.save(quizAttempt);
                }
            }
        }

        return mapToAttemptResponse(saved);
    }

    @Transactional
    public AssessmentAttemptResponse submitAttempt(Long attemptId) {
        AssessmentAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("AssessmentAttempt", attemptId));

        if (attempt.getStatus() == AttemptStatus.SUBMITTED) {
            return mapToAttemptResponse(attempt);
        }

        // Submitting child QuizAttempts
        List<QuizAttempt> quizAttempts = quizAttemptRepository.findByAssessmentAttemptId(attemptId);
        int totalScore = 0;
        for (QuizAttempt qa : quizAttempts) {
            if (qa.getStatus() == AttemptStatus.IN_PROGRESS) {
                List<StudentAnswer> answers = studentAnswerRepository.findByAttemptId(qa.getId());
                int rawScore = answers.stream()
                        .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                        .mapToInt(a -> a.getQuestion().getMarks())
                        .sum();
                int maxScore = qa.getQuiz().getTotalMarks() != null ? qa.getQuiz().getTotalMarks() : rawScore;
                qa.setScore(Math.min(rawScore, maxScore));
                qa.setStatus(AttemptStatus.SUBMITTED);
                qa.setSubmittedAt(LocalDateTime.now());
                quizAttemptRepository.save(qa);
            }
            totalScore += qa.getScore() != null ? qa.getScore() : 0;
        }

        attempt.setStatus(AttemptStatus.SUBMITTED);
        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setScore(totalScore);

        return mapToAttemptResponse(attemptRepository.save(attempt));
    }

    private AssessmentResponse mapToResponse(Assessment assessment) {
        return AssessmentResponse.builder()
                .id(assessment.getId())
                .title(assessment.getTitle())
                .description(assessment.getDescription())
                .durationMinutes(assessment.getDurationMinutes())
                .shareToken(assessment.getShareToken())
                .active(assessment.getActive())
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
                .startedAt(attempt.getStartedAt())
                .submittedAt(attempt.getSubmittedAt())
                .quizAttemptMap(quizAttemptMap)
                .build();
    }

    @Transactional
    public void saveCodingSubmission(Long assessmentAttemptId, Long codingTestId, String code, String language, Boolean passed) {
        AssessmentAttempt attempt = attemptRepository.findById(assessmentAttemptId)
                .orElseThrow(() -> new ResourceNotFoundException("AssessmentAttempt", assessmentAttemptId));

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new com.example.quiz.exception.BadRequestException("This attempt is already submitted. You cannot submit code.");
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
}
