package com.dbtraining.reconx.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.dbtraining.reconx.model.TradeType;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.dto.ReconResult.Status;
import com.dbtraining.reconx.model.EquityTrade;
import com.dbtraining.reconx.model.ReconciliationRule;
import com.dbtraining.reconx.model.Side;
import com.dbtraining.reconx.model.TradeRef;
/**
 * TICKET-ADV040 / ADV041 / ADV042 — TDD: write the test FIRST, then the impl.
 */
class ReconciliationEngineTest {

    private final ReconciliationEngine engine = new ReconciliationEngine();

    @Test
    void testReconcile_exactMatch_returnsMatched() {
        // TODO(TICKET-ADV040): two identical EquityTrades + EXACT rule -> one ReconResult with status MATCHED.
        // given
    ReconciliationEngine engine = new ReconciliationEngine();

    EquityTrade internalTrade = EquityTrade.builder()
            .tradeRef(TradeRef.of("EQU-20260602-0001"))
            .instrumentSymbol("AAPL")
            .quantity(new BigDecimal("100"))
            .price(new BigDecimal("150"))
            .currency("USD")
            .side(Side.BUY)
            .tradeDate(LocalDate.of(2026, 6, 2))
            .counterpartyId(1L)
            .build();

    EquityTrade externalTrade = EquityTrade.builder()
            .tradeRef(TradeRef.of("EQU-20260602-0001"))
            .instrumentSymbol("AAPL")
            .quantity(new BigDecimal("100"))
            .price(new BigDecimal("150"))
            .currency("USD")
            .side(Side.BUY)
            .tradeDate(LocalDate.of(2026, 6, 2))
            .counterpartyId(1L)
            .build();

    // when
    List<ReconResult> out = engine.reconcile(
            List.of(internalTrade),
            List.of(externalTrade),
            ReconciliationRule.EXACT);

    // then
    assertThat(out).hasSize(1);
    assertThat(out.get(0).status())
            .isEqualTo(ReconResult.Status.MATCHED);
    }

    @Test
    void testReconcile_priceTolerance_withinThreshold() {
        // TODO(TICKET-ADV041): prices 100.00 vs 100.50 + PRICE_TOLERANCE_1PCT rule -> status MATCHED.
        EquityTrade internal = equity("EQU-20260603-0002", "100.00", "1000");
        EquityTrade external = equity("EQU-20260603-0002", "100.50", "1000");

        List<ReconResult> out = engine.reconcile(List.of(internal), List.of(external),
                ReconciliationRule.PRICE_TOLERANCE_1PCT);

        assertThat(out.get(0).status()).isEqualTo(ReconResult.Status.MATCHED);
    }

    @Test
    void testReconcile_missingCounterpartyTrade_returnsBreak() {
        // TODO(TICKET-ADV042): internal trade with no external counterpart -> status BREAK,
        //                     discrepancyType = "MISSING_EXTERNAL".
        EquityTrade internal = equity("EQU-20260603-0003", "100.00", "1000");

        List<ReconResult> out = engine.reconcile(List.of(internal), List.of(), ReconciliationRule.EXACT);

        assertThat(out.get(0).status()).isEqualTo(ReconResult.Status.BREAK);
        assertThat(out.get(0).discrepancyType()).isEqualTo("MISSING_EXTERNAL");
    }

    @Test
    void testReconcile_emptyInternal_returnsEmpty() {
        // given
        List<TradeType> internal = List.of();
        List<TradeType> external = List.of();

        // when
        List<ReconResult> out =
                engine.reconcile(internal, external, ReconciliationRule.EXACT);

        // then
        assertThat(out).isEmpty();
    }

    @Test
    void testReconcile_allMismatched_returnsAllBreaks() {

        EquityTrade internal1 = equity("EQU-20260603-0001", "100.00", "100");
        EquityTrade internal2 = equity("EQU-20260603-0002", "200.00", "100");
        EquityTrade internal3 = equity("EQU-20260603-0003", "300.00", "100");

        EquityTrade external1 = equity("EQU-20260603-0001", "150.00", "100");
        EquityTrade external2 = equity("EQU-20260603-0002", "250.00", "100");
        EquityTrade external3 = equity("EQU-20260603-0003", "350.00", "100");

        List<ReconResult> out = engine.reconcile(
                List.of(internal1, internal2, internal3),
                List.of(external1, external2, external3),
                ReconciliationRule.EXACT);

        assertThat(out).hasSize(3);

        assertThat(out)
                .extracting(ReconResult::status)
                .containsOnly(Status.BREAK);
    }

    private EquityTrade equity(String ref, String price, String qty) {
        return EquityTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .instrumentSymbol("SAP.DE")
                .price(new BigDecimal(price))
                .quantity(new BigDecimal(qty))
                .currency("EUR").side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();
    }
}
