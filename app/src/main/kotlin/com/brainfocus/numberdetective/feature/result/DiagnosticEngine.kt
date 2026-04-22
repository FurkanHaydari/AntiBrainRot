package com.brainfocus.numberdetective.feature.result

import com.brainfocus.numberdetective.data.storage.*
import com.brainfocus.numberdetective.data.model.Hint
import com.brainfocus.numberdetective.R
import kotlin.math.max

/**
 * SİA Nöral Teşhis Motoru v2.0
 * 
 * Bu motor, dedektif performansını sadece "kazanma/kaybetme" bazlı değil, 
 * bilişsel verimlilik, zorluk çarpanı ve metodik daraltma hızı bazlı analiz eder.
 * 
 * ANALİZ POLİTİKASI (SİA PROTOKOLÜ):
 * 1. Zorluk Hiyerarşisi: 4 haneli seviyelerin (L3) stratejik ağırlığı, 3 hanelilere göre 1.9x daha fazladır.
 * 2. Normalizasyon: Her seviye için ayrı hamle/süre beklentisi (Baseline) tanımlanmıştır.
 * 3. Mikro-Verimlilik: Sadece sonuç değil, her tahmindeki verimlilik (Sarı/Yeşil oranı) punlanır.
 */
object DiagnosticEngine {
    
    // --- Baseline Ölçütleri ---
    // Her zorluk derecesi için "ideal" dedektiflik performansı eşikleri.
    private val TARGET_ATTEMPTS = listOf(4.0f, 5.0f, 8.0f) // L3 (4 hane) analiz yükü nedeniyle 8 hamle toleranslıdır.
    private val TARGET_SECONDS = listOf(30.0f, 60.0f, 100.0f) // Kritikal seviye hane hiyerarşisi gereği daha fazla süre ister.
    
    // -- L3 (Critical) seviyesi, stratejik çarpanıyla nihai senkronizasyon rütbesini domine eder. --
    private val LEVEL_WEIGHTS = listOf(1.0f, 1.3f, 1.9f)
    
    /**
     * Verimlilik (Sezgi/Yakınsama) indeksini hesaplayan merkezi motor fonksiyonu.
     * V2.3 Turbo: Katsayı 1.2f olarak sabitlendi.
     */
    fun calculateEfficiencyIndex(hints: List<com.brainfocus.numberdetective.data.model.Hint>, attempts: Int, levelNumber: Int): Float {
        val coins = calculateCoinsForLevel(hints, levelNumber)
        val digits = if (levelNumber == 3) 4 else 3
        return (coins.first.toFloat() / (max(1.0f, attempts.toFloat()) * digits.toFloat() * 1.2f))
            .coerceIn(0.1f, 1.2f)
    }
    
    /**
     * DiagnosticReport üzerinden nihai "Nöral Puan" (0-500) hesaplar.
     * V2.0: Puan toplama artık seviye rütbelerinin ağırlığına (numeric) dayanır.
     */
    fun calculateScoreFromReport(report: DiagnosticReport): Int {
        var score = 0
        
        // 1. Hassasiyet (Precision) - Max 100
        score += when (report.precision) {
            PrecisionLevel.ELITE -> 100
            PrecisionLevel.SHARP -> 75
            PrecisionLevel.STABLE -> 45
            PrecisionLevel.NOVICE -> 15
        }
        
        // 2. Hız (Velocity) - Max 100
        score += when (report.velocity) {
            VelocityLevel.FLASH -> 100
            VelocityLevel.RAPID -> 75
            VelocityLevel.STEADY -> 45
            VelocityLevel.DELIBERATE -> 15
        }
        
        // 3. Stabilite (Stability) - Max 100
        score += when (report.stability) {
            StabilityLevel.CONSTANT -> 100
            StabilityLevel.INTERMITTENT -> 40
        }
        
        // 4. Sezgi (Intuition) - Max 100
        score += when (report.intuition) {
            IntuitionLevel.ELITE -> 100
            IntuitionLevel.STRONG -> 80
            IntuitionLevel.ANALYTICAL -> 60
            IntuitionLevel.BASIC -> 40
            IntuitionLevel.MARGINAL -> 15
        }
        
        // 5. Yakınsama (Convergence) - Max 100
        score += when (report.convergence) {
            ConvergenceLevel.FOCUSED -> 100
            ConvergenceLevel.ALIGNED -> 80
            ConvergenceLevel.DRIFTING -> 50
            ConvergenceLevel.DISPERSED -> 30
            ConvergenceLevel.ERRATIC -> 10
        }

        return score
    }

    /**
     * Tüm oturumu analiz eden ana raporlama fonksiyonu.
     * V2.0: Ağırlıklı Bilişsel İndeksleme (Weighted Cognitive Indexing) protokolü uygulanır.
     */
    fun generateReport(session: GameSession): DiagnosticReport {
        if (session.levels.isEmpty()) return createEmptyReport()

        var weightedPrecisionSum = 0f
        var weightedVelocitySum = 0f
        var weightedEfficiencySum = 0f
        var totalWeights = 0f

        session.levels.forEach { level ->
            val weight = LEVEL_WEIGHTS.getOrElse(level.levelNumber - 1) { 1.0f }
            val targetAttempts = TARGET_ATTEMPTS.getOrElse(level.levelNumber - 1) { 5.0f }
            val targetSeconds = TARGET_SECONDS.getOrElse(level.levelNumber - 1) { 60.0f }
            
            // ANALİZ 1: Hassasiyet Normalizasyonu
            // "Zor seviyede yapılan 5 hamle, kolay seviyede yapılan 5 hamleden daha kıymetlidir."
            val actualAttempts = level.hints.count { !it.isSystemHint }.toFloat()
            val precisionIdx = (targetAttempts / max(1.0f, actualAttempts)).coerceIn(0.1f, 1.2f)
            
            // ANALİZ 2: Hız Verimliliği
            val velocityIdx = (targetSeconds / max(1.0f, level.durationSeconds.toFloat())).coerceIn(0.1f, 1.2f)
            
            // ANALİZ 3: Mikro-İsabet (Metodik Daraltma)
            // Sadece sonuç değil, hamle bașına düşen Sarı/Yeşil isabet yoğunluğu taranır.
            val playerHints = level.hints.filter { !it.isSystemHint }
            val digits = if (level.levelNumber == 3) 4 else 3
            val hitScore = playerHints.sumOf { (it.correct * 2) + it.misplaced }.toFloat()
            
            // Formül: Toplam İsabet / (Deneme Sayısı * Hane Sayısı * Siber-Sabit)
            // Bu indeks, "rastgele tahmin" ile "akıllı daraltma" arasındaki farkı yakalar.
            // V2.3 Turbo: Katsayı 1.5f -> 1.2f olarak esnetildi (Daha premium puan hissi).
            val efficiencyIdx = (hitScore / (max(1.0f, actualAttempts) * digits * 1.2f)).coerceIn(0.1f, 1.2f)

            weightedPrecisionSum += precisionIdx * weight
            weightedVelocitySum += velocityIdx * weight
            weightedEfficiencySum += efficiencyIdx * weight
            totalWeights += weight
        }

        // Ağırlıklı Ortalamaların konsolidasyonu
        val finalPrecisionIdx = weightedPrecisionSum / totalWeights
        val finalVelocityIdx = weightedVelocitySum / totalWeights
        val finalEfficiencyIdx = weightedEfficiencySum / totalWeights

        return generateReportFromIndices(
            precisionIdx = finalPrecisionIdx,
            velocityIdx = finalVelocityIdx,
            efficiencyIdx = finalEfficiencyIdx,
            session = session
        )
    }

    /**
     * Nöral İndeksleri (0.0 - 1.2) DiagnosticReport kategorilerine map eder.
     */
    private fun generateReportFromIndices(
        precisionIdx: Float,
        velocityIdx: Float,
        efficiencyIdx: Float,
        session: GameSession
    ): DiagnosticReport {
        
        // 1. DEDUCTIVE PRECISION (Tümdengelimsel Hassasiyet)
        // Logaritmik Eşikler: 1.0 üzeri oranlar "Elite" dedektif sınıfını temsil eder.
        val precision = when {
            !session.isWin -> PrecisionLevel.NOVICE
            precisionIdx >= 1.0f -> PrecisionLevel.ELITE
            precisionIdx >= 0.7f -> PrecisionLevel.SHARP
            precisionIdx >= 0.4f -> PrecisionLevel.STABLE
            else -> PrecisionLevel.NOVICE
        }

        // 2. SYNAPTIC VELOCITY (Sinaptik Hız)
        val velocity = when {
            velocityIdx >= 1.0f -> VelocityLevel.FLASH
            velocityIdx >= 0.7f -> VelocityLevel.RAPID
            velocityIdx >= 0.4f -> VelocityLevel.STEADY
            else -> VelocityLevel.DELIBERATE
        }

        // 3. LOGICAL CONVERGENCE (Yakınsama) & INTUITION
        // EfficiencyIdx; ipuçlarından veri süzme ("Data Filtering") hızını temsil eder.
        // V2.3 Turbo: Eşikler 0.7f -> 0.65f olarak esnetildi (5. diş ELITE/FOCUSED rütbesi için).
        val convergence = when {
            efficiencyIdx >= 0.65f -> ConvergenceLevel.FOCUSED
            efficiencyIdx >= 0.5f -> ConvergenceLevel.ALIGNED
            efficiencyIdx >= 0.3f -> ConvergenceLevel.DRIFTING
            else -> ConvergenceLevel.ERRATIC
        }

        val intuition = when {
            efficiencyIdx >= 0.65f -> IntuitionLevel.ELITE
            efficiencyIdx >= 0.5f -> IntuitionLevel.STRONG
            efficiencyIdx >= 0.3f -> IntuitionLevel.ANALYTICAL
            else -> IntuitionLevel.MARGINAL
        }

        // 4. NEURAL STABILITY (Nöral Stabilite)
        // Hafıza hataları (Duplicate Guess) ve Arşiv bağımlılığı analizi.
        val levelCount = session.levels.size
        val isMemoryStable = session.totalDuplicateGuesses == 0
        val isArchiveStable = session.totalArchiveChecks <= (levelCount * 2)
        
        val stability = if (session.isWin && isMemoryStable && isArchiveStable && session.logicalMistakes <= 2) {
            StabilityLevel.CONSTANT
        } else {
            StabilityLevel.INTERMITTENT
        }

        // V2.2 Granüler Puanlama: Basamaklı (Enum) yapıdan dinamik indeks yapısına geçiş.
        // Bu formül, 460-480 gibi quantized değerler yerine 463, 478 gibi granüler sonuçlar üretir.
        val rawPrecisionScore = precisionIdx * 100f
        val rawVelocityScore = velocityIdx * 100f
        val rawEfficiencyScore = efficiencyIdx * 200f // Intuition + Convergence ağırlığı
        val rawStabilityScore = if (stability == StabilityLevel.CONSTANT) 100f else 40f
        
        val weight = LEVEL_WEIGHTS.getOrElse((session.levels.firstOrNull()?.levelNumber ?: 1) - 1) { 1.0f }
        
        val totalScore = ((rawPrecisionScore + rawVelocityScore + rawEfficiencyScore + rawStabilityScore) * weight)
            .toInt().coerceIn(0, 500 * 2) // Çarpanla birlikte 500 üstüne çıkabilir (Zorluk ödülü)
            
        val syncLevel = when {
            totalScore >= 420 -> SyncLevel.OPTIMAL
            totalScore >= 320 -> SyncLevel.STABLE
            totalScore >= 200 -> SyncLevel.STANDARD
            totalScore >= 100 -> SyncLevel.SUBOPTIMAL
            else -> SyncLevel.CRITICAL
        }

        val finalReport = DiagnosticReport(
            precision = precision,
            velocity = velocity,
            stability = stability,
            intuition = intuition,
            convergence = convergence,
            syncLevel = syncLevel,
            score = totalScore
        )

        return finalReport
    }

    private fun createEmptyReport() = DiagnosticReport(
        precision = PrecisionLevel.NOVICE,
        velocity = VelocityLevel.DELIBERATE,
        stability = StabilityLevel.INTERMITTENT,
        intuition = IntuitionLevel.MARGINAL,
        convergence = ConvergenceLevel.ERRATIC
    )

    // Geriye dönük uyumluluk (Legacy Support) için basitleştirilmiş mapping metodu.
    fun generateReport(
        isWin: Boolean, 
        totalAttempts: Int, 
        totalTimeSeconds: Int, 
        totalHintsFound: Int = -1,
        isHelperModeEnabled: Boolean = false,
        logicalMistakes: Int = 0,
        totalArchiveChecks: Int = 0,
        totalDuplicateGuesses: Int = 0,
        totalCoins: Int = -1,
        totalMaxCoins: Int = -1,
        levelsPlayed: Int = 1,
        levelNumber: Int = 1,
        efficiencyIdx: Float = 0.5f
    ): DiagnosticReport {
        val mockSession = GameSession(
            id = "legacy_mock",
            timestamp = 0L,
            levels = listOf(LevelResult(levelNumber, "", emptyList(), totalTimeSeconds, 0)),
            totalScore = 0,
            isWin = isWin,
            logicalMistakes = logicalMistakes,
            totalArchiveChecks = totalArchiveChecks,
            totalDuplicateGuesses = totalDuplicateGuesses
        )
        
        // V2.3 Turbo: Legacy çağrıda veri sızıntısını önlemek için final indeksleri doğrudan map ediyoruz.
        // Bu sayede mock seans içindeki boş ipuçları (empty hints) verimliliği sıfıramaz.
        val targetAttempts = TARGET_ATTEMPTS.getOrElse(levelNumber - 1) { 5.0f }
        val targetSeconds = TARGET_SECONDS.getOrElse(levelNumber - 1) { 60.0f }

        val precisionIdx = (targetAttempts / max(1.0f, totalAttempts.toFloat())).coerceIn(0.1f, 1.2f)
        val velocityIdx = (targetSeconds / max(1.0f, totalTimeSeconds.toFloat())).coerceIn(0.1f, 1.2f)

        return generateReportFromIndices(
            precisionIdx = precisionIdx,
            velocityIdx = velocityIdx,
            efficiencyIdx = efficiencyIdx, // Parametre olarak gelen gerçek verimliliği kullan
            session = mockSession
        )
    }

    fun calculateSessionMetrics(session: GameSession): Pair<Int, Int> {
        val totalArchiveChecks = session.levels.sumOf { it.archiveChecks }
        val totalDuplicateGuesses = session.levels.sumOf { it.duplicateGuesses }
        return totalArchiveChecks to totalDuplicateGuesses
    }

    fun calculateCoinsForLevel(hints: List<Hint>, levelNumber: Int): Pair<Int, Int> {
        val maxPerHint = if (levelNumber == 3) 8 else 6
        val coinsEarned = hints.sumOf { (it.correct * 2) + it.misplaced }
        val maxPossible = hints.size * maxPerHint
        return coinsEarned to maxPossible
    }
}
