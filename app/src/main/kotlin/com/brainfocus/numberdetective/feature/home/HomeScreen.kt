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

    Box(modifier = Modifier.fillMaxSize()) {
        // --- Layer 1: Atmospheric Background ---
        Image(
            painter = painterResource(id = R.drawable.detective_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // --- Layer 2: Deep Gradient Overlay ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundOverlayGradient)
        )

        // --- Layer 3: UI Content ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // --- Section 1: Top (Settings & Title) ---
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
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.05f), CircleShape)
                            .border(1.dp, PrimaryCyan.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_attention),
                            contentDescription = "Archive",
                            tint = PrimaryCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { showSettings = true },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.05f), CircleShape)
                            .border(1.dp, PrimaryCyan.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = PrimaryCyan,
                            modifier = Modifier.size(20.dp)
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

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedVisibility(
                    visible = highScore > 0,
                    enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { -it }
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        border = RowDefaults.CardBorder
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.score_text, highScore),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = stringResource(R.string.app_title_1).uppercase(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Light
                    ),
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = stringResource(R.string.app_title_2).uppercase(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = PrimaryCyan
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Center: Glass HUD Briefing
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(1000, delayMillis = 400))
            ) {
                MissionBriefingPanel()
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom: Magnetic Play Button
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(1200, delayMillis = 800)) + 
                        slideInVertically(animationSpec = tween(1200, delayMillis = 800)) { it / 3 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ManualButton(onClick = onManualClick)
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    PlayButton(onClick = onPlayClick)
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    var showAboutDialog by remember { mutableStateOf(false) }
                    AboutButton(onClick = { showAboutDialog = true })

                    if (showAboutDialog) {
                        AboutGameDialog(onDismiss = { showAboutDialog = false })
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun MissionBriefingPanel() {
    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        border = RowDefaults.CardBorder
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.home_mission_title).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryCyan,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            BriefingRow(
                icon = "🔍",
                title = stringResource(R.string.home_step_1_title),
                description = stringResource(R.string.home_step_1_desc)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BriefingRow(
                icon = "🧠",
                title = stringResource(R.string.home_step_2_title),
                description = stringResource(R.string.home_step_2_desc)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BriefingRow(
                icon = "🎯",
                title = stringResource(R.string.home_step_3_title),
                description = stringResource(R.string.home_step_3_desc)
            )
        }
    }
}

@Composable
fun BriefingRow(icon: String, title: String, description: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 20.sp)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun PlayButton(onClick: () -> Unit) {
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
            .height(68.dp)
            .border(2.dp, PrimaryCyan.copy(alpha = glowAlpha), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
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
                style = MaterialTheme.typography.titleLarge,
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
fun ManualButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(1.dp, PrimaryCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
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
                Text(text = "📖", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    stringResource(R.string.home_manual_button).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = PrimaryCyan.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun AboutButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .border(1.dp, PrimaryCyan.copy(alpha = 0.25f), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
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
                Text(text = "🧬", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    stringResource(R.string.home_about_button).uppercase(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp
                    ),
                    color = Color.White.copy(alpha = 0.55f)
                )
            }
        }
    }
}

@Composable
fun AboutGameDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val coffeeUrl = stringResource(R.string.about_buy_coffee_url)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F1923),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                PrimaryCyan.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "✦",
                    fontSize = 24.sp,
                    color = PrimaryCyan
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.about_dialog_title).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = PrimaryCyan,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Divider line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(PrimaryCyan.copy(alpha = 0.15f))
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Body text
                Text(
                    text = stringResource(R.string.about_dialog_body),
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = Color.White.copy(alpha = 0.82f),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Buy Me a Coffee button
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(coffeeUrl))
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFDD00)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.about_buy_coffee),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color(0xFF1A1200)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Close
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "✕",
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
