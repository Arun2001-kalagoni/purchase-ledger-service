package com.arunkalagoni.ledger.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class TaxCalculatorTest {

    private final TaxRules rules = TaxRules.withSampleDefaults();
    private final TaxCalculator calculator = new TaxCalculator(rules);

    @Test
    void addsTaxOnTopWhenCountryIsTaxExclusive() {
        TaxBreakdown t = calculator.calculate("US", new BigDecimal("9.99"));

        assertThat(t.net()).isEqualByComparingTo("9.99");
        assertThat(t.tax()).isEqualByComparingTo("0.72");
        assertThat(t.gross()).isEqualByComparingTo("10.71");
    }

    @Test
    void extractsTaxFromPriceWhenCountryIsTaxInclusive() {
        TaxBreakdown t = calculator.calculate("DE", new BigDecimal("9.99"));

        assertThat(t.gross()).isEqualByComparingTo("9.99");
        assertThat(t.net()).isEqualByComparingTo("8.39");
        assertThat(t.tax()).isEqualByComparingTo("1.60");
    }

    @Test
    void amountsAlwaysBalance() {
        for (String country : new String[] {"US", "GB", "DE", "IN", "JP"}) {
            for (String price : new String[] {"0.99", "1.00", "4.99", "13.37", "99.99", "1234.56"}) {
                TaxBreakdown t = calculator.calculate(country, new BigDecimal(price));
                assertThat(t.net().add(t.tax())).isEqualByComparingTo(t.gross());
            }
        }
    }

    @Test
    void ruleChangesTakeEffectImmediately() {
        rules.put(new TaxRule("DE", new BigDecimal("0.16"), true));

        assertThat(calculator.calculate("DE", new BigDecimal("11.60")).net()).isEqualByComparingTo("10.00");
    }

    @Test
    void rejectsUnknownCountry() {
        assertThatThrownBy(() -> calculator.calculate("ZZ", BigDecimal.ONE))
                .isInstanceOf(TaxCalculator.UnknownCountryException.class);
    }
}
