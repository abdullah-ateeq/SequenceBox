package com.example.sequencebox.domain

import com.example.sequencebox.model.SequenceDirection
import com.example.sequencebox.model.SequenceResult
import com.example.sequencebox.model.SequenceType
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode

class GeometricSequenceCalculator {

    fun calculateGeometricSequence(
        firstTerm: BigDecimal,
        ratio: BigDecimal,
        numberOfTerms: Int,
        mathContext: MathContext = DEFAULT_MATH_CONTEXT
    ): Result<SequenceResult> {
        return try {
            require(numberOfTerms in 1..MAX_TERMS) {
                "Enter a whole number from 1 to 1000."
            }

            val terms = ArrayList<BigDecimal>(numberOfTerms)
            var current = firstTerm
            checkValueSize(current)
            terms.add(current)

            for (i in 2..numberOfTerms) {
                current = current.multiply(ratio, mathContext)
                checkValueSize(current)
                terms.add(current)
            }

            var isExact = true
            val sum: BigDecimal
            val nBig = BigDecimal(BigInteger.valueOf(numberOfTerms.toLong()))

            if (firstTerm.compareTo(BigDecimal.ZERO) == 0) {
                sum = BigDecimal.ZERO
            } else if (ratio.compareTo(BigDecimal.ONE) == 0) {
                sum = nBig.multiply(firstTerm, mathContext)
            } else if (ratio.compareTo(BigDecimal.ZERO) == 0) {
                sum = firstTerm
            } else {
                // Formula: S_n = a1 * (r^n - 1) / (r - 1)
                val rToN = powerBySquaring(ratio, BigInteger.valueOf(numberOfTerms.toLong()), mathContext)
                val numerator = firstTerm.multiply(rToN.subtract(BigDecimal.ONE), mathContext)
                val denominator = ratio.subtract(BigDecimal.ONE)

                sum = try {
                    numerator.divide(denominator)
                } catch (e: ArithmeticException) {
                    isExact = false
                    numerator.divide(denominator, mathContext)
                }
            }
            checkValueSize(sum)

            val min = terms.minOrNull() ?: firstTerm
            val max = terms.maxOrNull() ?: firstTerm

            val direction = determineDirection(terms)
            val formula = "aₙ = a₁ × rⁿ⁻¹"

            val result = SequenceResult(
                type = SequenceType.GEOMETRIC,
                firstTerm = firstTerm,
                stepOrRatio = ratio,
                numberOfTerms = numberOfTerms,
                terms = terms,
                sum = sum,
                minimum = min,
                maximum = max,
                firstGeneratedTerm = terms.first(),
                lastGeneratedTerm = terms.last(),
                formula = formula,
                direction = direction,
                isExact = isExact,
                calculatedAt = System.currentTimeMillis()
            )
            Result.success(result)
        } catch (e: OutputTooLargeException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun calculateGeometricTerm(
        firstTerm: BigDecimal,
        ratio: BigDecimal,
        termNumber: Int,
        mathContext: MathContext = DEFAULT_MATH_CONTEXT
    ): BigDecimal {
        require(termNumber in 1..MAX_DIRECT_TERM) {
            if (termNumber > MAX_DIRECT_TERM) "Term number is too large. Maximum is 100,000."
            else "Enter a whole number from 1 to 100,000."
        }
        if (termNumber == 1) return firstTerm
        if (firstTerm.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
        if (ratio.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
        if (ratio.compareTo(BigDecimal.ONE) == 0) return firstTerm

        val exponent = BigInteger.valueOf((termNumber - 1).toLong())
        val rPower = powerBySquaring(ratio, exponent, mathContext)
        val term = firstTerm.multiply(rPower, mathContext)
        checkValueSize(term)
        return term
    }

    fun powerBySquaring(
        base: BigDecimal,
        exponent: BigInteger,
        mathContext: MathContext = DEFAULT_MATH_CONTEXT
    ): BigDecimal {
        if (exponent == BigInteger.ZERO) return BigDecimal.ONE
        if (exponent < BigInteger.ZERO) {
            val positivePower = powerBySquaring(base, exponent.negate(), mathContext)
            return BigDecimal.ONE.divide(positivePower, mathContext)
        }

        var result = BigDecimal.ONE
        var b = base
        var exp = exponent

        while (exp > BigInteger.ZERO) {
            if (exp.testBit(0)) {
                result = result.multiply(b, mathContext)
                checkValueSize(result)
            }
            exp = exp.shiftRight(1)
            if (exp > BigInteger.ZERO) {
                b = b.multiply(b, mathContext)
                checkValueSize(b)
            }
        }
        return result
    }

    private fun determineDirection(terms: List<BigDecimal>): SequenceDirection {
        if (terms.size <= 1) return SequenceDirection.CONSTANT

        var allEqual = true
        for (i in 0 until terms.size - 1) {
            if (terms[i].compareTo(terms[i + 1]) != 0) {
                allEqual = false
                break
            }
        }
        if (allEqual) return SequenceDirection.CONSTANT

        // Check strictly alternating signs
        var alternating = true
        for (i in 0 until terms.size - 1) {
            val s1 = terms[i].signum()
            val s2 = terms[i + 1].signum()
            if (s1 == 0 || s2 == 0 || s1 == s2) {
                alternating = false
                break
            }
        }
        if (alternating) return SequenceDirection.ALTERNATING

        var strictlyIncreasing = true
        var strictlyDecreasing = true
        for (i in 0 until terms.size - 1) {
            val cmp = terms[i].compareTo(terms[i + 1])
            if (cmp >= 0) strictlyIncreasing = false
            if (cmp <= 0) strictlyDecreasing = false
        }
        if (strictlyIncreasing) return SequenceDirection.INCREASING
        if (strictlyDecreasing) return SequenceDirection.DECREASING

        return SequenceDirection.MIXED
    }

    private fun checkValueSize(value: BigDecimal) {
        val str = value.toPlainString()
        if (str.length > MAX_SAFE_DIGITS) {
            throw OutputTooLargeException()
        }
    }

    companion object {
        val DEFAULT_MATH_CONTEXT = MathContext(80, RoundingMode.HALF_UP)
        const val MAX_TERMS = 1000
        const val MAX_DIRECT_TERM = 100_000
        const val MAX_SAFE_DIGITS = 2000
    }
}
