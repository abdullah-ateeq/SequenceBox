package com.example.sequencebox.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sequencebox.data.HistoryRepository
import com.example.sequencebox.data.SettingsRepository
import com.example.sequencebox.domain.OutputTooLargeException
import com.example.sequencebox.domain.SequenceBoxCalculator
import com.example.sequencebox.domain.SequenceFormatter
import com.example.sequencebox.domain.SequenceValidator
import com.example.sequencebox.model.AppRoundingMode
import com.example.sequencebox.model.AppSettings
import com.example.sequencebox.model.SequenceHistory
import com.example.sequencebox.model.SequenceResult
import com.example.sequencebox.model.SequenceType
import com.example.sequencebox.model.ThemeMode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class SequenceBoxViewModel(
    private val calculator: SequenceBoxCalculator = SequenceBoxCalculator(),
    private val validator: SequenceValidator = SequenceValidator(),
    private val formatter: SequenceFormatter = SequenceFormatter(),
    private val settingsRepository: SettingsRepository? = null,
    private val historyRepository: HistoryRepository? = null,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val _uiState = MutableStateFlow(SequenceBoxUiState())
    val uiState: StateFlow<SequenceBoxUiState> = _uiState.asStateFlow()

    val settings: StateFlow<AppSettings> = settingsRepository?.settings
        ?: MutableStateFlow(AppSettings()).asStateFlow()

    internal var preCalculationDelayMs: Long = 0L
    private var calculationJob: Job? = null
    private var findTermJob: Job? = null

    init {
        // Restore last inputs from settings if available
        settingsRepository?.let { repo ->
            _uiState.update {
                it.copy(
                    type = repo.getLastType(),
                    firstTermInput = repo.getLastFirstTerm(),
                    stepOrRatioInput = repo.getLastStepOrRatio(),
                    numberOfTermsInput = repo.getLastNumberOfTerms().ifEmpty { "10" }
                )
            }
        }

        // Observe history
        historyRepository?.let { repo ->
            viewModelScope.launch {
                repo.history.collect { historyList ->
                    _uiState.update { it.copy(history = historyList) }
                }
            }
        }

        // Re-format display if settings (decimal places, rounding mode) change
        viewModelScope.launch {
            settings.collect { currentSettings ->
                reformatCurrentResult(currentSettings)
            }
        }
    }

    fun onTypeChanged(type: SequenceType) {
        if (_uiState.value.type == type) return
        _uiState.update {
            it.copy(
                type = type,
                errorMessage = null,
                findTermResult = null,
                findTermError = null
            )
        }
        settingsRepository?.saveLastInputs(
            type = type,
            firstTerm = _uiState.value.firstTermInput,
            stepOrRatio = _uiState.value.stepOrRatioInput,
            numberOfTerms = _uiState.value.numberOfTermsInput
        )
    }

    fun onFirstTermInputChanged(input: String) {
        _uiState.update { it.copy(firstTermInput = input, errorMessage = null) }
        persistInputs()
    }

    fun onStepOrRatioInputChanged(input: String) {
        _uiState.update { it.copy(stepOrRatioInput = input, errorMessage = null) }
        persistInputs()
    }

    fun onNumberOfTermsInputChanged(input: String) {
        _uiState.update { it.copy(numberOfTermsInput = input, errorMessage = null) }
        persistInputs()
    }

    fun onFindTermInputChanged(input: String) {
        _uiState.update { it.copy(findTermInput = input, findTermError = null) }
    }

    fun toggleFormulaExpanded() {
        _uiState.update { it.copy(isFormulaExpanded = !it.isFormulaExpanded) }
    }

    fun toggleFindTermExpanded() {
        _uiState.update { it.copy(isFindTermExpanded = !it.isFindTermExpanded) }
    }

    fun clearNotification() {
        _uiState.update { it.copy(userNotification = null) }
    }

    fun generateSequence() {
        calculationJob?.cancel()

        val state = _uiState.value
        val firstTermValidation = validator.validateFirstTerm(state.firstTermInput)
        if (firstTermValidation.isFailure) {
            _uiState.update {
                it.copy(
                    errorMessage = firstTermValidation.exceptionOrNull()?.message,
                    isCalculating = false
                )
            }
            return
        }

        val stepRatioValidation = validator.validateStepOrRatio(state.stepOrRatioInput, state.type)
        if (stepRatioValidation.isFailure) {
            _uiState.update {
                it.copy(
                    errorMessage = stepRatioValidation.exceptionOrNull()?.message,
                    isCalculating = false
                )
            }
            return
        }

        val termsValidation = validator.validateNumberOfTerms(state.numberOfTermsInput)
        if (termsValidation.isFailure) {
            _uiState.update {
                it.copy(
                    errorMessage = termsValidation.exceptionOrNull()?.message,
                    isCalculating = false
                )
            }
            return
        }

        val firstTerm = firstTermValidation.getOrThrow()
        val stepOrRatio = stepRatioValidation.getOrThrow()
        val numberOfTerms = termsValidation.getOrThrow()

        _uiState.update {
            it.copy(
                isCalculating = true,
                errorMessage = null,
                findTermResult = null,
                findTermError = null
            )
        }

        calculationJob = viewModelScope.launch {
            try {
                withTimeout(CALCULATION_TIMEOUT_MS) {
                    if (preCalculationDelayMs > 0) {
                        kotlinx.coroutines.delay(preCalculationDelayMs)
                    }
                    val result = withContext(defaultDispatcher) {
                        calculator.calculateSequence(
                            type = state.type,
                            firstTerm = firstTerm,
                            stepOrRatio = stepOrRatio,
                            numberOfTerms = numberOfTerms
                        ).getOrThrow()
                    }

                    val currentSettings = settings.value
                    val decPlaces = currentSettings.decimalPlaces
                    val rMode = currentSettings.roundingMode

                    val formattedTerms = formatter.formatTermsList(result.terms, decPlaces, rMode)
                    val formattedSum = formatter.formatNumber(result.sum, decPlaces, rMode)
                    val formattedFirst = formatter.formatNumber(result.firstGeneratedTerm, decPlaces, rMode)
                    val formattedLast = formatter.formatNumber(result.lastGeneratedTerm, decPlaces, rMode)
                    val formattedMin = formatter.formatNumber(result.minimum, decPlaces, rMode)
                    val formattedMax = formatter.formatNumber(result.maximum, decPlaces, rMode)
                    val hasRounding = formatter.hasAnyRounding(result, decPlaces, rMode.mathRoundingMode)

                    _uiState.update {
                        it.copy(
                            generatedTerms = formattedTerms,
                            sum = formattedSum,
                            firstTerm = formattedFirst,
                            lastTerm = formattedLast,
                            minimum = formattedMin,
                            maximum = formattedMax,
                            formula = result.formula,
                            direction = result.direction,
                            isExact = !hasRounding,
                            currentResult = result,
                            isCalculating = false,
                            errorMessage = null
                        )
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _uiState.update {
                    it.copy(
                        isCalculating = false,
                        errorMessage = "Calculation took too long. Try fewer terms or smaller values."
                    )
                }
            } catch (e: CancellationException) {
                _uiState.update {
                    it.copy(
                        isCalculating = false,
                        errorMessage = "Calculation cancelled."
                    )
                }
            } catch (e: OutputTooLargeException) {
                _uiState.update {
                    it.copy(
                        isCalculating = false,
                        errorMessage = e.message
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCalculating = false,
                        errorMessage = e.message ?: "An error occurred during calculation."
                    )
                }
            }
        }
    }

    fun cancelCalculation() {
        val wasActive = calculationJob?.isActive == true || _uiState.value.isCalculating
        calculationJob?.cancel()
        if (wasActive) {
            _uiState.update {
                it.copy(
                    isCalculating = false,
                    errorMessage = "Calculation cancelled."
                )
            }
        }
    }

    fun calculateDirectTerm() {
        findTermJob?.cancel()

        val state = _uiState.value
        val termValidation = validator.validateTermNumber(state.findTermInput)
        if (termValidation.isFailure) {
            _uiState.update {
                it.copy(
                    findTermError = termValidation.exceptionOrNull()?.message,
                    findTermResult = null
                )
            }
            return
        }

        val firstTermValidation = validator.validateFirstTerm(state.firstTermInput)
        if (firstTermValidation.isFailure) {
            _uiState.update {
                it.copy(
                    findTermError = firstTermValidation.exceptionOrNull()?.message,
                    findTermResult = null
                )
            }
            return
        }

        val stepRatioValidation = validator.validateStepOrRatio(state.stepOrRatioInput, state.type)
        if (stepRatioValidation.isFailure) {
            _uiState.update {
                it.copy(
                    findTermError = stepRatioValidation.exceptionOrNull()?.message,
                    findTermResult = null
                )
            }
            return
        }

        val termNumber = termValidation.getOrThrow()
        val firstTerm = firstTermValidation.getOrThrow()
        val stepOrRatio = stepRatioValidation.getOrThrow()

        _uiState.update {
            it.copy(
                isFindTermCalculating = true,
                findTermError = null
            )
        }

        findTermJob = viewModelScope.launch {
            try {
                withTimeout(CALCULATION_TIMEOUT_MS) {
                    val termValue = withContext(defaultDispatcher) {
                        calculator.calculateTerm(
                            type = state.type,
                            firstTerm = firstTerm,
                            stepOrRatio = stepOrRatio,
                            termNumber = termNumber
                        )
                    }
                    val currentSettings = settings.value
                    val formatted = formatter.formatNumber(
                        termValue,
                        currentSettings.decimalPlaces,
                        currentSettings.roundingMode
                    )
                    _uiState.update {
                        it.copy(
                            isFindTermCalculating = false,
                            findTermResult = "Term $termNumber = $formatted",
                            findTermError = null
                        )
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _uiState.update {
                    it.copy(
                        isFindTermCalculating = false,
                        findTermError = "Calculation took too long. Try fewer terms or smaller values."
                    )
                }
            } catch (e: OutputTooLargeException) {
                _uiState.update {
                    it.copy(
                        isFindTermCalculating = false,
                        findTermError = e.message
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isFindTermCalculating = false,
                        findTermError = e.message ?: "An error occurred."
                    )
                }
            }
        }
    }

    fun saveCurrentSequence(): Boolean {
        val result = _uiState.value.currentResult ?: return false
        val saved = historyRepository?.saveSequence(
            type = result.type,
            firstTerm = _uiState.value.firstTermInput,
            stepOrRatio = _uiState.value.stepOrRatioInput,
            numberOfTerms = result.numberOfTerms
        ) ?: false

        _uiState.update {
            it.copy(
                userNotification = if (saved) "Sequence saved to history." else "Sequence is already saved."
            )
        }
        return saved
    }

    fun restoreHistoryItem(item: SequenceHistory) {
        _uiState.update {
            it.copy(
                type = item.type,
                firstTermInput = item.firstTerm,
                stepOrRatioInput = item.stepOrRatio,
                numberOfTermsInput = item.numberOfTerms.toString(),
                errorMessage = null,
                findTermResult = null,
                findTermError = null
            )
        }
        persistInputs()
        generateSequence()
    }

    fun deleteHistoryItem(id: String) {
        historyRepository?.deleteSequence(id)
        _uiState.update {
            it.copy(userNotification = "Sequence removed from history.")
        }
    }

    fun clearHistory() {
        historyRepository?.clearHistory()
        _uiState.update {
            it.copy(userNotification = "History cleared.")
        }
    }

    fun clearInputsAndResults() {
        calculationJob?.cancel()
        findTermJob?.cancel()
        _uiState.update {
            it.copy(
                firstTermInput = "",
                stepOrRatioInput = "",
                numberOfTermsInput = "",
                generatedTerms = emptyList(),
                sum = null,
                firstTerm = null,
                lastTerm = null,
                minimum = null,
                maximum = null,
                formula = null,
                errorMessage = null,
                isCalculating = false,
                findTermInput = "",
                findTermResult = null,
                findTermError = null,
                direction = null,
                currentResult = null
            )
        }
        persistInputs()
    }

    fun getCopyText(): String? {
        val result = _uiState.value.currentResult ?: return null
        val currentSettings = settings.value
        return formatter.formatCopyShareText(
            result,
            currentSettings.decimalPlaces,
            currentSettings.roundingMode
        )
    }

    // Settings actions
    fun updateTheme(themeMode: ThemeMode) {
        settingsRepository?.updateTheme(themeMode)
    }

    fun updateDynamicColor(enabled: Boolean) {
        settingsRepository?.updateDynamicColor(enabled)
    }

    fun updateHaptics(enabled: Boolean) {
        settingsRepository?.updateHaptics(enabled)
    }

    fun updateAnimations(enabled: Boolean) {
        settingsRepository?.updateAnimations(enabled)
    }

    fun updateConfirmDelete(enabled: Boolean) {
        settingsRepository?.updateConfirmDelete(enabled)
    }

    fun updateDecimalPlaces(places: Int) {
        settingsRepository?.updateDecimalPlaces(places)
    }

    fun updateRoundingMode(roundingMode: AppRoundingMode) {
        settingsRepository?.updateRoundingMode(roundingMode)
    }

    fun resetSettings() {
        settingsRepository?.resetSettings()
        _uiState.update {
            it.copy(userNotification = "Settings reset to defaults.")
        }
    }

    private fun persistInputs() {
        val state = _uiState.value
        settingsRepository?.saveLastInputs(
            type = state.type,
            firstTerm = state.firstTermInput,
            stepOrRatio = state.stepOrRatioInput,
            numberOfTerms = state.numberOfTermsInput
        )
    }

    private fun reformatCurrentResult(currentSettings: AppSettings) {
        val result = _uiState.value.currentResult ?: return
        val decPlaces = currentSettings.decimalPlaces
        val rMode = currentSettings.roundingMode

        val formattedTerms = formatter.formatTermsList(result.terms, decPlaces, rMode)
        val formattedSum = formatter.formatNumber(result.sum, decPlaces, rMode)
        val formattedFirst = formatter.formatNumber(result.firstGeneratedTerm, decPlaces, rMode)
        val formattedLast = formatter.formatNumber(result.lastGeneratedTerm, decPlaces, rMode)
        val formattedMin = formatter.formatNumber(result.minimum, decPlaces, rMode)
        val formattedMax = formatter.formatNumber(result.maximum, decPlaces, rMode)
        val hasRounding = formatter.hasAnyRounding(result, decPlaces, rMode.mathRoundingMode)

        _uiState.update {
            it.copy(
                generatedTerms = formattedTerms,
                sum = formattedSum,
                firstTerm = formattedFirst,
                lastTerm = formattedLast,
                minimum = formattedMin,
                maximum = formattedMax,
                isExact = !hasRounding
            )
        }
    }

    companion object {
        var CALCULATION_TIMEOUT_MS = 5000L

        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val settingsRepo = SettingsRepository(application)
                    val historyRepo = HistoryRepository(application)
                    return SequenceBoxViewModel(
                        settingsRepository = settingsRepo,
                        historyRepository = historyRepo
                    ) as T
                }
            }
    }
}
