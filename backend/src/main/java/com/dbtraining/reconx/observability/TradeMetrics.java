package com.dbtraining.reconx.observability;

import com.dbtraining.reconx.repository.ReconBreakRepository;
import com.dbtraining.reconx.repository.TradeRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * TICKET-ADV083 — trade_created_total Counter
 * TICKET-ADV085 — recon_break_count Gauge (polled — wraps repo.countByStatus)
 * TICKET-ADV086 — trade_value_total DistributionSummary
 * TICKET-ADV092 — trades_by_status Gauges
 * ============================================================================
 */
@Component
public class TradeMetrics {

    private final Counter tradeCreated;
    private final DistributionSummary tradeValue;

    public TradeMetrics(MeterRegistry registry,
                        ReconBreakRepository breakRepo,
                        TradeRepository tradeRepo) {

        this.tradeCreated = Counter.builder("my_test_counter")
                .description("Total trades created")
                .register(registry);

        this.tradeValue = DistributionSummary.builder("trade_value_total")
                .description("Distribution of trade notional values")
                .baseUnit("USD")
                .publishPercentileHistogram()
                .register(registry);

        // ADV085
        Gauge.builder("recon_break_count",
                        breakRepo,
                        r -> r.countByStatus("OPEN"))
                .description("Open recon breaks")
                .register(registry);

        // ADV092 - Trades by Status

        Gauge.builder("trades_by_status",
                        tradeRepo,
                        r -> r.countByStatus("PENDING"))
                .description("Number of trades by status")
                .tag("status", "PENDING")
                .register(registry);

        Gauge.builder("trades_by_status",
                        tradeRepo,
                        r -> r.countByStatus("MATCHED"))
                .description("Number of trades by status")
                .tag("status", "MATCHED")
                .register(registry);

        Gauge.builder("trades_by_status",
                        tradeRepo,
                        r -> r.countByStatus("UNMATCHED"))
                .description("Number of trades by status")
                .tag("status", "UNMATCHED")
                .register(registry);

        Gauge.builder("trades_by_status",
                        tradeRepo,
                        r -> r.countByStatus("DISPUTED"))
                .description("Number of trades by status")
                .tag("status", "DISPUTED")
                .register(registry);

        Gauge.builder("trades_by_status",
                        tradeRepo,
                        r -> r.countByStatus("CANCELLED"))
                .description("Number of trades by status")
                .tag("status", "CANCELLED")
                .register(registry);
    }

    public void incrementTradeCreated() {
        tradeCreated.increment();
    }

    public void recordTradeValue(double value) {
        tradeValue.record(value);
    }
}