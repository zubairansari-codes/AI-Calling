import { useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import { useToast } from '../components/Toast';
import { Link, useNavigate } from 'react-router-dom';
import { Flame } from 'lucide-react';

export default function Signup() {
  const [formData, setFormData] = useState({ agencyName: '', ownerName: '', email: '', password: '', phone: '', city: '' });
  const [loading, setLoading] = useState(false);
  const { signup } = useAuth();
  const navigate = useNavigate();
  const { error, success } = useToast();

  const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await signup(formData);
      success('Account created successfully');
      navigate('/dashboard');
    } catch (err) {
      error(err.response?.data?.message || 'Signup failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center',
      background: 'var(--bg-primary)', position: 'relative', overflow: 'hidden', padding: '40px 20px'
    }}>
      <div style={{ position: 'absolute', top: 0, right: 0, width: '40vw', height: '40vw', background: 'var(--accent-primary-dim)', filter: 'blur(100px)', borderRadius: '50%', opacity: 0.5 }} className="animate-pulse-glow" />
      <div style={{ position: 'absolute', bottom: 0, left: 0, width: '40vw', height: '40vw', background: 'var(--accent-blue-dim)', filter: 'blur(100px)', borderRadius: '50%', opacity: 0.5 }} className="animate-pulse-glow" />

      <div className="glass-card animate-fade-up" style={{ width: '100%', maxWidth: '500px', padding: '40px', position: 'relative', zIndex: 10 }}>
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', marginBottom: '32px' }}>
          <div style={{ width: '48px', height: '48px', borderRadius: 'var(--radius-md)', background: 'var(--gradient-primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '16px', color: 'white' }}>
            <Flame size={24} />
          </div>
          <h1 style={{ fontSize: '1.5rem', marginBottom: '8px' }}>Create Agency</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>Start automating DSC calls today</p>
        </div>

        <form onSubmit={handleSubmit} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
          <div style={{ gridColumn: '1 / -1' }}>
            <label className="input-label">Agency Name</label>
            <input name="agencyName" required className="input-field" value={formData.agencyName} onChange={handleChange} placeholder="e.g. Sharma Gas Agency" />
          </div>
          <div>
            <label className="input-label">Owner Name</label>
            <input name="ownerName" required className="input-field" value={formData.ownerName} onChange={handleChange} placeholder="e.g. Ramesh Sharma" />
          </div>
          <div>
            <label className="input-label">City</label>
            <input name="city" required className="input-field" value={formData.city} onChange={handleChange} placeholder="e.g. Delhi" />
          </div>
          <div style={{ gridColumn: '1 / -1' }}>
            <label className="input-label">Email</label>
            <input type="email" name="email" required className="input-field" value={formData.email} onChange={handleChange} placeholder="agency@example.com" />
          </div>
          <div>
            <label className="input-label">Phone</label>
            <input name="phone" required className="input-field" value={formData.phone} onChange={handleChange} placeholder="10-digit number" />
          </div>
          <div>
            <label className="input-label">Password</label>
            <input type="password" name="password" required minLength="6" className="input-field" value={formData.password} onChange={handleChange} placeholder="••••••••" />
          </div>
          
          <button type="submit" disabled={loading} className="btn btn-primary" style={{ gridColumn: '1 / -1', marginTop: '8px', padding: '12px', fontSize: '1rem' }}>
            {loading ? 'Creating Account...' : 'Sign Up'}
          </button>
        </form>

        <div style={{ marginTop: '24px', textAlign: 'center', fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
          Already have an account? <Link to="/login" style={{ color: 'var(--accent-primary)', fontWeight: 600 }}>Sign In</Link>
        </div>
      </div>
    </div>
  );
}
