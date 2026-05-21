import { useEffect, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import Layout from '../../components/Layout'
import { codingApi } from '../../api/axios'
import toast from 'react-hot-toast'
import {
  Plus, Pencil, Trash2, Code2, X, Loader2, Check,
  Share2, Eye, Calendar, Clock, Terminal, ChevronRight
} from 'lucide-react'
import ShareLinkModal from '../../components/ShareLinkModal'

const DIFFICULTIES = ['EASY', 'MEDIUM', 'HARD']
const diffClass = { EASY: 'badge-easy', MEDIUM: 'badge-medium', HARD: 'badge-hard' }

function DeleteConfirmModal({ test, onClose, onConfirm, loading }) {
  return (
    <div className="modal-overlay" onClick={onClose}>
      <motion.div
        initial={{ scale: 0.9, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} exit={{ scale: 0.9, opacity: 0 }}
        className="modal-box" onClick={e => e.stopPropagation()}
        style={{ textAlign: 'center', maxWidth: 400 }}
      >
        <div style={{ width: 64, height: 64, borderRadius: '50%', background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 20px' }}>
          <Trash2 size={28} color="#f87171" />
        </div>
        <h2 style={{ fontSize: 22, fontWeight: 700, color: 'var(--text-main)', marginBottom: 10 }}>Delete Problem?</h2>
        <p style={{ fontSize: 14, color: 'var(--text-sec)', marginBottom: 8 }}>You are about to permanently delete:</p>
        <div style={{ background: 'rgba(239,68,68,0.07)', border: '1px solid rgba(239,68,68,0.2)', borderRadius: 10, padding: '10px 16px', marginBottom: 28 }}>
          <span style={{ fontSize: 15, fontWeight: 700, color: '#f87171' }}>"{test?.title}"</span>
        </div>
        <div style={{ display: 'flex', gap: 12 }}>
          <button onClick={onClose} className="btn-ghost" style={{ flex: 1, justifyContent: 'center' }} disabled={loading}>Cancel</button>
          <button onClick={onConfirm} disabled={loading}
            style={{ flex: 1, padding: '10px 16px', background: 'linear-gradient(135deg,#ef4444,#dc2626)', color: '#fff', border: 'none', borderRadius: 10, fontSize: 14, fontWeight: 700, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8 }}>
            {loading ? <Loader2 size={15} className="spin" /> : <Trash2 size={15} />}
            {loading ? 'Deleting…' : 'Yes, Delete'}
          </button>
        </div>
      </motion.div>
    </div>
  )
}

function CodingTestDetailModal({ test, onClose, onEdit, onDelete, onShare }) {
  const formatDate = (dt) => dt ? new Date(dt).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' }) : '—'

  return (
    <div className="modal-overlay" onClick={onClose} style={{ overflowY: 'auto', alignItems: 'flex-start' }}>
      <motion.div
        initial={{ scale: 0.95, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} exit={{ scale: 0.95, opacity: 0 }}
        className="modal-box" style={{ margin: '40px auto', maxWidth: 640, width: '95%' }} onClick={e => e.stopPropagation()}
      >
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 20 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <div style={{ width: 40, height: 40, borderRadius: 10, background: 'linear-gradient(135deg,#38bdf8,#3b82f6)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <Code2 size={18} color="#fff" />
            </div>
            <div>
              <h2 style={{ fontSize: 20, fontWeight: 800, color: 'var(--text-main)' }}>{test.title}</h2>
              <span className={`badge ${diffClass[test.difficulty] || ''}`} style={{ marginTop: 4, display: 'inline-block' }}>{test.difficulty}</span>
            </div>
          </div>
          <button onClick={onClose} style={{ background: 'none', border: 'none', color: 'var(--text-sec)', cursor: 'pointer', padding: 4 }}>
            <X size={20} />
          </button>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
          {/* Description */}
          <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid rgba(255,255,255,0.06)', borderRadius: 12, padding: 16 }}>
            <h3 style={{ fontSize: 14, fontWeight: 700, color: 'var(--text-main)', marginBottom: 8 }}>Problem Description</h3>
            <div
              className="leetcode-description"
              style={{ fontSize: 13, color: 'var(--text-sec)', lineHeight: 1.6, overflowY: 'auto', maxHeight: 200 }}
              dangerouslySetInnerHTML={{ __html: test.description || '<span style="font-style:italic;">No description provided.</span>' }}
            />
          </div>

          {/* Code Sample Inputs/Outputs */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
            <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid rgba(255,255,255,0.06)', borderRadius: 12, padding: 16 }}>
              <h4 style={{ fontSize: 13, fontWeight: 700, color: 'var(--text-main)', marginBottom: 6, display: 'flex', alignItems: 'center', gap: 6 }}>
                <Terminal size={14} color="#38bdf8" /> Sample Input
              </h4>
              <pre style={{ margin: 0, padding: 10, background: '#0a0f1d', borderRadius: 8, fontSize: 12, color: 'var(--text-main)', fontFamily: 'monospace', overflowX: 'auto', minHeight: 48 }}>
                {test.sampleInput || '—'}
              </pre>
            </div>
            <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid rgba(255,255,255,0.06)', borderRadius: 12, padding: 16 }}>
              <h4 style={{ fontSize: 13, fontWeight: 700, color: 'var(--text-main)', marginBottom: 6, display: 'flex', alignItems: 'center', gap: 6 }}>
                <Terminal size={14} color="#4ade80" /> Expected Output
              </h4>
              <pre style={{ margin: 0, padding: 10, background: '#0a0f1d', borderRadius: 8, fontSize: 12, color: 'var(--text-main)', fontFamily: 'monospace', overflowX: 'auto', minHeight: 48 }}>
                {test.sampleOutput || '—'}
              </pre>
            </div>
          </div>



          {/* Action buttons */}
          <div style={{ display: 'flex', gap: 12, marginTop: 8, paddingTop: 16, borderTop: '1px solid rgba(255,255,255,0.06)' }}>
            <button onClick={onShare} className="btn-ghost" style={{ display: 'flex', alignItems: 'center', gap: 6, color: '#38bdf8', border: '1px solid rgba(56,189,248,0.2)' }}>
              <Share2 size={14} /> Share Links
            </button>
            <button onClick={onDelete} className="btn-ghost" style={{ display: 'flex', alignItems: 'center', gap: 6, color: '#f87171', border: '1px solid rgba(239,68,68,0.2)', marginLeft: 'auto' }}>
              <Trash2 size={14} /> Delete
            </button>
            <button onClick={onEdit} className="btn-primary" style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
              <Pencil size={14} /> Edit Problem
            </button>
          </div>
        </div>
      </motion.div>
    </div>
  )
}

function CodingModal({ test, onClose, onSave }) {
  const [form, setForm] = useState({ title: '', description: '', sampleInput: '', sampleOutput: '', difficulty: 'EASY', ...test })
  const [loading, setLoading] = useState(false)
  const [importQuery, setImportQuery] = useState('')
  const [importing, setImporting] = useState(false)
  const [descTab, setDescTab] = useState('edit')

  const handleImport = async () => {
    if (!importQuery.trim()) {
      toast.error('Please enter a LeetCode URL or Slug')
      return
    }
    setImporting(true)
    try {
      const res = await codingApi.importLeetCode(importQuery)
      const data = res.data.data
      setForm(f => ({
        ...f,
        title: data.title || f.title,
        description: data.description || f.description,
        difficulty: data.difficulty || f.difficulty,
        sampleInput: data.sampleInput || f.sampleInput,
        sampleOutput: data.sampleOutput || f.sampleOutput,
      }))
      toast.success('LeetCode question imported!')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to import question')
    } finally {
      setImporting(false)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const payload = { ...form }

      if (test?.id) { await codingApi.update(test.id, payload); toast.success('Problem updated!'); }
      else { await codingApi.create(payload); toast.success('Problem created!'); }
      onSave()
    } catch (err) { toast.error(err.response?.data?.message || 'Error saving problem') }
    finally { setLoading(false) }
  }

  return (
    <div className="modal-overlay" onClick={onClose} style={{ overflowY: 'auto', alignItems: 'flex-start' }}>
      <motion.div initial={{ scale: 0.95, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} exit={{ scale: 0.95, opacity: 0 }} className="modal-box" style={{ margin: '40px auto', maxWidth: 600 }} onClick={e => e.stopPropagation()}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 24 }}>
          <h2 style={{ fontSize: 20, fontWeight: 700, color: 'var(--text-main)' }}>{test?.id ? 'Edit Problem' : 'Create Problem'}</h2>
          <button onClick={onClose} style={{ background: 'none', border: 'none', color: 'var(--text-sec)', cursor: 'pointer' }}><X size={20} /></button>
        </div>
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          {!test?.id && (
            <div style={{ background: 'rgba(56,189,248,0.04)', border: '1px dashed rgba(56,189,248,0.25)', borderRadius: 12, padding: 16, display: 'flex', flexDirection: 'column', gap: 10 }}>
              <label style={{ display: 'block', fontSize: 13, fontWeight: 700, color: '#38bdf8' }}>Import from LeetCode</label>
              <div style={{ display: 'flex', gap: 10 }}>
                <input
                  placeholder="e.g., https://leetcode.com/problems/two-sum/ or two-sum"
                  value={importQuery}
                  onChange={e => setImportQuery(e.target.value)}
                  className="input-field"
                  style={{ flex: 1, height: 40 }}
                />
                <button
                  type="button"
                  onClick={handleImport}
                  disabled={importing}
                  className="btn-primary"
                  style={{ height: 40, padding: '0 16px', background: 'linear-gradient(135deg, #38bdf8, #3b82f6)', border: 'none', minWidth: 100, display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                >
                  {importing ? <Loader2 size={16} className="spin" /> : 'Import'}
                </button>
              </div>
              <span style={{ fontSize: 11, color: 'var(--text-sec)' }}>Note: Expected output won't be imported. Please paste it manually.</span>
            </div>
          )}
          <div><label style={{ display: 'block', fontSize: 13, fontWeight: 600, color: 'var(--text-sec)', marginBottom: 8 }}>Title *</label>
          <input required value={form.title} onChange={e => setForm(f => ({ ...f, title: e.target.value }))} className="input-field" /></div>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 8 }}>
              <label style={{ fontSize: 13, fontWeight: 600, color: 'var(--text-sec)' }}>Description *</label>
              <div style={{ display: 'flex', gap: 4, background: 'rgba(255,255,255,0.05)', padding: 2, borderRadius: 6 }}>
                <button
                  type="button"
                  onClick={() => setDescTab('edit')}
                  style={{
                    padding: '4px 10px',
                    fontSize: 11,
                    fontWeight: 600,
                    borderRadius: 4,
                    border: 'none',
                    background: descTab === 'edit' ? 'var(--primary)' : 'transparent',
                    color: descTab === 'edit' ? '#fff' : 'var(--text-sec)',
                    cursor: 'pointer',
                    transition: 'all 0.2s'
                  }}
                >
                  Edit
                </button>
                <button
                  type="button"
                  onClick={() => setDescTab('preview')}
                  style={{
                    padding: '4px 10px',
                    fontSize: 11,
                    fontWeight: 600,
                    borderRadius: 4,
                    border: 'none',
                    background: descTab === 'preview' ? 'var(--primary)' : 'transparent',
                    color: descTab === 'preview' ? '#fff' : 'var(--text-sec)',
                    cursor: 'pointer',
                    transition: 'all 0.2s'
                  }}
                >
                  Preview
                </button>
              </div>
            </div>
            {descTab === 'edit' ? (
              <textarea
                required
                rows={5}
                value={form.description}
                onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
                className="input-field"
                style={{ resize: 'vertical', minHeight: 120 }}
              />
            ) : (
              <div
                className="leetcode-description"
                style={{
                  minHeight: 120,
                  maxHeight: 250,
                  overflowY: 'auto',
                  background: '#0d1117',
                  border: '1px solid rgba(255,255,255,0.08)',
                  borderRadius: 12,
                  padding: '12px 16px',
                  textAlign: 'left'
                }}
                dangerouslySetInnerHTML={{ __html: form.description || '<span style="color:var(--text-sec); font-style:italic;">No description provided.</span>' }}
              />
            )}
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
            <div><label style={{ display: 'block', fontSize: 13, fontWeight: 600, color: 'var(--text-sec)', marginBottom: 8 }}>Sample Input</label>
            <textarea rows={2} value={form.sampleInput || ''} onChange={e => setForm(f => ({ ...f, sampleInput: e.target.value }))} className="input-field" style={{ resize: 'none', fontFamily: 'monospace' }} /></div>
            <div><label style={{ display: 'block', fontSize: 13, fontWeight: 600, color: 'var(--text-sec)', marginBottom: 8 }}>Expected Output</label>
            <textarea rows={2} value={form.sampleOutput || ''} onChange={e => setForm(f => ({ ...f, sampleOutput: e.target.value }))} className="input-field" style={{ resize: 'none', fontFamily: 'monospace' }} /></div>
          </div>

          <div><label style={{ display: 'block', fontSize: 13, fontWeight: 600, color: 'var(--text-sec)', marginBottom: 8 }}>Difficulty</label>
            <div style={{ display: 'flex', gap: 8 }}>
              {DIFFICULTIES.map(d => (
                <button key={d} type="button" onClick={() => setForm(f => ({ ...f, difficulty: d }))}
                  style={{ flex: 1, padding: '8px', borderRadius: 8, fontSize: 12, fontWeight: 600, border: `1px solid ${form.difficulty === d ? (d === 'EASY' ? '#4ade80' : d === 'MEDIUM' ? '#fbbf24' : '#f87171') : 'rgba(255,255,255,0.1)'}`, background: form.difficulty === d ? (d === 'EASY' ? 'rgba(74,222,128,0.15)' : d === 'MEDIUM' ? 'rgba(251,191,36,0.15)' : 'rgba(248,113,113,0.15)') : 'rgba(255,255,255,0.03)', color: form.difficulty === d ? (d === 'EASY' ? '#4ade80' : d === 'MEDIUM' ? '#fbbf24' : '#f87171') : '#94a3b8', cursor: 'pointer', transition: 'all 0.2s' }}>
                  {d}
                </button>
              ))}
            </div>
          </div>
          <div style={{ display: 'flex', gap: 12, marginTop: 16 }}>
            <button type="button" onClick={onClose} className="btn-ghost" style={{ flex: 1, justifyContent: 'center' }}>Cancel</button>
            <button type="submit" disabled={loading} className="btn-primary" style={{ flex: 1, justifyContent: 'center' }}>
              {loading ? <Loader2 size={16} className="spin" /> : <Check size={16} />} {test?.id ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </motion.div>
    </div>
  )
}

export default function AdminCodingTests() {
  const [tests, setTests] = useState([])
  const [loading, setLoading] = useState(true)
  const [modal, setModal] = useState(null)
  const [detailModal, setDetailModal] = useState(null)
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleting, setDeleting] = useState(false)
  const [shareExam, setShareExam] = useState(null)

  const fetchTests = () => codingApi.getAll().then(r => setTests(r.data.data || [])).finally(() => setLoading(false))
  useEffect(() => { fetchTests() }, [])

  const handleDelete = async () => {
    if (!deleteTarget) return
    setDeleting(true)
    try {
      await codingApi.delete(deleteTarget.id)
      toast.success('Problem deleted')
      setDeleteTarget(null)
      setDetailModal(null)
      fetchTests()
    } catch {
      toast.error('Delete failed')
    } finally {
      setDeleting(false)
    }
  }

  return (
    <Layout title="Coding Tests" subtitle="Manage programming challenges"
      action={<button onClick={() => setModal({})} className="btn-primary"><Plus size={16} /><span> New Problem</span></button>}>
      {loading ? <div style={{ display: 'flex', justifyContent: 'center', padding: 80 }}><div className="spinner" /></div>
      : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill,minmax(320px,1fr))', gap: 20 }}>
          {tests.map((test, i) => (
            <motion.div key={test.id} initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.05 }}
              className="card"
              style={{ padding: 24, display: 'flex', flexDirection: 'column', cursor: 'pointer' }}
              onClick={() => setDetailModal(test)}
              onMouseOver={e => e.currentTarget.style.borderColor = 'rgba(56,189,248,0.4)'}
              onMouseOut={e => e.currentTarget.style.borderColor = ''}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 16 }}>
                <div style={{ width: 44, height: 44, borderRadius: 12, background: 'linear-gradient(135deg,#38bdf8,#3b82f6)', display: 'flex', alignItems: 'center', justifyContent: 'center', boxShadow: '0 0 16px rgba(56,189,248,0.3)' }}>
                  <Code2 size={20} color="#fff" />
                </div>
                <span className={`badge ${diffClass[test.difficulty] || ''}`}>{test.difficulty}</span>
              </div>
              <h3 style={{ fontSize: 25, fontWeight: 800, color: 'var(--text-main)', marginBottom: 8 }}>{test.title}</h3>
              <p style={{ fontSize: 13, color: 'var(--text-sec)', lineHeight: 1.6, marginBottom: 16, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                {test.description ? test.description.replace(/<[^>]*>/g, '') : ''}
              </p>
              <div style={{ display: 'flex', gap: 8, marginTop: 'auto', paddingTop: 16, borderTop: '1px solid rgba(255,255,255,0.06)' }} onClick={e => e.stopPropagation()}>
                <button onClick={() => setShareExam(test)} style={{ padding: '0 12px', height: 32, borderRadius: 8, background: 'rgba(56,189,248,0.1)', color: '#38bdf8', fontSize: 13, fontWeight: 600, display: 'flex', alignItems: 'center', gap: 6, border: 'none', cursor: 'pointer', marginRight: 'auto' }}>
                  <Share2 size={14} /> Share Links
                </button>
                <button onClick={() => setModal(test)} style={{ width: 32, height: 32, borderRadius: 8, background: 'rgba(255,255,255,0.05)', color: 'var(--text-sec)', display: 'flex', alignItems: 'center', justifyContent: 'center', border: 'none', cursor: 'pointer' }}><Pencil size={14} /></button>
                <button onClick={() => setDeleteTarget(test)} style={{ width: 32, height: 32, borderRadius: 8, background: 'rgba(239,68,68,0.1)', color: '#f87171', display: 'flex', alignItems: 'center', justifyContent: 'center', border: 'none', cursor: 'pointer' }}>
                  <Trash2 size={14} />
                </button>
              </div>
            </motion.div>
          ))}
          {tests.length === 0 && <div style={{ gridColumn: '1/-1', textAlign: 'center', padding: 80, color: 'var(--text-sec)' }}><Code2 size={40} style={{ margin: '0 auto 12px', opacity: 0.3 }} /><p>No problems yet</p></div>}
        </div>
      )}

      {/* Detail Modal */}
      <AnimatePresence>
        {detailModal && (
          <CodingTestDetailModal
            test={detailModal}
            onClose={() => setDetailModal(null)}
            onEdit={() => {
              const target = detailModal;
              setDetailModal(null);
              setModal(target);
            }}
            onDelete={() => {
              setDeleteTarget(detailModal);
            }}
            onShare={() => {
              setShareExam(detailModal);
            }}
          />
        )}
      </AnimatePresence>

      <AnimatePresence>{modal !== null && <CodingModal test={modal?.id ? modal : null} onClose={() => setModal(null)} onSave={() => { setModal(null); fetchTests() }} />}</AnimatePresence>
      <AnimatePresence>
        {deleteTarget && (
          <DeleteConfirmModal
            test={deleteTarget}
            onClose={() => { if (!deleting) setDeleteTarget(null) }}
            onConfirm={handleDelete}
            loading={deleting}
          />
        )}
      </AnimatePresence>
      <ShareLinkModal
        isOpen={!!shareExam}
        onClose={() => setShareExam(null)}
        examId={shareExam?.id}
        examType="CODING"
        shareToken={shareExam?.shareToken}
      />
    </Layout>
  )
}
