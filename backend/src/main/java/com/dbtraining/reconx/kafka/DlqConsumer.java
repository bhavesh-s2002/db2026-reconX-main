package com.dbtraining.reconx.kafka;

import com.dbtraining.reconx.dto.TradeEvent;
import com.dbtraining.reconx.repository.DlqMessageRepository;
import com.dbtraining.reconx.repository.entity.DlqMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * ============================================================================
 * TICKET-ADV136 — DlqConsumer
 *
 * WHAT:    Subscribes to `trade-events-dlq` and persists poison-pill records
 *          into the dlq_messages table.
 * HOW:     @KafkaListener on `trade-events-dlq`, groupId `dlq-monitor`.
 *          Receives ConsumerRecord + EXCEPTION_MESSAGE header -> repo.save(...)
 * ============================================================================
 */
@Component
public class DlqConsumer {

    private static final Logger log = LoggerFactory.getLogger(DlqConsumer.class);

    private final DlqMessageRepository repo;
    private final ObjectMapper mapper;

    public DlqConsumer(DlqMessageRepository repo, ObjectMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @KafkaListener(
            topics = "trade-events-dlq",
            groupId = "dlq-monitor"
    )
    public void onDlqMessage(ConsumerRecord<String, Object> record,
                             @Header(name = KafkaHeaders.EXCEPTION_MESSAGE, required = false) String exMsg) {
        Object val = record.value();
        String tradeRef = null;
        String eventId = UUID.randomUUID().toString();
        String payloadJson = null;

        if (val instanceof TradeEvent event) {
            tradeRef = event.tradeRef();
            if (event.eventId() != null) {
                eventId = event.eventId().toString();
            }
            try {
                payloadJson = mapper.writeValueAsString(event);
            } catch (Exception ignored) {}
        } else if (val != null) {
            payloadJson = val.toString();
        }

        log.error("DLQ message captured topic={} partition={} offset={} tradeRef={} reason={}",
                record.topic(), record.partition(), record.offset(), tradeRef, exMsg);

        repo.save(new DlqMessage(
                eventId,
                tradeRef,
                record.topic().replace("-dlq", ""),
                record.partition(),
                record.offset(),
                payloadJson,
                exMsg != null ? exMsg : "DLQ Failure",
                Instant.now()
        ));
    }
}
