package com.arunkalagoni.ledger.domain.port;

import com.arunkalagoni.ledger.domain.PurchaseEvent;
import java.time.LocalDate;
import java.util.List;

/**
 * Append-only history of raw purchase events (Cassandra in production), partitioned by purchase
 * date so a whole day can be read back for reconciliation.
 */
public interface EventHistoryStore {

    void append(PurchaseEvent event);

    List<PurchaseEvent> findByPurchaseDate(LocalDate date);
}
