package com.brainfocus.numberdetective.feature.result.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.core.designsystem.*
import com.brainfocus.numberdetective.data.storage.*
import kotlinx.coroutines.delay

@Composable
fun CognitiveDiagnosticReport(
    report: DiagnosticReport,
    isWin: Boolean,
    scaleFactor: Float,
    modifier: Modifier = Modifier,
    staggered: Boolean = true
) {
    val reportLines = remember(report) {
        listOf(
            Triple(R.string.eval_precision_label, getPrecisionText(report.precision), report.precision?.numeric ?: 3),
            Triple(R.string.eval_velocity_label, getVelocityText(report.velocity), report.velocity?.numeric ?: 3),
            Triple(R.string.eval_stability_label, getStabilityText(report.stability), report.stability?.numeric ?: 3),
            Triple(R.string.eval_intuition_label, getIntuitionText(report.intuition), report.intuition?.numeric ?: 3),
            Triple(R.string.eval_convergence_label, getConvergenceText(report.convergence), report.convergence?.numeric ?: 3),
            Triple(
                R.string.eval_conclusion_label, 
                getSyncLevelText(report.syncLevel),
                (report.syncLevel ?: SyncLevel.STANDARD).numeric
            )
        )
    }
    
    var visibleLinesCount by remember { mutableIntStateOf(if (staggered) 0 else reportLines.size) }

    if (staggered) {
        LaunchedEffect(report) {
            visibleLinesCount = 0
            reportLines.forEachIndexed { index, _ ->
                delay(if (index == 0) 500 else 600)
                visibleLinesCount = index + 1
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.eval_header),
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = Montserrat,
                fontSize = (12 * scaleFactor).sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (3 * scaleFactor).sp
            ),
            color = PrimaryCyan.copy(alpha = 0.8f)
        )
        
        Spacer(modifier = Modifier.height(8.dp * scaleFactor))
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp * scaleFactor)
        ) {
            reportLines.forEachIndexed { index, (labelRes, valueRes, powerLevel) ->
                val isConclusion = index == reportLines.size - 1
                
                AnimatedVisibility(
                    visible = visibleLinesCount > index,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { 20 },
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = if (isConclusion) 12.dp * scaleFactor else 0.dp),
                        horizontalAlignment = if (isConclusion) Alignment.CenterHorizontally else Alignment.Start
                    ) {
                        if (!isConclusion) {
                            Text(
                                text = stringResource(labelRes).uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = Montserrat,
                                    fontSize = (11 * scaleFactor).sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (1.5 * scaleFactor).sp
                                ),
                                color = PrimaryCyan.copy(alpha = 0.7f)
                            )
                            
                            Spacer(modifier = Modifier.height(6.dp * scaleFactor))
                            
                            val fullText = stringResource(valueRes)
                            val mainValue = fullText.substringBefore(" (")
                            val subInfo = if (fullText.contains(" (")) "(" + fullText.substringAfter(" (") else ""
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mainValue.uppercase(),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = Montserrat,
                                            fontSize = (16 * scaleFactor).sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = (0.5 * scaleFactor).sp,
                                            shadow = androidx.compose.ui.graphics.Shadow(
                                                color = PrimaryCyan.copy(alpha = 0.3f),
                                                blurRadius = (8 * scaleFactor)
                                            )
                                        ),
                                        color = Color.White.copy(alpha = 0.95f)
                                    )
                                    
                                    if (subInfo.isNotEmpty()) {
                                        Text(
                                            text = subInfo.uppercase(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = Montserrat,
                                                fontSize = (10 * scaleFactor).sp,
                                                fontWeight = FontWeight.Medium,
                                                letterSpacing = (0.5 * scaleFactor).sp
                                            ),
                                            color = TextSecondary.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                                
                                NeuralPowerBar(
                                    powerLevel = powerLevel, 
                                    scaleFactor = scaleFactor * 1.3f,
                                    isWin = isWin,
                                    isConvergence = index == 4
                                )
                            }
                        } else {
                             Text(
                                text = stringResource(labelRes).uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = Montserrat,
                                    fontSize = (13 * scaleFactor).sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (3 * scaleFactor).sp
                                ),
                                color = getSyncLevelColor(report.syncLevel, isWin).copy(alpha = 0.7f),
                                modifier = Modifier.padding(bottom = 4.dp * scaleFactor)
                            )
                            
                            Text(
                                text = stringResource(valueRes),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = Montserrat,
                                    fontSize = (22 * scaleFactor).coerceAtMost(34f).sp,
                                    fontWeight = FontWeight.Black,
                                    lineHeight = (30 * scaleFactor).sp,
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = getSyncLevelColor(report.syncLevel, isWin).copy(alpha = 0.6f),
                                        blurRadius = (16 * scaleFactor)
                                    )
                                ),
                                color = getSyncLevelColor(report.syncLevel, isWin),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NeuralPowerBar(powerLevel: Int, scaleFactor: Float, isWin: Boolean, isConvergence: Boolean = false) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp * scaleFactor),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { i ->
            val isActive = i < powerLevel
            Box(
                modifier = Modifier
                    .size(width = 8.dp * scaleFactor, height = 3.dp * scaleFactor)
                    .background(
                        color = when {
                            !isActive -> Color.White.copy(alpha = 0.05f)
                            isConvergence -> when {
                                powerLevel <= 1 -> HeatMap0.copy(alpha = 0.8f)
                                powerLevel <= 2 -> HeatMap1_2.copy(alpha = 0.8f)
                                powerLevel <= 3 -> HeatMap3_4.copy(alpha = 0.8f)
                                powerLevel <= 4 -> HeatMap5.copy(alpha = 0.8f)
                                else -> HeatMap6.copy(alpha = 0.8f)
                            }
                            isWin -> SuccessGreen.copy(alpha = 0.8f)
                            powerLevel <= 2 -> ErrorRed.copy(alpha = 0.8f)
                            else -> PrimaryCyan.copy(alpha = 0.8f)
                        },
                        shape = RoundedCornerShape(1.dp * scaleFactor)
                    )
            )
        }
    }
}

private fun getConvergenceText(level: ConvergenceLevel?) = when(level ?: ConvergenceLevel.DRIFTING) {
    ConvergenceLevel.FOCUSED -> R.string.eval_convergence_focused
    ConvergenceLevel.ALIGNED -> R.string.eval_convergence_aligned
    ConvergenceLevel.DRIFTING -> R.string.eval_convergence_drifting
    ConvergenceLevel.DISPERSED -> R.string.eval_convergence_dispersed
    ConvergenceLevel.ERRATIC -> R.string.eval_convergence_erratic
}

private fun getPrecisionText(level: PrecisionLevel?) = when(level ?: PrecisionLevel.STABLE) {
    PrecisionLevel.ELITE -> R.string.eval_precision_elite
    PrecisionLevel.SHARP -> R.string.eval_precision_sharp
    PrecisionLevel.STABLE -> R.string.eval_precision_standard
    PrecisionLevel.NOVICE -> R.string.eval_precision_novice
}

private fun getVelocityText(level: VelocityLevel?) = when(level ?: VelocityLevel.STEADY) {
    VelocityLevel.FLASH -> R.string.eval_velocity_flash
    VelocityLevel.RAPID -> R.string.eval_velocity_rapid
    VelocityLevel.STEADY -> R.string.eval_velocity_steady
    VelocityLevel.DELIBERATE -> R.string.eval_velocity_deliberate
}

private fun getStabilityText(level: StabilityLevel?) = when(level ?: StabilityLevel.INTERMITTENT) {
    StabilityLevel.CONSTANT -> R.string.eval_stability_constant
    StabilityLevel.INTERMITTENT -> R.string.eval_stability_intermittent
}

private fun getIntuitionText(level: IntuitionLevel?) = when(level ?: IntuitionLevel.ANALYTICAL) {
    IntuitionLevel.ELITE -> R.string.eval_intuition_elite
    IntuitionLevel.STRONG -> R.string.eval_intuition_strong
    IntuitionLevel.ANALYTICAL -> R.string.eval_intuition_analytical
    IntuitionLevel.BASIC -> R.string.eval_intuition_basic
    IntuitionLevel.MARGINAL -> R.string.eval_intuition_marginal
}

private fun getSyncLevelText(level: SyncLevel?) = when(level ?: SyncLevel.STANDARD) {
    SyncLevel.OPTIMAL -> R.string.sia_sync_optimal
    SyncLevel.STABLE -> R.string.sia_sync_stable
    SyncLevel.STANDARD -> R.string.sia_sync_standard
    SyncLevel.SUBOPTIMAL -> R.string.sia_sync_suboptimal
    SyncLevel.CRITICAL -> R.string.sia_sync_critical
}

private fun getSyncLevelColor(level: SyncLevel?, isWin: Boolean): Color = when(level ?: SyncLevel.STANDARD) {
    SyncLevel.OPTIMAL -> SuccessGreen
    SyncLevel.STABLE -> PrimaryCyan
    SyncLevel.STANDARD -> SecondaryBlue
    SyncLevel.SUBOPTIMAL -> if (isWin) WarningYellow else ErrorRed
    SyncLevel.CRITICAL -> ErrorRed
}
