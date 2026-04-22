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
    // Metalic reflection effect
    Box(
        modifier = modifier
            .size(width = 14.dp * scaleFactor, height = 36.dp * scaleFactor)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFB0BEC5).copy(alpha = 0.9f),
                        Color(0xFFCFD8DC).copy(alpha = 0.5f),
                        Color(0xFFB0BEC5).copy(alpha = 0.9f)
                    )
                ), 
                RoundedCornerShape(percent = 50)
            )
            .border(1.5.dp * scaleFactor, Color.White.copy(alpha = 0.4f), RoundedCornerShape(percent = 50))
    )
}

@Composable
fun MissionStamp(text: String, color: Color, scaleFactor: Float, modifier: Modifier = Modifier) {
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(4.dp * scaleFactor),
        border = androidx.compose.foundation.BorderStroke(2.5.dp * scaleFactor, color.copy(alpha = 0.4f)),
        modifier = modifier
            .rotate(-12f)
            .padding(8.dp * scaleFactor)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp * scaleFactor, vertical = 6.dp * scaleFactor),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Black,
                    fontSize = (20 * scaleFactor).coerceAtMost(28f).sp,
                    letterSpacing = (2 * scaleFactor).sp
                ),
                color = color.copy(alpha = 0.6f)
            )
            Text(
                text = "SIA OFFICIAL RECORDS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = (7 * scaleFactor).sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = color.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun DetectiveBriefingSheet(
    isWin: Boolean,
    scaleFactor: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Scanning")
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ScanLine"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp * scaleFactor))
            .background(
                Brush.verticalGradient(
                    colors = listOf(SurfaceCard, Color.Black.copy(alpha = 0.95f))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp * scaleFactor))
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp * scaleFactor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "CLASSIFIED",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Black,
                    fontSize = (60 * scaleFactor).sp
                ),
                color = Color.White.copy(alpha = 0.02f),
                modifier = Modifier.rotate(-45f)
            )
        }

        // 2. Content Layer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp * scaleFactor)
                .padding(bottom = 24.dp * scaleFactor) // Give room for the stamp
        ) {
            content()
        }

        // 3. Artifact Overlay (Scanning Line)
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val height = constraints.maxHeight.toFloat()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp * scaleFactor)
                    .graphicsLayer { translationY = height * scanProgress }
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, PrimaryCyan.copy(alpha = 0.12f), Color.Transparent)
                        )
                    )
            )
        }

        // 4. Paper Clip Accessory (Moved further left)
        PaperClip(
            scaleFactor = scaleFactor,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp * scaleFactor, top = (2).dp * scaleFactor)
        )

        // 5. Official Stamp (Reduced opacity for better readability of text underneath)
        MissionStamp(
            text = if (isWin) "APPROVED" else "COMPROMISED",
            color = if (isWin) SuccessGreen else ErrorRed,
            scaleFactor = scaleFactor,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .graphicsLayer { alpha = 0.7f }
                .padding(end = 8.dp * scaleFactor, bottom = 8.dp * scaleFactor)
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
            Column(modifier = Modifier.weight(1f, fill = false)) {
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

            Spacer(modifier = Modifier.width(24.dp * scaleFactor))

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
    maxWidth: androidx.compose.ui.unit.Dp = androidx.compose.ui.unit.Dp.Unspecified,
    isInterrogation: Boolean = false,
    isFullscreenMode: Boolean = false
) {
    val isLevel3 = hint.guess.length == 4
    val coins = (hint.correct * 2) + hint.misplaced
    
    val dynamicColor = if (isInterrogation) {
        if (isLevel3) {
            when {
                coins == 0 -> HeatMap0
                coins <= 2 -> HeatMap1_2
                coins <= 5 -> HeatMap3_4
                coins <= 7 -> HeatMap5
                else -> HeatMap6
            }
        } else {
            when {
                coins == 0 -> HeatMap0
                coins <= 2 -> HeatMap1_2
                coins <= 4 -> HeatMap3_4
                coins == 5 -> HeatMap5
                else -> HeatMap6
            }
        }
    } else labelColor

    val cardPadding = if (isFullscreenMode) 20.dp * scaleFactor else 14.dp * scaleFactor
    val labelFontSize = if (isFullscreenMode) (14 * scaleFactor).coerceAtMost(22f).sp else (11 * scaleFactor).coerceAtMost(16f).sp
    val digitBoxSize = if (isFullscreenMode) 52.dp * scaleFactor else 38.dp * scaleFactor
    val digitFontSize = if (isFullscreenMode) (24 * scaleFactor).coerceAtMost(36f).sp else (18 * scaleFactor).coerceAtMost(26f).sp
    val descriptionFontSize = if (isFullscreenMode) (18 * scaleFactor).coerceAtMost(28f).sp else (15 * scaleFactor).coerceAtMost(22f).sp
    val descriptionLineHeight = if (isFullscreenMode) (24 * scaleFactor).coerceAtMost(34f).sp else (20 * scaleFactor).coerceAtMost(28f).sp

    if (isFullscreenMode) {
        // Fullscreen mode: No separate card Surface, just a compact vertical unit for the unified sheet
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp * scaleFactor),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = (10 * scaleFactor).coerceAtMost(14f).sp,
                    letterSpacing = (1.5f * scaleFactor).sp
                ),
                color = if (isInterrogation) dynamicColor else labelColor.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 4.dp * scaleFactor)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp * scaleFactor)) {
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
                                .size(digitBoxSize * 0.9f) // Slightly more compact digits
                                .background(bgColor, RoundedCornerShape(8.dp * scaleFactor))
                                .border(1.dp, borderColor, RoundedCornerShape(8.dp * scaleFactor)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char.toString(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = digitFontSize,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (status != null) Color.White else PrimaryCyan
                            )
                        }
                    }
                }
            }

            val context = androidx.compose.ui.platform.LocalContext.current
            val isLevel3 = hint.guess.length == 4
            val hintText = hint.getDisplayText(context, isLevel3)
            
            Text(
                text = hintText, 
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = descriptionFontSize,
                    lineHeight = descriptionLineHeight,
                    fontWeight = FontWeight.Medium
                ), 
                color = Color.White.copy(alpha = 0.95f),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp * scaleFactor),
                textAlign = TextAlign.Center
            )
        }
    } else {
        Surface(
            color = SurfaceCard,
            shape = RoundedCornerShape(16.dp * scaleFactor),
            border = androidx.compose.foundation.BorderStroke(
                if (isInterrogation) 1.5.dp * scaleFactor else 1.dp, 
                if (isInterrogation) dynamicColor.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f)
            ),
            modifier = Modifier
                .then(if (maxWidth != androidx.compose.ui.unit.Dp.Unspecified) Modifier.widthIn(max = maxWidth) else Modifier)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(cardPadding / 1.5f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = labelFontSize,
                        letterSpacing = (2f * scaleFactor).sp
                    ),
                    color = if (isInterrogation) dynamicColor else labelColor,
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
                                    .size(digitBoxSize)
                                    .background(bgColor, RoundedCornerShape(8.dp * scaleFactor))
                                    .border(1.dp, borderColor, RoundedCornerShape(8.dp * scaleFactor)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char.toString(),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = digitFontSize,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (status != null) Color.White else PrimaryCyan
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp * scaleFactor))

                val context = androidx.compose.ui.platform.LocalContext.current
                val isLevel3 = hint.guess.length == 4
                val hintText = hint.getDisplayText(context, isLevel3)
            
                
                Text(
                    text = hintText, 
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = descriptionFontSize,
                        lineHeight = descriptionLineHeight,
                        fontWeight = FontWeight.Medium
                    ), 
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
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
            fontFamily = Montserrat,
            fontWeight = FontWeight.Black
        )
    }
}


/**
 * Shared layout defaults for rows and cards across the app.
 */
object RowDefaults {
    val CardBorder = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
}
