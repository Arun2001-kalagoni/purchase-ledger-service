package com.arunkalagoni.ledger.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

class LedgerServiceTest {

    private final InMemoryStores.Ledger ledger = new InMemoryStores.Ledger();
    private final InMemoryStores.History history = new InMemoryStores.History();
    private final LedgerService service =
            new LedgerService(ledger, history, new TaxCalculator(TaxRules.withSampleDefaults()));

    static PurchaseEvent event(String eventId) {
        return new PurchaseEvent(eventId, "p-" + eventId, "acct-1", "app-42", "us", "usd",
                new BigDecimal("4.99"), Instant.parse("2026-10-14T12:00:00Z"));
    }

    @Test
    void recordsANewEvent() {
        assertThat(service.record(event("e1"))).isEqualTo(LedgerService.Outcome.RECORDED);
        assertThat(ledger.rows).containsKey("e1");
        assertThat(history.rows).containsKey("e1");
    }

    @Test
    void redeliveredEventIsNotDoubleCounted() {
        service.record(event("e1"));

        assertThat(service.record(event("e1"))).isEqualTo(LedgerService.Outcome.DUPLICATE);
        assertThat(ledger.rows).hasSize(1);
    }

    @Test
    void concurrentDeliveriesOfTheSameEventRecordExactlyOnce() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Callable<LedgerService.Outcome>> tasks = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                tasks.add(() -> service.record(event("same")));
            }
            long recorded = 0;
            for (Future<LedgerService.Outcome> f : pool.invokeAll(tasks)) {
                if (f.get() == LedgerService.Outcome.RECORDED) {
                    recorded++;
                }
            }
            assertThat(recorded).isEqualTo(1);
            assertThat(ledger.rows).hasSize(1);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void normalizesCountryAndCurrency() {
        service.record(event("e1"));

        LedgerEntry entry = ledger.rows.get("e1");
        assertThat(entry.countryCode()).isEqualTo("US");
        assertThat(entry.currency()).isEqualTo("USD");
    }
}
