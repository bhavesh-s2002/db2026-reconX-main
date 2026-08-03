package com.dbtraining.reconx.kafka;

import com.dbtraining.reconx.dto.TradeEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TradeEventTest {

    @Test
    @DisplayName("TICKET-ADV130: TradeEvent factory methods generate valid events")
    void testTradeEventFactories() {
        TradeEvent created = TradeEvent.created("T-100", "{\"price\":100}");
        assertThat(created.eventId()).isNotNull();
        assertThat(created.tradeRef()).isEqualTo("T-100");
        assertThat(created.eventType()).isEqualTo(TradeEvent.EventType.TRADE_CREATED);
        assertThat(created.before()).isNull();
        assertThat(created.after()).isEqualTo("{\"price\":100}");
        assertThat(created.actor()).isEqualTo("SYSTEM");

        TradeEvent updated = TradeEvent.updated("T-100", "USER1", "{\"price\":100}", "{\"price\":105}");
        assertThat(updated.eventType()).isEqualTo(TradeEvent.EventType.TRADE_UPDATED);
        assertThat(updated.actor()).isEqualTo("USER1");
        assertThat(updated.before()).isEqualTo("{\"price\":100}");
        assertThat(updated.after()).isEqualTo("{\"price\":105}");

        TradeEvent cancelled = TradeEvent.cancelled("T-100", "{\"price\":105}");
        assertThat(cancelled.eventType()).isEqualTo(TradeEvent.EventType.TRADE_CANCELLED);
        assertThat(cancelled.before()).isEqualTo("{\"price\":105}");
        assertThat(cancelled.after()).isNull();
    }

    @Test
    @DisplayName("TICKET-ADV128: KafkaTopicsConfig declares 4 topics")
    void testKafkaTopicsConfig() {
        KafkaTopicsConfig config = new KafkaTopicsConfig();
        NewTopic tradeEvents = config.tradeEvents();
        NewTopic reconResults = config.reconResults();
        NewTopic systemAlerts = config.systemAlerts();
        NewTopic tradeEventsDlq = config.tradeEventsDlq();

        assertThat(tradeEvents.name()).isEqualTo(KafkaTopicsConfig.TRADE_EVENTS);
        assertThat(tradeEvents.numPartitions()).isEqualTo(3);

        assertThat(reconResults.name()).isEqualTo(KafkaTopicsConfig.RECON_RESULTS);
        assertThat(reconResults.numPartitions()).isEqualTo(2);

        assertThat(systemAlerts.name()).isEqualTo(KafkaTopicsConfig.SYSTEM_ALERTS);
        assertThat(systemAlerts.numPartitions()).isEqualTo(1);

        assertThat(tradeEventsDlq.name()).isEqualTo(KafkaTopicsConfig.TRADE_EVENTS_DLQ);
        assertThat(tradeEventsDlq.numPartitions()).isEqualTo(3);
    }
}
