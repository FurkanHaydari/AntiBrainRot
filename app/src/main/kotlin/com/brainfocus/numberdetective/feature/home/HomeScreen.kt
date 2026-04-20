package com.brainfocus.numberdetective.feature.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.core.designsystem.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings

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
        
        // Robust scale factor: Baseline is 360dp (standard phone), scales up to 2.2x for tablets
        val scaleFactor = (maxWidth / 360.dp).coerceIn(1f, 2.2f)
        
        // Derived responsive dimensions
        // Derived responsive dimensions (Calculated as floats first for safe scaling)
        val titleBaseSize = 34f
        val titleFontSize = (titleBaseSize * scaleFactor).sp
        val buttonHeight = (58.dp * scaleFactor).coerceAtMost(100.dp)
        val playButtonHeight = (62.dp * scaleFactor).coerceAtMost(92.dp) // Slimmer button
        val horizontalPadding = (24.dp * scaleFactor).coerceAtMost(80.dp)
        val verticalPadding = (20.dp * scaleFactor).coerceAtMost(60.dp) // Reduced top/bottom padding to shift up

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
                .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- TOP: Identity & Access ---
            val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()
            val isHelperModeEnabled by viewModel.isHelperModeEnabled.collectAsState()
            var showSettings by remember { mutableStateOf(false) }

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
                            painter = painterResource(id = R.drawable.ic_attention),
                            contentDescription = "Archive",
                            tint = PrimaryCyan,
                            modifier = Modifier.size((22.dp * scaleFactor).coerceAtMost(40.dp))
                        )
                    }

                    IconButton(
                        onClick = { showSettings = true },
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

                if (showSettings) {
                    SettingsDialog(
                        onDismiss = { showSettings = false },
                        currentLanguage = currentLanguage,
                        onLanguageChange = onLanguageChange,
                        isSoundEnabled = isSoundEnabled,
                        onSoundToggle = { viewModel.toggleSound(it) },
                        isHelperModeEnabled = isHelperModeEnabled,
                        onHelperModeToggle = { viewModel.toggleHelperMode(it) },
                        onManualClick = onManualClick
                    )
                }

                Spacer(modifier = Modifier.height(12.dp * scaleFactor))

                // High Score Badge
                AnimatedVisibility(
                    visible = highScore > 0,
                    enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { -it }
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(14.dp * scaleFactor),
                        border = RowDefaults.CardBorder
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp * scaleFactor, vertical = 8.dp * scaleFactor),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🏆",
                                fontSize = (16 * scaleFactor).coerceAtMost(28f).sp,
                                modifier = Modifier.padding(end = 8.dp * scaleFactor)
                            )
                            Text(
                                text = stringResource(R.string.score_text, highScore),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = (14 * scaleFactor).coerceAtMost(24f).sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = PrimaryCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp * scaleFactor))
                
                // Game Title
                Text(
                    text = stringResource(R.string.app_title_1).uppercase(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = (titleBaseSize * scaleFactor * 0.75f).sp,
                        fontWeight = FontWeight.ExtraLight,
                        letterSpacing = (5 * scaleFactor).sp
                    ),
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.app_title_2).uppercase(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = titleFontSize,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (3 * scaleFactor).sp
                    ),
                    color = PrimaryCyan,
                    textAlign = TextAlign.Center
                )
            }

            // Balanced space above and below Briefing
            Spacer(modifier = Modifier.weight(1f).heightIn(min = 24.dp * scaleFactor))

            // --- CENTER: Briefing ---
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(1000, delayMillis = 400))
            ) {
                MissionBriefingPanel(scaleFactor, maxWidth)
            }

            // Balanced space above and below Briefing
            Spacer(modifier = Modifier.weight(1f).heightIn(min = 24.dp * scaleFactor))

            // --- BOTTOM: Actions ---
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(1200, delayMillis = 800)) + 
                        slideInVertically(animationSpec = tween(1200, delayMillis = 800)) { it / 3 }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .widthIn(max = (400.dp * scaleFactor).coerceAtMost(maxHeight))
                        .padding(bottom = 16.dp * scaleFactor) // Ensure it doesn't touch the navigation bar
                ) {
                    PlayButton(onClick = onPlayClick, height = playButtonHeight, scaleFactor = scaleFactor)
                    
                    Spacer(modifier = Modifier.height(14.dp * scaleFactor))

                    ManualButton(onClick = onManualClick, height = buttonHeight * 0.85f, scaleFactor = scaleFactor)
                    
                    Spacer(modifier = Modifier.height(14.dp * scaleFactor))

                    var showAboutDialog by remember { mutableStateOf(false) }
                    AboutButton(onClick = { showAboutDialog = true }, height = buttonHeight * 0.85f, scaleFactor = scaleFactor)

                    if (showAboutDialog) {
                        AboutGameDialog(
                            scaleFactor = scaleFactor,
                            maxWidth = maxWidth,
                            onDismiss = { showAboutDialog = false }
                        )
                    }
                }
            }

            // Final push to keep everything shifted slightly up
            Spacer(modifier = Modifier.weight(0.4f))
        }
    }
}

@Composable
fun MissionBriefingPanel(scaleFactor: Float, maxWidth: androidx.compose.ui.unit.Dp) {
    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(28.dp * scaleFactor),
        modifier = Modifier
            .widthIn(max = (420.dp * scaleFactor).coerceAtMost(maxWidth)) // Prevent excessive width
            .padding(horizontal = 16.dp * scaleFactor), // Increased padding for a slimmer look
        border = RowDefaults.CardBorder
    ) {
        Column(
            modifier = Modifier.padding(24.dp * scaleFactor),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.home_mission_title).uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = (14 * scaleFactor).coerceAtMost(24f).sp),
                color = PrimaryCyan,
                letterSpacing = (2 * scaleFactor).sp
            )
            
            Spacer(modifier = Modifier.height(24.dp * scaleFactor))
            
            BriefingRow(
                icon = "🔍",
                title = stringResource(R.string.home_step_1_title),
                description = stringResource(R.string.home_step_1_desc),
                scaleFactor = scaleFactor
            )
            
            Spacer(modifier = Modifier.height(16.dp * scaleFactor))
            
            BriefingRow(
                icon = "🧠",
                title = stringResource(R.string.home_step_2_title),
                description = stringResource(R.string.home_step_2_desc),
                scaleFactor = scaleFactor
            )
            
            Spacer(modifier = Modifier.height(16.dp * scaleFactor))
            
            BriefingRow(
                icon = "🎯",
                title = stringResource(R.string.home_step_3_title),
                description = stringResource(R.string.home_step_3_desc),
                scaleFactor = scaleFactor
            )
        }
    }
}

@Composable
fun BriefingRow(icon: String, title: String, description: String, scaleFactor: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size((44.dp * scaleFactor).coerceAtMost(70.dp))
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp * scaleFactor))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp * scaleFactor)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = (20 * scaleFactor).coerceAtMost(32f).sp)
        }
        
        Spacer(modifier = Modifier.width(16.dp * scaleFactor))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = (16 * scaleFactor).coerceAtMost(26f).sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = (12 * scaleFactor).coerceAtMost(18f).sp,
                    lineHeight = (16 * scaleFactor).coerceAtMost(24f).sp
                ),
                color = TextSecondary
            )
        }
    }
}

@Composable
fun PlayButton(onClick: () -> Unit, height: androidx.compose.ui.unit.Dp, scaleFactor: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .border(2.dp, PrimaryCyan.copy(alpha = glowAlpha), RoundedCornerShape(20.dp * scaleFactor)),
        shape = RoundedCornerShape(20.dp * scaleFactor),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PlayButtonGradient),
            contentAlignment = Alignment.Center
        ) {
            Text(
                stringResource(R.string.start_button),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = (20 * scaleFactor).coerceAtMost(32f).sp),
                color = Color.White
            )
        }
    }
}

@Composable
fun LanguageButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) PrimaryCyan.copy(alpha = 0.2f) else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        border = if (isSelected) RowDefaults.CardBorder else null
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = if (isSelected) PrimaryCyan else Color.White.copy(alpha = 0.5f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 12.sp
        )
    }
}

object RowDefaults {
    val CardBorder @Composable get() = androidx.compose.foundation.BorderStroke(
        width = 1.dp,
        brush = CardBorderGradient
    )
}

@Composable
fun ManualButton(onClick: () -> Unit, height: androidx.compose.ui.unit.Dp, scaleFactor: Float) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .border(1.dp, PrimaryCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp * scaleFactor)),
        shape = RoundedCornerShape(16.dp * scaleFactor),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "📖", fontSize = (18 * scaleFactor).coerceAtMost(28f).sp)
                Spacer(modifier = Modifier.width(12.dp * scaleFactor))
                Text(
                    stringResource(R.string.home_manual_button).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = (16 * scaleFactor).coerceAtMost(24f).sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (2 * scaleFactor).sp
                    ),
                    color = PrimaryCyan.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
fun AboutButton(onClick: () -> Unit, height: androidx.compose.ui.unit.Dp, scaleFactor: Float) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .border(1.dp, PrimaryCyan.copy(alpha = 0.25f), RoundedCornerShape(14.dp * scaleFactor)),
        shape = RoundedCornerShape(14.dp * scaleFactor),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.03f)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "🧬", fontSize = (16 * scaleFactor).coerceAtMost(24f).sp)
                Spacer(modifier = Modifier.width(10.dp * scaleFactor))
                Text(
                    stringResource(R.string.home_about_button).uppercase(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = (14 * scaleFactor).coerceAtMost(20f).sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (1.5 * scaleFactor).sp
                    ),
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
fun AboutGameDialog(
    scaleFactor: Float,
    maxWidth: androidx.compose.ui.unit.Dp,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coffeeUrl = stringResource(R.string.about_buy_coffee_url)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp * scaleFactor),
            color = Color(0xFF0F1923),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                PrimaryCyan.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .widthIn(max = (400.dp * scaleFactor).coerceAtMost(maxWidth))
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp * scaleFactor)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "✦",
                    fontSize = (24 * scaleFactor).coerceAtMost(36f).sp,
                    color = PrimaryCyan
                )

                Spacer(modifier = Modifier.height(8.dp * scaleFactor))

                Text(
                    text = stringResource(R.string.about_dialog_title).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = (16 * scaleFactor).coerceAtMost(24f).sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (2 * scaleFactor).sp
                    ),
                    color = PrimaryCyan,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp * scaleFactor))

                // Divider line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(PrimaryCyan.copy(alpha = 0.15f))
                )

                Spacer(modifier = Modifier.height(20.dp * scaleFactor))

                // Body text
                Text(
                    text = stringResource(R.string.about_dialog_body),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = (14 * scaleFactor).coerceAtMost(22f).sp,
                        lineHeight = (22 * scaleFactor).coerceAtMost(32f).sp
                    ),
                    color = Color.White.copy(alpha = 0.82f),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(28.dp * scaleFactor))

                // Buy Me a Coffee button
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(coffeeUrl))
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((52.dp * scaleFactor).coerceAtMost(80.dp)),
                    shape = RoundedCornerShape(14.dp * scaleFactor),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFDD00)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.about_buy_coffee),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = (14 * scaleFactor).coerceAtMost(20f).sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (0.5 * scaleFactor).sp
                        ),
                        color = Color(0xFF1A1200)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp * scaleFactor))

                // Close
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "✕",
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = (13 * scaleFactor).coerceAtMost(20f).sp
                    )
                }
            }
        }
    }
}
