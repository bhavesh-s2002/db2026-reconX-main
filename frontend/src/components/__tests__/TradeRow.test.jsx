import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { TradeRow, areEqual } from '../TradeRow.jsx';

describe('<TradeRow>', () => {
  const sampleTrade = {
    id: 101,
    tradeRef: 'TRD-101',
    instrument: 'AAPL',
    quantity: 100,
    price: 150.50,
    status: 'MATCHED',
  };

  it('renders trade details correctly', () => {
    render(<TradeRow trade={sampleTrade} onClick={() => {}} />);

    expect(screen.getByText('TRD-101')).toBeInTheDocument();
    expect(screen.getByText('AAPL')).toBeInTheDocument();
    expect(screen.getByText('100')).toBeInTheDocument();
    expect(screen.getByText('150.5')).toBeInTheDocument();
    expect(screen.getByText('MATCHED')).toBeInTheDocument();
  });

  it('invokes onClick callback when row is clicked', () => {
    const handleClick = vi.fn();
    render(<TradeRow trade={sampleTrade} onClick={handleClick} />);

    fireEvent.click(screen.getByTestId('trade-row-101'));
    expect(handleClick).toHaveBeenCalledWith(101);
  });

  describe('areEqual predicate for React.memo', () => {
    it('returns true when trade id, status, price, and onClick are identical', () => {
      const fn = () => {};
      const prev = { trade: { ...sampleTrade }, onClick: fn };
      const next = { trade: { ...sampleTrade, quantity: 200 }, onClick: fn }; // quantity unrendered change

      expect(areEqual(prev, next)).toBe(true);
    });

    it('returns false when status or price changes', () => {
      const fn = () => {};
      const prev = { trade: { ...sampleTrade }, onClick: fn };
      const nextStatus = { trade: { ...sampleTrade, status: 'UNMATCHED' }, onClick: fn };
      const nextPrice = { trade: { ...sampleTrade, price: 160.00 }, onClick: fn };

      expect(areEqual(prev, nextStatus)).toBe(false);
      expect(areEqual(prev, nextPrice)).toBe(false);
    });

    it('returns false when onClick function reference changes', () => {
      const prev = { trade: { ...sampleTrade }, onClick: () => {} };
      const next = { trade: { ...sampleTrade }, onClick: () => {} }; // new arrow function instance

      expect(areEqual(prev, next)).toBe(false);
    });
  });
});
