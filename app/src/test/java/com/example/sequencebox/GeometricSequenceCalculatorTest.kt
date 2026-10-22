package com.example.sequencebox

import com.example.sequencebox.domain.GeometricSequenceCalculator
import com.example.sequencebox.model.SequenceDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.BigInteger

class GeometricSequenceCalculatorTest {

    private val calculator = GeometricSequenceCalculator()

    // 9. Geometric sequence generation: Start = 2, Ratio = 3, Terms = 6 -> 2, 6, 18, 54, 162, 486
    @Test
    fun testGeometricSequenceGeneration_basic() {
        val result = calculator.calculateGeometricSequence(
            firstTerm = BigDecimal("2"),
            ratio = BigDecimal("3"),
            numberOfTerms = 6
        ).getOrThrow()

        val expected = listOf("2", "6", "18", "54", "162", "486")
        val actual = result.terms.map { it.stripTrailingZeros().toPlainString() }
        assertEquals(expected, actual)
        assertEquals(BigDecimal("486"), result.lastGeneratedTerm.stripTrailingZeros())
        assertEquals(BigDecimal("728"), result.sum.stripTrailingZeros())
        assertEquals(SequenceDirection.INCREASING, result.direction)
    }

    // 13. Negative geometric ratio: 2, -2, 6 terms -> 2, -4, 8, -16, 32, -64 (Direction: Alternating)
    @Test
    fun testNegativeGeometricRatio() {
        val result = calculator.calculateGeometricSequence(
            firstTerm = BigDecimal("2"),
            ratio = BigDecimal("-2"),
            numberOfTerms = 6
        ).getOrThrow()

        val expected = listOf("2", "-4", "8", "-16", "32", "-64")
        val actual = result.terms.map { it.stripTrailingZeros().toPlainString() }
        assertEquals(expected, actual)
        assertEquals(SequenceDirection.ALTERNATING, result.direction)
    }

    // 14. Fractional geometric ratio: 16, 0.5, 5 terms -> 16, 8, 4, 2, 1
    @Test
    fun testFractionalGeometricRatio() {
        val result = calculator.calculateGeometricSequence(
            firstTerm = BigDecimal("16"),
            ratio = BigDecimal("0.5"),
            numberOfTerms = 5
        ).getOrThrow()

        val expected = listOf("16", "8", "4", "2", "1")
        val actual = result.terms.map { it.stripTrailingZeros().toPlainString() }
        assertEquals(expected, actual)
        assertEquals(BigDecimal("31"), result.sum.stripTrailingZeros())
        assertEquals(SequenceDirection.DECREASING, result.direction)
    }

    // 15. Zero geometric ratio: First = 5, Ratio = 0, Terms = 6 -> 5, 0, 0, 0, 0, 0, Sum = 5
    @Test
    fun testZeroGeometricRatio() {
        val result = calculator.calculateGeometricSequence(
            firstTerm = BigDecimal("5"),
            ratio = BigDecimal("0"),
            numberOfTerms = 6
        ).getOrThrow()

        val expected = listOf("5", "0", "0", "0", "0", "0")
        val actual = result.terms.map { it.stripTrailingZeros().toPlainString() }
        assertEquals(expected, actual)
        assertEquals(BigDecimal("5"), result.sum.stripTrailingZeros())
    }

    // 16. Ratio of one: First = 5, Ratio = 1, Terms = 6 -> 5, 5, 5, 5, 5, 5, Sum = 30
    @Test
    fun testRatioOfOne() {
        val result = calculator.calculateGeometricSequence(
            firstTerm = BigDecimal("5"),
            ratio = BigDecimal("1"),
            numberOfTerms = 6
        ).getOrThrow()

        val expected = listOf("5", "5", "5", "5", "5", "5")
        val actual = result.terms.map { it.stripTrailingZeros().toPlainString() }
        assertEquals(expected, actual)
        assertEquals(0, BigDecimal("30").compareTo(result.sum))
        assertEquals(SequenceDirection.CONSTANT, result.direction)
    }

    // 17. First term zero: First = 0, Ratio = 5, Terms = 5 -> 0, 0, 0, 0, 0, Sum = 0
    @Test
    fun testFirstTermZeroGeometric() {
        val result = calculator.calculateGeometricSequence(
            firstTerm = BigDecimal("0"),
            ratio = BigDecimal("5"),
            numberOfTerms = 5
        ).getOrThrow()

        val expected = listOf("0", "0", "0", "0", "0")
        val actual = result.terms.map { it.stripTrailingZeros().toPlainString() }
        assertEquals(expected, actual)
        assertEquals(BigDecimal("0"), result.sum.stripTrailingZeros())
        assertEquals(SequenceDirection.CONSTANT, result.direction)
    }

    // 18. One-term sequence
    @Test
    fun testOneTermGeometric() {
        val result = calculator.calculateGeometricSequence(
            firstTerm = BigDecimal("25"),
            ratio = BigDecimal("2"),
            numberOfTerms = 1
        ).getOrThrow()

        assertEquals(1, result.terms.size)
        assertEquals(BigDecimal("25"), result.terms.first().stripTrailingZeros())
        assertEquals(BigDecimal("25"), result.sum.stripTrailingZeros())
        assertEquals(SequenceDirection.CONSTANT, result.direction)
    }

    // 20. Geometric nth term: a₁ = 2, r = 3, n = 10 -> a₁₀ = 2 * 3^9 = 39366
    @Test
    fun testGeometricNthTerm() {
        val term10 = calculator.calculateGeometricTerm(
            firstTerm = BigDecimal("2"),
            ratio = BigDecimal("3"),
            termNumber = 10
        )
        assertEquals(BigDecimal("39366"), term10.stripTrailingZeros())
    }

    // 22. Geometric sum: 2, 3, 6 terms -> Sum = 728
    @Test
    fun testGeometricSum() {
        val result = calculator.calculateGeometricSequence(
            firstTerm = BigDecimal("2"),
            ratio = BigDecimal("3"),
            numberOfTerms = 6
        ).getOrThrow()

        assertEquals(BigDecimal("728"), result.sum.stripTrailingZeros())
    }

    // 23. Geometric sum ratio = 1
    @Test
    fun testGeometricSumRatioOne() {
        val result = calculator.calculateGeometricSequence(
            firstTerm = BigDecimal("5"),
            ratio = BigDecimal("1"),
            numberOfTerms = 6
        ).getOrThrow()

        assertEquals(0, BigDecimal("30").compareTo(result.sum))
    }

    // 24 & 25. Minimum and Maximum: 2, -4, 8, -16 -> Max = 8, Min = -16
    @Test
    fun testGeometricMinMax() {
        val result = calculator.calculateGeometricSequence(
            firstTerm = BigDecimal("2"),
            ratio = BigDecimal("-2"),
            numberOfTerms = 4
        ).getOrThrow()

        assertEquals(BigDecimal("-16"), result.minimum.stripTrailingZeros())
        assertEquals(BigDecimal("8"), result.maximum.stripTrailingZeros())
    }

    // 29 & 30. Alternating and Mixed detection
    @Test
    fun testAlternatingAndMixedDetection() {
        val alt = calculator.calculateGeometricSequence(BigDecimal("2"), BigDecimal("-2"), 4).getOrThrow()
        assertEquals(SequenceDirection.ALTERNATING, alt.direction)

        val mixed = calculator.calculateGeometricSequence(BigDecimal("5"), BigDecimal("0"), 4).getOrThrow()
        assertEquals(SequenceDirection.MIXED, mixed.direction)
    }

    // 33. Large geometric powers: exponentiation by squaring
    @Test
    fun testLargeGeometricPowers() {
        val base = BigDecimal("2")
        val power = calculator.powerBySquaring(base, BigInteger.valueOf(30))
        // 2^30 = 1073741824
        assertEquals(BigDecimal("1073741824"), power.stripTrailingZeros())
    }
}
