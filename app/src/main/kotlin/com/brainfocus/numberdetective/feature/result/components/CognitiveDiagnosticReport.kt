package com.brainfocus.numberdetective.feature.result.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
            Triple(R.string.eval_precision_label, getPrecisionText(report.precision), report.precision.numeric),
            Triple(R.string.eval_velocity_label, getVelocityText(report.velocity), report.velocity.numeric),
            Triple(R.string.eval_stability_label, getStabilityText(report.stability), report.stability.numeric),
            Triple(R.string.eval_intuition_label, getIntuitionText(report.intuition), report.intuition.numeric),
            Triple(R.string.eval_convergence_label, getConvergenceText(report.convergence), report.convergence.numeric),
            Triple(
                R.string.eval_conclusion_label, 
                if (isWin) R.string.sia_evaluation_win else R.string.sia_evaluation_loss,
                if (isWin) 5 else 2
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
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.eval_header),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = (10 * scaleFactor).sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (2 * scaleFactor).sp
            ),
            color = PrimaryCyan.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp * scaleFactor))
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp * scaleFactor)
        ) {
            reportLines.forEachIndexed { index, (labelRes, valueRes, powerLevel) ->
                AnimatedVisibility(
                    visible = visibleLinesCount > index,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { 20 },
                    exit = fadeOut()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(labelRes).uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = (8 * scaleFactor).sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = PrimaryCyan.copy(alpha = 0.4f)
                            )
                            
                            // Neural Power Bar (Staggered or immediate)
                            NeuralPowerBar(
                                powerLevel = powerLevel, 
                                scaleFactor = scaleFactor, 
                                isWin = isWin && index == reportLines.size - 1,
                                isConvergence = index == 4 // Convergence is the 5th line
                            )
                        }
                        
                        Text(
                            text = stringResource(valueRes),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontSize = (11 * scaleFactor).coerceAtMost(14f).sp,
                                fontWeight = if (index == reportLines.size - 1) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (index == reportLines.size - 1) 
                                (if (isWin) SuccessGreen else ErrorRed) 
                                else TextSecondary.copy(alpha = 0.8f),
                            textAlign = TextAlign.Start
                        )
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

private fun getConvergenceText(level: ConvergenceLevel) = when(level) {
    ConvergenceLevel.FOCUSED -> R.string.eval_convergence_focused
    ConvergenceLevel.ALIGNED -> R.string.eval_convergence_aligned
    ConvergenceLevel.DRIFTING -> R.string.eval_convergence_drifting
    ConvergenceLevel.DISPERSED -> R.string.eval_convergence_dispersed
    ConvergenceLevel.ERRATIC -> R.string.eval_convergence_erratic
}

private fun getPrecisionText(level: PrecisionLevel) = when(level) {
    PrecisionLevel.ELITE -> R.string.eval_precision_elite
    PrecisionLevel.SHARP -> R.string.eval_precision_sharp
    PrecisionLevel.STABLE -> R.string.eval_precision_standard
    PrecisionLevel.NOVICE -> R.string.eval_precision_novice
}

private fun getVelocityText(level: VelocityLevel) = when(level) {
    VelocityLevel.FLASH -> R.string.eval_velocity_flash
    VelocityLevel.RAPID -> R.string.eval_velocity_rapid
    VelocityLevel.STEADY -> R.string.eval_velocity_steady
    VelocityLevel.DELIBERATE -> R.string.eval_velocity_deliberate
}

private fun getStabilityText(level: StabilityLevel) = when(level) {
    StabilityLevel.CONSTANT -> R.string.eval_stability_constant
    StabilityLevel.INTERMITTENT -> R.string.eval_stability_intermittent
}

private fun getIntuitionText(level: IntuitionLevel) = when(level) {
    IntuitionLevel.STRONG -> R.string.eval_intuition_strong
    IntuitionLevel.ANALYTICAL -> R.string.eval_intuition_analytical
    IntuitionLevel.MARGINAL -> R.string.eval_intuition_marginal
}
