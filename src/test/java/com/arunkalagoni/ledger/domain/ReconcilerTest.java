package com.arunkalagoni.ledger.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ReconcilerTest {

    private static final LocalDate DAY = LocalDate.parse("2026-10-14");

    private final InMemoryStores.Ledger ledger = new InMemoryStores.Ledger();
    private final InMemoryStores.History history = new InMemoryStores.History();
    private final TaxCalculator calculator = new TaxCalculator(TaxRules.withSampleDefaults());
    private final LedgerService service = new LedgerService(ledger, history, calculator);
    private final Reconciler reconciler = new Reconciler(ledger, history, calculator);

    @Test
    void balancedWhenBothStoresAgree() {
        service.record(LedgerServiceTest.event("e1"));
        service.record(LedgerServiceTest.event("e2"));

        ReconciliationReport report = reconciler.reconcile(DAY);

        assertThat(report.isBalanced()).isTrue();
        assertThat(report.ledgerCount()).isEqualTo(2);
    }

    @Test
    void detectsEventThatNeverReachedTheLedger() {
        service.record(LedgerServiceTest.event("e1"));
        history.append(LedgerServiceTest.event("lost"));

        ReconciliationReport report = reconciler.reconcile(DAY);

        assertThat(report.isBalanced()).isFalse();
        assertThat(report.missingFromLedger()).containsExactly("lost");
        assertThat(report.mismatchedTotals()).containsKey("US/USD");
    }

    @Test
    void detectsLedgerAmountThatWasAlteredAfterTheFact() {
        service.record(LedgerServiceTest.event("e1"));
        LedgerEntry original = ledger.rows.get("e1");
        ledger.rows.put("e1", new LedgerEntry(original.eventId(), original.purchaseId(),
                original.accountId(), original.productId(), original.countryCode(), original.currency(),
                new BigDecimal("1.00"), new BigDecimal("0.07"), new BigDecimal("1.07"),
                original.taxRate(), original.purchasedAt()));

        ReconciliationReport report = reconciler.reconcile(DAY);

        assertThat(report.missingFromLedger()).isEmpty();
        assertThat(report.mismatchedTotals()).containsKey("US/USD");
    }
}
