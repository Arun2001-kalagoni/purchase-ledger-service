package com.arunkalagoni.ledger.adapter.postgres;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface LedgerEntryJpaRepository extends JpaRepository<LedgerEntryEntity, String> {

    List<LedgerEntryEntity> findByPurchaseDate(LocalDate purchaseDate);

    /**
     * Inserts the row unless the event_id already exists. Returns the number of rows inserted
     * (1 or 0), so the duplicate check is atomic in the database rather than in Java.
     */
    @Modifying
    @Query(value = """
            INSERT INTO ledger_entry (event_id, purchase_id, account_id, product_id, country_code,
                currency, net_amount, tax_amount, gross_amount, tax_rate, purchased_at, purchase_date)
            VALUES (:eventId, :purchaseId, :accountId, :productId, :countryCode, :currency,
                :netAmount, :taxAmount, :grossAmount, :taxRate, :purchasedAt, :purchaseDate)
            ON CONFLICT (event_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("eventId") String eventId,
                       @Param("purchaseId") String purchaseId,
                       @Param("accountId") String accountId,
                       @Param("productId") String productId,
                       @Param("countryCode") String countryCode,
                       @Param("currency") String currency,
                       @Param("netAmount") BigDecimal netAmount,
                       @Param("taxAmount") BigDecimal taxAmount,
                       @Param("grossAmount") BigDecimal grossAmount,
                       @Param("taxRate") BigDecimal taxRate,
                       @Param("purchasedAt") Instant purchasedAt,
                       @Param("purchaseDate") LocalDate purchaseDate);
}
