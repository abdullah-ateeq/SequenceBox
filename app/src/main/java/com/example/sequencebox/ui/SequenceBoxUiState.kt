package com.example.sequencebox.ui

import com.example.sequencebox.model.SequenceDirection
import com.example.sequencebox.model.SequenceHistory
import com.example.sequencebox.model.SequenceResult
import com.example.sequencebox.model.SequenceType

data class SequenceBoxUiState(
    val type: SequenceType = SequenceType.ARITHMETIC,

    val firstTermInput: String = "",
    val stepOrRatioInput: String = "",
    val numberOfTermsInput: String = "10",

    val generatedTerms: List<String> = emptyList(),

    val sum: String? = null,
    val firstTerm: String? = null,
    val lastTerm: String? = null,
    val minimum: String? = null,
    val maximum: String? = null,

    val formula: String? = null,

    val errorMessage: String? = null,

    val isCalculating: Boolean = false,

    val history: List<SequenceHistory> = emptyList(),

    // Extended UI properties
    val findTermInput: String = "",
    val findTermResult: String? = null,
    val findTermError: String? = null,
    val isFindTermCalculating: Boolean = false,
    val direction: SequenceDirection? = null,
    val isExact: Boolean = true,
    val isFormulaExpanded: Boolean = true,
    val isFindTermExpanded: Boolean = false,
    val userNotification: String? = null,
    val currentResult: SequenceResult? = null
)
