package com.brainfocus.numberdetective.feature.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.core.designsystem.*
import com.brainfocus.numberdetective.feature.home.RowDefaults
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 7 })
    val scope = rememberCoroutineScope()

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

        // --- Layer 3: Pager Content ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.tutorial_title).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryCyan.copy(alpha = 0.7f),
                letterSpacing = 4.sp,
                modifier = Modifier.padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                TutorialCard(page)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Page Indicators
            Row(
                Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration) PrimaryCyan else Color.White.copy(alpha = 0.2f)
                    val width = if (pagerState.currentPage == iteration) 24.dp else 8.dp
                    
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .width(width)
                            .height(8.dp)
                            .animateContentSize()
                    )
                }
            }

            // Navigation Buttons
            val isLastPage = pagerState.currentPage == pagerState.pageCount - 1
            
            Button(
                onClick = {
                    if (isLastPage) {
                        viewModel.completeOnboarding()
                        onFinish()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(20.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (isLastPage) PlayButtonGradient else androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.1f))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isLastPage) stringResource(R.string.tutorial_finish_button).uppercase() else stringResource(R.string.tutorial_continue_button).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- Layer 4: Close Button ---
        IconButton(
            onClick = onFinish,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
                .statusBarsPadding()
                .size(44.dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                .border(1.dp, PrimaryCyan.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Manual",
                tint = PrimaryCyan.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun TutorialCard(page: Int) {
    val title = when (page) {
        0 -> R.string.tutorial_page1_title
        1 -> R.string.tutorial_page2_title
        2 -> R.string.tutorial_page3_title
        3 -> R.string.tutorial_page4_title
        4 -> R.string.tutorial_page5_title
        5 -> R.string.tutorial_page6_title
        else -> R.string.tutorial_page7_title
    }
    
    val description = when (page) {
        0 -> R.string.tutorial_page1_desc
        1 -> R.string.tutorial_page2_desc
        2 -> R.string.tutorial_page3_desc
        3 -> R.string.tutorial_page4_desc
        4 -> R.string.tutorial_page5_desc
        5 -> R.string.tutorial_page6_desc
        else -> R.string.tutorial_page7_desc
    }

    val icon = when (page) {
        0 -> "🎯"
        1 -> "🔒"
        2 -> "📂"
        3 -> "🎨"
        4 -> "🚀"
        5 -> "🎖️"
        else -> "⚖️"
    }

    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(horizontal = 8.dp),
        border = RowDefaults.CardBorder
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                    .border(2.dp, PrimaryCyan.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 48.sp)
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = stringResource(title).uppercase(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = PrimaryCyan,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(description),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )
        }
    }
}
