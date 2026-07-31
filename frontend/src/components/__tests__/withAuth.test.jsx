import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { describe, it, expect } from 'vitest';
import { withAuth } from '../withAuth.jsx';
import { AuthContext } from '@context/AuthContext.jsx';

function ProtectedPage() {
  return <div>Protected Content</div>;
}

const WrappedPage = withAuth(ProtectedPage);

describe('withAuth HOC', () => {
  it('redirects to /login when user is unauthenticated', () => {
    render(
      <AuthContext.Provider value={{ user: null }}>
        <MemoryRouter initialEntries={['/dashboard']}>
          <Routes>
            <Route path="/dashboard" element={<WrappedPage />} />
            <Route path="/login" element={<div>Login Page</div>} />
          </Routes>
        </MemoryRouter>
      </AuthContext.Provider>
    );

    expect(screen.getByText('Login Page')).toBeInTheDocument();
    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
  });

  it('renders wrapped component when user is authenticated', () => {
    render(
      <AuthContext.Provider value={{ user: { token: 'mock-token', role: 'ADMIN' } }}>
        <MemoryRouter initialEntries={['/dashboard']}>
          <Routes>
            <Route path="/dashboard" element={<WrappedPage />} />
            <Route path="/login" element={<div>Login Page</div>} />
          </Routes>
        </MemoryRouter>
      </AuthContext.Provider>
    );

    expect(screen.getByText('Protected Content')).toBeInTheDocument();
    expect(screen.queryByText('Login Page')).not.toBeInTheDocument();
  });

  it('sets appropriate displayName for DevTools', () => {
    expect(WrappedPage.displayName).toBe('withAuth(ProtectedPage)');
  });
});
