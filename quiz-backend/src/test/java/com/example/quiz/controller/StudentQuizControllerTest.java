package com.example.quiz.controller;

import com.example.quiz.dto.response.ApiResponse;
import com.example.quiz.dto.response.QuizResponse;
import com.example.quiz.dto.response.AssessmentResponse;
import com.example.quiz.exception.ResourceNotFoundException;
import com.example.quiz.service.QuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentQuizController.class)
public class StudentQuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuizService quizService;

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

    // ========== Get Active Quizzes Tests ==========

    @Test
    public void testGetQuizzesSuccess() throws Exception {
        List<QuizResponse> quizzes = Arrays.asList(quizResponse);
        when(quizService.getActiveQuizzes()).thenReturn(quizzes);

        mockMvc.perform(get("/student/quizzes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Quizzes fetched successfully"))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Java Fundamentals"))
                .andExpect(jsonPath("$.data[0].active").value(true));
    }

    @Test
    public void testGetQuizzesMultiple() throws Exception {
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

        mockMvc.perform(get("/student/quizzes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("Java Fundamentals"))
                .andExpect(jsonPath("$.data[1].title").value("Python Basics"));
    }

    @Test
    public void testGetQuizzesEmpty() throws Exception {
        when(quizService.getActiveQuizzes()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/student/quizzes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    public void testGetQuizzesOnlyActive() throws Exception {
        List<QuizResponse> quizzes = Arrays.asList(quizResponse);
        when(quizService.getActiveQuizzes()).thenReturn(quizzes);

        mockMvc.perform(get("/student/quizzes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].active").value(true));
    }

    // ========== Get Quiz By ID Tests ==========

    @Test
    public void testGetQuizByIdSuccess() throws Exception {
        when(quizService.getQuizById(1L)).thenReturn(quizResponse);

        mockMvc.perform(get("/student/quizzes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Quiz fetched successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Java Fundamentals"));
    }

    @Test
    public void testGetQuizByIdNotFound() throws Exception {
        when(quizService.getQuizById(999L))
                .thenThrow(new ResourceNotFoundException("Quiz", 999L));

        mockMvc.perform(get("/student/quizzes/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetQuizByIdInvalidId() throws Exception {
        when(quizService.getQuizById(0L))
                .thenThrow(new ResourceNotFoundException("Quiz", 0L));

        mockMvc.perform(get("/student/quizzes/0"))
                .andExpect(status().isNotFound());
    }

    // ========== Get Assessment Tests ==========

    @Test
    public void testGetAssessmentSuccess() throws Exception {
        when(quizService.getAssessment(1L)).thenReturn(assessmentResponse);

        mockMvc.perform(get("/student/quizzes/1/assessment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Assessment content fetched successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Java Fundamentals"));
    }

    @Test
    public void testGetAssessmentNotFound() throws Exception {
        when(quizService.getAssessment(999L))
                .thenThrow(new ResourceNotFoundException("Exam", 999L));

        mockMvc.perform(get("/student/quizzes/999/assessment"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAssessmentInvalidId() throws Exception {
        when(quizService.getAssessment(-1L))
                .thenThrow(new ResourceNotFoundException("Exam", -1L));

        mockMvc.perform(get("/student/quizzes/-1/assessment"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAssessmentCorrectContent() throws Exception {
        when(quizService.getAssessment(1L)).thenReturn(assessmentResponse);

        mockMvc.perform(get("/student/quizzes/1/assessment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.durationMinutes").value(30))
                .andExpect(jsonPath("$.data.active").value(true));
    }
}

