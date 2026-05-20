import { useEffect, useState } from 'react'
import { motion } from 'framer-motion'
import Layout from '../../components/Layout'
import { adminApi } from '../../api/axios'
import { BarChart2, Search, CheckCircle2, XCircle, Trophy } from 'lucide-react'

export default function Results() {
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [filter, setFilter] = useState('ALL')

  useEffect(() => {
    adminApi.getResults()
      .then(r => {
        const d = (r.data.data || []).map(x => {
          const rawPct = x.totalMarks > 0 ? ((x.score || 0) / x.totalMarks) * 100 : 0;
          const percentage = Math.min(rawPct, 100);
          return { ...x, percentage, passed: percentage >= 60 };
        });
        setResults(d);
      })
      .finally(() => setLoading(false))
  }, [])

  const filtered = results.filter(r => {
    const sMatch = r.studentName?.toLowerCase().includes(search.toLowerCase()) || r.quizTitle?.toLowerCase().includes(search.toLowerCase())
    const fMatch = filter === 'ALL' || (filter === 'PASSED' ? r.passed : !r.passed)
    return sMatch && fMatch
  })

  const avg = results.length > 0 ? (results.reduce((a, r) => a + (r.percentage || 0), 0) / results.length).toFixed(1) : '—'
  const passRate = results.length > 0 ? ((results.filter(r => r.passed).length / results.length) * 100).toFixed(0) : '—'

  return (
    <Layout title="All Results" subtitle="Detailed student performance overview">
      {/* Summary Stats */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3,1fr)', gap: 16, marginBottom: 24 }}>
        {[
          { label: 'Total Attempts', val: results.length, icon: BarChart2, color: 'var(--primary)', bg: 'rgba(37,99,235,0.1)' },
          { label: 'Avg Score', val: avg !== '—' ? `${avg}%` : '—', icon: Trophy, color: '#38bdf8', bg: 'rgba(56,189,248,0.1)' },
          { label: 'Pass Rate', val: passRate !== '—' ? `${passRate}%` : '—', icon: CheckCircle2, color: '#4ade80', bg: 'rgba(74,222,128,0.1)' },
        ].map(({ label, val, icon: Icon, color, bg }, i) => (
          <motion.div key={label} initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.05 }} className="card" style={{ padding: '20px 24px', display: 'flex', alignItems: 'center', gap: 16 }}>
            <div style={{ width: 44, height: 44, borderRadius: 12, background: bg, display: 'flex', alignItems: 'center', justifyContent: 'center' }}><Icon size={20} color={color} /></div>
            <div>
              <div style={{ fontSize: 24, fontWeight: 800, color: 'var(--text-main)' }}>{val}</div>
              <div style={{ fontSize: 12, color: 'var(--text-sec)', marginTop: 2 }}>{label}</div>
            </div>
          </motion.div>
        ))}
      </div>

      {/* Toolbar */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 24, flexWrap: 'wrap' }}>
        <div style={{ position: 'relative', width: 280 }}>
          <Search size={15} color="#64748b" style={{ position: 'absolute', left: 14, top: '50%', transform: 'translateY(-50%)' }} />
          <input value={search} onChange={e => setSearch(e.target.value)} placeholder="Search student or quiz..." className="input-field" style={{ paddingLeft: 40 }} />
        </div>
        <div style={{ display: 'flex', gap: 8 }}>
          {['ALL', 'PASSED', 'FAILED'].map(f => (
            <button key={f} onClick={() => setFilter(f)} style={{ padding: '8px 16px', borderRadius: 8, fontSize: 12, fontWeight: 600, border: `1px solid ${filter === f ? 'var(--primary)' : 'rgba(255,255,255,0.1)'}`, background: filter === f ? 'rgba(37,99,235,0.15)' : 'rgba(255,255,255,0.03)', color: filter === f ? 'var(--primary-400)' : '#94a3b8', cursor: 'pointer', transition: 'all 0.2s' }}>{f}</button>
          ))}
        </div>
      </div>

      {loading ? <div style={{ display: 'flex', justifyContent: 'center', padding: 80 }}><div className="spinner" /></div>
      : (
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>{['Student', 'Email', 'Phone', 'Quiz', 'Score', '%', 'Correct', 'Status'].map(h => <th key={h}>{h}</th>)}</tr>
            </thead>
            <tbody>
              {filtered.map((r, i) => (
                <motion.tr key={r.attemptId} initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: i * 0.02 }}>
                  <td><div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                    <div style={{ width: 28, height: 28, borderRadius: 8, background: 'linear-gradient(135deg,var(--primary),var(--primary-400))', color: '#fff', fontSize: 11, fontWeight: 700, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>{r.studentName?.[0]?.toUpperCase()}</div>
                    <span style={{ fontWeight: 500 }}>{r.studentName}</span>
                  </div></td>
                  <td style={{ color: 'var(--text-sec)', fontSize: 13 }}>{r.studentEmail}</td>
                  <td style={{ color: 'var(--text-sec)', fontSize: 13 }}>{r.studentPhone || '—'}</td>
                  <td style={{ color: 'var(--text-sec)', maxWidth: 160, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{r.quizTitle}</td>
                  <td style={{ fontFamily: 'monospace', color: 'var(--text-main)' }}>{r.score}/{r.totalMarks}</td>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                      <div className="progress-track" style={{ width: 60 }}><div className="progress-fill" style={{ width: `${r.percentage || 0}%` }} /></div>
                      <span style={{ fontWeight: 700, fontSize: 12 }} className="grad">{r.percentage?.toFixed(1)}%</span>
                    </div>
                  </td>
                  <td style={{ color: 'var(--text-sec)' }}>
                    <span style={{ color: '#38bdf8', fontWeight: 700 }}>
                      {r.correctAnswers != null ? r.correctAnswers : '—'}
                    </span>
                    {r.totalQuestions != null ? <span style={{ color: 'var(--text-sec)' }}>/{r.totalQuestions}</span> : ''}
                  </td>
                  <td>{r.passed ? <span className="badge badge-active"><CheckCircle2 size={10} style={{ marginRight: 4 }} /> Passed</span> : <span className="badge badge-hard"><XCircle size={10} style={{ marginRight: 4 }} /> Failed</span>}</td>
                </motion.tr>
              ))}
            </tbody>
          </table>
          {filtered.length === 0 && <div style={{ textAlign: 'center', padding: '60px 0', color: 'var(--text-sec)' }}><p>No results found</p></div>}
        </div>
      )}
    </Layout>
  )
}
