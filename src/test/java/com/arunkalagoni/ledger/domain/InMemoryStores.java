package com.arunkalagoni.ledger.domain;

import com.arunkalagoni.ledger.domain.port.EventHistoryStore;
import com.arunkalagoni.ledger.domain.port.LedgerStore;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory implementations of the storage ports, used to test domain logic without databases. */
final class InMemoryStores {

    private InMemoryStores() {}

    static final class Ledger implements LedgerStore {
        final Map<String, LedgerEntry> rows = new ConcurrentHashMap<>();

        @Override
        public boolean saveIfAbsent(LedgerEntry entry) {
            return rows.putIfAbsent(entry.eventId(), entry) == null;
        }

        @Override
        public Optional<LedgerEntry> findByEventId(String eventId) {
            return Optional.ofNullable(rows.get(eventId));
        }

        @Override
        public List<LedgerEntry> findByPurchaseDate(LocalDate date) {
            return rows.values().stream()
                    .filter(e -> LocalDate.ofInstant(e.purchasedAt(), ZoneOffset.UTC).equals(date))
                    .toList();
        }
    }

    static final class History implements EventHistoryStore {
        final Map<String, PurchaseEvent> rows = new ConcurrentHashMap<>();

        @Override
        public void append(PurchaseEvent event) {
            rows.put(event.eventId(), event); // upsert by eventId, like the Cassandra clustering key
        }

        @Override
        public List<PurchaseEvent> findByPurchaseDate(LocalDate date) {
            return rows.values().stream()
                    .filter(e -> LocalDate.ofInstant(e.purchasedAt(), ZoneOffset.UTC).equals(date))
                    .toList();
        }
    }
}
