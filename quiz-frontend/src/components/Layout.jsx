import { motion } from 'framer-motion'
import { useState, useEffect } from 'react'
import { Sun, Moon } from 'lucide-react'
import Sidebar from './Sidebar'

export default function Layout({ children, title, subtitle, action }) {
  const [theme, setTheme] = useState(localStorage.getItem('quiz_theme') || 'dark')
  const [isSidebarOpen, setIsSidebarOpen] = useState(() => {
    const saved = localStorage.getItem('quiz_sidebar')
    return saved === null ? true : saved === 'true'
  })

  const toggleSidebar = () => setIsSidebarOpen(prev => {
    const next = !prev
    localStorage.setItem('quiz_sidebar', String(next))
    return next
  })

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme)
    localStorage.setItem('quiz_theme', theme)
  }, [theme])

  const toggleTheme = () => setTheme(t => t === 'dark' ? 'light' : 'dark')

  return (
    <div className="page-bg" style={{ minHeight: '100vh' }}>
      <Sidebar isOpen={isSidebarOpen} toggle={toggleSidebar} />
      <div className="main-content" style={{ marginLeft: isSidebarOpen ? 240 : 0 }}>
        {/* Header */}
        <div className="page-header">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
              {!isSidebarOpen && (
                <button onClick={toggleSidebar} className="btn-ghost" style={{ padding: 6, minWidth: 32, minHeight: 32, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="3" y1="12" x2="21" y2="12"></line><line x1="3" y1="6" x2="21" y2="6"></line><line x1="3" y1="18" x2="21" y2="18"></line></svg>
                </button>
              )}
              <div>
                {title && <h1 style={{ fontSize: 32, fontWeight: 900, color: 'var(--text-main)', letterSpacing: '-0.025em', lineHeight: 1.15 }}>{title}</h1>}
                {subtitle && <p style={{ fontSize: 15, color: 'var(--text-sec)', marginTop: 5, fontWeight: 500 }}>{subtitle}</p>}
              </div>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
              {action && <div>{action}</div>}
              <button 
                onClick={toggleTheme} 
                className="btn-ghost" 
                style={{ padding: '8px', borderRadius: '50%', minWidth: '40px', minHeight: '40px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                title="Toggle Theme"
              >
                {theme === 'dark' ? <Sun size={18} color="#fbbf24" /> : <Moon size={18} color="#7c3aed" />}
              </button>
            </div>
          </div>
        </div>
        {/* Body */}
        <motion.div
          style={{ padding: 32 }}
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.35 }}
        >
          {children}
        </motion.div>
      </div>
    </div>
  )
}
