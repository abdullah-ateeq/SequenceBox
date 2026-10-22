package com.example.sequencebox.model

data class SequenceHistory(
    val id: String,
    val type: SequenceType,
    val firstTerm: String,
    val stepOrRatio: String,
    val numberOfTerms: Int,
    val savedAt: Long
)
