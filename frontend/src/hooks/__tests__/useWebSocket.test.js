import { renderHook, act } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { useWebSocket } from '../useWebSocket.js';

class MockWebSocket {
  static instances = [];

  constructor(url) {
    this.url = url;
    this.readyState = MockWebSocket.CONNECTING;
    this.onopen = null;
    this.onmessage = null;
    this.onerror = null;
    this.onclose = null;
    MockWebSocket.instances.push(this);
  }

  simulateOpen() {
    this.readyState = MockWebSocket.OPEN;
    if (this.onopen) this.onopen();
  }

  simulateMessage(data) {
    if (this.onmessage) this.onmessage({ data: typeof data === 'string' ? data : JSON.stringify(data) });
  }

  simulateClose() {
    this.readyState = MockWebSocket.CLOSED;
    if (this.onclose) this.onclose();
  }

  send(payload) {
    this.sentPayload = payload;
  }

  close() {
    this.readyState = MockWebSocket.CLOSED;
    if (this.onclose) this.onclose();
  }
}

MockWebSocket.CONNECTING = 0;
MockWebSocket.OPEN = 1;
MockWebSocket.CLOSING = 2;
MockWebSocket.CLOSED = 3;

describe('useWebSocket hook', () => {
  const originalWebSocket = global.WebSocket;

  beforeEach(() => {
    MockWebSocket.instances = [];
    global.WebSocket = MockWebSocket;
  });

  afterEach(() => {
    global.WebSocket = originalWebSocket;
  });

  it('opens a single websocket connection on mount and sets status', () => {
    const { result } = renderHook(() => useWebSocket('ws://localhost:8080/stream'));

    expect(MockWebSocket.instances.length).toBe(1);
    expect(result.current.status).toBe('connecting');

    act(() => {
      MockWebSocket.instances[0].simulateOpen();
    });

    expect(result.current.status).toBe('open');
  });

  it('updates data state when receiving JSON message', () => {
    const { result } = renderHook(() => useWebSocket('ws://localhost:8080/stream'));
    act(() => {
      MockWebSocket.instances[0].simulateOpen();
    });

    const sampleMessage = { type: 'TRADE_UPDATE', tradeId: 101 };
    act(() => {
      MockWebSocket.instances[0].simulateMessage(sampleMessage);
    });

    expect(result.current.data).toEqual(sampleMessage);
  });

  it('sends payload when socket status is OPEN', () => {
    const { result } = renderHook(() => useWebSocket('ws://localhost:8080/stream'));
    act(() => {
      MockWebSocket.instances[0].simulateOpen();
    });

    act(() => {
      result.current.send({ action: 'ping' });
    });

    expect(MockWebSocket.instances[0].sentPayload).toBe(JSON.stringify({ action: 'ping' }));
  });

  it('closes websocket connection on unmount', () => {
    const { unmount } = renderHook(() => useWebSocket('ws://localhost:8080/stream'));
    act(() => {
      MockWebSocket.instances[0].simulateOpen();
    });

    unmount();
    expect(MockWebSocket.instances[0].readyState).toBe(MockWebSocket.CLOSED);
  });
});
