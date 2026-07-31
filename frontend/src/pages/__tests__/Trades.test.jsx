import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import Trades from '../Trades.jsx';
import { AuthContext } from '@context/AuthContext.jsx';
import { api } from '@services/apiService.js';

vi.mock('@services/apiService.js', () => ({
  api: {
    listTrades: vi.fn(),
  },
}));

describe('<Trades>', () => {
  const mockTradesResponse = {
    items: [
      { id: 1, tradeRef: 'TRD-100', instrument: 'AAPL', quantity: 50, price: 150.00, status: 'MATCHED' },
      { id: 2, tradeRef: 'TRD-200', instrument: 'GOOGL', quantity: 20, price: 2800.00, status: 'PENDING' },
    ],
    totalPages: 1,
  };

  beforeEach(() => {
    vi.clearAllMocks();
    api.listTrades.mockResolvedValue(mockTradesResponse);
  });

  it('renders trades list and filters by search input', async () => {
    render(
      <AuthContext.Provider value={{ user: { token: 'mock-jwt', role: 'TRADER' } }}>
        <MemoryRouter>
          <Trades />
        </MemoryRouter>
      </AuthContext.Provider>
    );

    await waitFor(() => {
      expect(screen.getByText('TRD-100')).toBeInTheDocument();
      expect(screen.getByText('TRD-200')).toBeInTheDocument();
    });

    const searchInput = screen.getByLabelText('Filter by status');
    fireEvent.change(searchInput, { target: { value: 'MATCHED' } });

    expect(searchInput.value).toBe('MATCHED');
  });

  it('selects trade row when clicked', async () => {
    render(
      <AuthContext.Provider value={{ user: { token: 'mock-jwt', role: 'TRADER' } }}>
        <MemoryRouter>
          <Trades />
        </MemoryRouter>
      </AuthContext.Provider>
    );

    await waitFor(() => {
      expect(screen.getByTestId('trade-row-1')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByTestId('trade-row-1'));
    expect(screen.getByText('(Selected: #1)')).toBeInTheDocument();
  });
});
