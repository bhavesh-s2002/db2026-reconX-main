package com.dbtraining.reconx.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

public class AuditEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(AuditEventPublisher.class);
    private final ApplicationEventPublisher eventPublisher;
    private final AuditProperties properties;

    public AuditEventPublisher(ApplicationEventPublisher eventPublisher, AuditProperties properties) {
        this.eventPublisher = eventPublisher;
        this.properties = properties;
        log.info("AuditEventPublisher initialized with topic: {} (enabled={})", properties.getTopic(), properties.isEnabled());
    }

    public void publish(String action, Object details) {
        if (properties.isEnabled()) {
            log.info("Publishing audit event [action={}, topic={}]: {}", action, properties.getTopic(), details);
        }
    }

    public AuditProperties getProperties() {
        return properties;
    }
}
