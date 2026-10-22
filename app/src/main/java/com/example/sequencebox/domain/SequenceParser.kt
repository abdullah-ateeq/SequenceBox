package com.example.sequencebox.domain

import java.math.BigDecimal

class SequenceParser {

    fun parseDecimal(input: String): Result<BigDecimal> {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Input cannot be empty."))
        }
        if (trimmed.length > MAX_INPUT_LENGTH) {
            return Result.failure(IllegalArgumentException("Number is too long. Maximum $MAX_INPUT_LENGTH characters."))
        }
        if (trimmed.contains(" ") || trimmed.contains("\t") || trimmed.contains("\n")) {
            return Result.failure(IllegalArgumentException("Enter a valid decimal number."))
        }
        // Reject scientific notation (e.g. 1E5, 1e-5)
        if (trimmed.contains("e", ignoreCase = true)) {
            return Result.failure(IllegalArgumentException("Enter a valid decimal number."))
        }
        // Strict decimal pattern: optional sign, digits, optional single decimal point with digits
        val decimalRegex = Regex("^[+-]?(?:\\d+(?:\\.\\d+)?|\\.\\d+)$")
        if (!decimalRegex.matches(trimmed)) {
            return Result.failure(IllegalArgumentException("Enter a valid decimal number."))
        }

        return try {
            Result.success(BigDecimal(trimmed))
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Enter a valid decimal number."))
        }
    }

    fun parsePositiveInteger(input: String): Result<Int> {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Input cannot be empty."))
        }
        if (trimmed.length > MAX_INPUT_LENGTH) {
            return Result.failure(IllegalArgumentException("Number is too long. Maximum $MAX_INPUT_LENGTH characters."))
        }
        if (trimmed.contains(" ") || trimmed.contains("\t") || trimmed.contains("\n")) {
            return Result.failure(IllegalArgumentException("Enter a whole number from 1 to 1000."))
        }
        val intRegex = Regex("^[+]?\\d+$")
        if (!intRegex.matches(trimmed)) {
            return Result.failure(IllegalArgumentException("Enter a whole number from 1 to 1000."))
        }
        return try {
            val clean = if (trimmed.startsWith("+")) trimmed.substring(1) else trimmed
            val value = clean.toInt()
            Result.success(value)
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Enter a whole number from 1 to 1000."))
        }
    }

    companion object {
        const val MAX_INPUT_LENGTH = 500
    }
}
