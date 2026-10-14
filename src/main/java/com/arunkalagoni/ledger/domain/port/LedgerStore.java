package com.arunkalagoni.ledger.domain.port;

import com.arunkalagoni.ledger.domain.LedgerEntry;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** The system of record for ledger entries (PostgreSQL in production). */
public interface LedgerStore {

    /**
     * Saves the entry unless one with the same {@code eventId} already exists.
     *
     * @return true if the entry was written, false if it was a duplicate
     */
    boolean saveIfAbsent(LedgerEntry entry);

    Optional<LedgerEntry> findByEventId(String eventId);

    List<LedgerEntry> findByPurchaseDate(LocalDate date);
}
