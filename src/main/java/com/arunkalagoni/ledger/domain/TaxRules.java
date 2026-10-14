package com.arunkalagoni.ledger.domain;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of country tax rules.
 *
 * <p>Rules change when local laws change, so they are data rather than code: callers can replace a
 * rule at runtime with {@link #put(TaxRule)}. The defaults below are illustrative sample rates for
 * development, not authoritative tax guidance.
 */
public final class TaxRules {

    private final Map<String, TaxRule> rules = new ConcurrentHashMap<>();

    public static TaxRules withSampleDefaults() {
        TaxRules registry = new TaxRules();
        registry.put(new TaxRule("US", new BigDecimal("0.0725"), false));
        registry.put(new TaxRule("GB", new BigDecimal("0.20"), true));
        registry.put(new TaxRule("DE", new BigDecimal("0.19"), true));
        registry.put(new TaxRule("IN", new BigDecimal("0.18"), true));
        registry.put(new TaxRule("JP", new BigDecimal("0.10"), true));
        return registry;
    }

    public void put(TaxRule rule) {
        rules.put(rule.countryCode(), rule);
    }

    public Optional<TaxRule> forCountry(String countryCode) {
        return Optional.ofNullable(rules.get(countryCode.toUpperCase()));
    }
}
