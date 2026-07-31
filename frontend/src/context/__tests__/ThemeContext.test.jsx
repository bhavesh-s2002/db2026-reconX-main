import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { ThemeProvider, useTheme } from '../ThemeContext.jsx';

function ThemeConsumer() {
  const { theme, toggle } = useTheme();
  return (
    <div>
      <span data-testid="theme-val">{theme}</span>
      <button onClick={toggle}>Toggle Theme</button>
    </div>
  );
}

describe('ThemeContext & ThemeProvider', () => {
  beforeEach(() => {
    localStorage.clear();
    delete document.documentElement.dataset.theme;
  });

  it('provides default theme and toggles between light and dark', () => {
    render(
      <ThemeProvider>
        <ThemeConsumer />
      </ThemeProvider>
    );

    expect(screen.getByTestId('theme-val').textContent).toBe('light');

    fireEvent.click(screen.getByText('Toggle Theme'));

    expect(screen.getByTestId('theme-val').textContent).toBe('dark');
    expect(document.documentElement.dataset.theme).toBe('dark');
    expect(localStorage.getItem('reconx-theme')).toBe('dark');
  });
});
