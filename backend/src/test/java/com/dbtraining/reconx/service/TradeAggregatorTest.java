package com.dbtraining.reconx.service;

import com.dbtraining.reconx.repository.AuditLogRepository;
import com.dbtraining.reconx.repository.entity.AuditLogEntry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeAggregatorTest {

    @Mock
    private AuditLogRepository auditRepo;

    @Spy
    private ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    private TradeAggregator aggregator;

    @Test
    @DisplayName("TICKET-ADV137: Rebuild state sequentially from audit log events")
    void testRebuildStateFromAuditLog() {
        String tradeRef = "TRD-REBUILD-100";
        Instant now = Instant.now();

        AuditLogEntry createEvent = new AuditLogEntry("e-1", tradeRef, "TRADE_CREATED", now, "TRADER", null, "{\"status\":\"PENDING\",\"price\":100}");
        AuditLogEntry updateEvent = new AuditLogEntry("e-2", tradeRef, "TRADE_UPDATED", now.plusSeconds(5), "TRADER", "{\"status\":\"PENDING\",\"price\":100}", "{\"status\":\"MATCHED\",\"price\":105}");

        when(auditRepo.findByTradeRefOrderByEventTimestampAsc(tradeRef))
                .thenReturn(List.of(createEvent, updateEvent));

        Optional<JsonNode> result = aggregator.rebuild(tradeRef);
        assertThat(result).isPresent();
        assertThat(result.get().get("status").asText()).isEqualTo("MATCHED");
        assertThat(result.get().get("price").asInt()).isEqualTo(105);
    }

    @Test
    @DisplayName("TICKET-ADV137: Rebuild state for cancelled trade returns empty optional")
    void testRebuildCancelledTradeReturnsEmpty() {
        String tradeRef = "TRD-CANCEL-100";
        Instant now = Instant.now();

        AuditLogEntry createEvent = new AuditLogEntry("e-1", tradeRef, "TRADE_CREATED", now, "TRADER", null, "{\"status\":\"PENDING\"}");
        AuditLogEntry cancelEvent = new AuditLogEntry("e-2", tradeRef, "TRADE_CANCELLED", now.plusSeconds(5), "TRADER", "{\"status\":\"PENDING\"}", null);

        when(auditRepo.findByTradeRefOrderByEventTimestampAsc(tradeRef))
                .thenReturn(List.of(createEvent, cancelEvent));

        Optional<JsonNode> result = aggregator.rebuild(tradeRef);
        assertThat(result).isEmpty();
    }
}
