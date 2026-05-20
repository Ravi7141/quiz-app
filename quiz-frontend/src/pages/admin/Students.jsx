import { useEffect, useState } from 'react'
import { motion } from 'framer-motion'
import { useNavigate } from 'react-router-dom'
import Layout from '../../components/Layout'
import { adminApi } from '../../api/axios'
import toast from 'react-hot-toast'
import { Users, Search, Mail, ChevronRight, BarChart2, Trash2, Loader2 } from 'lucide-react'

export default function Students() {
  const [students, setStudents] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [deleting, setDeleting] = useState(null)
  const navigate = useNavigate()

  const fetchStudents = () => {
    adminApi.getStudents()
      .then(r => setStudents(r.data.data || []))
      .finally(() => setLoading(false))
  }

  useEffect(() => { fetchStudents() }, [])

  const handleDelete = async (e, student) => {
    e.stopPropagation()
    if (!confirm(`Delete student "${student.name}"? This cannot be undone.`)) return
    setDeleting(student.id)
    try {
      await adminApi.deleteStudent(student.id)
      toast.success('Student deleted')
      setStudents(prev => prev.filter(s => s.id !== student.id))
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to delete student')
    } finally {
      setDeleting(null)
    }
  }

  const filtered = students.filter(s =>
    s.name?.toLowerCase().includes(search.toLowerCase()) ||
    s.email?.toLowerCase().includes(search.toLowerCase()) ||
    s.phone?.includes(search)
  )

  return (
    <Layout title="Students" subtitle={`${students.length} registered accounts`}>
      <div style={{ position: 'relative', width: 300, marginBottom: 24 }}>
        <Search size={15} color="#64748b" style={{ position: 'absolute', left: 14, top: '50%', transform: 'translateY(-50%)' }} />
        <input value={search} onChange={e => setSearch(e.target.value)} placeholder="Search name or email..." className="input-field" style={{ paddingLeft: 40 }} />
      </div>

      {loading ? <div style={{ display: 'flex', justifyContent: 'center', padding: 80 }}><div className="spinner" /></div>
      : (
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                {['#', 'Name', 'Email', 'Phone', 'Role', 'Actions'].map(h => <th key={h}>{h}</th>)}
              </tr>
            </thead>
            <tbody>
              {filtered.map((s, i) => (
                <motion.tr key={s.id} initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: i * 0.03 }}
                  style={{ cursor: 'pointer' }}
                  onClick={() => navigate(`/admin/students/${s.id}`)}
                  onMouseOver={e => e.currentTarget.style.background = 'rgba(124,58,237,0.04)'}
                  onMouseOut={e => e.currentTarget.style.background = 'transparent'}>
                  <td style={{ color: 'var(--text-sec)' }}>{i + 1}</td>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                      <div style={{ width: 36, height: 36, borderRadius: '50%', background: 'linear-gradient(135deg,#7c3aed,#38bdf8)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontSize: 13, fontWeight: 700 }}>
                        {s.name?.[0]?.toUpperCase() || 'U'}
                      </div>
                      <span style={{ fontWeight: 600, fontSize: 15 }}>{s.name}</span>
                    </div>
                  </td>
                  <td><div style={{ display: 'flex', alignItems: 'center', gap: 8, color: 'var(--text-sec)' }}><Mail size={14} /> {s.email}</div></td>
                  <td style={{ color: 'var(--text-sec)', fontSize: 13 }}>{s.phone || '—'}</td>
                  <td><span className={`badge ${s.role === 'ADMIN' ? 'badge-admin' : 'badge-student'}`}>{s.role}</span></td>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }} onClick={e => e.stopPropagation()}>
                      <button
                        onClick={e => { e.stopPropagation(); navigate(`/admin/students/${s.id}`) }}
                        style={{ display: 'inline-flex', alignItems: 'center', gap: 6, padding: '6px 14px', borderRadius: 8, background: 'rgba(56,189,248,0.1)', border: '1px solid rgba(56,189,248,0.2)', color: '#38bdf8', fontSize: 13, fontWeight: 600, cursor: 'pointer' }}
                      >
                        <BarChart2 size={13} /> View <ChevronRight size={13} />
                      </button>
                      <button
                        onClick={e => handleDelete(e, s)}
                        disabled={deleting === s.id}
                        style={{ display: 'inline-flex', alignItems: 'center', gap: 6, padding: '6px 12px', borderRadius: 8, background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.2)', color: '#f87171', fontSize: 13, fontWeight: 600, cursor: 'pointer' }}
                      >
                        {deleting === s.id ? <Loader2 size={13} className="spin" /> : <Trash2 size={13} />}
                        {deleting === s.id ? '' : 'Delete'}
                      </button>
                    </div>
                  </td>
                </motion.tr>
              ))}
            </tbody>
          </table>
          {filtered.length === 0 && <div style={{ textAlign: 'center', padding: '60px 0', color: 'var(--text-sec)' }}><Users size={40} style={{ margin: '0 auto 12px', opacity: 0.3 }} /><p>No students found</p></div>}
        </div>
      )}
    </Layout>
  )
}
