package com.example.quiz.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CompilerService {

    public String executeCode(String code, String language, String input) {
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
        // On Windows, 'python' usually works. On Linux, it might be 'python3'. We'll try python first.
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
        // Find public class name
        String className = "Main";
        if (code.contains("public class ")) {
            int start = code.indexOf("public class ") + 13;
            int end = code.indexOf("{", start);
            if (end > start) {
                className = code.substring(start, end).trim();
                // Handle interfaces or generic brackets if present
                if (className.contains("<")) className = className.substring(0, className.indexOf("<")).trim();
                if (className.contains("implements")) className = className.substring(0, className.indexOf("implements")).trim();
                if (className.contains("extends")) className = className.substring(0, className.indexOf("extends")).trim();
            }
        }
        Path sourcePath = dir.resolve(className + ".java");
        Files.writeString(sourcePath, code);

        String compileResult = executeCommand(dir.toFile(), null, "javac", className + ".java");
        if (!compileResult.isEmpty() && compileResult.toLowerCase().contains("error")) {
            return "Compile Error:\n" + compileResult;
        }
        return executeCommand(dir.toFile(), input, "java", className);
    }

    private String executeCommand(File dir, String input, String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(dir);
        pb.redirectErrorStream(true); // Merge stderr into stdout
        
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
