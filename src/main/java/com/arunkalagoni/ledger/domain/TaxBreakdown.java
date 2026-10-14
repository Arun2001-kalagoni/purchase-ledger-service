package com.arunkalagoni.ledger.domain;

import java.math.BigDecimal;

/** The result of applying a country's tax rule to a list price. */
public record TaxBreakdown(BigDecimal net, BigDecimal tax, BigDecimal gross, BigDecimal rate) {}
