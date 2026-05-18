import { useState, useEffect } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { X, Link as LinkIcon, QrCode, Copy, CheckCircle2 } from 'lucide-react'
import { examTokenApi } from '../api/axios'
import { QRCodeSVG } from 'qrcode.react'
import toast from 'react-hot-toast'

export default function ShareLinkModal({ isOpen, onClose, examId, examType }) {
  const [emails, setEmails] = useState('')
  const [generating, setGenerating] = useState(false)
  const [tokens, setTokens] = useState([])
  const [loading, setLoading] = useState(false)
  const [showQrToken, setShowQrToken] = useState(null)

  useEffect(() => {
    if (isOpen) fetchTokens()
  }, [isOpen])

  const fetchTokens = async () => {
    setLoading(true)
    try {
      const res = await examTokenApi.getForExam(examType, examId)
      setTokens(res.data.data)
    } catch (err) {
      toast.error('Failed to load existing links')
    } finally {
      setLoading(false)
    }
  }

  const handleGenerate = async () => {
    const lines = emails.split('\n').map(e => e.trim()).filter(Boolean)
    if (!lines.length) return toast.error('Enter at least one line')

    setGenerating(true)
    try {
      // Send raw lines to backend; it will handle parsing names, emails, and phones
      const res = await examTokenApi.generate({ examId, examType, emails: lines })
      toast.success(`Generated ${res.data.data.length} unique links`)
      setEmails('')
      fetchTokens()
    } catch (err) {
      toast.error('Failed to generate links')
    } finally {
      setGenerating(false)
    }
  }

  const getLink = (token) => `${window.location.origin}/exam/entry/${token}`

  const handleCopyLink = (token) => {
    navigator.clipboard.writeText(getLink(token))
    toast.success('Link copied!')
  }

  const handleCopyDetails = (t) => {
    const text = `Hi ${t.studentEmail},\n\nHere is your private link to access the assessment: ${t.examTitle}\n\nLink: ${getLink(t.token)}\n\nDo not share this link with anyone, it is strictly tied to your email.\nGood luck!`
    navigator.clipboard.writeText(text)
    toast.success('Details copied!')
  }

  const handleCopyAll = () => {
    const available = tokens.filter(t => !t.isUsed)
    if (!available.length) return toast.error('No available links to copy')
    const text = available.map(t => `${t.studentEmail}: ${getLink(t.token)}`).join('\n')
    navigator.clipboard.writeText(text)
    toast.success(`Copied ${available.length} links to clipboard!`)
  }

  return (
    <AnimatePresence>
      {isOpen && (
        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
          style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.7)', zIndex: 9999, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 24 }}>
          <motion.div initial={{ scale: 0.95, y: 20 }} animate={{ scale: 1, y: 0 }} exit={{ scale: 0.95, y: 20 }}
            style={{ background: '#161b22', border: '1px solid rgba(255,255,255,0.1)', borderRadius: 20, width: '100%', maxWidth: 700, maxHeight: '90vh', display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
            
            <div style={{ padding: '20px 24px', borderBottom: '1px solid rgba(255,255,255,0.05)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h2 style={{ color: '#fff', fontSize: 18, fontWeight: 700 }}>Share Private Exam Links</h2>
              <button onClick={onClose} style={{ background: 'none', border: 'none', color: '#94a3b8', cursor: 'pointer' }}><X size={20} /></button>
            </div>

            <div style={{ padding: 24, overflowY: 'auto' }}>
              <div style={{ marginBottom: 24 }}>
                <label style={{ display: 'block', fontSize: 13, fontWeight: 600, color: '#94a3b8', marginBottom: 8 }}>Bulk Import (Enter "Name, Email, Phone" or just "Email")</label>
                <textarea 
                  value={emails}
                  onChange={e => setEmails(e.target.value)}
                  placeholder="John Doe, john@example.com, 1234567890&#10;alice@test.com"
                  style={{ width: '100%', height: 100, background: '#0d1117', border: '1px solid rgba(255,255,255,0.1)', borderRadius: 10, padding: 12, color: '#fff', fontSize: 14, resize: 'none', marginBottom: 12 }}
                />
                <button onClick={handleGenerate} disabled={generating}
                  style={{ padding: '10px 20px', background: '#7c3aed', color: '#fff', borderRadius: 8, border: 'none', fontWeight: 600, cursor: generating ? 'not-allowed' : 'pointer', opacity: generating ? 0.7 : 1 }}>
                  {generating ? 'Generating...' : 'Generate Links'}
                </button>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                <h3 style={{ color: '#e2e8f0', fontSize: 15, fontWeight: 600 }}>Generated Tokens</h3>
                {tokens.length > 0 && (
                  <button onClick={handleCopyAll}
                    style={{ padding: '6px 12px', background: 'rgba(56,189,248,0.1)', color: '#38bdf8', borderRadius: 6, border: '1px solid rgba(56,189,248,0.2)', fontSize: 13, fontWeight: 600, cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 6 }}>
                    <Copy size={14} /> Copy All Links
                  </button>
                )}
              </div>
              {loading ? (
                <div style={{ color: '#64748b', textAlign: 'center', padding: 20 }}>Loading...</div>
              ) : tokens.length === 0 ? (
                <div style={{ color: '#64748b', textAlign: 'center', padding: 20, background: 'rgba(255,255,255,0.02)', borderRadius: 10 }}>No links generated yet.</div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                  {tokens.map(t => (
                    <div key={t.token} style={{ background: 'rgba(255,255,255,0.03)', border: '1px solid rgba(255,255,255,0.05)', borderRadius: 10, padding: 16 }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                        <div>
                          <div style={{ color: '#fff', fontWeight: 600, fontSize: 14 }}>{t.studentEmail}</div>
                          <div style={{ color: t.isUsed ? '#ef4444' : '#4ade80', fontSize: 12, fontWeight: 600 }}>{t.isUsed ? 'Already Used' : 'Available'}</div>
                        </div>
                        <div style={{ display: 'flex', gap: 8 }}>
                          <button onClick={() => handleCopyLink(t.token)} disabled={t.isUsed} title="Copy Link"
                            style={{ padding: 8, background: 'rgba(255,255,255,0.05)', border: 'none', borderRadius: 6, color: t.isUsed ? '#64748b' : '#38bdf8', cursor: t.isUsed ? 'not-allowed' : 'pointer' }}>
                            <LinkIcon size={16} />
                          </button>
                          <button onClick={() => setShowQrToken(showQrToken === t.token ? null : t.token)} disabled={t.isUsed} title="Show QR Code"
                            style={{ padding: 8, background: 'rgba(255,255,255,0.05)', border: 'none', borderRadius: 6, color: t.isUsed ? '#64748b' : '#a78bfa', cursor: t.isUsed ? 'not-allowed' : 'pointer' }}>
                            <QrCode size={16} />
                          </button>
                          <button onClick={() => handleCopyDetails(t)} disabled={t.isUsed} title="Copy Details"
                            style={{ padding: 8, background: 'rgba(255,255,255,0.05)', border: 'none', borderRadius: 6, color: t.isUsed ? '#64748b' : '#f59e0b', cursor: t.isUsed ? 'not-allowed' : 'pointer' }}>
                            <Copy size={16} />
                          </button>
                        </div>
                      </div>
                      
                      {showQrToken === t.token && !t.isUsed && (
                        <div style={{ marginTop: 16, display: 'flex', flexDirection: 'column', alignItems: 'center', background: '#fff', padding: 16, borderRadius: 10 }}>
                          <QRCodeSVG value={getLink(t.token)} size={150} />
                          <div style={{ color: '#000', fontSize: 12, marginTop: 12, fontWeight: 600 }}>Scan to start exam</div>
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  )
}
