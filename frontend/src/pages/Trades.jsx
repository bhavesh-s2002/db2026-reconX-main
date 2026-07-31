// Compound DataTable + useDebouncedSearch driving a paginated trades list.
import React, { useCallback, useEffect, useState } from 'react';
import { withAuth } from '@components/withAuth.jsx';
import DataTable from '@components/DataTable.jsx';
import TradeRow from '@components/TradeRow.jsx';
import { useDebouncedSearch } from '@hooks/useDebouncedSearch.js';
import { api } from '@services/apiService.js';

function Trades() {
  const [search, setSearch] = useState('');
  const debounced = useDebouncedSearch(search, 300);
  const [page, setPage] = useState(0);
  const [selectedId, setSelectedId] = useState(null);
  const [data, setData] = useState({ items: [], totalPages: 0 });

  // Reference-stable callback across renders to prevent unnecessary TradeRow re-renders
  const handleSelect = useCallback((id) => {
    setSelectedId(id);
  }, []);

  useEffect(() => {
    let cancelled = false;
    const params = new URLSearchParams();
    params.set('page', String(page));
    if (debounced) params.set('status', debounced);

    api.listTrades(params.toString())
      .then((res) => {
        if (cancelled) return;
        if (res && Array.isArray(res.items)) {
          setData({ items: res.items, totalPages: res.totalPages ?? 0 });
        } else if (Array.isArray(res)) {
          setData({ items: res, totalPages: 1 });
        } else {
          setData({ items: [], totalPages: 0 });
        }
      })
      .catch(() => {
        if (!cancelled) setData({ items: [], totalPages: 0 });
      });

    return () => { cancelled = true; };
  }, [page, debounced]);

  return (
    <section>
      <h2>Trades {selectedId && <span>(Selected: #{selectedId})</span>}</h2>
      <input
        aria-label="Filter by status"
        placeholder="status filter (PENDING/MATCHED/…)"
        value={search}
        onChange={(e) => setSearch(e.target.value.toUpperCase())}
      />
      <DataTable>
        <DataTable.Header columns={[
          { key: 'tradeRef', label: 'Ref' },
          { key: 'instrument', label: 'Instrument' },
          { key: 'quantity', label: 'Qty' },
          { key: 'price', label: 'Price' },
          { key: 'status', label: 'Status' },
        ]} />
        <DataTable.Body
          rows={data.items}
          render={(t) => (
            <TradeRow key={t.id} trade={t} onClick={handleSelect} />
          )}
        />
        <DataTable.Pagination
          page={page}
          totalPages={Math.max(1, data.totalPages)}
          onChange={setPage}
        />
      </DataTable>
    </section>
  );
}

export default withAuth(Trades);

