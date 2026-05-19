import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { motion, AnimatePresence } from 'framer-motion'
import { authApi } from '../../api/axios'
import logo from '../../assets/logo.png'
import { useAuth } from '../../context/AuthContext'
import toast from 'react-hot-toast'
import { Zap, Mail, Lock, Eye, EyeOff, ArrowRight, Loader2 } from 'lucide-react'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', password: '' })
  const [loading, setLoading] = useState(false)
  const [showPw, setShowPw] = useState(false)

  // Forgot password states
  const [showForgotModal, setShowForgotModal] = useState(false)
  const [forgotStep, setForgotStep] = useState(1) // 1: enter email, 2: enter otp & new pw
  const [forgotEmail, setForgotEmail] = useState('')
  const [otpCode, setOtpCode] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [forgotLoading, setForgotLoading] = useState(false)

  const handleRequestOtp = async (e) => {
    e.preventDefault()
    setForgotLoading(true)
    try {
      await authApi.forgotPasswordRequest({ email: forgotEmail })
      toast.success('Verification code sent to your email!')
      setForgotStep(2)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to send verification code. Please check the email.')
    } finally {
      setForgotLoading(false)
    }
  }

  const handleResetPassword = async (e) => {
    e.preventDefault()
    setForgotLoading(true)
    try {
      await authApi.forgotPasswordReset({
        email: forgotEmail,
        otp: otpCode,
        newPassword: newPassword
      })
      toast.success('Password reset successfully! You can now log in.')
      setShowForgotModal(false)
      // Reset state
      setForgotStep(1)
      setForgotEmail('')
      setOtpCode('')
      setNewPassword('')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to reset password. Please check the details.')
    } finally {
      setForgotLoading(false)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const res = await authApi.login(form)
      const data = res.data.data
      login(data)
      toast.success(`Welcome back, ${data.name}!`)
      // If a shareable quiz link was saved, redirect there (students only)
      const savedRedirect = localStorage.getItem('quiz_redirect')
      if (savedRedirect && data.role === 'STUDENT') {
        localStorage.removeItem('quiz_redirect')
        navigate(savedRedirect, { replace: true })
      } else {
        navigate(data.role === 'ADMIN' ? '/admin/dashboard' : '/student/dashboard', { replace: true })
      }
    } catch (err) {
      toast.error(err.response?.data?.message || 'Invalid credentials. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ minHeight: '100vh', background: 'var(--bg-main)', display: 'flex' }}>

      {/* ── Left: Branding Panel ── */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '60px 56px', background: 'radial-gradient(ellipse 90% 80% at 40% 50%, rgba(124,58,237,0.13) 0%, transparent 65%)', borderRight: '1px solid var(--glass-border)', position: 'relative', overflow: 'hidden' }}>
        {/* bg orbs */}
        <div style={{ position: 'absolute', top: '10%', left: '10%', width: 340, height: 340, borderRadius: '50%', background: 'rgba(124,58,237,0.06)', border: '1px solid rgba(124,58,237,0.1)', pointerEvents: 'none' }} />
        <div style={{ position: 'absolute', bottom: '15%', right: '10%', width: 200, height: 200, borderRadius: '50%', background: 'rgba(56,189,248,0.06)', border: '1px solid rgba(56,189,248,0.1)', pointerEvents: 'none' }} />

        <motion.div initial={{ opacity: 0, x: -30 }} animate={{ opacity: 1, x: 0 }} transition={{ duration: 0.6 }} style={{ position: 'relative', zIndex: 1, maxWidth: 400, textAlign: 'center' }}>
          {/* Logo */}
          <div className="float-anim" style={{ display: 'flex', justifyContent: 'center', margin: '0 auto 28px' }}>
            <img src={logo} alt="QuizVault Logo" style={{ width: 88, height: 88, borderRadius: 24, boxShadow: '0 0 48px rgba(124,58,237,0.5)', objectFit: 'contain' }} />
          </div>
          <h2 style={{ fontSize: 42, fontWeight: 900, letterSpacing: '-0.03em', marginBottom: 16, lineHeight: 1.1 }} className="grad">QuizVault</h2>

          {/* Divider */}
          <div style={{ width: 60, height: 3, borderRadius: 99, background: 'linear-gradient(90deg, #7c3aed, #38bdf8)', margin: '0 auto 20px' }} />

          <p style={{ color: 'var(--text-sec)', fontSize: 16, lineHeight: 1.7, marginBottom: 36 }}>
            The modern quiz &amp; coding platform.<br />Test your skills and track your progress.
          </p>

          <div style={{ display: 'flex', flexDirection: 'column', gap: 14, textAlign: 'left' }}>
            {[
              { icon: '🎯', text: '10,000+ Quizzes available' },
              { icon: '👥', text: '500K+ Active learners' },
              { icon: '⚡', text: 'Instant grading & analytics' },
            ].map(({ icon, text }) => (
              <div key={text} style={{ display: 'flex', alignItems: 'center', gap: 14, fontSize: 15, color: 'var(--text-sec)', background: 'var(--glass-bg)', border: '1px solid var(--glass-border)', borderRadius: 12, padding: '12px 16px' }}>
                <span style={{ fontSize: 20 }}>{icon}</span>
                <span>{text}</span>
              </div>
            ))}
          </div>
        </motion.div>
      </div>

      {/* ── Right: Login Form ── */}
      <div style={{ width: 520, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '40px 56px', background: 'var(--bg-main)' }}>
        <motion.div initial={{ opacity: 0, y: 24 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5, delay: 0.1 }} style={{ width: '100%' }}>
          {/* Form header */}
          <div style={{ marginBottom: 32 }}>
            <div style={{ display: 'inline-flex', alignItems: 'center', gap: 8, background: 'rgba(124,58,237,0.1)', border: '1px solid rgba(124,58,237,0.2)', borderRadius: 20, padding: '5px 14px', marginBottom: 20 }}>
              <div style={{ width: 7, height: 7, borderRadius: '50%', background: '#7c3aed' }} />
              <span style={{ fontSize: 12, fontWeight: 600, color: '#a78bfa', letterSpacing: '0.04em', textTransform: 'uppercase' }}>Secure Login</span>
            </div>
            <h1 style={{ fontSize: 36, fontWeight: 900, letterSpacing: '-0.025em', color: 'var(--text-main)', marginBottom: 10, lineHeight: 1.1 }}>Welcome back</h1>
            <p style={{ color: 'var(--text-sec)', fontSize: 15.5 }}>Sign in to your QuizVault account</p>
          </div>



          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 22 }}>
            {/* Email */}
            <div>
              <label style={{ display: 'block', fontSize: 14, fontWeight: 600, color: 'var(--text-main)', marginBottom: 10 }}>Email address</label>
              <div style={{ position: 'relative' }}>
                <Mail size={16} color="#6b7280" style={{ position: 'absolute', left: 16, top: '50%', transform: 'translateY(-50%)', pointerEvents: 'none' }} />
                <input
                  type="email" required value={form.email}
                  onChange={e => setForm(f => ({ ...f, email: e.target.value }))}
                  placeholder="you@example.com"
                  className="input-field"
                  style={{ paddingLeft: 46, fontSize: 15, height: 52 }}
                />
              </div>
            </div>

            {/* Password */}
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
                <label style={{ fontSize: 14, fontWeight: 600, color: 'var(--text-main)' }}>Password</label>
                <button type="button" onClick={() => { setShowForgotModal(true); setForgotStep(1); }}
                  style={{ background: 'none', border: 'none', color: '#a78bfa', fontSize: 13, fontWeight: 600, cursor: 'pointer', padding: 0 }}>
                  Forgot password?
                </button>
              </div>
              <div style={{ position: 'relative' }}>
                <Lock size={16} color="#6b7280" style={{ position: 'absolute', left: 16, top: '50%', transform: 'translateY(-50%)', pointerEvents: 'none' }} />
                <input
                  type={showPw ? 'text' : 'password'} required value={form.password}
                  onChange={e => setForm(f => ({ ...f, password: e.target.value }))}
                  placeholder="Enter your password"
                  className="input-field"
                  style={{ paddingLeft: 46, paddingRight: 52, fontSize: 15, height: 52 }}
                />
                <button type="button" onClick={() => setShowPw(s => !s)}
                  style={{ position: 'absolute', right: 16, top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-sec)', display: 'flex', padding: 0 }}>
                  {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>

            {/* Submit */}
            <motion.button
              type="submit" disabled={loading}
              whileHover={!loading ? { scale: 1.01, y: -1 } : {}}
              whileTap={!loading ? { scale: 0.98 } : {}}
              className="btn-primary"
              style={{ justifyContent: 'center', fontSize: 16, padding: '15px', marginTop: 4, height: 54, opacity: loading ? 0.7 : 1, cursor: loading ? 'not-allowed' : 'pointer' }}
            >
              {loading ? <><Loader2 size={18} className="spin" /> Signing in…</> : <>Sign In <ArrowRight size={18} /></>}
            </motion.button>
          </form>

          <div style={{ marginTop: 28, textAlign: 'center' }}>
            <p style={{ fontSize: 15, color: 'var(--text-sec)' }}>
              Don't have an account?{' '}
              <Link to="/register" style={{ color: '#a78bfa', fontWeight: 700, textDecoration: 'none' }}>Create one free →</Link>
            </p>
            <Link to="/" style={{ display: 'block', marginTop: 14, fontSize: 13, color: 'var(--text-sec)', textDecoration: 'none', opacity: 0.7 }}>
              ← Back to homepage
            </Link>
          </div>
        </motion.div>
      </div>

      {/* ── Forgot Password Modal ── */}
      <AnimatePresence>
        {showForgotModal && (
          <div className="modal-overlay" style={{
            position: 'fixed', inset: 0, background: 'rgba(6,8,24,0.88)', backdropFilter: 'blur(8px)', zIndex: 999, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 24
          }} onClick={() => setShowForgotModal(false)}>
            <motion.div
              initial={{ scale: 0.95, opacity: 0, y: 16 }}
              animate={{ scale: 1, opacity: 1, y: 0 }}
              exit={{ scale: 0.95, opacity: 0, y: 16 }}
              style={{
                background: '#161b22', border: '1px solid rgba(255,255,255,0.08)', borderRadius: 24, padding: '40px 48px', maxWidth: 460, width: '100%', boxShadow: '0 24px 64px rgba(0,0,0,0.6)'
              }}
              onClick={e => e.stopPropagation()}
            >
              {/* Modal Header */}
              <div style={{ marginBottom: 28, textAlign: 'center' }}>
                <div style={{ width: 56, height: 56, borderRadius: 16, background: 'rgba(124,58,237,0.1)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 16px', border: '1px solid rgba(124,58,237,0.2)' }}>
                  <Lock size={24} color="#a78bfa" />
                </div>
                <h2 style={{ fontSize: 24, fontWeight: 800, color: '#fff', marginBottom: 8 }}>
                  {forgotStep === 1 ? 'Reset Password' : 'Verify Code'}
                </h2>
                <p style={{ fontSize: 13.5, color: '#94a3b8', lineHeight: 1.5 }}>
                  {forgotStep === 1 
                    ? 'Enter your email address and we will send you a 6-digit OTP code to verify your identity.' 
                    : `Enter the 6-digit code sent to ${forgotEmail} and choose your new password.`}
                </p>
              </div>

              {forgotStep === 1 ? (
                <form onSubmit={handleRequestOtp} style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
                  <div>
                    <label style={{ display: 'block', fontSize: 13.5, fontWeight: 600, color: '#e2e8f0', marginBottom: 8 }}>Email address</label>
                    <div style={{ position: 'relative' }}>
                      <Mail size={15} color="#6b7280" style={{ position: 'absolute', left: 16, top: '50%', transform: 'translateY(-50%)', pointerEvents: 'none' }} />
                      <input
                        type="email" required value={forgotEmail}
                        onChange={e => setForgotEmail(e.target.value)}
                        placeholder="you@example.com"
                        className="input-field"
                        style={{ paddingLeft: 46, fontSize: 14.5, height: 48 }}
                      />
                    </div>
                  </div>

                  <div style={{ display: 'flex', gap: 12, marginTop: 8 }}>
                    <button type="button" onClick={() => setShowForgotModal(false)}
                      style={{ flex: 1, padding: '12px', borderRadius: 10, background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)', color: '#94a3b8', fontSize: 14, fontWeight: 600, cursor: 'pointer' }}>
                      Cancel
                    </button>
                    <button type="submit" disabled={forgotLoading}
                      style={{ flex: 1, padding: '12px', borderRadius: 10, background: 'linear-gradient(135deg,#7c3aed,#4f46e5)', border: 'none', color: '#fff', fontSize: 14, fontWeight: 700, cursor: forgotLoading ? 'not-allowed' : 'pointer', opacity: forgotLoading ? 0.7 : 1 }}>
                      {forgotLoading ? 'Sending...' : 'Send OTP'}
                    </button>
                  </div>
                </form>
              ) : (
                <form onSubmit={handleResetPassword} style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
                  {/* OTP Code */}
                  <div>
                    <label style={{ display: 'block', fontSize: 13.5, fontWeight: 600, color: '#e2e8f0', marginBottom: 8 }}>Verification Code</label>
                    <input
                      type="text" required maxLength={6} pattern="\d{6}" value={otpCode}
                      onChange={e => setOtpCode(e.target.value.replace(/\D/g, ''))}
                      placeholder="Enter 6-digit OTP"
                      className="input-field"
                      style={{ fontSize: 16, textAlign: 'center', letterSpacing: '0.2em', height: 48, fontWeight: 700 }}
                    />
                  </div>

                  {/* New Password */}
                  <div>
                    <label style={{ display: 'block', fontSize: 13.5, fontWeight: 600, color: '#e2e8f0', marginBottom: 8 }}>New Password</label>
                    <input
                      type="password" required value={newPassword}
                      onChange={e => setNewPassword(e.target.value)}
                      placeholder="Enter new password"
                      className="input-field"
                      style={{ fontSize: 14.5, height: 48 }}
                    />
                  </div>

                  <div style={{ display: 'flex', gap: 12, marginTop: 8 }}>
                    <button type="button" onClick={() => setForgotStep(1)}
                      style={{ flex: 1, padding: '12px', borderRadius: 10, background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)', color: '#94a3b8', fontSize: 14, fontWeight: 600, cursor: 'pointer' }}>
                      Back
                    </button>
                    <button type="submit" disabled={forgotLoading}
                      style={{ flex: 1, padding: '12px', borderRadius: 10, background: 'linear-gradient(135deg,#7c3aed,#4f46e5)', border: 'none', color: '#fff', fontSize: 14, fontWeight: 700, cursor: forgotLoading ? 'not-allowed' : 'pointer', opacity: forgotLoading ? 0.7 : 1 }}>
                      {forgotLoading ? 'Resetting...' : 'Reset Password'}
                    </button>
                  </div>
                </form>
              )}
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  )
}
