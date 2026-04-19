package com.brainfocus.numberdetective.data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.brainfocus.numberdetective.data.storage.GameSession
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "number_detective_prefs")

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()

    private object PreferencesKeys {
        val HIGH_SCORE = intPreferencesKey("high_score")
        val CURRENT_LEVEL = intPreferencesKey("current_level")
        val REMAINING_ATTEMPTS = intPreferencesKey("remaining_attempts")
        val REMAINING_TIME = intPreferencesKey("remaining_time")
        val CURRENT_SCORE = intPreferencesKey("current_score")
        val CORRECT_ANSWER = stringPreferencesKey("correct_answer")
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val IS_SOUND_ENABLED = booleanPreferencesKey("is_sound_enabled")
        val IS_HELPER_MODE_ENABLED = booleanPreferencesKey("is_helper_mode_enabled")
        val ALL_TIME_HIGH_SCORE = intPreferencesKey("all_time_high_score")
        val LAST_SCORE_DATE = stringPreferencesKey("last_score_date")
        val SESSIONS_HISTORY = stringPreferencesKey("sessions_history")
    }

    // High Score Flow
    val highScoreFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        val lastDate = preferences[PreferencesKeys.LAST_SCORE_DATE] ?: ""
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        if (lastDate == currentDate) {
            preferences[PreferencesKeys.HIGH_SCORE] ?: 0
        } else {
            0 // New day, reset daily high score view
        }
    }

    val allTimeHighScoreFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ALL_TIME_HIGH_SCORE] ?: 0
    }

    suspend fun saveHighScore(score: Int) {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        context.dataStore.edit { preferences ->
            val lastDate = preferences[PreferencesKeys.LAST_SCORE_DATE] ?: ""
            
            // Handle Daily High Score
            val currentDailyHighScore = if (lastDate == currentDate) {
                preferences[PreferencesKeys.HIGH_SCORE] ?: 0
            } else {
                0
            }
            
            if (score > currentDailyHighScore) {
                preferences[PreferencesKeys.HIGH_SCORE] = score
            } else if (lastDate != currentDate) {
                // First game of a new day, set the daily score
                preferences[PreferencesKeys.HIGH_SCORE] = score
            }

            // Handle All-Time High Score
            val currentAllTime = preferences[PreferencesKeys.ALL_TIME_HIGH_SCORE] ?: 0
            if (score > currentAllTime) {
                preferences[PreferencesKeys.ALL_TIME_HIGH_SCORE] = score
            }

            preferences[PreferencesKeys.LAST_SCORE_DATE] = currentDate
        }
    }

    // Game State Flow (Allows resuming an incomplete game)
    val currentLevelFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CURRENT_LEVEL] ?: 1
    }

    val remainingAttemptsFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REMAINING_ATTEMPTS] ?: -1
    }

    val remainingTimeFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REMAINING_TIME] ?: -1
    }
    
    val currentScoreFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CURRENT_SCORE] ?: 0
    }

    val correctAnswerFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CORRECT_ANSWER]
    }

    val isFirstLaunchFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_FIRST_LAUNCH] ?: true
    }

    val isSoundEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_SOUND_ENABLED] ?: true
    }

    val isHelperModeEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_HELPER_MODE_ENABLED] ?: false
    }

    suspend fun toggleSound(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_SOUND_ENABLED] = enabled
        }
    }

    suspend fun toggleHelperMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_HELPER_MODE_ENABLED] = enabled
        }
    }

    suspend fun completeOnboarding() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_FIRST_LAUNCH] = false
        }
    }

    suspend fun saveGameState(
        level: Int,
        attempts: Int,
        time: Int,
        score: Int,
        answer: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_LEVEL] = level
            preferences[PreferencesKeys.REMAINING_ATTEMPTS] = attempts
            preferences[PreferencesKeys.REMAINING_TIME] = time
            preferences[PreferencesKeys.CURRENT_SCORE] = score
            preferences[PreferencesKeys.CORRECT_ANSWER] = answer
        }
    }

    suspend fun clearGameState() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.CURRENT_LEVEL)
            preferences.remove(PreferencesKeys.REMAINING_ATTEMPTS)
            preferences.remove(PreferencesKeys.REMAINING_TIME)
            preferences.remove(PreferencesKeys.CURRENT_SCORE)
            preferences.remove(PreferencesKeys.CORRECT_ANSWER)
        }
    }

    // Sessions History
    val historyFlow: Flow<List<GameSession>> = context.dataStore.data.map { preferences ->
        val json = preferences[PreferencesKeys.SESSIONS_HISTORY] ?: "[]"
        val type = object : TypeToken<List<GameSession>>() {}.type
        try {
            gson.fromJson<List<GameSession>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveGameSession(session: GameSession) {
        context.dataStore.edit { preferences ->
            val json = preferences[PreferencesKeys.SESSIONS_HISTORY] ?: "[]"
            val type = object : TypeToken<List<GameSession>>() {}.type
            val currentHistory: MutableList<GameSession> = try {
                gson.fromJson<List<GameSession>>(json, type)?.toMutableList() ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }

            // Add new session at the beginning
            currentHistory.add(0, session)

            // Keep only the last 50 games
            val limitedHistory = if (currentHistory.size > 50) {
                currentHistory.take(50)
            } else {
                currentHistory
            }

            preferences[PreferencesKeys.SESSIONS_HISTORY] = gson.toJson(limitedHistory)
        }
    }
}
