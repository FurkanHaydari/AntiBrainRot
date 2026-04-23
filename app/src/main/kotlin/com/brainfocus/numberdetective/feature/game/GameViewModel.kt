package com.brainfocus.numberdetective.feature.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.game.NumberDetectiveGame
import com.brainfocus.numberdetective.data.model.GameState
import com.brainfocus.numberdetective.data.model.GuessResult
import com.brainfocus.numberdetective.data.model.Hint
import com.brainfocus.numberdetective.core.sound.SoundManager
import com.brainfocus.numberdetective.data.storage.DataStoreManager
import com.brainfocus.numberdetective.data.storage.GameResultStorage
import com.brainfocus.numberdetective.data.model.FieldReport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import com.brainfocus.numberdetective.feature.result.DiagnosticEngine
import com.brainfocus.numberdetective.data.model.DigitStatus
import kotlin.math.max

@HiltViewModel
@Suppress("unused")
class GameViewModel @Inject constructor(
    application: Application,
    private val game: NumberDetectiveGame,
    private val soundManager: SoundManager,
    private val dataStoreManager: DataStoreManager
) : AndroidViewModel(application) {
    private var _attempts = 0
    private var _attemptsInLevel = 0
    private var _levelStartSeconds = 0
    private var _archiveChecksInLevel = 0
    private var _duplicateGuessesInLevel = 0
    private val _logicalMistakesCount = MutableStateFlow(0)
    val logicalMistakesCount: StateFlow<Int> = _logicalMistakesCount
    
    private val _currentReport = MutableStateFlow<FieldReport?>(null)
    val currentReport: StateFlow<FieldReport?> = _currentReport
    
    private val _remainingAttempts = MutableStateFlow(MAX_ATTEMPTS)
    val remainingAttempts: StateFlow<Int> = _remainingAttempts
    
    private val _guesses = MutableStateFlow<List<String>>(emptyList())
    val guesses: StateFlow<List<String>> = _guesses
    
    private val _gameState = MutableStateFlow<GameState>(GameState.Initial)
    val gameState: StateFlow<GameState> = _gameState
    
    private val _hints = MutableStateFlow<List<Hint>>(emptyList())
    val hints: StateFlow<List<Hint>> = _hints
    
    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score
    
    private val _correctAnswer = MutableStateFlow("")
    val correctAnswer: StateFlow<String> = _correctAnswer

    private val _currentLevel = MutableStateFlow(1)
    val currentLevel: StateFlow<Int> = _currentLevel

    private val _remainingTime = MutableStateFlow(180) 
    val remainingTime: StateFlow<Int> = _remainingTime

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused

    private val _countdownValue = MutableStateFlow<Int?>(null)
    val countdownValue: StateFlow<Int?> = _countdownValue

    // Settings Flows
    val dailyHighScore = dataStoreManager.highScoreFlow
    val allTimeHighScore = dataStoreManager.allTimeHighScoreFlow
    val isSoundEnabled = dataStoreManager.isSoundEnabledFlow
    val isHelperModeEnabled = dataStoreManager.isHelperModeEnabledFlow
    private var isHelperModeEnabledLocal = false

    private var timerJob: Job? = null
    private var countdownJob: Job? = null
    var startTime = System.currentTimeMillis()

    val attempts: Int get() = _attempts

    init {
        // Observe settings to update local states
        viewModelScope.launch {
            dataStoreManager.isSoundEnabledFlow.collect { enabled ->
                soundManager.setSoundEnabled(enabled)
            }
        }
        viewModelScope.launch {
            dataStoreManager.isHelperModeEnabledFlow.collect { enabled ->
                isHelperModeEnabledLocal = enabled
            }
        }
        
        startNewGame()
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch { dataStoreManager.toggleSound(enabled) }
    }

    fun toggleHelperMode(enabled: Boolean) {
        viewModelScope.launch { dataStoreManager.toggleHelperMode(enabled) }
    }

    fun startNewGame(isFirstGame: Boolean = true) {
        if (isFirstGame) {
            _attempts = 0
            _logicalMistakesCount.value = 0
            _remainingAttempts.value = MAX_ATTEMPTS
            _score.value = 0
            startTime = System.currentTimeMillis()
            _remainingTime.value = 180
            _currentLevel.value = 1
            
            // Clear previous session data
            GameResultStorage.currentSessionLevels.clear()
        }
        
        _attemptsInLevel = 0
        _archiveChecksInLevel = 0
        _duplicateGuessesInLevel = 0
        _levelStartSeconds = getTimeInSeconds()
        _guesses.value = emptyList()
        game.startNewGame(_currentLevel.value)
        _correctAnswer.value = game.getCorrectAnswer()
        Log.d("NumberDetectiveDebug", "Target Number for Level ${_currentLevel.value}: ${_correctAnswer.value}")
        _gameState.value = GameState.Playing

        // Restore Hint Generation Logic with dynamic calculation to prevent hardcoded mismatches
        val hintList = mutableListOf<Hint>()
        val secret = _correctAnswer.value
        
        fun createHint(hintStr: String, hintIndex: Int): Hint {
            val statuses = calculateDigitStatuses(hintStr, secret)
            val correctCount = statuses.count { it == DigitStatus.CORRECT_POS }
            val misplacedCount = statuses.count { it == DigitStatus.WRONG_POS }
            
            return Hint(
                guess = hintStr,
                correct = correctCount,
                misplaced = misplacedCount,
                descriptionRes = getHintResId(_currentLevel.value, hintIndex),
                digitStatuses = statuses,
                isSystemHint = true
            )
        }

        if (_currentLevel.value == 3) {
            hintList.addAll(listOf(
                createHint(game.firstHint, 1),
                createHint(game.secondHint, 2),
                createHint(game.thirdHint, 3),
                createHint(game.fourthHint, 4),
                createHint(game.fifthHint, 5)
            ))
            _hints.value = hintList
        } else {
            val h = listOf(
                createHint(game.firstHint, 1),
                createHint(game.secondHint, 2),
                createHint(game.thirdHint, 3),
                createHint(game.fourthHint, 4),
                createHint(game.fifthHint, 5)
            )
            _hints.value = if (_currentLevel.value == 2) h.shuffled() else h
        }
        
        if (isFirstGame) {
            startTimer()
            startCountdown()
        }
    }

    fun nextLevel() {
        if (_gameState.value !is GameState.Playing) return
        
        val nextLvl = _currentLevel.value + 1
        if (nextLvl > MAX_LEVELS) {
            _gameState.value = GameState.Win
            timerJob?.cancel()
            return
        }
        
        _currentLevel.value = nextLvl
        
        // Dynamic bonuses
        var bonusAttempts = 0
        var bonusTime = 0
        when (nextLvl) {
            2 -> {
                bonusAttempts = 2
                bonusTime = 40
            }
            3 -> {
                bonusAttempts = 3
                bonusTime = 80
            }
        }
        _remainingAttempts.value += bonusAttempts
        _remainingTime.value += bonusTime
        
        soundManager.playLevelUpSound()
        
        // Trigger Promotion Report
        _currentReport.value = FieldReport.Promotion(nextLvl, bonusAttempts, bonusTime)
        _isPaused.value = true
        
        startNewGame(false)
    }

    private fun saveCurrentLevelToHistory() {
        val levelDuration = getTimeInSeconds() - _levelStartSeconds
        val levelScore = calculateLevelScore()
        
        val levelResult = com.brainfocus.numberdetective.data.storage.LevelResult(
            levelNumber = _currentLevel.value,
            secretNumber = _correctAnswer.value,
            hints = _hints.value,
            durationSeconds = levelDuration,
            scoreGained = levelScore,
            archiveChecks = _archiveChecksInLevel,
            duplicateGuesses = _duplicateGuessesInLevel
        )
        GameResultStorage.currentSessionLevels.add(levelResult)
    }

    private fun finalizeGameSession(isWin: Boolean) {
        saveCurrentLevelToHistory()
        val sessionLevels = GameResultStorage.currentSessionLevels.toList()
        
        // Let the motor handle all the "math" of aggregating metrics
        val partialSession = com.brainfocus.numberdetective.data.storage.GameSession(
            id = "", 
            timestamp = 0L, 
            levels = sessionLevels, 
            totalScore = 0, 
            isWin = isWin
        )
        val metrics = DiagnosticEngine.calculateSessionMetrics(partialSession)

        val session = com.brainfocus.numberdetective.data.storage.GameSession(
            id = java.util.UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            levels = sessionLevels,
            totalScore = _score.value,
            isWin = isWin,
            isHelperMode = isHelperModeEnabledLocal,
            logicalMistakes = _logicalMistakesCount.value,
            totalArchiveChecks = metrics.first,
            totalDuplicateGuesses = metrics.second
        )
        // Inject final diagnostic report into the session itself
        val finalSession = session.copy(
            diagnosticReport = DiagnosticEngine.generateReport(session)
        )
        GameResultStorage.lastGameSession = finalSession
        
        // Save to persistent storage
        viewModelScope.launch {
            dataStoreManager.saveGameSession(finalSession)
        }
    }

    fun makeGuess(guess: String): GuessResult {
        if (guess.isBlank() || !guess.all { it.isDigit() }) return GuessResult.Invalid
        
        // Logical Analysis: Check if the guess contradicts previous evidence
        if (!isGuessLogical(guess, _hints.value)) {
            _logicalMistakesCount.value++
            Log.d("NumberDetectiveDebug", "Logical mistake detected! Total: ${_logicalMistakesCount.value}")
        }

        // Protocol Check: Unique Digits
        if (guess.toSet().size != guess.length) {
            _currentReport.value = FieldReport.Validation(
                title = R.string.report_unique_title,
                message = R.string.report_unique_msg
            )
            _isPaused.value = true
            soundManager.playPartialWrongSound()
            return GuessResult.Invalid
        }

        // Analysis Check: Duplicate Guess
        if (_guesses.value.contains(guess)) {
            _duplicateGuessesInLevel++
            _currentReport.value = FieldReport.Validation(
                title = R.string.report_duplicate_title,
                message = R.string.report_duplicate_msg
            )
            _isPaused.value = true
            soundManager.playPartialWrongSound()
            return GuessResult.Invalid
        }

        _attempts++
        _attemptsInLevel++
        val currentGuesses = _guesses.value.toMutableList()
        currentGuesses.add(guess)
        _guesses.value = currentGuesses
        _remainingAttempts.value--

        val result = game.makeGuess(guess)
        val requiredDigits = if (_currentLevel.value == 3) 4 else 3
        
        // Calculate Digit Statuses ALWAYS (for history), but UI determines if shown in Game
        val digitStatuses = calculateDigitStatuses(guess, _correctAnswer.value)

        // Add this guess as a new Hint to the log
        val newHint = Hint(
            guess = guess,
            correct = result.correct,
            misplaced = result.misplaced,
            descriptionRes = if (result.correct == requiredDigits) R.string.log_analysis_success else R.string.log_analysis_attempt,
            digitStatuses = digitStatuses,
            timestamp = getTimeInSeconds()
        )
        
        val updatedHints = _hints.value.toMutableList()
        updatedHints.add(newHint)
        _hints.value = updatedHints
        
        return when {
            result.correct == requiredDigits -> {
                if (_currentLevel.value >= MAX_LEVELS) {
                    finalizeGameSession(true)
                    _gameState.value = GameState.Win
                    soundManager.playWinSound()
                    timerJob?.cancel()
                    viewModelScope.launch { dataStoreManager.saveHighScore(_score.value) }
                } else {
                    saveCurrentLevelToHistory()
                    nextLevel()
                }
                GuessResult.Correct
            }
            _remainingAttempts.value <= 0 -> {
                finalizeGameSession(false)
                _gameState.value = GameState.GameOver
                soundManager.playLoseSound()
                timerJob?.cancel()
                viewModelScope.launch { dataStoreManager.saveHighScore(_score.value) }
                GuessResult.Wrong
            }
            else -> {
                soundManager.playPartialWrongSound()
                _currentReport.value = FieldReport.Compromised(_remainingAttempts.value)
                _isPaused.value = true
                GuessResult.Partial
            }
        }
    }

    private fun isGuessLogical(guess: String, previousHints: List<Hint>): Boolean {
        previousHints.forEach { hint ->
            if (hint.digitStatuses.isNullOrEmpty()) return@forEach
            
            hint.guess.forEachIndexed { i, char ->
                val status = hint.digitStatuses.getOrNull(i) ?: return@forEachIndexed
                when (status) {
                    com.brainfocus.numberdetective.data.model.DigitStatus.INCORRECT -> {
                        // If it's confirmed incorrect, it shouldn't be in the guess at all
                        if (guess.contains(char)) return false
                    }
                    com.brainfocus.numberdetective.data.model.DigitStatus.CORRECT_POS -> {
                        // If it's at correct position, it must be at the same position in current guess
                        // Also, if the guess is shorter than index (shouldn't happen with validation), skip
                        if (i < guess.length && guess[i] != char) return false
                    }
                    com.brainfocus.numberdetective.data.model.DigitStatus.WRONG_POS -> {
                        // If it's misplaced, it shouldn't be in THAT SAME position again
                        if (i < guess.length && guess[i] == char) return false
                    }
                }
            }
        }
        return true
    }

    private fun calculateDigitStatuses(guess: String, answer: String): List<com.brainfocus.numberdetective.data.model.DigitStatus> {
        val statuses = mutableListOf<com.brainfocus.numberdetective.data.model.DigitStatus>()
        guess.forEachIndexed { index, char ->
            statuses.add(
                when {
                    char == answer[index] -> com.brainfocus.numberdetective.data.model.DigitStatus.CORRECT_POS
                    answer.contains(char) -> com.brainfocus.numberdetective.data.model.DigitStatus.WRONG_POS
                    else -> com.brainfocus.numberdetective.data.model.DigitStatus.INCORRECT
                }
            )
        }
        return statuses
    }

    fun dismissReport() {
        val wasPromotion = _currentReport.value is FieldReport.Promotion
        _currentReport.value = null
        
        if (wasPromotion) {
            startCountdown()
            return
        }

        // Resume countdown if it was in progress during pause or validation
        val currentCV = _countdownValue.value
        if (currentCV != null) {
            startCountdown(currentCV)
        } else {
            _isPaused.value = false
        }
    }

    private fun startCountdown(startFrom: Int? = null) {
        val initialValue = startFrom ?: 3
        countdownJob?.cancel()
        _isPaused.value = true
        countdownJob = viewModelScope.launch {
            for (i in initialValue downTo 1) {
                _countdownValue.value = i
                soundManager.playBeepSound()
                delay(1000)
            }
            _countdownValue.value = 0 // "GO!"
            delay(500)
            _countdownValue.value = null
            _isPaused.value = false
            _levelStartSeconds = getTimeInSeconds()
        }
    }

    fun pauseGame() {
        if (_gameState.value is GameState.Playing && _currentReport.value == null) {
            countdownJob?.cancel()
            _isPaused.value = true
            _currentReport.value = FieldReport.Pause()
        }
    }

    fun resumeGame() {
        if (_currentReport.value is FieldReport.Pause) {
            _currentReport.value = null
            
            // Resume countdown if it was in progress (including 0/"GO!" state)
            val currentCV = _countdownValue.value
            if (currentCV != null) {
                startCountdown(currentCV)
            } else {
                _isPaused.value = false
            }
        }
    }

    fun recordArchiveOpen() {
        _archiveChecksInLevel++
    }

    private fun calculateLevelScore(): Int {
        // Generate a localized report for just this level
        val levelTime = getTimeInSeconds() - _levelStartSeconds
        
        // Verimlilik verisini doğrudan motor hesaplıyor.
        val efficiencyIdx = DiagnosticEngine.calculateEfficiencyIndex(_hints.value, _attemptsInLevel, _currentLevel.value)

        val levelReport = DiagnosticEngine.generateReport(
            isWin = true,
            totalAttempts = _attemptsInLevel,
            totalTimeSeconds = levelTime,
            logicalMistakes = _logicalMistakesCount.value,
            totalArchiveChecks = _archiveChecksInLevel,
            totalDuplicateGuesses = _duplicateGuessesInLevel,
            levelNumber = _currentLevel.value,
            efficiencyIdx = efficiencyIdx
        )

        val levelScore = levelReport.score
        
        _score.value += levelScore
        return levelScore
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_remainingTime.value > 0 && _gameState.value !is GameState.Win && _gameState.value !is GameState.GameOver) {
                if (!_isPaused.value) {
                    _remainingTime.value--
                    
                    val beepCount = when {
                        _remainingTime.value <= 10 -> 4
                        _remainingTime.value <= 30 -> 3
                        _remainingTime.value <= 60 -> 2
                        else -> 1
                    }

                    viewModelScope.launch {
                        repeat(beepCount) { i ->
                            soundManager.playBeepSound()
                            if (beepCount > 1 && i < beepCount - 1) delay(120)
                        }
                    }
                }
                delay(1000)
                
                if (_remainingTime.value == 0) {
                    finalizeGameSession(false)
                    _gameState.value = GameState.GameOver
                    soundManager.playLoseSound()
                    viewModelScope.launch { dataStoreManager.saveHighScore(_score.value) }
                }
            }
        }
    }

    fun getTimeInSeconds(): Int = ((System.currentTimeMillis() - startTime) / 1000).toInt()

    private fun getHintResId(level: Int, hintNumber: Int): Int? {
        return if (level == 3) {
            when (hintNumber) {
                1 -> R.string.hint_l3_1
                2 -> R.string.hint_l3_2
                3 -> R.string.hint_l3_3
                4 -> R.string.hint_l3_4
                5 -> R.string.hint_l3_5
                else -> null
            }
        } else {
            when (hintNumber) {
                1 -> R.string.hint_1
                2 -> R.string.hint_2
                3 -> R.string.hint_3
                4 -> R.string.hint_4
                5 -> R.string.hint_5
                else -> null
            }
        }
    }

    companion object {
        const val MAX_ATTEMPTS = 3
        const val MAX_LEVELS = 3
    }
}
