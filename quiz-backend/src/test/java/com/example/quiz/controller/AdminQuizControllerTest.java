package com.example.quiz.controller;

import com.example.quiz.dto.request.QuizRequest;
import com.example.quiz.dto.response.ApiResponse;
import com.example.quiz.dto.response.QuizResponse;
import com.example.quiz.exception.ResourceNotFoundException;
import com.example.quiz.service.QuizService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminQuizController.class)
public class AdminQuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuizService quizService;

    @Autowired
    private ObjectMapper objectMapper;

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

    // ========== Create Quiz Tests ==========

    @Test
    public void testCreateQuizSuccess() throws Exception {
        when(quizService.createQuiz(any(QuizRequest.class)))
                .thenReturn(quizResponse);

        mockMvc.perform(post("/admin/quizzes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validQuizRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Quiz created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Java Fundamentals"))
                .andExpect(jsonPath("$.data.totalMarks").value(100));
    }

    @Test
    public void testCreateQuizMissingTitle() throws Exception {
        QuizRequest invalidRequest = new QuizRequest();
        invalidRequest.setDescription("Learn the basics of Java");
        invalidRequest.setDurationMinutes(30);
        invalidRequest.setTotalMarks(100);
        invalidRequest.setActive(true);

        mockMvc.perform(post("/admin/quizzes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateQuizMissingDuration() throws Exception {
        QuizRequest invalidRequest = new QuizRequest();
        invalidRequest.setTitle("Java Fundamentals");
        invalidRequest.setDescription("Learn the basics of Java");
        invalidRequest.setTotalMarks(100);
        invalidRequest.setActive(true);

        mockMvc.perform(post("/admin/quizzes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateQuizMissingTotalMarks() throws Exception {
        QuizRequest invalidRequest = new QuizRequest();
        invalidRequest.setTitle("Java Fundamentals");
        invalidRequest.setDescription("Learn the basics of Java");
        invalidRequest.setDurationMinutes(30);
        invalidRequest.setActive(true);

        mockMvc.perform(post("/admin/quizzes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateQuizInvalidDurationNegative() throws Exception {
        QuizRequest invalidRequest = new QuizRequest();
        invalidRequest.setTitle("Java Fundamentals");
        invalidRequest.setDescription("Learn the basics of Java");
        invalidRequest.setDurationMinutes(-5);
        invalidRequest.setTotalMarks(100);
        invalidRequest.setActive(true);

        mockMvc.perform(post("/admin/quizzes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateQuizInvalidMarksNegative() throws Exception {
        QuizRequest invalidRequest = new QuizRequest();
        invalidRequest.setTitle("Java Fundamentals");
        invalidRequest.setDescription("Learn the basics of Java");
        invalidRequest.setDurationMinutes(30);
        invalidRequest.setTotalMarks(-10);
        invalidRequest.setActive(true);

        mockMvc.perform(post("/admin/quizzes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateQuizInvalidDurationZero() throws Exception {
        QuizRequest invalidRequest = new QuizRequest();
        invalidRequest.setTitle("Java Fundamentals");
        invalidRequest.setDescription("Learn the basics of Java");
        invalidRequest.setDurationMinutes(0);
        invalidRequest.setTotalMarks(100);
        invalidRequest.setActive(true);

        mockMvc.perform(post("/admin/quizzes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // ========== Get All Quizzes Tests ==========

    @Test
    public void testGetAllQuizzesSuccess() throws Exception {
        List<QuizResponse> quizzes = Arrays.asList(quizResponse);
        when(quizService.getAllQuizzes()).thenReturn(quizzes);

        mockMvc.perform(get("/admin/quizzes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("All quizzes fetched successfully"))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Java Fundamentals"));
    }

    @Test
    public void testGetAllQuizzesEmpty() throws Exception {
        when(quizService.getAllQuizzes()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/quizzes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    // ========== Get Quiz By ID Tests ==========

    @Test
    public void testGetQuizByIdSuccess() throws Exception {
        when(quizService.getQuizById(1L)).thenReturn(quizResponse);

        mockMvc.perform(get("/admin/quizzes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Java Fundamentals"));
    }

    @Test
    public void testGetQuizByIdNotFound() throws Exception {
        when(quizService.getQuizById(999L))
                .thenThrow(new ResourceNotFoundException("Quiz", 999L));

        mockMvc.perform(get("/admin/quizzes/999"))
                .andExpect(status().isNotFound());
    }

    // ========== Update Quiz Tests ==========

    @Test
    public void testUpdateQuizSuccess() throws Exception {
        QuizRequest updateRequest = new QuizRequest();
        updateRequest.setTitle("Updated Quiz Title");
        updateRequest.setDurationMinutes(45);

        QuizResponse updatedResponse = QuizResponse.builder()
                .id(1L)
                .title("Updated Quiz Title")
                .description("Learn the basics of Java")
                .durationMinutes(45)
                .totalMarks(100)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(quizService.updateQuiz(eq(1L), any(QuizRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/admin/quizzes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Updated Quiz Title"))
                .andExpect(jsonPath("$.data.durationMinutes").value(45));
    }

    @Test
    public void testUpdateQuizNotFound() throws Exception {
        when(quizService.updateQuiz(eq(999L), any(QuizRequest.class)))
                .thenThrow(new ResourceNotFoundException("Quiz", 999L));

        mockMvc.perform(put("/admin/quizzes/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validQuizRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateQuizInvalidDuration() throws Exception {
        QuizRequest invalidRequest = new QuizRequest();
        invalidRequest.setDurationMinutes(-1);

        mockMvc.perform(put("/admin/quizzes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // ========== Delete Quiz Tests ==========

    @Test
    public void testDeleteQuizSuccess() throws Exception {
        doNothing().when(quizService).deleteQuiz(1L);

        mockMvc.perform(delete("/admin/quizzes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Quiz deleted successfully"));
    }

    @Test
    public void testDeleteQuizNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Quiz", 999L))
                .when(quizService).deleteQuiz(999L);

        mockMvc.perform(delete("/admin/quizzes/999"))
                .andExpect(status().isNotFound());
    }
}

