package com.brainfocus.numberdetective.data.storage

import com.brainfocus.numberdetective.data.model.Hint

data class LevelResult(
    val levelNumber: Int,
    val secretNumber: String,
    val hints: List<Hint>,
    val durationSeconds: Int,
    val scoreGained: Int
)

enum class PrecisionLevel(val numeric: Int) { ELITE(5), SHARP(4), STABLE(3), NOVICE(1) }
enum class VelocityLevel(val numeric: Int) { FLASH(5), RAPID(4), STEADY(3), DELIBERATE(1) }
enum class StabilityLevel(val numeric: Int) { CONSTANT(5), INTERMITTENT(2) }
enum class IntuitionLevel(val numeric: Int) { STRONG(5), ANALYTICAL(4), MARGINAL(2) }

data class DiagnosticReport(
    val precision: PrecisionLevel,
    val velocity: VelocityLevel,
    val stability: StabilityLevel,
    val intuition: IntuitionLevel
)

data class GameSession(
    val id: String,
    val timestamp: Long,
    val levels: List<LevelResult>,
    val totalScore: Int,
    val isWin: Boolean,
    val isHelperMode: Boolean = false,
    val logicalMistakes: Int = 0,
    val diagnosticReport: DiagnosticReport? = null
)

object GameResultStorage {
    // Current ongoing session data
    var currentSessionLevels = mutableListOf<LevelResult>()
    
    // Last completed session for Result screen
    var lastGameSession: GameSession? = null
}
