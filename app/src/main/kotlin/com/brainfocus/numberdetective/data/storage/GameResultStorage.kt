package com.brainfocus.numberdetective.data.storage

import com.brainfocus.numberdetective.data.model.Hint

data class LevelResult(
    val levelNumber: Int,
    val secretNumber: String,
    val hints: List<Hint>,
    val durationSeconds: Int,
    val scoreGained: Int,
    val archiveChecks: Int = 0,
    val duplicateGuesses: Int = 0
)

enum class PrecisionLevel(val numeric: Int) { ELITE(5), SHARP(4), STABLE(3), NOVICE(1) }
enum class VelocityLevel(val numeric: Int) { FLASH(5), RAPID(4), STEADY(3), DELIBERATE(1) }
enum class StabilityLevel(val numeric: Int) { CONSTANT(5), INTERMITTENT(2) }
enum class IntuitionLevel(val numeric: Int) { ELITE(5), STRONG(4), ANALYTICAL(3), BASIC(2), MARGINAL(1) }
enum class ConvergenceLevel(val numeric: Int) { FOCUSED(5), ALIGNED(4), DRIFTING(3), DISPERSED(2), ERRATIC(1) }
enum class SyncLevel(val numeric: Int) { OPTIMAL(5), STABLE(4), STANDARD(3), SUBOPTIMAL(2), CRITICAL(1) }

data class DiagnosticReport(
    val precision: PrecisionLevel,
    val velocity: VelocityLevel,
    val stability: StabilityLevel,
    val intuition: IntuitionLevel,
    val convergence: ConvergenceLevel,
    val syncLevel: SyncLevel = SyncLevel.STANDARD,
    val score: Int = 0
)

data class GameSession(
    val id: String,
    val timestamp: Long,
    val levels: List<LevelResult>,
    val totalScore: Int,
    val isWin: Boolean,
    val isHelperMode: Boolean = false,
    val logicalMistakes: Int = 0,
    val totalArchiveChecks: Int = 0,
    val totalDuplicateGuesses: Int = 0,
    val diagnosticReport: DiagnosticReport? = null
)

object GameResultStorage {
    // Current ongoing session data
    var currentSessionLevels = mutableListOf<LevelResult>()
    
    // Last completed session for Result screen
    var lastGameSession: GameSession? = null
}
