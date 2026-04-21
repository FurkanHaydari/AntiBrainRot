package com.brainfocus.numberdetective.feature.result

import com.brainfocus.numberdetective.data.storage.*

object DiagnosticEngine {
    fun generateReport(session: GameSession): DiagnosticReport {
        val totalAttempts = session.levels.sumOf { it.hints.size }
        val totalTime = session.levels.sumOf { it.durationSeconds }
        
        var totalCorrectPos = 0
        var totalMisplaced = 0
        
        session.levels.forEach { level ->
            level.hints.forEach { hint ->
                totalCorrectPos += hint.correct
                totalMisplaced += hint.misplaced
            }
        }
        
        return generateReport(
            isWin = session.isWin,
            totalAttempts = totalAttempts,
            totalTimeSeconds = totalTime,
            totalHintsFound = totalCorrectPos + totalMisplaced,
            isHelperModeEnabled = session.isHelperMode,
            logicalMistakes = session.logicalMistakes
        )
    }

    fun generateReport(
        isWin: Boolean, 
        totalAttempts: Int, 
        totalTimeSeconds: Int, 
        totalHintsFound: Int = -1,
        isHelperModeEnabled: Boolean = false,
        logicalMistakes: Int = 0
    ): DiagnosticReport {
        // Zero-effort case detection
        if (totalAttempts == 0) {
            return DiagnosticReport(
                precision = PrecisionLevel.NOVICE,
                velocity = VelocityLevel.DELIBERATE,
                stability = StabilityLevel.INTERMITTENT,
                intuition = IntuitionLevel.MARGINAL
            )
        }

        val levelCount = if (isWin) 3 else 1
        val avgAttempts = totalAttempts.toFloat() / levelCount
        val avgSeconds = totalTimeSeconds.toFloat() / levelCount
        
        // --- 1. Precision Logic (Penalized by mistakes) ---
        val mistakePenalty = if (isHelperModeEnabled) 1.5f else 1.0f
        val adjustedAvgAttempts = avgAttempts + (logicalMistakes * mistakePenalty)
        
        val precision = when {
            !isWin && (avgAttempts < 5f || totalHintsFound == 0) -> PrecisionLevel.NOVICE
            adjustedAvgAttempts <= 4.0f && isWin -> PrecisionLevel.ELITE
            adjustedAvgAttempts <= 7.0f -> if (isWin) PrecisionLevel.SHARP else PrecisionLevel.STABLE
            adjustedAvgAttempts <= 10.0f -> PrecisionLevel.STABLE
            else -> PrecisionLevel.NOVICE
        }

        // --- 2. Velocity Logic ---
        val velocity = when {
            !isWin && totalTimeSeconds < 20 -> VelocityLevel.DELIBERATE
            avgSeconds <= 20f -> VelocityLevel.FLASH
            avgSeconds <= 40f -> VelocityLevel.RAPID
            avgSeconds <= 60f -> VelocityLevel.STEADY
            else -> VelocityLevel.DELIBERATE
        }

        // --- 3. Intuition Logic (Capped by Helper Mode) ---
        var intuition = when {
            totalHintsFound == 0 -> IntuitionLevel.MARGINAL
            totalHintsFound != -1 && (totalHintsFound.toFloat() / totalAttempts) > 1.2f -> IntuitionLevel.STRONG
            totalHintsFound != -1 && (totalHintsFound.toFloat() / totalAttempts) > 0.5f -> IntuitionLevel.ANALYTICAL
            else -> IntuitionLevel.MARGINAL
        }
        
        // If helper mode is ON, max intuition is capped at Analytical (4 bars) 
        // because the user is being given visual hints that reduce pure intuition effort.
        if (isHelperModeEnabled && intuition == IntuitionLevel.STRONG) {
            intuition = IntuitionLevel.ANALYTICAL
        }

        return DiagnosticReport(
            precision = precision,
            velocity = velocity,
            stability = if (isWin && avgAttempts < 8 && logicalMistakes <= 2) StabilityLevel.CONSTANT else StabilityLevel.INTERMITTENT,
            intuition = intuition
        )
    }
}
