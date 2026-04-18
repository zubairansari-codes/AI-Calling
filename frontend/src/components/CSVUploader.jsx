import { useState, useRef } from 'react';
import { Upload, FileText, X, CheckCircle } from 'lucide-react';

export default function CSVUploader({ onUpload, accept = '.csv' }) {
  const [dragOver, setDragOver] = useState(false);
  const [file, setFile] = useState(null);
  const [preview, setPreview] = useState(null);
  const inputRef = useRef();

  const handleFile = (f) => {
    if (!f) return;
    setFile(f);
    onUpload?.(f);

    // Parse preview
    const reader = new FileReader();
    reader.onload = (e) => {
      const lines = e.target.result.split('\n').filter(l => l.trim());
      const headers = lines[0].split(',').map(h => h.trim());
      const rows = lines.slice(1, 6).map(l => l.split(',').map(c => c.trim()));
      setPreview({ headers, rows, total: lines.length - 1 });
    };
    reader.readAsText(f);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setDragOver(false);
    handleFile(e.dataTransfer.files[0]);
  };

  return (
    <div>
      <div
        onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
        onDragLeave={() => setDragOver(false)}
        onDrop={handleDrop}
        onClick={() => inputRef.current?.click()}
        style={{
          border: `2px dashed ${dragOver ? 'var(--accent-primary)' : 'var(--border-medium)'}`,
          borderRadius: 'var(--radius-lg)',
          padding: '40px 24px',
          textAlign: 'center',
          cursor: 'pointer',
          transition: 'all var(--transition-base)',
          background: dragOver ? 'var(--accent-primary-dim)' : 'transparent',
        }}
      >
        <input ref={inputRef} type="file" accept={accept} style={{ display: 'none' }}
          onChange={(e) => handleFile(e.target.files[0])} />

        {file ? (
          <div className="animate-scale-in" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px' }}>
            <CheckCircle size={32} style={{ color: 'var(--accent-primary)' }} />
            <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{file.name}</span>
            <span style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)' }}>
              {preview ? `${preview.total} customers` : 'Parsing...'}
            </span>
            <button className="btn btn-ghost" onClick={(e) => { e.stopPropagation(); setFile(null); setPreview(null); }}
              style={{ fontSize: '0.8125rem', marginTop: '4px' }}>
              <X size={14} /> Remove
            </button>
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px' }}>
            <Upload size={32} style={{ color: 'var(--text-muted)' }} />
            <span style={{ fontWeight: 600 }}>Drop CSV file here</span>
            <span style={{ fontSize: '0.8125rem', color: 'var(--text-muted)' }}>or click to browse</span>
          </div>
        )}
      </div>

      {preview && (
        <div className="animate-fade-up" style={{ marginTop: '16px', overflowX: 'auto' }}>
          <table className="data-table" style={{ fontSize: '0.8125rem' }}>
            <thead>
              <tr>
                {preview.headers.map((h, i) => <th key={i}>{h}</th>)}
              </tr>
            </thead>
            <tbody>
              {preview.rows.map((row, i) => (
                <tr key={i}>
                  {row.map((cell, j) => <td key={j}>{cell}</td>)}
                </tr>
              ))}
            </tbody>
          </table>
          {preview.total > 5 && (
            <div style={{ padding: '8px 16px', color: 'var(--text-muted)', fontSize: '0.75rem' }}>
              Showing 5 of {preview.total} rows
            </div>
          )}
        </div>
      )}
    </div>
  );
}
