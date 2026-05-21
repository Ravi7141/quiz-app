package com.example.quiz.service;

import com.example.quiz.dto.request.CodeRunRequest;
import com.example.quiz.dto.request.CodingTestRequest;
import com.example.quiz.dto.response.CodingTestResponse;
import com.example.quiz.entity.CodingTest;
import com.example.quiz.entity.TestCase;
import com.example.quiz.entity.User;
import com.example.quiz.exception.ResourceNotFoundException;
import com.example.quiz.repository.CodingTestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

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
                .testCases(request.getTestCases() != null ? request.getTestCases() : new java.util.ArrayList<>())
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
        if (request.getTestCases() != null) {
            test.getTestCases().clear();
            test.getTestCases().addAll(request.getTestCases());
        }

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

        long totalExecutionTimeMs = 0;
        int passedCount = 0;
        
        List<TestCase> testCases = test.getTestCases();
        if (testCases == null || testCases.isEmpty()) {
            // Fallback to sample input if no test cases defined
            testCases = List.of(new TestCase(test.getSampleInput(), test.getSampleOutput()));
        }
        
        int totalTestCases = testCases.size();
        String firstFailedOutput = null;

        for (TestCase tc : testCases) {
            long startTime = System.currentTimeMillis();
            String output = compilerService.executeCode(request.getCode(), request.getLanguage(), tc.getInput());
            totalExecutionTimeMs += (System.currentTimeMillis() - startTime);

            String expected = tc.getExpectedOutput() != null ? tc.getExpectedOutput().trim() : "";
            if (output.trim().equals(expected)) {
                passedCount++;
            } else if (firstFailedOutput == null) {
                firstFailedOutput = output;
            }
        }

        boolean allPassed = (passedCount == totalTestCases && totalTestCases > 0);
        String finalOutput = allPassed ? "All test cases passed!" : (firstFailedOutput != null ? firstFailedOutput : "Output did not match expected output.");

        return Map.of(
                "status", allPassed ? "ACCEPTED" : "FAILED",
                "message", allPassed ? "All test cases passed!" : "Some test cases failed.",
                "testCasesPassed", passedCount,
                "totalTestCases", totalTestCases,
                "executionTimeMs", totalExecutionTimeMs,
                "output", finalOutput,
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
                .testCases(test.getTestCases())
                .build();
    }

    public Map<String, Object> importLeetCodeQuestion(String queryStr) {
        String slug = extractSlug(queryStr);
        if (slug.isEmpty()) {
            throw new IllegalArgumentException("Invalid LeetCode URL or Slug");
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://leetcode.com/graphql";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

        String graphQLQuery = "query questionData($titleSlug: String!) { " +
                "question(titleSlug: $titleSlug) { " +
                "title " +
                "content " +
                "difficulty " +
                "sampleTestCase " +
                "} " +
                "}";

        Map<String, Object> variables = Map.of("titleSlug", slug);
        Map<String, Object> requestBody = Map.of(
                "query", graphQLQuery,
                "variables", variables
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            Map<String, Object> body = response.getBody();
            if (body == null || !body.containsKey("data")) {
                throw new RuntimeException("Failed to fetch data from LeetCode. Response was empty.");
            }

            Map<String, Object> data = (Map<String, Object>) body.get("data");
            Map<String, Object> question = (Map<String, Object>) data.get("question");
            if (question == null) {
                throw new RuntimeException("Question not found on LeetCode with slug: " + slug);
            }

            String title = (String) question.get("title");
            String content = (String) question.get("content");
            String difficultyStr = (String) question.get("difficulty");
            String sampleTestCase = (String) question.get("sampleTestCase");

            String difficulty = "EASY";
            if (difficultyStr != null) {
                if (difficultyStr.equalsIgnoreCase("Medium")) {
                    difficulty = "MEDIUM";
                } else if (difficultyStr.equalsIgnoreCase("Hard")) {
                    difficulty = "HARD";
                }
            }

            String sampleOutputStr = extractExpectedOutput(content);

            return Map.of(
                    "title", title != null ? title : "",
                    "description", content != null ? content : "",
                    "difficulty", difficulty,
                    "sampleInput", sampleTestCase != null ? sampleTestCase : "",
                    "sampleOutput", sampleOutputStr
            );
        } catch (Exception e) {
            log.error("Failed to import LeetCode question", e);
            throw new RuntimeException("Failed to import LeetCode question: " + e.getMessage());
        }
    }

    private String extractExpectedOutput(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        try {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "(?i)Output:\\s*(?:</strong>|</b>|</span>)?\\s*(?:<code>)?\\s*([^\\n<\\r]+)"
            );
            java.util.regex.Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String match = matcher.group(1).trim();
                return cleanHtmlEntities(match);
            }
        } catch (Exception e) {
            log.error("Error extracting expected output from content", e);
        }
        return "";
    }

    private String cleanHtmlEntities(String text) {
        if (text == null) return "";
        return text.replace("&quot;", "\"")
                   .replace("&amp;", "&")
                   .replace("&lt;", "<")
                   .replace("&gt;", ">")
                   .replace("&nbsp;", " ")
                   .trim();
    }

    private String extractSlug(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        input = input.trim();
        if (input.contains("leetcode.com/problems/")) {
            int index = input.indexOf("leetcode.com/problems/");
            String rest = input.substring(index + "leetcode.com/problems/".length());
            if (rest.contains("/")) {
                String[] parts = rest.split("/");
                for (String part : parts) {
                    if (!part.trim().isEmpty()) {
                        return part;
                    }
                }
            }
            return rest;
        }
        return input;
    }
}
