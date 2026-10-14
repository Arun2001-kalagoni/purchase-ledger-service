package com.arunkalagoni.ledger.reconciliation;

import com.arunkalagoni.ledger.domain.Reconciler;
import com.arunkalagoni.ledger.domain.ReconciliationReport;
import java.time.Clock;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Reconciles the previous UTC day every morning and logs any mismatch for alerting. */
@Component
public class DailyReconciliationJob {

    private static final Logger log = LoggerFactory.getLogger(DailyReconciliationJob.class);

    private final Reconciler reconciler;
    private final Clock clock = Clock.systemUTC();

    public DailyReconciliationJob(Reconciler reconciler) {
        this.reconciler = reconciler;
    }

    @Scheduled(cron = "${ledger.reconciliation.cron}", zone = "UTC")
    public void run() {
        LocalDate yesterday = LocalDate.now(clock).minusDays(1);
        ReconciliationReport report = reconciler.reconcile(yesterday);
        if (report.isBalanced()) {
            log.info("reconciliation {} balanced: {} entries", yesterday, report.ledgerCount());
        } else {
            log.error("reconciliation {} MISMATCH: missingFromLedger={}, missingFromHistory={}, totals={}",
                    yesterday, report.missingFromLedger(), report.missingFromHistory(),
                    report.mismatchedTotals().keySet());
        }
    }
}
