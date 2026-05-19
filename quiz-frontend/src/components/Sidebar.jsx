import { Link, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import {
  LayoutDashboard, BookOpen, Code2, Users, BarChart2,
  LogOut, Zap, ChevronRight, User
} from 'lucide-react'

import logo from '../assets/logo.png'

const studentLinks = []
const adminLinks = [
  { to: '/admin/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/admin/quizzes', label: 'Manage Quizzes', icon: BookOpen },
  { to: '/admin/coding', label: 'Coding Tests', icon: Code2 },
  { to: '/admin/students', label: 'Students', icon: Users },
  { to: '/admin/results', label: 'Results', icon: BarChart2 },
]

export default function Sidebar({ isOpen, toggle }) {
  const { user, logout, isAdmin } = useAuth()
  const { pathname } = useLocation()
  const links = isAdmin ? adminLinks : studentLinks

  return (
    <div className="sidebar" style={{ transform: isOpen ? 'translateX(0)' : 'translateX(-100%)' }}>
      {/* Logo */}
      <div style={{ padding: '20px 20px 16px', borderBottom: '1px solid var(--glass-border)', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: 10, textDecoration: 'none' }}>
          <img src={logo} alt="QuizVault Logo" style={{ width: 32, height: 32, borderRadius: 8, objectFit: 'contain' }} />
          <span style={{ fontWeight: 800, fontSize: 20, letterSpacing: '-0.03em' }} className="grad">QuizVault</span>
        </Link>
        {/* Close Button */}
        <button onClick={toggle} style={{ background: 'none', border: 'none', color: 'var(--text-sec)', cursor: 'pointer' }}>
           <ChevronRight size={18} style={{ transform: 'rotate(180deg)' }} />
        </button>
      </div>

      {/* Nav — NO "NAVIGATION" label */}
      <nav style={{ flex: 1, padding: '16px 14px', overflowY: 'auto' }}>
        {links.map(({ to, label, icon: Icon }) => {
          const active = pathname === to || (to !== '/student/dashboard' && to !== '/admin/dashboard' && pathname.startsWith(to))
          return (
            <Link key={to} to={to} className={`nav-link ${active ? 'active' : ''}`}>
              <Icon size={17} />
              <span style={{ flex: 1, fontSize: 15, fontWeight: 600 }}>{label}</span>
              {active && <ChevronRight size={13} color="#7c3aed" />}
            </Link>
          )
        })}
      </nav>

      {/* User chip — clickable → profile */}
      <Link to="/profile" style={{ textDecoration: 'none', margin: '10px 12px', padding: '8px 10px', background: 'rgba(255,255,255,0.08)', border: '1px solid rgba(255,255,255,0.15)', borderRadius: 10, display: 'block', transition: 'background 0.2s' }}
        onMouseOver={e => e.currentTarget.style.background = 'rgba(124,58,237,0.35)'}
        onMouseOut={e => e.currentTarget.style.background = 'rgba(255,255,255,0.08)'}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <div style={{ width: 28, height: 28, borderRadius: 8, background: 'linear-gradient(135deg,#7c3aed,#38bdf8)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontWeight: 700, fontSize: 11, flexShrink: 0 }}>
            {user?.name?.[0]?.toUpperCase() || 'U'}
          </div>
          <div style={{ minWidth: 0, flex: 1 }}>
            <div style={{ fontSize: 12, fontWeight: 700, color: '#ffffff', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{user?.name || 'User'}</div>
            <span className={`badge ${isAdmin ? 'badge-admin' : 'badge-student'}`} style={{ marginTop: 2, fontSize: 10, padding: '2px 7px' }}>
              {user?.role}
            </span>
          </div>
          <User size={12} color="rgba(255,255,255,0.5)" />
        </div>
      </Link>

      {/* Logout */}
      <div style={{ padding: '0 14px 14px', borderTop: 'none' }}>
        <button onClick={logout}
          style={{ display: 'flex', alignItems: 'center', gap: 10, width: '100%', padding: '11px 14px', borderRadius: 8, background: 'none', border: 'none', cursor: 'pointer', color: 'rgba(255,255,255,0.55)', fontSize: 15, fontWeight: 600, transition: 'all 0.15s' }}
          onMouseOver={e => { e.currentTarget.style.background = 'rgba(239,68,68,0.15)'; e.currentTarget.style.color = '#f87171'; }}
          onMouseOut={e => { e.currentTarget.style.background = 'none'; e.currentTarget.style.color = 'rgba(255,255,255,0.55)'; }}>
          <LogOut size={17} />
          Sign Out
        </button>
      </div>
    </div>
  )
}
