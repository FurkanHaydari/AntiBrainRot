package com.brainfocus.numberdetective.feature.history

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.core.designsystem.*
import com.brainfocus.numberdetective.data.storage.GameSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val history by viewModel.history.collectAsState()
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scaleFactor = (maxHeight.value / 720f).coerceIn(1.0f, 2.2f)
        val maxWidthDp = maxWidth

        // --- Layer 1: Background ---
        Image(
            painter = painterResource(id = R.drawable.detective_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Black.copy(alpha = 0.95f))
                    )
                )
        )

        // --- Layer 2: Main Content ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(48.dp * scaleFactor)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp * scaleFactor))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PrimaryCyan,
                        modifier = Modifier.size(24.dp * scaleFactor)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp * scaleFactor))
                Text(
                    text = stringResource(R.string.label_tab_archive).uppercase(),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = Montserrat,
                        letterSpacing = (2 * scaleFactor).sp,
                        fontSize = (24 * scaleFactor).coerceAtMost(32f).sp
                    ),
                    color = PrimaryCyan
                )
            }

            Spacer(modifier = Modifier.height(24.dp * scaleFactor))

            if (history.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.msg_no_evidence_waiting),
                        color = TextSecondary.copy(alpha = 0.5f),
                        letterSpacing = (1 * scaleFactor).sp,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = (16 * scaleFactor).coerceAtMost(24f).sp
                        )
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp * scaleFactor),
                    contentPadding = PaddingValues(bottom = 24.dp * scaleFactor),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(history.size) { index ->
                        val session = history[index]
                        val caseNumber = history.size - index
                        HistoryItem(
                            session = session, 
                            caseNumber = caseNumber,
                            scaleFactor = scaleFactor,
                            maxWidth = maxWidthDp,
                            onClick = { onNavigateToDetail(session.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(session: GameSession, caseNumber: Int, scaleFactor: Float, maxWidth: androidx.compose.ui.unit.Dp, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val dateString = remember(session.timestamp) { dateFormat.format(Date(session.timestamp)) }
    
    Surface(
        onClick = onClick,
        color = SurfaceCard,
        shape = RoundedCornerShape(16.dp * scaleFactor),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier
            .widthIn(max = (550.dp * scaleFactor).coerceAtMost(maxWidth))
            .fillMaxWidth()
    ) {
        Box {
            // Paperclip Effect
            PaperClip(
                scaleFactor = scaleFactor,
                modifier = Modifier
                    .padding(start = 12.dp * scaleFactor, top = (-4).dp * scaleFactor)
                    .align(Alignment.TopStart)
            )

            Column(
                modifier = Modifier
                    .padding(20.dp * scaleFactor)
                    .fillMaxWidth()
            ) {
                // Header: Case # and Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = PrimaryCyan.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp * scaleFactor),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "CASE #$caseNumber",
                            modifier = Modifier.padding(horizontal = 8.dp * scaleFactor, vertical = 2.dp * scaleFactor),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = (14 * scaleFactor).coerceAtMost(20f).sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = PrimaryCyan
                        )
                    }
                    
                    Box(contentAlignment = Alignment.Center) {
                        MissionStamp(
                            text = if (session.isWin) stringResource(R.string.mission_accomplished) 
                                   else stringResource(R.string.mission_failed),
                            color = if (session.isWin) SuccessGreen else ErrorRed,
                            scaleFactor = scaleFactor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp * scaleFactor))

                // Date and Session Details
                Text(
                    text = dateString.uppercase(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = (16 * scaleFactor).coerceAtMost(24f).sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = (12f / 10f * scaleFactor).sp
                    ),
                    color = TextSecondary.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(20.dp * scaleFactor))

                // Score Detail (Primary focus in the new layout)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.final_score).uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = (14 * scaleFactor).coerceAtMost(20f).sp,
                                letterSpacing = (1 * scaleFactor).sp
                            ),
                            color = TextSecondary.copy(alpha = 0.4f)
                        )
                        Text(
                            text = session.totalScore.toString(),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = (40 * scaleFactor).coerceAtMost(56f).sp
                            ),
                            color = PrimaryCyan
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp * scaleFactor))
            
            // Level summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                session.levels.forEach { level ->
                    LevelBadge(levelNumber = level.levelNumber, scaleFactor = scaleFactor)
                }
            }
        }
    }
}

@Composable
fun LevelBadge(levelNumber: Int, scaleFactor: Float) {
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp * scaleFactor))
            .border(1.dp, PrimaryCyan.copy(alpha = 0.2f), RoundedCornerShape(6.dp * scaleFactor))
            .padding(horizontal = 8.dp * scaleFactor, vertical = 4.dp * scaleFactor)
    ) {
        Text(
            text = "LVL $levelNumber",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = (16 * scaleFactor).coerceAtMost(22f).sp
            ),
            color = TextSecondary
        )
    }
}
@Composable
fun MissionStamp(text: String, color: Color, scaleFactor: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .rotate(-15f)
            .border(
                width = (2.dp * scaleFactor),
                color = color.copy(alpha = 0.4f),
                shape = RoundedCornerShape(4.dp * scaleFactor)
            )
            .padding(horizontal = 8.dp * scaleFactor, vertical = 4.dp * scaleFactor)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                fontSize = (16 * scaleFactor).coerceAtMost(22f).sp,
                letterSpacing = (1.5f * scaleFactor).sp,
                fontFamily = FontFamily.Monospace
            ),
            color = color.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun PaperClip(scaleFactor: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 12.dp * scaleFactor, height = 32.dp * scaleFactor)
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(percent = 50))
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(percent = 50))
    )
}
