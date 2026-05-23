package com.example.quiz.service;

import com.example.quiz.dto.request.QuizRequest;
import com.example.quiz.dto.response.QuizResponse;
import com.example.quiz.entity.Quiz;
import com.example.quiz.entity.User;
import com.example.quiz.enums.Role;
import com.example.quiz.exception.ResourceNotFoundException;
import com.example.quiz.repository.QuestionRepository;
import com.example.quiz.repository.QuizAttemptRepository;
import com.example.quiz.repository.QuizRepository;
import com.example.quiz.repository.StudentAnswerRepository;
import com.example.quiz.repository.CodingTestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Mock
    private StudentAnswerRepository studentAnswerRepository;

    @Mock
    private CodingTestRepository codingTestRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private QuizService quizService;

    private Quiz quiz;
    private QuizRequest quizRequest;
    private User admin;

    @BeforeEach
    void setUp() {
        admin = User.builder()
                .id(1L)
                .name("Admin User")
                .email("admin@example.com")
                .role(Role.ADMIN)
                .build();

        quiz = Quiz.builder()
                .id(1L)
                .title("Java Fundamentals")
                .description("Learn basics of Java")
                .durationMinutes(30)
                .totalMarks(100)
                .active(true)
                .createdBy(admin)
                .createdAt(LocalDateTime.now())
                .build();

        quizRequest = new QuizRequest();
        quizRequest.setTitle("Java Fundamentals");
        quizRequest.setDescription("Learn basics of Java");
        quizRequest.setDurationMinutes(30);
        quizRequest.setTotalMarks(100);
        quizRequest.setActive(true);
    }

    // ========== Create Quiz Tests ==========

    @Test
    public void testCreateQuizSuccess() {
        when(authService.getCurrentUser()).thenReturn(admin);
        when(quizRepository.save(any(Quiz.class))).thenReturn(quiz);

        QuizResponse response = quizService.createQuiz(quizRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Java Fundamentals", response.getTitle());
        assertEquals(30, response.getDurationMinutes());
        assertEquals(100, response.getTotalMarks());
        assertTrue(response.getActive());
        verify(quizRepository, times(1)).save(any(Quiz.class));
    }

    @Test
    public void testCreateQuizWithoutDescription() {
        QuizRequest requestNoDesc = new QuizRequest();
        requestNoDesc.setTitle("Java Fundamentals");
        requestNoDesc.setDurationMinutes(30);
        requestNoDesc.setTotalMarks(100);
        requestNoDesc.setActive(true);

        when(authService.getCurrentUser()).thenReturn(admin);
        when(quizRepository.save(any(Quiz.class))).thenReturn(quiz);

        QuizResponse response = quizService.createQuiz(requestNoDesc);

        assertNotNull(response);
        assertEquals("Java Fundamentals", response.getTitle());
        verify(quizRepository, times(1)).save(any(Quiz.class));
    }

    @Test
    public void testCreateQuizInactiveByDefault() {
        QuizRequest requestInactive = new QuizRequest();
        requestInactive.setTitle("Inactive Quiz");
        requestInactive.setDurationMinutes(30);
        requestInactive.setTotalMarks(100);
        requestInactive.setActive(false);

        Quiz inactiveQuiz = Quiz.builder()
                .id(2L)
                .title("Inactive Quiz")
                .durationMinutes(30)
                .totalMarks(100)
                .active(false)
                .createdBy(admin)
                .createdAt(LocalDateTime.now())
                .build();

        when(authService.getCurrentUser()).thenReturn(admin);
        when(quizRepository.save(any(Quiz.class))).thenReturn(inactiveQuiz);

        QuizResponse response = quizService.createQuiz(requestInactive);

        assertNotNull(response);
        assertFalse(response.getActive());
        verify(quizRepository, times(1)).save(any(Quiz.class));
    }

    // ========== Get All Quizzes Tests ==========

    @Test
    public void testGetAllQuizzesSuccess() {
        Quiz quiz2 = Quiz.builder()
                .id(2L)
                .title("Python Basics")
                .description("Learn Python")
                .durationMinutes(45)
                .totalMarks(80)
                .active(true)
                .createdBy(admin)
                .createdAt(LocalDateTime.now())
                .build();

        when(authService.getCurrentUser()).thenReturn(admin);
        when(quizRepository.findByCreatedById(1L)).thenReturn(Arrays.asList(quiz, quiz2));

        List<QuizResponse> responses = quizService.getAllQuizzes();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Java Fundamentals", responses.get(0).getTitle());
        assertEquals("Python Basics", responses.get(1).getTitle());
        verify(quizRepository, times(1)).findByCreatedById(1L);
    }

    @Test
    public void testGetAllQuizzesEmpty() {
        when(authService.getCurrentUser()).thenReturn(admin);
        when(quizRepository.findByCreatedById(1L)).thenReturn(Collections.emptyList());

        List<QuizResponse> responses = quizService.getAllQuizzes();

        assertNotNull(responses);
        assertEquals(0, responses.size());
        verify(quizRepository, times(1)).findByCreatedById(1L);
    }

    @Test
    public void testGetAllQuizzesNoCurrentUser() {
        when(authService.getCurrentUser()).thenReturn(null);
        when(quizRepository.findAll()).thenReturn(Arrays.asList(quiz));

        List<QuizResponse> responses = quizService.getAllQuizzes();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(quizRepository, times(1)).findAll();
    }

    // ========== Get Quiz By ID Tests ==========

    @Test
    public void testGetQuizByIdSuccess() {
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));

        QuizResponse response = quizService.getQuizById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Java Fundamentals", response.getTitle());
        verify(quizRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetQuizByIdNotFound() {
        when(quizRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            quizService.getQuizById(999L);
        });
        verify(quizRepository, times(1)).findById(999L);
    }

    // ========== Update Quiz Tests ==========

    @Test
    public void testUpdateQuizSuccess() {
        QuizRequest updateRequest = new QuizRequest();
        updateRequest.setTitle("Updated Quiz");
        updateRequest.setDurationMinutes(45);

        Quiz updatedQuiz = Quiz.builder()
                .id(1L)
                .title("Updated Quiz")
                .description("Learn basics of Java")
                .durationMinutes(45)
                .totalMarks(100)
                .active(true)
                .createdBy(admin)
                .createdAt(LocalDateTime.now())
                .build();

        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(quizRepository.save(any(Quiz.class))).thenReturn(updatedQuiz);

        QuizResponse response = quizService.updateQuiz(1L, updateRequest);

        assertNotNull(response);
        assertEquals("Updated Quiz", response.getTitle());
        assertEquals(45, response.getDurationMinutes());
        verify(quizRepository, times(1)).findById(1L);
        verify(quizRepository, times(1)).save(any(Quiz.class));
    }

    @Test
    public void testUpdateQuizPartialUpdate() {
        QuizRequest partialRequest = new QuizRequest();
        partialRequest.setActive(false);

        Quiz updatedQuiz = Quiz.builder()
                .id(1L)
                .title("Java Fundamentals")
                .description("Learn basics of Java")
                .durationMinutes(30)
                .totalMarks(100)
                .active(false)
                .createdBy(admin)
                .createdAt(LocalDateTime.now())
                .build();

        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(quizRepository.save(any(Quiz.class))).thenReturn(updatedQuiz);

        QuizResponse response = quizService.updateQuiz(1L, partialRequest);

        assertNotNull(response);
        assertEquals("Java Fundamentals", response.getTitle());
        assertFalse(response.getActive());
        assertEquals(30, response.getDurationMinutes());
        verify(quizRepository, times(1)).findById(1L);
    }

    @Test
    public void testUpdateQuizNotFound() {
        when(quizRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            quizService.updateQuiz(999L, quizRequest);
        });
        verify(quizRepository, times(1)).findById(999L);
    }

    // ========== Delete Quiz Tests ==========

    @Test
    public void testDeleteQuizSuccess() {
        when(quizRepository.existsById(1L)).thenReturn(true);
        when(quizAttemptRepository.findByQuizId(1L)).thenReturn(Collections.emptyList());
        doNothing().when(quizRepository).deleteById(1L);

        assertDoesNotThrow(() -> quizService.deleteQuiz(1L));
        verify(quizRepository, times(1)).existsById(1L);
        verify(quizRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteQuizNotFound() {
        when(quizRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            quizService.deleteQuiz(999L);
        });
        verify(quizRepository, times(1)).existsById(999L);
        verify(quizRepository, never()).deleteById(any());
    }

    // ========== Get Active Quizzes Tests ==========

    @Test
    public void testGetActiveQuizzesSuccess() {
        when(authService.getCurrentUser()).thenReturn(null);
        when(quizRepository.findByActiveTrue()).thenReturn(Arrays.asList(quiz));

        List<QuizResponse> responses = quizService.getActiveQuizzes();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertTrue(responses.get(0).getActive());
        verify(quizRepository, times(1)).findByActiveTrue();
    }

    @Test
    public void testGetActiveQuizzesEmpty() {
        when(authService.getCurrentUser()).thenReturn(null);
        when(quizRepository.findByActiveTrue()).thenReturn(Collections.emptyList());

        List<QuizResponse> responses = quizService.getActiveQuizzes();

        assertNotNull(responses);
        assertEquals(0, responses.size());
    }
}

