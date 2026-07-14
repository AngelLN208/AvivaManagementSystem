import { useEffect, useState } from 'react';

export function useCurrentTime(intervalMs = 30_000) {
  const [now, setNow] = useState(() => new Date());

  useEffect(() => {
    const timerId = window.setInterval(() => setNow(new Date()), intervalMs);
    return () => window.clearInterval(timerId);
  }, [intervalMs]);

  return now;
}
