package com.example.sequencebox.model

import java.math.BigDecimal

data class SequenceResult(
    val type: SequenceType,
    val firstTerm: BigDecimal,
    val stepOrRatio: BigDecimal,
    val numberOfTerms: Int,
    val terms: List<BigDecimal>,
    val sum: BigDecimal,
    val minimum: BigDecimal,
    val maximum: BigDecimal,
    val firstGeneratedTerm: BigDecimal,
    val lastGeneratedTerm: BigDecimal,
    val formula: String,
    val direction: SequenceDirection,
    val isExact: Boolean,
    val calculatedAt: Long
)
