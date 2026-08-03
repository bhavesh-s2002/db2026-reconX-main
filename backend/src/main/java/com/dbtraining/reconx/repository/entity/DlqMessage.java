package com.dbtraining.reconx.repository.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dlq_messages")
public class DlqMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 36)
    private String eventId;

    @Column(name = "trade_ref", length = 30)
    private String tradeRef;

    @Column(name = "original_topic", nullable = false, length = 100)
    private String originalTopic;

    @Column(name = "partition", nullable = false)
    private Integer partition;

    @Column(name = "offset", nullable = false)
    private Long offset;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "reason", length = 2000)
    private String reason;

    @Column(name = "first_seen")
    private Instant firstSeen;

    public DlqMessage() {}

    public DlqMessage(String eventId, String tradeRef, String originalTopic, Integer partition, Long offset, String payload, String reason, Instant firstSeen) {
        this.eventId = eventId;
        this.tradeRef = tradeRef;
        this.originalTopic = originalTopic;
        this.partition = partition;
        this.offset = offset;
        this.payload = payload;
        this.reason = reason;
        this.firstSeen = firstSeen;
    }

    public Long getId() { return id; }
    public String getEventId() { return eventId; }
    public String getTradeRef() { return tradeRef; }
    public String getOriginalTopic() { return originalTopic; }
    public Integer getPartition() { return partition; }
    public Long getOffset() { return offset; }
    public String getPayload() { return payload; }
    public String getReason() { return reason; }
    public Instant getFirstSeen() { return firstSeen; }

    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setTradeRef(String tradeRef) { this.tradeRef = tradeRef; }
    public void setOriginalTopic(String originalTopic) { this.originalTopic = originalTopic; }
    public void setPartition(Integer partition) { this.partition = partition; }
    public void setOffset(Long offset) { this.offset = offset; }
    public void setPayload(String payload) { this.payload = payload; }
    public void setReason(String reason) { this.reason = reason; }
    public void setFirstSeen(Instant firstSeen) { this.firstSeen = firstSeen; }
}
