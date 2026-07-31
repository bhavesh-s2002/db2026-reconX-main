import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { withErrorBoundary } from '../withErrorBoundary.jsx';

function ProblematicComponent({ shouldThrow }) {
  if (shouldThrow) {
    throw new Error('Test error rendering component');
  }
  return <div>Normal Component</div>;
}

const WrappedComponent = withErrorBoundary(ProblematicComponent);

describe('withErrorBoundary HOC', () => {
  it('renders wrapped component normally when no error occurs', () => {
    render(<WrappedComponent shouldThrow={false} />);
    expect(screen.getByText('Normal Component')).toBeInTheDocument();
  });

  it('catches render errors and displays fallback UI', () => {
    // Suppress console.error output during intentional boundary catch
    const spy = vi.spyOn(console, 'error').mockImplementation(() => {});

    render(<WrappedComponent shouldThrow={true} />);
    expect(screen.getByRole('alert')).toBeInTheDocument();
    expect(screen.getByText('Something went wrong')).toBeInTheDocument();
    expect(screen.getByText('Test error rendering component')).toBeInTheDocument();

    spy.mockRestore();
  });

  it('sets appropriate displayName for DevTools', () => {
    expect(WrappedComponent.displayName).toBe('withErrorBoundary(ProblematicComponent)');
  });
});
