package com.brainfocus.numberdetective.core.designsystem

import java.util.Locale

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.data.model.FieldReport

@Composable
fun FieldReportOverlay(
    report: FieldReport, 
    scaleFactor: Float,
    maxWidth: androidx.compose.ui.unit.Dp,
    onDismiss: () -> Unit,
    onExit: () -> Unit,
    remainingTime: Int
) {
    val isPauseReport = report is FieldReport.Pause
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = (400.dp * scaleFactor).coerceAtMost(maxWidth))
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            color = SurfaceCard,
            shape = RoundedCornerShape(28.dp * scaleFactor),
            border = RowDefaults.CardBorder
        ) {
            Column(
                modifier = Modifier.padding(24.dp * scaleFactor),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp * scaleFactor)
                        .background(
                            when {
                                isPauseReport -> PrimaryCyan.copy(alpha = 0.1f)
                                report.isPositive -> SuccessGreen.copy(alpha = 0.1f)
                                else -> ErrorRed.copy(alpha = 0.1f)
                            },
                            CircleShape
                        )
                        .border(
                            1.dp, 
                            when {
                                isPauseReport -> PrimaryCyan
                                report.isPositive -> SuccessGreen
                                else -> ErrorRed
                            }, 
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPauseReport) "⏸️" else if (report.isPositive) "🎖️" else "⚠️", 
                        fontSize = (32 * scaleFactor).coerceAtMost(48f).sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp * scaleFactor))

                Text(
                    text = stringResource(report.titleRes).uppercase(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = Montserrat, 
                        fontWeight = FontWeight.Bold, 
                        letterSpacing = (2 * scaleFactor).sp,
                        fontSize = (20 * scaleFactor).coerceAtMost(32f).sp
                    ),
                    color = if (isPauseReport) PrimaryCyan else if (report.isPositive) PrimaryCyan else ErrorRed,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp * scaleFactor))

                Text(
                    text = if (report.pluralRes != 0) {
                        pluralStringResource(report.pluralRes, report.quantity, *report.messageArgs.toTypedArray())
                    } else {
                        stringResource(report.messageRes, *report.messageArgs.toTypedArray())
                    },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = (16 * scaleFactor).coerceAtMost(24f).sp,
                        lineHeight = (22 * scaleFactor).coerceAtMost(32f).sp
                    ),
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                if (isPauseReport) {
                    Spacer(modifier = Modifier.height(16.dp * scaleFactor))
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp * scaleFactor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp * scaleFactor),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.label_time).uppercase(),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = (12 * scaleFactor).coerceAtMost(18f).sp
                                ),
                                color = TextSecondary
                            )
                            val locale = LocalConfiguration.current.locales[0]
                            Text(
                                text = "%02d:%02d".format(locale, remainingTime / 60, remainingTime % 60),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = (16 * scaleFactor).coerceAtMost(24f).sp
                                ),
                                color = PrimaryCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp * scaleFactor))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp * scaleFactor)) {
                    DetectiveButton(
                        text = stringResource(if (isPauseReport) R.string.resume_mission else R.string.continue_mission),
                        onClick = onDismiss,
                        scaleFactor = scaleFactor
                    )

                    if (isPauseReport) {
                        OutlinedButton(
                            onClick = onExit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp * scaleFactor),
                            shape = RoundedCornerShape(16.dp * scaleFactor),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color.White.copy(alpha = 0.1f)
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.exit_mission),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = Montserrat,
                                    letterSpacing = (1 * scaleFactor).sp,
                                    fontSize = (16 * scaleFactor).coerceAtMost(22f).sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
