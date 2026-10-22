package com.example.sequencebox.domain

import com.example.sequencebox.model.SequenceDirection
import com.example.sequencebox.model.SequenceResult
import com.example.sequencebox.model.SequenceType
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode

class ArithmeticSequenceCalculator {

    fun calculateArithmeticSequence(
        firstTerm: BigDecimal,
        difference: BigDecimal,
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
                current = current.add(difference, mathContext)
                checkValueSize(current)
                terms.add(current)
            }

            // Sum formula: S_n = n / 2 * [2*a1 + (n - 1)*d]
            val nBig = BigDecimal(BigInteger.valueOf(numberOfTerms.toLong()))
            val two = BigDecimal(2)
            val nMinusOne = BigDecimal(BigInteger.valueOf((numberOfTerms - 1).toLong()))
            val twoA1 = two.multiply(firstTerm, mathContext)
            val nMinusOneD = nMinusOne.multiply(difference, mathContext)
            val sumInside = twoA1.add(nMinusOneD, mathContext)
            val sum = nBig.multiply(sumInside, mathContext).divide(two, mathContext)
            checkValueSize(sum)

            val min = terms.minOrNull() ?: firstTerm
            val max = terms.maxOrNull() ?: firstTerm

            val direction = when {
                numberOfTerms == 1 -> SequenceDirection.CONSTANT
                difference.compareTo(BigDecimal.ZERO) > 0 -> SequenceDirection.INCREASING
                difference.compareTo(BigDecimal.ZERO) < 0 -> SequenceDirection.DECREASING
                else -> SequenceDirection.CONSTANT
            }

            val formula = "aₙ = a₁ + (n - 1)d"

            val result = SequenceResult(
                type = SequenceType.ARITHMETIC,
                firstTerm = firstTerm,
                stepOrRatio = difference,
                numberOfTerms = numberOfTerms,
                terms = terms,
                sum = sum,
                minimum = min,
                maximum = max,
                firstGeneratedTerm = terms.first(),
                lastGeneratedTerm = terms.last(),
                formula = formula,
                direction = direction,
                isExact = true,
                calculatedAt = System.currentTimeMillis()
            )
            Result.success(result)
        } catch (e: OutputTooLargeException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun calculateArithmeticTerm(
        firstTerm: BigDecimal,
        difference: BigDecimal,
        termNumber: Int,
        mathContext: MathContext = DEFAULT_MATH_CONTEXT
    ): BigDecimal {
        require(termNumber in 1..MAX_DIRECT_TERM) {
            if (termNumber > MAX_DIRECT_TERM) "Term number is too large. Maximum is 100,000."
            else "Enter a whole number from 1 to 100,000."
        }
        val nMinusOne = BigDecimal(BigInteger.valueOf((termNumber - 1).toLong()))
        val step = nMinusOne.multiply(difference, mathContext)
        val term = firstTerm.add(step, mathContext)
        checkValueSize(term)
        return term
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
