import { useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import { useToast } from '../components/Toast';
import { Link, useNavigate } from 'react-router-dom';
import { Flame } from 'lucide-react';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();
  const { error, success } = useToast();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await login(email, password);
      success('Logged in successfully');
      navigate('/dashboard');
    } catch (err) {
      error(err.response?.data?.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center',
      background: 'var(--bg-primary)', position: 'relative', overflow: 'hidden'
    }}>
      {/* Background gradients */}
      <div style={{ position: 'absolute', top: '-10%', left: '-10%', width: '50vw', height: '50vw', background: 'var(--accent-primary-dim)', filter: 'blur(100px)', borderRadius: '50%', opacity: 0.5 }} className="animate-pulse-glow" />
      <div style={{ position: 'absolute', bottom: '-10%', right: '-10%', width: '50vw', height: '50vw', background: 'var(--accent-blue-dim)', filter: 'blur(100px)', borderRadius: '50%', opacity: 0.5 }} className="animate-pulse-glow" />

      <div className="glass-card animate-fade-up" style={{ width: '100%', maxWidth: '400px', padding: '40px', position: 'relative', zIndex: 10 }}>
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', marginBottom: '32px' }}>
          <div style={{ width: '48px', height: '48px', borderRadius: 'var(--radius-md)', background: 'var(--gradient-primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '16px', color: 'white' }}>
            <Flame size={24} />
          </div>
          <h1 style={{ fontSize: '1.5rem', marginBottom: '8px' }}>Welcome Back</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>Automate your DSC collections</p>
        </div>

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div>
            <label className="input-label">Email</label>
            <input type="email" required className="input-field" value={email} onChange={e => setEmail(e.target.value)} placeholder="agency@example.com" />
          </div>
          <div>
            <label className="input-label">Password</label>
            <input type="password" required className="input-field" value={password} onChange={e => setPassword(e.target.value)} placeholder="••••••••" />
          </div>
          
          <button type="submit" disabled={loading} className="btn btn-primary" style={{ marginTop: '8px', padding: '12px', fontSize: '1rem' }}>
            {loading ? 'Logging in...' : 'Sign In'}
          </button>
        </form>

        <div style={{ marginTop: '24px', textAlign: 'center', fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
          Don't have an account? <Link to="/signup" style={{ color: 'var(--accent-primary)', fontWeight: 600 }}>Create Agency</Link>
        </div>
      </div>
    </div>
  );
}
