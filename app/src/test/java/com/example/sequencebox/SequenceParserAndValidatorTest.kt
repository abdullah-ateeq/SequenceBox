package com.example.sequencebox

import com.example.sequencebox.domain.SequenceParser
import com.example.sequencebox.domain.SequenceValidator
import com.example.sequencebox.model.SequenceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class SequenceParserAndValidatorTest {

    private val parser = SequenceParser()
    private val validator = SequenceValidator(parser)

    // 1. Decimal parsing
    @Test
    fun testDecimalParsing_validInputs() {
        assertEquals(BigDecimal("2"), parser.parseDecimal("2").getOrNull())
        assertEquals(BigDecimal("-5"), parser.parseDecimal("-5").getOrNull())
        assertEquals(BigDecimal("0"), parser.parseDecimal("0").getOrNull())
        assertEquals(BigDecimal("1.5"), parser.parseDecimal("1.5").getOrNull())
        assertEquals(BigDecimal("-2.75"), parser.parseDecimal("-2.75").getOrNull())
        assertEquals(BigDecimal("0.125"), parser.parseDecimal("0.125").getOrNull())
        assertEquals(BigDecimal("100"), parser.parseDecimal("+100").getOrNull())
        // Surrounding whitespace allowed
        assertEquals(BigDecimal("25"), parser.parseDecimal("  25  ").getOrNull())
    }

    // 2. Integer parsing
    @Test
    fun testIntegerParsing_validInputs() {
        assertEquals(6, parser.parsePositiveInteger("6").getOrNull())
        assertEquals(1, parser.parsePositiveInteger("1").getOrNull())
        assertEquals(1000, parser.parsePositiveInteger("1000").getOrNull())
        assertEquals(10, parser.parsePositiveInteger("+10").getOrNull())
    }

    // 3. Empty input validation
    @Test
    fun testEmptyInputValidation() {
        val firstTermRes = validator.validateFirstTerm("")
        assertTrue(firstTermRes.isFailure)
        assertEquals("Enter a first term.", firstTermRes.exceptionOrNull()?.message)

        val diffRes = validator.validateStepOrRatio("", SequenceType.ARITHMETIC)
        assertTrue(diffRes.isFailure)
        assertEquals("Enter a common difference.", diffRes.exceptionOrNull()?.message)

        val ratioRes = validator.validateStepOrRatio("   ", SequenceType.GEOMETRIC)
        assertTrue(ratioRes.isFailure)
        assertEquals("Enter a common ratio.", ratioRes.exceptionOrNull()?.message)

        val termsRes = validator.validateNumberOfTerms("")
        assertTrue(termsRes.isFailure)
        assertEquals("Enter a whole number from 1 to 1000.", termsRes.exceptionOrNull()?.message)

        val termNumRes = validator.validateTermNumber("")
        assertTrue(termNumRes.isFailure)
        assertEquals("Enter a term number.", termNumRes.exceptionOrNull()?.message)
    }

    // 4. Invalid decimal
    @Test
    fun testInvalidDecimalParsing() {
        val inputs = listOf("abc", "1.2.3", "--", "++", "+-", "1E5", "1e-5", "1 000")
        for (input in inputs) {
            val res = parser.parseDecimal(input)
            assertTrue("Expected failure for '$input'", res.isFailure)
            assertEquals("Enter a valid decimal number.", res.exceptionOrNull()?.message)
        }
    }

    // 5. Invalid term count
    @Test
    fun testInvalidTermCount() {
        val invalidInputs = listOf("0", "-1", "1.5", "abc", "1 0")
        for (input in invalidInputs) {
            val res = validator.validateNumberOfTerms(input)
            assertTrue("Expected failure for '$input'", res.isFailure)
            assertEquals("Enter a whole number from 1 to 1000.", res.exceptionOrNull()?.message)
        }
    }

    // 6. Term count minimum
    @Test
    fun testTermCountMinimum() {
        assertTrue(validator.validateNumberOfTerms("1").isSuccess)
        val zeroRes = validator.validateNumberOfTerms("0")
        assertTrue(zeroRes.isFailure)
        assertEquals("Enter a whole number from 1 to 1000.", zeroRes.exceptionOrNull()?.message)
    }

    // 7. Term count maximum
    @Test
    fun testTermCountMaximum() {
        assertTrue(validator.validateNumberOfTerms("1000").isSuccess)
        val overRes = validator.validateNumberOfTerms("1001")
        assertTrue(overRes.isFailure)
        assertEquals("Enter a whole number from 1 to 1000.", overRes.exceptionOrNull()?.message)
    }

    @Test
    fun testDirectTermLimit() {
        assertTrue(validator.validateTermNumber("1").isSuccess)
        assertTrue(validator.validateTermNumber("100000").isSuccess)
        val overRes = validator.validateTermNumber("100001")
        assertTrue(overRes.isFailure)
        assertEquals("Term number is too large. Maximum is 100,000.", overRes.exceptionOrNull()?.message)
    }

    @Test
    fun testInputLengthLimit() {
        val longString = "1".repeat(501)
        val res = parser.parseDecimal(longString)
        assertTrue(res.isFailure)
        assertEquals("Number is too long. Maximum 500 characters.", res.exceptionOrNull()?.message)
    }
}
