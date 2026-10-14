package com.arunkalagoni.ledger.domain;

import com.arunkalagoni.ledger.domain.port.EventHistoryStore;
import com.arunkalagoni.ledger.domain.port.LedgerStore;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Checks that the ledger and the raw event history agree for a given day.
 *
 * <p>Two independent stores should describe the same set of purchases. Reconciliation catches
 * events that reached history but never landed in the ledger (or the reverse), and compares
 * per-country, per-currency gross totals by recomputing tax from the raw events.
 */
public final class Reconciler {

    private final LedgerStore ledger;
    private final EventHistoryStore history;
    private final TaxCalculator taxCalculator;

    public Reconciler(LedgerStore ledger, EventHistoryStore history, TaxCalculator taxCalculator) {
        this.ledger = ledger;
        this.history = history;
        this.taxCalculator = taxCalculator;
    }

    public ReconciliationReport reconcile(LocalDate date) {
        List<LedgerEntry> entries = ledger.findByPurchaseDate(date);
        List<PurchaseEvent> events = history.findByPurchaseDate(date);

        Set<String> ledgerIds = entries.stream().map(LedgerEntry::eventId).collect(Collectors.toSet());
        Set<String> historyIds = events.stream().map(PurchaseEvent::eventId).collect(Collectors.toSet());

        Set<String> missingFromLedger = new HashSet<>(historyIds);
        missingFromLedger.removeAll(ledgerIds);
        Set<String> missingFromHistory = new HashSet<>(ledgerIds);
        missingFromHistory.removeAll(historyIds);

        Map<String, BigDecimal> ledgerTotals = totals(entries, e -> key(e.countryCode(), e.currency()),
                LedgerEntry::grossAmount);
        Map<String, BigDecimal> historyTotals = totals(events, e -> key(e.countryCode(), e.currency()),
                e -> taxCalculator.calculate(e.countryCode(), e.listPrice()).gross());

        Set<String> keys = new HashSet<>(ledgerTotals.keySet());
        keys.addAll(historyTotals.keySet());
        Map<String, BigDecimal[]> mismatchedTotals = new TreeMap<>();
        for (String k : keys) {
            BigDecimal l = ledgerTotals.getOrDefault(k, BigDecimal.ZERO);
            BigDecimal h = historyTotals.getOrDefault(k, BigDecimal.ZERO);
            if (l.compareTo(h) != 0) {
                mismatchedTotals.put(k, new BigDecimal[] {l, h});
            }
        }

        return new ReconciliationReport(date, entries.size(), events.size(),
                Set.copyOf(missingFromLedger), Set.copyOf(missingFromHistory), mismatchedTotals);
    }

    private static String key(String country, String currency) {
        return country + "/" + currency;
    }

    private static <T> Map<String, BigDecimal> totals(List<T> items, Function<T, String> keyFn,
                                                      Function<T, BigDecimal> amountFn) {
        return items.stream().collect(Collectors.groupingBy(keyFn,
                Collectors.reducing(BigDecimal.ZERO, amountFn, BigDecimal::add)));
    }
}
