package com.brainfocus.numberdetective.feature.home

import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.brainfocus.numberdetective.BuildConfig
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.core.designsystem.*

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    isSoundEnabled: Boolean,
    onSoundToggle: (Boolean) -> Unit,
    isHelperModeEnabled: Boolean,
    onHelperModeToggle: (Boolean) -> Unit,
    onManualClick: () -> Unit,
    onDiagnosticClick: () -> Unit,
    scaleFactor: Float = 1.0f
) {
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    if (showAboutDialog) {
        AboutGameDialog(
            scaleFactor = scaleFactor,
            onDismiss = { showAboutDialog = false }
        )
    }

    if (showPrivacyDialog) {
        PrivacyPolicyDialog(
            scaleFactor = scaleFactor,
            onDismiss = { showPrivacyDialog = false }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 550.dp)
                    .fillMaxWidth(0.92f)
                    .clickable(enabled = false) {},
                color = SurfaceCard,
                shape = RoundedCornerShape(28.dp * scaleFactor),
                border = RowDefaults.CardBorder
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp * scaleFactor),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.settings_title),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = (20 * scaleFactor).coerceAtMost(32f).sp,
                                fontFamily = Montserrat,
                                letterSpacing = (1 * scaleFactor).sp
                            ),
                            color = PrimaryCyan
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp * scaleFactor)
                                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close, 
                                contentDescription = null, 
                                tint = Color.Gray, 
                                modifier = Modifier.size(18.dp * scaleFactor)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp * scaleFactor))

                    // Language Selection
                    SettingRow(label = stringResource(R.string.settings_language), scaleFactor = scaleFactor) {
                        Row(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp * scaleFactor))
                                .padding(4.dp * scaleFactor)
                        ) {
                            LanguageOption(
                                text = "TR",
                                isSelected = currentLanguage == "tr",
                                scaleFactor = scaleFactor,
                                onClick = { 
                                    onDismiss()
                                    onLanguageChange("tr") 
                                }
                            )
                            LanguageOption(
                                text = "EN",
                                isSelected = currentLanguage == "en",
                                scaleFactor = scaleFactor,
                                onClick = { 
                                    onDismiss()
                                    onLanguageChange("en") 
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp * scaleFactor))

                    // Sound Toggle
                    SettingRow(label = stringResource(R.string.settings_sound), scaleFactor = scaleFactor) {
                        Switch(
                            checked = isSoundEnabled,
                            onCheckedChange = onSoundToggle,
                            modifier = Modifier.scale(0.9f * scaleFactor),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PrimaryCyan,
                                checkedTrackColor = PrimaryCyan.copy(alpha = 0.3f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp * scaleFactor))

                    // Helper Mode Toggle
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.settings_helper),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = (15 * scaleFactor).sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                            Switch(
                                checked = isHelperModeEnabled,
                                onCheckedChange = onHelperModeToggle,
                                modifier = Modifier.scale(0.9f * scaleFactor),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = SuccessGreen,
                                    checkedTrackColor = SuccessGreen.copy(alpha = 0.3f),
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                                )
                            )
                        }
                        Text(
                            text = stringResource(R.string.settings_helper_desc),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = (12 * scaleFactor).sp,
                                lineHeight = (16 * scaleFactor).sp
                            ),
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp * scaleFactor, end = 32.dp * scaleFactor)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp * scaleFactor))

                    // How to Play / Manual Button
                    DetectiveButton(
                        text = "📖 " + stringResource(R.string.tutorial_title).uppercase(),
                        isPrimary = false,
                        scaleFactor = scaleFactor * 0.9f,
                        onClick = {
                            onDismiss()
                            onManualClick()
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp * scaleFactor))

                    // Scoring Protocol Button
                    DetectiveButton(
                        text = "🧠 " + stringResource(R.string.settings_scoring_manual).uppercase(),
                        isPrimary = false,
                        scaleFactor = scaleFactor * 0.9f,
                        onClick = {
                            onDismiss()
                            onDiagnosticClick()
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp * scaleFactor))
                    
                    val context = LocalContext.current
                    
                    // Rate App Button
                    DetectiveButton(
                        text = "⭐ " + stringResource(R.string.settings_rate_app).uppercase(),
                        isPrimary = false,
                        scaleFactor = scaleFactor * 0.9f,
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=${context.packageName}"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
                                context.startActivity(intent)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp * scaleFactor))

                    // Privacy Policy Button
                    DetectiveButton(
                        text = "📜 " + stringResource(R.string.settings_privacy_policy).uppercase(),
                        isPrimary = false,
                        scaleFactor = scaleFactor * 0.9f,
                        onClick = {
                            showPrivacyDialog = true
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp * scaleFactor))
                    
                    Text(
                        text = "VERSION ${BuildConfig.VERSION_NAME} - NOIR EDITION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = (9 * scaleFactor).sp,
                            letterSpacing = (2 * scaleFactor).sp
                        ),
                        color = Color.White.copy(alpha = 0.2f)
                    )

                    Spacer(modifier = Modifier.height(16.dp * scaleFactor))

                    Spacer(modifier = Modifier.height(24.dp))

                    // About Game Text Link
                    Text(
                        text = stringResource(R.string.home_about_button),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = (13 * scaleFactor).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Montserrat,
                            letterSpacing = (1 * scaleFactor).sp
                        ),
                        color = PrimaryCyan.copy(alpha = 0.7f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp * scaleFactor))
                            .clickable { showAboutDialog = true }
                            .padding(8.dp * scaleFactor)
                    )
                }
            }
        }
    }
}

@Composable
fun AboutGameDialog(
    scaleFactor: Float,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coffeeUrl = stringResource(R.string.about_buy_coffee_url)

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp * scaleFactor),
            color = Color(0xFF0F1923),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                PrimaryCyan.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .widthIn(max = if (configuration.screenWidthDp > 600) 800.dp else 440.dp * scaleFactor)
                .fillMaxWidth(0.95f)
                .heightIn(max = screenHeight * 0.95f)
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp * scaleFactor)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
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
                        letterSpacing = (2 * scaleFactor).sp,
                        fontFamily = Montserrat
                    ),
                    color = PrimaryCyan,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp * scaleFactor))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(PrimaryCyan.copy(alpha = 0.15f))
                )

                Spacer(modifier = Modifier.height(20.dp * scaleFactor))

                Text(
                    text = stringResource(R.string.about_dialog_body),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = (14 * scaleFactor).coerceAtMost(22f).sp,
                        lineHeight = (22 * scaleFactor).coerceAtMost(32f).sp,
                        fontFamily = Montserrat
                    ),
                    color = Color.White.copy(alpha = 0.82f),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(28.dp * scaleFactor))

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, coffeeUrl.toUri())
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
                            letterSpacing = (0.5 * scaleFactor).sp,
                            fontFamily = Montserrat
                        ),
                        color = Color(0xFF1A1200)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp * scaleFactor))

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

@Composable
fun SettingRow(label: String, scaleFactor: Float, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = (15 * scaleFactor).sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.White
        )
        content()
    }
}

@Composable
fun LanguageOption(text: String, isSelected: Boolean, scaleFactor: Float, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp * scaleFactor))
            .background(if (isSelected) PrimaryCyan.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 16.dp * scaleFactor, vertical = 8.dp * scaleFactor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = (14 * scaleFactor).sp
            ),
            color = if (isSelected) PrimaryCyan else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun PrivacyPolicyDialog(
    scaleFactor: Float,
    onDismiss: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp * scaleFactor),
            color = Color(0xFF0F1923),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                PrimaryCyan.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .widthIn(max = if (configuration.screenWidthDp > 600) 800.dp else 440.dp * scaleFactor)
                .fillMaxWidth(0.95f)
                .heightIn(max = screenHeight * 0.95f)
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp * scaleFactor)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "📜",
                    fontSize = (24 * scaleFactor).coerceAtMost(36f).sp,
                    color = PrimaryCyan
                )

                Spacer(modifier = Modifier.height(8.dp * scaleFactor))

                Text(
                    text = stringResource(R.string.privacy_policy_title).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = (16 * scaleFactor).coerceAtMost(24f).sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (2 * scaleFactor).sp,
                        fontFamily = Montserrat
                    ),
                    color = PrimaryCyan,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp * scaleFactor))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(PrimaryCyan.copy(alpha = 0.15f))
                )

                Spacer(modifier = Modifier.height(20.dp * scaleFactor))

                Text(
                    text = stringResource(R.string.privacy_policy_body),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = (14 * scaleFactor).coerceAtMost(22f).sp,
                        lineHeight = (22 * scaleFactor).coerceAtMost(32f).sp,
                        fontFamily = Montserrat
                    ),
                    color = Color.White.copy(alpha = 0.82f),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(28.dp * scaleFactor))

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
