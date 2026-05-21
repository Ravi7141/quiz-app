package com.example.quiz.service;

import com.example.quiz.dto.request.QuestionRequest;
import com.example.quiz.dto.response.QuestionResponse;
import com.example.quiz.entity.Question;
import com.example.quiz.entity.Quiz;
import com.example.quiz.exception.BadRequestException;
import com.example.quiz.exception.ResourceNotFoundException;
import com.example.quiz.repository.QuestionRepository;
import com.example.quiz.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Question Service — handles all question CRUD operations.
 */
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;

    // ─── Add Question ─────────────────────────────────────────────────────────

    public QuestionResponse addQuestion(QuestionRequest request) {
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", request.getQuizId()));

        Question question = Question.builder()
                .questionText(request.getQuestionText())
                .questionImage(request.getQuestionImage())
                .optionA(request.getOptionA())
                .optionB(request.getOptionB())
                .optionC(request.getOptionC())
                .optionD(request.getOptionD())
                .correctAnswer(request.getCorrectAnswer())
                .marks(request.getMarks())
                .quiz(quiz)
                .build();

        return mapToResponse(questionRepository.save(question), true);
    }

    public List<QuestionResponse> addQuestionsBulk(List<QuestionRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new BadRequestException("At least one question is required for bulk import.");
        }

        Long quizId = requests.get(0).getQuizId();
        if (quizId == null) {
            throw new BadRequestException("Quiz ID must be provided for all imported questions.");
        }

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", quizId));

        if (requests.stream().anyMatch(req -> req.getQuizId() == null || !quizId.equals(req.getQuizId()))) {
            throw new BadRequestException("All imported questions must belong to the same quiz.");
        }

        List<Question> questions = requests.stream().map(request -> Question.builder()
                .questionText(request.getQuestionText())
                .questionImage(request.getQuestionImage())
                .optionA(request.getOptionA())
                .optionB(request.getOptionB())
                .optionC(request.getOptionC())
                .optionD(request.getOptionD())
                .correctAnswer(request.getCorrectAnswer())
                .marks(request.getMarks())
                .quiz(quiz)
                .build())
                .collect(Collectors.toList());

        List<Question> saved = questionRepository.saveAll(questions);
        return saved.stream().map(q -> mapToResponse(q, true)).collect(Collectors.toList());
    }

    // ─── Get Questions By Quiz (Student — answers hidden) ─────────────────────

    public List<QuestionResponse> getQuestionsByQuiz(Long quizId) {
        if (!quizRepository.existsById(quizId)) {
            throw new ResourceNotFoundException("Quiz", quizId);
        }
        return questionRepository.findByQuizId(quizId)
                .stream()
                .map(q -> mapToResponse(q, false))
                .collect(Collectors.toList());
    }

    // ─── Get Questions By Quiz (Admin — answers visible) ──────────────────────

    public List<QuestionResponse> getQuestionsByQuizForAdmin(Long quizId) {
        if (!quizRepository.existsById(quizId)) {
            throw new ResourceNotFoundException("Quiz", quizId);
        }
        return questionRepository.findByQuizId(quizId)
                .stream()
                .map(q -> mapToResponse(q, true))
                .collect(Collectors.toList());
    }

    // ─── Update Question ──────────────────────────────────────────────────────

    /**
     * Update an existing question.
     *
     * Steps:
     *  1. Find the question → throw if not found.
     *  2. Update the fields provided (non-null fields only).
     *  3. If quizId is provided, re-link to the new quiz.
     *  4. Save and return updated QuestionResponse.
     */
    public QuestionResponse updateQuestion(Long id, QuestionRequest request) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question", id));

        if (request.getQuestionText() != null)  question.setQuestionText(request.getQuestionText());
        if (request.getQuestionImage() != null) question.setQuestionImage(request.getQuestionImage());
        if (request.getOptionA() != null)       question.setOptionA(request.getOptionA());
        if (request.getOptionB() != null)       question.setOptionB(request.getOptionB());
        if (request.getOptionC() != null)       question.setOptionC(request.getOptionC());
        if (request.getOptionD() != null)       question.setOptionD(request.getOptionD());
        if (request.getCorrectAnswer() != null) question.setCorrectAnswer(request.getCorrectAnswer());
        if (request.getMarks() != null)         question.setMarks(request.getMarks());

        if (request.getQuizId() != null) {
            Quiz quiz = quizRepository.findById(request.getQuizId())
                    .orElseThrow(() -> new ResourceNotFoundException("Quiz", request.getQuizId()));
            question.setQuiz(quiz);
        }

        return mapToResponse(questionRepository.save(question), true);
    }

    // ─── Delete Question ──────────────────────────────────────────────────────

    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Question", id);
        }
        questionRepository.deleteById(id);
    }

    // ─── Mapper ──────────────────────────────────────────────────────────────

    private QuestionResponse mapToResponse(Question question, boolean includeAnswer) {
        return QuestionResponse.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .questionImage(question.getQuestionImage())
                .optionA(question.getOptionA())
                .optionB(question.getOptionB())
                .optionC(question.getOptionC())
                .optionD(question.getOptionD())
                .correctAnswer(includeAnswer ? question.getCorrectAnswer() : null)
                .marks(question.getMarks())
                .quizId(question.getQuiz().getId())
                .build();
    }
}
