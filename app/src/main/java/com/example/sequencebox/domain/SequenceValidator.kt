package com.example.sequencebox.domain

import com.example.sequencebox.model.SequenceType
import java.math.BigDecimal

class SequenceValidator(
    private val parser: SequenceParser = SequenceParser()
) {

    fun validateFirstTerm(input: String): Result<BigDecimal> {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Enter a first term."))
        }
        return parser.parseDecimal(trimmed)
    }

    fun validateStepOrRatio(input: String, type: SequenceType): Result<BigDecimal> {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            val field = if (type == SequenceType.ARITHMETIC) "common difference" else "common ratio"
            return Result.failure(IllegalArgumentException("Enter a $field."))
        }
        return parser.parseDecimal(trimmed)
    }

    fun validateNumberOfTerms(input: String): Result<Int> {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Enter a whole number from 1 to 1000."))
        }
        val parseResult = parser.parsePositiveInteger(trimmed)
        if (parseResult.isFailure) {
            return Result.failure(IllegalArgumentException("Enter a whole number from 1 to 1000."))
        }
        val terms = parseResult.getOrThrow()
        if (terms < MIN_TERMS || terms > MAX_TERMS) {
            return Result.failure(IllegalArgumentException("Enter a whole number from 1 to 1000."))
        }
        return Result.success(terms)
    }

    fun validateTermNumber(input: String): Result<Int> {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Enter a term number."))
        }
        if (trimmed.length > SequenceParser.MAX_INPUT_LENGTH) {
            return Result.failure(IllegalArgumentException("Number is too long. Maximum ${SequenceParser.MAX_INPUT_LENGTH} characters."))
        }
        val intRegex = Regex("^[+]?\\d+$")
        if (!intRegex.matches(trimmed)) {
            return Result.failure(IllegalArgumentException("Enter a whole number from 1 to 100,000."))
        }
        return try {
            val clean = if (trimmed.startsWith("+")) trimmed.substring(1) else trimmed
            val value = clean.toInt()
            if (value < 1) {
                Result.failure(IllegalArgumentException("Enter a whole number from 1 to 100,000."))
            } else if (value > MAX_DIRECT_TERM) {
                Result.failure(IllegalArgumentException("Term number is too large. Maximum is 100,000."))
            } else {
                Result.success(value)
            }
        } catch (e: NumberFormatException) {
            Result.failure(IllegalArgumentException("Term number is too large. Maximum is 100,000."))
        }
    }

    companion object {
        const val MIN_TERMS = 1
        const val MAX_TERMS = 1000
        const val MAX_DIRECT_TERM = 100_000
    }
}
