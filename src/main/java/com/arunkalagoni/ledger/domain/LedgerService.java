package com.arunkalagoni.ledger.domain;

import com.arunkalagoni.ledger.domain.port.EventHistoryStore;
import com.arunkalagoni.ledger.domain.port.LedgerStore;

/**
 * Records purchase events in the ledger exactly once.
 *
 * <p>The raw event is written to history first, then the ledger write is guarded by the event's
 * idempotency key. If the same event is delivered again, history may receive it twice, but the
 * history store is keyed by eventId too, so the duplicate overwrites itself, and the ledger skips it.
 */
public final class LedgerService {

    public enum Outcome { RECORDED, DUPLICATE }

    private final LedgerStore ledger;
    private final EventHistoryStore history;
    private final TaxCalculator taxCalculator;

    public LedgerService(LedgerStore ledger, EventHistoryStore history, TaxCalculator taxCalculator) {
        this.ledger = ledger;
        this.history = history;
        this.taxCalculator = taxCalculator;
    }

    public Outcome record(PurchaseEvent event) {
        if (ledger.findByEventId(event.eventId()).isPresent()) {
            return Outcome.DUPLICATE;
        }

        TaxBreakdown tax = taxCalculator.calculate(event.countryCode(), event.listPrice());
        LedgerEntry entry = LedgerEntry.from(event, tax);

        history.append(event);
        // saveIfAbsent is the real guard: two consumers racing on the same event both pass the
        // check above, but only one insert succeeds against the unique eventId constraint.
        return ledger.saveIfAbsent(entry) ? Outcome.RECORDED : Outcome.DUPLICATE;
    }
}
