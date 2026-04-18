import { NavLink, useNavigate } from 'react-router-dom';
import { LayoutDashboard, Megaphone, PhoneCall, BarChart3, Settings, CreditCard, LogOut, Flame, ChevronLeft, Wand2 } from 'lucide-react';
import { useState } from 'react';
import { useAuth } from '../hooks/useAuth';

const navItems = [
  { path: '/setup', label: 'Setup Wizard', icon: Wand2 },
  { path: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { path: '/campaigns', label: 'Campaigns', icon: Megaphone },
  { path: '/calls', label: 'Call Logs', icon: PhoneCall },
  { path: '/analytics', label: 'Analytics', icon: BarChart3 },
  { path: '/settings', label: 'Settings', icon: Settings },
  { path: '/billing', label: 'Billing', icon: CreditCard },
];

export default function Sidebar() {
  const [collapsed, setCollapsed] = useState(false);
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => { logout(); navigate('/login'); };
  const w = collapsed ? 'var(--sidebar-collapsed)' : 'var(--sidebar-width)';

  return (
    <aside style={{
      width: w, minHeight: '100vh', background: 'var(--bg-secondary)',
      borderRight: '1px solid var(--border-subtle)', display: 'flex', flexDirection: 'column',
      transition: 'width var(--transition-base)', position: 'fixed', top: 0, left: 0, zIndex: 100,
      overflow: 'hidden',
    }}>
      {/* Logo */}
      <div style={{
        padding: '20px 16px', display: 'flex', alignItems: 'center', gap: '12px',
        borderBottom: '1px solid var(--border-subtle)',
      }}>
        <div className="animate-pulse-glow" style={{
          width: '38px', height: '38px', borderRadius: 'var(--radius-md)',
          background: 'var(--gradient-primary)', display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontSize: '20px', flexShrink: 0, color: 'var(--accent-primary)',
        }}>
          <Flame size={20} color="white" />
        </div>
        {!collapsed && (
          <div className="animate-fade-up" style={{ overflow: 'hidden', whiteSpace: 'nowrap' }}>
            <div style={{ fontWeight: 800, fontSize: '1rem', fontFamily: "'Plus Jakarta Sans', sans-serif" }}>Gas DSC</div>
            <div style={{ fontSize: '0.6875rem', color: 'var(--text-muted)' }}>AI Caller Platform</div>
          </div>
        )}
      </div>

      {/* Nav */}
      <nav style={{ flex: 1, padding: '12px 8px', display: 'flex', flexDirection: 'column', gap: '2px' }}>
        {navItems.filter(item => !(item.path === '/setup' && user?.setupCompleted)).map((item, i) => (
          <NavLink key={item.path} to={item.path} className="animate-slide-in-left"
            style={({ isActive }) => ({
              display: 'flex', alignItems: 'center', gap: '12px',
              padding: collapsed ? '12px 14px' : '10px 14px',
              borderRadius: 'var(--radius-md)',
              color: isActive ? 'var(--accent-primary)' : 'var(--text-secondary)',
              background: isActive ? 'var(--accent-primary-dim)' : 'transparent',
              fontWeight: isActive ? 600 : 500,
              fontSize: '0.875rem', textDecoration: 'none',
              transition: 'all var(--transition-fast)',
              animationDelay: `${i * 50}ms`,
              justifyContent: collapsed ? 'center' : 'flex-start',
              position: 'relative',
            })}
          >
            <item.icon size={20} style={{ flexShrink: 0 }} />
            {!collapsed && <span>{item.label}</span>}
          </NavLink>
        ))}
      </nav>

      {/* Footer */}
      <div style={{ padding: '12px 8px', borderTop: '1px solid var(--border-subtle)' }}>
        {!collapsed && user && (
          <div style={{ padding: '8px 14px', marginBottom: '8px' }}>
            <div style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--text-primary)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
              {user.agencyName}
            </div>
            <div style={{ fontSize: '0.6875rem', color: 'var(--text-muted)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
              {user.email}
            </div>
          </div>
        )}

        <button onClick={handleLogout} style={{
          display: 'flex', alignItems: 'center', gap: '12px', width: '100%',
          padding: '10px 14px', borderRadius: 'var(--radius-md)',
          background: 'transparent', border: 'none', color: 'var(--text-muted)',
          cursor: 'pointer', fontSize: '0.875rem',
          transition: 'all var(--transition-fast)',
          justifyContent: collapsed ? 'center' : 'flex-start',
        }} onMouseEnter={e => e.target.style.color = 'var(--accent-red)'}
           onMouseLeave={e => e.target.style.color = 'var(--text-muted)'}>
          <LogOut size={18} /> {!collapsed && 'Logout'}
        </button>

        <button onClick={() => setCollapsed(!collapsed)} style={{
          display: 'flex', alignItems: 'center', justifyContent: 'center', width: '100%',
          padding: '8px', marginTop: '4px', borderRadius: 'var(--radius-md)',
          background: 'transparent', border: 'none', color: 'var(--text-muted)',
          cursor: 'pointer', transition: 'all var(--transition-fast)',
        }}>
          <ChevronLeft size={16} style={{ transform: collapsed ? 'rotate(180deg)' : 'none', transition: 'transform var(--transition-base)' }} />
        </button>
      </div>
    </aside>
  );
}
