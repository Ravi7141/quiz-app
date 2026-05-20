package com.example.quiz.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CompilerService {

    @Value("${judge0.enabled:false}")
    private boolean judge0Enabled;

    @Value("${judge0.url:http://localhost:2358}")
    private String judge0Url;

    @Value("${judge0.api-key:}")
    private String judge0ApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String executeCode(String code, String language, String input) {
        if (judge0Enabled) {
            try {
                return executeWithJudge0(code, language, input);
            } catch (Exception e) {
                log.error("Judge0 execution failed, falling back to local compiler execution", e);
                // fall back to local execution
            }
        }
        return executeLocally(code, language, input);
    }

    private String executeWithJudge0(String code, String language, String input) throws Exception {
        int languageId = getJudge0LanguageId(language);
        if (languageId == -1) {
            throw new IllegalArgumentException("Unsupported language for Judge0: " + language);
        }

        // Prepare request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("source_code", code);
        requestBody.put("language_id", languageId);
        requestBody.put("stdin", input != null ? input : "");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (judge0ApiKey != null && !judge0ApiKey.trim().isEmpty()) {
            headers.set("X-RapidAPI-Key", judge0ApiKey.trim());
            headers.set("X-RapidAPI-Host", "judge0-ce.p.rapidapi.com");
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        String submitUrl = judge0Url.trim() + "/submissions?base64_encoded=false&wait=true";

        log.info("Sending request to Judge0: {}", submitUrl);
        ResponseEntity<Map> response = restTemplate.postForEntity(submitUrl, entity, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<String, Object> body = response.getBody();
            log.info("Judge0 response body: {}", body);

            Map<String, Object> status = (Map<String, Object>) body.get("status");
            int statusId = status != null ? (Integer) status.get("id") : 3;

            if (statusId == 6) { // Compilation Error
                String compileOutput = (String) body.get("compile_output");
                return "Compile Error:\n" + (compileOutput != null ? compileOutput : "Unknown Compilation Error");
            }

            if (statusId == 3) { // Accepted
                String stdout = (String) body.get("stdout");
                return stdout != null ? stdout.trim() : "";
            }

            // Other errors (Runtime error, Time Limit Exceeded, etc.)
            String stderr = (String) body.get("stderr");
            String message = (String) body.get("message");
            String statusDesc = status != null ? (String) status.get("description") : "Execution Failed";

            StringBuilder errBuilder = new StringBuilder();
            errBuilder.append("Execution error (").append(statusDesc).append(")");
            if (stderr != null && !stderr.isEmpty()) {
                errBuilder.append(":\n").append(stderr);
            } else if (message != null && !message.isEmpty()) {
                errBuilder.append(":\n").append(message);
            }
            return errBuilder.toString().trim();
        } else {
            throw new RuntimeException("HTTP request failed with status: " + response.getStatusCode());
        }
    }

    private int getJudge0LanguageId(String language) {
        switch (language.toLowerCase()) {
            case "java":
                return 91; // JDK 17
            case "python":
            case "python3":
                return 71; // Python 3.8.1
            case "javascript":
            case "node":
            case "js":
                return 93; // Node.js 18.15.0
            case "cpp":
            case "c++":
                return 75; // Clang 9
            default:
                return -1;
        }
    }

    private String executeLocally(String code, String language, String input) {
        String output;
        try {
            Path tempDir = Files.createTempDirectory("code_exec_");
            switch (language.toLowerCase()) {
                case "java":
                    output = runJava(tempDir, code, input);
                    break;
                case "python":
                case "python3":
                    output = runPython(tempDir, code, input);
                    break;
                case "javascript":
                case "node":
                case "js":
                    output = runJavaScript(tempDir, code, input);
                    break;
                case "cpp":
                case "c++":
                    output = runCpp(tempDir, code, input);
                    break;
                default:
                    return "Language not supported: " + language;
            }
            // Cleanup
            deleteDirectory(tempDir.toFile());
        } catch (Exception e) {
            log.error("Execution error", e);
            output = "Execution Error: " + e.getMessage();
        }
        return output;
    }

    private String runPython(Path dir, String code, String input) throws Exception {
        Path scriptPath = dir.resolve("script.py");
        Files.writeString(scriptPath, code);
        return executeCommand(dir.toFile(), input, "python", "script.py");
    }

    private String runJavaScript(Path dir, String code, String input) throws Exception {
        Path scriptPath = dir.resolve("script.js");
        Files.writeString(scriptPath, code);
        return executeCommand(dir.toFile(), input, "node", "script.js");
    }

    private String runCpp(Path dir, String code, String input) throws Exception {
        Path sourcePath = dir.resolve("main.cpp");
        Files.writeString(sourcePath, code);

        String compileResult = executeCommand(dir.toFile(), null, "g++", "main.cpp", "-o", "main.exe");
        if (!compileResult.isEmpty() && compileResult.toLowerCase().contains("error")) {
            return "Compile Error:\n" + compileResult;
        }
        return executeCommand(dir.toFile(), input, "main.exe");
    }

    private String runJava(Path dir, String code, String input) throws Exception {
        String className = "Main";
        if (code.contains("public class ")) {
            int start = code.indexOf("public class ") + 13;
            int end = code.indexOf("{", start);
            if (end > start) {
                className = code.substring(start, end).trim();
                if (className.contains("<")) className = className.substring(0, className.indexOf("<")).trim();
                if (className.contains("implements")) className = className.substring(0, className.indexOf("implements")).trim();
                if (className.contains("extends")) className = className.substring(0, className.indexOf("extends")).trim();
            }
        }
        Path sourcePath = dir.resolve(className + ".java");
        Files.writeString(sourcePath, code);

        String compileResult = executeCommand(dir.toFile(), null, "javac", "-cp", ".", className + ".java");
        if (!compileResult.isEmpty() && compileResult.toLowerCase().contains("error")) {
            return "Compile Error:\n" + compileResult;
        }
        return executeCommand(dir.toFile(), input, "java", "-cp", ".", className);
    }

    private String executeCommand(File dir, String input, String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(dir);
        pb.redirectErrorStream(true);

        Process process;
        try {
            process = pb.start();
        } catch (IOException e) {
            return "Command not found or failed to start: " + String.join(" ", command) + "\nMake sure the compiler/runtime is installed and in your system PATH.";
        }

        if (input != null && !input.isEmpty()) {
            try (OutputStream os = process.getOutputStream()) {
                os.write(input.getBytes());
                os.flush();
            } catch (IOException ignored) {}
        }

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        boolean finished = process.waitFor(5, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            return output.toString() + "\nError: Execution Timed Out (5s limit)";
        }

        return output.toString().trim();
    }

    private void deleteDirectory(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteDirectory(f);
                }
            }
        }
        file.delete();
    }
}
