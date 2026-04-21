package com.brainfocus.numberdetective.feature.result

import com.brainfocus.numberdetective.data.storage.*

object DiagnosticEngine {
    fun generateReport(session: GameSession): DiagnosticReport {
        val totalAttempts = session.levels.sumOf { it.hints.size }
        val totalTime = session.levels.sumOf { it.durationSeconds }
        
        var totalCorrectPos = 0
        var totalMisplaced = 0
        var totalCoins = 0
        var totalMaxCoins = 0
        
        session.levels.forEach { level ->
            val maxPerHint = if (level.levelNumber == 3) 8 else 6
            level.hints.forEach { hint ->
                totalCorrectPos += hint.correct
                totalMisplaced += hint.misplaced
                totalCoins += (hint.correct * 2) + hint.misplaced
                totalMaxCoins += maxPerHint
            }
        }
        
        return generateReport(
            isWin = session.isWin,
            totalAttempts = totalAttempts,
            totalTimeSeconds = totalTime,
            totalHintsFound = totalCorrectPos + totalMisplaced,
            isHelperModeEnabled = session.isHelperMode,
            logicalMistakes = session.logicalMistakes,
            totalCoins = totalCoins,
            totalMaxCoins = totalMaxCoins
        )
    }

    fun generateReport(
        isWin: Boolean, 
        totalAttempts: Int, 
        totalTimeSeconds: Int, 
        totalHintsFound: Int = -1,
        isHelperModeEnabled: Boolean = false,
        logicalMistakes: Int = 0,
        totalCoins: Int = -1,
        totalMaxCoins: Int = -1
    ): DiagnosticReport {
        // Zero-effort case detection: Kullanıcı hiçbir deneme yapmadan oyun biterse düşük profil döndürülür.
        if (totalAttempts == 0) {
            return DiagnosticReport(
                precision = PrecisionLevel.NOVICE,
                velocity = VelocityLevel.DELIBERATE,
                stability = StabilityLevel.INTERMITTENT,
                intuition = IntuitionLevel.MARGINAL,
                convergence = ConvergenceLevel.ERRATIC
            )
        }

        val levelCount = if (isWin) 3 else 1
        val avgAttempts = totalAttempts.toFloat() / levelCount
        val avgSeconds = totalTimeSeconds.toFloat() / levelCount
        
        /**
         * 1. DEDUCTIVE PRECISION (Tümdengelimsel Hassasiyet)
         * Bu metrik, her bölüm için harcanan ortalama deneme sayısına dayalıdır.
         * Mantıksal hatalar (Logical Mistakes) bu skoru sert bir şekilde cezalandırır.
         * Elite: Ortalama 4 deneme altı. Sharp: 7 altı. Stable: 10 altı.
         */
        val mistakePenalty = if (isHelperModeEnabled) 1.5f else 1.0f
        val adjustedAvgAttempts = avgAttempts + (logicalMistakes * mistakePenalty)
        
        val precision = when {
            !isWin && (avgAttempts < 5f || totalHintsFound == 0) -> PrecisionLevel.NOVICE
            adjustedAvgAttempts <= 4.0f && isWin -> PrecisionLevel.ELITE
            adjustedAvgAttempts <= 7.0f -> if (isWin) PrecisionLevel.SHARP else PrecisionLevel.STABLE
            adjustedAvgAttempts <= 10.0f -> PrecisionLevel.STABLE
            else -> PrecisionLevel.NOVICE
        }

        /**
         * 2. SYNAPTIC VELOCITY (Sinaptik Hız)
         * Kullanıcının her bölüm için harcadığı ortalama saniyeye göre hesaplanır.
         * Flash: 20sn altı. Rapid: 40sn altı. Steady: 60sn altı.
         */
        val velocity = when {
            !isWin && totalTimeSeconds < 20 -> VelocityLevel.DELIBERATE
            avgSeconds <= 20f -> VelocityLevel.FLASH
            avgSeconds <= 40f -> VelocityLevel.RAPID
            avgSeconds <= 60f -> VelocityLevel.STEADY
            else -> VelocityLevel.DELIBERATE
        }

        /**
         * 4. LOGICAL CONVERGENCE (Mantıksal Yakınsama)
         * Isı haritası (Heat Map) verimliliğine dayalıdır. Toplam kazanılan coin miktarının,
         * o ana kadar yapılabilecek maksimum coin miktarına oranıdır.
         * Hedefe ne kadar doğrudan/dolambaçsız ulaşıldığını ölçer.
         */
        val efficiency = if (totalMaxCoins > 0 && totalCoins != -1) {
            totalCoins.toFloat() / totalMaxCoins
        } else if (totalHintsFound != -1) {
            // Fallback estimation if raw coins are missing
            (totalHintsFound.toFloat() / (totalAttempts * 1.5f)).coerceIn(0f, 1f)
        } else 0f

        val convergence = when {
            efficiency >= 0.8f -> ConvergenceLevel.FOCUSED
            efficiency >= 0.6f -> ConvergenceLevel.ALIGNED
            efficiency >= 0.4f -> ConvergenceLevel.DRIFTING
            efficiency >= 0.2f -> ConvergenceLevel.DISPERSED
            else -> ConvergenceLevel.ERRATIC
        }

        /**
         * 3. NUMERICAL INTUITION (Sayısal Sezgi)
         * Kullanıcının her denemesinde ne kadar "isabetli" veri topladığını (Coin/Efficiency) ölçer.
         * Sadece bulmaya değil, en az denemede en yüksek isabetle (yeşil/sarı) gitmeye bakar.
         * Verimlilik sadece ipucu sayısıyla değil, tahminin doğruluğuyla (coin sistemi) ölçülmektedir.
         */
        var intuition = when {
            efficiency >= 0.8f -> IntuitionLevel.ELITE
            efficiency >= 0.6f -> IntuitionLevel.STRONG
            efficiency >= 0.4f -> IntuitionLevel.ANALYTICAL
            efficiency >= 0.2f -> IntuitionLevel.BASIC
            else -> IntuitionLevel.MARGINAL
        }
        
        // Yardımcı mod kullanımı sezgisel skoru bir nebze kısıtlar.
        if (isHelperModeEnabled && intuition == IntuitionLevel.ELITE) {
            intuition = IntuitionLevel.STRONG
        }

        return DiagnosticReport(
            /**
             * 5. NEURAL STABILITY (Nöral Stabilite)
             * Oyunun geneline yayılan tutarlılığı ölçer. Mantıksal hataların azlığı,
             * ortalama deneme sayısının düşüklüğü ve oyunun kazanılmış olması sabitlik belirtisidir.
             */
            precision = precision,
            velocity = velocity,
            stability = if (isWin && avgAttempts < 8 && logicalMistakes <= 2) StabilityLevel.CONSTANT else StabilityLevel.INTERMITTENT,
            intuition = intuition,
            convergence = convergence
        )
    }
}
