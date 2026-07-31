import React from 'react';
import { render } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { useInfiniteScroll } from '../useInfiniteScroll.js';

class MockIntersectionObserver {
  static instances = [];

  constructor(callback, options) {
    this.callback = callback;
    this.options = options;
    this.observedElement = null;
    this.isDisconnected = false;
    MockIntersectionObserver.instances.push(this);
  }

  observe(element) {
    this.observedElement = element;
  }

  unobserve() {}

  disconnect() {
    this.isDisconnected = true;
  }

  triggerIntersect(isIntersecting = true) {
    this.callback([{ isIntersecting, target: this.observedElement }]);
  }
}

function TestSentinelComponent({ loadMore }) {
  const sentinelRef = useInfiniteScroll(loadMore);
  return <div data-testid="sentinel" ref={sentinelRef} />;
}

describe('useInfiniteScroll hook', () => {
  const originalIntersectionObserver = global.IntersectionObserver;

  beforeEach(() => {
    MockIntersectionObserver.instances = [];
    global.IntersectionObserver = MockIntersectionObserver;
  });

  afterEach(() => {
    global.IntersectionObserver = originalIntersectionObserver;
  });

  it('attaches IntersectionObserver to sentinel element on mount', () => {
    const loadMore = vi.fn();
    render(<TestSentinelComponent loadMore={loadMore} />);

    expect(MockIntersectionObserver.instances.length).toBe(1);
    const observer = MockIntersectionObserver.instances[0];
    expect(observer.observedElement).not.toBeNull();
  });

  it('triggers loadMore callback when sentinel intersects viewport', () => {
    const loadMore = vi.fn();
    render(<TestSentinelComponent loadMore={loadMore} />);

    const observer = MockIntersectionObserver.instances[0];

    // Trigger intersection
    observer.triggerIntersect(true);
    expect(loadMore).toHaveBeenCalledTimes(1);

    // Non-intersecting event should not re-trigger loadMore
    observer.triggerIntersect(false);
    expect(loadMore).toHaveBeenCalledTimes(1);
  });

  it('disconnects observer on component unmount', () => {
    const loadMore = vi.fn();
    const { unmount } = render(<TestSentinelComponent loadMore={loadMore} />);

    const observer = MockIntersectionObserver.instances[0];
    unmount();

    expect(observer.isDisconnected).toBe(true);
  });
});
