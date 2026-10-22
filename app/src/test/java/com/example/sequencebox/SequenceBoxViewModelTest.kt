package com.example.sequencebox

import com.example.sequencebox.data.HistoryRepository
import com.example.sequencebox.data.SettingsRepository
import com.example.sequencebox.domain.OutputTooLargeException
import com.example.sequencebox.domain.SequenceBoxCalculator
import com.example.sequencebox.domain.SequenceFormatter
import com.example.sequencebox.domain.SequenceValidator
import com.example.sequencebox.model.AppRoundingMode
import com.example.sequencebox.model.SequenceHistory
import com.example.sequencebox.model.SequenceType
import com.example.sequencebox.model.ThemeMode
import com.example.sequencebox.ui.SequenceBoxViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class SequenceBoxViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakePrefsSettings = FakeSharedPreferences()
    private val fakePrefsHistory = FakeSharedPreferences()

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var historyRepository: HistoryRepository
    private lateinit var viewModel: SequenceBoxViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        settingsRepository = SettingsRepository(fakePrefsSettings)
        historyRepository = HistoryRepository(fakePrefsHistory)
        viewModel = SequenceBoxViewModel(
            calculator = SequenceBoxCalculator(),
            validator = SequenceValidator(),
            formatter = SequenceFormatter(),
            settingsRepository = settingsRepository,
            historyRepository = historyRepository,
            defaultDispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        SequenceBoxViewModel.CALCULATION_TIMEOUT_MS = 5000L
        Dispatchers.resetMain()
    }

    // 37. Output-size protection
    @Test
    fun testOutputSizeProtection() = runTest(testDispatcher) {
        viewModel.onTypeChanged(SequenceType.GEOMETRIC)
        viewModel.onFirstTermInputChanged("1000000000000000000")
        viewModel.onStepOrRatioInputChanged("1000000000000000000")
        viewModel.onNumberOfTermsInputChanged("500")

        viewModel.generateSequence()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
        assertEquals(
            "The sequence becomes too large to display safely. Try fewer terms or smaller values.",
            state.errorMessage
        )
    }

    // 38. Calculation timeout
    @Test
    fun testCalculationTimeoutProtection() = runTest(testDispatcher) {
        viewModel.onFirstTermInputChanged("2")
        viewModel.onStepOrRatioInputChanged("3")
        viewModel.onNumberOfTermsInputChanged("6")

        viewModel.preCalculationDelayMs = 6000L
        viewModel.generateSequence()
        testScheduler.advanceTimeBy(5500L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(
            "Calculation took too long. Try fewer terms or smaller values.",
            state.errorMessage
        )
        assertFalse(state.isCalculating)
    }

    // 39. Calculation cancellation
    @Test
    fun testCalculationCancellation() = runTest(testDispatcher) {
        viewModel.onFirstTermInputChanged("2")
        viewModel.onStepOrRatioInputChanged("3")
        viewModel.onNumberOfTermsInputChanged("6")

        viewModel.generateSequence()
        viewModel.cancelCalculation()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Calculation cancelled.", state.errorMessage)
        assertFalse(state.isCalculating)
    }

    // 42. History save
    @Test
    fun testHistorySave() = runTest(testDispatcher) {
        viewModel.onFirstTermInputChanged("2")
        viewModel.onStepOrRatioInputChanged("3")
        viewModel.onNumberOfTermsInputChanged("6")
        viewModel.generateSequence()
        advanceUntilIdle()

        val saved = viewModel.saveCurrentSequence()
        assertTrue(saved)
        advanceUntilIdle()

        val history = viewModel.uiState.value.history
        assertEquals(1, history.size)
        assertEquals("2", history[0].firstTerm)
        assertEquals("3", history[0].stepOrRatio)
        assertEquals(6, history[0].numberOfTerms)
    }

    // 43. History restore
    @Test
    fun testHistoryRestore() = runTest(testDispatcher) {
        val historyItem = SequenceHistory(
            id = "test-1",
            type = SequenceType.GEOMETRIC,
            firstTerm = "2",
            stepOrRatio = "3",
            numberOfTerms = 6,
            savedAt = System.currentTimeMillis()
        )

        viewModel.restoreHistoryItem(historyItem)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(SequenceType.GEOMETRIC, state.type)
        assertEquals("2", state.firstTermInput)
        assertEquals("3", state.stepOrRatioInput)
        assertEquals("6", state.numberOfTermsInput)
        assertEquals("728", state.sum)
        assertEquals("486", state.lastTerm)
    }

    // 44. History limit: maximum 10 entries; 11th entry removes oldest
    @Test
    fun testHistoryLimit() = runTest(testDispatcher) {
        for (i in 1..10) {
            historyRepository.saveSequence(
                type = SequenceType.ARITHMETIC,
                firstTerm = "$i",
                stepOrRatio = "1",
                numberOfTerms = 5
            )
        }
        advanceUntilIdle()
        assertEquals(10, historyRepository.history.value.size)

        // Add 11th entry
        historyRepository.saveSequence(
            type = SequenceType.ARITHMETIC,
            firstTerm = "11",
            stepOrRatio = "1",
            numberOfTerms = 5
        )
        advanceUntilIdle()

        val history = historyRepository.history.value
        assertEquals(10, history.size)
        // Most recent should be 11
        assertEquals("11", history.first().firstTerm)
        // Oldest entry (1) should have been removed, oldest remaining is 2
        assertEquals("2", history.last().firstTerm)
    }

    // 45. Duplicate history: consecutive duplicate not added
    @Test
    fun testDuplicateHistory() = runTest(testDispatcher) {
        val firstSave = historyRepository.saveSequence(
            type = SequenceType.ARITHMETIC,
            firstTerm = "2",
            stepOrRatio = "3",
            numberOfTerms = 6
        )
        assertTrue(firstSave)

        // Try exact same consecutively
        val secondSave = historyRepository.saveSequence(
            type = SequenceType.ARITHMETIC,
            firstTerm = "2",
            stepOrRatio = "3",
            numberOfTerms = 6
        )
        assertFalse(secondSave)

        advanceUntilIdle()
        assertEquals(1, historyRepository.history.value.size)
    }

    // 46. History deletion
    @Test
    fun testHistoryDeletion() = runTest(testDispatcher) {
        historyRepository.saveSequence(
            type = SequenceType.ARITHMETIC,
            firstTerm = "5",
            stepOrRatio = "2",
            numberOfTerms = 4
        )
        advanceUntilIdle()
        val id = historyRepository.history.value.first().id

        viewModel.deleteHistoryItem(id)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.history.isEmpty())

        // Test clear all history
        historyRepository.saveSequence(
            type = SequenceType.ARITHMETIC,
            firstTerm = "1",
            stepOrRatio = "1",
            numberOfTerms = 3
        )
        advanceUntilIdle()
        assertEquals(1, historyRepository.history.value.size)

        viewModel.clearHistory()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.history.isEmpty())
    }

    // 47. Settings persistence
    @Test
    fun testSettingsPersistence() = runTest(testDispatcher) {
        viewModel.updateTheme(ThemeMode.DARK)
        viewModel.updateDecimalPlaces(4)
        viewModel.updateRoundingMode(AppRoundingMode.HALF_EVEN)

        val settings = viewModel.settings.value
        assertEquals(ThemeMode.DARK, settings.themeMode)
        assertEquals(4, settings.decimalPlaces)
        assertEquals(AppRoundingMode.HALF_EVEN, settings.roundingMode)

        // Verify loaded from prefs
        val loadedRepo = SettingsRepository(fakePrefsSettings)
        val loaded = loadedRepo.settings.value
        assertEquals(ThemeMode.DARK, loaded.themeMode)
        assertEquals(4, loaded.decimalPlaces)
        assertEquals(AppRoundingMode.HALF_EVEN, loaded.roundingMode)
    }

    // 48. Settings reset
    @Test
    fun testSettingsReset() = runTest(testDispatcher) {
        // Save history first
        historyRepository.saveSequence(
            type = SequenceType.ARITHMETIC,
            firstTerm = "2",
            stepOrRatio = "3",
            numberOfTerms = 6
        )

        // Change settings
        viewModel.updateTheme(ThemeMode.DARK)
        viewModel.updateDecimalPlaces(20)
        viewModel.updateDynamicColor(false)

        // Reset settings
        viewModel.resetSettings()

        val settings = viewModel.settings.value
        assertEquals(ThemeMode.SYSTEM, settings.themeMode)
        assertTrue(settings.dynamicColor)
        assertTrue(settings.hapticsEnabled)
        assertTrue(settings.animationsEnabled)
        assertTrue(settings.confirmDelete)
        assertEquals(10, settings.decimalPlaces)
        assertEquals(AppRoundingMode.HALF_UP, settings.roundingMode)

        // History remains intact!
        assertEquals(1, historyRepository.history.value.size)
    }

    // Clear inputs test
    @Test
    fun testClearInputs() = runTest(testDispatcher) {
        viewModel.onFirstTermInputChanged("2")
        viewModel.onStepOrRatioInputChanged("3")
        viewModel.onNumberOfTermsInputChanged("6")
        viewModel.generateSequence()
        advanceUntilIdle()

        viewModel.clearInputsAndResults()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.firstTermInput)
        assertEquals("", state.stepOrRatioInput)
        assertEquals("", state.numberOfTermsInput)
        assertTrue(state.generatedTerms.isEmpty())
        assertNull(state.sum)
    }
}
