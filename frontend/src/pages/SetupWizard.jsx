import { useState, useEffect } from 'react';
import { useNavigate, Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { Bot, Phone, Upload, CheckCircle } from 'lucide-react';
import CSVUploader from '../components/CSVUploader';
import { useToast } from '../components/Toast';
import api from '../utils/api';

export default function SetupWizard() {
  const [step, setStep] = useState(1);
  const [agentName, setAgentName] = useState('Rahul');
  const [transferNumber, setTransferNumber] = useState('');
  const [file, setFile] = useState(null);
  const [launching, setLaunching] = useState(false);
  const navigate = useNavigate();
  const { user, updateUser } = useAuth();
  const { success, error: showError } = useToast();

  if (user?.setupCompleted) {
    return <Navigate to="/dashboard" replace />;
  }

  const handleNext = () => setStep(s => s + 1);
  const handleLaunch = async () => {
    if (!agentName.trim()) { showError('Please enter an agent name'); return; }
    if (!transferNumber.trim()) { showError('Please enter a transfer number'); return; }
    setLaunching(true);
    try {
      // 1. Complete Agency Setup & create ElevenLabs agent
      await api.post('/api/agency/setup', {
        agentName: agentName,
        transferNumber: transferNumber,
        emergencyNumber: null
      });
      
      // 2. If a CSV was uploaded, create and start the campaign automatically
      if (file) {
        const formData = new FormData();
        const campaignData = { 
          name: "Initial AI Campaign", 
          deliveryDate: new Date().toISOString().split('T')[0] 
        };
        formData.append('campaign', new Blob([JSON.stringify(campaignData)], { type: 'application/json' }));
        formData.append('file', file);
        
        const campRes = await api.post('/api/campaigns', formData, { 
          headers: { 'Content-Type': 'multipart/form-data' } 
        });
        
        // Start the calls!
        await api.post(`/api/campaigns/${campRes.data.id}/start`);
      }

      // Update auth context so the button hides
      updateUser({ ...user, setupCompleted: true });

      success('Setup complete! Campaign launched successfully.');
      navigate('/dashboard');
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to complete setup';
      showError(msg);
    } finally {
      setLaunching(false);
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '40px' }}>
      <div className="glass-card animate-scale-in" style={{ width: '100%', maxWidth: '700px', padding: '40px' }}>
        {/* Progress Bar */}
        <div style={{ display: 'flex', marginBottom: '40px', position: 'relative' }}>
          <div style={{ position: 'absolute', top: '15px', left: 0, right: 0, height: '4px', background: 'var(--bg-tertiary)', zIndex: 0 }} />
          <div style={{ position: 'absolute', top: '15px', left: 0, width: step === 1 ? '33%' : step === 2 ? '66%' : '100%', height: '4px', background: 'var(--gradient-primary)', transition: 'width 0.5s ease', zIndex: 1 }} />
          
          {[1, 2, 3].map(num => (
            <div key={num} style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', zIndex: 2 }}>
              <div style={{ 
                width: '32px', height: '32px', borderRadius: '50%', 
                background: step >= num ? 'var(--accent-primary)' : 'var(--bg-tertiary)', 
                color: step >= num ? 'white' : 'var(--text-muted)',
                display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 600,
                transition: 'all 0.3s ease'
              }}>
                {step > num ? <CheckCircle size={18} /> : num}
              </div>
            </div>
          ))}
        </div>

        {step === 1 && (
          <div className="animate-slide-in-left">
            <h2 style={{ fontSize: '1.5rem', marginBottom: '8px' }}>Name Your AI Agent</h2>
            <p style={{ color: 'var(--text-secondary)', marginBottom: '24px' }}>This is the name your customers will hear on the call.</p>
            
            <div style={{ display: 'flex', gap: '32px' }}>
              <div style={{ flex: 1 }}>
                <label className="input-label">AI Agent Name</label>
                <input className="input-field" value={agentName} onChange={e => setAgentName(e.target.value)} placeholder="e.g. Rahul, Priya" />
                <button className="btn btn-primary" style={{ marginTop: '24px', width: '100%' }} onClick={handleNext}>Next Step</button>
              </div>
              <div className="glass-card" style={{ flex: 1, padding: '20px', background: 'var(--bg-tertiary)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '16px' }}>
                  <div style={{ width: '40px', height: '40px', borderRadius: '50%', background: 'var(--accent-primary-dim)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                     <Bot size={20} color="var(--accent-primary)" />
                  </div>
                  <div>
                    <div style={{ fontWeight: 600 }}>Live Preview</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--accent-primary)' }}>ElevenLabs Voice</div>
                  </div>
                </div>
                <div style={{ padding: '12px', background: 'var(--bg-secondary)', borderRadius: 'var(--radius-md)', fontSize: '0.875rem', lineHeight: 1.5 }}>
                  "Namaste! Main {agentName || '...'} bol raha hoon aapki Gas Agency se. Aapko cylinder connect ho gaya hoga, mujhe bas uska DSC number batayiye."
                </div>
              </div>
            </div>
          </div>
        )}

        {step === 2 && (
          <div className="animate-slide-in-left">
            <h2 style={{ fontSize: '1.5rem', marginBottom: '8px' }}>Transfer Options</h2>
            <p style={{ color: 'var(--text-secondary)', marginBottom: '24px' }}>Where should the AI transfer calls if a customer has a complex issue?</p>
            
            <div style={{ marginBottom: '24px' }}>
              <label className="input-label">Office / Supervisor Number</label>
              <div style={{ position: 'relative' }}>
                <Phone size={18} style={{ position: 'absolute', left: '12px', top: '13px', color: 'var(--text-muted)' }} />
                <input className="input-field" style={{ paddingLeft: '40px' }} value={transferNumber} onChange={e => setTransferNumber(e.target.value)} placeholder="10-digit mobile or landline" />
              </div>
            </div>
            
            <div style={{ display: 'flex', gap: '12px' }}>
              <button className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setStep(1)}>Back</button>
              <button className="btn btn-primary" style={{ flex: 2 }} onClick={handleNext}>Confirm & Next</button>
            </div>
          </div>
        )}

        {step === 3 && (
          <div className="animate-slide-in-left">
            <h2 style={{ fontSize: '1.5rem', marginBottom: '8px' }}>Launch First Campaign</h2>
            <p style={{ color: 'var(--text-secondary)', marginBottom: '24px' }}>Upload your first delivery list (CSV) to start the magic.</p>
            
            <CSVUploader onUpload={setFile} />
            
            <div style={{ display: 'flex', gap: '12px', marginTop: '32px' }}>
              <button className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setStep(2)}>Back</button>
              <button className="btn btn-primary animate-pulse-glow" style={{ flex: 2 }} onClick={handleLaunch} disabled={!file || launching}>
                 {launching ? '⏳ Launching...' : '🚀 Launch AI Campaign'}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
