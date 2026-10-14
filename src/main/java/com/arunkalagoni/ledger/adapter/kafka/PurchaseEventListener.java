package com.arunkalagoni.ledger.adapter.kafka;

import com.arunkalagoni.ledger.domain.LedgerService;
import com.arunkalagoni.ledger.domain.PurchaseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes purchase events and records them in the ledger.
 *
 * <p>Events are keyed by purchaseId upstream, so all events for one purchase land on the same
 * partition and are processed in order. Offsets are committed only after the ledger write
 * succeeds; a crash before commit causes redelivery, which {@link LedgerService} absorbs through
 * its idempotency key.
 */
@Component
public class PurchaseEventListener {

    private static final Logger log = LoggerFactory.getLogger(PurchaseEventListener.class);

    private final LedgerService ledgerService;
    private final ObjectMapper mapper;

    public PurchaseEventListener(LedgerService ledgerService, ObjectMapper mapper) {
        this.ledgerService = ledgerService;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "${ledger.kafka.topic}", groupId = "${ledger.kafka.group-id}")
    public void onMessage(String payload) throws JsonProcessingException {
        PurchaseEvent event = mapper.readValue(payload, PurchaseEvent.class);
        LedgerService.Outcome outcome = ledgerService.record(event);
        log.info("purchase event {} for purchase {}: {}", event.eventId(), event.purchaseId(), outcome);
    }
}
