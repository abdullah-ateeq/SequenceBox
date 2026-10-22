package com.example.sequencebox

import com.example.sequencebox.domain.ArithmeticSequenceCalculator
import com.example.sequencebox.model.SequenceDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class ArithmeticSequenceCalculatorTest {

    private val calculator = ArithmeticSequenceCalculator()

    // 8. Arithmetic sequence generation: Start = 2, Step = 3, Terms = 6 -> 2, 5, 8, 11, 14, 17
    @Test
    fun testArithmeticSequenceGeneration_basic() {
        val result = calculator.calculateArithmeticSequence(
            firstTerm = BigDecimal("2"),
            difference = BigDecimal("3"),
            numberOfTerms = 6
        ).getOrThrow()

        val expected = listOf("2", "5", "8", "11", "14", "17").map { BigDecimal(it) }
        assertEquals(expected, result.terms)
        assertEquals(BigDecimal("17"), result.lastGeneratedTerm)
        assertEquals(BigDecimal("57"), result.sum)
        assertEquals(SequenceDirection.INCREASING, result.direction)
    }

    // 10. Negative arithmetic difference: First = 20, Difference = -4, Terms = 5 -> 20, 16, 12, 8, 4
    @Test
    fun testNegativeArithmeticDifference() {
        val result = calculator.calculateArithmeticSequence(
            firstTerm = BigDecimal("20"),
            difference = BigDecimal("-4"),
            numberOfTerms = 5
        ).getOrThrow()

        val expected = listOf("20", "16", "12", "8", "4").map { BigDecimal(it) }
        assertEquals(expected, result.terms)
        assertEquals(BigDecimal("4"), result.lastGeneratedTerm)
        assertEquals(BigDecimal("60"), result.sum)
        assertEquals(SequenceDirection.DECREASING, result.direction)
    }

    // 11. Decimal arithmetic difference: First = 1, Difference = 0.5, Terms = 5 -> 1, 1.5, 2, 2.5, 3
    @Test
    fun testDecimalArithmeticDifference() {
        val result = calculator.calculateArithmeticSequence(
            firstTerm = BigDecimal("1"),
            difference = BigDecimal("0.5"),
            numberOfTerms = 5
        ).getOrThrow()

        val expected = listOf("1.0", "1.5", "2.0", "2.5", "3.0")
        val actual = result.terms.map { it.stripTrailingZeros().toPlainString() }
        assertEquals(listOf("1", "1.5", "2", "2.5", "3"), actual)
        assertEquals(BigDecimal("10.0").stripTrailingZeros(), result.sum.stripTrailingZeros())
    }

    // 12. Zero arithmetic difference
    @Test
    fun testZeroArithmeticDifference() {
        val result = calculator.calculateArithmeticSequence(
            firstTerm = BigDecimal("5"),
            difference = BigDecimal("0"),
            numberOfTerms = 4
        ).getOrThrow()

        val actual = result.terms.map { it.stripTrailingZeros().toPlainString() }
        assertEquals(listOf("5", "5", "5", "5"), actual)
        assertEquals(SequenceDirection.CONSTANT, result.direction)
        assertEquals(0, BigDecimal("20").compareTo(result.sum))
    }

    // 17. First term zero
    @Test
    fun testFirstTermZeroArithmetic() {
        val result = calculator.calculateArithmeticSequence(
            firstTerm = BigDecimal("0"),
            difference = BigDecimal("3"),
            numberOfTerms = 5
        ).getOrThrow()

        val expected = listOf("0", "3", "6", "9", "12")
        val actual = result.terms.map { it.stripTrailingZeros().toPlainString() }
        assertEquals(expected, actual)
        assertEquals(0, BigDecimal("30").compareTo(result.sum))
    }

    // 18. One-term sequence: First = 25, Step = 100, Terms = 1 -> 25, Sum = 25
    @Test
    fun testOneTermArithmetic() {
        val result = calculator.calculateArithmeticSequence(
            firstTerm = BigDecimal("25"),
            difference = BigDecimal("100"),
            numberOfTerms = 1
        ).getOrThrow()

        assertEquals(1, result.terms.size)
        assertEquals(BigDecimal("25"), result.terms.first().stripTrailingZeros())
        assertEquals(BigDecimal("25"), result.sum.stripTrailingZeros())
        assertEquals(BigDecimal("25"), result.minimum.stripTrailingZeros())
        assertEquals(BigDecimal("25"), result.maximum.stripTrailingZeros())
        assertEquals(SequenceDirection.CONSTANT, result.direction)
    }

    // 19. Arithmetic nth term: a₁ = 2, d = 3, n = 10 -> a₁₀ = 2 + 9 * 3 = 29
    @Test
    fun testArithmeticNthTerm() {
        val term10 = calculator.calculateArithmeticTerm(
            firstTerm = BigDecimal("2"),
            difference = BigDecimal("3"),
            termNumber = 10
        )
        assertEquals(BigDecimal("29"), term10.stripTrailingZeros())
    }

    // 21. Arithmetic sum: 2, 5, 8, 11, 14, 17 -> Sum = 57
    @Test
    fun testArithmeticSum() {
        val result = calculator.calculateArithmeticSequence(
            firstTerm = BigDecimal("2"),
            difference = BigDecimal("3"),
            numberOfTerms = 6
        ).getOrThrow()

        assertEquals(BigDecimal("57"), result.sum.stripTrailingZeros())
    }

    // 24 & 25. Minimum and Maximum
    @Test
    fun testArithmeticMinMax() {
        // Increasing
        val inc = calculator.calculateArithmeticSequence(
            firstTerm = BigDecimal("2"),
            difference = BigDecimal("3"),
            numberOfTerms = 4
        ).getOrThrow()
        assertEquals(BigDecimal("2"), inc.minimum.stripTrailingZeros())
        assertEquals(BigDecimal("11"), inc.maximum.stripTrailingZeros())

        // Decreasing
        val dec = calculator.calculateArithmeticSequence(
            firstTerm = BigDecimal("20"),
            difference = BigDecimal("-4"),
            numberOfTerms = 5
        ).getOrThrow()
        assertEquals(0, BigDecimal("4").compareTo(dec.minimum))
        assertEquals(0, BigDecimal("20").compareTo(dec.maximum))
    }

    // 26, 27, 28. Increasing, Decreasing, Constant detection
    @Test
    fun testArithmeticDirectionDetection() {
        val inc = calculator.calculateArithmeticSequence(BigDecimal("1"), BigDecimal("2"), 3).getOrThrow()
        assertEquals(SequenceDirection.INCREASING, inc.direction)

        val dec = calculator.calculateArithmeticSequence(BigDecimal("10"), BigDecimal("-1"), 3).getOrThrow()
        assertEquals(SequenceDirection.DECREASING, dec.direction)

        val constSeq = calculator.calculateArithmeticSequence(BigDecimal("7"), BigDecimal("0"), 3).getOrThrow()
        assertEquals(SequenceDirection.CONSTANT, constSeq.direction)
    }

    // 31. BigDecimal precision
    @Test
    fun testBigDecimalPrecision() {
        val first = BigDecimal("0.1")
        val diff = BigDecimal("0.2")
        val result = calculator.calculateArithmeticSequence(first, diff, 3).getOrThrow()
        // In Float/Double: 0.1 + 0.2 != 0.3. In BigDecimal: 0.3 exactly.
        assertEquals(BigDecimal("0.3"), result.terms[1].stripTrailingZeros())
        assertEquals(BigDecimal("0.5"), result.terms[2].stripTrailingZeros())
    }

    // 32. Large integer calculations: larger than Long.MAX_VALUE
    @Test
    fun testLargeIntegerCalculations() {
        val largeFirst = BigDecimal("9223372036854775807") // Long.MAX_VALUE
        val largeDiff = BigDecimal("1000000000000000000")
        val result = calculator.calculateArithmeticSequence(largeFirst, largeDiff, 5).getOrThrow()

        // 9223372036854775807 + 4 * 1000000000000000000 = 13223372036854775807
        assertEquals("13223372036854775807", result.lastGeneratedTerm.toPlainString())
        // Sum calculation with large integer
        val term5 = calculator.calculateArithmeticTerm(largeFirst, largeDiff, 5)
        assertEquals("13223372036854775807", term5.toPlainString())
    }
}
