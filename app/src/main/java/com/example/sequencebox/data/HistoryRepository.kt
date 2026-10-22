package com.example.sequencebox.data

import android.content.Context
import android.content.SharedPreferences
import com.example.sequencebox.model.SequenceHistory
import com.example.sequencebox.model.SequenceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class HistoryRepository(
    private val prefs: SharedPreferences
) {
    constructor(context: Context) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    private val _history = MutableStateFlow<List<SequenceHistory>>(loadHistory())
    val history: StateFlow<List<SequenceHistory>> = _history.asStateFlow()

    private fun loadHistory(): List<SequenceHistory> {
        val jsonString = prefs.getString(KEY_HISTORY_LIST, null) ?: return emptyList()
        val list = mutableListOf<SequenceHistory>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val typeStr = obj.getString("type")
                val type = try {
                    SequenceType.valueOf(typeStr)
                } catch (e: Exception) {
                    SequenceType.ARITHMETIC
                }
                list.add(
                    SequenceHistory(
                        id = obj.getString("id"),
                        type = type,
                        firstTerm = obj.getString("firstTerm"),
                        stepOrRatio = obj.getString("stepOrRatio"),
                        numberOfTerms = obj.getInt("numberOfTerms"),
                        savedAt = obj.getLong("savedAt")
                    )
                )
            }
        } catch (e: Exception) {
            // In case of parsing error, return empty list safely
            return emptyList()
        }
        return list
    }

    private fun persistHistory(list: List<SequenceHistory>) {
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("type", item.type.name)
                put("firstTerm", item.firstTerm)
                put("stepOrRatio", item.stepOrRatio)
                put("numberOfTerms", item.numberOfTerms)
                put("savedAt", item.savedAt)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_HISTORY_LIST, jsonArray.toString()).apply()
        _history.value = list
    }

    fun saveSequence(
        type: SequenceType,
        firstTerm: String,
        stepOrRatio: String,
        numberOfTerms: Int
    ): Boolean {
        val currentList = _history.value.toMutableList()

        // Consecutive duplicate protection:
        // If the exact same sequence configuration is saved consecutively, do not create an unnecessary duplicate.
        if (currentList.isNotEmpty()) {
            val mostRecent = currentList.first()
            if (mostRecent.type == type &&
                mostRecent.firstTerm == firstTerm &&
                mostRecent.stepOrRatio == stepOrRatio &&
                mostRecent.numberOfTerms == numberOfTerms
            ) {
                return false
            }
        }

        val newEntry = SequenceHistory(
            id = UUID.randomUUID().toString(),
            type = type,
            firstTerm = firstTerm,
            stepOrRatio = stepOrRatio,
            numberOfTerms = numberOfTerms,
            savedAt = System.currentTimeMillis()
        )

        // Prepend new entry
        currentList.add(0, newEntry)

        // Enforce maximum 10 entries limit
        val trimmedList = if (currentList.size > MAX_HISTORY_ITEMS) {
            currentList.subList(0, MAX_HISTORY_ITEMS)
        } else {
            currentList
        }

        persistHistory(trimmedList)
        return true
    }

    fun deleteSequence(id: String) {
        val currentList = _history.value.toMutableList()
        val changed = currentList.removeAll { it.id == id }
        if (changed) {
            persistHistory(currentList)
        }
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY_LIST).apply()
        _history.value = emptyList()
    }

    companion object {
        private const val PREFS_NAME = "sequencebox_history"
        private const val KEY_HISTORY_LIST = "history_entries"
        const val MAX_HISTORY_ITEMS = 10
    }
}
