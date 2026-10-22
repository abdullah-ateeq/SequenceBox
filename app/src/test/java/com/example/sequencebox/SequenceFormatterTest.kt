package com.example.sequencebox

import com.example.sequencebox.domain.ArithmeticSequenceCalculator
import com.example.sequencebox.domain.GeometricSequenceCalculator
import com.example.sequencebox.domain.SequenceFormatter
import com.example.sequencebox.model.AppRoundingMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class SequenceFormatterTest {

    private val formatter = SequenceFormatter()
    private val arithCalc = ArithmeticSequenceCalculator()
    private val geomCalc = GeometricSequenceCalculator()

    // 34. Display rounding & trailing zero removal
    @Test
    fun testTrailingZeroRemoval() {
        // Display 2.5 instead of 2.5000000000
        val v1 = BigDecimal("2.5000000000")
        assertEquals("2.5", formatter.formatNumber(v1, 10))

        // Display 5 instead of 5.0000000000
        val v2 = BigDecimal("5.0000000000")
        assertEquals("5", formatter.formatNumber(v2, 10))
    }

    // 35. Decimal-place settings
    @Test
    fun testDecimalPlaceSettings() {
        val num = BigDecimal("3.1415926535")
        assertEquals("3", formatter.formatNumber(num, decimalPlaces = 0))
        assertEquals("3.14", formatter.formatNumber(num, decimalPlaces = 2))
        assertEquals("3.1416", formatter.formatNumber(num, decimalPlaces = 4))
        assertEquals("3.1415926535", formatter.formatNumber(num, decimalPlaces = 10))
    }

    // 36. Rounding modes
    @Test
    fun testRoundingModes() {
        val num = BigDecimal("2.55")

        assertEquals("2.6", formatter.formatNumber(num, 1, AppRoundingMode.HALF_UP))
        assertEquals("2.5", formatter.formatNumber(num, 1, AppRoundingMode.HALF_DOWN))
        assertEquals("2.6", formatter.formatNumber(num, 1, AppRoundingMode.HALF_EVEN)) // 6 is even
        assertEquals("2.6", formatter.formatNumber(num, 1, AppRoundingMode.UP))
        assertEquals("2.5", formatter.formatNumber(num, 1, AppRoundingMode.DOWN))
        assertEquals("2.6", formatter.formatNumber(num, 1, AppRoundingMode.CEILING))
        assertEquals("2.5", formatter.formatNumber(num, 1, AppRoundingMode.FLOOR))

        // Negative number ceiling vs floor
        val neg = BigDecimal("-2.55")
        assertEquals("-2.5", formatter.formatNumber(neg, 1, AppRoundingMode.CEILING))
        assertEquals("-2.6", formatter.formatNumber(neg, 1, AppRoundingMode.FLOOR))
    }

    // 40. Copy formatting (Arithmetic)
    @Test
    fun testCopyFormattingArithmetic() {
        val result = arithCalc.calculateArithmeticSequence(
            firstTerm = BigDecimal("2"),
            difference = BigDecimal("3"),
            numberOfTerms = 6
        ).getOrThrow()

        val copyText = formatter.formatCopyShareText(result)
        val expected = """
SequenceBox

Arithmetic Sequence

First Term: 2
Common Difference: 3
Terms: 6

Sequence:
2, 5, 8, 11, 14, 17

Sum: 57
First Term: 2
Last Term: 17

Formula:
aₙ = a₁ + (n - 1)d
        """.trimIndent()

        assertEquals(expected, copyText)
    }

    // 41. Share formatting (Geometric)
    @Test
    fun testShareFormattingGeometric() {
        val result = geomCalc.calculateGeometricSequence(
            firstTerm = BigDecimal("2"),
            ratio = BigDecimal("3"),
            numberOfTerms = 6
        ).getOrThrow()

        val shareText = formatter.formatCopyShareText(result)
        val expected = """
SequenceBox

Geometric Sequence

First Term: 2
Common Ratio: 3
Terms: 6

Sequence:
2, 6, 18, 54, 162, 486

Sum: 728
First Term: 2
Last Term: 486

Formula:
aₙ = a₁ × rⁿ⁻¹
        """.trimIndent()

        assertEquals(expected, shareText)
    }
}
