import { motion } from 'framer-motion'
import { useState, useEffect } from 'react'
import Sidebar from './Sidebar'

export default function Layout({ children, title, subtitle, action }) {
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
    if (window.innerWidth <= 768) {
      setIsSidebarOpen(false)
    }
  }, [])

  return (
    <div className="page-bg" style={{ minHeight: '100vh' }}>
      <Sidebar isOpen={isSidebarOpen} toggle={toggleSidebar} />
      {isSidebarOpen && <div className="sidebar-overlay md:hidden" onClick={toggleSidebar}></div>}
      <div className={`main-content ${isSidebarOpen ? '' : 'sidebar-closed'}`}>
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
              {/* theme toggle removed per request */}
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
