package com.example.sequencebox.domain

import com.example.sequencebox.model.AppRoundingMode
import com.example.sequencebox.model.SequenceResult
import com.example.sequencebox.model.SequenceType
import java.math.BigDecimal
import java.math.RoundingMode

class SequenceFormatter {

    fun formatNumber(
        value: BigDecimal,
        decimalPlaces: Int = 10,
        roundingMode: AppRoundingMode = AppRoundingMode.HALF_UP
    ): String {
        return formatBigDecimal(value, decimalPlaces, roundingMode.mathRoundingMode)
    }

    fun wasRounded(
        value: BigDecimal,
        decimalPlaces: Int,
        roundingMode: RoundingMode
    ): Boolean {
        val stripped = value.stripTrailingZeros()
        if (stripped.scale() <= decimalPlaces) {
            return false
        }
        val rounded = stripped.setScale(decimalPlaces, roundingMode).stripTrailingZeros()
        return stripped.compareTo(rounded) != 0
    }

    fun hasAnyRounding(
        result: SequenceResult,
        decimalPlaces: Int,
        roundingMode: RoundingMode
    ): Boolean {
        if (!result.isExact) return true
        if (wasRounded(result.sum, decimalPlaces, roundingMode)) return true
        for (term in result.terms) {
            if (wasRounded(term, decimalPlaces, roundingMode)) return true
        }
        return false
    }

    fun formatTermsList(
        terms: List<BigDecimal>,
        decimalPlaces: Int = 10,
        roundingMode: AppRoundingMode = AppRoundingMode.HALF_UP
    ): List<String> {
        return terms.map { formatBigDecimal(it, decimalPlaces, roundingMode.mathRoundingMode) }
    }

    fun formatCopyShareText(
        result: SequenceResult,
        decimalPlaces: Int = 10,
        roundingMode: AppRoundingMode = AppRoundingMode.HALF_UP
    ): String {
        val rMode = roundingMode.mathRoundingMode
        val firstFormatted = formatBigDecimal(result.firstTerm, decimalPlaces, rMode)
        val stepRatioFormatted = formatBigDecimal(result.stepOrRatio, decimalPlaces, rMode)
        val sumFormatted = formatBigDecimal(result.sum, decimalPlaces, rMode)
        val lastFormatted = formatBigDecimal(result.lastGeneratedTerm, decimalPlaces, rMode)
        val sequenceFormatted = result.terms.joinToString(", ") {
            formatBigDecimal(it, decimalPlaces, rMode)
        }

        return buildString {
            appendLine("SequenceBox")
            appendLine()
            if (result.type == SequenceType.ARITHMETIC) {
                appendLine("Arithmetic Sequence")
                appendLine()
                appendLine("First Term: $firstFormatted")
                appendLine("Common Difference: $stepRatioFormatted")
                appendLine("Terms: ${result.numberOfTerms}")
                appendLine()
                appendLine("Sequence:")
                appendLine(sequenceFormatted)
                appendLine()
                appendLine("Sum: $sumFormatted")
                appendLine("First Term: $firstFormatted")
                appendLine("Last Term: $lastFormatted")
                appendLine()
                appendLine("Formula:")
                append("aₙ = a₁ + (n - 1)d")
            } else {
                appendLine("Geometric Sequence")
                appendLine()
                appendLine("First Term: $firstFormatted")
                appendLine("Common Ratio: $stepRatioFormatted")
                appendLine("Terms: ${result.numberOfTerms}")
                appendLine()
                appendLine("Sequence:")
                appendLine(sequenceFormatted)
                appendLine()
                appendLine("Sum: $sumFormatted")
                appendLine("First Term: $firstFormatted")
                appendLine("Last Term: $lastFormatted")
                appendLine()
                appendLine("Formula:")
                append("aₙ = a₁ × rⁿ⁻¹")
            }
        }
    }

    private fun formatBigDecimal(
        value: BigDecimal,
        decimalPlaces: Int,
        roundingMode: RoundingMode
    ): String {
        val stripped = value.stripTrailingZeros()
        // If the number is an integer, scale might be negative (e.g. 100 with scale -2)
        if (stripped.scale() < 0) {
            return stripped.setScale(0).toPlainString()
        }
        val targetScale = decimalPlaces.coerceAtLeast(0)
        return if (stripped.scale() > targetScale) {
            val rounded = stripped.setScale(targetScale, roundingMode).stripTrailingZeros()
            if (rounded.scale() < 0) rounded.setScale(0).toPlainString() else rounded.toPlainString()
        } else {
            stripped.toPlainString()
        }
    }
}
