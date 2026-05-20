import { motion } from 'framer-motion'
import { CheckCircle2 } from 'lucide-react'

export default function Result() {
  return (
    <div style={{ minHeight: '100vh', background: '#0a0d14', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 24 }}>
      <motion.div initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }}
        style={{ background: '#161b22', border: '1px solid rgba(255,255,255,0.1)', borderRadius: 20, padding: '56px 40px', maxWidth: 480, width: '100%', textAlign: 'center' }}>

        <div style={{ width: 88, height: 88, borderRadius: '50%', background: 'rgba(74,222,128,0.1)', border: '1px solid rgba(74,222,128,0.3)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 28px' }}>
          <CheckCircle2 size={44} color="#4ade80" />
        </div>

        <h1 style={{ fontSize: 28, fontWeight: 800, color: '#fff', marginBottom: 12 }}>
          Submitted Successfully!
        </h1>

        <p style={{ fontSize: 15, color: '#94a3b8', lineHeight: 1.7, marginBottom: 32 }}>
          Your exam has been recorded. Thank you for completing the assessment.
        </p>

        <div style={{ padding: '20px 24px', background: 'rgba(167,139,250,0.07)', borderRadius: 12, border: '1px solid rgba(167,139,250,0.2)' }}>
          <p style={{ fontSize: 15, color: '#a78bfa', fontWeight: 700, marginBottom: 6 }}>
            🕐 Results will be declared soon
          </p>
          <p style={{ fontSize: 13, color: '#64748b' }}>
            You may now safely close this window.
          </p>
        </div>

      </motion.div>
    </div>
  )
}
