import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { examTokenApi } from '../../api/axios'
import { AlertTriangle, Clock, ArrowRight, XCircle } from 'lucide-react'
import { motion } from 'framer-motion'

export default function ExamEntry() {
  const { token } = useParams()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [examData, setExamData] = useState(null)
  const [now, setNow] = useState(new Date())

  useEffect(() => {
    verifyToken()
  }, [token])

  useEffect(() => {
    const timer = setInterval(() => setNow(new Date()), 1000)
    return () => clearInterval(timer)
  }, [])

  const verifyToken = async () => {
    try {
      const res = await examTokenApi.verify(token)
      setExamData(res.data.data)
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid or expired token')
    } finally {
      setLoading(false)
    }
  }

  const handleStart = () => {
    if (examData.examType === 'QUIZ') {
      navigate(`/student/quizzes/${examData.examId}/attempt?token=${token}&studentId=${examData.studentId}`)
    } else {
      navigate(`/student/coding/${examData.examId}?token=${token}&studentId=${examData.studentId}`)
    }
  }

  if (loading) {
    return (
      <div style={{ minHeight: '100vh', background: '#0d1117', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <div className="spinner" />
      </div>
    )
  }

  if (error) {
    return (
      <div style={{ minHeight: '100vh', background: '#0d1117', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 24 }}>
        <motion.div initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }}
          style={{ background: '#161b22', border: '1px solid #ef4444', borderRadius: 20, padding: 40, maxWidth: 460, width: '100%', textAlign: 'center' }}>
          <XCircle size={48} color="#ef4444" style={{ margin: '0 auto 20px' }} />
          <h2 style={{ color: '#fff', fontSize: 24, fontWeight: 700, marginBottom: 12 }}>Access Denied</h2>
          <p style={{ color: '#94a3b8', fontSize: 15 }}>{error}</p>
        </motion.div>
      </div>
    )
  }

  return (
    <div style={{ minHeight: '100vh', background: '#0d1117', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 24 }}>
      <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }}
        style={{ background: '#161b22', border: '1px solid rgba(255,255,255,0.1)', borderRadius: 24, padding: '48px', maxWidth: 540, width: '100%' }}>
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <div style={{ display: 'inline-block', padding: '8px 16px', background: 'rgba(124,58,237,0.15)', color: '#a78bfa', borderRadius: 20, fontSize: 13, fontWeight: 700, letterSpacing: '0.05em', marginBottom: 16 }}>
            {examData.examType === 'QUIZ' ? 'ASSESSMENT QUIZ' : 'CODING CHALLENGE'}
          </div>
          <h1 style={{ fontSize: 28, color: '#fff', fontWeight: 800, marginBottom: 8 }}>{examData.examTitle}</h1>
          <p style={{ color: '#94a3b8', fontSize: 15 }}>Welcome, <strong style={{ color: '#e2e8f0' }}>{examData.studentName || examData.studentEmail}</strong></p>
        </div>

        <div style={{ background: '#0d1117', borderRadius: 16, padding: 24, marginBottom: 32 }}>
          {examData.description && (
            <div style={{ marginBottom: 20 }}>
              <div style={{ color: '#64748b', fontSize: 12, textTransform: 'uppercase', fontWeight: 700, marginBottom: 6 }}>Instructions</div>
              <p style={{ color: '#cbd5e1', fontSize: 14, lineHeight: 1.6 }}>{examData.description}</p>
            </div>
          )}
          
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
            {examData.durationMinutes && (
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <Clock size={18} color="#7c3aed" />
                <div>
                  <div style={{ fontSize: 11, color: '#64748b', fontWeight: 700, textTransform: 'uppercase' }}>Duration</div>
                  <div style={{ color: '#fff', fontWeight: 600 }}>{examData.durationMinutes} minutes</div>
                </div>
              </div>
            )}
            {examData.difficulty && (
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <AlertTriangle size={18} color="#fbbf24" />
                <div>
                  <div style={{ fontSize: 11, color: '#64748b', fontWeight: 700, textTransform: 'uppercase' }}>Difficulty</div>
                  <div style={{ color: '#fff', fontWeight: 600 }}>{examData.difficulty}</div>
                </div>
              </div>
            )}
          </div>
        </div>

        {(() => {
          let hasNotStarted = false
          let hasEnded = false
          if (examData.scheduledFor && new Date(examData.scheduledFor) > now) {
            hasNotStarted = true
          }
          if (examData.validUntil && new Date(examData.validUntil) < now) {
            hasEnded = true
          }

          if (hasEnded) {
            return (
              <div style={{ textAlign: 'center', padding: 16, background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)', borderRadius: 12, color: '#f87171' }}>
                <AlertTriangle size={24} style={{ margin: '0 auto 8px' }} />
                <div style={{ fontWeight: 700 }}>Exam Concluded</div>
                <div style={{ fontSize: 13, marginTop: 4 }}>This exam ended on {new Date(examData.validUntil).toLocaleString()}</div>
              </div>
            )
          }

          if (hasNotStarted) {
            return (
              <div style={{ textAlign: 'center', padding: 16, background: 'rgba(251,191,36,0.1)', border: '1px solid rgba(251,191,36,0.3)', borderRadius: 12, color: '#fbbf24' }}>
                <Clock size={24} style={{ margin: '0 auto 8px' }} />
                <div style={{ fontWeight: 700 }}>Exam Not Yet Started</div>
                <div style={{ fontSize: 13, marginTop: 4 }}>This exam will start on {new Date(examData.scheduledFor).toLocaleString()}</div>
              </div>
            )
          }

          return (
            <button onClick={handleStart} style={{ width: '100%', padding: 16, borderRadius: 12, background: 'linear-gradient(135deg, #7c3aed, #4f46e5)', color: '#fff', fontSize: 16, fontWeight: 700, border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 10, boxShadow: '0 10px 25px rgba(124,58,237,0.3)' }}>
              Enter Exam <ArrowRight size={18} />
            </button>
          )
        })()}
      </motion.div>
    </div>
  )
}
