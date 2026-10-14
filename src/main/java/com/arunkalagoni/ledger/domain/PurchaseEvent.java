package com.arunkalagoni.ledger.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * A purchase event as it arrives from the upstream commerce system.
 *
 * <p>{@code eventId} is the idempotency key: the same purchase can be delivered more than once
 * (producer retries, consumer rebalances), and the ledger must record it exactly once.
 *
 * <p>{@code listPrice} is the price shown to the customer. Whether it already includes tax depends
 * on the country, so the ledger never stores it directly; {@link TaxCalculator} splits it into
 * net, tax, and gross amounts.
 */
public record PurchaseEvent(
        String eventId,
        String purchaseId,
        String accountId,
        String productId,
        String countryCode,
        String currency,
        BigDecimal listPrice,
        Instant purchasedAt) {

    public PurchaseEvent {
        requireText(eventId, "eventId");
        requireText(purchaseId, "purchaseId");
        requireText(accountId, "accountId");
        requireText(productId, "productId");
        requireText(countryCode, "countryCode");
        requireText(currency, "currency");
        Objects.requireNonNull(listPrice, "listPrice is required");
        Objects.requireNonNull(purchasedAt, "purchasedAt is required");
        if (listPrice.signum() < 0) {
            throw new IllegalArgumentException("listPrice must not be negative");
        }
        countryCode = countryCode.toUpperCase();
        currency = currency.toUpperCase();
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
    }
}
