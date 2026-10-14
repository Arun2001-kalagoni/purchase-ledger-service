package com.arunkalagoni.ledger.config;

import com.arunkalagoni.ledger.domain.LedgerService;
import com.arunkalagoni.ledger.domain.Reconciler;
import com.arunkalagoni.ledger.domain.TaxCalculator;
import com.arunkalagoni.ledger.domain.TaxRules;
import com.arunkalagoni.ledger.domain.port.EventHistoryStore;
import com.arunkalagoni.ledger.domain.port.LedgerStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the framework-free domain classes into Spring. */
@Configuration
public class DomainConfig {

    @Bean
    public TaxRules taxRules() {
        return TaxRules.withSampleDefaults();
    }

    @Bean
    public TaxCalculator taxCalculator(TaxRules rules) {
        return new TaxCalculator(rules);
    }

    @Bean
    public LedgerService ledgerService(LedgerStore ledger, EventHistoryStore history, TaxCalculator calc) {
        return new LedgerService(ledger, history, calc);
    }

    @Bean
    public Reconciler reconciler(LedgerStore ledger, EventHistoryStore history, TaxCalculator calc) {
        return new Reconciler(ledger, history, calc);
    }
}
