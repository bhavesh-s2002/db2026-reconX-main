package com.dbtraining.reconx.controller;

import com.dbtraining.reconx.dto.TradeEvent;
import com.dbtraining.reconx.kafka.TradeEventProducer;
import com.dbtraining.reconx.repository.DlqMessageRepository;
import com.dbtraining.reconx.repository.entity.DlqMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ============================================================================
 * TICKET-ADV136 — DlqAdminController
 *
 * WHAT:    POST /api/v1/admin/dlq/replay endpoint for re-publishing a DLQ
 *          message back to the main topic and removing it from the database.
 * HOW:     Accepts eventId + optional dryRun flag. Requires ADMIN role.
 * ============================================================================
 */
@RestController
@RequestMapping("/v1/admin/dlq")
@Tag(name = "dlq-admin")
@PreAuthorize("hasRole('ADMIN')")
public class DlqAdminController {

    private final DlqMessageRepository repo;
    private final TradeEventProducer producer;
    private final ObjectMapper mapper;

    public DlqAdminController(DlqMessageRepository repo, TradeEventProducer producer, ObjectMapper mapper) {
        this.repo = repo;
        this.producer = producer;
        this.mapper = mapper;
    }

    @PostMapping("/replay")
    @Operation(summary = "Replay a single DLQ message back to main topic by eventId")
    public ResponseEntity<Map<String, Object>> replay(
            @RequestParam String eventId,
            @RequestParam(defaultValue = "false") boolean dryRun) {

        DlqMessage msg = repo.findByEventId(eventId)
                .orElseThrow(() -> new IllegalArgumentException("No DLQ message with eventId: " + eventId));

        if (dryRun) {
            return ResponseEntity.ok(Map.of(
                    "dryRun", true,
                    "wouldReplayTo", msg.getOriginalTopic(),
                    "tradeRef", msg.getTradeRef() != null ? msg.getTradeRef() : "",
                    "eventId", msg.getEventId()
            ));
        }

        try {
            if (msg.getPayload() != null) {
                TradeEvent event = mapper.readValue(msg.getPayload(), TradeEvent.class);
                producer.publish(event);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse and replay DLQ message payload: " + ex.getMessage());
        }

        repo.delete(msg);

        return ResponseEntity.ok(Map.of(
                "replayed", true,
                "eventId", eventId,
                "topic", msg.getOriginalTopic()
        ));
    }
}
