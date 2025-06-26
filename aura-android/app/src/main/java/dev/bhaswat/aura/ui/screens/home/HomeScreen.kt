package dev.bhaswat.aura.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import dev.bhaswat.aura.R
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import dev.bhaswat.aura.ui.theme.AccentBlue
import dev.bhaswat.aura.ui.theme.AppBackground
import dev.bhaswat.aura.ui.theme.AuraTheme
import dev.bhaswat.aura.ui.theme.CardBackground
import dev.bhaswat.aura.ui.theme.ChipBorder
import dev.bhaswat.aura.ui.theme.PrimaryText
import dev.bhaswat.aura.ui.theme.SecondaryText
import dev.bhaswat.aura.ui.theme.SliderTrack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    // The viewModel is provided to the homeScreen
    homeViewModel: HomeViewModel,
    onNavigateToPlan: () -> Unit // This lambda will trigger navigation
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val errorMessage by homeViewModel.errorState.collectAsStateWithLifecycle()


    LaunchedEffect(Unit) {
        homeViewModel.navigationEvent.collect {
            onNavigateToPlan()
        }
    }


    // Main background for the entire screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // The main white card
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(CardBackground)
                .padding(24.dp)
        ) {
            // Top Bar: "Aura" and Help Icon
            Row(
                modifier = Modifier.fillMaxWidth() ,
                horizontalArrangement = Arrangement.Center ,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aura" ,
                    style = MaterialTheme.typography.titleMedium ,
                    fontWeight = FontWeight.Bold ,
                    color = PrimaryText ,
                    modifier = Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline ,
                    contentDescription = "Help" ,
                    tint = SecondaryText
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // -- What do you want to learn? --
            Text(
                "What do you want to learn?" ,
                style = MaterialTheme.typography.titleLarge ,
                fontWeight = FontWeight.Bold ,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.topic ,
                onValueChange = homeViewModel::onTopicChange ,
                placeholder = { Text("e.g., Learn to code" , color = SecondaryText) } ,
                modifier = Modifier.fillMaxWidth() ,
                shape = RoundedCornerShape(12.dp) ,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlue ,
                    unfocusedBorderColor = ChipBorder ,
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // -- How many hours per week? --
            Text(
                "How many hours per week?" ,
                style = MaterialTheme.typography.titleLarge ,
                fontWeight = FontWeight.Bold ,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Hours per week" , color = PrimaryText , modifier = Modifier.weight(1f))
                Text(
                    "${uiState.hours.toInt()}" ,
                    color = PrimaryText ,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = uiState.hours ,
                onValueChange = homeViewModel::onHoursChange ,
                valueRange = 1f..10f ,
                steps = 8 ,
                colors = SliderDefaults.colors(
                    thumbColor = AccentBlue ,
                    activeTrackColor = AccentBlue ,
                    inactiveTrackColor = SliderTrack
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // -- What's your learning style? --
            Text(
                "What's your learning style?" ,
                style = MaterialTheme.typography.titleLarge ,
                fontWeight = FontWeight.Bold ,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StyleChip(
                    label = "Video tutorials" ,
                    isSelected = uiState.selectedStyle == "Video tutorials" ,
                    onSelected = { homeViewModel.onStyleChange("Video tutorials") })
                StyleChip(
                    label = "Reading articles" ,
                    isSelected = uiState.selectedStyle == "Reading articles" ,
                    onSelected = { homeViewModel.onStyleChange("Reading articles") })
            }
            Spacer(modifier = Modifier.height(8.dp))
            StyleChip(
                label = "Interactive exercises" ,
                isSelected = uiState.selectedStyle == "Interactive exercises" ,
                onSelected = { homeViewModel.onStyleChange("Interactive exercises") })


            Spacer(modifier = Modifier.weight(1f)) // Pushes button to the bottom

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // -- Create My Plan Button --
            Button(
                onClick = homeViewModel::onCreatePlanClick ,
                enabled = uiState.topic.isNotBlank() ,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp) ,
                shape = RoundedCornerShape(16.dp) ,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text(
                    text = "Create my plan" ,
                    color = PrimaryText ,
                    fontWeight = FontWeight.Bold ,
                    fontSize = 16.sp
                )
            }
        }

        AnimatedVisibility(
            visible = uiState.isLoading ,
            enter = fadeIn() ,
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)) ,
                contentAlignment = Alignment.Center
            ) {
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.ai_loader))
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever, // Loop the animation
                    modifier = Modifier.size(200.dp)
                )
            }
        }
    }
}

// Helper composable for the styled chips
@Composable
fun StyleChip(label: String, isSelected: Boolean, onSelected: () -> Unit) {
    // We use a Surface, which is a basic building block for UI sections.
    Surface(
        // The onClick logic is now handled by this modifier
        modifier = Modifier.clickable(onClick = onSelected),
        shape = RoundedCornerShape(12.dp),
        // We manually control the color based on the 'isSelected' state
        color = if (isSelected) AccentBlue else Color.Transparent,
        // We manually control the border as well
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) Color.Transparent else ChipBorder
        )
    ) {
        // The Text is placed inside the Surface
        Text(
            text = label,
            color = if (isSelected) Color.White else PrimaryText,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 14.sp
        )
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AuraTheme {

    }
}