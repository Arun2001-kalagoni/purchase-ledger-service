package com.arunkalagoni.ledger.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * One recorded purchase in the ledger, with the price already split into net, tax, and gross.
 * Invariant: {@code net + tax == gross}.
 */
public record LedgerEntry(
        String eventId,
        String purchaseId,
        String accountId,
        String productId,
        String countryCode,
        String currency,
        BigDecimal netAmount,
        BigDecimal taxAmount,
        BigDecimal grossAmount,
        BigDecimal taxRate,
        Instant purchasedAt) {

    public LedgerEntry {
        if (netAmount.add(taxAmount).compareTo(grossAmount) != 0) {
            throw new IllegalStateException(
                    "Ledger entry " + eventId + " is unbalanced: net + tax != gross");
        }
    }

    public static LedgerEntry from(PurchaseEvent event, TaxBreakdown tax) {
        return new LedgerEntry(
                event.eventId(),
                event.purchaseId(),
                event.accountId(),
                event.productId(),
                event.countryCode(),
                event.currency(),
                tax.net(),
                tax.tax(),
                tax.gross(),
                tax.rate(),
                event.purchasedAt());
    }
}
