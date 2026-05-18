import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { authApi } from '../../api/axios'
import { useAuth } from '../../context/AuthContext'
import toast from 'react-hot-toast'
import { Zap, Mail, Lock, Eye, EyeOff, ArrowRight, Loader2 } from 'lucide-react'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', password: '' })
  const [loading, setLoading] = useState(false)
  const [showPw, setShowPw] = useState(false)

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
          <div style={{ width: 88, height: 88, borderRadius: 24, background: 'linear-gradient(135deg,#7c3aed,#4f46e5)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 28px', boxShadow: '0 0 48px rgba(124,58,237,0.5)' }} className="float-anim">
            <Zap size={40} color="#fff" />
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
              <label style={{ display: 'block', fontSize: 14, fontWeight: 600, color: 'var(--text-main)', marginBottom: 10 }}>Password</label>
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
    </div>
  )
}
