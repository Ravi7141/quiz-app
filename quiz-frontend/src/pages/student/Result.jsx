import { useParams } from 'react-router-dom'
import { motion } from 'framer-motion'
import { CheckCircle2 } from 'lucide-react'

export default function Result() {
  const { attemptId } = useParams()

  return (
    <div style={{ minHeight: '100vh', background: '#0d1117', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 24 }}>
      <motion.div initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }}
        style={{ background: '#161b22', border: '1px solid rgba(255,255,255,0.1)', borderRadius: 20, padding: '48px 32px', maxWidth: 500, width: '100%', textAlign: 'center' }}>
        
        <div style={{ width: 80, height: 80, borderRadius: '50%', background: 'rgba(74,222,128,0.1)', border: '1px solid rgba(74,222,128,0.3)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 24px' }}>
          <CheckCircle2 size={40} color="#4ade80" />
        </div>
        
        <h1 style={{ fontSize: 26, fontWeight: 800, color: '#fff', marginBottom: 12 }}>
          Thank You!
        </h1>
        
        <p style={{ fontSize: 16, color: '#94a3b8', lineHeight: 1.6 }}>
          Your exam has been successfully submitted.
        </p>
        
        <div style={{ marginTop: 32, padding: 16, background: 'rgba(255,255,255,0.03)', borderRadius: 12, border: '1px solid rgba(255,255,255,0.05)' }}>
          <p style={{ fontSize: 14, color: '#e2e8f0', fontWeight: 600 }}>
            Results will be declared shortly.
          </p>
          <p style={{ fontSize: 13, color: '#64748b', marginTop: 4 }}>
            You may now safely close this window.
          </p>
        </div>

      </motion.div>
    </div>
  )
}
