// TICKET-ADV119 — React.memo on <TradeRow /> with custom areEqual function
import React from 'react';

function TradeRowImpl({ trade, onClick }) {
  return (
    <div
      className="trade-row"
      onClick={() => onClick && onClick(trade.id)}
      data-testid={`trade-row-${trade.id}`}
      role="row"
    >
      <span className="trade-cell">{trade.tradeRef}</span>
      <span className="trade-cell">{trade.instrumentSymbol ?? trade.instrumentId ?? trade.instrument ?? trade.symbol ?? '-'}</span>
      <span className="trade-cell">{trade.quantity ?? trade.qty}</span>
      <span className="trade-cell">{trade.price}</span>
      <span className="trade-cell">
        <span className={`status-pill ${trade.status ? trade.status.toLowerCase() : ''}`}>
          {trade.status}
        </span>
      </span>
    </div>
  );
}

// Custom equality check — only compares rendered fields + handler identity
export function areEqual(prev, next) {
  return (
    prev.trade?.id === next.trade?.id &&
    prev.trade?.status === next.trade?.status &&
    prev.trade?.price === next.trade?.price &&
    prev.onClick === next.onClick
  );
}

export const TradeRow = React.memo(TradeRowImpl, areEqual);
export default TradeRow;
