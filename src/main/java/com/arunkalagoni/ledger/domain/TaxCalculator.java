package com.arunkalagoni.ledger.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Splits a list price into net, tax, and gross amounts using the country's rule.
 *
 * <p>Money is always {@link BigDecimal} at scale 2 with banker's rounding, and tax is derived as
 * {@code gross - net} (inclusive) or {@code net * rate} (exclusive) so the three amounts always
 * balance exactly, with no rounding drift.
 */
public final class TaxCalculator {

    private static final int MONEY_SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;

    private final TaxRules rules;

    public TaxCalculator(TaxRules rules) {
        this.rules = rules;
    }

    public TaxBreakdown calculate(String countryCode, BigDecimal listPrice) {
        TaxRule rule = rules.forCountry(countryCode)
                .orElseThrow(() -> new UnknownCountryException(countryCode));

        BigDecimal price = listPrice.setScale(MONEY_SCALE, ROUNDING);

        if (rule.taxInclusive()) {
            BigDecimal gross = price;
            BigDecimal net = gross.divide(BigDecimal.ONE.add(rule.rate()), MONEY_SCALE, ROUNDING);
            return new TaxBreakdown(net, gross.subtract(net), gross, rule.rate());
        }

        BigDecimal net = price;
        BigDecimal tax = net.multiply(rule.rate()).setScale(MONEY_SCALE, ROUNDING);
        return new TaxBreakdown(net, tax, net.add(tax), rule.rate());
    }

    /** Thrown when a purchase arrives for a country with no configured rule. */
    public static final class UnknownCountryException extends RuntimeException {
        public UnknownCountryException(String countryCode) {
            super("No tax rule configured for country " + countryCode);
        }
    }
}
