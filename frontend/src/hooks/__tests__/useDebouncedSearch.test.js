import { renderHook, act } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { useDebouncedSearch } from '../useDebouncedSearch.js';

describe('useDebouncedSearch hook', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('returns initial query immediately on first render', () => {
    const { result } = renderHook(() => useDebouncedSearch('AAPL', 300));
    expect(result.current).toBe('AAPL');
  });

  it('debounces rapid query changes and only updates after delay', () => {
    const { result, rerender } = renderHook(
      ({ query, delay }) => useDebouncedSearch(query, delay),
      { initialProps: { query: 'A', delay: 300 } }
    );

    expect(result.current).toBe('A');

    // Simulate fast typing 'AA', 'AAP', 'AAPL'
    rerender({ query: 'AA', delay: 300 });
    act(() => { vi.advanceTimersByTime(100); });
    expect(result.current).toBe('A'); // Not updated yet

    rerender({ query: 'AAP', delay: 300 });
    act(() => { vi.advanceTimersByTime(100); });
    expect(result.current).toBe('A'); // Still cancelled & reset

    rerender({ query: 'AAPL', delay: 300 });
    act(() => { vi.advanceTimersByTime(200); });
    expect(result.current).toBe('A'); // 200ms < 300ms

    // Advance remaining 100ms to complete 300ms pause after last keystroke
    act(() => { vi.advanceTimersByTime(100); });
    expect(result.current).toBe('AAPL');
  });
});
