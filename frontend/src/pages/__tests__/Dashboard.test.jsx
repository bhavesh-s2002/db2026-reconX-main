import React from 'react';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import Dashboard from '../Dashboard.jsx';
import { ThemeProvider } from '@context/ThemeContext.jsx';
import { AuthContext } from '@context/AuthContext.jsx';

class MockEventSource {
  constructor(url) {
    this.url = url;
    this.onopen = null;
    this.onerror = null;
    this.onmessage = null;
  }
  close() {}
}

const trades = [
  { id: 1, tradeRef: 'TRD-2026-0001', instrument: 'SAP.DE', quantity: 100, price: 250, status: 'MATCHED' },
  { id: 2, tradeRef: 'TRD-2026-0002', instrument: 'SAP.DE', quantity: 50, price: 251, status: 'UNMATCHED' },
];

function renderWithProviders(ui) {
  const user = { email: 'trader@db.com', role: 'TRADER' };
  return render(
    <AuthContext.Provider value={{ user, isLoading: false }}>
      <ThemeProvider>
        <MemoryRouter>{ui}</MemoryRouter>
      </ThemeProvider>
    </AuthContext.Provider>
  );
}

describe('<Dashboard />', () => {
  const originalEventSource = global.EventSource;

  beforeEach(() => {
    global.EventSource = MockEventSource;
  });

  afterEach(() => {
    global.EventSource = originalEventSource;
  });

  it('shows summary cards with role queries', () => {
    renderWithProviders(<Dashboard trades={trades} />);

    expect(screen.getByRole('heading', { name: /^portfolio value/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /^matched trades$/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /^unmatched trades$/i })).toBeInTheDocument();
    // 100 * 250 + 50 * 251 = 37,550
    expect(screen.getByText(/37,550/)).toBeInTheDocument();
  });
});
