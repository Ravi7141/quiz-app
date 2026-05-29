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
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
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

        // Serialize manually — avoids RestTemplate converter issues with Map<String,Object>
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        String jsonBody = mapper.writeValueAsString(java.util.Map.of(
                "source_code", code,
                "language_id", languageId,
                "stdin", input != null ? input : ""
        ));
        log.info("Judge0 request — language: {}, languageId: {}, url: {}, codeLen: {}", language, languageId, judge0Url, code.length());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
        java.net.URI submitUri = java.net.URI.create(judge0Url.trim() + "/submissions?base64_encoded=false&wait=true");

        log.info("Sending to Judge0: {}", submitUri);
        ResponseEntity<Map> response = restTemplate.exchange(submitUri, org.springframework.http.HttpMethod.POST, entity, Map.class);

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
                return 62; // Java (OpenJDK 13.0.1)
            case "python":
            case "python3":
                return 71; // Python (3.8.1)
            case "javascript":
            case "node":
            case "js":
                return 63; // JavaScript (Node.js 12.14.0)
            case "cpp":
            case "c++":
                return 54; // C++ (GCC 9.2.0)
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

        String compilerPath = "g++";
        if (new java.io.File(System.getProperty("user.home") + "/mingw64/bin/g++.exe").exists()) {
            compilerPath = System.getProperty("user.home") + "/mingw64/bin/g++.exe";
        } else if (new java.io.File("C:/mingw64/bin/g++.exe").exists()) {
            compilerPath = "C:/mingw64/bin/g++.exe";
        }

        String compileResult = executeCommand(dir.toFile(), null, 20, compilerPath, "-static", "main.cpp", "-o", "main.exe");
        if (compileResult.contains("Command not found")) {
            log.info("g++ not found, falling back to Piston API for C++");
            return runWithPistonAPI("c++", "10.2.0", code, input);
        }
        if (!compileResult.isEmpty() && compileResult.toLowerCase().contains("error")) {
            return "Compile Error:\n" + compileResult;
        }
        return executeCommand(dir.toFile(), input, dir.toFile().getAbsolutePath() + java.io.File.separator + "main.exe");
    }

    private String runWithPistonAPI(String language, String version, String code, String input) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("language", language);
            request.put("version", version);
            
            Map<String, String> file = new HashMap<>();
            file.put("content", code);
            request.put("files", java.util.Arrays.asList(file));
            
            if (input != null && !input.isEmpty()) {
                request.put("stdin", input);
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity("https://emkc.org/api/v2/piston/execute", entity, Map.class);
            Map<String, Object> body = response.getBody();
            if (body != null) {
                if (body.containsKey("compile")) {
                    Map<String, Object> compile = (Map<String, Object>) body.get("compile");
                    Integer codeStatus = (Integer) compile.get("code");
                    if (codeStatus != null && codeStatus != 0) {
                        return "Compile Error:\n" + compile.get("output");
                    }
                }
                Map<String, Object> run = (Map<String, Object>) body.get("run");
                if (run != null) {
                    return (String) run.get("output");
                }
            }
            return "Execution failed via Cloud API.";
        } catch (Exception ex) {
            log.error("Piston API execution failed", ex);
            return "Execution Failed: Cloud compiler unavailable.";
        }
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

        String compileResult = executeCommand(dir.toFile(), null, 20, "javac", "-cp", ".", className + ".java");
        if (!compileResult.isEmpty() && compileResult.toLowerCase().contains("error")) {
            return "Compile Error:\n" + compileResult;
        }
        return executeCommand(dir.toFile(), input, "java", "-cp", ".", className);
    }

    private String executeCommand(File dir, String input, String... command) throws Exception {
        return executeCommand(dir, input, 8, command); // Default 8 seconds
    }

    private String executeCommand(File dir, String input, long timeoutSeconds, String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(dir);
        pb.redirectErrorStream(true);
        File outputFile = new File(dir, "output.txt");
        pb.redirectOutput(outputFile);

        Process process;
        try {
            process = pb.start();
        } catch (IOException e) {
            return "Command not found or failed to start: " + String.join(" ", command) + "\nMake sure the compiler/runtime is installed and in your system PATH.";
        }

        try (OutputStream os = process.getOutputStream()) {
            if (input != null && !input.isEmpty()) {
                os.write(input.getBytes());
                os.flush();
            }
        } catch (IOException ignored) {}

        boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            String partialOutput = "";
            if (outputFile.exists()) {
                partialOutput = Files.readString(outputFile.toPath());
            }
            return partialOutput + "\nError: Execution Timed Out (" + timeoutSeconds + "s limit)";
        }

        if (outputFile.exists()) {
            return Files.readString(outputFile.toPath()).trim();
        }
        return "";
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

    public List<String> executeMultipleInputs(String code, String language, List<String> inputs) {
        if (judge0Enabled) {
            List<String> results = new ArrayList<>();
            for (String input : inputs) {
                try {
                    results.add(executeWithJudge0(code, language, input));
                } catch (Exception e) {
                    log.error("Judge0 execution failed", e);
                    results.add("Execution Error: " + e.getMessage());
                }
            }
            return results;
        }
        return executeLocallyMultiple(code, language, inputs);
    }

    private List<String> executeLocallyMultiple(String code, String language, List<String> inputs) {
        List<String> results = new ArrayList<>();
        try {
            Path tempDir = Files.createTempDirectory("code_exec_");
            switch (language.toLowerCase()) {
                case "java":
                    results = runJavaMultiple(tempDir, code, inputs);
                    break;
                case "python":
                case "python3":
                    results = runPythonMultiple(tempDir, code, inputs);
                    break;
                case "javascript":
                case "node":
                case "js":
                    results = runJavaScriptMultiple(tempDir, code, inputs);
                    break;
                case "cpp":
                case "c++":
                    results = runCppMultiple(tempDir, code, inputs);
                    break;
                default:
                    return Collections.nCopies(inputs.size(), "Language not supported: " + language);
            }
            deleteDirectory(tempDir.toFile());
        } catch (Exception e) {
            log.error("Execution error", e);
            return Collections.nCopies(inputs.size(), "Execution Error: " + e.getMessage());
        }
        return results;
    }

    private List<String> runJavaMultiple(Path dir, String code, List<String> inputs) throws Exception {
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

        String compileResult = executeCommand(dir.toFile(), null, 20, "javac", "-cp", ".", className + ".java");
        if (!compileResult.isEmpty() && compileResult.toLowerCase().contains("error")) {
            return Collections.nCopies(inputs.size(), "Compile Error:\n" + compileResult);
        }

        List<String> outputs = new ArrayList<>();
        for (String input : inputs) {
            outputs.add(executeCommand(dir.toFile(), input, "java", "-cp", ".", className));
        }
        return outputs;
    }

    private List<String> runPythonMultiple(Path dir, String code, List<String> inputs) throws Exception {
        Path scriptPath = dir.resolve("script.py");
        Files.writeString(scriptPath, code);
        List<String> outputs = new ArrayList<>();
        for (String input : inputs) {
            outputs.add(executeCommand(dir.toFile(), input, "python", "script.py"));
        }
        return outputs;
    }

    private List<String> runJavaScriptMultiple(Path dir, String code, List<String> inputs) throws Exception {
        Path scriptPath = dir.resolve("script.js");
        Files.writeString(scriptPath, code);
        List<String> outputs = new ArrayList<>();
        for (String input : inputs) {
            outputs.add(executeCommand(dir.toFile(), input, "node", "script.js"));
        }
        return outputs;
    }

    private List<String> runCppMultiple(Path dir, String code, List<String> inputs) throws Exception {
        Path sourcePath = dir.resolve("main.cpp");
        Files.writeString(sourcePath, code);

        String compilerPath = "g++";
        if (new java.io.File(System.getProperty("user.home") + "/mingw64/bin/g++.exe").exists()) {
            compilerPath = System.getProperty("user.home") + "/mingw64/bin/g++.exe";
        } else if (new java.io.File("C:/mingw64/bin/g++.exe").exists()) {
            compilerPath = "C:/mingw64/bin/g++.exe";
        }

        String compileResult = executeCommand(dir.toFile(), null, 20, compilerPath, "-static", "main.cpp", "-o", "main.exe");
        if (compileResult.contains("Command not found")) {
            log.info("g++ not found, falling back to Piston API for C++");
            List<String> outputs = new ArrayList<>();
            for (String input : inputs) {
                outputs.add(runWithPistonAPI("c++", "10.2.0", code, input));
            }
            return outputs;
        }
        if (!compileResult.isEmpty() && compileResult.toLowerCase().contains("error")) {
            return Collections.nCopies(inputs.size(), "Compile Error:\n" + compileResult);
        }
        
        List<String> outputs = new ArrayList<>();
        for (String input : inputs) {
            outputs.add(executeCommand(dir.toFile(), input, dir.toFile().getAbsolutePath() + java.io.File.separator + "main.exe"));
        }
        return outputs;
    }
}
