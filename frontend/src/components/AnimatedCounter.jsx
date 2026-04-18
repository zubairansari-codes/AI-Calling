import { useState, useEffect, useRef } from 'react';

export default function AnimatedCounter({ value, duration = 1200, prefix = '', suffix = '', decimals = 0 }) {
  const [display, setDisplay] = useState(0);
  const startRef = useRef(null);
  const prevRef = useRef(0);

  useEffect(() => {
    const from = prevRef.current;
    const to = typeof value === 'number' ? value : 0;
    if (from === to) return;

    const start = performance.now();
    const diff = to - from;

    function step(now) {
      const elapsed = now - start;
      const progress = Math.min(elapsed / duration, 1);
      // Ease out cubic
      const eased = 1 - Math.pow(1 - progress, 3);
      const current = from + diff * eased;
      setDisplay(current);

      if (progress < 1) {
        startRef.current = requestAnimationFrame(step);
      } else {
        prevRef.current = to;
      }
    }

    startRef.current = requestAnimationFrame(step);
    return () => { if (startRef.current) cancelAnimationFrame(startRef.current); };
  }, [value, duration]);

  const formatted = decimals > 0 ? display.toFixed(decimals) : Math.round(display);
  return <span className="animated-counter">{prefix}{formatted.toLocaleString()}{suffix}</span>;
}
