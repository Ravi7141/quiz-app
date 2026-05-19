import { useEffect, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import Layout from '../../components/Layout'
import ShareLinkModal from '../../components/ShareLinkModal'
import { assessmentApi, adminQuizApi, codingApi } from '../../api/axios'
import toast from 'react-hot-toast'
import { Plus, Trash2, Check, X, Loader2, Link2, Calendar, Clock, BookOpen, Terminal, Sparkles, Copy, Mail } from 'lucide-react'

function ShareModal({ assessment, onClose }) {
  const shareUrl = `${window.location.origin}/assessment/${assessment?.shareToken}`

  const copyToClipboard = () => {
    navigator.clipboard.writeText(shareUrl)
    toast.success('Link copied to clipboard!')
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <motion.div
        initial={{ scale: 0.9, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        exit={{ scale: 0.9, opacity: 0 }}
        className="modal-box"
        onClick={e => e.stopPropagation()}
        style={{ maxWidth: 500, width: '100%' }}
      >
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 20 }}>
          <h2 style={{ fontSize: 20, fontWeight: 700, color: 'var(--text-main)', display: 'flex', alignItems: 'center', gap: 8 }}>
            <Sparkles size={20} color="#a78bfa" />
            Share Assessment Link
          </h2>
          <button onClick={onClose} style={{ background: 'none', border: 'none', color: 'var(--text-sec)', cursor: 'pointer' }}>
            <X size={20} />
          </button>
        </div>
        <p style={{ fontSize: 14, color: 'var(--text-sec)', marginBottom: 16 }}>
          Students can use the link below to attempt the entire assessment (both Quiz and Coding sections in one flow):
        </p>
        <div style={{ display: 'flex', background: 'rgba(255,255,255,0.03)', border: '1px solid rgba(255,255,255,0.08)', borderRadius: 10, padding: 12, alignItems: 'center', justifyContent: 'space-between', gap: 12, marginBottom: 24 }}>
          <span style={{ fontSize: 13, color: 'var(--text-main)', wordBreak: 'break-all', fontFamily: 'monospace' }}>
            {shareUrl}
          </span>
          <button onClick={copyToClipboard} className="btn-ghost" style={{ padding: '8px 12px', background: 'rgba(124,58,237,0.1)', color: '#c084fc', border: '1px solid rgba(124,58,237,0.2)' }}>
            <Copy size={14} /> Copy
          </button>
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
          <button onClick={onClose} className="btn-primary" style={{ minWidth: 100, justifyContent: 'center' }}>Done</button>
        </div>
      </motion.div>
    </div>
  )
}

function CreateAssessmentModal({ quizzes, codingTests, onClose, onSave }) {
  const [form, setForm] = useState({
    title: '',
    description: '',
    durationMinutes: '',
    sections: []
  })
  const [loading, setLoading] = useState(false)

  const addSection = () => {
    setForm(f => ({
      ...f,
      sections: [...f.sections, { type: 'QUIZ', referenceId: '', order: f.sections.length + 1 }]
    }))
  }

  const removeSection = (index) => {
    setForm(f => {
      const updated = f.sections.filter((_, idx) => idx !== index)
      // re-order them
      return {
        ...f,
        sections: updated.map((s, idx) => ({ ...s, order: idx + 1 }))
      }
    })
  }

  const updateSection = (index, field, value) => {
    setForm(f => {
      const updated = [...f.sections]
      updated[index] = { ...updated[index], [field]: value }
      if (field === 'type') {
        // clear reference ID when switching types
        updated[index].referenceId = ''
      }
      return { ...f, sections: updated }
    })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.durationMinutes || Number(form.durationMinutes) < 1) {
      toast.error('Duration must be at least 1 minute')
      return
    }
    if (form.sections.length === 0) {
      toast.error('Please add at least one section')
      return
    }
    const invalidSec = form.sections.find(s => !s.referenceId)
    if (invalidSec) {
      toast.error('Please select a valid quiz or coding test for all sections')
      return
    }

    setLoading(true)
    try {
      const payload = {
        title: form.title,
        description: form.description,
        durationMinutes: Number(form.durationMinutes),
        sections: form.sections.map(s => ({
          type: s.type,
          referenceId: Number(s.referenceId),
          order: s.order
        }))
      }
      await assessmentApi.create(payload)
      toast.success('Assessment created successfully!')
      onSave()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create assessment')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose} style={{ overflowY: 'auto', alignItems: 'flex-start' }}>
      <motion.div
        initial={{ scale: 0.95, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        exit={{ scale: 0.95, opacity: 0 }}
        className="modal-box"
        style={{ margin: '40px auto', maxWidth: 600, width: '100%' }}
        onClick={e => e.stopPropagation()}
      >
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 24 }}>
          <h2 style={{ fontSize: 20, fontWeight: 700, color: 'var(--text-main)' }}>Create Unified Assessment</h2>
          <button onClick={onClose} style={{ background: 'none', border: 'none', color: 'var(--text-sec)', cursor: 'pointer' }}>
            <X size={20} />
          </button>
        </div>
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <div>
            <label style={{ display: 'block', fontSize: 13, fontWeight: 600, color: 'var(--text-sec)', marginBottom: 8 }}>Assessment Title *</label>
            <input required value={form.title} onChange={e => setForm(f => ({ ...f, title: e.target.value }))} className="input-field" placeholder="e.g. Java Fullstack Hiring Test" />
          </div>
          <div>
            <label style={{ display: 'block', fontSize: 13, fontWeight: 600, color: 'var(--text-sec)', marginBottom: 8 }}>Description</label>
            <textarea value={form.description} onChange={e => setForm(f => ({ ...f, description: e.target.value }))} rows={2} className="input-field" style={{ resize: 'none' }} placeholder="Optional description or candidate instructions" />
          </div>
          <div>
            <label style={{ display: 'block', fontSize: 13, fontWeight: 600, color: 'var(--text-sec)', marginBottom: 8 }}>Duration (minutes) *</label>
            <input required type="number" value={form.durationMinutes} onChange={e => setForm(f => ({ ...f, durationMinutes: e.target.value }))} className="input-field" placeholder="e.g. 60" min="1" />
          </div>

          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
              <label style={{ fontSize: 14, fontWeight: 700, color: 'var(--text-main)' }}>Assessment Flow / Sections</label>
              <button type="button" onClick={addSection} className="btn-ghost" style={{ fontSize: 12, padding: '6px 12px', color: '#c084fc', border: '1px solid rgba(124,58,237,0.2)' }}>
                <Plus size={14} /> Add Section
              </button>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              {form.sections.map((sec, idx) => (
                <div key={idx} style={{ display: 'flex', gap: 10, alignItems: 'center', background: 'rgba(255,255,255,0.02)', border: '1px solid rgba(255,255,255,0.06)', borderRadius: 10, padding: 12 }}>
                  <div style={{ background: '#7c3aed', color: '#fff', width: 24, height: 24, borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 12, fontWeight: 700, flexShrink: 0 }}>
                    {idx + 1}
                  </div>
                  <select
                    value={sec.type}
                    onChange={e => updateSection(idx, 'type', e.target.value)}
                    className="input-field"
                    style={{ width: 110, padding: '8px 12px' }}
                  >
                    <option value="QUIZ">MCQ Quiz</option>
                    <option value="CODING">Coding Test</option>
                  </select>

                  <select
                    value={sec.referenceId}
                    onChange={e => updateSection(idx, 'referenceId', e.target.value)}
                    className="input-field"
                    style={{ flex: 1, padding: '8px 12px' }}
                    required
                  >
                    <option value="">-- Select Target --</option>
                    {sec.type === 'QUIZ'
                      ? quizzes.map(q => <option key={q.id} value={q.id}>{q.title}</option>)
                      : codingTests.map(t => <option key={t.id} value={t.id}>{t.title}</option>)}
                  </select>

                  <button type="button" onClick={() => removeSection(idx)} style={{ background: 'none', border: 'none', color: '#ef4444', cursor: 'pointer', padding: 4 }}>
                    <Trash2 size={16} />
                  </button>
                </div>
              ))}

              {form.sections.length === 0 && (
                <div style={{ textAlign: 'center', padding: 20, color: 'var(--text-sec)', border: '1px dashed rgba(255,255,255,0.1)', borderRadius: 10 }}>
                  No sections added. Click "Add Section" to configure the exam flow.
                </div>
              )}
            </div>
          </div>

          <div style={{ display: 'flex', gap: 12, marginTop: 16 }}>
            <button type="button" onClick={onClose} className="btn-ghost" style={{ flex: 1, justifyContent: 'center' }}>Cancel</button>
            <button type="submit" disabled={loading} className="btn-primary" style={{ flex: 1, justifyContent: 'center' }}>
              {loading ? <Loader2 size={16} className="spin" /> : <Check size={16} />} Create
            </button>
          </div>
        </form>
      </motion.div>
    </div>
  )
}

export default function ManageAssessments() {
  const [assessments, setAssessments] = useState([])
  const [quizzes, setQuizzes] = useState([])
  const [codingTests, setCodingTests] = useState([])
  const [loading, setLoading] = useState(true)
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [shareTarget, setShareTarget] = useState(null)
  const [privateShareTarget, setPrivateShareTarget] = useState(null)

  const fetchData = async () => {
    try {
      const [resAssess, resQuizzes, resCoding] = await Promise.all([
        assessmentApi.getAll(),
        adminQuizApi.getAll(),
        codingApi.getAll()
      ])
      setAssessments(resAssess.data.data || [])
      setQuizzes(resQuizzes.data.data || [])
      setCodingTests(resCoding.data.data || [])
    } catch (err) {
      toast.error('Failed to load data')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [])

  return (
    <Layout
      title="Manage Assessments"
      subtitle="Combine Quiz & Coding tests into a single assessment link"
      action={<button onClick={() => setShowCreateModal(true)} className="btn-primary"><Plus size={16} /> New Assessment</button>}
    >
      {loading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: 80 }}><div className="spinner" /></div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(340px, 1fr))', gap: 24 }}>
          {assessments.map((assess, idx) => (
            <motion.div
              key={assess.id}
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: idx * 0.05 }}
              className="card"
              style={{ padding: 24, display: 'flex', flexDirection: 'column', minHeight: 220 }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 12 }}>
                <div style={{ width: 44, height: 44, borderRadius: 12, background: 'rgba(124,58,237,0.1)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <Sparkles size={20} color="#a78bfa" />
                </div>
                <span className={`badge ${assess.active ? 'badge-active' : 'badge-off'}`}>
                  {assess.active ? 'Active' : 'Inactive'}
                </span>
              </div>

              <h3 style={{ fontSize: 20, fontWeight: 800, color: 'var(--text-main)', marginBottom: 8 }}>
                {assess.title}
              </h3>
              {assess.description && (
                <p style={{ fontSize: 13, color: 'var(--text-sec)', lineHeight: 1.5, marginBottom: 16, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                  {assess.description}
                </p>
              )}

              <div style={{ display: 'flex', gap: 16, marginTop: 'auto', paddingTop: 16, borderTop: '1px solid rgba(255,255,255,0.06)', fontSize: 13, color: 'var(--text-sec)' }}>
                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                  <Clock size={14} /> {assess.durationMinutes} mins
                </span>
                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                  <Link2 size={14} /> Shared Flow
                </span>
              </div>

              <div style={{ display: 'flex', gap: 12, marginTop: 16 }}>
                <button
                  onClick={() => setShareTarget(assess)}
                  className="btn-ghost"
                  style={{ flex: 1, justifyContent: 'center', color: '#c084fc', border: '1px solid rgba(124,58,237,0.2)', background: 'rgba(124,58,237,0.05)', fontSize: 13 }}
                >
                  <Link2 size={13} /> Public Link
                </button>
                <button
                  onClick={() => setPrivateShareTarget(assess)}
                  className="btn-ghost"
                  style={{ flex: 1, justifyContent: 'center', color: '#a78bfa', border: '1px solid rgba(124,58,237,0.2)', background: 'rgba(124,58,237,0.05)', fontSize: 13 }}
                >
                  <Mail size={13} /> Private Links
                </button>
              </div>
            </motion.div>
          ))}

          {assessments.length === 0 && (
            <div style={{ gridColumn: '1/-1', textAlign: 'center', padding: 80, color: 'var(--text-sec)' }}>
              <Sparkles size={40} style={{ margin: '0 auto 12px', opacity: 0.3 }} />
              <p style={{ fontSize: 16, fontWeight: 600, marginBottom: 4 }}>No Assessments Yet</p>
              <p style={{ fontSize: 13 }}>Create your first unified assessment flow combining MCQ Quiz and Coding tests.</p>
            </div>
          )}
        </div>
      )}

      <AnimatePresence>
        {showCreateModal && (
          <CreateAssessmentModal
            quizzes={quizzes}
            codingTests={codingTests}
            onClose={() => setShowCreateModal(false)}
            onSave={() => {
              setShowCreateModal(false)
              fetchData()
            }}
          />
        )}
      </AnimatePresence>

      <AnimatePresence>
        {shareTarget && (
          <ShareModal
            assessment={shareTarget}
            onClose={() => setShareTarget(null)}
          />
        )}
      </AnimatePresence>

      <ShareLinkModal
        isOpen={!!privateShareTarget}
        onClose={() => setPrivateShareTarget(null)}
        examId={privateShareTarget?.id}
        examType="ASSESSMENT"
      />
    </Layout>
  )
}
