package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.TradeRequest;
import com.dbtraining.reconx.exception.DuplicateTradeRefException;
import com.dbtraining.reconx.exception.InvalidTradeException;
import com.dbtraining.reconx.exception.TradeNotFoundException;
import com.dbtraining.reconx.kafka.TradeEventProducer;
import com.dbtraining.reconx.observability.TradeMetrics;
import com.dbtraining.reconx.repository.CounterpartyRepository;
import com.dbtraining.reconx.repository.InstrumentRepository;
import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.entity.Trade;
import com.dbtraining.reconx.dto.TradeEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static com.dbtraining.reconx.repository.TradeSpecifications.*;

/**
 * ============================================================================
 * TICKET-ADV064 — TradeService.create (POST endpoint backing)
 * TICKET-ADV065 — update
 * TICKET-ADV066 — updateStatus (PATCH)
 * TICKET-ADV067 — softDelete
 * TICKET-ADV083 — increments trade_created_total Counter on create
 * TICKET-ADV129 — publishes TradeEvent on every state change
 * TICKET-ADV055/ADV056 — list() uses Specifications + filter query
 * ============================================================================
 */
@Service
@Transactional
public class TradeService {

    private static final Set<String> VALID_STATUSES = Set.of("PENDING", "MATCHED", "UNMATCHED", "DISPUTED", "CANCELLED", "NEW");

    private final TradeRepository tradeRepo;
    private final CounterpartyRepository cpRepo;
    private final InstrumentRepository instRepo;
    private final TradeEventProducer events;
    private final TradeMetrics metrics;

    public TradeService(TradeRepository tradeRepo,
                        CounterpartyRepository cpRepo,
                        InstrumentRepository instRepo,
                        TradeEventProducer events,
                        TradeMetrics metrics) {
        this.tradeRepo = tradeRepo;
        this.cpRepo = cpRepo;
        this.instRepo = instRepo;
        this.events = events;
        this.metrics = metrics;
    }

    private static void initLazyRelations(Trade t) {
        if (t != null) {
            if (t.getInstrument() != null) t.getInstrument().getSymbol();
            if (t.getCounterparty() != null) t.getCounterparty().getName();
        }
    }

    public Trade create(TradeRequest req, String actor) {
        tradeRepo.findByTradeRef(req.tradeRef()).ifPresent(t -> {
            throw new DuplicateTradeRefException(req.tradeRef());
        });
        var instrument = instRepo.findById(req.instrumentId())
                .orElseThrow(() -> new TradeNotFoundException("instrument " + req.instrumentId()));
        var counterparty = cpRepo.findById(req.counterpartyId())
                .orElseThrow(() -> new TradeNotFoundException("counterparty " + req.counterpartyId()));

        var t = new Trade();
        t.setTradeRef(req.tradeRef());
        t.setInstrument(instrument);
        t.setCounterparty(counterparty);
        t.setAssetClass(req.assetClass());
        t.setSide(req.side());
        t.setQuantity(req.quantity());
        t.setPrice(req.price());
        t.setTradeDate(req.tradeDate());
        t.setStatus("PENDING");
        Trade saved = tradeRepo.save(t);
        initLazyRelations(saved);

        metrics.incrementTradeCreated();
        metrics.recordTradeValue(req.quantity().multiply(req.price()).doubleValue());
        events.publish(new TradeEvent(UUID.randomUUID(), saved.getTradeRef(),
                TradeEvent.EventType.TRADE_CREATED, Instant.now(), actor,
                null, "status=PENDING"));
        return saved;
    }

    public Trade update(Long id, TradeRequest req, String actor) {
        var t = tradeRepo.findById(id)
                .orElseThrow(() -> new TradeNotFoundException("id " + id));
        String before = "status=" + t.getStatus() + ",qty=" + t.getQuantity() + ",price=" + t.getPrice();

        var instrument = instRepo.findById(req.instrumentId())
                .orElseThrow(() -> new TradeNotFoundException("instrument " + req.instrumentId()));
        var counterparty = cpRepo.findById(req.counterpartyId())
                .orElseThrow(() -> new TradeNotFoundException("counterparty " + req.counterpartyId()));

        t.setInstrument(instrument);
        t.setCounterparty(counterparty);
        t.setAssetClass(req.assetClass());
        t.setSide(req.side());
        t.setQuantity(req.quantity());
        t.setPrice(req.price());
        t.setTradeDate(req.tradeDate());
        Trade saved = tradeRepo.save(t);
        initLazyRelations(saved);

        events.publish(new TradeEvent(UUID.randomUUID(), saved.getTradeRef(),
                TradeEvent.EventType.TRADE_UPDATED, Instant.now(), actor,
                before, "qty=" + saved.getQuantity() + ",price=" + saved.getPrice()));
        return saved;
    }

    public Trade updateStatus(Long id, String status, String actor) {
        if (status == null || !VALID_STATUSES.contains(status.toUpperCase())) {
            throw new InvalidTradeException("Invalid trade status: " + status);
        }
        var t = tradeRepo.findById(id)
                .orElseThrow(() -> new TradeNotFoundException("id " + id));
        String before = "status=" + t.getStatus();
        t.setStatus(status.toUpperCase());
        Trade saved = tradeRepo.save(t);
        initLazyRelations(saved);

        events.publish(new TradeEvent(UUID.randomUUID(), saved.getTradeRef(),
                TradeEvent.EventType.TRADE_UPDATED, Instant.now(), actor,
                before, "status=" + status));
        return saved;
    }

    public void softDelete(Long id, String actor) {
        Trade t = tradeRepo.findById(id)
                    .orElseThrow(() -> new TradeNotFoundException("id=" + id));
        t.softDelete();
        tradeRepo.save(t);
        events.publish(new TradeEvent(UUID.randomUUID(), t.getTradeRef(),
            TradeEvent.EventType.TRADE_CANCELLED, Instant.now(), actor, null, null));
        }

    @Transactional(readOnly = true)
    public Trade findById(Long id) {
        Trade t = tradeRepo.findById(id)
                .orElseThrow(() -> new TradeNotFoundException("id " + id));
        initLazyRelations(t);
        return t;
    }

    @Transactional(readOnly = true)
    public Page<Trade> list(LocalDate from, LocalDate to, String status, Long counterpartyId, Pageable pageable) {
        Specification<Trade> spec = Specification
                .where(tradeDateBetween(from, to))
                .and(hasStatus(status))
                .and(hasCounterparty(counterpartyId));
        return tradeRepo.findAll(spec, pageable);
    }
}
