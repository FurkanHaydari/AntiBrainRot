package com.brainfocus.numberdetective.feature.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.core.designsystem.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onPlayClick: () -> Unit,
    onManualClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onLanguageChange: (String) -> Unit,
    currentLanguage: String
) {
    val highScore by viewModel.highScore.collectAsState()
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxWidth = maxWidth
        val maxHeight = maxHeight
        
        
        // Derived responsive dimensions
        val isLandscape = maxWidth > maxHeight
        val isTablet = maxWidth > 600.dp
        
        val scaleFactor = if (isLandscape) {
            minOf(maxWidth / 720.dp, maxHeight / 400.dp).coerceIn(0.8f, 1.3f)
        } else {
            minOf(maxWidth / 360.dp, maxHeight / 640.dp).coerceIn(1f, 1.7f)
        }
        
        val titleBaseSize = if (isLandscape) 28f else 34f
        val titleFontSize = (titleBaseSize * scaleFactor).sp
        val horizontalPadding = (24.dp * scaleFactor).coerceAtMost(80.dp)
        val verticalPadding = (16.dp * scaleFactor).coerceAtMost(48.dp)

        // --- Background Layers ---
        Image(
            painter = painterResource(id = R.drawable.detective_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(modifier = Modifier.fillMaxSize().background(BackgroundOverlayGradient))

        // --- Main UI ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- TOP: Identity & Access ---
            val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()
            val isHelperModeEnabled by viewModel.isHelperModeEnabled.collectAsState()
            var showSettings by remember { mutableStateOf(false) }

            HomeHeader(
                highScore = highScore,
                scaleFactor = scaleFactor,
                onHistoryClick = onHistoryClick,
                onSettingsClick = { showSettings = true }
            )

            Spacer(modifier = Modifier.height(10.dp * scaleFactor)) // Space below the score bar (Reduced from 16dp)

            if (showSettings) {
                SettingsDialog(
                    onDismiss = { showSettings = false },
                    currentLanguage = currentLanguage,
                    onLanguageChange = onLanguageChange,
                    isSoundEnabled = isSoundEnabled,
                    onSoundToggle = { viewModel.toggleSound(it) },
                    isHelperModeEnabled = isHelperModeEnabled,
                    onHelperModeToggle = { viewModel.toggleHelperMode(it) },
                    onManualClick = onManualClick,
                    scaleFactor = scaleFactor
                )
            }

            // --- ADAPTIVE CONTENT ---
            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(32.dp * scaleFactor, Alignment.CenterHorizontally)
                ) {
                    // Left: Title & Status
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(vertical = 16.dp * scaleFactor)
                    ) {
                        Text(
                            text = stringResource(R.string.app_title_1).uppercase(),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = (titleBaseSize * scaleFactor * 0.7f).sp,
                                fontWeight = FontWeight.ExtraLight,
                                letterSpacing = (5 * scaleFactor).sp,
                                fontFamily = Montserrat
                            ),
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.app_title_2).uppercase(),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = titleFontSize,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (3 * scaleFactor).sp,
                                fontFamily = Montserrat
                            ),
                            color = PrimaryCyan,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.weight(1f)) // Push button to bottom
                        
                        // --- ACTION BUTTONS WITH ANIMATION ---
                        val infiniteTransition = rememberInfiniteTransition(label = "StartButtonAnim")
                        val pulseScale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.05f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1200, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "PulseScale"
                        )
                        val glowAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 0.9f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1200, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "GlowAlpha"
                        )

                        DetectiveButton(
                            text = stringResource(R.string.start_button),
                            isPrimary = true,
                            scaleFactor = scaleFactor,
                            onClick = onPlayClick,
                            pulseScale = pulseScale,
                            glowAlpha = glowAlpha,
                            modifier = Modifier
                                .widthIn(max = if (maxWidth > 600.dp) 400.dp else 240.dp)
                                .fillMaxWidth(if (maxWidth > 600.dp) 0.85f else 0.9f)
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp * scaleFactor)) // Bottom margin
                    }

                    // Right: Briefing
                    Box(modifier = Modifier.weight(1.2f), contentAlignment = Alignment.Center) {
                        MissionBriefingPanel(scaleFactor, maxWidth)
                    }
                }
            } else {
                // Portrait Layout
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.weight(0.7f)) // Reduced from 1.0f to bring title closer to top scores
                    
                    // Header Column
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.app_title_1).uppercase(),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = (titleBaseSize * scaleFactor * 0.75f).sp,
                                fontWeight = FontWeight.ExtraLight,
                                letterSpacing = (5 * scaleFactor).sp,
                                fontFamily = Montserrat
                            ),
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.app_title_2).uppercase(),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = titleFontSize,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (3 * scaleFactor).sp,
                                fontFamily = Montserrat
                            ),
                            color = PrimaryCyan,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.weight(1.5f)) // Decompressed title area

                    MissionBriefingPanel(scaleFactor, maxWidth)

                    Spacer(modifier = Modifier.weight(0.9f))

                    // --- ACTION BUTTONS WITH ANIMATION ---
                    val infiniteTransition = rememberInfiniteTransition(label = "StartButtonAnim")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "PulseScale"
                    )
                    val glowAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 0.9f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "GlowAlpha"
                    )

                    DetectiveButton(
                        text = stringResource(R.string.start_button),
                        isPrimary = true,
                        scaleFactor = scaleFactor,
                        onClick = onPlayClick,
                        pulseScale = pulseScale,
                        glowAlpha = glowAlpha,
                        modifier = Modifier
                            .widthIn(max = if (maxWidth > 600.dp) 750.dp else 500.dp)
                            .fillMaxWidth(if (maxWidth > 600.dp) 0.95f else 0.9f)
                            .padding(bottom = (32.dp * scaleFactor).coerceAtLeast(16.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun HomeHeader(
    highScore: Int,
    scaleFactor: Float,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onHistoryClick,
                modifier = Modifier
                    .size((44.dp * scaleFactor).coerceAtMost(80.dp))
                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                    .border(1.dp, PrimaryCyan.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = "Archive",
                    tint = PrimaryCyan,
                    modifier = Modifier.size((22.dp * scaleFactor).coerceAtMost(40.dp))
                )
            }

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size((44.dp * scaleFactor).coerceAtMost(80.dp))
                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                    .border(1.dp, PrimaryCyan.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = PrimaryCyan,
                    modifier = Modifier.size((22.dp * scaleFactor).coerceAtMost(40.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp * scaleFactor))

        // Case Status Badge
        AnimatedVisibility(
            visible = highScore > 0,
            enter = fadeIn(tween(1000)) + expandVertically(tween(1000))
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Color.White.copy(alpha = 0.05f),
                        RoundedCornerShape(12.dp * scaleFactor)
                    )
                    .border(
                        1.dp,
                        PrimaryCyan.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp * scaleFactor)
                    )
                    .padding(horizontal = 14.dp * scaleFactor, vertical = 6.dp * scaleFactor)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ID: #${10000 + (highScore % 1000)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = (9 * scaleFactor).sp,
                            fontFamily = Montserrat,
                            color = Color.White.copy(alpha = 0.4f),
                            letterSpacing = (1.5 * scaleFactor).sp
                        )
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp * scaleFactor))
                    
                    Box(modifier = Modifier.size(1.dp, 10.dp * scaleFactor).background(Color.White.copy(alpha = 0.1f)))
                    
                    Spacer(modifier = Modifier.width(12.dp * scaleFactor))

                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = PrimaryCyan.copy(alpha = 0.8f),
                        modifier = Modifier
                            .size(16.dp * scaleFactor)
                            .padding(end = 4.dp * scaleFactor)
                    )
                    
                    Text(
                        text = stringResource(R.string.score_text, highScore).uppercase(),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = (11 * scaleFactor).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Montserrat,
                            color = PrimaryCyan,
                            letterSpacing = (1 * scaleFactor).sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MissionBriefingPanel(scaleFactor: Float, maxWidth: androidx.compose.ui.unit.Dp) {
    Surface(
        modifier = Modifier
            .widthIn(max = if (maxWidth > 600.dp) 750.dp else 500.dp)
            .fillMaxWidth(if (maxWidth > 600.dp) 0.95f else 0.9f),
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(20.dp * scaleFactor),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp * scaleFactor),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.home_mission_title).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = (13 * scaleFactor).coerceAtMost(20f).sp,
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold
                    ),
                    color = PrimaryCyan,
                    letterSpacing = (2.5 * scaleFactor).sp,
                    modifier = Modifier.align(Alignment.Center)
                )
                
                // Subtle "CONFIDENTIAL" tab
                Text(
                    text = "SIA-7",
                    fontSize = (8 * scaleFactor).sp,
                    color = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp * scaleFactor))
            
            BriefingRow(
                icon = "🔍",
                title = stringResource(R.string.home_step_1_title),
                description = stringResource(R.string.home_step_1_desc),
                cognitiveLabel = stringResource(R.string.home_cognitive_1),
                scaleFactor = scaleFactor
            )
            
            Spacer(modifier = Modifier.height(28.dp * scaleFactor))
            
            BriefingRow(
                icon = "🧠",
                title = stringResource(R.string.home_step_2_title),
                description = stringResource(R.string.home_step_2_desc),
                cognitiveLabel = stringResource(R.string.home_cognitive_2),
                scaleFactor = scaleFactor
            )
            
            Spacer(modifier = Modifier.height(28.dp * scaleFactor))
            
            BriefingRow(
                icon = "🎯",
                title = stringResource(R.string.home_step_3_title),
                description = stringResource(R.string.home_step_3_desc),
                cognitiveLabel = stringResource(R.string.home_cognitive_3),
                scaleFactor = scaleFactor
            )
        }
    }
}

@Composable
fun BriefingRow(icon: String, title: String, description: String, cognitiveLabel: String, scaleFactor: Float) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size((44.dp * scaleFactor).coerceAtMost(64.dp))
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp * scaleFactor))
                .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp * scaleFactor)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = (22 * scaleFactor).coerceAtMost(32f).sp)
        }
        
        Spacer(modifier = Modifier.width(18.dp * scaleFactor))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = (15 * scaleFactor).coerceAtMost(24f).sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Montserrat
                ),
                color = Color.White
            )
            
            // Cognitive Tag - Now below title with more distinct style
            Text(
                text = cognitiveLabel.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = (8.5f * scaleFactor).sp,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (0.5f * scaleFactor).sp
                ),
                color = PrimaryCyan.copy(alpha = 0.7f),
                modifier = Modifier.padding(vertical = 2.dp * scaleFactor)
            )
            
            Spacer(modifier = Modifier.height(2.dp * scaleFactor))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = (12.5f * scaleFactor).coerceAtMost(18f).sp,
                    lineHeight = (16f * scaleFactor).coerceAtMost(24f).sp,
                    fontFamily = Montserrat
                ),
                color = TextSecondary.copy(alpha = 0.9f)
            )
        }
    }
}
