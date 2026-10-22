package com.example.sequencebox.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sequencebox.model.SequenceType

@Composable
fun SequenceTypeSelector(
    selectedType: SequenceType,
    onTypeSelected: (SequenceType) -> Unit,
    modifier: Modifier = Modifier
) {
    val containerShape = RoundedCornerShape(12.dp)
    val itemShape = RoundedCornerShape(10.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(containerShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            SequenceType.values().forEach { type ->
                val isSelected = selectedType == type
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    label = "typeSelectorBg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "typeSelectorText"
                )

                val label = if (type == SequenceType.ARITHMETIC) "ARITHMETIC" else "GEOMETRIC"

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(itemShape)
                        .background(backgroundColor)
                        .clickable { onTypeSelected(type) }
                        .semantics {
                            role = Role.Tab
                            contentDescription = "Select $label sequence type"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        letterSpacing = 1.sp,
                        color = textColor
                    )
                }
            }
        }
    }
}
