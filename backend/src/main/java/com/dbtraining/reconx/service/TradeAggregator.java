package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.TradeEvent;
import com.dbtraining.reconx.repository.AuditLogRepository;
import com.dbtraining.reconx.repository.entity.AuditLogEntry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * ============================================================================
 * TICKET-ADV137 — TradeAggregator (Event Sourcing Rebuild)
 *
 * WHAT:    Rebuilds current trade state by sequentially processing stored
 *          audit log events for a tradeRef.
 * HOW:     Reads audit_log ordered by eventTimestamp ASC, folding state.
 * ============================================================================
 */
@Service
public class TradeAggregator {

    private final AuditLogRepository auditRepo;
    private final ObjectMapper mapper;

    public TradeAggregator(AuditLogRepository auditRepo, ObjectMapper mapper) {
        this.auditRepo = auditRepo;
        this.mapper = mapper;
    }

    public Optional<JsonNode> rebuild(String tradeRef) {
        List<AuditLogEntry> events = auditRepo.findByTradeRefOrderByEventTimestampAsc(tradeRef);
        if (events.isEmpty()) {
            return Optional.empty();
        }

        JsonNode state = null;
        for (AuditLogEntry e : events) {
            TradeEvent.EventType type = TradeEvent.EventType.valueOf(e.getEventType());
            switch (type) {
                case TRADE_CREATED, TRADE_UPDATED -> {
                    if (e.getAfterState() != null) {
                        try {
                            state = mapper.readTree(e.getAfterState());
                        } catch (Exception ex) {
                            state = mapper.valueToTree(e.getAfterState());
                        }
                    }
                }
                case TRADE_CANCELLED -> state = null;
            }
        }
        return Optional.ofNullable(state);
    }
}
