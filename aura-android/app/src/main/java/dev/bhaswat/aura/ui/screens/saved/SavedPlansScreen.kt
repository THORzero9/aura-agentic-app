package dev.bhaswat.aura.ui.screens.saved

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.bhaswat.aura.network.LearningPlanResponse
import dev.bhaswat.aura.ui.theme.CardBackground
import dev.bhaswat.aura.ui.theme.PrimaryText
import dev.bhaswat.aura.ui.theme.SecondaryText


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPlansScreen(
    // We get an instance of our new ViewModel
    savedPlansViewModel: SavedPlansViewModel = viewModel(),
    // We no longer pass the list, but we still need navigation callbacks
    onPlanClicked: (LearningPlanResponse) -> Unit,
    onNavigateBack: () -> Unit
) {
    // Collect the state from the ViewModel
    val uiState by savedPlansViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Saved Plans", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // If it's loading, show a spinner
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (uiState.savedPlans.isEmpty()) {
                // If there are no plans, show a message
                Text("You have no saved plans yet.", style = MaterialTheme.typography.bodyLarge)
            } else {
                // Otherwise, show the list of plans
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.savedPlans) { plan ->
                        SavedPlanItem(plan = plan, onClick = { onPlanClicked(plan) })
                    }
                }
            }
        }
    }
}

@Composable
fun SavedPlanItem(plan: LearningPlanResponse , onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plan.planTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
                Text(
                    text = "${plan.modules.size} weeks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "View Plan",
                tint = SecondaryText
            )
        }
    }
}

