# 🧪 Quiz Application - Complete API Testing Guide

> **Date:** May 13, 2026 | **Base URL:** `http://localhost:8080`

---

## 📋 Table of Contents
1. [Flow Overview](#flow-overview)
2. [Phase 1: Authentication & User Setup](#phase-1-authentication--user-setup)
3. [Phase 2: Admin - Create Quiz & Questions](#phase-2-admin---create-quiz--questions)
4. [Phase 3: Student - Browse & View Quizzes](#phase-3-student---browse--view-quizzes)
5. [Phase 4: Student - Take Quiz & Submit Answers](#phase-4-student---take-quiz--submit-answers)
6. [Phase 5: Student - View Results](#phase-5-student---view-results)
7. [Phase 6: Coding Tests (Optional)](#phase-6-coding-tests-optional)
8. [Phase 7: Admin - Monitor & Analytics](#phase-7-admin---monitor--analytics)

---

## 🔄 Flow Overview

```
USER REGISTRATION
       ↓
USER LOGIN (Save user ID & role)
       ↓
[IF ADMIN] ─→ CREATE QUIZ → ADD QUESTIONS
       ↓
[IF STUDENT] ─→ VIEW QUIZZES → START QUIZ → ANSWER QUESTIONS → SUBMIT QUIZ → VIEW RESULTS
       ↓
[IF ADMIN] ─→ VIEW STATS/RESULTS/STUDENTS
```

---

## ✅ Phase 1: Authentication & User Setup

### 1.1 Register a Student User

**Endpoint:** `POST /register`

```json
{
  "name": "Alice Johnson",
  "email": "alice@example.com",
  "password": "password123",
  "role": "STUDENT"
}
```

**Expected Response (201 Created):**
```json
{
  "success": true,
  "message": "Registered successfully",
  "data": {
    "id": 1,
    "name": "Alice Johnson",
    "email": "alice@example.com",
    "role": "STUDENT",
    "token": "eyJhbGc..."
  }
}
```

**⚠️ Important:** Save the `id` (1) - you'll use it for all student operations!

---

### 1.2 Register More Students

**Endpoint:** `POST /register`

```json
{
  "name": "Bob Smith",
  "email": "bob@example.com",
  "password": "password123",
  "role": "STUDENT"
}
```

**Expected Response:** User ID 2

---

### 1.3 Register an Admin User

**Endpoint:** `POST /register`

```json
{
  "name": "Admin User",
  "email": "admin@example.com",
  "password": "adminpass123",
  "role": "ADMIN"
}
```

**Expected Response:** User ID 3

---

### 1.4 Login as Student (Alice)

**Endpoint:** `POST /login`

```json
{
  "email": "alice@example.com",
  "password": "password123"
}
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "id": 1,
    "name": "Alice Johnson",
    "email": "alice@example.com",
    "role": "STUDENT",
    "token": "eyJhbGc..."
  }
}
```

---

### 1.5 Login as Admin

**Endpoint:** `POST /login`

```json
{
  "email": "admin@example.com",
  "password": "adminpass123"
}
```

**Expected Response:** User ID 3, Token, Role: ADMIN

---

## 📝 Phase 2: Admin - Create Quiz & Questions

**Admin ID:** 3 (from registration above)

### 2.1 Create a Quiz

**Endpoint:** `POST /admin/quizzes`

```json
{
  "title": "Java Fundamentals",
  "description": "Learn the basics of Java programming",
  "durationMinutes": 30,
  "totalMarks": 100,
  "active": true
}
```

**Expected Response (201 Created):**
```json
{
  "success": true,
  "message": "Quiz created successfully",
  "data": {
    "id": 1,
    "title": "Java Fundamentals",
    "description": "Learn the basics of Java programming",
    "durationMinutes": 30,
    "totalMarks": 100,
    "active": true,
    "createdAt": "2026-05-13T10:00:00"
  }
}
```

**⚠️ Save Quiz ID:** 1

---

### 2.2 Create Second Quiz

**Endpoint:** `POST /admin/quizzes`

```json
{
  "title": "Python Basics",
  "description": "Introduction to Python programming",
  "durationMinutes": 45,
  "totalMarks": 100,
  "active": true
}
```

**Expected Response:** Quiz ID 2

---

### 2.3 Add Question 1 to Quiz 1

**Endpoint:** `POST /admin/questions`

```json
{
  "questionText": "What is JVM?",
  "optionA": "Java Virtual Machine",
  "optionB": "Java Variable Module",
  "optionC": "Java Version Manager",
  "optionD": "Just Virtual Machine",
  "correctAnswer": "A",
  "marks": 10,
  "quizId": 1
}
```

**Expected Response (201 Created):**
```json
{
  "success": true,
  "message": "Question added successfully",
  "data": {
    "id": 1,
    "questionText": "What is JVM?",
    "optionA": "Java Virtual Machine",
    "optionB": "Java Variable Module",
    "optionC": "Java Version Manager",
    "optionD": "Just Virtual Machine",
    "correctAnswer": "A",
    "marks": 10,
    "quizId": 1
  }
}
```

**⚠️ Save Question ID:** 1

---

### 2.4 Add Question 2 to Quiz 1

**Endpoint:** `POST /admin/questions`

```json
{
  "questionText": "What does OOP stand for?",
  "optionA": "Object Oriented Programming",
  "optionB": "Object Oriented Procedure",
  "optionC": "Operational Object Programming",
  "optionD": "Object Operation Program",
  "correctAnswer": "A",
  "marks": 10,
  "quizId": 1
}
```

**Expected Response:** Question ID 2

---

### 2.5 Add Question 3 to Quiz 1

**Endpoint:** `POST /admin/questions`

```json
{
  "questionText": "Which of these is NOT a primitive data type in Java?",
  "optionA": "int",
  "optionB": "String",
  "optionC": "boolean",
  "optionD": "double",
  "correctAnswer": "B",
  "marks": 10,
  "quizId": 1
}
```

**Expected Response:** Question ID 3

---

### 2.6 Add Question 4 to Quiz 1

**Endpoint:** `POST /admin/questions`

```json
{
  "questionText": "What is the correct syntax for method declaration in Java?",
  "optionA": "void methodName() {}",
  "optionB": "function methodName() {}",
  "optionC": "def methodName() {}",
  "optionD": "method void methodName() {}",
  "correctAnswer": "A",
  "marks": 10,
  "quizId": 1
}
```

**Expected Response:** Question ID 4

---

### 2.7 Add Question 5 to Quiz 1

**Endpoint:** `POST /admin/questions`

```json
{
  "questionText": "What is encapsulation?",
  "optionA": "Wrapping data and methods in a single unit",
  "optionB": "Inheriting properties from parent",
  "optionC": "Implementing multiple interfaces",
  "optionD": "Creating objects from classes",
  "correctAnswer": "A",
  "marks": 10,
  "quizId": 1
}
```

**Expected Response:** Question ID 5

---

### 2.8 Add Questions to Quiz 2 (Python Basics)

**Endpoint:** `POST /admin/questions`

```json
{
  "questionText": "What is Python?",
  "optionA": "A snake species",
  "optionB": "A high-level programming language",
  "optionC": "A type of programming paradigm",
  "optionD": "None of the above",
  "correctAnswer": "B",
  "marks": 10,
  "quizId": 2
}
```

**Expected Response:** Question ID 6

---

### 2.9 Add More Questions to Quiz 2

**Endpoint:** `POST /admin/questions`

```json
{
  "questionText": "How do you create a list in Python?",
  "optionA": "list = [1, 2, 3]",
  "optionB": "list = (1, 2, 3)",
  "optionC": "list = {1, 2, 3}",
  "optionD": "list = <1, 2, 3>",
  "correctAnswer": "A",
  "marks": 10,
  "quizId": 2
}
```

**Expected Response:** Question ID 7

---

## 👁️ Phase 3: Student - Browse & View Quizzes

**Student ID:** 1 (Alice)

### 3.1 Get All Active Quizzes (Student View)

**Endpoint:** `GET /student/quizzes`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Quizzes fetched successfully",
  "data": [
    {
      "id": 1,
      "title": "Java Fundamentals",
      "description": "Learn the basics of Java programming",
      "durationMinutes": 30,
      "totalMarks": 100,
      "active": true
    },
    {
      "id": 2,
      "title": "Python Basics",
      "description": "Introduction to Python programming",
      "durationMinutes": 45,
      "totalMarks": 100,
      "active": true
    }
  ]
}
```

---

### 3.2 Get Quiz Details (Quiz 1)

**Endpoint:** `GET /student/quizzes/1`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Quiz fetched successfully",
  "data": {
    "id": 1,
    "title": "Java Fundamentals",
    "description": "Learn the basics of Java programming",
    "durationMinutes": 30,
    "totalMarks": 100,
    "active": true,
    "questionCount": 5
  }
}
```

---

## 🎯 Phase 4: Student - Take Quiz & Submit Answers

**Student ID:** 1 (Alice) | **Quiz ID:** 1

### 4.1 Start Quiz

**Endpoint:** `POST /student/quizzes/start/1?studentId=1`

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Quiz started successfully",
  "data": {
    "attemptId": 1,
    "quizId": 1,
    "quizTitle": "Java Fundamentals",
    "studentId": 1,
    "studentName": "Alice Johnson",
    "status": "IN_PROGRESS",
    "startedAt": "2026-05-13T10:15:00",
    "durationMinutes": 30
  }
}
```

**⚠️ Save Attempt ID:** 1

---

### 4.2 Fetch Questions for Quiz (with answers hidden)

**Endpoint:** `GET /questions/quiz/1`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Questions fetched successfully",
  "data": [
    {
      "id": 1,
      "questionText": "What is JVM?",
      "optionA": "Java Virtual Machine",
      "optionB": "Java Variable Module",
      "optionC": "Java Version Manager",
      "optionD": "Just Virtual Machine",
      "marks": 10,
      "quizId": 1,
      "correctAnswer": null
    },
    {
      "id": 2,
      "questionText": "What does OOP stand for?",
      "optionA": "Object Oriented Programming",
      "optionB": "Object Oriented Procedure",
      "optionC": "Operational Object Programming",
      "optionD": "Object Operation Program",
      "marks": 10,
      "quizId": 1,
      "correctAnswer": null
    }
  ]
}
```

**Note:** `correctAnswer` is `null` for students!

---

### 4.3 Submit Answer to Question 1

**Endpoint:** `POST /student/quizzes/submit-answer`

```json
{
  "attemptId": 1,
  "questionId": 1,
  "selectedOption": "A"
}
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Answer recorded successfully"
}
```

---

### 4.4 Submit Answer to Question 2

**Endpoint:** `POST /student/quizzes/submit-answer`

```json
{
  "attemptId": 1,
  "questionId": 2,
  "selectedOption": "A"
}
```

---

### 4.5 Submit Answer to Question 3

**Endpoint:** `POST /student/quizzes/submit-answer`

```json
{
  "attemptId": 1,
  "questionId": 3,
  "selectedOption": "B"
}
```

---

### 4.6 Submit Answer to Question 4

**Endpoint:** `POST /student/quizzes/submit-answer`

```json
{
  "attemptId": 1,
  "questionId": 4,
  "selectedOption": "A"
}
```

---

### 4.7 Submit Answer to Question 5

**Endpoint:** `POST /student/quizzes/submit-answer`

```json
{
  "attemptId": 1,
  "questionId": 5,
  "selectedOption": "A"
}
```

---

### 4.8 Submit Quiz (Mark as Complete)

**Endpoint:** `POST /student/quizzes/submit/1`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Quiz submitted successfully",
  "data": {
    "attemptId": 1,
    "quizId": 1,
    "studentId": 1,
    "status": "SUBMITTED",
    "submittedAt": "2026-05-13T10:35:00"
  }
}
```

---

## 📊 Phase 5: Student - View Results

**Attempt ID:** 1 (from Phase 4)

### 5.1 Get Quiz Result

**Endpoint:** `GET /student/results/1`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Result fetched successfully",
  "data": {
    "attemptId": 1,
    "quizId": 1,
    "quizTitle": "Java Fundamentals",
    "studentId": 1,
    "studentName": "Alice Johnson",
    "totalQuestions": 5,
    "correctAnswers": 5,
    "score": 50,
    "totalMarks": 100,
    "percentage": 50.0,
    "passed": true,
    "submittedAt": "2026-05-13T10:35:00"
  }
}
```

---

## 💻 Phase 6: Coding Tests (Optional)

### 6.1 Create Coding Test (Admin)

**Endpoint:** `POST /admin/coding-tests`

```json
{
  "title": "Two Sum Problem",
  "description": "Given an array of integers and a target, find two numbers that add up to the target",
  "sampleInput": "nums = [2,7,11,15], target = 9",
  "sampleOutput": "[0,1]",
  "difficulty": "EASY"
}
```

**Expected Response (201 Created):**
```json
{
  "success": true,
  "message": "Coding test created successfully",
  "data": {
    "id": 1,
    "title": "Two Sum Problem",
    "description": "Given an array of integers and a target, find two numbers that add up to the target",
    "sampleInput": "nums = [2,7,11,15], target = 9",
    "sampleOutput": "[0,1]",
    "difficulty": "EASY",
    "createdAt": "2026-05-13T10:00:00"
  }
}
```

---

### 6.2 Get All Coding Tests (Student)

**Endpoint:** `GET /student/coding-tests`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Coding tests fetched successfully",
  "data": [
    {
      "id": 1,
      "title": "Two Sum Problem",
      "description": "Given an array of integers and a target, find two numbers that add up to the target",
      "sampleInput": "nums = [2,7,11,15], target = 9",
      "sampleOutput": "[0,1]",
      "difficulty": "EASY"
    }
  ]
}
```

---

### 6.3 Run Code (Test Execution)

**Endpoint:** `POST /student/code/run`

```json
{
  "codingTestId": 1,
  "language": "JAVA",
  "code": "public class Solution { public int[] twoSum(int[] nums, int target) { for(int i=0; i<nums.length; i++) { for(int j=i+1; j<nums.length; j++) { if(nums[i] + nums[j] == target) { return new int[]{i, j}; } } } return null; } }"
}
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Code executed successfully",
  "data": {
    "output": "[0, 1]",
    "status": "PASSED",
    "executionTime": "25ms"
  }
}
```

---

### 6.4 Submit Code

**Endpoint:** `POST /student/code/submit`

```json
{
  "codingTestId": 1,
  "language": "JAVA",
  "code": "public class Solution { public int[] twoSum(int[] nums, int target) { for(int i=0; i<nums.length; i++) { for(int j=i+1; j<nums.length; j++) { if(nums[i] + nums[j] == target) { return new int[]{i, j}; } } } return null; } }"
}
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Code submitted successfully",
  "data": {
    "status": "ACCEPTED",
    "message": "All test cases passed!"
  }
}
```

---

## 📈 Phase 7: Admin - Monitor & Analytics

**Admin ID:** 3

### 7.1 Get All Quizzes (Including Inactive)

**Endpoint:** `GET /admin/quizzes`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "All quizzes fetched successfully",
  "data": [
    {
      "id": 1,
      "title": "Java Fundamentals",
      "description": "Learn the basics of Java programming",
      "durationMinutes": 30,
      "totalMarks": 100,
      "active": true,
      "questionCount": 5,
      "createdAt": "2026-05-13T10:00:00"
    },
    {
      "id": 2,
      "title": "Python Basics",
      "description": "Introduction to Python programming",
      "durationMinutes": 45,
      "totalMarks": 100,
      "active": true,
      "questionCount": 2,
      "createdAt": "2026-05-13T10:05:00"
    }
  ]
}
```

---

### 7.2 Get Questions for Admin (With Correct Answers)

**Endpoint:** `GET /admin/questions/quiz/1`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Questions fetched for admin",
  "data": [
    {
      "id": 1,
      "questionText": "What is JVM?",
      "optionA": "Java Virtual Machine",
      "optionB": "Java Variable Module",
      "optionC": "Java Version Manager",
      "optionD": "Just Virtual Machine",
      "marks": 10,
      "quizId": 1,
      "correctAnswer": "A"
    },
    {
      "id": 2,
      "questionText": "What does OOP stand for?",
      "optionA": "Object Oriented Programming",
      "optionB": "Object Oriented Procedure",
      "optionC": "Operational Object Programming",
      "optionD": "Object Operation Program",
      "marks": 10,
      "quizId": 1,
      "correctAnswer": "A"
    }
  ]
}
```

**Note:** `correctAnswer` IS visible for admin!

---

### 7.3 Update a Quiz

**Endpoint:** `PUT /admin/quizzes/1`

```json
{
  "title": "Java Fundamentals - Updated",
  "active": false
}
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Quiz updated successfully",
  "data": {
    "id": 1,
    "title": "Java Fundamentals - Updated",
    "description": "Learn the basics of Java programming",
    "durationMinutes": 30,
    "totalMarks": 100,
    "active": false
  }
}
```

---

### 7.4 Update a Question

**Endpoint:** `PUT /admin/questions/1`

```json
{
  "questionText": "What is the JVM? Updated",
  "correctAnswer": "A"
}
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Question updated successfully",
  "data": {
    "id": 1,
    "questionText": "What is the JVM? Updated",
    "optionA": "Java Virtual Machine",
    "optionB": "Java Variable Module",
    "optionC": "Java Version Manager",
    "optionD": "Just Virtual Machine",
    "correctAnswer": "A",
    "marks": 10,
    "quizId": 1
  }
}
```

---

### 7.5 Get Admin Stats

**Endpoint:** `GET /admin/stats`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Stats fetched successfully",
  "data": {
    "totalQuizzes": 2,
    "totalQuestions": 7,
    "totalStudents": 2,
    "totalAttempts": 1
  }
}
```

---

### 7.6 Get All Students

**Endpoint:** `GET /admin/students`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Students fetched successfully",
  "data": [
    {
      "id": 1,
      "name": "Alice Johnson",
      "email": "alice@example.com",
      "role": "STUDENT"
    },
    {
      "id": 2,
      "name": "Bob Smith",
      "email": "bob@example.com",
      "role": "STUDENT"
    }
  ]
}
```

---

### 7.7 Get All Results

**Endpoint:** `GET /admin/results`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Results fetched successfully",
  "data": [
    {
      "attemptId": 1,
      "quizId": 1,
      "quizTitle": "Java Fundamentals",
      "studentId": 1,
      "studentName": "Alice Johnson",
      "totalQuestions": 5,
      "correctAnswers": 5,
      "score": 50,
      "totalMarks": 100,
      "percentage": 50.0,
      "passed": true,
      "submittedAt": "2026-05-13T10:35:00"
    }
  ]
}
```

---

### 7.8 Delete a Question

**Endpoint:** `DELETE /admin/questions/5`

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Question deleted successfully"
}
```

---

### 7.9 Delete a Quiz

**Endpoint:** `DELETE /admin/quizzes/2`

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Quiz deleted successfully"
}
```

---

## 🎬 Quick Test Sequence

Follow this order for complete end-to-end testing:

```
1. Register Alice (Student) → Save ID: 1
2. Register Bob (Student) → Save ID: 2
3. Register Admin → Save ID: 3
4. Login Alice → Verify token
5. Create Quiz 1 → Save ID: 1
6. Create 5 Questions for Quiz 1 → Save IDs: 1-5
7. Create Quiz 2 → Save ID: 2
8. Create 2 Questions for Quiz 2 → Save IDs: 6-7
9. Get All Quizzes (Student view) → Should see 2 active quizzes
10. Get Quiz Details (Student view)
11. Start Quiz → Save Attempt ID: 1
12. Submit 5 Answers
13. Submit Quiz
14. Get Results
15. Get Admin Stats
16. Get All Students
17. Get All Results
18. Update Quiz
19. Create Coding Test
20. Get Coding Tests
21. Run Code
22. Submit Code
23. Delete Question
24. Delete Quiz
```

---

## 📝 Testing Tips

1. **Use Postman or Insomnia** for API testing with request history
2. **Always save IDs** returned from POST operations for use in subsequent requests
3. **Student view** hides correct answers; **Admin view** shows them
4. **Only active quizzes** appear in student quiz list
5. **Quiz must be started** before submitting answers
6. **All answers must be submitted** before submitting the quiz
7. **Results are calculated** after quiz submission

---

## ⚠️ Common Errors

| Error | Cause | Solution |
|-------|-------|----------|
| 400 Bad Request | Missing/invalid fields | Check JSON format and required fields |
| 404 Not Found | Invalid ID | Verify ID exists in system |
| 409 Conflict | Duplicate email | Use different email for registration |
| 500 Server Error | Database issue | Check application logs |

---

## 🔗 Summary of All Endpoints

### Authentication
- `POST /register` - Register new user
- `POST /login` - Login user

### Admin Quiz Management
- `POST /admin/quizzes` - Create quiz
- `GET /admin/quizzes` - Get all quizzes (including inactive)
- `PUT /admin/quizzes/{id}` - Update quiz
- `DELETE /admin/quizzes/{id}` - Delete quiz

### Admin Question Management
- `POST /admin/questions` - Add question
- `GET /admin/questions/quiz/{quizId}` - Get questions with answers
- `PUT /admin/questions/{id}` - Update question
- `DELETE /admin/questions/{id}` - Delete question

### Student Quiz Operations
- `GET /student/quizzes` - Get all active quizzes
- `GET /student/quizzes/{id}` - Get quiz details
- `POST /student/quizzes/start/{quizId}?studentId={id}` - Start quiz
- `POST /student/quizzes/submit-answer` - Submit answer
- `POST /student/quizzes/submit/{attemptId}` - Submit quiz
- `GET /questions/quiz/{quizId}` - Get questions (no answers shown)

### Results
- `GET /student/results/{attemptId}` - Get quiz result

### Coding Tests
- `GET /student/coding-tests` - List all coding problems
- `POST /student/code/run` - Run code
- `POST /student/code/submit` - Submit code
- `POST /admin/coding-tests` - Create coding problem
- `PUT /admin/coding-tests/{id}` - Update coding problem
- `DELETE /admin/coding-tests/{id}` - Delete coding problem

### Admin Analytics
- `GET /admin/stats` - Get system statistics
- `GET /admin/students` - Get all students
- `GET /admin/results` - Get all results

---

**Total Endpoints:** 28 | **Total Phases:** 7 | **Last Updated:** May 13, 2026


