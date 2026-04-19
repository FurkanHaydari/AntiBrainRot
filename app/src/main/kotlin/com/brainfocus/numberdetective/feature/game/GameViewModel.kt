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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.annotation.StringRes

sealed class FieldReport(
    @StringRes val titleRes: Int,
    @StringRes val messageRes: Int,
    val isPositive: Boolean,
    val messageArgs: List<Any> = emptyList()
) {
    class Promotion(level: Int, bonusAttempts: Int, bonusTime: Int) : FieldReport(
        titleRes = R.string.report_promotion_title,
        messageRes = R.string.report_promotion_msg,
        messageArgs = listOf(level, bonusAttempts, bonusTime),
        isPositive = true
    )
    class Compromised(remaining: Int) : FieldReport(
        titleRes = R.string.report_compromised_title,
        messageRes = R.string.report_compromised_msg,
        messageArgs = listOf(remaining),
        isPositive = false
    )
    class Validation(@StringRes title: Int, @StringRes message: Int) : FieldReport(
        titleRes = title,
        messageRes = message,
        isPositive = false
    )
    class Pause : FieldReport(
        titleRes = R.string.report_pause_title,
        messageRes = R.string.report_pause_msg,
        isPositive = true
    )
}

@HiltViewModel
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
    private val _wrongAttempts = MutableStateFlow(0)
    
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
            _wrongAttempts.value = 0
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
        _levelStartSeconds = getTimeInSeconds()
        _guesses.value = emptyList()
        game.startNewGame(_currentLevel.value)
        _correctAnswer.value = game.getCorrectAnswer()
        _gameState.value = GameState.Playing

        // Restore Hint Generation Logic
        val hintList = mutableListOf<Hint>()
        if (_currentLevel.value == 3) {
            hintList.addAll(listOf(
                Hint(game.firstHint, 1, 0, descriptionRes = getHintResId(3, 1)),
                Hint(game.secondHint, 0, 1, descriptionRes = getHintResId(3, 2)),
                Hint(game.thirdHint, 1, 1, descriptionRes = getHintResId(3, 3)),
                Hint(game.fourthHint, 1, 1, descriptionRes = getHintResId(3, 4)),
                Hint(game.fifthHint, 2, 0, descriptionRes = getHintResId(3, 5))
            ))
            _hints.value = hintList
        } else {
            val h = listOf(
                Hint(game.firstHint, 1, 0, descriptionRes = getHintResId(_currentLevel.value, 1)),
                Hint(game.secondHint, 0, 1, descriptionRes = getHintResId(_currentLevel.value, 2)),
                Hint(game.thirdHint, 0, 2, descriptionRes = getHintResId(_currentLevel.value, 3)),
                Hint(game.fourthHint, 0, 2, descriptionRes = getHintResId(_currentLevel.value, 4)),
                Hint(game.fifthHint, 1, 1, descriptionRes = getHintResId(_currentLevel.value, 5))
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
            _gameState.value = GameState.Win(_score.value)
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
            scoreGained = levelScore
        )
        GameResultStorage.currentSessionLevels.add(levelResult)
    }

    private fun finalizeGameSession(isWin: Boolean) {
        saveCurrentLevelToHistory()
        val session = com.brainfocus.numberdetective.data.storage.GameSession(
            id = java.util.UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            levels = GameResultStorage.currentSessionLevels.toList(),
            totalScore = _score.value,
            isWin = isWin
        )
        GameResultStorage.lastGameSession = session
        
        // Save to persistent storage
        viewModelScope.launch {
            dataStoreManager.saveGameSession(session)
        }
    }

    fun makeGuess(guess: String): GuessResult {
        if (guess.isBlank() || !guess.all { it.isDigit() }) return GuessResult.Invalid
        
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
                    _gameState.value = GameState.Win(_score.value)
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
                _gameState.value = GameState.GameOver(_score.value)
                soundManager.playLoseSound()
                timerJob?.cancel()
                viewModelScope.launch { dataStoreManager.saveHighScore(_score.value) }
                GuessResult.Wrong
            }
            else -> {
                soundManager.playPartialWrongSound()
                
                // If helper mode is active, we store the per-digit status in the hint
                // But we need to check helper mode synchronously here.
                // I'll grab it from the flow's current value if possible or just use a state variable.
                
                // Simple workaround: the ViewModel can have a local variable updated by the flow.
                
                
                _currentReport.value = FieldReport.Compromised(_remainingAttempts.value)
                _isPaused.value = true
                GuessResult.Partial(result.correct, result.misplaced)
            }
        }
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
        if (_currentReport.value is FieldReport.Promotion) {
            _currentReport.value = null
            startCountdown()
            return
        }
        _currentReport.value = null
        _isPaused.value = false
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        _isPaused.value = true
        countdownJob = viewModelScope.launch {
            for (i in 3 downTo 1) {
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
            _isPaused.value = false
        }
    }

    fun recordArchiveOpen() {
        _archiveChecksInLevel++
    }

    private fun calculateLevelScore(): Int {
        val baseScore = when (_currentLevel.value) {
            1 -> 1000
            2 -> 2500
            3 -> 5000
            else -> 1000
        }
        
        val timeTakenInLevel = getTimeInSeconds() - _levelStartSeconds
        val timePenalty = timeTakenInLevel * 5 // 5 points per second
        val attemptsPenalty = _attemptsInLevel * 100 // 100 points per attempt
        
        // --- Archive & Assistance Penalties ---
        val perCheckPenalty = if (isHelperModeEnabledLocal) 150 else 50
        val archivePenalty = _archiveChecksInLevel * perCheckPenalty
        
        var levelScore = maxOf(0, baseScore - timePenalty - attemptsPenalty - archivePenalty)
        
        // --- Skill Bonuses (Added after zero-floor protection) ---
        // 1. Deduction Bonus: Solved in 2 or fewer attempts
        if (_attemptsInLevel <= 2) {
            levelScore += 500
        }
        
        // 2. Flash Clearance: Solved in under 20 seconds
        if (timeTakenInLevel < 20) {
            levelScore += 300
        }
        
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
                    _gameState.value = GameState.GameOver(_score.value)
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
