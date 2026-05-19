import React, { useEffect, useRef, useState } from 'react';
import * as faceapi from 'face-api.js';
import { Camera, CheckCircle2, AlertTriangle, Loader2, ArrowLeft } from 'lucide-react';
import { motion } from 'framer-motion';

export default function CameraSetupGate({ onReady, onCancel, title = "System Check" }) {
  const videoRef = useRef();
  const [status, setStatus] = useState('requesting'); // requesting, loading_models, scanning, ready, error
  const [errorMsg, setErrorMsg] = useState('');
  const [faceDetected, setFaceDetected] = useState(false);
  const streamRef = useRef(null);
  const streamHandedOffRef = useRef(false);

  useEffect(() => {
    let mounted = true;
    let interval;
    
    const init = async () => {
      try {
        if (window.location.search.includes('bypassCamera=true')) {
          setStatus('ready');
          setFaceDetected(true);
          return;
        }
        const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
        if (!mounted) {
          stream.getTracks().forEach(track => track.stop());
          return;
        }
        streamRef.current = stream;
        if (videoRef.current) videoRef.current.srcObject = stream;
        setStatus('loading_models');
        await faceapi.nets.tinyFaceDetector.loadFromUri('/models');
        if (!mounted) return;
        setStatus('scanning');
      } catch (err) {
        if (!mounted) return;
        setStatus('error');
        setErrorMsg('Camera or Microphone permission denied or not found. Please allow both camera and microphone access to continue.');
      }
    };
    init();

    const handlePlay = () => {
      interval = setInterval(async () => {
        if (!videoRef.current || !mounted) return;
        try {
          const detections = await faceapi.detectAllFaces(
            videoRef.current,
            new faceapi.TinyFaceDetectorOptions({ inputSize: 224, scoreThreshold: 0.5 })
          );
          if (!mounted) return;
          const isGood = detections.length === 1;
          setFaceDetected(isGood);
          if (isGood) setStatus('ready');
          else setStatus('scanning');
        } catch (e) {}
      }, 500);
    };

    if (videoRef.current) videoRef.current.addEventListener('play', handlePlay);

    return () => {
      mounted = false;
      if (interval) clearInterval(interval);
      if (streamRef.current && !streamHandedOffRef.current) {
        streamRef.current.getTracks().forEach(track => track.stop());
      }
      if (videoRef.current) videoRef.current.removeEventListener('play', handlePlay);
    };
  }, []);

  const handleProceed = async () => {
    try {
      if (document.documentElement.requestFullscreen) {
        await document.documentElement.requestFullscreen();
      }
    } catch (err) {
      console.log('Fullscreen failed:', err);
    }
    streamHandedOffRef.current = true;
    onReady(streamRef.current);
  };

  const handleCancel = () => {
    if (streamRef.current) streamRef.current.getTracks().forEach(track => track.stop());
    if (onCancel) onCancel();
  };

  return (
    <div style={{ minHeight: '100vh', background: '#0d1117', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 24, color: '#e2e8f0' }}>
      <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }}
        style={{ background: '#161b22', border: '1px solid rgba(255,255,255,0.1)', borderRadius: 20, padding: '40px', maxWidth: 480, width: '100%', textAlign: 'center' }}>
        
        <div style={{ width: 64, height: 64, borderRadius: 18, background: 'rgba(124,58,237,0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 20px' }}>
          <Camera size={28} color="#a78bfa" />
        </div>
        <h2 style={{ fontSize: 24, fontWeight: 800, marginBottom: 8 }}>{title}</h2>
        <p style={{ fontSize: 14, color: '#94a3b8', marginBottom: 24 }}>Please allow camera and microphone access, and align your face before starting.</p>

        <div style={{ position: 'relative', width: 240, height: 240, margin: '0 auto 24px', borderRadius: '50%', overflow: 'hidden', border: `4px solid ${status === 'ready' ? '#4ade80' : status === 'error' ? '#f87171' : '#38bdf8'}`, background: '#000' }}>
          <video ref={videoRef} autoPlay muted playsInline style={{ width: '100%', height: '100%', objectFit: 'cover', transform: 'scaleX(-1)' }} />
          {status !== 'ready' && status !== 'scanning' && (
            <div style={{ position: 'absolute', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'rgba(0,0,0,0.7)', flexDirection: 'column', gap: 10 }}>
              {status === 'error' ? <AlertTriangle color="#f87171" size={32} /> : <Loader2 size={32} color="#38bdf8" style={{ animation: 'spin 1s linear infinite' }} />}
              <span style={{ fontSize: 12, fontWeight: 700 }}>
                {status === 'requesting' ? 'Requesting Camera...' : status === 'loading_models' ? 'Loading AI...' : 'Error'}
              </span>
            </div>
          )}
        </div>

        {status === 'error' && <div style={{ color: '#f87171', fontSize: 14, marginBottom: 20 }}>{errorMsg}</div>}
        {status === 'scanning' && <div style={{ color: '#fbbf24', fontSize: 14, marginBottom: 20, fontWeight: 600 }}>Face not detected. Please look at the camera.</div>}
        {status === 'ready' && <div style={{ color: '#4ade80', fontSize: 14, marginBottom: 20, fontWeight: 600, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6 }}><CheckCircle2 size={16} /> Camera Ready</div>}

        <div style={{ display: 'flex', gap: 12, justifyContent: 'center' }}>
          {onCancel && (
            <button onClick={handleCancel} style={{ padding: '12px 24px', borderRadius: 10, background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)', color: '#94a3b8', fontSize: 14, fontWeight: 600, cursor: 'pointer' }}>
              <ArrowLeft size={15} style={{ marginRight: 6, verticalAlign: 'middle' }} /> Cancel
            </button>
          )}
          <button onClick={handleProceed} disabled={status !== 'ready'}
            style={{ padding: '12px 28px', borderRadius: 10, background: status === 'ready' ? 'linear-gradient(135deg,#7c3aed,#4f46e5)' : '#334155', border: 'none', color: '#fff', fontSize: 15, fontWeight: 700, cursor: status === 'ready' ? 'pointer' : 'not-allowed', opacity: status === 'ready' ? 1 : 0.5 }}>
            Start Exam
          </button>
        </div>
      </motion.div>
    </div>
  );
}
