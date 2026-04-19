package com.brainfocus.numberdetective.data.storage

import com.brainfocus.numberdetective.data.model.Hint

data class LevelResult(
    val levelNumber: Int,
    val secretNumber: String,
    val hints: List<Hint>,
    val durationSeconds: Int,
    val scoreGained: Int
)

data class GameSession(
    val id: String,
    val timestamp: Long,
    val levels: List<LevelResult>,
    val totalScore: Int,
    val isWin: Boolean
)

object GameResultStorage {
    // Current ongoing session data
    var currentSessionLevels = mutableListOf<LevelResult>()
    
    // Last completed session for Result screen
    var lastGameSession: GameSession? = null
}
