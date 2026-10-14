package com.arunkalagoni.ledger.adapter.postgres;

import com.arunkalagoni.ledger.domain.LedgerEntry;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** JPA mapping for the ledger_entry table. See schema.sql for constraints and indexes. */
@Entity
@Table(name = "ledger_entry", indexes = {
        @Index(name = "idx_ledger_purchase_date", columnList = "purchase_date"),
        @Index(name = "idx_ledger_purchase_id", columnList = "purchase_id")})
public class LedgerEntryEntity {

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private String eventId;

    @Column(name = "purchase_id", nullable = false)
    private String purchaseId;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "net_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "gross_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "tax_rate", nullable = false, precision = 7, scale = 5)
    private BigDecimal taxRate;

    @Column(name = "purchased_at", nullable = false)
    private Instant purchasedAt;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    protected LedgerEntryEntity() {}

    LedgerEntry toDomain() {
        return new LedgerEntry(eventId, purchaseId, accountId, productId, countryCode, currency,
                netAmount, taxAmount, grossAmount, taxRate, purchasedAt);
    }
}
