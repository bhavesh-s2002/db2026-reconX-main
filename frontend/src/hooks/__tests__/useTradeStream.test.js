import { renderHook, act } from '@testing-library/react';
import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { useTradeStream } from '../useTradeStream.js';

class MockEventSource {
  static instances = [];

  constructor(url) {
    this.url = url;
    this.readyState = MockEventSource.CONNECTING;
    this.onopen = null;
    this.onmessage = null;
    this.onerror = null;
    MockEventSource.instances.push(this);
  }

  simulateOpen() {
    this.readyState = MockEventSource.OPEN;
    if (this.onopen) this.onopen();
  }

  simulateMessage(data) {
    if (this.onmessage) {
      this.onmessage({ data: typeof data === 'string' ? data : JSON.stringify(data) });
    }
  }

  simulateError() {
    this.readyState = MockEventSource.CLOSED;
    if (this.onerror) this.onerror();
  }

  close() {
    this.readyState = MockEventSource.CLOSED;
  }
}

MockEventSource.CONNECTING = 0;
MockEventSource.OPEN = 1;
MockEventSource.CLOSED = 2;

describe('useTradeStream hook', () => {
  const originalEventSource = global.EventSource;

  beforeEach(() => {
    MockEventSource.instances = [];
    global.EventSource = MockEventSource;
  });

  afterEach(() => {
    global.EventSource = originalEventSource;
  });

  it('creates EventSource on mount and updates connection state', () => {
    const { result } = renderHook(() => useTradeStream('/api/v1/trades/stream'));

    expect(MockEventSource.instances.length).toBe(1);
    expect(result.current.isConnected).toBe(false);

    act(() => {
      MockEventSource.instances[0].simulateOpen();
    });
    expect(result.current.isConnected).toBe(true);

    act(() => {
      MockEventSource.instances[0].simulateError();
    });
    expect(result.current.isConnected).toBe(false);
  });

  it('accumulates incoming trades up to MAX_BUFFER limit', () => {
    const { result } = renderHook(() => useTradeStream('/api/v1/trades/stream'));
    act(() => {
      MockEventSource.instances[0].simulateOpen();
    });

    const trade1 = { id: 1, symbol: 'AAPL', price: 150 };
    const trade2 = { id: 2, symbol: 'MSFT', price: 300 };

    act(() => {
      MockEventSource.instances[0].simulateMessage(trade1);
    });
    expect(result.current.trades).toEqual([trade1]);

    act(() => {
      MockEventSource.instances[0].simulateMessage(trade2);
    });
    expect(result.current.trades).toEqual([trade2, trade1]);
  });

  it('closes EventSource connection on unmount', () => {
    const { unmount } = renderHook(() => useTradeStream('/api/v1/trades/stream'));
    act(() => {
      MockEventSource.instances[0].simulateOpen();
    });

    unmount();
    expect(MockEventSource.instances[0].readyState).toBe(MockEventSource.CLOSED);
  });
});
