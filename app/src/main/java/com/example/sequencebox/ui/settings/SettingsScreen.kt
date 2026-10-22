package com.example.sequencebox.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sequencebox.model.AppRoundingMode
import com.example.sequencebox.model.AppSettings
import com.example.sequencebox.model.ThemeMode
import com.example.sequencebox.ui.SequenceBoxViewModel
import com.example.sequencebox.util.HapticFeedbackHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SequenceBoxViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val view = LocalView.current
    val scrollState = rememberScrollState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showDecimalPlacesDialog by remember { mutableStateOf(false) }
    var showRoundingModeDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showResetSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1: Appearance
            SettingsSectionHeader(title = "Appearance")
            SettingsCard {
                SettingsClickableItem(
                    title = "Theme",
                    subtitle = settings.themeMode.displayName,
                    onClick = { showThemeDialog = true }
                )
                SettingsDivider()
                SettingsSwitchItem(
                    title = "Dynamic Color",
                    subtitle = "Use Material 3 colors based on wallpaper",
                    checked = settings.dynamicColor,
                    onCheckedChange = {
                        HapticFeedbackHelper.performLightHaptic(view, settings.hapticsEnabled)
                        viewModel.updateDynamicColor(it)
                    }
                )
            }

            // Section 2: Behavior
            SettingsSectionHeader(title = "Behavior")
            SettingsCard {
                SettingsSwitchItem(
                    title = "Haptic Feedback",
                    subtitle = "Vibrate on interactions and errors",
                    checked = settings.hapticsEnabled,
                    onCheckedChange = { viewModel.updateHaptics(it) }
                )
                SettingsDivider()
                SettingsSwitchItem(
                    title = "Animations",
                    subtitle = "Enable card and transition animations",
                    checked = settings.animationsEnabled,
                    onCheckedChange = {
                        HapticFeedbackHelper.performLightHaptic(view, settings.hapticsEnabled)
                        viewModel.updateAnimations(it)
                    }
                )
                SettingsDivider()
                SettingsSwitchItem(
                    title = "Confirm Delete",
                    subtitle = "Ask for confirmation before deleting history",
                    checked = settings.confirmDelete,
                    onCheckedChange = {
                        HapticFeedbackHelper.performLightHaptic(view, settings.hapticsEnabled)
                        viewModel.updateConfirmDelete(it)
                    }
                )
            }

            // Section 3: Calculation
            SettingsSectionHeader(title = "Calculation")
            SettingsCard {
                SettingsClickableItem(
                    title = "Decimal Places",
                    subtitle = "${settings.decimalPlaces} decimal places (default: 10)",
                    onClick = { showDecimalPlacesDialog = true }
                )
                SettingsDivider()
                SettingsClickableItem(
                    title = "Rounding Mode",
                    subtitle = settings.roundingMode.displayName,
                    onClick = { showRoundingModeDialog = true }
                )
            }

            // Section 4: Data
            SettingsSectionHeader(title = "Data")
            SettingsCard {
                SettingsClickableItem(
                    title = "Clear History",
                    subtitle = "Delete all stored sequences",
                    isDestructive = true,
                    onClick = { showClearHistoryDialog = true }
                )
                SettingsDivider()
                SettingsClickableItem(
                    title = "Reset Settings",
                    subtitle = "Restore all settings to default values",
                    isDestructive = true,
                    onClick = { showResetSettingsDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Theme Mode Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Theme") },
            text = {
                Column {
                    ThemeMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    HapticFeedbackHelper.performLightHaptic(view, settings.hapticsEnabled)
                                    viewModel.updateTheme(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.themeMode == mode,
                                onClick = {
                                    HapticFeedbackHelper.performLightHaptic(view, settings.hapticsEnabled)
                                    viewModel.updateTheme(mode)
                                    showThemeDialog = false
                                }
                            )
                            Text(
                                text = mode.displayName,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Decimal Places Dialog
    if (showDecimalPlacesDialog) {
        AlertDialog(
            onDismissRequest = { showDecimalPlacesDialog = false },
            title = { Text("Display Decimal Places") },
            text = {
                Column {
                    AppSettings.ALLOWED_DECIMAL_PLACES.forEach { places ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    HapticFeedbackHelper.performLightHaptic(view, settings.hapticsEnabled)
                                    viewModel.updateDecimalPlaces(places)
                                    showDecimalPlacesDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.decimalPlaces == places,
                                onClick = {
                                    HapticFeedbackHelper.performLightHaptic(view, settings.hapticsEnabled)
                                    viewModel.updateDecimalPlaces(places)
                                    showDecimalPlacesDialog = false
                                }
                            )
                            Text(
                                text = "$places decimal places",
                                fontSize = 15.sp,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDecimalPlacesDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Rounding Mode Dialog
    if (showRoundingModeDialog) {
        AlertDialog(
            onDismissRequest = { showRoundingModeDialog = false },
            title = { Text("Rounding Mode") },
            text = {
                Column {
                    AppRoundingMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    HapticFeedbackHelper.performLightHaptic(view, settings.hapticsEnabled)
                                    viewModel.updateRoundingMode(mode)
                                    showRoundingModeDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.roundingMode == mode,
                                onClick = {
                                    HapticFeedbackHelper.performLightHaptic(view, settings.hapticsEnabled)
                                    viewModel.updateRoundingMode(mode)
                                    showRoundingModeDialog = false
                                }
                            )
                            Text(
                                text = mode.displayName,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRoundingModeDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Clear History Confirmation Dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear all saved sequences?") },
            text = { Text("This will permanently remove all items from history.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        HapticFeedbackHelper.performLightHaptic(view, settings.hapticsEnabled)
                        viewModel.clearHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Settings Confirmation Dialog
    if (showResetSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showResetSettingsDialog = false },
            title = { Text("Reset Settings?") },
            text = { Text("This will restore default settings. Your saved sequences will remain intact.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        HapticFeedbackHelper.performLightHaptic(view, settings.hapticsEnabled)
                        viewModel.resetSettings()
                        showResetSettingsDialog = false
                    }
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetSettingsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsClickableItem(
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        thickness = 0.8.dp
    )
}
