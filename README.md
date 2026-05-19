# 🎓 QuizVault — Full-Stack Java & React Assessment Portal

Welcome to **QuizVault**, a premium, state-of-the-art full-stack online examination and coding assessment portal. This is a complete Java (Spring Boot) and React application engineered with a sleek modern design system, robust anti-cheat controls, and secure data isolation.

---

## 📂 Project Architecture & Directory Structure

```
quiz-app/
├── quiz-backend/      # Spring Boot + Spring Data JPA + PostgreSQL Database REST API
├── quiz-frontend/     # React + Vite + Vanilla CSS Premium Dark UI Dashboard
└── README.md          # Project documentation and start-up guide
```

---

## ⚡ Features Overview

### 🔒 Security & Cheat Prevention
* **Full-Screen Enforcer**: Automatically enters full-screen testing mode. Any escape attempt exits the test immediately.
* **Anti-Cheat Triggers**: Logs tab switches, window blur events, and browser state changes to detect malicious behavior in real-time.
* **Security Interceptors**: Attach logged-in identity properties to request interceptors securely.

### 👥 Student & Test Lifecycle
* **Instant Sharing Tokens**: Bulk import student emails and generate unique links.
* **Auto-Registration**: Bulk imported emails instantly register student profiles automatically under the creating coordinator.
* **Timer Countdown**: Smooth countdown timers that trigger auto-submission on expiry.

### ⚙️ Core Technical Architecture
* **Spring Boot Backend**: Secure REST API endpoints, automated database schema configuration, and Spring Data JPA repositories.
* **React Frontend**: Optimized Vite client with high-performance routing, dynamic layouts, and polished micro-animations.

---

## 🚀 How to Run the App Locally

### 1. Database Setup
Ensure PostgreSQL is running on your local machine with:
* **Host**: `localhost:5432`
* **Database Name**: `exam_portal`
* **Username**: `postgres`
* **Password**: `00000000` (Configure inside `quiz-backend/src/main/resources/application.properties` if credentials differ).

---

### 2. Run the Backend (`quiz-backend`)
Open a new terminal window:
```powershell
cd quiz-backend
.\mvnw spring-boot:run
```
* **REST API Endpoint**: `http://localhost:8080`

---

### 3. Run the Frontend (`quiz-frontend`)
Open a second terminal window:
```powershell
cd quiz-frontend
npm install
npm run dev
```
* **Frontend Dev Server**: `http://localhost:5173`

---

Enjoy using **QuizVault**! 🚀
