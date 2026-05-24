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

        List<TestCase> publicCases = test.getTestCases() != null ? 
            test.getTestCases().stream().filter(tc -> Boolean.FALSE.equals(tc.getIsHidden())).toList() : List.of();
            
        if (publicCases.isEmpty()) {
            publicCases = List.of(new TestCase(test.getSampleInput(), test.getSampleOutput(), false));
        }

        return executeTestCases(request, publicCases);
    }

    // ─── Submit Code ──────────────────────────────────────────────────────────

    public Map<String, Object> submitCode(CodeRunRequest request) {
        CodingTest test = codingTestRepository.findById(request.getCodingTestId())
                .orElseThrow(() -> new ResourceNotFoundException("CodingTest", request.getCodingTestId()));

        log.info("Submitting {} code for problem: {}", request.getLanguage(), test.getTitle());

        List<TestCase> allCases = test.getTestCases() != null ? test.getTestCases() : List.of();
            
        if (allCases.isEmpty()) {
            // fallback
            allCases = List.of(new TestCase(test.getSampleInput(), test.getSampleOutput(), true));
        }

        return executeTestCases(request, allCases);
    }

    private Map<String, Object> executeTestCases(CodeRunRequest request, List<TestCase> testCases) {
        long totalExecutionTimeMs = 0;
        int passedCount = 0;
        int totalTestCases = testCases.size();
        String firstFailedOutput = null;

        List<String> inputs = new java.util.ArrayList<>();
        for (TestCase tc : testCases) {
            inputs.add(tc.getInput());
        }

        long startTime = System.currentTimeMillis();
        List<String> outputs = compilerService.executeMultipleInputs(request.getCode(), request.getLanguage(), inputs);
        totalExecutionTimeMs = System.currentTimeMillis() - startTime;

        for (int i = 0; i < testCases.size(); i++) {
            TestCase tc = testCases.get(i);
            String output = outputs.get(i);

            String expected = tc.getExpectedOutput() != null ? tc.getExpectedOutput().trim() : "";
            String actual = output != null ? output.trim() : "";

            // Strip quotes from both expected and actual for resilient comparison
            if (expected.startsWith("\"") && expected.endsWith("\"") && expected.length() >= 2) {
                expected = expected.substring(1, expected.length() - 1);
            }
            if (actual.startsWith("\"") && actual.endsWith("\"") && actual.length() >= 2) {
                actual = actual.substring(1, actual.length() - 1);
            }

            if (actual.equals(expected)) {
                passedCount++;
            } else if (firstFailedOutput == null) {
                firstFailedOutput = output; // Keep original output for display
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

        // Enhanced GraphQL query — also fetches exampleTestcaseList and metaData
        String graphQLQuery = "query questionData($titleSlug: String!) { " +
                "question(titleSlug: $titleSlug) { " +
                "title " +
                "content " +
                "difficulty " +
                "sampleTestCase " +
                "exampleTestcaseList " +
                "metaData " +
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

            // exampleTestcaseList contains all example inputs (one per line group)
            Object rawExampleList = question.get("exampleTestcaseList");

            String difficulty = "EASY";
            if (difficultyStr != null) {
                if (difficultyStr.equalsIgnoreCase("Medium")) {
                    difficulty = "MEDIUM";
                } else if (difficultyStr.equalsIgnoreCase("Hard")) {
                    difficulty = "HARD";
                }
            }

            // ── Extract example I/O pairs from HTML content ──────────────────
            List<Map<String, String>> publicTestCases = extractExampleTestCases(content);

            // First sample output for the main sampleOutput field
            String sampleOutputStr = publicTestCases.isEmpty()
                    ? extractExpectedOutput(content)
                    : publicTestCases.get(0).get("expectedOutput");

            // Use sampleTestCase as sampleInput if not already extracted
            String sampleInputStr = sampleTestCase != null ? sampleTestCase : "";

            // ── Build internal test cases from exampleTestcaseList ─────────────
            // LeetCode exampleTestcaseList has one entry per example (multi-line inputs joined)
            List<Map<String, String>> internalTestCases = new java.util.ArrayList<>();
            if (rawExampleList instanceof List<?> exampleList) {
                for (int i = 0; i < Math.min(exampleList.size(), 10); i++) {
                    Object entry = exampleList.get(i);
                    if (entry instanceof String inputStr) {
                        // Match with the corresponding output extracted from HTML
                        String expectedOut = (i < publicTestCases.size())
                                ? publicTestCases.get(i).get("expectedOutput")
                                : "";
                        Map<String, String> tc = new java.util.HashMap<>();
                        tc.put("input", inputStr);
                        tc.put("expectedOutput", expectedOut != null ? expectedOut : "");
                        internalTestCases.add(tc);
                    }
                }
            }
            // Fallback: if no internal from exampleTestcaseList, use public ones as internal too
            if (internalTestCases.isEmpty() && !publicTestCases.isEmpty()) {
                internalTestCases.addAll(publicTestCases);
            }

            Map<String, Object> result = new java.util.HashMap<>();
            result.put("title", title != null ? title : "");
            result.put("description", content != null ? content : "");
            result.put("difficulty", difficulty);
            result.put("sampleInput", sampleInputStr);
            result.put("sampleOutput", sampleOutputStr != null ? sampleOutputStr : "");
            result.put("publicTestCases", publicTestCases);
            result.put("internalTestCases", internalTestCases);
            return result;

        } catch (Exception e) {
            log.error("Failed to import LeetCode question", e);
            throw new RuntimeException("Failed to import LeetCode question: " + e.getMessage());
        }
    }

    /**
     * Parses "Example N:" blocks from LeetCode HTML content.
     * Extracts Input and Output for each example.
     */
    /** Strips trailing explanation / constraints text that leaked into the output capture. */
    private String trimOutput(String raw) {
        if (raw == null) return "";
        // Stop at any of these common LeetCode section markers
        return raw.replaceAll("(?i)\\s*(?:Explanation|Constraints|Note|Follow up|Example\\s*\\d).*", "").trim();
    }

    private List<Map<String, String>> extractExampleTestCases(String content) {
        List<Map<String, String>> result = new java.util.ArrayList<>();
        if (content == null || content.isEmpty()) return result;

        try {
            // Decode HTML entities first so &quot; → " before any regex runs
            String decoded = content
                .replace("&quot;", "\"").replace("&#34;", "\"")
                .replace("&amp;", "&")
                .replace("&nbsp;", " ").replace("&#39;", "'");
            // Note: do NOT decode &lt;/&gt; — we still need tag detection below

            // Primary: match <strong>Input:</strong> ... <strong>Output:</strong> ...
            // Allows both <br/> and plain newline between Input and Output blocks
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "(?i)<strong>Input:</strong>\\s*(?:</strong>)?\\s*([^<]+?)\\s*(?:<br\\s*/?>|\\n)\\s*" +
                "<strong>Output:</strong>\\s*(?:</strong>)?\\s*(?:<code>)?\\s*([^<\\n]+)",
                java.util.regex.Pattern.DOTALL
            );
            java.util.regex.Matcher matcher = pattern.matcher(decoded);
            while (matcher.find()) {
                String input  = matcher.group(1).replaceAll("<[^>]+>", "").trim();
                String output = trimOutput(matcher.group(2).replaceAll("<[^>]+>", "").trim());
                Map<String, String> tc = new java.util.HashMap<>();
                tc.put("input", input);
                tc.put("expectedOutput", output);
                result.add(tc);
            }

            // Fallback: strip all tags, then match plain-text patterns
            if (result.isEmpty()) {
                String plainText = decoded.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ");
                java.util.regex.Pattern alt = java.util.regex.Pattern.compile(
                    "Input:\\s*([^\\n<]+?)[\\s\\S]*?Output:\\s*([^\\n<]+?)(?=\\s*(?:Explanation|Constraints|Note|Follow|Example\\s*\\d|$))"
                );
                java.util.regex.Matcher altM = alt.matcher(plainText);
                while (altM.find()) {
                    Map<String, String> tc = new java.util.HashMap<>();
                    tc.put("input", altM.group(1).trim());
                    tc.put("expectedOutput", trimOutput(altM.group(2).trim()));
                    result.add(tc);
                }
            }
        } catch (Exception e) {
            log.error("Error parsing example test cases from HTML", e);
        }
        return result;
    }


    private String extractExpectedOutput(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        try {
            // Decode entities before matching so &quot; → " etc.
            String decoded = content
                .replace("&quot;", "\"").replace("&#34;", "\"")
                .replace("&amp;", "&").replace("&lt;", "<")
                .replace("&gt;", ">").replace("&nbsp;", " ").replace("&#39;", "'");

            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "(?i)Output:\\s*(?:</strong>|</b>|</span>)?\\s*(?:<code>)?\\s*([^\\n<\\r]+)"
            );
            java.util.regex.Matcher matcher = pattern.matcher(decoded);
            if (matcher.find()) {
                return trimOutput(matcher.group(1).replaceAll("<[^>]+>", "").trim());
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
