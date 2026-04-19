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

    Box(modifier = Modifier.fillMaxSize()) {
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
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PrimaryCyan
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(R.string.label_tab_archive).uppercase(),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = Montserrat,
                        letterSpacing = 2.sp
                    ),
                    color = PrimaryCyan
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (history.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.msg_no_evidence_waiting),
                        color = TextSecondary.copy(alpha = 0.5f),
                        letterSpacing = 1.sp,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(history.size) { index ->
                        val session = history[index]
                        val caseNumber = history.size - index
                        HistoryItem(
                            session = session, 
                            caseNumber = caseNumber,
                            onClick = { onNavigateToDetail(session.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(session: GameSession, caseNumber: Int, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val dateString = remember(session.timestamp) { dateFormat.format(Date(session.timestamp)) }
    
    Surface(
        onClick = onClick,
        color = SurfaceCard,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = PrimaryCyan.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "#$caseNumber",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = PrimaryCyan,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (session.isWin) stringResource(R.string.mission_accomplished).uppercase() 
                                   else stringResource(R.string.mission_failed).uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (session.isWin) SuccessGreen else ErrorRed,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary.copy(alpha = 0.6f)
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = session.totalScore.toString(),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryCyan
                    )
                    Text(
                        text = stringResource(R.string.final_score).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary.copy(alpha = 0.4f),
                        fontSize = 8.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Level summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                session.levels.forEach { level ->
                    LevelBadge(levelNumber = level.levelNumber)
                }
            }
        }
    }
}

@Composable
fun LevelBadge(levelNumber: Int) {
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
            .border(1.dp, PrimaryCyan.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "LVL $levelNumber",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontSize = 9.sp
        )
    }
}
