package com.example.sequencebox.domain

import com.example.sequencebox.model.SequenceResult
import com.example.sequencebox.model.SequenceType
import java.math.BigDecimal
import java.math.MathContext

open class SequenceBoxCalculator(
    private val arithmeticCalculator: ArithmeticSequenceCalculator = ArithmeticSequenceCalculator(),
    private val geometricCalculator: GeometricSequenceCalculator = GeometricSequenceCalculator()
) {

    open fun calculateSequence(
        type: SequenceType,
        firstTerm: BigDecimal,
        stepOrRatio: BigDecimal,
        numberOfTerms: Int,
        mathContext: MathContext = ArithmeticSequenceCalculator.DEFAULT_MATH_CONTEXT
    ): Result<SequenceResult> {
        return when (type) {
            SequenceType.ARITHMETIC -> arithmeticCalculator.calculateArithmeticSequence(
                firstTerm = firstTerm,
                difference = stepOrRatio,
                numberOfTerms = numberOfTerms,
                mathContext = mathContext
            )
            SequenceType.GEOMETRIC -> geometricCalculator.calculateGeometricSequence(
                firstTerm = firstTerm,
                ratio = stepOrRatio,
                numberOfTerms = numberOfTerms,
                mathContext = mathContext
            )
        }
    }

    open fun calculateTerm(
        type: SequenceType,
        firstTerm: BigDecimal,
        stepOrRatio: BigDecimal,
        termNumber: Int,
        mathContext: MathContext = ArithmeticSequenceCalculator.DEFAULT_MATH_CONTEXT
    ): BigDecimal {
        return when (type) {
            SequenceType.ARITHMETIC -> arithmeticCalculator.calculateArithmeticTerm(
                firstTerm = firstTerm,
                difference = stepOrRatio,
                termNumber = termNumber,
                mathContext = mathContext
            )
            SequenceType.GEOMETRIC -> geometricCalculator.calculateGeometricTerm(
                firstTerm = firstTerm,
                ratio = stepOrRatio,
                termNumber = termNumber,
                mathContext = mathContext
            )
        }
    }
}
