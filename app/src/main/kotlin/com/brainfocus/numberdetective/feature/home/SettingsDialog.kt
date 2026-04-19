package com.brainfocus.numberdetective.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    onManualClick: () -> Unit
) {
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
                    .fillMaxWidth(0.85f)
                    .clickable(enabled = false) {},
                color = SurfaceCard,
                shape = RoundedCornerShape(28.dp),
                border = RowDefaults.CardBorder
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
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
                                fontFamily = Montserrat,
                                letterSpacing = 1.sp
                            ),
                            color = PrimaryCyan
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp).background(Color.White.copy(alpha = 0.05f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Language Selection
                    SettingRow(label = stringResource(R.string.settings_language)) {
                        Row(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .padding(4.dp)
                        ) {
                            LanguageOption(
                                text = "TR",
                                isSelected = currentLanguage == "tr",
                                onClick = { 
                                    onDismiss()
                                    onLanguageChange("tr") 
                                }
                            )
                            LanguageOption(
                                text = "EN",
                                isSelected = currentLanguage == "en",
                                onClick = { 
                                    onDismiss()
                                    onLanguageChange("en") 
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sound Toggle
                    SettingRow(label = stringResource(R.string.settings_sound)) {
                        Switch(
                            checked = isSoundEnabled,
                            onCheckedChange = onSoundToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PrimaryCyan,
                                checkedTrackColor = PrimaryCyan.copy(alpha = 0.3f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Helper Mode Toggle
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.settings_helper),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Switch(
                                checked = isHelperModeEnabled,
                                onCheckedChange = onHelperModeToggle,
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
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 4.dp, end = 48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // How to Play / Manual Button
                    Button(
                        onClick = {
                            onDismiss()
                            onManualClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(1.dp, PrimaryCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "📖", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.tutorial_title).uppercase(),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = PrimaryCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        text = "VERSION ${BuildConfig.VERSION_NAME} - NOIR EDITION",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.2f),
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SettingRow(label: String, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        content()
    }
}

@Composable
fun LanguageOption(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) PrimaryCyan.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) PrimaryCyan else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
