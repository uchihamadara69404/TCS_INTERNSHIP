package com.yourco.bigout;

public record Rule(
    String loanTypePattern,
    Integer minTenure,
    Integer maxTenure,
    Double minAmount,
    Double maxAmount,
    String categoryName,
    Integer priority
) {}
