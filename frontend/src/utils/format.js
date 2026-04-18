export function formatPhone(phone) {
  if (!phone) return '';
  const clean = phone.replace(/\D/g, '');
  if (clean.length === 10) return `+91 ${clean.slice(0,5)} ${clean.slice(5)}`;
  return phone;
}

export function formatDate(dateStr) {
  if (!dateStr) return '—';
  return new Date(dateStr).toLocaleDateString('en-IN', {
    day: 'numeric', month: 'short', year: 'numeric'
  });
}

export function formatTime(dateStr) {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleTimeString('en-IN', {
    hour: '2-digit', minute: '2-digit'
  });
}

export function formatDateTime(dateStr) {
  if (!dateStr) return '—';
  return `${formatDate(dateStr)} ${formatTime(dateStr)}`;
}

export function formatDuration(seconds) {
  if (!seconds) return '0s';
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return m > 0 ? `${m}m ${s}s` : `${s}s`;
}

export function formatNumber(num) {
  if (num == null) return '0';
  return new Intl.NumberFormat('en-IN').format(num);
}

export function formatPercent(num) {
  if (num == null) return '0%';
  return `${Math.round(num * 10) / 10}%`;
}

export function formatCurrency(amount) {
  if (amount == null) return '₹0';
  return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(amount);
}

export const statusColors = {
  PENDING: { bg: 'var(--accent-blue-dim)', color: 'var(--accent-blue)', label: 'Pending' },
  QUEUED: { bg: 'var(--accent-blue-dim)', color: 'var(--accent-blue)', label: 'Queued' },
  IN_PROGRESS: { bg: 'var(--accent-amber-dim)', color: 'var(--accent-amber)', label: 'In Progress' },
  DSC_COLLECTED: { bg: 'var(--accent-primary-dim)', color: 'var(--accent-primary)', label: 'DSC Collected' },
  TRANSFERRED: { bg: 'var(--accent-purple-dim)', color: 'var(--accent-purple)', label: 'Transferred' },
  NO_ANSWER: { bg: 'var(--accent-amber-dim)', color: 'var(--accent-amber)', label: 'No Answer' },
  VOICEMAIL: { bg: 'var(--accent-amber-dim)', color: 'var(--accent-amber)', label: 'Voicemail' },
  FAILED: { bg: 'var(--accent-red-dim)', color: 'var(--accent-red)', label: 'Failed' },
  BUSY: { bg: 'var(--accent-amber-dim)', color: 'var(--accent-amber)', label: 'Busy' },
  RETRY_SCHEDULED: { bg: 'var(--accent-blue-dim)', color: 'var(--accent-blue)', label: 'Retry' },
  // Campaign statuses
  DRAFT: { bg: 'rgba(148,163,184,0.15)', color: 'var(--text-secondary)', label: 'Draft' },
  READY: { bg: 'var(--accent-blue-dim)', color: 'var(--accent-blue)', label: 'Ready' },
  RUNNING: { bg: 'var(--accent-primary-dim)', color: 'var(--accent-primary)', label: 'Running' },
  PAUSED: { bg: 'var(--accent-amber-dim)', color: 'var(--accent-amber)', label: 'Paused' },
  COMPLETED: { bg: 'var(--accent-secondary-dim)', color: 'var(--accent-secondary)', label: 'Completed' },
  CANCELLED: { bg: 'var(--accent-red-dim)', color: 'var(--accent-red)', label: 'Cancelled' },
};
