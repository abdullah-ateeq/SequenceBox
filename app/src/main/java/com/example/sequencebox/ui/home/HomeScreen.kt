package com.example.sequencebox.ui.home

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sequencebox.model.SequenceType
import com.example.sequencebox.ui.SequenceBoxViewModel
import com.example.sequencebox.util.ClipboardHelper
import com.example.sequencebox.util.HapticFeedbackHelper
import com.example.sequencebox.util.ShareHelper

@Composable
fun HomeScreen(
    viewModel: SequenceBoxViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    val view = LocalView.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.userNotification) {
        uiState.userNotification?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearNotification()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            HapticFeedbackHelper.performErrorHaptic(view, settings.hapticsEnabled)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // App Header
            Text(
                text = "SequenceBox",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Generate arithmetic and geometric sequences.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Sequence Type Selector [ ARITHMETIC ] [ GEOMETRIC ]
            SequenceTypeSelector(
                selectedType = uiState.type,
                onTypeSelected = { newType ->
                    HapticFeedbackHelper.performLightHaptic(view, settings.hapticsEnabled)
                    viewModel.onTypeChanged(newType)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mode header & Subtitle
            val isArithmetic = uiState.type == SequenceType.ARITHMETIC
            val modeHeader = if (isArithmetic) "Arithmetic Sequence" else "Geometric Sequence"
            val modeSubtitle = if (isArithmetic) "Add the same step to each term." else "Multiply each term by the same ratio."

            Text(
                text = modeHeader,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = modeSubtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Input 1: FIRST TERM
            Text(
                text = "FIRST TERM",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = uiState.firstTermInput,
                onValueChange = viewModel::onFirstTermInputChanged,
                placeholder = { Text("e.g. 2") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "First term of the sequence." }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input 2: COMMON DIFFERENCE / COMMON RATIO
            val stepLabel = if (isArithmetic) "COMMON DIFFERENCE" else "COMMON RATIO"
            val stepPlaceholder = if (isArithmetic) "e.g. 3 or -0.5" else "e.g. 2 or 0.5"
            val stepAccessibility = if (isArithmetic) "Common difference." else "Common ratio."

            Text(
                text = stepLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = uiState.stepOrRatioInput,
                onValueChange = viewModel::onStepOrRatioInputChanged,
                placeholder = { Text(stepPlaceholder) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = stepAccessibility }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input 3: NUMBER OF TERMS
            Text(
                text = "NUMBER OF TERMS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = uiState.numberOfTermsInput,
                onValueChange = viewModel::onNumberOfTermsInputChanged,
                placeholder = { Text("1 to 1000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Number of terms." }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Error Message (if any)
            if (uiState.errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(14.dp)
                ) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Calculation in progress bar or action buttons
            if (uiState.isCalculating) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Calculating…",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.cancelCalculation() },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text("CANCEL")
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            HapticFeedbackHelper.performLightHaptic(view, settings.hapticsEnabled)
                            viewModel.generateSequence()
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .semantics { contentDescription = "Generate sequence." }
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GENERATE SEQUENCE",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            HapticFeedbackHelper.performLightHaptic(view, settings.hapticsEnabled)
                            viewModel.clearInputsAndResults()
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear inputs")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Generated Sequence Result Section
            AnimatedVisibility(
                visible = uiState.generatedTerms.isNotEmpty() && !uiState.isCalculating,
                enter = fadeIn() + scaleIn(initialScale = 0.95f),
                exit = fadeOut() + scaleOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    // Sequence Result Card
                    SequenceCard(
                        type = uiState.type,
                        terms = uiState.generatedTerms,
                        isExact = uiState.isExact
                    )

                    // Summary Card
                    if (uiState.sum != null && uiState.firstTerm != null && uiState.lastTerm != null) {
                        SummaryCard(
                            type = uiState.type,
                            termsCount = uiState.generatedTerms.size,
                            firstTerm = uiState.firstTerm!!,
                            lastTerm = uiState.lastTerm!!,
                            stepOrRatio = uiState.stepOrRatioInput,
                            sum = uiState.sum!!,
                            direction = uiState.direction,
                            minimum = uiState.minimum ?: "-",
                            maximum = uiState.maximum ?: "-"
                        )
                    }

                    // Formula Card
                    FormulaCard(
                        type = uiState.type,
                        isExpanded = uiState.isFormulaExpanded,
                        onToggle = viewModel::toggleFormulaExpanded
                    )

                    // Action buttons: [ COPY ] [ SHARE ] [ SAVE ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                HapticFeedbackHelper.performLightHaptic(view, settings.hapticsEnabled)
                                val text = viewModel.getCopyText()
                                if (text != null) {
                                    ClipboardHelper.copyToClipboard(context, text)
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("COPY", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                HapticFeedbackHelper.performLightHaptic(view, settings.hapticsEnabled)
                                val text = viewModel.getCopyText()
                                if (text != null) {
                                    ShareHelper.shareText(context, text)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SHARE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                HapticFeedbackHelper.performLightHaptic(view, settings.hapticsEnabled)
                                viewModel.saveCurrentSequence()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SAVE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Find Term Section
                    FindTermCard(
                        termNumberInput = uiState.findTermInput,
                        onTermNumberChanged = viewModel::onFindTermInputChanged,
                        onCalculateTerm = {
                            HapticFeedbackHelper.performLightHaptic(view, settings.hapticsEnabled)
                            viewModel.calculateDirectTerm()
                        },
                        resultText = uiState.findTermResult,
                        errorMessage = uiState.findTermError,
                        isCalculating = uiState.isFindTermCalculating,
                        isExpanded = uiState.isFindTermExpanded,
                        onToggleExpanded = viewModel::toggleFindTermExpanded
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
