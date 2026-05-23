package com.example.quiz.controller;

import com.example.quiz.dto.response.QuizResponse;
import com.example.quiz.dto.response.AssessmentResponse;
import com.example.quiz.exception.ResourceNotFoundException;
import com.example.quiz.service.QuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StudentQuizControllerTest {

    @Mock
    private QuizService quizService;

    @InjectMocks
    private StudentQuizController studentQuizController;

    private QuizResponse quizResponse;
    private AssessmentResponse assessmentResponse;

    @BeforeEach
    void setUp() {
        quizResponse = QuizResponse.builder()
                .id(1L)
                .title("Java Fundamentals")
                .description("Learn the basics of Java")
                .durationMinutes(30)
                .totalMarks(100)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        assessmentResponse = AssessmentResponse.builder()
                .id(1L)
                .title("Java Fundamentals")
                .description("Learn the basics of Java")
                .durationMinutes(30)
                .active(true)
                .build();
    }

    @Test
    public void testGetQuizzesSuccess() {
        List<QuizResponse> quizzes = Arrays.asList(quizResponse);
        when(quizService.getActiveQuizzes()).thenReturn(quizzes);
        var result = studentQuizController.getQuizzes();
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getData().size());
        assertTrue(result.getBody().getData().get(0).getActive());
    }

    @Test
    public void testGetQuizzesMultiple() {
        QuizResponse quiz2 = QuizResponse.builder()
                .id(2L)
                .title("Python Basics")
                .description("Learn Python")
                .durationMinutes(45)
                .totalMarks(80)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        List<QuizResponse> quizzes = Arrays.asList(quizResponse, quiz2);
        when(quizService.getActiveQuizzes()).thenReturn(quizzes);
        var result = studentQuizController.getQuizzes();
        assertEquals(2, result.getBody().getData().size());
        assertEquals("Java Fundamentals", result.getBody().getData().get(0).getTitle());
        assertEquals("Python Basics", result.getBody().getData().get(1).getTitle());
    }

    @Test
    public void testGetQuizzesEmpty() {
        when(quizService.getActiveQuizzes()).thenReturn(Collections.emptyList());
        var result = studentQuizController.getQuizzes();
        assertEquals(0, result.getBody().getData().size());
    }

    @Test
    public void testGetQuizByIdSuccess() {
        when(quizService.getQuizById(1L)).thenReturn(quizResponse);
        var result = studentQuizController.getQuizById(1L);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Java Fundamentals", result.getBody().getData().getTitle());
    }

    @Test
    public void testGetQuizByIdNotFound() {
        when(quizService.getQuizById(999L))
                .thenThrow(new ResourceNotFoundException("Quiz", 999L));
        assertThrows(ResourceNotFoundException.class, () -> studentQuizController.getQuizById(999L));
    }

    @Test
    public void testGetAssessmentSuccess() {
        when(quizService.getAssessment(1L)).thenReturn(assessmentResponse);
        var result = studentQuizController.getAssessment(1L);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Java Fundamentals", result.getBody().getData().getTitle());
    }

    @Test
    public void testGetAssessmentNotFound() {
        when(quizService.getAssessment(999L))
                .thenThrow(new ResourceNotFoundException("Exam", 999L));
        assertThrows(ResourceNotFoundException.class, () -> studentQuizController.getAssessment(999L));
    }

    @Test
    public void testGetAssessmentCorrectContent() {
        when(quizService.getAssessment(1L)).thenReturn(assessmentResponse);
        var result = studentQuizController.getAssessment(1L);
        assertEquals(30, result.getBody().getData().getDurationMinutes());
        assertTrue(result.getBody().getData().getActive());
    }
}
