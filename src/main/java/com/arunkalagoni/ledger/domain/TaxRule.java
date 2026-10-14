package com.arunkalagoni.ledger.domain;

import java.math.BigDecimal;

/**
 * How tax works in one country.
 *
 * @param countryCode   ISO 3166-1 alpha-2 code, for example "US" or "DE"
 * @param rate          tax rate as a fraction, for example 0.19 for 19%
 * @param taxInclusive  true when shown prices already include tax (common for VAT countries);
 *                      false when tax is added on top of the shown price
 */
public record TaxRule(String countryCode, BigDecimal rate, boolean taxInclusive) {

    public TaxRule {
        if (rate.signum() < 0 || rate.compareTo(BigDecimal.ONE) >= 0) {
            throw new IllegalArgumentException("rate must be in [0, 1): " + rate);
        }
        countryCode = countryCode.toUpperCase();
    }
}
