package com.arunkalagoni.ledger.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

/**
 * Result of reconciling one day.
 *
 * @param mismatchedTotals keyed by "COUNTRY/CURRENCY"; each value is {ledgerTotal, historyTotal}
 */
public record ReconciliationReport(
        LocalDate date,
        int ledgerCount,
        int historyCount,
        Set<String> missingFromLedger,
        Set<String> missingFromHistory,
        Map<String, BigDecimal[]> mismatchedTotals) {

    public boolean isBalanced() {
        return missingFromLedger.isEmpty() && missingFromHistory.isEmpty() && mismatchedTotals.isEmpty();
    }
}
