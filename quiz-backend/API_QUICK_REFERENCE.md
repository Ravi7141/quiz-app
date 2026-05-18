# 📌 Quiz API - Quick Reference Cheat Sheet

## 🚀 Getting Started

1. **Start Spring Boot Application:** `mvn spring-boot:run`
2. **Base URL:** `http://localhost:8080`
3. **Import Postman Collection:** Use `Quiz_API_Postman_Collection.json`

---

## 👤 User IDs (Example)
| User | ID | Email | Password | Role |
|------|----|----|---------|------|
| Alice | 1 | alice@example.com | password123 | STUDENT |
| Bob | 2 | bob@example.com | password123 | STUDENT |
| Admin | 3 | admin@example.com | adminpass123 | ADMIN |

---

## 📚 Quiz & Question IDs (Example)
| Resource | ID | Description |
|----------|----|----|
| Quiz 1 | 1 | Java Fundamentals (30 min, 100 marks) |
| Quiz 2 | 2 | Python Basics (45 min, 100 marks) |
| Q1-Q5 | 1-5 | Java quiz questions |
| Q6-Q7 | 6-7 | Python quiz questions |
| Attempt 1 | 1 | Alice's Quiz 1 attempt |
| Coding Test 1 | 1 | Two Sum Problem |

---

## 🔑 Authentication Endpoints

### Register
```
POST /register
{
  "name": "Name",
  "email": "email@example.com",
  "password": "password123",
  "role": "STUDENT" or "ADMIN"
}
```

### Login
```
POST /login
{
  "email": "email@example.com",
  "password": "password123"
}
```

---

## 📝 Quiz Management (Admin)

### Create Quiz
```
POST /admin/quizzes
{
  "title": "Quiz Title",
  "description": "Description",
  "durationMinutes": 30,
  "totalMarks": 100,
  "active": true
}
```

### Get All Quizzes
```
GET /admin/quizzes
```

### Update Quiz
```
PUT /admin/quizzes/{id}
{
  "title": "New Title",
  "active": false
}
```

### Delete Quiz
```
DELETE /admin/quizzes/{id}
```

---

## ❓ Question Management (Admin)

### Add Question
```
POST /admin/questions
{
  "questionText": "Question?",
  "optionA": "Option A",
  "optionB": "Option B",
  "optionC": "Option C",
  "optionD": "Option D",
  "correctAnswer": "A",
  "marks": 10,
  "quizId": 1
}
```

### Get Questions (Admin - With Answers)
```
GET /admin/questions/quiz/{quizId}
```

### Update Question
```
PUT /admin/questions/{id}
{
  "questionText": "Updated?",
  "correctAnswer": "A"
}
```

### Delete Question
```
DELETE /admin/questions/{id}
```

---

## 🎯 Student Quiz Flow

### View All Quizzes
```
GET /student/quizzes
```

### View Quiz Details
```
GET /student/quizzes/{id}
```

### Get Questions (Student - No Answers)
```
GET /questions/quiz/{quizId}
```

### Start Quiz
```
POST /student/quizzes/start/{quizId}?studentId={studentId}
→ Save attemptId from response
```

### Submit an Answer
```
POST /student/quizzes/submit-answer
{
  "attemptId": 1,
  "questionId": 1,
  "selectedOption": "A"
}
```

### Submit Quiz
```
POST /student/quizzes/submit/{attemptId}
```

### View Results
```
GET /student/results/{attemptId}
```

---

## 💻 Coding Tests

### Get All Coding Tests (Student)
```
GET /student/coding-tests
```

### Create Coding Test (Admin)
```
POST /admin/coding-tests
{
  "title": "Problem Title",
  "description": "Description",
  "sampleInput": "Input",
  "sampleOutput": "Output",
  "difficulty": "EASY|MEDIUM|HARD"
}
```

### Run Code
```
POST /student/code/run
{
  "codingTestId": 1,
  "language": "JAVA|PYTHON|CPP",
  "code": "code here"
}
```

### Submit Code
```
POST /student/code/submit
{
  "codingTestId": 1,
  "language": "JAVA",
  "code": "code here"
}
```

### Update Coding Test (Admin)
```
PUT /admin/coding-tests/{id}
{
  "title": "New Title",
  "difficulty": "MEDIUM"
}
```

### Delete Coding Test (Admin)
```
DELETE /admin/coding-tests/{id}
```

---

## 📊 Admin Analytics

### Get Statistics
```
GET /admin/stats
```
Returns: totalQuizzes, totalQuestions, totalStudents, totalAttempts

### Get All Students
```
GET /admin/students
```

### Get All Results
```
GET /admin/results
```

---

## ✅ Step-by-Step Test Flow

### Step 1: Authentication (5 min)
```
1. POST /register          → Alice, Student, ID: 1
2. POST /register          → Bob, Student, ID: 2
3. POST /register          → Admin User, ID: 3
4. POST /login             → Login Alice
5. POST /login             → Login Admin (Admin)
```

### Step 2: Quiz Creation (5 min)
```
6. POST /admin/quizzes          → Java Quiz, ID: 1
7. POST /admin/quizzes          → Python Quiz, ID: 2
8-14. POST /admin/questions     → Add 7 questions (IDs: 1-7)
```

### Step 3: Student Quiz (10 min)
```
15. GET /student/quizzes              → View all quizzes
16. GET /student/quizzes/1            → View Quiz 1 details
17. POST /student/quizzes/start/1?studentId=1     → Start quiz, Attempt ID: 1
18. GET /questions/quiz/1             → Get questions (no answers shown)
19-23. POST /student/quizzes/submit-answer  → Answer Q1-Q5
24. POST /student/quizzes/submit/1          → Submit quiz
```

### Step 4: Results (2 min)
```
25. GET /student/results/1      → View results
```

### Step 5: Admin Monitoring (5 min)
```
26. GET /admin/quizzes                 → View all quizzes
27. GET /admin/questions/quiz/1        → View Q with answers
28. PUT /admin/quizzes/1               → Update quiz
29. GET /admin/stats                   → View statistics
30. GET /admin/students                → View all students
31. GET /admin/results                 → View all results
```

### Step 6: Coding Tests (5 min)
```
32. POST /admin/coding-tests           → Create coding test
33. GET /student/coding-tests          → List tests
34. POST /student/code/run             → Run code
35. POST /student/code/submit          → Submit code
```

**Total Time:** ~40 minutes for full testing

---

## 🎨 Response Format (All Endpoints)

### Success (200, 201)
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { /* actual data */ }
}
```

### Error (400, 404, 500)
```json
{
  "success": false,
  "message": "Error description"
}
```

---

## ⚠️ Important Notes

1. **Student ID Required:** Always pass `studentId` as query param in start quiz endpoint
2. **Answer Options:** Only A, B, C, D are valid
3. **Difficulty Levels:** EASY, MEDIUM, HARD
4. **Languages:** JAVA, PYTHON, CPP
5. **Roles:** STUDENT (default), ADMIN
6. **Active Quiz Only:** Students only see active quizzes
7. **Correct Answers Hidden:** Students never see correct answers
8. **Admin Visibility:** Admin sees all quizzes (active + inactive)

---

## 🔍 Status Codes

| Code | Meaning |
|------|---------|
| 200 | OK - Request successful |
| 201 | Created - New resource created |
| 400 | Bad Request - Invalid data |
| 404 | Not Found - Resource doesn't exist |
| 409 | Conflict - Duplicate (e.g., email) |
| 500 | Server Error - Internal issue |

---

## 📋 Common Test Scenarios

### Scenario 1: New Student Takes Quiz
1. Register → Get ID
2. View quizzes
3. Start quiz
4. Answer all questions
5. Submit quiz
6. Check results

### Scenario 2: Admin Creates Quiz
1. Create quiz
2. Add questions
3. Verify questions
4. Deactivate quiz
5. Delete quiz

### Scenario 3: Multiple Students
1. Register 3 students
2. All take same quiz
3. Admin views all results
4. Check statistics

---

## 💡 Pro Tips

1. **Use Postman Collections:** Import JSON collection for all pre-built requests
2. **Set Variables:** Use `{{base_url}}` variable for easy environment switching
3. **Save IDs:** Copy IDs from responses to use in following requests
4. **Test in Order:** Follow the step-by-step flow for best results
5. **Check Logs:** Read error messages carefully for validation errors
6. **Database Reset:** Delete quiz attempts if testing multiple times

---

**Version:** 1.0 | **Last Updated:** May 13, 2026 | **API Count:** 28 Endpoints


