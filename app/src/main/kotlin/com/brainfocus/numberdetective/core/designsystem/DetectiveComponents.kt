package com.brainfocus.numberdetective.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.data.model.DigitStatus
import com.brainfocus.numberdetective.data.model.Hint
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info

// --- ACCESSORIES ---

@Composable
fun PaperClip(scaleFactor: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 12.dp * scaleFactor, height = 32.dp * scaleFactor)
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(percent = 50))
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(percent = 50))
    )
}

@Composable
fun MissionStamp(text: String, color: Color, scaleFactor: Float) {
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(4.dp * scaleFactor),
        border = androidx.compose.foundation.BorderStroke(2.dp * scaleFactor, color.copy(alpha = 0.6f)),
        modifier = Modifier
            .rotate(-15f)
            .padding(8.dp * scaleFactor)
    ) {
        Text(
            text = text.uppercase(),
            modifier = Modifier.padding(horizontal = 12.dp * scaleFactor, vertical = 4.dp * scaleFactor),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = (22 * scaleFactor).coerceAtMost(32f).sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = (2 * scaleFactor).sp
            ),
            color = color.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun LevelBadge(levelNumber: Int, scaleFactor: Float) {
    Box(
        modifier = Modifier
            .size(24.dp * scaleFactor)
            .background(PrimaryCyan.copy(alpha = 0.1f), CircleShape)
            .border(1.dp, PrimaryCyan.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = levelNumber.toString(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = (10 * scaleFactor).coerceAtMost(16f).sp,
                fontWeight = FontWeight.Bold
            ),
            color = PrimaryCyan
        )
    }
}

// --- HEADERS ---

@Composable
fun DetectiveHeader(
    title: String,
    subtitle: String? = null,
    rightContent: @Composable (BoxScope.() -> Unit)? = null,
    scaleFactor: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp * scaleFactor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (2 * scaleFactor).sp,
                        fontSize = (14 * scaleFactor).coerceAtMost(22f).sp
                    ),
                    color = PrimaryCyan
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = (11 * scaleFactor).coerceAtMost(16f).sp
                        ),
                        color = SuccessGreen.copy(alpha = 0.8f),
                        letterSpacing = (1 * scaleFactor).sp
                    )
                }
            }
            rightContent?.let {
                Box(content = it)
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp * scaleFactor),
            color = Color.White.copy(alpha = 0.1f),
            thickness = 1.dp
        )
    }
}

// --- CARDS ---

@Composable
fun DetectiveHintCard(
    hint: Hint,
    isHelperModeEnabled: Boolean,
    scaleFactor: Float,
    label: String,
    labelColor: Color = PrimaryCyan.copy(alpha = 0.7f),
    maxWidth: androidx.compose.ui.unit.Dp = androidx.compose.ui.unit.Dp.Unspecified
) {
    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(16.dp * scaleFactor),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier
            .then(if (maxWidth != androidx.compose.ui.unit.Dp.Unspecified) Modifier.widthIn(max = maxWidth) else Modifier)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp * scaleFactor),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = (11 * scaleFactor).coerceAtMost(16f).sp,
                    letterSpacing = (1.5f * scaleFactor).sp
                ),
                color = labelColor,
                modifier = Modifier.padding(bottom = 8.dp * scaleFactor)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp * scaleFactor)) {
                    hint.guess.forEachIndexed { digitIndex, char ->
                        val status = if (isHelperModeEnabled) hint.digitStatuses?.getOrNull(digitIndex) else null
                        val bgColor = when (status) {
                            DigitStatus.CORRECT_POS -> SuccessGreen.copy(alpha = 0.2f)
                            DigitStatus.WRONG_POS -> WarningYellow.copy(alpha = 0.2f)
                            DigitStatus.INCORRECT -> ErrorRed.copy(alpha = 0.2f)
                            else -> Color.White.copy(alpha = 0.05f)
                        }
                        val borderColor = when (status) {
                            DigitStatus.CORRECT_POS -> SuccessGreen
                            DigitStatus.WRONG_POS -> WarningYellow
                            DigitStatus.INCORRECT -> ErrorRed
                            else -> Color.White.copy(alpha = 0.1f)
                        }

                        Box(
                            modifier = Modifier
                                .size(38.dp * scaleFactor)
                                .background(bgColor, RoundedCornerShape(8.dp * scaleFactor))
                                .border(1.dp, borderColor, RoundedCornerShape(8.dp * scaleFactor)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char.toString(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = (18 * scaleFactor).coerceAtMost(26f).sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (status != null) Color.White else PrimaryCyan
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp * scaleFactor))

            val hintText = if (hint.descriptionRes != null) {
                stringResource(hint.descriptionRes, *hint.descriptionArgs.toTypedArray())
            } else {
                hint.description
            }
            
            Text(
                text = hintText, 
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = (15 * scaleFactor).coerceAtMost(22f).sp,
                    lineHeight = (20 * scaleFactor).coerceAtMost(28f).sp,
                    fontWeight = FontWeight.Medium
                ), 
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

// --- BUTTONS ---

@Composable
fun DetectiveButton(
    text: String,
    onClick: () -> Unit,
    scaleFactor: Float,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    enabled: Boolean = true,
    pulseScale: Float = 1f,
    glowAlpha: Float = 0f,
    height: androidx.compose.ui.unit.Dp = 56.dp
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(height * scaleFactor)
            .graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
            }
            .border(
                (1.dp * scaleFactor) + (0.5.dp * glowAlpha * scaleFactor),
                if (isPrimary && enabled) PrimaryCyan.copy(alpha = 0.5f + (glowAlpha * 0.5f)) 
                else Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(16.dp * scaleFactor)
            ),
        shape = RoundedCornerShape(16.dp * scaleFactor),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isPrimary && enabled) PlayButtonGradient 
                    else Brush.linearGradient(listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.02f)))
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = Montserrat,
                    letterSpacing = (1.5 * scaleFactor).sp,
                    fontSize = (16 * scaleFactor).coerceAtMost(22f).sp,
                    fontWeight = FontWeight.Bold
                ),
                color = if (enabled) Color.White else Color.Gray
            )
        }
    }
}

// --- STATS ---

@Composable
fun DetectiveStatItem(
    label: String,
    value: String,
    color: Color,
    scaleFactor: Float,
    onClick: (() -> Unit)? = null
) {
    val scale = remember { Animatable(1f) }
    
    LaunchedEffect(value) {
        if (onClick != null) {
            scale.animateTo(1.15f, tween(150))
            scale.animateTo(1f, tween(150))
        }
    }

    val baseModifier = Modifier
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }

    val finalModifier = if (onClick != null) {
        baseModifier
            .clip(RoundedCornerShape(8.dp * scaleFactor))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable { onClick() }
            .padding(horizontal = 12.dp * scaleFactor, vertical = 6.dp * scaleFactor)
    } else {
        baseModifier.padding(horizontal = 12.dp * scaleFactor, vertical = 6.dp * scaleFactor)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = finalModifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label.uppercase(), 
                style = MaterialTheme.typography.labelSmall, 
                color = TextSecondary.copy(alpha = 0.6f), 
                fontSize = (10 * scaleFactor).coerceAtMost(16f).sp,
                letterSpacing = (1 * scaleFactor).sp
            )
            if (onClick != null) {
                Spacer(modifier = Modifier.width(4.dp * scaleFactor))
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = color.copy(alpha = 0.6f),
                    modifier = Modifier.size(10.dp * scaleFactor)
                )
            }
        }
        Text(
            text = value, 
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = (18 * scaleFactor).coerceAtMost(26f).sp,
                fontWeight = FontWeight.Black
            ), 
            color = color,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun SIAOfficialHeader(scaleFactor: Float) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp * scaleFactor),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "STRATEGIC INTELLIGENCE AGENCY",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (2 * scaleFactor).sp,
                fontSize = (10 * scaleFactor).coerceAtMost(14f).sp
            ),
            color = PrimaryCyan
        )
        Text(
            text = "DIVISION: LOGIC & CRYPTOGRAPHY",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = (8 * scaleFactor).coerceAtMost(12f).sp,
                fontWeight = FontWeight.Medium
            ),
            color = TextSecondary.copy(alpha = 0.5f)
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 4.dp * scaleFactor),
            color = PrimaryCyan.copy(alpha = 0.2f),
            thickness = 1.dp
        )
    }
}

/**
 * Shared layout defaults for rows and cards across the app.
 */
object RowDefaults {
    val CardBorder = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
}
