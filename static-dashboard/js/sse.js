// TICKET-ADV104 / TICKET-ADV105
// Demo SSE feed with prepend, animation and DOM cap.

(function () {

  const FEED_EL = document.getElementById("trade-feed");
  if (!FEED_EL) return;

  const demoEvents = [
    {
      tradeRef: "EQU-20260603-0001",
      symbol: "SAP.DE",
      qty: 1000,
      price: 125.50,
      status: "MATCHED"
    },
    {
      tradeRef: "FX-20260603-0001",
      symbol: "EUR/USD",
      qty: 1000000,
      price: 1.0852,
      status: "PENDING"
    },
    {
      tradeRef: "EQU-20260603-0002",
      symbol: "AAPL",
      qty: 500,
      price: 178.20,
      status: "BREAK"
    }
  ];

  function escapeHtml(str) {
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
  }

  function formatQty(value) {
    return new Intl.NumberFormat("en-US").format(value);
  }

  function formatPrice(value) {
    return new Intl.NumberFormat("en-US", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 4
    }).format(value);
  }

  function prependTradeRow(trade) {

    let statusModifier = "";

    if (trade.status === "MATCHED") {
      statusModifier = "trade-card--matched";
    } else if (
        trade.status === "BREAK" ||
        trade.status === "UNMATCHED"
    ) {
      statusModifier = "trade-card--break";
    }

    const row = document.createElement("article");

    row.className =
        `trade-card ${statusModifier} trade-card--new`;

    row.innerHTML = `
      <header class="trade-card__header">
        <strong>${escapeHtml(trade.tradeRef)}</strong>
        <span>${escapeHtml(trade.status)}</span>
      </header>

      <div class="trade-card__body">
        <div>${escapeHtml(trade.symbol)}</div>
        <div>Qty: ${formatQty(trade.qty)}</div>
        <div>Price: ${formatPrice(trade.price)}</div>
      </div>
    `;

    FEED_EL.prepend(row);

    setTimeout(() => {
      row.classList.remove("trade-card--new");
    }, 500);

    while (FEED_EL.children.length > 50) {
      FEED_EL.lastElementChild.remove();
    }
  }

  demoEvents.forEach((trade, index) => {
    setTimeout(() => {
      prependTradeRow(trade);
    }, index * 500);
  });

})();