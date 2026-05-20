import { useEffect, useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { motion, AnimatePresence } from 'framer-motion'
import Layout from '../../components/Layout'
import { assessmentApi, adminQuizApi, codingApi } from '../../api/axios'
import api from '../../api/axios'
import toast from 'react-hot-toast'
import {
  ArrowLeft, Sparkles, Clock, Calendar, BookOpen, Terminal,
  Pencil, Check, X, Loader2, Plus, Trash2, ChevronRight, Users, Trophy, CheckCircle2, XCircle
} from 'lucide-react'

function EditAssessmentModal({ assessment, quizzes, codingTests, currentSections, onClose, onSave }) {
  const [form, setForm] = useState({
    title: assessment.title || '',
    description: assessment.description || '',
    durationMinutes: assessment.durationMinutes || '',
    scheduledFor: assessment.scheduledFor ? assessment.scheduledFor.slice(0, 16) : '',
    validUntil: assessment.validUntil ? assessment.validUntil.slice(0, 16) : '',
    passingPercentage: assessment.passingPercentage || '',
    sections: currentSections.map(s => ({
      type: s.type,
      referenceId: s.referenceId,
      order: s.order
    }))
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
      return { ...f, sections: updated.map((s, idx) => ({ ...s, order: idx + 1 })) }
    })
  }

  const updateSection = (index, field, value) => {
    setForm(f => {
      const updated = [...f.sections]
      updated[index] = { ...updated[index], [field]: value }
      if (field === 'type') updated[index].referenceId = ''
      return { ...f, sections: updated }
    })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.durationMinutes || Number(form.durationMinutes) < 1) { toast.error('Duration must be at least 1 minute'); return }
    if (form.sections.length === 0) { toast.error('Please add at least one section'); return }
    if (form.sections.find(s => !s.referenceId)) { toast.error('Please select a valid quiz or coding test for all sections'); return }

    setLoading(true)
    try {
      const payload = {
        title: form.title,
        description: form.description,
        durationMinutes: Number(form.durationMinutes),
        scheduledFor: form.scheduledFor ? form.scheduledFor + ':00' : null,
        validUntil: form.validUntil ? form.validUntil + ':00' : null,
        passingPercentage: form.passingPercentage ? Number(form.passingPercentage) : null,
        sections: form.sections.map(s => ({ type: s.type, referenceId: Number(s.referenceId), order: s.order }))
      }
      await assessmentApi.update(assessment.id, payload)
      toast.success('Assessment updated!')
      onSave()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Update failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose} style={{ overflowY: 'auto', alignItems: 'flex-start' }}>
      <motion.div
        initial={{ scale: 0.95, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} exit={{ scale: 0.95, opacity: 0 }}
        className="modal-box" style={{ margin: '40px auto', maxWidth: 620, width: '100%' }} onClick={e => e.stopPropagation()}
      >
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 24 }}>
          <h2 style={{ fontSize: 20, fontWeight: 700, color: 'var(--text-main)' }}>Edit Assessment</h2>
          <button onClick={onClose} style={{ background: 'none', border: 'none', color: 'var(--text-sec)', cursor: 'pointer' }}>
            <X size={20} />
          </button>
        </div>
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <div>
            <label style={{ display: 'block', fontSize: 13, fontWeight: 600, color: 'var(--text-sec)', marginBottom: 8 }}>Title *</label>
            <input required value={form.title} onChange={e => setForm(f => ({ ...f, title: e.target.value }))} className="input-field" />
          </div>
          <div>
            <label style={{ display: 'block', fontSize: 13, fontWeight: 600, color: 'var(--text-sec)', marginBottom: 8 }}>Description</label>
            <textarea rows={2} value={form.description} onChange={e => setForm(f => ({ ...f, description: e.target.value }))} className="input-field" style={{ resize: 'none' }} />
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
            <div>
              <label style={{ display: 'block', fontSize: 13, fontWeight: 600, color: 'var(--text-sec)', marginBottom: 8 }}>Duration (minutes) *</label>
              <input required type="number" min="1" value={form.durationMinutes} onChange={e => setForm(f => ({ ...f, durationMinutes: e.target.value }))} className="input-field" />
            </div>
            <div>
              <label style={{ display: 'block', fontSize: 13, fontWeight: 600, color: 'var(--text-sec)', marginBottom: 8 }}>Passing Percentage (%)</label>
              <input type="number" min="0" max="100" value={form.passingPercentage} onChange={e => setForm(f => ({ ...f, passingPercentage: e.target.value }))} className="input-field" placeholder="e.g. 70" />
            </div>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
            <div>
              <label style={{ display: 'block', fontSize: 13, fontWeight: 600, color: 'var(--text-sec)', marginBottom: 8 }}>Start Date & Time</label>
              <input type="datetime-local" value={form.scheduledFor} onChange={e => setForm(f => ({ ...f, scheduledFor: e.target.value }))} className="input-field" />
            </div>
            <div>
              <label style={{ display: 'block', fontSize: 13, fontWeight: 600, color: 'var(--text-sec)', marginBottom: 8 }}>End Date & Time</label>
              <input type="datetime-local" value={form.validUntil} onChange={e => setForm(f => ({ ...f, validUntil: e.target.value }))} className="input-field" />
            </div>
          </div>

          {/* Sections */}
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
                  <div style={{ background: '#7c3aed', color: '#fff', width: 24, height: 24, borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 12, fontWeight: 700, flexShrink: 0 }}>{idx + 1}</div>
                  <select value={sec.type} onChange={e => updateSection(idx, 'type', e.target.value)} className="input-field" style={{ width: 110, padding: '8px 12px' }}>
                    <option value="QUIZ">MCQ Quiz</option>
                    <option value="CODING">Coding Test</option>
                  </select>
                  <select value={sec.referenceId} onChange={e => updateSection(idx, 'referenceId', e.target.value)} className="input-field" style={{ flex: 1, padding: '8px 12px' }} required>
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

          <div style={{ display: 'flex', gap: 12, marginTop: 8 }}>
            <button type="button" onClick={onClose} className="btn-ghost" style={{ flex: 1, justifyContent: 'center' }}>Cancel</button>
            <button type="submit" disabled={loading} className="btn-primary" style={{ flex: 1, justifyContent: 'center' }}>
              {loading ? <Loader2 size={16} className="spin" /> : <Check size={16} />} Save Changes
            </button>
          </div>
        </form>
      </motion.div>
    </div>
  )
}

export default function AssessmentDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [assessment, setAssessment] = useState(null)
  const [loading, setLoading] = useState(true)
  const [showEdit, setShowEdit] = useState(false)
  const [deleting, setDeleting] = useState(false)
  const [quizzes, setQuizzes] = useState([])
  const [codingTests, setCodingTests] = useState([])
  const [sectionDetails, setSectionDetails] = useState([])
  const [submissions, setSubmissions] = useState([])

  const handleDelete = async () => {
    if (!window.confirm(`Delete "${assessment?.title}"? This will permanently remove all attempts and answers. This cannot be undone.`)) return
    setDeleting(true)
    try {
      await assessmentApi.delete(id)
      toast.success('Assessment deleted successfully')
      navigate('/admin/assessments')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to delete assessment')
      setDeleting(false)
    }
  }

  const fetchData = async () => {
    try {
      const [res, resQ, resC] = await Promise.all([
        assessmentApi.getById(id),
        adminQuizApi.getAll(),
        codingApi.getAll()
      ])
      const assessmentData = res.data.data
      setAssessment(assessmentData)
      setQuizzes(resQ.data.data || [])
      setCodingTests(resC.data.data || [])

      if (assessmentData.shareToken) {
        try {
          const detailRes = await assessmentApi.getByToken(assessmentData.shareToken)
          setSectionDetails(detailRes.data.data.sections || [])
        } catch (err) {
          console.error("Failed to load sections", err)
        }
      }

      // Load assessment submissions
      try {
        const subRes = await api.get(`/admin/assessments/${id}/results`)
        setSubmissions(subRes.data.data || [])
      } catch (err) {
        console.error("Failed to load submissions", err)
      }
    } catch {
      toast.error('Failed to load assessment')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchData() }, [id])

  const formatDate = (dt) => dt ? new Date(dt).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' }) : '—'

  if (loading) return (
    <Layout title="Assessment Detail">
      <div style={{ display: 'flex', justifyContent: 'center', padding: 80 }}><div className="spinner" /></div>
    </Layout>
  )

  if (!assessment) return (
    <Layout title="Not Found">
      <div style={{ textAlign: 'center', padding: 80, color: 'var(--text-sec)' }}>Assessment not found.</div>
    </Layout>
  )

  return (
    <Layout
      title={assessment.title}
      subtitle={assessment.description || 'Assessment details'}
      action={
        <div style={{ display: 'flex', gap: 12 }}>
          <Link to="/admin/assessments" className="btn-ghost" style={{ padding: '8px 16px', fontSize: 13, textDecoration: 'none' }}>
            <ArrowLeft size={14} /> Back
          </Link>
          <button onClick={() => setShowEdit(true)} className="btn-primary" style={{ padding: '8px 16px', fontSize: 13 }}>
            <Pencil size={14} /> Edit
          </button>
          <button
            onClick={handleDelete}
            disabled={deleting}
            style={{
              padding: '8px 16px', fontSize: 13, display: 'flex', alignItems: 'center', gap: 6,
              background: '#ef4444', color: '#fff', border: 'none', borderRadius: 8,
              cursor: deleting ? 'not-allowed' : 'pointer', opacity: deleting ? 0.7 : 1,
              fontWeight: 600
            }}
          >
            {deleting ? <Loader2 size={14} className="spin" /> : <Trash2 size={14} />}
            {deleting ? 'Deleting...' : 'Delete'}
          </button>
        </div>
      }
    >
      {/* Meta cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: 16, marginBottom: 32 }}>
        {[
          { icon: <Clock size={18} color="#a78bfa" />, label: 'Duration', value: `${assessment.durationMinutes} minutes` },
          { icon: <BookOpen size={18} color="#f472b6" />, label: 'Passing Cutoff', value: assessment.passingPercentage ? `${assessment.passingPercentage}%` : 'None' },
          { icon: <Calendar size={18} color="#38bdf8" />, label: 'Start', value: formatDate(assessment.scheduledFor) },
          { icon: <Calendar size={18} color="#f87171" />, label: 'End', value: formatDate(assessment.validUntil) },
          { icon: <Sparkles size={18} color="#4ade80" />, label: 'Status', value: assessment.active ? 'Active' : 'Inactive' },
        ].map(item => (
          <div key={item.label} className="card" style={{ padding: 20, display: 'flex', flexDirection: 'column', gap: 8 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              {item.icon}
              <span style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-sec)', textTransform: 'uppercase', letterSpacing: 1 }}>{item.label}</span>
            </div>
            <span style={{ fontSize: 16, fontWeight: 700, color: 'var(--text-main)' }}>{item.value}</span>
          </div>
        ))}
      </div>

      {/* Sections and Assessment Info */}
      <div className="card" style={{ padding: 24 }}>
        <h3 style={{ fontSize: 16, fontWeight: 700, color: 'var(--text-main)', marginBottom: 16 }}>Assessment Details</h3>
        
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10, marginBottom: 24 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', padding: '12px 0', borderBottom: '1px solid rgba(255,255,255,0.06)', fontSize: 13 }}>
            <span style={{ color: 'var(--text-sec)' }}>Total Sections</span>
            <span style={{ color: 'var(--text-main)', fontWeight: 600 }}>{sectionDetails.length}</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', padding: '12px 0', fontSize: 13 }}>
            <span style={{ color: 'var(--text-sec)' }}>Created</span>
            <span style={{ color: 'var(--text-main)' }}>{formatDate(assessment.createdAt)}</span>
          </div>
        </div>

        {sectionDetails.length > 0 && (
          <div>
            <h4 style={{ fontSize: 14, fontWeight: 600, color: 'var(--text-main)', marginBottom: 12 }}>Sections</h4>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              {sectionDetails.map((section, idx) => {
                let marks = 0;
                let details = '';
                if (section.type === 'QUIZ' && section.questions) {
                  marks = section.questions.reduce((sum, q) => sum + (q.marks || 0), 0);
                  details = `${section.questions.length} Questions`;
                } else if (section.type === 'CODING') {
                  marks = 20; // Fixed marks for coding test
                  details = `1 Coding Problem`;
                }
                
                return (
                  <div key={section.id || idx} style={{ padding: 16, borderRadius: 8, background: 'rgba(255,255,255,0.03)', border: '1px solid rgba(255,255,255,0.05)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 }}>
                      <div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                          <span style={{ fontSize: 12, fontWeight: 600, color: 'var(--primary)', padding: '2px 8px', borderRadius: 4, background: 'rgba(99, 102, 241, 0.1)' }}>
                            Section {section.order}
                          </span>
                          <span style={{ fontSize: 14, fontWeight: 600, color: 'var(--text-main)' }}>{section.title}</span>
                        </div>
                        <div style={{ fontSize: 12, color: 'var(--text-sec)' }}>{details} &bull; Type: {section.type}</div>
                      </div>
                      <div style={{ textAlign: 'right' }}>
                        <div style={{ fontSize: 16, fontWeight: 700, color: 'var(--text-main)' }}>{marks}</div>
                        <div style={{ fontSize: 11, color: 'var(--text-sec)', textTransform: 'uppercase' }}>Marks</div>
                      </div>
                    </div>
                    {section.description && <div style={{ fontSize: 13, color: 'var(--text-sec)', marginTop: 8 }}>{section.description}</div>}
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </div>

      {/* Submissions Table */}
      <div className="card" style={{ padding: 24, marginTop: 24 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 20 }}>
          <Users size={18} color="#a78bfa" />
          <h3 style={{ fontSize: 16, fontWeight: 700, color: 'var(--text-main)' }}>Student Submissions</h3>
          <span style={{ marginLeft: 'auto', fontSize: 12, color: 'var(--text-sec)', background: 'rgba(255,255,255,0.06)', padding: '3px 10px', borderRadius: 20 }}>
            {submissions.length} submitted
          </span>
        </div>

        {submissions.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '40px 0', color: 'var(--text-sec)', fontSize: 13 }}>
            No submissions yet for this assessment
          </div>
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  {['Student', 'Email', 'Score', 'Percentage', 'Status', 'Submitted At'].map(h => <th key={h}>{h}</th>)}
                </tr>
              </thead>
              <tbody>
                {submissions.map((s, i) => (
                  <motion.tr key={s.id || i} initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: i * 0.03 }}>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                        <div style={{ width: 28, height: 28, borderRadius: 8, background: 'linear-gradient(135deg,#7c3aed,#38bdf8)', color: '#fff', fontSize: 11, fontWeight: 700, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                          {s.studentName?.[0]?.toUpperCase()}
                        </div>
                        <span style={{ fontWeight: 500 }}>{s.studentName}</span>
                      </div>
                    </td>
                    <td style={{ color: 'var(--text-sec)', fontSize: 13 }}>{s.studentEmail}</td>
                    <td style={{ fontFamily: 'monospace', fontWeight: 700, color: 'var(--text-main)' }}>
                      {s.score ?? '—'} / {s.totalMarks ?? '—'}
                    </td>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <div className="progress-track" style={{ width: 60 }}>
                          <div className="progress-fill" style={{ width: `${Math.min(s.percentage || 0, 100)}%` }} />
                        </div>
                        <span style={{ fontWeight: 700, fontSize: 12 }} className="grad">{(s.percentage || 0).toFixed(1)}%</span>
                      </div>
                    </td>
                    <td>
                      {s.passed
                        ? <span className="badge badge-active"><CheckCircle2 size={10} style={{ marginRight: 4 }} /> Passed</span>
                        : <span className="badge badge-hard"><XCircle size={10} style={{ marginRight: 4 }} /> Failed</span>}
                    </td>
                    <td style={{ color: 'var(--text-sec)', fontSize: 12 }}>
                      {s.submittedAt ? new Date(s.submittedAt).toLocaleString('en-IN', { dateStyle: 'short', timeStyle: 'short' }) : '—'}
                    </td>
                  </motion.tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <AnimatePresence>
        {showEdit && (
          <EditAssessmentModal
            assessment={assessment}
            quizzes={quizzes}
            codingTests={codingTests}
            currentSections={sectionDetails}
            onClose={() => setShowEdit(false)}
            onSave={() => { setShowEdit(false); fetchData() }}
          />
        )}
      </AnimatePresence>
    </Layout>
  )
}
