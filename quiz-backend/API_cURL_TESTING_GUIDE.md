# 🔧 Quiz API - cURL Commands Testing Guide

> Test all API endpoints using cURL from command line or PowerShell

---

## 📋 Table of Contents

1. [Phase 1: Authentication](#phase-1-authentication)
2. [Phase 2: Admin Quiz & Questions](#phase-2-admin-quiz--questions)
3. [Phase 3: Student Browse](#phase-3-student-browse)
4. [Phase 4: Student Take Quiz](#phase-4-student-take-quiz)
5. [Phase 5: Results](#phase-5-results)
6. [Phase 6: Coding Tests](#phase-6-coding-tests)
7. [Phase 7: Admin Analytics](#phase-7-admin-analytics)

---

## ⚙️ Setup

### Windows PowerShell Users
Add this alias to make commands shorter (optional):
```powershell
$baseUrl = "http://localhost:8080"
```

### Linux/Mac Users
```bash
export BASE_URL="http://localhost:8080"
```

Then replace `http://localhost:8080` with `$BASE_URL` or `$baseUrl` in all commands

---

## ✅ Phase 1: Authentication

### 1.1 Register Alice (Student)
```bash
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Alice Johnson\",
    \"email\": \"alice@example.com\",
    \"password\": \"password123\",
    \"role\": \"STUDENT\"
  }"
```

**Save the returned ID (should be 1)**

### 1.2 Register Bob (Student)
```bash
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Bob Smith\",
    \"email\": \"bob@example.com\",
    \"password\": \"password123\",
    \"role\": \"STUDENT\"
  }"
```

### 1.3 Register Admin User
```bash
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Admin User\",
    \"email\": \"admin@example.com\",
    \"password\": \"adminpass123\",
    \"role\": \"ADMIN\"
  }"
```

### 1.4 Login Alice
```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"alice@example.com\",
    \"password\": \"password123\"
  }"
```

### 1.5 Login Admin
```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"admin@example.com\",
    \"password\": \"adminpass123\"
  }"
```

---

## 📝 Phase 2: Admin Quiz & Questions

### 2.1 Create Quiz - Java Fundamentals
```bash
curl -X POST http://localhost:8080/admin/quizzes \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"Java Fundamentals\",
    \"description\": \"Learn the basics of Java programming\",
    \"durationMinutes\": 30,
    \"totalMarks\": 100,
    \"active\": true
  }"
```

**Save Quiz ID (should be 1)**

### 2.2 Create Quiz - Python Basics
```bash
curl -X POST http://localhost:8080/admin/quizzes \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"Python Basics\",
    \"description\": \"Introduction to Python programming\",
    \"durationMinutes\": 45,
    \"totalMarks\": 100,
    \"active\": true
  }"
```

### 2.3 Add Question 1 to Quiz 1
```bash
curl -X POST http://localhost:8080/admin/questions \
  -H "Content-Type: application/json" \
  -d "{
    \"questionText\": \"What is JVM?\",
    \"optionA\": \"Java Virtual Machine\",
    \"optionB\": \"Java Variable Module\",
    \"optionC\": \"Java Version Manager\",
    \"optionD\": \"Just Virtual Machine\",
    \"correctAnswer\": \"A\",
    \"marks\": 10,
    \"quizId\": 1
  }"
```

### 2.4 Add Question 2
```bash
curl -X POST http://localhost:8080/admin/questions \
  -H "Content-Type: application/json" \
  -d "{
    \"questionText\": \"What does OOP stand for?\",
    \"optionA\": \"Object Oriented Programming\",
    \"optionB\": \"Object Oriented Procedure\",
    \"optionC\": \"Operational Object Programming\",
    \"optionD\": \"Object Operation Program\",
    \"correctAnswer\": \"A\",
    \"marks\": 10,
    \"quizId\": 1
  }"
```

### 2.5 Add Question 3
```bash
curl -X POST http://localhost:8080/admin/questions \
  -H "Content-Type: application/json" \
  -d "{
    \"questionText\": \"Which of these is NOT a primitive data type in Java?\",
    \"optionA\": \"int\",
    \"optionB\": \"String\",
    \"optionC\": \"boolean\",
    \"optionD\": \"double\",
    \"correctAnswer\": \"B\",
    \"marks\": 10,
    \"quizId\": 1
  }"
```

### 2.6 Add Question 4
```bash
curl -X POST http://localhost:8080/admin/questions \
  -H "Content-Type: application/json" \
  -d "{
    \"questionText\": \"What is the correct syntax for method declaration in Java?\",
    \"optionA\": \"void methodName() {}\",
    \"optionB\": \"function methodName() {}\",
    \"optionC\": \"def methodName() {}\",
    \"optionD\": \"method void methodName() {}\",
    \"correctAnswer\": \"A\",
    \"marks\": 10,
    \"quizId\": 1
  }"
```

### 2.7 Add Question 5
```bash
curl -X POST http://localhost:8080/admin/questions \
  -H "Content-Type: application/json" \
  -d "{
    \"questionText\": \"What is encapsulation?\",
    \"optionA\": \"Wrapping data and methods in a single unit\",
    \"optionB\": \"Inheriting properties from parent\",
    \"optionC\": \"Implementing multiple interfaces\",
    \"optionD\": \"Creating objects from classes\",
    \"correctAnswer\": \"A\",
    \"marks\": 10,
    \"quizId\": 1
  }"
```

### 2.8 Add Question 6 (Quiz 2)
```bash
curl -X POST http://localhost:8080/admin/questions \
  -H "Content-Type: application/json" \
  -d "{
    \"questionText\": \"What is Python?\",
    \"optionA\": \"A snake species\",
    \"optionB\": \"A high-level programming language\",
    \"optionC\": \"A type of programming paradigm\",
    \"optionD\": \"None of the above\",
    \"correctAnswer\": \"B\",
    \"marks\": 10,
    \"quizId\": 2
  }"
```

### 2.9 Add Question 7 (Quiz 2)
```bash
curl -X POST http://localhost:8080/admin/questions \
  -H "Content-Type: application/json" \
  -d "{
    \"questionText\": \"How do you create a list in Python?\",
    \"optionA\": \"list = [1, 2, 3]\",
    \"optionB\": \"list = (1, 2, 3)\",
    \"optionC\": \"list = {1, 2, 3}\",
    \"optionD\": \"list = <1, 2, 3>\",
    \"correctAnswer\": \"A\",
    \"marks\": 10,
    \"quizId\": 2
  }"
```

---

## 👁️ Phase 3: Student Browse

### 3.1 Get All Active Quizzes
```bash
curl -X GET http://localhost:8080/student/quizzes \
  -H "Content-Type: application/json"
```

### 3.2 Get Quiz Details (Quiz 1)
```bash
curl -X GET http://localhost:8080/student/quizzes/1 \
  -H "Content-Type: application/json"
```

### 3.3 Get Quiz Details (Quiz 2)
```bash
curl -X GET http://localhost:8080/student/quizzes/2 \
  -H "Content-Type: application/json"
```

---

## 🎯 Phase 4: Student Take Quiz

### 4.1 Start Quiz (Quiz 1, Student 1)
```bash
curl -X POST "http://localhost:8080/student/quizzes/start/1?studentId=1" \
  -H "Content-Type: application/json"
```

**Save the returned attemptId (should be 1)**

### 4.2 Get Questions for Quiz 1 (No Answers Shown)
```bash
curl -X GET http://localhost:8080/questions/quiz/1 \
  -H "Content-Type: application/json"
```

### 4.3 Submit Answer - Question 1
```bash
curl -X POST http://localhost:8080/student/quizzes/submit-answer \
  -H "Content-Type: application/json" \
  -d "{
    \"attemptId\": 1,
    \"questionId\": 1,
    \"selectedOption\": \"A\"
  }"
```

### 4.4 Submit Answer - Question 2
```bash
curl -X POST http://localhost:8080/student/quizzes/submit-answer \
  -H "Content-Type: application/json" \
  -d "{
    \"attemptId\": 1,
    \"questionId\": 2,
    \"selectedOption\": \"A\"
  }"
```

### 4.5 Submit Answer - Question 3
```bash
curl -X POST http://localhost:8080/student/quizzes/submit-answer \
  -H "Content-Type: application/json" \
  -d "{
    \"attemptId\": 1,
    \"questionId\": 3,
    \"selectedOption\": \"B\"
  }"
```

### 4.6 Submit Answer - Question 4
```bash
curl -X POST http://localhost:8080/student/quizzes/submit-answer \
  -H "Content-Type: application/json" \
  -d "{
    \"attemptId\": 1,
    \"questionId\": 4,
    \"selectedOption\": \"A\"
  }"
```

### 4.7 Submit Answer - Question 5
```bash
curl -X POST http://localhost:8080/student/quizzes/submit-answer \
  -H "Content-Type: application/json" \
  -d "{
    \"attemptId\": 1,
    \"questionId\": 5,
    \"selectedOption\": \"A\"
  }"
```

### 4.8 Submit Quiz (Mark as Complete)
```bash
curl -X POST http://localhost:8080/student/quizzes/submit/1 \
  -H "Content-Type: application/json"
```

---

## 📊 Phase 5: Results

### 5.1 Get Quiz Results
```bash
curl -X GET http://localhost:8080/student/results/1 \
  -H "Content-Type: application/json"
```

---

## 💻 Phase 6: Coding Tests

### 6.1 Create Coding Test (Admin)
```bash
curl -X POST http://localhost:8080/admin/coding-tests \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"Two Sum Problem\",
    \"description\": \"Given an array of integers and a target, find two numbers that add up to the target\",
    \"sampleInput\": \"nums = [2,7,11,15], target = 9\",
    \"sampleOutput\": \"[0,1]\",
    \"difficulty\": \"EASY\"
  }"
```

### 6.2 Get All Coding Tests (Student)
```bash
curl -X GET http://localhost:8080/student/coding-tests \
  -H "Content-Type: application/json"
```

### 6.3 Run Code
```bash
curl -X POST http://localhost:8080/student/code/run \
  -H "Content-Type: application/json" \
  -d "{
    \"codingTestId\": 1,
    \"language\": \"JAVA\",
    \"code\": \"public class Solution { public int[] twoSum(int[] nums, int target) { for(int i=0; i<nums.length; i++) { for(int j=i+1; j<nums.length; j++) { if(nums[i] + nums[j] == target) { return new int[]{i, j}; } } } return null; } }\"
  }"
```

### 6.4 Submit Code
```bash
curl -X POST http://localhost:8080/student/code/submit \
  -H "Content-Type: application/json" \
  -d "{
    \"codingTestId\": 1,
    \"language\": \"JAVA\",
    \"code\": \"public class Solution { public int[] twoSum(int[] nums, int target) { for(int i=0; i<nums.length; i++) { for(int j=i+1; j<nums.length; j++) { if(nums[i] + nums[j] == target) { return new int[]{i, j}; } } } return null; } }\"
  }"
```

### 6.5 Update Coding Test
```bash
curl -X PUT http://localhost:8080/admin/coding-tests/1 \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"Two Sum - Updated\",
    \"difficulty\": \"MEDIUM\"
  }"
```

### 6.6 Delete Coding Test
```bash
curl -X DELETE http://localhost:8080/admin/coding-tests/1 \
  -H "Content-Type: application/json"
```

---

## 📈 Phase 7: Admin Analytics

### 7.1 Get All Quizzes (Admin View)
```bash
curl -X GET http://localhost:8080/admin/quizzes \
  -H "Content-Type: application/json"
```

### 7.2 Get Questions for Admin (With Answers)
```bash
curl -X GET http://localhost:8080/admin/questions/quiz/1 \
  -H "Content-Type: application/json"
```

### 7.3 Update Quiz - Deactivate
```bash
curl -X PUT http://localhost:8080/admin/quizzes/1 \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"Java Fundamentals - Updated\",
    \"active\": false
  }"
```

### 7.4 Update Question
```bash
curl -X PUT http://localhost:8080/admin/questions/1 \
  -H "Content-Type: application/json" \
  -d "{
    \"questionText\": \"What is the JVM? (Updated)\",
    \"correctAnswer\": \"A\"
  }"
```

### 7.5 Get Admin Stats
```bash
curl -X GET http://localhost:8080/admin/stats \
  -H "Content-Type: application/json"
```

### 7.6 Get All Students
```bash
curl -X GET http://localhost:8080/admin/students \
  -H "Content-Type: application/json"
```

### 7.7 Get All Results
```bash
curl -X GET http://localhost:8080/admin/results \
  -H "Content-Type: application/json"
```

### 7.8 Delete Question
```bash
curl -X DELETE http://localhost:8080/admin/questions/5 \
  -H "Content-Type: application/json"
```

### 7.9 Delete Quiz
```bash
curl -X DELETE http://localhost:8080/admin/quizzes/2 \
  -H "Content-Type: application/json"
```

---

## 🎬 One-Liner Test Scripts

### Run All Authentication Tests
```bash
echo "Registering Alice..." && curl -s -X POST http://localhost:8080/register -H "Content-Type: application/json" -d "{\"name\":\"Alice Johnson\",\"email\":\"alice@example.com\",\"password\":\"password123\",\"role\":\"STUDENT\"}" && echo "" && echo "Registering Bob..." && curl -s -X POST http://localhost:8080/register -H "Content-Type: application/json" -d "{\"name\":\"Bob Smith\",\"email\":\"bob@example.com\",\"password\":\"password123\",\"role\":\"STUDENT\"}" && echo "" && echo "Registering Admin..." && curl -s -X POST http://localhost:8080/register -H "Content-Type: application/json" -d "{\"name\":\"Admin User\",\"email\":\"admin@example.com\",\"password\":\"adminpass123\",\"role\":\"ADMIN\"}"
```

### Check if Server is Running
```bash
curl -s http://localhost:8080/student/quizzes && echo "✓ Server is running!" || echo "✗ Server is down!"
```

### Get Admin Stats
```bash
curl -s http://localhost:8080/admin/stats | python -m json.tool
```

### List All Students
```bash
curl -s http://localhost:8080/admin/students | python -m json.tool
```

---

## 💡 PowerShell Tips

### For Windows PowerShell Users

Save this as a script `quiz-api-test.ps1`:
```powershell
$baseUrl = "http://localhost:8080"

# Function to make API calls
function Invoke-QuizAPI {
    param (
        [string]$Method,
        [string]$Endpoint,
        [object]$Body
    )
    
    $uri = "$baseUrl$Endpoint"
    $params = @{
        Method = $Method
        Uri = $uri
        ContentType = "application/json"
    }
    
    if ($Body) {
        $params['Body'] = $Body | ConvertTo-Json -Depth 10
    }
    
    Invoke-RestMethod @params
}

# Example: Register user
$user = @{
    name = "Alice Johnson"
    email = "alice@example.com"
    password = "password123"
    role = "STUDENT"
}

$result = Invoke-QuizAPI -Method "POST" -Endpoint "/register" -Body $user
$result
```

---

## 🔍 Pretty Print JSON Responses

### Linux/Mac
```bash
# Using jq (install with: brew install jq or apt-get install jq)
curl -s http://localhost:8080/student/quizzes | jq .

# Using python
curl -s http://localhost:8080/student/quizzes | python -m json.tool
```

### PowerShell
```powershell
$response = curl -s http://localhost:8080/student/quizzes
$response | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

---

## 📋 Response Validation Script (Bash)

Save as `validate-api.sh`:
```bash
#!/bin/bash
BASE_URL="http://localhost:8080"

echo "Testing Quiz API..."
echo "===================="

# Test 1: Register
echo "1. Testing Register..."
curl -s -X POST $BASE_URL/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com","password":"test123"}' \
  | grep -q "success" && echo "✓ Register works!" || echo "✗ Register failed!"

# Test 2: Get Quizzes
echo "2. Testing Get All Quizzes..."
curl -s -X GET $BASE_URL/student/quizzes \
  -H "Content-Type: application/json" \
  | grep -q "success" && echo "✓ Get Quizzes works!" || echo "✗ Get Quizzes failed!"

# Test 3: Get Stats
echo "3. Testing Get Stats..."
curl -s -X GET $BASE_URL/admin/stats \
  -H "Content-Type: application/json" \
  | grep -q "success" && echo "✓ Get Stats works!" || echo "✗ Get Stats failed!"

echo "===================="
echo "Testing complete!"
```

Run it:
```bash
chmod +x validate-api.sh
./validate-api.sh
```

---

## ⚡ Quick Test Commands

| Action | Command |
|--------|---------|
| Get all quizzes | `curl http://localhost:8080/student/quizzes` |
| Get stats | `curl http://localhost:8080/admin/stats` |
| Get students | `curl http://localhost:8080/admin/students` |
| Get results | `curl http://localhost:8080/admin/results` |
| Get q1 details | `curl http://localhost:8080/student/quizzes/1` |
| Get questions | `curl http://localhost:8080/questions/quiz/1` |

---

**Version:** 1.0 | **Last Updated:** May 13, 2026


