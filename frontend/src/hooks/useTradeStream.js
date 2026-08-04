// useTradeStream() — SSE subscription returning live trades.
import { useEffect, useState } from 'react';
import { api } from '@services/apiService.js';

const MAX_BUFFER = 200;

export function useTradeStream(url = '/api/v1/trades/stream') {
  const [trades, setTrades] = useState([]);
  const [isConnected, setConnected] = useState(false);

  useEffect(() => {
    let active = true;
    api.listTrades()
      .then((res) => {
        if (!active) return;
        const items = res?.items ?? (Array.isArray(res) ? res : []);
        setTrades(items);
      })
      .catch(() => {});

    const sse = new EventSource(url);
    sse.onopen  = () => setConnected(true);
    sse.onerror = () => setConnected(false);
    sse.onmessage = (e) => {
      try {
        const trade = JSON.parse(e.data);
        if (trade && trade.id) {
          setTrades((prev) => [trade, ...prev].slice(0, MAX_BUFFER));
        }
      } catch { /* ignore ping or malformed payload */ }
    };
    return () => {
      active = false;
      sse.close();
    };
  }, [url]);

  return { trades, isConnected };
}
