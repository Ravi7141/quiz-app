import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

// Request interceptor — attach logged-in user email as token
api.interceptors.request.use((config) => {
  const user = JSON.parse(localStorage.getItem('quizvault_user') || 'null')
  if (user?.email) {
    config.headers.Authorization = `Bearer ${user.email}`
  }
  return config
})

// Response interceptor — unwrap data
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('quizvault_user')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

// ─── Auth ──────────────────────────────────────────────────────
export const authApi = {
  register: (data) => api.post('/register', data),
  login: (data) => api.post('/login', data),
  forgotPasswordRequest: (data) => api.post('/forgot-password/request', data),
  forgotPasswordReset: (data) => api.post('/forgot-password/reset', data),
}

// ─── Student Quiz ──────────────────────────────────────────────
export const studentQuizApi = {
  getAll: () => api.get('/student/quizzes'),
  getById: (id) => api.get(`/student/quizzes/${id}`),
  getAssessment: (id) => api.get(`/student/quizzes/${id}/assessment`),
}

// ─── Quiz Attempt ──────────────────────────────────────────────
export const attemptApi = {
  start: (quizId, studentId) =>
    api.post(`/student/quizzes/start/${quizId}?studentId=${studentId}`),
  submitAnswer: (data) => api.post('/student/quizzes/submit-answer', data),
  submitCoding: (data) => api.post('/student/quizzes/submit-coding', data),
  submit: (attemptId) => api.post(`/student/quizzes/submit/${attemptId}`),
}

// ─── Results ───────────────────────────────────────────────────
export const resultApi = {
  get: (attemptId) => api.get(`/student/results/${attemptId}`),
}

// ─── Questions ─────────────────────────────────────────────────
export const questionApi = {
  getForStudent: (quizId) => api.get(`/questions/quiz/${quizId}`),
  getForAdmin: (quizId) => api.get(`/admin/questions/quiz/${quizId}`),
  add: (data) => api.post('/admin/questions', data),
  bulkAdd: (quizId, data) => api.post('/admin/questions/bulk', data),
  update: (id, data) => api.put(`/admin/questions/${id}`, data),
  delete: (id) => api.delete(`/admin/questions/${id}`),
}

// ─── Admin Quiz ────────────────────────────────────────────────
export const adminQuizApi = {
  getAll: () => api.get('/admin/quizzes'),
  getById: (id) => api.get(`/admin/quizzes/${id}`),
  create: (data) => api.post('/admin/quizzes', data),
  update: (id, data) => api.put(`/admin/quizzes/${id}`, data),
  delete: (id) => api.delete(`/admin/quizzes/${id}`),
}

// ─── Admin General ─────────────────────────────────────────────
export const adminApi = {
  getStats: () => api.get('/admin/stats'),
  getStudents: () => api.get('/admin/students'),
  getStudentResults: (id) => api.get(`/admin/students/${id}/results`),
  getResults: () => api.get('/admin/results'),
  getQuizResults: (quizId) => api.get(`/admin/quizzes/${quizId}/results`),
}

// ─── Coding Tests ──────────────────────────────────────────────
export const codingApi = {
  getAll: () => api.get('/student/coding-tests'),
  getById: (id) => api.get(`/student/coding-tests/${id}`),
  run: (data) => api.post('/student/code/run', data),
  submit: (data) => api.post('/student/code/submit', data),
  create: (data) => api.post('/admin/coding-tests', data),
  update: (id, data) => api.put(`/admin/coding-tests/${id}`, data),
  delete: (id) => api.delete(`/admin/coding-tests/${id}`),
  importLeetCode: (query) => api.get(`/admin/coding-tests/import-leetcode?query=${encodeURIComponent(query)}`),
}

// ─── Exam Tokens ───────────────────────────────────────────────
export const examTokenApi = {
  generate: (data) => api.post('/admin/tokens/generate', data),
  getForExam: (type, id) => api.get(`/admin/tokens/exam/${type}/${id}`),
  verify: (token) => api.get(`/api/tokens/verify?token=${token}`),
  consume: (token) => api.post(`/api/tokens/consume?token=${token}`),
  emailAll: (type, id, baseUrl) =>
    api.post(`/admin/tokens/exam/${type}/${id}/email-all?baseUrl=${encodeURIComponent(baseUrl)}`),
}

// ─── Unified Assessments ───────────────────────────────────────
export const assessmentApi = {
  create: (data) => api.post('/admin/assessments', data),
  getAll: () => api.get('/admin/assessments'),
  regenerateShareToken: (id) => api.post(`/admin/assessments/${id}/share`),
  getByToken: (token) => api.get(`/assessment/${token}`),
  startAttempt: (assessmentId, studentId) =>
    api.post(`/assessment/start?assessmentId=${assessmentId}&studentId=${studentId}`),
  submitAttempt: (attemptId) =>
    api.post(`/assessment/submit?attemptId=${attemptId}`),
  submitCoding: (assessmentAttemptId, codingTestId, code, language, passed) =>
    api.post(`/assessment/submit-coding?assessmentAttemptId=${assessmentAttemptId}&codingTestId=${codingTestId}&code=${encodeURIComponent(code)}&language=${language}&passed=${passed}`),
}

export default api
