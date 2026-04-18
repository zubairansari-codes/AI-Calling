import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';

export default function MainLayout() {
  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      <Sidebar />
      <main style={{ 
        flex: 1, 
        marginLeft: 'var(--sidebar-width)', 
        transition: 'margin-left var(--transition-base)' // In case sidebar width changes
      }}>
        <div className="page-container animate-fade-up">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
