package com.arunkalagoni.ledger.adapter.cassandra;

import com.arunkalagoni.ledger.domain.PurchaseEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * Cassandra row for the raw event history.
 *
 * <p>Partition key: purchase_date, so reconciliation reads one day from a single partition.
 * Clustering key: event_id, so writing the same event twice is an upsert, not a duplicate.
 */
@Table("purchase_event_history")
public class PurchaseEventRow {

    @PrimaryKeyColumn(name = "purchase_date", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private LocalDate purchaseDate;

    @PrimaryKeyColumn(name = "event_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private String eventId;

    @Column("purchase_id") private String purchaseId;
    @Column("account_id") private String accountId;
    @Column("product_id") private String productId;
    @Column("country_code") private String countryCode;
    @Column("currency") private String currency;
    @Column("list_price") private BigDecimal listPrice;
    @Column("purchased_at") private Instant purchasedAt;

    protected PurchaseEventRow() {}

    static PurchaseEventRow from(PurchaseEvent e) {
        PurchaseEventRow row = new PurchaseEventRow();
        row.purchaseDate = LocalDate.ofInstant(e.purchasedAt(), ZoneOffset.UTC);
        row.eventId = e.eventId();
        row.purchaseId = e.purchaseId();
        row.accountId = e.accountId();
        row.productId = e.productId();
        row.countryCode = e.countryCode();
        row.currency = e.currency();
        row.listPrice = e.listPrice();
        row.purchasedAt = e.purchasedAt();
        return row;
    }

    PurchaseEvent toDomain() {
        return new PurchaseEvent(eventId, purchaseId, accountId, productId, countryCode, currency,
                listPrice, purchasedAt);
    }
}
