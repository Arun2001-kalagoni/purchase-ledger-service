package com.arunkalagoni.ledger.adapter.postgres;

import com.arunkalagoni.ledger.domain.LedgerEntry;
import com.arunkalagoni.ledger.domain.port.LedgerStore;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PostgresLedgerStore implements LedgerStore {

    private final LedgerEntryJpaRepository repository;

    public PostgresLedgerStore(LedgerEntryJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public boolean saveIfAbsent(LedgerEntry entry) {
        return repository.insertIfAbsent(
                entry.eventId(), entry.purchaseId(), entry.accountId(), entry.productId(),
                entry.countryCode(), entry.currency(), entry.netAmount(), entry.taxAmount(),
                entry.grossAmount(), entry.taxRate(), entry.purchasedAt(),
                LocalDate.ofInstant(entry.purchasedAt(), ZoneOffset.UTC)) == 1;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LedgerEntry> findByEventId(String eventId) {
        return repository.findById(eventId).map(LedgerEntryEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LedgerEntry> findByPurchaseDate(LocalDate date) {
        return repository.findByPurchaseDate(date).stream().map(LedgerEntryEntity::toDomain).toList();
    }
}
