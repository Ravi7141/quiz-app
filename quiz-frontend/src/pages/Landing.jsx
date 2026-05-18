import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { Zap, BookOpen, Users, Target, Trophy, ArrowRight, Sun, Moon, ShieldCheck, Star, Brain } from 'lucide-react'

const anim = (delay = 0) => ({
  initial: { opacity: 0, y: 24 },
  whileInView: { opacity: 1, y: 0 },
  viewport: { once: true },
  transition: { duration: 0.55, delay, ease: 'easeOut' },
})

const FEATURES = [
  {
    icon: BookOpen,
    color: '#7c3aed',
    title: 'Exam Builder',
    desc: 'Design secure, timed assessments with mixed question types, scoring rules, and review settings.',
  },
  {
    icon: Users,
    color: '#38bdf8',
    title: 'Student Management',
    desc: 'Group learners by class, send invites in bulk, and manage who can access each exam.',
  },
  {
    icon: Target,
    color: '#22c55e',
    title: 'Secure Access',
    desc: 'Use tokenized links, timed windows, and exam passcodes to keep quiz sessions controlled.',
  },
  {
    icon: Trophy,
    color: '#fbbf24',
    title: 'Analytics & Reports',
    desc: 'Visualize student performance across exams, classes, and questions with export-ready reports.',
  },
]

const STATS = [
  { value: '10K+', label: 'Exams Created' },
  { value: '80K+', label: 'Students' },
  { value: '220K+', label: 'Questions Bank' },
  { value: '99%', label: 'Teacher Adoption' },
]

const STEPS = [
  { icon: Target, num: '01', title: 'Register & Configure', desc: 'Create your account and set up classes, subjects, and exam rules.' },
  { icon: BookOpen, num: '02', title: 'Build Exams Fast', desc: 'Compose MCQs or coding tests, schedule time windows, and review before launch.' },
  { icon: Trophy, num: '03', title: 'Launch & Review', desc: 'Send secure student links, collect submissions, and analyze results immediately.' },
]

export default function Landing() {
  const [theme, setTheme] = useState(() => localStorage.getItem('quiz_theme') || 'dark')

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme)
    localStorage.setItem('quiz_theme', theme)
  }, [theme])

  const toggleTheme = () => setTheme(t => (t === 'dark' ? 'light' : 'dark'))

  return (
    <div style={{ background: 'var(--bg-main)', minHeight: '100vh', color: 'var(--text-main)' }}>
      <nav className="landing-nav fade-in">
        <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: 10, textDecoration: 'none' }}>
          <div style={{ width: 36, height: 36, borderRadius: 10, background: 'linear-gradient(135deg,#7c3aed,#4f46e5)', display: 'flex', alignItems: 'center', justifyContent: 'center', boxShadow: '0 0 16px rgba(124,58,237,0.5)' }}>
            <Zap size={18} color="#fff" />
          </div>
          <span style={{ fontWeight: 800, fontSize: 18, letterSpacing: '-0.02em' }} className="grad">QuizVault</span>
        </Link>

        <div style={{ display: 'flex', alignItems: 'center', gap: 18, flexWrap: 'wrap' }}>
          <a href="#features" className="nav-link">Features</a>
          <a href="#" className="nav-link">Analytics</a>
          <button onClick={toggleTheme} title={theme === 'dark' ? 'Switch to Light Mode' : 'Switch to Dark Mode'} className="theme-toggle">
            {theme === 'dark' ? <Sun size={18} color="#fbbf24" /> : <Moon size={18} color="#7c3aed" />}
          </button>
          <Link to="/login" className="btn-ghost" style={{ padding: '10px 20px', fontSize: 14 }}>Login</Link>
          <Link to="/register" className="btn-primary" style={{ padding: '10px 22px', fontSize: 14 }}>Sign Up</Link>
        </div>
      </nav>

      <section className="hero-bg">
        <div className="hero-wrapper">
          <motion.div {...anim(0.1)} className="hero-copy">
            <div className="hero-badge">
              <Zap size={14} color="currentColor" /> Secure exam delivery for modern classrooms
            </div>
            <h1>
              Create exams and review results.
            </h1>
            <p>
              QuizVault gives educators a polished space to build secure assessments and track student performance.
            </p>
            <div className="hero-actions">
              <Link to="/register" className="btn-primary" style={{ fontSize: 16 }}>Start Free</Link>
              <a href="#features" className="btn-ghost" style={{ fontSize: 16 }}>View Features</a>
            </div>
          </motion.div>

          <motion.div {...anim(0.2)} className="hero-visual">
            <div className="hero-visual-card hero-visual-card-large glass">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 28 }}>
                <span className="pill">Exam Dashboard</span>
                <span style={{ color: 'var(--text-main)', fontSize: 12 }}>Live</span>
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 18, marginBottom: 24 }}>
                <div className="info-box">
                  <div style={{ fontSize: 12, color: 'var(--text-sec)', textTransform: 'uppercase', letterSpacing: '0.18em' }}>Duration</div>
                  <div style={{ fontSize: 28, fontWeight: 800 }}>45m</div>
                </div>
                <div className="info-box">
                  <div style={{ fontSize: 12, color: 'var(--text-sec)', textTransform: 'uppercase', letterSpacing: '0.18em' }}>Questions</div>
                  <div style={{ fontSize: 28, fontWeight: 800 }}>32</div>
                </div>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', gap: 18, alignItems: 'center' }}>
                <div>
                  <div style={{ fontSize: 12, color: 'var(--text-sec)', marginBottom: 8 }}>Invited students</div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                    <div className="avatar-dot avatar-purple" />
                    <div className="avatar-dot avatar-blue" />
                    <div className="avatar-dot avatar-green" />
                    <span style={{ color: 'var(--text-main)', fontWeight: 700 }}>+24</span>
                  </div>
                </div>
                <div className="pill pill-success">96% Pass</div>
              </div>
            </div>

            <div className="hero-visual-stack">
              <div className="hero-visual-card glass">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
                  <span style={{ fontWeight: 700 }}>Live Analytics</span>
                  <span style={{ fontSize: 12, color: 'var(--text-main)' }}>Today</span>
                </div>
                <div className="analytics-grid">
                  <div className="mini-card">
                    <div style={{ color: 'var(--text-sec)', fontSize: 12, marginBottom: 8 }}>Avg. Score</div>
                    <div style={{ fontSize: 22, fontWeight: 800 }}>82</div>
                  </div>
                  <div className="mini-card">
                    <div style={{ color: 'var(--text-sec)', fontSize: 12, marginBottom: 8 }}>Completion</div>
                    <div style={{ fontSize: 22, fontWeight: 800 }}>78%</div>
                  </div>
                </div>
              </div>

              <div className="hero-visual-card glass">
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 12 }}>
                  <div>
                    <div style={{ fontSize: 12, color: 'var(--text-sec)', textTransform: 'uppercase', letterSpacing: '0.18em', marginBottom: 10 }}>Security</div>
                    <div style={{ fontSize: 22, fontWeight: 800, marginBottom: 4 }}>Tokenized</div>
                    <div style={{ fontSize: 13, color: 'var(--text-sec)' }}>Private access links and timed windows.</div>
                  </div>
                  <ShieldCheck size={28} color="#7c3aed" />
                </div>
              </div>
            </div>
          </motion.div>
        </div>
      </section>

      <section className="stats-preview-section">
        <div className="stats-preview-wrapper">
          <div className="stats-grid">
            {STATS.map(({ value, label }, index) => (
              <motion.div key={label} {...anim(0.15 + index * 0.05)} className="glass-card stat-card">
                <div style={{ fontSize: 24, fontWeight: 800, marginBottom: 6 }}>{value}</div>
                <div style={{ fontSize: 13, color: 'var(--text-sec)' }}>{label}</div>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      <section id="features" className="section" style={{ paddingTop: 80, paddingBottom: 64 }}>
        <div style={{ maxWidth: 1080, margin: '0 auto', padding: '0 24px' }}>
          <motion.div {...anim()} style={{ textAlign: 'center', marginBottom: 54 }}>
            <h2 style={{ fontSize: 'clamp(30px,4vw,42px)', fontWeight: 800, marginBottom: 14 }}>Powerful features for modern educators</h2>
            <p style={{ color: 'var(--text-sec)', fontSize: 16, maxWidth: 720, margin: '0 auto' }}>
              Empower your classroom with secure exam delivery, student management, and analytics built for fast, fair assessment.
            </p>
          </motion.div>

          <div className="feature-grid">
            {FEATURES.map(({ icon: Icon, color, title, desc }, i) => (
              <motion.div key={title} {...anim(0.12 + i * 0.08)} className="feature-card">
                <div style={{ width: 48, height: 48, display: 'inline-flex', alignItems: 'center', justifyContent: 'center', borderRadius: 16, background: 'rgba(255,255,255,0.06)', marginBottom: 18 }}>
                  <Icon size={22} color={color} />
                </div>
                <h3 style={{ fontSize: 20, fontWeight: 700, marginBottom: 10, color: 'var(--text-main)' }}>{title}</h3>
                <p style={{ color: 'var(--text-sec)', lineHeight: 1.75 }}>{desc}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      <section className="cta-banner">
        <div style={{ maxWidth: 900, margin: '0 auto' }}>
          <h2 style={{ fontSize: 'clamp(32px,3.5vw,44px)', fontWeight: 800, marginBottom: 14 }}>Ready to launch your first exam?</h2>
          <p style={{ fontSize: 17, color: 'rgba(255,255,255,0.9)', lineHeight: 1.8, maxWidth: 700, margin: '0 auto 28px' }}>
            Start building secure assessments, invite students with private links, and review results instantly from a polished teacher dashboard.
          </p>
          <Link to="/register" className="btn-primary" style={{ fontSize: 16, padding: '14px 34px' }}>
            Create Account Now
          </Link>
        </div>
      </section>

      <footer className="landing-footer">
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <div style={{ width: 34, height: 34, borderRadius: 12, background: 'linear-gradient(135deg,#7c3aed,#4f46e5)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Zap size={16} color="#fff" />
          </div>
          <div>
            <div style={{ fontWeight: 800, color: 'var(--text-main)' }}>QuizVault</div>
            <div style={{ fontSize: 13, color: 'var(--text-sec)' }}>Secure exams for every classroom.</div>
          </div>
        </div>

        <div className="footer-links">
          <a href="#features">Features</a>
          <a href="#">Pricing</a>
          <a href="#">Terms</a>
          <a href="#">Contact</a>
        </div>
      </footer>
    </div>
  )
}
