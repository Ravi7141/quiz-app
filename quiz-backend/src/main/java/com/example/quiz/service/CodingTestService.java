package com.example.quiz.service;

import com.example.quiz.dto.request.CodeRunRequest;
import com.example.quiz.dto.request.CodingTestRequest;
import com.example.quiz.dto.response.CodingTestResponse;
import com.example.quiz.entity.CodingTest;
import com.example.quiz.entity.User;
import com.example.quiz.exception.ResourceNotFoundException;
import com.example.quiz.repository.CodingTestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CodingTestService — handles list, create, update, delete, run, submit.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodingTestService {

    private final CodingTestRepository codingTestRepository;
    private final CompilerService compilerService;
    private final AuthService authService;

    // ─── List All ────────────────────────────────────────────────────────────

    public List<CodingTestResponse> getAllCodingTests() {
        User currentUser = authService.getCurrentUser();
        List<CodingTest> tests;
        if (currentUser != null) {
            if (currentUser.getRole() == com.example.quiz.enums.Role.ADMIN) {
                tests = codingTestRepository.findByCreatedById(currentUser.getId());
            } else if (currentUser.getCreatedBy() != null) {
                tests = codingTestRepository.findByCreatedById(currentUser.getCreatedBy().getId());
            } else {
                tests = codingTestRepository.findAll();
            }
        } else {
            tests = codingTestRepository.findAll();
        }
        return tests.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get By ID ───────────────────────────────────────────────────────────

    public CodingTestResponse getCodingTestById(Long id) {
        CodingTest test = codingTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CodingTest", id));
        return mapToResponse(test);
    }

    // ─── Create (Admin) ───────────────────────────────────────────────────────

    /**
     * Create a new coding problem.
     *
     * Steps:
     *  1. Map request → CodingTest entity.
     *  2. Save and return CodingTestResponse.
     */
    public CodingTestResponse createCodingTest(CodingTestRequest request) {
        User currentAdmin = authService.getCurrentUser();
        CodingTest test = CodingTest.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .sampleInput(request.getSampleInput())
                .sampleOutput(request.getSampleOutput())
                .difficulty(request.getDifficulty())
                .scheduledFor(request.getScheduledFor())
                .validUntil(request.getValidUntil())
                .createdBy(currentAdmin)
                .build();
        return mapToResponse(codingTestRepository.save(test));
    }

    // ─── Update (Admin) ───────────────────────────────────────────────────────

    /**
     * Update an existing coding problem.
     *
     * Steps:
     *  1. Find by id → throw if not found.
     *  2. Update non-null fields.
     *  3. Save and return updated response.
     */
    public CodingTestResponse updateCodingTest(Long id, CodingTestRequest request) {
        CodingTest test = codingTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CodingTest", id));

        if (request.getTitle() != null)        test.setTitle(request.getTitle());
        if (request.getDescription() != null)  test.setDescription(request.getDescription());
        if (request.getSampleInput() != null)  test.setSampleInput(request.getSampleInput());
        if (request.getSampleOutput() != null) test.setSampleOutput(request.getSampleOutput());
        if (request.getDifficulty() != null)   test.setDifficulty(request.getDifficulty());
        if (request.getScheduledFor() != null) test.setScheduledFor(request.getScheduledFor());
        if (request.getValidUntil() != null)   test.setValidUntil(request.getValidUntil());

        return mapToResponse(codingTestRepository.save(test));
    }

    // ─── Delete (Admin) ───────────────────────────────────────────────────────

    public void deleteCodingTest(Long id) {
        if (!codingTestRepository.existsById(id)) {
            throw new ResourceNotFoundException("CodingTest", id);
        }
        codingTestRepository.deleteById(id);
    }

    // ─── Run Code ─────────────────────────────────────────────────────────────

    public Map<String, Object> runCode(CodeRunRequest request) {
        CodingTest test = codingTestRepository.findById(request.getCodingTestId())
                .orElseThrow(() -> new ResourceNotFoundException("CodingTest", request.getCodingTestId()));

        log.info("Running {} code for problem: {}", request.getLanguage(), test.getTitle());

        long startTime = System.currentTimeMillis();
        String output = compilerService.executeCode(request.getCode(), request.getLanguage(), test.getSampleInput());
        long executionTimeMs = System.currentTimeMillis() - startTime;

        return Map.of(
                "status", "EXECUTED",
                "output", output,
                "expectedOutput", test.getSampleOutput() == null ? "" : test.getSampleOutput(),
                "executionTimeMs", executionTimeMs,
                "language", request.getLanguage()
        );
    }

    // ─── Submit Code ──────────────────────────────────────────────────────────

    public Map<String, Object> submitCode(CodeRunRequest request) {
        CodingTest test = codingTestRepository.findById(request.getCodingTestId())
                .orElseThrow(() -> new ResourceNotFoundException("CodingTest", request.getCodingTestId()));

        log.info("Submitting {} code for problem: {}", request.getLanguage(), test.getTitle());

        long startTime = System.currentTimeMillis();
        String output = compilerService.executeCode(request.getCode(), request.getLanguage(), test.getSampleInput());
        long executionTimeMs = System.currentTimeMillis() - startTime;

        boolean passed = false;
        String expected = test.getSampleOutput() != null ? test.getSampleOutput().trim() : "";
        if (output.trim().equals(expected)) {
            passed = true;
        }

        return Map.of(
                "status", passed ? "ACCEPTED" : "FAILED",
                "message", passed ? "All test cases passed!" : "Output did not match expected output.",
                "testCasesPassed", passed ? 1 : 0,
                "totalTestCases", 1,
                "executionTimeMs", executionTimeMs,
                "output", output,
                "language", request.getLanguage()
        );
    }

    // ─── Mapper ──────────────────────────────────────────────────────────────

    private CodingTestResponse mapToResponse(CodingTest test) {
        return CodingTestResponse.builder()
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
