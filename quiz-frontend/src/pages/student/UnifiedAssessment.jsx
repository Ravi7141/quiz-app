import { useEffect, useState, useCallback, useRef } from 'react'
import { useParams, useNavigate, useSearchParams, useLocation } from 'react-router-dom'
import { motion, AnimatePresence } from 'framer-motion'
import Editor from '@monaco-editor/react'
import { questionApi, attemptApi, studentQuizApi, examTokenApi, codingApi, assessmentApi } from '../../api/axios'
import { useAuth } from '../../context/AuthContext'
import toast from 'react-hot-toast'
import {
  ChevronLeft, ChevronRight, CheckCircle2, Clock, Send,
  AlertTriangle, Shield, Flag, Play, Terminal, XCircle, Loader2, ArrowLeft, ShieldAlert
} from 'lucide-react'
import FaceDetectionGuard from '../../components/FaceDetectionGuard'
import CameraSetupGate from '../../components/CameraSetupGate'

const LANGUAGES = [
  { val: 'javascript', label: 'JavaScript' },
  { val: 'python',     label: 'Python 3'   },
  { val: 'java',       label: 'Java'        },
  { val: 'cpp',        label: 'C++'         },
]

const STARTERS = {
  javascript: '// Write your solution here\nfunction solution(input) {\n  // your code\n}\n',
  python:     '# Write your solution here\ndef solution(input):\n    pass\n',
  java:       'public class Solution {\n    public static void main(String[] args) {\n        // your code\n    }\n}\n',
  cpp:        '#include <iostream>\nusing namespace std;\n\nint main() {\n    // your code\n    return 0;\n}\n',
}

const diffClass = { EASY: 'badge-easy', MEDIUM: 'badge-medium', HARD: 'badge-hard' }
const MAX_VIOLATIONS = 3

export default function UnifiedAssessment() {
  const { id } = useParams()
  const { user, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')
  const tokenStudentId = searchParams.get('studentId')
  const activeStudentId = tokenStudentId || user?.id

  const attemptIdRef = useRef(null)
  const autoSubmittedRef = useRef(false)
  const antiCheatActiveRef = useRef(false)
  const violRef = useRef(0)
  const cameraStreamRef = useRef(null)

  // Candidate enrollment details (if accessing via shared link without active session)
  const [candidateForm, setCandidateForm] = useState({ name: '', email: '', phone: '' })
  const [candidateSubmitting, setCandidateSubmitting] = useState(false)

  const handleCandidateSubmit = async (e) => {
    e.preventDefault()
    setCandidateSubmitting(true)
    try {
      const res = await assessmentApi.enroll(candidateForm.name, candidateForm.email, candidateForm.phone)
      const userData = res.data.data
      login(userData)
      toast.success(`Welcome, ${userData.name}! Please proceed to the system check.`)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to register. Please try again.')
    } finally {
      setCandidateSubmitting(false)
    }
  }

  // System Setup
  const [cameraVerified, setCameraVerified] = useState(false)
  const [loading, setLoading] = useState(true)

  // Assessment Data
  const [exam, setExam] = useState(null)
  const [questions, setQuestions] = useState([])
  const [codingTests, setCodingTests] = useState([])
  const [attemptId, setAttemptId] = useState(null)

  // Assessment Progress
  const [currentSection, setCurrentSection] = useState('quiz') // quiz, coding
  const [currentQuestion, setCurrentQuestion] = useState(0)
  const [currentCoding, setCurrentCoding] = useState(0)

  // MCQ state
  const [answers, setAnswers] = useState({})
  const [markedForReview, setMarkedForReview] = useState(new Set())
  const [quizAttemptMap, setQuizAttemptMap] = useState({})

  // Coding state (stored per coding test ID)
  const [codingStates, setCodingStates] = useState({})

  // Proctoring/Anti-cheat
  const [violations, setViolations] = useState(0)
  const [violationPopup, setViolationPopup] = useState(null)
  const [showConfirm, setShowConfirm] = useState(false)
  const [timeLeft, setTimeLeft] = useState(null)

  // Compilation/Submission UI state (for the active coding challenge)
  const [consoleOutput, setConsoleOutput] = useState(null)

  useEffect(() => {
    if (cameraVerified) {
      const timer = setTimeout(() => { antiCheatActiveRef.current = true }, 2000)
      return () => clearTimeout(timer)
    }
  }, [cameraVerified])

  // ── Init ─────────────────────────────────────────────────────
  useEffect(() => {
    if (!cameraVerified) return
    let cancelled = false

    const init = async () => {
      try {
        // 1. Fetch details of the unified assessment using the share token
        const detailsRes = await assessmentApi.getByToken(id)
        if (cancelled) return
        const assessment = detailsRes.data.data

        // 2. Start the master assessment attempt session
        const startRes = await assessmentApi.startAttempt(assessment.id, activeStudentId)
        if (cancelled) return

        const aId = startRes.data.data.attemptId
        setAttemptId(aId)
        attemptIdRef.current = aId

        // Store the Quiz ID -> QuizAttempt ID map returned by the backend
        const qAttemptMap = startRes.data.data.quizAttemptMap || {}
        setQuizAttemptMap(qAttemptMap)

        setExam(assessment) // Unified assessment metadata

        // Consolidate questions and coding tests from the sections
        const allQuestions = []
        const allCodingTests = []
        const sortedSections = assessment.sections || []

        sortedSections.forEach(section => {
          if (section.type === 'QUIZ' && section.questions) {
            // Tag each question with its quizId/referenceId to look up the correct QuizAttempt ID
            const tagged = section.questions.map(q => ({
              ...q,
              quizAttemptId: qAttemptMap[section.referenceId]
            }))
            allQuestions.push(...tagged)
          } else if (section.type === 'CODING' && section.codingTest) {
            allCodingTests.push(section.codingTest)
          }
        })

        setQuestions(allQuestions)
        setCodingTests(allCodingTests)

        // Initialize coding states
        const initialCodingStates = {}
        allCodingTests.forEach(test => {
          initialCodingStates[test.id] = {
            code: STARTERS.javascript,
            language: 'javascript',
            output: null,
            running: false,
            submitting: false,
            passed: false
          }
        })
        setCodingStates(initialCodingStates)

        // Set countdown timer if exam has a duration
        if (assessment.durationMinutes && assessment.durationMinutes > 0) {
          setTimeLeft(assessment.durationMinutes * 60)
        }

        // Set initial section: if no MCQ questions, start with coding
        if (allQuestions.length === 0 && allCodingTests.length > 0) {
          setCurrentSection('coding')
        }

        if (location.state?.requestFullscreen) {
          try { await document.documentElement.requestFullscreen() } catch {}
        }
      } catch (err) {
        if (cancelled) return
        const msg = err.response?.data?.message || ''
        if (document.fullscreenElement) {
          try { await document.exitFullscreen() } catch {}
        }
        toast.error(msg || 'Could not start assessment. Please try again.')
        if (window.history.length > 1) {
          navigate(-1)
        } else {
          navigate('/', { replace: true })
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    init()
    return () => { cancelled = true }
  }, [cameraVerified, id, activeStudentId, navigate, location.state])

  // ── Timer countdown ────────────────────────────────────────────
  useEffect(() => {
    if (timeLeft === null || !cameraVerified || loading) return
    if (timeLeft <= 0) {
      if (!autoSubmittedRef.current) {
        autoSubmittedRef.current = true
        toast('⏱️ Time is up! Submitting exam…', { duration: 3000, icon: '⏱️' })
        setTimeout(() => handleFinish(true), 1500)
      }
      return
    }
    const tick = setInterval(() => setTimeLeft(t => t - 1), 1000)
    return () => clearInterval(tick)
  }, [timeLeft, cameraVerified, loading])

  // ── Anti-cheat violation recorder ────────────────────────────────
  const handleViolation = useCallback((reason) => {
    if (!antiCheatActiveRef.current || autoSubmittedRef.current) return
    violRef.current += 1
    const count = violRef.current
    setViolations(count)

    const isAutoSubmit = count >= MAX_VIOLATIONS
    setViolationPopup({ count, reason, autoSubmit: isAutoSubmit })

    if (isAutoSubmit) {
      autoSubmittedRef.current = true
      setTimeout(() => handleFinish(true), 2500)
    }
  }, [])

  // ── Anti-cheat event listeners ─────────────────────────────────
  useEffect(() => {
    if (!cameraVerified || loading) return

    const onBlur = () => handleViolation('Window focus lost — switching windows is not allowed.')
    const onVisibility = () => { if (document.hidden) handleViolation('Tab switching detected.') }
    const onKey = (e) => {
      if ((e.ctrlKey || e.metaKey) && e.key === 't') {
        e.preventDefault()
        handleViolation('Opening new tab detected.')
      }
    }
    const onFullscreen = () => {
      if (!document.fullscreenElement) handleViolation('Fullscreen was exited.')
    }

    window.addEventListener('blur', onBlur)
    document.addEventListener('visibilitychange', onVisibility)
    document.addEventListener('keydown', onKey)
    document.addEventListener('fullscreenchange', onFullscreen)
    return () => {
      window.removeEventListener('blur', onBlur)
      document.removeEventListener('visibilitychange', onVisibility)
      document.removeEventListener('keydown', onKey)
      document.removeEventListener('fullscreenchange', onFullscreen)
    }
  }, [cameraVerified, loading, handleViolation])

  // ── MCQ option selection ─────────────────────────────────────────
  const selectAnswer = useCallback(async (questionId, option, quizAttemptId) => {
    setAnswers(prev => ({ ...prev, [questionId]: option }))
    if (!quizAttemptId) return
    try {
      await attemptApi.submitAnswer({ attemptId: quizAttemptId, questionId, selectedOption: option })
    } catch {
      // Silent fail - stored locally
    }
  }, [])

  const toggleReview = (questionId) => {
    setMarkedForReview(prev => {
      const next = new Set(prev)
      if (next.has(questionId)) next.delete(questionId)
      else next.add(questionId)
      return next
    })
  }

  // ── Coding editor action handlers ───────────────────────────────
  const handleCodeChange = (val) => {
    const activeTest = codingTests[currentCoding]
    if (!activeTest) return
    setCodingStates(prev => ({
      ...prev,
      [activeTest.id]: {
        ...prev[activeTest.id],
        code: val
      }
    }))
  }

  const handleLangChange = (newLang) => {
    const activeTest = codingTests[currentCoding]
    if (!activeTest) return
    setCodingStates(prev => ({
      ...prev,
      [activeTest.id]: {
        ...prev[activeTest.id],
        language: newLang,
        code: STARTERS[newLang]
      }
    }))
  }

  const handleRunCode = async () => {
    const activeTest = codingTests[currentCoding]
    if (!activeTest) return
    const currentState = codingStates[activeTest.id]

    setCodingStates(prev => ({
      ...prev,
      [activeTest.id]: { ...prev[activeTest.id], running: true }
    }))
    setConsoleOutput(null)

    try {
      const res = await codingApi.run({
        codingTestId: activeTest.id,
        code: currentState.code,
        language: currentState.language.toUpperCase()
      })
      const data = res.data.data
      setConsoleOutput({ type: 'run', data })
      
      setCodingStates(prev => ({
        ...prev,
        [activeTest.id]: {
          ...prev[activeTest.id],
          running: false,
          output: { type: 'run', data }
        }
      }))
    } catch (err) {
      const errMsg = err.response?.data?.message || 'Execution error'
      setConsoleOutput({ type: 'error', data: errMsg })
      setCodingStates(prev => ({
        ...prev,
        [activeTest.id]: {
          ...prev[activeTest.id],
          running: false,
          output: { type: 'error', data: errMsg }
        }
      }))
    }
  }

  const handleSubmitCode = async () => {
    const activeTest = codingTests[currentCoding]
    if (!activeTest) return
    const currentState = codingStates[activeTest.id]

    setCodingStates(prev => ({
      ...prev,
      [activeTest.id]: { ...prev[activeTest.id], submitting: true }
    }))
    setConsoleOutput(null)

    try {
      const res = await codingApi.submit({
        codingTestId: activeTest.id,
        code: currentState.code,
        language: currentState.language.toUpperCase()
      })
      const data = res.data.data
      setConsoleOutput({ type: 'submit', data })

      const passed = data?.status === 'ACCEPTED' || data?.testCasesPassed === data?.totalTestCases
      toast[passed ? 'success' : 'error'](
        passed ? `✅ All ${data.testCasesPassed || ''} tests passed!` : `❌ ${data.testCasesPassed || 0}/${data.totalTestCases || '?'} tests passed`
      )

      // Save record in database via studentCodingSubmission
      try {
        await assessmentApi.submitCoding(
          attemptIdRef.current,
          activeTest.id,
          currentState.code,
          currentState.language.toUpperCase(),
          passed
        )
      } catch (err) {
        console.error("Failed to record code submission:", err)
      }

      setCodingStates(prev => ({
        ...prev,
        [activeTest.id]: {
          ...prev[activeTest.id],
          submitting: false,
          passed: passed,
          output: { type: 'submit', data }
        }
      }))
    } catch (err) {
      const errMsg = err.response?.data?.message || 'Submission error'
      setConsoleOutput({ type: 'error', data: errMsg })
      setCodingStates(prev => ({
        ...prev,
        [activeTest.id]: {
          ...prev[activeTest.id],
          submitting: false,
          output: { type: 'error', data: errMsg }
        }
      }))
    }
  }

  // ── Final Submit ───────────────────────────────────────────────
  const handleFinish = async (force = false) => {
    if (!force && !showConfirm) {
      setShowConfirm(true)
      return
    }
    setShowConfirm(false)
    setLoading(true)

    try {
      await assessmentApi.submitAttempt(attemptIdRef.current)
      if (token) {
        try { await examTokenApi.consume(token) } catch {}
      }
      toast.success('Assessment submitted successfully!')
      if (document.fullscreenElement) {
        try { await document.exitFullscreen() } catch {}
      }
      navigate('/student/success', { replace: true })
    } catch (err) {
      toast.error('Submission failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const formatTime = (s) => {
    if (s === null) return '--:--'
    const m = Math.floor(s / 60).toString().padStart(2, '0')
    const sec = (s % 60).toString().padStart(2, '0')
    return `${m}:${sec}`
  }

  const isUrgent = timeLeft !== null && timeLeft <= 60
  const timerClass = timeLeft > 0
    ? timeLeft <= 60 ? 'danger' : timeLeft <= 180 ? 'warning' : ''
    : ''

  if (!activeStudentId) {
    return (
      <div style={{ minHeight: '100vh', background: '#0a0d14', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 24, color: '#e2e8f0' }}>
        <div style={{ textAlign: 'center' }}>
          <Loader2 size={48} style={{ animation: 'spin 1s linear infinite', color: '#a78bfa', margin: '0 auto 16px' }} />
          <h3 style={{ fontSize: 18, fontWeight: 700, color: '#fff', marginBottom: 6 }}>Preparing Exam Session</h3>
          <p style={{ fontSize: 13, color: '#94a3b8' }}>Please wait while we initialize your secure proctoring environment...</p>
        </div>
      </div>
    )
  }

  if (!cameraVerified) {
    return (
      <CameraSetupGate
        onReady={(stream) => {
          cameraStreamRef.current = stream
          setCameraVerified(true)
        }}
        onCancel={() => navigate(-1)}
        title="Assessment System Check"
      />
    )
  }

  if (loading) {
    return (
      <div style={{ minHeight: '100vh', background: 'var(--bg-main)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <div className="spinner" />
      </div>
    )
  }

  const answered = Object.keys(answers).length
  const totalQuestions = questions.length
  const progress = totalQuestions > 0 ? (answered / totalQuestions) * 100 : 0

  return (
    <div style={{ minHeight: '100vh', background: 'var(--bg-main)', color: 'var(--text-main)', display: 'flex', flexDirection: 'column' }}>
      
      {/* Proctoring camera & guard */}
      <FaceDetectionGuard onViolation={handleViolation} sharedStream={cameraStreamRef.current} />

      {/* Violation Popup Overlay */}
      <AnimatePresence>
        {violationPopup && (
          <div style={{ position: 'fixed', inset: 0, zIndex: 10000, background: 'rgba(0,0,0,0.85)', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 24 }}>
            <motion.div initial={{ opacity: 0, scale: 0.9 }} animate={{ opacity: 1, scale: 1 }} exit={{ opacity: 0, scale: 0.9 }}
              style={{ background: '#161b22', border: '1px solid #ef4444', borderRadius: 24, padding: 40, maxWidth: 480, width: '100%', textAlign: 'center' }}>
              <div style={{ width: 64, height: 64, borderRadius: '50%', background: 'rgba(239,68,68,0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 24px' }}>
                <AlertTriangle size={32} color="#ef4444" />
              </div>
              <h2 style={{ color: '#fff', fontSize: 24, fontWeight: 800, marginBottom: 12 }}>Proctoring Alert</h2>
              <p style={{ color: '#ef4444', fontSize: 16, fontWeight: 700, marginBottom: 8 }}>Violation Detected: {violationPopup.reason}</p>
              <p style={{ color: '#94a3b8', fontSize: 14, marginBottom: 24 }}>
                This is warning <strong style={{ color: '#f87171' }}>{violationPopup.count} of {MAX_VIOLATIONS}</strong>.
                {violationPopup.autoSubmit ? ' Maximum violations reached. The assessment is being submitted automatically.' : ' Please keep your face aligned, do not switch tabs or windows, and remain silent.'}
              </p>
              {!violationPopup.autoSubmit && (
                <button onClick={() => setViolationPopup(null)} className="btn-primary" style={{ margin: '0 auto' }}>I Understand</button>
              )}
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Confirm Final Submit Modal */}
      <AnimatePresence>
        {showConfirm && (
          <div style={{ position: 'fixed', inset: 0, zIndex: 9999, background: 'rgba(5,8,22,0.8)', backdropFilter: 'blur(8px)', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 20 }}>
            <motion.div initial={{ opacity: 0, y: 15 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: 15 }}
              style={{ background: '#161b22', border: '1px solid rgba(255,255,255,0.08)', borderRadius: 24, padding: 32, maxWidth: 460, width: '100%', boxShadow: '0 20px 50px rgba(0,0,0,0.5)' }}>
              <h3 style={{ fontSize: 20, fontWeight: 800, color: '#fff', marginBottom: 12 }}>Submit Assessment?</h3>
              <p style={{ color: '#94a3b8', fontSize: 14, lineHeight: 1.6, marginBottom: 24 }}>
                Are you sure you want to finish the entire assessment? Once submitted, you cannot change your answers or write any more code.
              </p>
              <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end' }}>
                <button onClick={() => setShowConfirm(false)} style={{ padding: '10px 20px', borderRadius: 10, background: 'rgba(255,255,255,0.05)', border: 'none', color: '#94a3b8', fontWeight: 600, cursor: 'pointer' }}>Cancel</button>
                <button onClick={() => handleFinish(true)} className="btn-primary" style={{ background: 'linear-gradient(135deg,#7c3aed,#4f46e5)' }}>Submit Assessment</button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Section View Routing */}
      {currentSection === 'quiz' ? (
        // ── QUIZ SECTION ─────────────────────────────────────────────────────────────
        <div style={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
          {/* Header Bar */}
          <div style={{ height: 72, background: 'rgba(6,8,24,0.92)', borderBottom: '1px solid rgba(255,255,255,0.06)', position: 'sticky', top: 0, zIndex: 40, backdropFilter: 'blur(12px)' }}>
            <div style={{ maxWidth: 1200, margin: '0 auto', height: '100%', padding: '0 24px', display: 'flex', alignItems: 'center', justifyItems: 'space-between', gap: 32 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                <span style={{ fontSize: 16, fontWeight: 800, color: '#fff' }}>{exam?.title}</span>
                <span style={{ background: 'rgba(124,58,237,0.15)', color: '#a78bfa', padding: '4px 10px', borderRadius: 12, fontSize: 11, fontWeight: 700 }}>QUIZ SECTION</span>
              </div>

              <div style={{ flex: 1 }}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 6, fontSize: 12, fontWeight: 600 }}>
                  <span style={{ color: 'rgba(255,255,255,0.5)' }}>Progress: {answered} / {totalQuestions}</span>
                  <span style={{
                    display: 'flex', alignItems: 'center', gap: 6,
                    color: isUrgent ? '#f87171' : '#38bdf8',
                    fontFamily: 'monospace', fontSize: 15, fontWeight: 700,
                    ...(isUrgent ? { animation: 'pulse 1s ease-in-out infinite' } : {}),
                  }}>
                    <Clock size={14} /> {timeLeft !== null ? formatTime(timeLeft) : 'No limit'}
                  </span>
                </div>
                <div className="progress-track"><div className="progress-fill" style={{ width: `${progress}%` }} /></div>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                {violations > 0 && (
                  <span style={{ display: 'flex', alignItems: 'center', gap: 4, background: 'rgba(239,68,68,0.15)', border: '1px solid rgba(239,68,68,0.3)', color: '#ef4444', padding: '6px 12px', borderRadius: 8, fontSize: 12, fontWeight: 700 }}>
                    <ShieldAlert size={14} /> {violations}/{MAX_VIOLATIONS}
                  </span>
                )}
                {codingTests.length > 0 && (
                  <button onClick={() => setCurrentSection('coding')} className="btn-primary" style={{ padding: '10px 20px', background: '#21262d', border: '1px solid rgba(255,255,255,0.1)' }}>
                    Coding Section <ChevronRight size={14} />
                  </button>
                )}
                <button onClick={() => setShowConfirm(true)} className="btn-primary" style={{ padding: '10px 20px' }}>
                  <Send size={14} /> Submit Exam
                </button>
              </div>
            </div>
          </div>

          {/* Main Area */}
          <div style={{ flex: 1, padding: '32px 40px' }}>
            {totalQuestions === 0 ? (
              <div style={{ textAlign: 'center', marginTop: 80 }}>
                <p style={{ color: 'var(--text-sec)', marginBottom: 20 }}>No MCQ questions in this section.</p>
                {codingTests.length > 0 && (
                  <button onClick={() => setCurrentSection('coding')} className="btn-primary">Go to Coding Section</button>
                )}
              </div>
            ) : (
              <div style={{ maxWidth: 1200, margin: '0 auto', display: 'grid', gridTemplateColumns: '1fr 300px', gap: 32, alignItems: 'start' }}>
                {/* Left: Question Card */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
                  <AnimatePresence mode="wait">
                    <motion.div key={currentQuestion} initial={{ opacity: 0, x: 10 }} animate={{ opacity: 1, x: 0 }} exit={{ opacity: 0, x: -10 }} transition={{ duration: 0.2 }}
                      className="card" style={{ padding: '40px 48px', minHeight: 460, display: 'flex', flexDirection: 'column' }}>

                      {/* Question Header */}
                      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 28 }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                          <div style={{ width: 42, height: 42, borderRadius: 12, background: 'rgba(124,58,237,0.15)', color: '#a78bfa', fontSize: 16, fontWeight: 700, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                            {currentQuestion + 1}
                          </div>
                          <span style={{ fontSize: 14, color: 'var(--text-sec)', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.06em' }}>
                            Question {currentQuestion + 1} of {totalQuestions}
                          </span>
                          {markedForReview.has(questions[currentQuestion]?.id) && (
                            <span style={{ display: 'flex', alignItems: 'center', gap: 4, fontSize: 11, fontWeight: 700, color: '#f59e0b', background: 'rgba(245,158,11,0.12)', border: '1px solid rgba(245,158,11,0.3)', borderRadius: 6, padding: '3px 8px' }}>
                              <Flag size={10} /> Marked for Review
                            </span>
                          )}
                        </div>

                        <button onClick={() => toggleReview(questions[currentQuestion]?.id)}
                          style={{
                            display: 'flex', alignItems: 'center', gap: 6, padding: '7px 14px', borderRadius: 8, fontSize: 12, fontWeight: 700, cursor: 'pointer', border: 'none', transition: 'all 0.2s',
                            background: markedForReview.has(questions[currentQuestion]?.id) ? 'rgba(245,158,11,0.15)' : 'rgba(255,255,255,0.06)',
                            color: markedForReview.has(questions[currentQuestion]?.id) ? '#f59e0b' : 'var(--text-sec)',
                          }}>
                          <Flag size={13} /> {markedForReview.has(questions[currentQuestion]?.id) ? 'Unmark' : 'Mark for Review'}
                        </button>
                      </div>

                      {/* Question Text */}
                      <h2 style={{ fontSize: 20, fontWeight: 700, color: '#fff', lineHeight: 1.5, marginBottom: 32 }}>
                        {questions[currentQuestion]?.questionText}
                      </h2>

                      {/* Options */}
                      <div style={{ display: 'flex', flexDirection: 'column', gap: 16, flex: 1 }}>
                        {[
                          { key: 'A', val: questions[currentQuestion]?.optionA },
                          { key: 'B', val: questions[currentQuestion]?.optionB },
                          { key: 'C', val: questions[currentQuestion]?.optionC },
                          { key: 'D', val: questions[currentQuestion]?.optionD }
                        ].filter(o => o.val).map(opt => {
                          const isSelected = answers[questions[currentQuestion]?.id] === opt.key
                          return (
                            <div key={opt.key} onClick={() => selectAnswer(questions[currentQuestion]?.id, opt.key, questions[currentQuestion]?.quizAttemptId)}
                              className={`option-card ${isSelected ? 'active' : ''}`}
                              style={{ display: 'flex', alignItems: 'center', gap: 16, padding: '18px 24px', borderRadius: 14, cursor: 'pointer', border: '1px solid rgba(255,255,255,0.05)', background: isSelected ? 'rgba(124,58,237,0.12)' : 'rgba(255,255,255,0.02)', transition: 'all 0.15s' }}>
                              <div style={{
                                width: 22, height: 22, borderRadius: '50%', border: isSelected ? '2px solid #a78bfa' : '2px solid rgba(255,255,255,0.2)',
                                display: 'flex', alignItems: 'center', justifyContent: 'center'
                              }}>
                                {isSelected && <div style={{ width: 10, height: 10, borderRadius: '50%', background: '#a78bfa' }} />}
                              </div>
                              <span style={{ fontSize: 15, fontWeight: 500, color: isSelected ? '#fff' : 'var(--text-sec)' }}>{opt.val}</span>
                            </div>
                          )
                        })}
                      </div>

                      {/* Nav Buttons */}
                      <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 40, paddingTop: 24, borderTop: '1px solid rgba(255,255,255,0.05)' }}>
                        <button disabled={currentQuestion === 0} onClick={() => setCurrentQuestion(c => c - 1)} className="btn-sec" style={{ opacity: currentQuestion === 0 ? 0.3 : 1 }}>
                          <ChevronLeft size={16} /> Previous
                        </button>
                        
                        {currentQuestion < totalQuestions - 1 ? (
                          <button onClick={() => setCurrentQuestion(c => c + 1)} className="btn-primary">
                            Next Question <ChevronRight size={16} />
                          </button>
                        ) : codingTests.length > 0 ? (
                          <button onClick={() => setCurrentSection('coding')} className="btn-primary" style={{ background: 'linear-gradient(135deg,#3b82f6,#2563eb)' }}>
                            Continue to Coding Section <ChevronRight size={16} />
                          </button>
                        ) : (
                          <button onClick={() => setShowConfirm(true)} className="btn-primary">
                            Final Submit <CheckCircle2 size={16} />
                          </button>
                        )}
                      </div>

                    </motion.div>
                  </AnimatePresence>
                </div>

                {/* Right: Question Navigation Panel */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
                  <div className="card" style={{ padding: 24 }}>
                    <h3 style={{ fontSize: 14, fontWeight: 700, color: 'var(--text-sec)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 16 }}>Questions</h3>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: 10 }}>
                      {questions.map((q, idx) => {
                        const isCurrent = idx === currentQuestion
                        const isAnswered = answers[q.id] !== undefined
                        const isMarked = markedForReview.has(q.id)
                        let bg = 'rgba(255,255,255,0.04)', color = 'var(--text-sec)', border = '1px solid rgba(255,255,255,0.06)'
                        if (isCurrent) {
                          bg = 'rgba(124,58,237,0.15)'
                          border = '2px solid #a78bfa'
                          color = '#fff'
                        } else if (isMarked) {
                          bg = 'rgba(245,158,11,0.2)'
                          color = '#fbbf24'
                          border = '1px solid rgba(245,158,11,0.4)'
                        } else if (isAnswered) {
                          bg = 'rgba(16,185,129,0.15)'
                          color = '#34d399'
                          border = '1px solid rgba(16,185,129,0.3)'
                        }
                        return (
                          <button key={q.id} onClick={() => setCurrentQuestion(idx)}
                            style={{ height: 42, borderRadius: 10, background: bg, border, color, fontWeight: 700, fontSize: 14, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', transition: 'all 0.15s' }}>
                            {idx + 1}
                          </button>
                        )
                      })}
                    </div>

                    <div style={{ marginTop: 24, display: 'flex', flexDirection: 'column', gap: 10, fontSize: 12, borderTop: '1px solid rgba(255,255,255,0.05)', paddingTop: 16 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}><div style={{ width: 12, height: 12, borderRadius: 3, background: 'rgba(16,185,129,0.15)', border: '1px solid rgba(16,185,129,0.3)' }} /> Answered</div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}><div style={{ width: 12, height: 12, borderRadius: 3, background: 'rgba(245,158,11,0.2)', border: '1px solid rgba(245,158,11,0.4)' }} /> Marked for Review</div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}><div style={{ width: 12, height: 12, borderRadius: 3, background: 'rgba(255,255,255,0.04)', border: '1px solid rgba(255,255,255,0.06)' }} /> Unanswered</div>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      ) : (
        // ── CODING SECTION ───────────────────────────────────────────────────────────
        <div style={{ display: 'flex', flexDirection: 'column', flex: 1, minHeight: 0 }}>
          {/* Header Bar */}
          <div className="editor-header">
            <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
              {questions.length > 0 && (
                <button onClick={() => setCurrentSection('quiz')}
                  style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 13, color: '#94a3b8', background: 'none', border: 'none', cursor: 'pointer' }}>
                  <ArrowLeft size={14} /> Back to Quiz
                </button>
              )}
              {questions.length > 0 && <div style={{ width: 1, height: 16, background: 'rgba(255,255,255,0.1)' }} />}
              <span style={{ fontSize: 14, fontWeight: 700, color: '#e2e8f0' }}>Coding Challenge</span>
              {codingTests.length > 1 && (
                <select value={currentCoding} onChange={e => { setCurrentCoding(Number(e.target.value)); setConsoleOutput(null) }}
                  style={{ background: '#21262d', border: '1px solid rgba(255,255,255,0.12)', color: '#e2e8f0', padding: '4px 10px', borderRadius: 6, fontSize: 12, outline: 'none', cursor: 'pointer' }}>
                  {codingTests.map((t, idx) => <option key={t.id} value={idx}>Problem {idx + 1}: {t.title}</option>)}
                </select>
              )}
              {violations > 0 && (
                <div style={{ display: 'flex', alignItems: 'center', gap: 6, padding: '3px 10px', borderRadius: 6, background: 'rgba(245,158,11,0.1)', border: '1px solid rgba(245,158,11,0.3)', fontSize: 12, fontWeight: 700, color: '#f59e0b' }}>
                  <ShieldAlert size={12} /> {violations}/{MAX_VIOLATIONS}
                </div>
              )}
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              {/* Timer */}
              {timeLeft !== null && (
                <div className={`editor-timer ${timerClass}`}>
                  <Clock size={14} />
                  {formatTime(timeLeft)}
                </div>
              )}

              <select value={codingStates[codingTests[currentCoding]?.id]?.language || 'javascript'} onChange={e => handleLangChange(e.target.value)}
                style={{ background: '#21262d', border: '1px solid rgba(255,255,255,0.12)', color: '#e2e8f0', padding: '6px 12px', borderRadius: 6, fontSize: 13, outline: 'none', cursor: 'pointer' }}>
                {LANGUAGES.map(l => <option key={l.val} value={l.val} style={{ background: '#161b22', color: '#e2e8f0' }}>{l.label}</option>)}
              </select>

              <button onClick={handleRunCode} disabled={codingStates[codingTests[currentCoding]?.id]?.running}
                style={{ display: 'flex', alignItems: 'center', gap: 6, padding: '7px 16px', borderRadius: 8, background: '#21262d', border: '1px solid rgba(56,189,248,0.3)', color: '#38bdf8', fontSize: 13, fontWeight: 600, cursor: 'pointer' }}>
                {codingStates[codingTests[currentCoding]?.id]?.running ? <Loader2 size={14} style={{ animation: 'spin 0.8s linear infinite' }} /> : <Play size={14} />} Run
              </button>

              <button onClick={handleSubmitCode} disabled={codingStates[codingTests[currentCoding]?.id]?.submitting}
                style={{ display: 'flex', alignItems: 'center', gap: 6, padding: '7px 18px', borderRadius: 8, background: '#21262d', border: '1px solid rgba(167,139,250,0.3)', color: '#a78bfa', fontSize: 13, fontWeight: 600, cursor: 'pointer' }}>
                {codingStates[codingTests[currentCoding]?.id]?.submitting ? <Loader2 size={14} style={{ animation: 'spin 0.8s linear infinite' }} /> : <Send size={14} />} Submit Code
              </button>

              <button onClick={() => setShowConfirm(true)}
                style={{ display: 'flex', alignItems: 'center', gap: 6, padding: '7px 18px', borderRadius: 8, background: 'linear-gradient(135deg,#ef4444,#dc2626)', border: 'none', color: '#fff', fontSize: 13, fontWeight: 700, cursor: 'pointer', boxShadow: '0 2px 12px rgba(239,68,68,0.4)' }}>
                Final Submit
              </button>
            </div>
          </div>

          {/* Body */}
          <div className="editor-body" style={{ flex: 1, minHeight: 0 }}>
            {/* Problem Panel */}
            <div className="problem-panel" style={{ overflowY: 'auto' }}>
              {codingTests[currentCoding] ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 22 }}>
                  <div>
                    <h2 style={{ fontSize: 20, fontWeight: 800, color: '#e2e8f0', marginBottom: 8, lineHeight: 1.3 }}>{codingTests[currentCoding].title}</h2>
                    <span className={`badge ${diffClass[codingTests[currentCoding].difficulty] || ''}`}>{codingTests[currentCoding].difficulty}</span>
                  </div>
                  <div>
                    <div style={{ fontSize: 11, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.08em', color: '#64748b', marginBottom: 10 }}>Description</div>
                    <div className="leetcode-description" style={{ fontSize: 14, color: '#cbd5e1', lineHeight: 1.7 }} dangerouslySetInnerHTML={{ __html: codingTests[currentCoding].description }} />
                  </div>
                  {codingTests[currentCoding].sampleInput && (
                    <div>
                      <div style={{ fontSize: 11, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.08em', color: '#64748b', marginBottom: 10 }}>Sample Input</div>
                      <pre style={{ background: '#0d1117', padding: '12px 14px', borderRadius: 8, fontSize: 13, color: '#38bdf8', overflowX: 'auto', border: '1px solid rgba(56,189,248,0.2)', fontFamily: "'Fira Code', monospace", lineHeight: 1.6 }}>{codingTests[currentCoding].sampleInput}</pre>
                    </div>
                  )}
                  {codingTests[currentCoding].sampleOutput && (
                    <div>
                      <div style={{ fontSize: 11, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.08em', color: '#64748b', marginBottom: 10 }}>Expected Output</div>
                      <pre style={{ background: '#0d1117', padding: '12px 14px', borderRadius: 8, fontSize: 13, color: '#4ade80', overflowX: 'auto', border: '1px solid rgba(74,222,128,0.2)', fontFamily: "'Fira Code', monospace", lineHeight: 1.6 }}>{codingTests[currentCoding].sampleOutput}</pre>
                    </div>
                  )}
                </div>
              ) : (
                <div style={{ display: 'flex', justifyContent: 'center', paddingTop: 40 }}><div className="spinner" /></div>
              )}
            </div>

            {/* Monaco Editor Panel */}
            <div className="editor-right" style={{ display: 'flex', flexDirection: 'column', minHeight: 0 }}>
              <div style={{ flex: 1, position: 'relative', minHeight: 0 }}>
                <Editor
                  language={codingStates[codingTests[currentCoding]?.id]?.language || 'javascript'}
                  value={codingStates[codingTests[currentCoding]?.id]?.code || ''}
                  onChange={handleCodeChange}
                  theme="vs-dark"
                  options={{
                    fontSize: 15,
                    fontFamily: "'Fira Code', 'Cascadia Code', monospace",
                    fontLigatures: true,
                    minimap: { enabled: false },
                    padding: { top: 20 },
                    lineNumbers: 'on',
                    renderLineHighlight: 'all',
                    scrollBeyondLastLine: false,
                  }}
                />
              </div>

              {/* Console Output Panel */}
              <div style={{ height: '240px', background: '#0b0e14', borderTop: '1px solid rgba(255,255,255,0.08)', display: 'flex', flexDirection: 'column' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '10px 16px', background: '#11151d', borderBottom: '1px solid rgba(255,255,255,0.05)', fontSize: 12, fontWeight: 700, color: '#94a3b8' }}>
                  <Terminal size={14} /> Console / Output
                </div>
                <div style={{ flex: 1, padding: 16, overflowY: 'auto', fontFamily: "'Fira Code', monospace", fontSize: 13 }}>
                  {!consoleOutput ? (
                    <div style={{ color: '#64748b', fontSize: 13 }}>Click "Run" or "Submit Code" to see compilation and test output here.</div>
                  ) : consoleOutput.type === 'error' ? (
                    <div style={{ color: '#ef4444' }}>{consoleOutput.data}</div>
                  ) : consoleOutput.type === 'run' ? (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        {consoleOutput.data.success ? (
                          <span style={{ display: 'flex', alignItems: 'center', gap: 4, color: '#4ade80', fontWeight: 700 }}><CheckCircle2 size={15} /> RUN SUCCESS</span>
                        ) : (
                          <span style={{ display: 'flex', alignItems: 'center', gap: 4, color: '#ef4444', fontWeight: 700 }}><XCircle size={15} /> RUN FAIL</span>
                        )}
                        <span style={{ color: '#64748b' }}>({consoleOutput.data.executionTimeMs || 0}ms)</span>
                      </div>
                      {consoleOutput.data.compileMessage && (
                        <pre style={{ color: '#cbd5e1', whiteSpace: 'pre-wrap', background: '#0d1117', padding: 12, borderRadius: 6 }}>{consoleOutput.data.compileMessage}</pre>
                      )}
                      <div>
                        <div style={{ color: '#94a3b8', fontSize: 11, textTransform: 'uppercase', fontWeight: 700, marginBottom: 4 }}>Standard Output:</div>
                        <pre style={{ color: '#e2e8f0', whiteSpace: 'pre-wrap' }}>{consoleOutput.data.stdout || '(no stdout output)'}</pre>
                      </div>
                    </div>
                  ) : (
                    // Submit output
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <span style={{
                          fontWeight: 700, padding: '4px 10px', borderRadius: 6, fontSize: 12,
                          background: consoleOutput.data.status === 'ACCEPTED' ? 'rgba(74,222,128,0.15)' : 'rgba(239,68,68,0.15)',
                          color: consoleOutput.data.status === 'ACCEPTED' ? '#4ade80' : '#ef4444'
                        }}>{consoleOutput.data.status}</span>
                        <span style={{ color: '#94a3b8' }}>
                          Passed: {consoleOutput.data.testCasesPassed} / {consoleOutput.data.totalTestCases} test cases.
                        </span>
                      </div>
                      {consoleOutput.data.errorMessage && (
                        <div style={{ color: '#ef4444', background: 'rgba(239,68,68,0.06)', padding: 12, borderRadius: 6, border: '1px solid rgba(239,68,68,0.15)' }}>
                          <strong>Error message:</strong>
                          <pre style={{ whiteSpace: 'pre-wrap', marginTop: 4 }}>{consoleOutput.data.errorMessage}</pre>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
