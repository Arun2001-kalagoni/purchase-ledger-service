package com.arunkalagoni.ledger.api;

import com.arunkalagoni.ledger.domain.LedgerEntry;
import com.arunkalagoni.ledger.domain.Reconciler;
import com.arunkalagoni.ledger.domain.ReconciliationReport;
import com.arunkalagoni.ledger.domain.port.LedgerStore;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class LedgerController {

    private final LedgerStore ledger;
    private final Reconciler reconciler;

    public LedgerController(LedgerStore ledger, Reconciler reconciler) {
        this.ledger = ledger;
        this.reconciler = reconciler;
    }

    @GetMapping("/ledger/{eventId}")
    public ResponseEntity<LedgerEntry> getEntry(@PathVariable String eventId) {
        return ledger.findByEventId(eventId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/reconciliation")
    public ReconciliationReport reconcile(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return reconciler.reconcile(date);
    }
}
