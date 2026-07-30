// TICKET-ADV106 — Sortable, Resizable Table

(function () {

    const table = document.getElementById("trades-table");
    const tbody = document.getElementById("trades-tbody");

    if (!table || !tbody) return;

    let rows = [

        {
            tradeRef: "EQU-20260603-0001",
            symbol: "SAP.DE",
            quantity: 1000,
            price: 125.50,
            status: "MATCHED"
        },

        {
            tradeRef: "FX-20260603-0001",
            symbol: "EUR/USD",
            quantity: 1000000,
            price: 1.0852,
            status: "PENDING"
        },

        {
            tradeRef: "EQU-20260603-0002",
            symbol: "AAPL",
            quantity: 500,
            price: 178.20,
            status: "BREAK"
        }

    ];

    function renderRows() {

        tbody.innerHTML = rows.map(r => `

            <tr>

                <td>${r.tradeRef}</td>
                <td>${r.symbol}</td>
                <td>${r.quantity}</td>
                <td>${r.price}</td>
                <td>${r.status}</td>

            </tr>

        `).join("");

    }

    renderRows();

    /* ==============================
       SORTING
       ============================== */

    table.querySelectorAll("thead th").forEach(th => {

        th.addEventListener("click", function (e) {

            if (e.target.classList.contains("resize-handle")) return;

            const col = this.dataset.col;
            const type = this.dataset.type || "string";

            const dir =
                this.getAttribute("aria-sort") === "ascending"
                    ? "descending"
                    : "ascending";

            table.querySelectorAll("thead th")
                .forEach(h => h.removeAttribute("aria-sort"));

            this.setAttribute("aria-sort", dir);

            const multiplier = dir === "ascending" ? 1 : -1;

            rows.sort((a, b) => {

                const av = a[col];
                const bv = b[col];

                if (type === "number") {
                    return (Number(av) - Number(bv)) * multiplier;
                }

                return String(av)
                        .localeCompare(String(bv))
                    * multiplier;

            });

            renderRows();

        });

    });

    /* ==============================
       COLUMN RESIZE
       ============================== */

    table.querySelectorAll(".resize-handle").forEach(handle => {

        handle.addEventListener("mousedown", function (e) {

            e.preventDefault();

            const th = handle.closest("th");

            const startX = e.clientX;

            const startWidth = th.offsetWidth;

            function onMove(ev) {

                th.style.width =
                    startWidth + (ev.clientX - startX) + "px";

            }

            function onUp() {

                document.removeEventListener("mousemove", onMove);
                document.removeEventListener("mouseup", onUp);

            }

            document.addEventListener("mousemove", onMove);
            document.addEventListener("mouseup", onUp);

        });

    });

})();