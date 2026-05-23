package com.example.quiz.controller;

import com.example.quiz.dto.request.QuizRequest;
import com.example.quiz.dto.response.QuizResponse;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
public class AdminQuizControllerTest {

    @Mock
    private QuizService quizService;

    @InjectMocks
    private AdminQuizController adminQuizController;

    private QuizRequest validQuizRequest;
    private QuizResponse quizResponse;

    @BeforeEach
    void setUp() {
        validQuizRequest = new QuizRequest();
        validQuizRequest.setTitle("Java Fundamentals");
        validQuizRequest.setDescription("Learn the basics of Java");
        validQuizRequest.setDurationMinutes(30);
        validQuizRequest.setTotalMarks(100);
        validQuizRequest.setActive(true);

        quizResponse = QuizResponse.builder()
                .id(1L)
                .title("Java Fundamentals")
                .description("Learn the basics of Java")
                .durationMinutes(30)
                .totalMarks(100)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    public void testCreateQuizSuccess() {
        when(quizService.createQuiz(any(QuizRequest.class))).thenReturn(quizResponse);
        var result = adminQuizController.createQuiz(validQuizRequest);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertTrue(result.getBody().isSuccess());
        assertEquals("Java Fundamentals", result.getBody().getData().getTitle());
    }

    @Test
    public void testGetAllQuizzesSuccess() {
        List<QuizResponse> quizzes = Arrays.asList(quizResponse);
        when(quizService.getAllQuizzes()).thenReturn(quizzes);
        var result = adminQuizController.getAllQuizzes();
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getData().size());
    }

    @Test
    public void testGetAllQuizzesEmpty() {
        when(quizService.getAllQuizzes()).thenReturn(Collections.emptyList());
        var result = adminQuizController.getAllQuizzes();
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(0, result.getBody().getData().size());
    }

    @Test
    public void testGetQuizByIdSuccess() {
        when(quizService.getQuizById(1L)).thenReturn(quizResponse);
        var result = adminQuizController.getQuizById(1L);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Java Fundamentals", result.getBody().getData().getTitle());
    }

    @Test
    public void testGetQuizByIdNotFound() {
        when(quizService.getQuizById(999L))
                .thenThrow(new ResourceNotFoundException("Quiz", 999L));
        assertThrows(ResourceNotFoundException.class, () -> adminQuizController.getQuizById(999L));
    }

    @Test
    public void testUpdateQuizSuccess() {
        QuizRequest updateRequest = new QuizRequest();
        updateRequest.setTitle("Updated Quiz");
        updateRequest.setDurationMinutes(45);

        QuizResponse updatedResponse = QuizResponse.builder()
                .id(1L)
                .title("Updated Quiz")
                .description("Learn the basics of Java")
                .durationMinutes(45)
                .totalMarks(100)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(quizService.updateQuiz(eq(1L), any(QuizRequest.class))).thenReturn(updatedResponse);
        var result = adminQuizController.updateQuiz(1L, updateRequest);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Updated Quiz", result.getBody().getData().getTitle());
        assertEquals(45, result.getBody().getData().getDurationMinutes());
    }

    @Test
    public void testUpdateQuizNotFound() {
        when(quizService.updateQuiz(eq(999L), any(QuizRequest.class)))
                .thenThrow(new ResourceNotFoundException("Quiz", 999L));
        assertThrows(ResourceNotFoundException.class, () -> adminQuizController.updateQuiz(999L, validQuizRequest));
    }

    @Test
    public void testDeleteQuizSuccess() {
        doNothing().when(quizService).deleteQuiz(1L);
        var result = adminQuizController.deleteQuiz(1L);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isSuccess());
    }

    @Test
    public void testDeleteQuizNotFound() {
        doThrow(new ResourceNotFoundException("Quiz", 999L)).when(quizService).deleteQuiz(999L);
        assertThrows(ResourceNotFoundException.class, () -> adminQuizController.deleteQuiz(999L));
    }
}
