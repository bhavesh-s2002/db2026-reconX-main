import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import AddTrade from '../AddTrade.jsx';
import { AuthContext } from '@context/AuthContext.jsx';
import { api } from '@services/apiService.js';

vi.mock('@services/apiService.js', () => ({
  api: {
    createTrade: vi.fn(),
  },
}));

describe('<AddTrade>', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders trade form fields', () => {
    render(
      <AuthContext.Provider value={{ user: { token: 'mock-jwt', role: 'TRADER' } }}>
        <MemoryRouter>
          <AddTrade />
        </MemoryRouter>
      </AuthContext.Provider>
    );

    expect(screen.getByText('Add trade')).toBeInTheDocument();
    expect(screen.getByLabelText(/Trade ref/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Quantity/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Price/i)).toBeInTheDocument();
  });

  it('shows validation errors when submitting empty form', async () => {
    render(
      <AuthContext.Provider value={{ user: { token: 'mock-jwt', role: 'TRADER' } }}>
        <MemoryRouter>
          <AddTrade />
        </MemoryRouter>
      </AuthContext.Provider>
    );

    fireEvent.click(screen.getByRole('button', { name: 'Submit' }));

    await waitFor(() => {
      expect(screen.getAllByRole('alert').length).toBeGreaterThan(0);
    });

    expect(api.createTrade).not.toHaveBeenCalled();
  });
});
