import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { examTokenApi } from '../../api/axios'
import { AlertTriangle, Clock, ArrowRight, XCircle, X } from 'lucide-react'
import { motion } from 'framer-motion'

export default function ExamEntry() {
  const { token } = useParams()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [examData, setExamData] = useState(null)
  const [now, setNow] = useState(new Date())
  const [showInstructions, setShowInstructions] = useState(false)

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

  const handleEnterExamClick = () => {
    setShowInstructions(true)
  }

  const handleConfirmStart = () => {
    setShowInstructions(false)
    navigate(`/assessment/${examData.examId}?token=${token}&studentId=${examData.studentId}`, { state: { requestFullscreen: true } })
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
          {examData.examType === 'CODING' ? (
            <div style={{ marginBottom: 20 }}>
              <div style={{ color: '#64748b', fontSize: 12, textTransform: 'uppercase', fontWeight: 700, marginBottom: 6 }}>Instructions</div>
              <div style={{ color: '#cbd5e1', fontSize: 14, lineHeight: 1.6, display: 'flex', flexDirection: 'column', gap: 8 }}>
                <div>• Please ensure you have a stable internet connection and your camera is working.</div>
                <div>• The assessment requires you to stay in fullscreen mode.</div>
                <div>• Switching tabs or exiting fullscreen may flag a violation.</div>
                <div>• The exam will automatically submit when the timer ends.</div>
              </div>
            </div>
          ) : examData.description && (
            <div style={{ marginBottom: 20 }}>
              <div style={{ color: '#64748b', fontSize: 12, textTransform: 'uppercase', fontWeight: 700, marginBottom: 6 }}>Instructions</div>
              <p style={{ color: '#cbd5e1', fontSize: 14, lineHeight: 1.6 }} dangerouslySetInnerHTML={{ __html: examData.description }}></p>
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
            <button onClick={handleEnterExamClick} style={{ width: '100%', padding: 16, borderRadius: 12, background: 'linear-gradient(135deg, #7c3aed, #4f46e5)', color: '#fff', fontSize: 16, fontWeight: 700, border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 10, boxShadow: '0 10px 25px rgba(124,58,237,0.3)' }}>
              Enter Exam <ArrowRight size={18} />
            </button>
          )
        })()}
      </motion.div>

      {/* Test Instructions Modal */}
      {showInstructions && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: 'rgba(0, 0, 0, 0.75)',
          backdropFilter: 'blur(5px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 9999,
          padding: '24px'
        }}>
          <motion.div 
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            style={{
              background: '#ffffff',
              borderRadius: '16px',
              maxWidth: '660px',
              width: '100%',
              boxShadow: '0 25px 50px -12px rgba(0,0,0,0.5)',
              border: '1px solid rgba(0,0,0,0.1)',
              display: 'flex',
              flexDirection: 'column',
              maxHeight: '90vh',
              overflow: 'hidden'
            }}
          >
            {/* Header */}
            <div style={{
              padding: '20px 24px',
              borderBottom: '1px solid #e2e8f0',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              background: '#ffffff'
            }}>
              <h2 style={{
                margin: 0,
                fontSize: '16px',
                fontWeight: 700,
                color: '#1e293b',
                letterSpacing: '0.05em'
              }}>TEST INSTRUCTIONS</h2>
              <button 
                onClick={() => setShowInstructions(false)}
                style={{
                  background: 'none',
                  border: 'none',
                  cursor: 'pointer',
                  padding: 4,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: '#64748b',
                  borderRadius: '50%',
                  transition: 'background-color 0.2s'
                }}
                onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f1f5f9'}
                onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
              >
                <X size={20} />
              </button>
            </div>

            {/* Scrollable Rules List */}
            <div style={{
              padding: '28px 24px',
              overflowY: 'auto',
              flex: 1,
              color: '#334155',
              fontSize: '14.5px',
              lineHeight: '1.7',
              background: '#ffffff'
            }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '20px', textAlign: 'left' }}>
                <div style={{ display: 'flex', gap: '12px' }}>
                  <span style={{ fontWeight: 600, color: '#64748b', minWidth: '20px' }}>1.</span>
                  <span>Use recommended browser such as Chrome, Edge, Brave, Opera, or Safari when opening the Test link. Avoid using instant-click browsers as they may impair your usability.</span>
                </div>

                <div style={{ display: 'flex', gap: '12px' }}>
                  <span style={{ fontWeight: 600, color: '#64748b', minWidth: '20px' }}>2.</span>
                  <span>Fill in your attendee details accurately, i.e, your Name, Email ID, etc. Any obscured or incomplete details entered leads to disqualification.</span>
                </div>

                <div style={{ display: 'flex', gap: '12px', color: '#dc2626' }}>
                  <span style={{ fontWeight: 700, minWidth: '20px' }}>3.</span>
                  <span style={{ fontWeight: 600 }}>You can log in a maximum of 5 times within the test duration before submitting the test. After submission, you will not be able to retake the test.</span>
                </div>

                <div style={{ display: 'flex', gap: '12px' }}>
                  <span style={{ fontWeight: 600, color: '#64748b', minWidth: '20px' }}>4.</span>
                  <span>If you do not complete the test within the given time, it will automatically submit with the number of answers you attempted.</span>
                </div>

                <div style={{ display: 'flex', gap: '12px' }}>
                  <span style={{ fontWeight: 600, color: '#64748b', minWidth: '20px' }}>5.</span>
                  <span>Do not exit fullscreen mode, leave the test page, or switch tabs, as it may lead to disqualification.</span>
                </div>

                <div style={{ display: 'flex', gap: '12px' }}>
                  <span style={{ fontWeight: 600, color: '#64748b', minWidth: '20px' }}>6.</span>
                  <span>Some questions may contain images. If you find them difficult to read, simply click on the image to zoom in for a better view.</span>
                </div>

                <div style={{ display: 'flex', gap: '12px' }}>
                  <span style={{ fontWeight: 600, color: '#64748b', minWidth: '20px' }}>7.</span>
                  <span>If you experience any technical issues during the test, please use the live chat option for assistance.</span>
                </div>

                <div style={{
                  textAlign: 'center',
                  fontWeight: 700,
                  fontSize: '15px',
                  color: '#1e293b',
                  marginTop: '16px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '6px'
                }}>
                  All the Best for your Test 🤝
                </div>
              </div>
            </div>

            {/* Footer button */}
            <div style={{
              padding: '20px 24px',
              borderTop: '1px solid #e2e8f0',
              display: 'flex',
              justifyContent: 'center',
              background: '#ffffff',
              borderBottomLeftRadius: '16px',
              borderBottomRightRadius: '16px'
            }}>
              <button
                onClick={handleConfirmStart}
                style={{
                  background: '#0066ff',
                  color: '#ffffff',
                  border: 'none',
                  borderRadius: '6px',
                  padding: '12px 32px',
                  fontSize: '14.5px',
                  fontWeight: 700,
                  cursor: 'pointer',
                  boxShadow: '0 4px 14px rgba(0, 102, 255, 0.35)',
                  transition: 'background-color 0.2s, transform 0.1s'
                }}
                onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#0052cc'}
                onMouseLeave={(e) => e.currentTarget.style.backgroundColor = '#0066ff'}
              >
                I understood
              </button>
            </div>
          </motion.div>
        </div>
      )}
    </div>
  )
}
