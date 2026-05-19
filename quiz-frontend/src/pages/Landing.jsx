import { useState, useEffect, useRef } from 'react'
import { Link } from 'react-router-dom'
import logo from '../assets/logo.png'
import { motion, useScroll, useTransform, useInView, AnimatePresence } from 'framer-motion'
import { Zap, BookOpen, Users, Target, Trophy, ArrowRight, Sun, Moon, ShieldCheck, Star, Brain, CheckCircle, BarChart3, Clock, GraduationCap, Sparkles, Layers, ChevronRight, Mail, MapPin, Phone, Globe, Camera, Play, MessageCircle, Share2 } from 'lucide-react'
import ParticleBackground from '../components/ParticleBackground'

const fadeUp = (delay = 0) => ({
  initial: { opacity: 0, y: 40 },
  whileInView: { opacity: 1, y: 0 },
  viewport: { once: true, margin: '-50px' },
  transition: { duration: 0.6, delay, ease: [0.25, 0.4, 0.25, 1] },
})

const stagger = (staggerDelay = 0.08) => ({
  initial: { opacity: 0, y: 30 },
  whileInView: { opacity: 1, y: 0 },
  viewport: { once: true, margin: '-30px' },
  transition: { duration: 0.5, ease: [0.25, 0.4, 0.25, 1] },
})

const scaleIn = (delay = 0) => ({
  initial: { opacity: 0, scale: 0.85 },
  whileInView: { opacity: 1, scale: 1 },
  viewport: { once: true },
  transition: { duration: 0.5, delay, ease: [0.25, 0.4, 0.25, 1] },
})

const FEATURES = [
  {
    icon: BookOpen, color: '#7c3aed',
    title: 'Exam Builder',
    desc: 'Design secure, timed assessments with mixed question types, scoring rules, and review settings.',
  },
  {
    icon: Users, color: '#38bdf8',
    title: 'Student Management',
    desc: 'Group learners by class, send invites in bulk, and manage who can access each exam.',
  },
  {
    icon: Target, color: '#22c55e',
    title: 'Secure Access',
    desc: 'Use tokenized links, timed windows, and exam passcodes to keep quiz sessions controlled.',
  },
  {
    icon: Trophy, color: '#fbbf24',
    title: 'Analytics & Reports',
    desc: 'Visualize student performance across exams, classes, and questions with export-ready reports.',
  },
]

const STATS = [
  { value: '10K+', label: 'Exams Created', suffix: 'exams' },
  { value: '80K+', label: 'Students', suffix: 'learners' },
  { value: '220K+', label: 'Questions Bank', suffix: 'questions' },
  { value: '99%', label: 'Teacher Adoption', suffix: 'satisfaction' },
]

const STEPS = [
  { icon: GraduationCap, title: 'Register & Configure', desc: 'Create your account and set up classes, subjects, and exam rules.', gradient: 'from-violet-500 to-purple-600' },
  { icon: Layers, title: 'Build Exams Fast', desc: 'Compose MCQs or coding tests, schedule time windows, and review before launch.', gradient: 'from-blue-500 to-cyan-500' },
  { icon: Trophy, title: 'Launch & Review', desc: 'Send secure student links, collect submissions, and analyze results immediately.', gradient: 'from-emerald-500 to-teal-500' },
]

function FloatingIllustration({ theme }) {
  const isLight = theme === 'light'
  const cardBg = isLight ? 'rgba(255,255,255,0.6)' : 'rgba(255,255,255,0.06)'
  const cardBorder = isLight ? 'rgba(148,163,184,0.2)' : 'rgba(255,255,255,0.1)'
  const textMuted = isLight ? 'rgba(100,116,139,0.7)' : 'rgba(255,255,255,0.35)'
  const textMain = isLight ? '#1e293b' : '#e2e8f0'
  const barTrack = isLight ? 'rgba(148,163,184,0.2)' : 'rgba(255,255,255,0.08)'
  const statCardBg = isLight ? 'rgba(248,250,252,0.8)' : 'rgba(255,255,255,0.05)'
  const statCardBorder = isLight ? 'rgba(148,163,184,0.15)' : 'rgba(255,255,255,0.06)'
  const dotColor = isLight ? 'rgba(124,58,237,0.3)' : 'rgba(124,58,237,0.4)'
  const dotColor2 = isLight ? 'rgba(56,189,248,0.3)' : 'rgba(56,189,248,0.5)'

  return (
    <div className="relative w-full h-full flex items-center justify-center">
      <svg viewBox="0 0 500 420" fill="none" xmlns="http://www.w3.org/2000/svg" className="w-full h-auto max-w-[500px]">
        <defs>
          <linearGradient id="purpleGlow" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stopColor="#7c3aed" stopOpacity="0.3" />
            <stop offset="100%" stopColor="#4f46e5" stopOpacity="0.1" />
          </linearGradient>
          <linearGradient id="blueGlow" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stopColor="#38bdf8" stopOpacity="0.25" />
            <stop offset="100%" stopColor="#0ea5e9" stopOpacity="0.08" />
          </linearGradient>
          <filter id="glow">
            <feGaussianBlur stdDeviation="6" result="blur" />
            <feMerge><feMergeNode in="blur" /><feMergeNode in="SourceGraphic" /></feMerge>
          </filter>
          <filter id="softGlow">
            <feGaussianBlur stdDeviation="3" result="blur" />
            <feMerge><feMergeNode in="blur" /><feMergeNode in="SourceGraphic" /></feMerge>
          </filter>
        </defs>

        {/* Background gradient orbs */}
        <motion.ellipse cx="250" cy="210" rx="220" ry="190" fill="url(#purpleGlow)" animate={{ opacity: [0.5, 0.8, 0.5], scale: [1, 1.05, 1] }} transition={{ duration: 6, repeat: Infinity, ease: 'easeInOut' }} />
        <motion.ellipse cx="300" cy="180" rx="160" ry="140" fill="url(#blueGlow)" animate={{ opacity: [0.3, 0.6, 0.3], scale: [1, 1.08, 1] }} transition={{ duration: 8, repeat: Infinity, ease: 'easeInOut', delay: 1 }} />

        {/* Main dashboard card */}
        <motion.g initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.8, delay: 0.2 }}>
          <rect x="60" y="100" width="380" height="260" rx="24" fill={cardBg} stroke={cardBorder} strokeWidth="1" />
          <rect x="60" y="100" width="380" height="260" rx="24" fill="url(#purpleGlow)" />
        </motion.g>

        {/* Card header */}
        <motion.g initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ duration: 0.5, delay: 0.4 }}>
          <rect x="88" y="128" width="100" height="10" rx="5" fill="rgba(124,58,237,0.6)" />
          <rect x="88" y="148" width="60" height="8" rx="4" fill={textMuted} />
          <rect x="370" y="128" width="42" height="24" rx="12" fill="rgba(56,189,248,0.2)" stroke="rgba(56,189,248,0.3)" strokeWidth="0.5" />
        </motion.g>

        {/* Bar chart */}
        <motion.g initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ duration: 0.5, delay: 0.5 }}>
          <rect x="88" y="190" width="120" height="8" rx="4" fill={barTrack} />
          <motion.rect x="88" y="190" width="90" height="8" rx="4" fill="#7c3aed" initial={{ width: 0 }} animate={{ width: 90 }} transition={{ duration: 1, delay: 0.8, ease: 'easeOut' }} />
          <rect x="88" y="208" width="120" height="8" rx="4" fill={barTrack} />
          <motion.rect x="88" y="208" width="70" height="8" rx="4" fill="#38bdf8" initial={{ width: 0 }} animate={{ width: 70 }} transition={{ duration: 1, delay: 1, ease: 'easeOut' }} />
          <rect x="88" y="226" width="120" height="8" rx="4" fill={barTrack} />
          <motion.rect x="88" y="226" width="105" height="8" rx="4" fill="#22c55e" initial={{ width: 0 }} animate={{ width: 105 }} transition={{ duration: 1, delay: 1.2, ease: 'easeOut' }} />
          <rect x="88" y="244" width="120" height="8" rx="4" fill={barTrack} />
          <motion.rect x="88" y="244" width="55" height="8" rx="4" fill="#fbbf24" initial={{ width: 0 }} animate={{ width: 55 }} transition={{ duration: 1, delay: 1.4, ease: 'easeOut' }} />
        </motion.g>

        {/* Pie chart on right side */}
        <motion.g initial={{ opacity: 0, scale: 0.8 }} animate={{ opacity: 1, scale: 1 }} transition={{ duration: 0.6, delay: 0.7 }}>
          <circle cx="350" cy="230" r="45" fill={isLight ? 'rgba(148,163,184,0.08)' : 'rgba(255,255,255,0.03)'} stroke={isLight ? 'rgba(148,163,184,0.15)' : 'rgba(255,255,255,0.08)'} strokeWidth="1" />
          <motion.circle cx="350" cy="230" r="45" fill="none" stroke="#7c3aed" strokeWidth="10" strokeDasharray="160 123" strokeDashoffset="0" strokeLinecap="round" initial={{ strokeDashoffset: 283 }} animate={{ strokeDashoffset: 123 }} transition={{ duration: 1.2, delay: 1 }} transform="rotate(-90 350 230)" />
          <motion.circle cx="350" cy="230" r="45" fill="none" stroke="#38bdf8" strokeWidth="10" strokeDasharray="80 203" strokeDashoffset="0" strokeLinecap="round" initial={{ strokeDashoffset: 283 }} animate={{ strokeDashoffset: 203 }} transition={{ duration: 1.2, delay: 1.3 }} transform="rotate(100 350 230)" />
        </motion.g>

        {/* Checkmark circle */}
        <motion.g initial={{ opacity: 0, scale: 0 }} animate={{ opacity: 1, scale: 1 }} transition={{ duration: 0.5, delay: 1.5, type: 'spring', stiffness: 200 }}>
          <circle cx="350" cy="230" r="16" fill="rgba(34,197,94,0.15)" stroke="rgba(34,197,94,0.4)" strokeWidth="1" />
          <path d="M343 230l5 5 9-9" stroke="#22c55e" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" fill="none" />
        </motion.g>

        {/* Bottom stat cards */}
        <motion.g initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5, delay: 0.9 }}>
          <rect x="88" y="280" width="90" height="50" rx="12" fill={statCardBg} stroke={statCardBorder} strokeWidth="0.5" />
          <text x="133" y="302" textAnchor="middle" fill="#a78bfa" fontSize="16" fontWeight="bold">82%</text>
          <text x="133" y="318" textAnchor="middle" fill={textMuted} fontSize="10">Avg. Score</text>
        </motion.g>
        <motion.g initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5, delay: 1.1 }}>
          <rect x="193" y="280" width="90" height="50" rx="12" fill={statCardBg} stroke={statCardBorder} strokeWidth="0.5" />
          <text x="238" y="302" textAnchor="middle" fill="#38bdf8" fontSize="16" fontWeight="bold">96%</text>
          <text x="238" y="318" textAnchor="middle" fill={textMuted} fontSize="10">Pass Rate</text>
        </motion.g>
        <motion.g initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5, delay: 1.3 }}>
          <rect x="298" y="280" width="90" height="50" rx="12" fill={statCardBg} stroke={statCardBorder} strokeWidth="0.5" />
          <text x="343" y="302" textAnchor="middle" fill="#22c55e" fontSize="16" fontWeight="bold">78%</text>
          <text x="343" y="318" textAnchor="middle" fill={textMuted} fontSize="10">Completion</text>
        </motion.g>

        {/* Floating elements */}
        <motion.g animate={{ y: [0, -8, 0], rotate: [0, 3, 0] }} transition={{ duration: 4, repeat: Infinity, ease: 'easeInOut' }}>
          <circle cx="72" cy="90" r="8" fill={dotColor} filter="url(#glow)" />
          <rect x="66" y="84" width="12" height="12" rx="3" fill="rgba(124,58,237,0.6)" />
        </motion.g>
        <motion.g animate={{ y: [0, -12, 0], x: [0, 6, 0] }} transition={{ duration: 5, repeat: Infinity, ease: 'easeInOut', delay: 1 }}>
          <circle cx="440" cy="80" r="6" fill={dotColor2} filter="url(#glow)" />
        </motion.g>
        <motion.g animate={{ y: [0, -6, 0], rotate: [0, -2, 0] }} transition={{ duration: 3.5, repeat: Infinity, ease: 'easeInOut', delay: 0.5 }}>
          <rect x="410" cy="340" y="340" width="16" height="16" rx="4" fill="rgba(251,191,36,0.3)" />
        </motion.g>

        {/* Small floating dots */}
        {[...Array(6)].map((_, i) => (
          <motion.circle
            key={i}
            cx={60 + i * 80}
            cy={90}
            r={2}
            fill={i % 2 === 0 ? dotColor : dotColor2}
            animate={{ y: [0, -6 - i * 2, 0], opacity: [0.3, 0.8, 0.3] }}
            transition={{ duration: 3 + i * 0.5, repeat: Infinity, ease: 'easeInOut', delay: i * 0.3 }}
          />
        ))}
      </svg>
    </div>
  )
}

function Counter({ value, label }) {
  const ref = useRef(null)
  const isInView = useInView(ref, { once: true })
  const [count, setCount] = useState(0)
  const num = parseInt(value)

  useEffect(() => {
    if (!isInView) return
    let start = 0
    const duration = 2000
    const step = Math.ceil(num / (duration / 16))
    const timer = setInterval(() => {
      start += step
      if (start >= num) {
        setCount(num)
        clearInterval(timer)
      } else {
        setCount(start)
      }
    }, 16)
    return () => clearInterval(timer)
  }, [isInView, num])

  return (
    <div ref={ref} className="stat-number">
      <span className="stat-value">{value.includes('+') ? `${count}+` : value.includes('%') ? `${count}%` : count}</span>
      <span className="stat-label">{label}</span>
    </div>
  )
}

export default function Landing() {
  const [theme, setTheme] = useState(() => localStorage.getItem('quiz_theme') || 'dark')
  const [mounted, setMounted] = useState(false)
  const { scrollYProgress } = useScroll()
  const heroOpacity = useTransform(scrollYProgress, [0, 0.12], [1, 0])

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme)
    localStorage.setItem('quiz_theme', theme)
    setMounted(true)
  }, [theme])

  const toggleTheme = () => setTheme(t => (t === 'dark' ? 'light' : 'dark'))

  if (!mounted) return null

  return (
    <div style={{ background: 'var(--bg-main)', minHeight: '100vh', color: 'var(--text-main)', overflow: 'hidden' }}>
      <ParticleBackground />

      {/* Navbar */}
      <motion.nav
        className="landing-nav"
        initial={{ y: -80, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ duration: 0.6, ease: [0.25, 0.4, 0.25, 1] }}
      >
        <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: 10, textDecoration: 'none' }}>
          <motion.img
            src={logo}
            alt="QuizVault Logo"
            style={{ width: 36, height: 36, borderRadius: 10, objectFit: 'contain' }}
            whileHover={{ scale: 1.1, rotate: -5 }}
            whileTap={{ scale: 0.95 }}
          />
          <span style={{ fontWeight: 800, fontSize: 18, letterSpacing: '-0.02em' }} className="grad">QuizVault</span>
        </Link>

        <div style={{ display: 'flex', alignItems: 'center', gap: 18, flexWrap: 'wrap' }}>
          <motion.a href="#features" className="nav-link" whileHover={{ scale: 1.05 }}>Features</motion.a>
          <motion.a href="#how-it-works" className="nav-link" whileHover={{ scale: 1.05 }}>How It Works</motion.a>
          <motion.button onClick={toggleTheme} title={theme === 'dark' ? 'Switch to Light Mode' : 'Switch to Dark Mode'} className="theme-toggle" whileHover={{ scale: 1.1 }} whileTap={{ scale: 0.9 }}>
            <motion.div key={theme} initial={{ rotate: -90, opacity: 0 }} animate={{ rotate: 0, opacity: 1 }} transition={{ duration: 0.3 }}>
              {theme === 'dark' ? <Sun size={18} color="#fbbf24" /> : <Moon size={18} color="#7c3aed" />}
            </motion.div>
          </motion.button>
          <Link to="/login" className="btn-ghost" style={{ padding: '10px 20px', fontSize: 14 }}>Login</Link>
          <Link to="/register" className="btn-primary" style={{ padding: '10px 22px', fontSize: 14 }}>Sign Up</Link>
        </div>
      </motion.nav>

      {/* Hero Section */}
      <motion.section className="hero-bg" style={{ opacity: heroOpacity }}>
        <div className="hero-wrapper">
          <div className="hero-copy">
            <motion.div
              initial={{ opacity: 0, x: -30 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.6, delay: 0.2 }}
              className="hero-badge"
            >
              <Sparkles size={14} color="#a78bfa" />
              <span>Secure exam delivery for modern classrooms</span>
            </motion.div>

            <motion.h1
              initial={{ opacity: 0, y: 30 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.35 }}
            >
              Create exams and{' '}
              <span className="grad" style={{ display: 'inline-block' }}>
                review results
              </span>
              <motion.span
                className="inline-block ml-1"
                animate={{ opacity: [1, 0] }}
                transition={{ duration: 0.8, repeat: Infinity, repeatType: 'reverse' }}
              >
                .
              </motion.span>
            </motion.h1>

            <motion.p
              initial={{ opacity: 0, y: 30 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.5 }}
            >
              QuizVault gives educators a polished space to build secure assessments and track student performance with real-time analytics.
            </motion.p>

            <motion.div
              className="hero-actions"
              initial={{ opacity: 0, y: 30 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.65 }}
            >
              <motion.div whileHover={{ scale: 1.04 }} whileTap={{ scale: 0.96 }}>
                <Link to="/register" className="btn-primary" style={{ fontSize: 16, padding: '14px 32px' }}>
                  Start Free
                  <ArrowRight size={18} />
                </Link>
              </motion.div>
              <motion.a href="#features" className="btn-ghost" style={{ fontSize: 16 }} whileHover={{ scale: 1.04 }} whileTap={{ scale: 0.96 }}>
                View Features
              </motion.a>
            </motion.div>

            {/* Trust indicators */}
            <motion.div
              className="hero-trust"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.8 }}
            >
              <div className="trust-avatars">
                <div className="trust-avatar" style={{ background: '#7c3aed' }} />
                <div className="trust-avatar" style={{ background: '#38bdf8', marginLeft: -8 }} />
                <div className="trust-avatar" style={{ background: '#22c55e', marginLeft: -8 }} />
                <div className="trust-avatar" style={{ background: '#fbbf24', marginLeft: -8 }} />
              </div>
              <span className="trust-text">
                <strong>2,400+</strong> educators trust QuizVault
              </span>
            </motion.div>
          </div>

          <motion.div
            className="hero-visual-svg"
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.8, delay: 0.4, ease: [0.25, 0.4, 0.25, 1] }}
          >
            <FloatingIllustration theme={theme} />
          </motion.div>
        </div>
      </motion.section>

      {/* Stats Section */}
      <section className="stats-section">
        <div className="stats-container">
          <motion.div className="stats-grid-new" {...fadeUp()}>
            {STATS.map((stat, i) => (
              <motion.div key={stat.label} className="stat-card-new" {...scaleIn(0.1 + i * 0.08)}>
                <Counter value={stat.value} label={stat.label} />
              </motion.div>
            ))}
          </motion.div>
        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="section">
        <div className="section-container">
          <motion.div {...fadeUp()} style={{ textAlign: 'center', marginBottom: 60 }}>
            <motion.div
              className="section-badge"
              initial={{ opacity: 0, scale: 0.8 }}
              whileInView={{ opacity: 1, scale: 1 }}
              viewport={{ once: true }}
            >
              <Zap size={14} /> Powerful Features
            </motion.div>
            <h2 className="section-title">Everything you need to manage exams</h2>
            <p className="section-desc">
              Empower your classroom with secure exam delivery, student management, and analytics built for fast, fair assessment.
            </p>
          </motion.div>

          <div className="feature-grid-new">
            {FEATURES.map(({ icon: Icon, color, title, desc }, i) => (
              <motion.div
                key={title}
                className="feature-card-new"
                {...stagger(0.1 + i * 0.1)}
                whileHover={{ y: -8, transition: { duration: 0.3 } }}
              >
                <motion.div
                  className="feature-icon-wrap"
                  style={{ background: `${color}15`, borderColor: `${color}30` }}
                  whileHover={{ scale: 1.1, rotate: -5 }}
                >
                  <Icon size={24} color={color} />
                </motion.div>
                <h3 className="feature-title">{title}</h3>
                <p className="feature-desc">{desc}</p>
                <div className="feature-link">
                  <span>Learn more</span>
                  <ChevronRight size={14} />
                </div>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* How It Works */}
      <section id="how-it-works" className="section" style={{ background: 'var(--glass-bg)' }}>
        <div className="section-container">
          <motion.div {...fadeUp()} style={{ textAlign: 'center', marginBottom: 60 }}>
            <motion.div
              className="section-badge"
              initial={{ opacity: 0, scale: 0.8 }}
              whileInView={{ opacity: 1, scale: 1 }}
              viewport={{ once: true }}
            >
              <BarChart3 size={14} /> How It Works
            </motion.div>
            <h2 className="section-title">Get started in three simple steps</h2>
            <p className="section-desc">
              From setup to results, QuizVault streamlines the entire exam lifecycle.
            </p>
          </motion.div>

          <div className="steps-grid">
            {STEPS.map(({ icon: Icon, title, desc, gradient }, i) => (
              <motion.div
                key={title}
                className="step-card"
                {...fadeUp(0.15 + i * 0.12)}
              >
                <div className="step-number">{`0${i + 1}`}</div>
                <motion.div
                  className={`step-icon-wrap bg-gradient-to-br ${gradient}`}
                  whileHover={{ scale: 1.1, rotate: 360 }}
                  transition={{ duration: 0.6 }}
                >
                  <Icon size={24} color="#fff" />
                </motion.div>
                <h3 className="step-title">{title}</h3>
                <p className="step-desc">{desc}</p>
                {i < 2 && (
                  <div className="step-connector">
                    <motion.div
                      animate={{ y: [0, 4, 0] }}
                      transition={{ duration: 1.5, repeat: Infinity, ease: 'easeInOut' }}
                    >
                      <ArrowRight size={20} color="#7c3aed" />
                    </motion.div>
                  </div>
                )}
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Banner */}
      <section className="cta-section">
        <motion.div
          className="cta-banner-new"
          initial={{ opacity: 0, y: 40 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true, margin: '-80px' }}
          transition={{ duration: 0.6, ease: [0.25, 0.4, 0.25, 1] }}
        >
          <div className="cta-glow" />
          <motion.div
            className="cta-content"
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.5, delay: 0.2 }}
          >
            <motion.div
              className="cta-badge"
              initial={{ opacity: 0, scale: 0.8 }}
              whileInView={{ opacity: 1, scale: 1 }}
              viewport={{ once: true }}
              transition={{ delay: 0.3 }}
            >
              <Sparkles size={14} /> Get Started Today
            </motion.div>
            <h2 className="cta-title">Ready to launch your first exam?</h2>
            <p className="cta-desc">
              Start building secure assessments, invite students with private links, and review results instantly from a polished teacher dashboard.
            </p>
            <motion.div whileHover={{ scale: 1.05 }} whileTap={{ scale: 0.95 }}>
              <Link to="/register" className="btn-primary cta-btn">
                Create Account Now
                <ArrowRight size={20} />
              </Link>
            </motion.div>
          </motion.div>
        </motion.div>
      </section>

      {/* Footer */}
      <footer className="site-footer">
        <div className="footer-main">
          <div className="footer-grid">
            {/* Brand Column */}
            <div className="footer-col footer-brand-col">
              <motion.div
                className="footer-logo"
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
              >
                <img src={logo} alt="QuizVault Logo" style={{ width: 42, height: 42, borderRadius: 12, objectFit: 'contain' }} />
                <span className="footer-logo-text grad">QuizVault</span>
              </motion.div>
              <p className="footer-brand-desc">
                Create, share, and track results easily with a secure online quiz and test maker platform.
              </p>
              <div className="footer-contact">
                <div className="footer-contact-item">
                  <Mail size={14} />
                  <span>support@quizvault.com</span>
                </div>
                <div className="footer-contact-item">
                  <MapPin size={14} />
                  <span>San Francisco, CA, USA</span>
                </div>
              </div>
              <div className="footer-rate">
                <span className="footer-rate-label">Rate Portal</span>
                <div className="footer-stars">
                  {[...Array(5)].map((_, i) => (
                    <motion.span
                      key={i}
                      className="footer-star"
                      initial={{ opacity: 0, scale: 0 }}
                      whileInView={{ opacity: 1, scale: 1 }}
                      viewport={{ once: true }}
                      transition={{ delay: i * 0.06 }}
                      whileHover={{ scale: 1.3, rotate: 10 }}
                    >
                      <Star size={16} color="#fbbf24" fill="#fbbf24" />
                    </motion.span>
                  ))}
                </div>
              </div>
            </div>

            {/* Quick Links */}
            <div className="footer-col">
              <h4 className="footer-heading">Quick Links</h4>
              <ul className="footer-list">
                {[
                  { label: 'Home', href: '/' },
                  { label: 'Features', href: '#features' },
                  { label: 'How It Works', href: '#how-it-works' },
                  { label: 'Pricing', href: '#' },
                  { label: 'FAQs', href: '#' },
                ].map((link) => (
                  <li key={link.label}>
                    <motion.a
                      href={link.href}
                      className="footer-link"
                      whileHover={{ x: 4 }}
                    >
                      {link.label}
                    </motion.a>
                  </li>
                ))}
              </ul>
            </div>

            {/* Support */}
            <div className="footer-col">
              <h4 className="footer-heading">Support</h4>
              <ul className="footer-list">
                {[
                  { label: 'Help Center', href: '#' },
                  { label: 'Video Tutorials', href: '#' },
                  { label: 'Blog', href: '#' },
                  { label: 'Contact Us', href: '#' },
                  { label: 'Portal Status', href: '#' },
                ].map((link) => (
                  <li key={link.label}>
                    <motion.a
                      href={link.href}
                      className="footer-link"
                      whileHover={{ x: 4 }}
                    >
                      {link.label}
                    </motion.a>
                  </li>
                ))}
              </ul>
            </div>

            {/* Legal & Social */}
            <div className="footer-col">
              <h4 className="footer-heading">Legal</h4>
              <ul className="footer-list">
                {[
                  { label: 'Privacy Policy', href: '#' },
                  { label: 'Terms & Conditions', href: '#' },
                  { label: 'Cookie Policy', href: '#' },
                  { label: 'Refund Policy', href: '#' },
                ].map((link) => (
                  <li key={link.label}>
                    <motion.a
                      href={link.href}
                      className="footer-link"
                      whileHover={{ x: 4 }}
                    >
                      {link.label}
                    </motion.a>
                  </li>
                ))}
              </ul>
              <div className="footer-social-wrap">
                <h4 className="footer-heading" style={{ marginTop: 24 }}>Follow Us</h4>
                <div className="footer-social">
                  {[
                    { icon: Globe, href: '#', color: '#1877F2' },
                    { icon: Camera, href: '#', color: '#E4405F' },
                    { icon: MessageCircle, href: '#', color: '#25D366' },
                    { icon: Share2, href: '#', color: '#0A66C2' },
                    { icon: Play, href: '#', color: '#FF0000' },
                  ].map(({ icon: Icon, href, color }) => (
                    <motion.a
                      key={href}
                      href={href}
                      className="footer-social-icon"
                      style={{ '--social-color': color }}
                      whileHover={{ y: -4, scale: 1.1 }}
                      whileTap={{ scale: 0.9 }}
                    >
                      <Icon size={16} />
                    </motion.a>
                  ))}
                </div>
              </div>
            </div>
          </div>

          {/* Bottom Bar */}
          <div className="footer-bottom">
            <div className="footer-bottom-inner">
              <div className="footer-legal-links">
                <a href="#">Privacy Policy</a>
                <span className="footer-sep">|</span>
                <a href="#">Terms & Conditions</a>
              </div>
              <p className="footer-copy">
                &copy; 2026 QuizVault. All rights reserved.
              </p>
            </div>
          </div>
        </div>
      </footer>
    </div>
  )
}
