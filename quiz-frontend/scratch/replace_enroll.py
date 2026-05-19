import os

file_path = r"c:\Users\nayak\H\Vault_Project\quiz-app\quiz-frontend\src\pages\student\UnifiedAssessment.jsx"

with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

target = """  if (!activeStudentId) {
    return (
      <div style={{ minHeight: '100vh', background: '#0a0d14', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 24, color: '#e2e8f0' }}>
        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }}
          style={{ background: '#111827', border: '1px solid rgba(255,255,255,0.08)', borderRadius: 24, padding: '40px', maxWidth: 480, width: '100%', boxShadow: '0 20px 50px rgba(0,0,0,0.5)', position: 'relative' }}>
          
          <div style={{ width: 64, height: 64, borderRadius: 18, background: 'rgba(124,58,237,0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 20px' }}>
            <ShieldAlert size={28} color="#a78bfa" />
          </div>
          <h2 style={{ fontSize: 24, fontWeight: 800, marginBottom: 8, textAlign: 'center', color: '#fff' }}>Candidate Details</h2>
          <p style={{ fontSize: 14, color: '#94a3b8', marginBottom: 28, textAlign: 'center' }}>Please enter your details to start the assessment environment.</p>

          <form onSubmit={handleCandidateSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 20, textAlign: 'left' }}>
            <div>
              <label style={{ display: 'block', fontSize: 13.5, fontWeight: 600, color: '#e2e8f0', marginBottom: 8 }}>Full Name *</label>
              <input
                type="text" required value={candidateForm.name}
                onChange={e => setCandidateForm(f => ({ ...f, name: e.target.value }))}
                placeholder="John Doe"
                style={{ width: '100%', padding: '12px 16px', borderRadius: 10, background: '#1f2937', border: '1px solid rgba(255,255,255,0.1)', color: '#fff', fontSize: 14.5, outline: 'none' }}
              />
            </div>
            <div>
              <label style={{ display: 'block', fontSize: 13.5, fontWeight: 600, color: '#e2e8f0', marginBottom: 8 }}>Email Address *</label>
              <input
                type="email" required value={candidateForm.email}
                onChange={e => setCandidateForm(f => ({ ...f, email: e.target.value }))}
                placeholder="john@example.com"
                style={{ width: '100%', padding: '12px 16px', borderRadius: 10, background: '#1f2937', border: '1px solid rgba(255,255,255,0.1)', color: '#fff', fontSize: 14.5, outline: 'none' }}
              />
            </div>
            <div>
              <label style={{ display: 'block', fontSize: 13.5, fontWeight: 600, color: '#e2e8f0', marginBottom: 8 }}>Phone Number (Optional)</label>
              <input
                type="tel" value={candidateForm.phone}
                onChange={e => setCandidateForm(f => ({ ...f, phone: e.target.value }))}
                placeholder="1234567890"
                style={{ width: '100%', padding: '12px 16px', borderRadius: 10, background: '#1f2937', border: '1px solid rgba(255,255,255,0.1)', color: '#fff', fontSize: 14.5, outline: 'none' }}
              />
            </div>

            <button type="submit" disabled={candidateSubmitting}
              style={{ width: '100%', padding: '14px 28px', borderRadius: 10, background: 'linear-gradient(135deg,#7c3aed,#4f46e5)', border: 'none', color: '#fff', fontSize: 15, fontWeight: 700, cursor: candidateSubmitting ? 'not-allowed' : 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8, marginTop: 12, opacity: candidateSubmitting ? 0.7 : 1 }}>
              {candidateSubmitting ? <Loader2 size={16} className="animate-spin" style={{ animation: 'spin 1s linear infinite' }} /> : null} Proceed to Exam
            </button>
          </form>
        </motion.div>
      </div>
    )
  }"""

replacement = """  if (!activeStudentId) {
    return (
      <div style={{ minHeight: '100vh', background: '#0a0d14', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 24, color: '#e2e8f0' }}>
        <div style={{ textAlign: 'center' }}>
          <Loader2 size={48} style={{ animation: 'spin 1s linear infinite', color: '#a78bfa', margin: '0 auto 16px' }} />
          <h3 style={{ fontSize: 18, fontWeight: 700, color: '#fff', marginBottom: 6 }}>Preparing Exam Session</h3>
          <p style={{ fontSize: 13, color: '#94a3b8' }}>Please wait while we initialize your secure proctoring environment...</p>
        </div>
      </div>
    )
  }"""

# Normalize line endings
content_norm = content.replace("\\r\\n", "\\n").replace("\\r", "\\n")
target_norm = target.replace("\\r\\n", "\\n").replace("\\r", "\\n")
replacement_norm = replacement.replace("\\r\\n", "\\n").replace("\\r", "\\n")

if target_norm in content_norm:
    new_content = content_norm.replace(target_norm, replacement_norm)
    with open(file_path, "w", encoding="utf-8", newline="") as f:
        f.write(new_content)
    print("SUCCESS")
else:
    # Try exact match with original content
    if target in content:
        new_content = content.replace(target, replacement)
        with open(file_path, "w", encoding="utf-8") as f:
            f.write(new_content)
        print("SUCCESS EXACT")
    else:
        print("TARGET NOT FOUND IN FILE")
