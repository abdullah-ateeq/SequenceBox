package com.example.sequencebox.ui.home

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sequencebox.model.SequenceDirection
import com.example.sequencebox.model.SequenceType

@Composable
fun SummaryCard(
    type: SequenceType,
    termsCount: Int,
    firstTerm: String,
    lastTerm: String,
    stepOrRatio: String,
    sum: String,
    direction: SequenceDirection?,
    minimum: String,
    maximum: String,
    modifier: Modifier = Modifier
) {
    val stepLabel = if (type == SequenceType.ARITHMETIC) "Common Difference" else "Common Ratio"
    val typeLabel = if (type == SequenceType.ARITHMETIC) "Arithmetic" else "Geometric"
    val directionLabel = when (direction) {
        SequenceDirection.INCREASING -> "Increasing"
        SequenceDirection.DECREASING -> "Decreasing"
        SequenceDirection.CONSTANT -> "Constant"
        SequenceDirection.ALTERNATING -> "Alternating"
        SequenceDirection.MIXED -> "Mixed"
        null -> "-"
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "SEQUENCE SUMMARY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            SummaryRow(label = "Type", value = typeLabel)
            SummaryDivider()
            SummaryRow(label = "Terms", value = termsCount.toString())
            SummaryDivider()
            SummaryRow(label = "First Term", value = firstTerm)
            SummaryDivider()
            SummaryRow(label = "Last Term", value = lastTerm)
            SummaryDivider()
            SummaryRow(label = stepLabel, value = stepOrRatio)
            SummaryDivider()
            SummaryRow(label = "Sum", value = sum, isEmphasized = true)
            SummaryDivider()
            SummaryRow(label = "Direction", value = directionLabel)
            SummaryDivider()
            SummaryRow(label = "Minimum", value = minimum)
            SummaryDivider()
            SummaryRow(label = "Maximum", value = maximum)
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    isEmphasized: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = if (isEmphasized) 16.sp else 15.sp,
            fontWeight = if (isEmphasized) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isEmphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SummaryDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        thickness = 0.8.dp
    )
}
