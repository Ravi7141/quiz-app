import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { motion, AnimatePresence } from 'framer-motion'
import Layout from '../../components/Layout'
import { questionApi, studentQuizApi } from '../../api/axios'
import toast from 'react-hot-toast'
import { Plus, Pencil, Trash2, HelpCircle, X, Loader2, Check, ArrowLeft, UploadCloud } from 'lucide-react'

const OPTIONS = ['A', 'B', 'C', 'D']

function QuestionModal({ question, quizId, onClose, onSave }) {
  const [form, setForm] = useState({ quizId: Number(quizId), questionText: '', optionA: '', optionB: '', optionC: '', optionD: '', correctAnswer: 'A', marks: 1, ...question })
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const payload = { ...form, marks: Number(form.marks), quizId: Number(quizId) }
      if (question?.id) { await questionApi.update(question.id, payload); toast.success('Updated!'); }
      else { await questionApi.add(payload); toast.success('Added!'); }
      onSave()
    } catch (err) { toast.error(err.response?.data?.message || 'Error saving question') }
    finally { setLoading(false) }
  }

  return (
    <div className="modal-overlay" onClick={onClose} style={{ overflowY: 'auto', alignItems: 'flex-start' }}>
      <motion.div initial={{ scale: 0.95, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} exit={{ scale: 0.95, opacity: 0 }} className="modal-box" style={{ margin: '40px auto', maxWidth: 600 }} onClick={e => e.stopPropagation()}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 24 }}>
          <h2 style={{ fontSize: 20, fontWeight: 700, color: 'var(--text-main)' }}>{question?.id ? 'Edit Question' : 'Add Question'}</h2>
          <button onClick={onClose} style={{ background: 'none', border: 'none', color: 'var(--text-sec)', cursor: 'pointer' }}><X size={20} /></button>
        </div>
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <div><label style={{ display: 'block', fontSize: 13, fontWeight: 600, color: 'var(--text-sec)', marginBottom: 8 }}>Question Text *</label>
          <textarea required rows={3} value={form.questionText} onChange={e => setForm(f => ({ ...f, questionText: e.target.value }))} className="input-field" style={{ resize: 'none' }} /></div>
          {OPTIONS.map(opt => (
            <div key={opt}><label style={{ display: 'block', fontSize: 13, fontWeight: 600, color: 'var(--text-sec)', marginBottom: 8 }}>Option {opt} *</label>
            <input required value={form[`option${opt}`]} onChange={e => setForm(f => ({ ...f, [`option${opt}`]: e.target.value }))} className="input-field" /></div>
          ))}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
            <div><label style={{ display: 'block', fontSize: 13, fontWeight: 600, color: 'var(--text-sec)', marginBottom: 8 }}>Correct Answer *</label>
            <select value={form.correctAnswer} onChange={e => setForm(f => ({ ...f, correctAnswer: e.target.value }))} className="input-field" style={{ cursor: 'pointer' }}>
              {OPTIONS.map(o => <option key={o} value={o}>Option {o}</option>)}
            </select></div>
            <div><label style={{ display: 'block', fontSize: 13, fontWeight: 600, color: 'var(--text-sec)', marginBottom: 8 }}>Marks</label>
            <input type="number" min="1" value={form.marks} onChange={e => setForm(f => ({ ...f, marks: e.target.value }))} className="input-field" /></div>
          </div>
          <div style={{ display: 'flex', gap: 12, marginTop: 16 }}>
            <button type="button" onClick={onClose} className="btn-ghost" style={{ flex: 1, justifyContent: 'center' }}>Cancel</button>
            <button type="submit" disabled={loading} className="btn-primary" style={{ flex: 1, justifyContent: 'center' }}>{loading ? <Loader2 size={16} className="spin" /> : <Check size={16} />} {question?.id ? 'Update' : 'Add'}</button>
          </div>
        </form>
      </motion.div>
    </div>
  )
}

export default function ManageQuestions() {
  const { id: quizId } = useParams()
  const [questions, setQuestions] = useState([])
  const [quiz, setQuiz] = useState(null)
  const [loading, setLoading] = useState(true)
  const [modal, setModal] = useState(null)
  const [importModal, setImportModal] = useState(false)
  const [deleting, setDeleting] = useState(null)

  const fetchData = () => {
    Promise.all([questionApi.getForAdmin(quizId), studentQuizApi.getById(quizId).catch(() => ({ data: { data: null } }))])
      .then(([qs, qz]) => { setQuestions(qs.data.data || []); setQuiz(qz.data.data); })
      .finally(() => setLoading(false))
  }
  useEffect(() => { fetchData() }, [quizId])

  const handleDelete = async (id) => {
    if (!confirm('Delete this question?')) return
    setDeleting(id)
    try { await questionApi.delete(id); toast.success('Question deleted'); fetchData(); }
    catch { toast.error('Delete failed'); setDeleting(null); }
  }

  return (
    <Layout title={quiz?.title || 'Questions'} subtitle={`${questions.length} questions`}
      action={
        <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
          <Link to="/admin/quizzes" className="btn-ghost" style={{ padding: '8px 16px', fontSize: 13, textDecoration: 'none' }}><ArrowLeft size={14} /> Back</Link>
          <button onClick={() => setImportModal(true)} className="btn-ghost" style={{ padding: '8px 16px', fontSize: 13 }}><UploadCloud size={14} /> Import Questions</button>
          <button onClick={() => setModal({})} className="btn-primary" style={{ padding: '8px 16px', fontSize: 13 }}><Plus size={14} /> Add Question</button>
        </div>
      }>
      {loading ? <div style={{ display: 'flex', justifyContent: 'center', padding: 80 }}><div className="spinner" /></div>
      : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          {questions.map((q, i) => (
            <motion.div key={q.id} initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.05 }} className="card" style={{ padding: 24 }}>
              <div style={{ display: 'flex', alignItems: 'flex-start', gap: 16 }}>
                <div style={{ width: 32, height: 32, borderRadius: 8, background: 'rgba(124,58,237,0.1)', color: '#a78bfa', fontSize: 12, fontWeight: 700, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                  Q{i + 1}
                </div>
                <div style={{ flex: 1 }}>
                  <p style={{ fontSize: 15, fontWeight: 600, color: 'var(--text-main)', lineHeight: 1.5, marginBottom: 20 }}>{q.questionText}</p>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 20 }}>
                    {OPTIONS.map(opt => (
                      <div key={opt} style={{ display: 'flex', alignItems: 'center', gap: 10, padding: 12, borderRadius: 8, background: q.correctAnswer === opt ? 'rgba(56,189,248,0.1)' : 'rgba(255,255,255,0.02)', border: `1px solid ${q.correctAnswer === opt ? 'rgba(56,189,248,0.3)' : 'transparent'}` }}>
                        <div style={{ width: 20, height: 20, borderRadius: 6, background: q.correctAnswer === opt ? '#38bdf8' : 'rgba(255,255,255,0.1)', color: q.correctAnswer === opt ? '#fff' : '#94a3b8', fontSize: 11, fontWeight: 700, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>{opt}</div>
                        <span style={{ fontSize: 13, color: q.correctAnswer === opt ? '#e0f2fe' : '#cbd5e1' }}>{q[`option${opt}`]}</span>
                      </div>
                    ))}
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', paddingTop: 16, borderTop: '1px solid rgba(255,255,255,0.06)' }}>
                    <span className="badge badge-active">Correct Answer: {q.correctAnswer} • {q.marks || 1} mark{q.marks !== 1 ? 's' : ''}</span>
                    <div style={{ display: 'flex', gap: 8 }}>
                      <button onClick={() => setModal(q)} style={{ width: 32, height: 32, borderRadius: 8, background: 'rgba(255,255,255,0.05)', color: 'var(--text-sec)', display: 'flex', alignItems: 'center', justifyContent: 'center', border: 'none', cursor: 'pointer' }}><Pencil size={14} /></button>
                      <button onClick={() => handleDelete(q.id)} disabled={deleting === q.id} style={{ width: 32, height: 32, borderRadius: 8, background: 'rgba(239,68,68,0.1)', color: '#f87171', display: 'flex', alignItems: 'center', justifyContent: 'center', border: 'none', cursor: 'pointer' }}>
                        {deleting === q.id ? <Loader2 size={14} className="spin" /> : <Trash2 size={14} />}
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </motion.div>
          ))}
          {questions.length === 0 && <div style={{ textAlign: 'center', padding: 80, color: 'var(--text-sec)' }}><HelpCircle size={40} style={{ margin: '0 auto 12px', opacity: 0.3 }} /><p>No questions added yet.</p></div>}
        </div>
      )}
      <AnimatePresence>
        {modal !== null && <QuestionModal question={modal?.id ? modal : null} quizId={quizId} onClose={() => setModal(null)} onSave={() => { setModal(null); fetchData(); }} />}
        {importModal && <ImportModal quizId={quizId} onClose={() => setImportModal(false)} onImported={() => { setImportModal(false); fetchData(); }} />}
      </AnimatePresence>
    </Layout>
  )
}

function ImportModal({ quizId, onClose, onImported }) {
  const [content, setContent] = useState('')
  const [fileName, setFileName] = useState('')
  const [loading, setLoading] = useState(false)

  const handleFile = async (event) => {
    const file = event.target.files?.[0]
    if (!file) return
    setFileName(file.name)
    const fileText = await file.text()
    setContent(fileText)
  }

  const parseQuestions = () => {
    const trimmed = content.trim()
    if (!trimmed) {
      throw new Error('Paste JSON or CSV text for import.')
    }

    let rawQuestions = []
    if (trimmed.startsWith('[')) {
      rawQuestions = JSON.parse(trimmed)
    } else {
      const lines = trimmed.split(/\r?\n/).filter(line => line.trim())
      rawQuestions = lines.map((line, index) => {
        const parts = line.split(',').map(part => part.trim())
        if (parts.length < 7) {
          throw new Error(`Line ${index + 1} requires 7 values: questionText, optionA, optionB, optionC, optionD, correctAnswer, marks`)
        }
        return {
          questionText: parts[0],
          optionA: parts[1],
          optionB: parts[2],
          optionC: parts[3],
          optionD: parts[4],
          correctAnswer: parts[5].toUpperCase(),
          marks: Number(parts[6]) || 1,
          quizId: Number(quizId),
        }
      })
    }

    if (!Array.isArray(rawQuestions) || rawQuestions.length === 0) {
      throw new Error('No valid questions found for import.')
    }

    return rawQuestions.map((question, index) => ({
      ...question,
      quizId: Number(quizId),
      marks: Number(question.marks || 1),
      correctAnswer: String(question.correctAnswer).toUpperCase(),
    }))
  }

  const handleImport = async () => {
    setLoading(true)
    try {
      const questions = parseQuestions()
      await questionApi.bulkAdd(quizId, questions)
      toast.success(`${questions.length} questions imported successfully`)
      onImported()
    } catch (err) {
      toast.error(err.response?.data?.message || err.message || 'Import failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose} style={{ overflowY: 'auto', alignItems: 'flex-start' }}>
      <motion.div initial={{ scale: 0.95, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} exit={{ scale: 0.95, opacity: 0 }} className="modal-box" style={{ margin: '40px auto', maxWidth: 680 }} onClick={e => e.stopPropagation()}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 24 }}>
          <h2 style={{ fontSize: 20, fontWeight: 700, color: 'var(--text-main)' }}>Import Questions</h2>
          <button onClick={onClose} style={{ background: 'none', border: 'none', color: 'var(--text-sec)', cursor: 'pointer' }}><X size={20} /></button>
        </div>
        <div style={{ display: 'grid', gap: 16 }}>
          <div>
            <label style={{ display: 'block', fontSize: 13, fontWeight: 600, color: 'var(--text-sec)', marginBottom: 8 }}>Paste JSON or CSV</label>
            <textarea rows={10} value={content} onChange={e => setContent(e.target.value)} className="input-field" style={{ resize: 'vertical', minHeight: 240 }} placeholder={`Paste JSON array or CSV rows:
questionText,optionA,optionB,optionC,optionD,correctAnswer,marks`} />
          </div>
          <label style={{ display: 'block', fontSize: 13, fontWeight: 600, color: 'var(--text-sec)' }}>
            Or upload a file
            <input type="file" accept=".json,.txt" onChange={handleFile} style={{ display: 'block', marginTop: 10 }} />
            {fileName && <div style={{ marginTop: 8, color: 'var(--text-main)', fontSize: 13 }}>Loaded: {fileName}</div>}
          </label>
          <div style={{ padding: 16, borderRadius: 16, background: 'rgba(255,255,255,0.04)' }}>
            <p style={{ marginBottom: 8, fontWeight: 700 }}>CSV format</p>
            <p style={{ fontSize: 13, color: 'var(--text-sec)', lineHeight: 1.7 }}>
              Each row should contain: questionText, optionA, optionB, optionC, optionD, correctAnswer, marks.
            </p>
          </div>
          <div style={{ display: 'flex', gap: 12, marginTop: 8 }}>
            <button onClick={onClose} className="btn-ghost" style={{ flex: 1, justifyContent: 'center' }}>Cancel</button>
            <button onClick={handleImport} disabled={loading} className="btn-primary" style={{ flex: 1, justifyContent: 'center' }}>
              {loading ? <Loader2 size={16} className="spin" /> : <Check size={16} />} Import
            </button>
          </div>
        </div>
      </motion.div>
    </div>
  )
}
