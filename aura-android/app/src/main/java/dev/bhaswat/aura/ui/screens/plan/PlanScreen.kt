package dev.bhaswat.aura.ui.screens.plan

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import dev.bhaswat.aura.network.LearningModule
import dev.bhaswat.aura.network.LearningPlanResponse
import dev.bhaswat.aura.network.Resource
import dev.bhaswat.aura.ui.theme.AccentBlue
import dev.bhaswat.aura.ui.theme.AppBackground
import dev.bhaswat.aura.ui.theme.AuraTheme
import dev.bhaswat.aura.ui.theme.CardBackground
import dev.bhaswat.aura.ui.theme.PrimaryText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(
    plan: LearningPlanResponse // The screen takes the generated plan as a parameter
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(plan.planTitle, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBackground,
                    titleContentColor = PrimaryText
                )
            )
        }
    ) { paddingValues ->
        // LazyColumn is an efficient way to display a long, scrollable list
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackground)
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Loop through each module in the plan
            items(plan.modules) { module ->
                ModuleCard(module = module)
            }
        }
    }
}

@Composable
fun ModuleCard(module: LearningModule) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Week ${module.week}: ${module.topic}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Loop through each resource in the module
            module.resources.forEach { resource ->
                ResourceItem(resource = resource)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ResourceItem(resource: Resource) {

    val context = LocalContext.current

    val icon = when (resource.type.lowercase()) {
        "video" -> Icons.Default.PlayCircleOutline
        else -> Icons.AutoMirrored.Filled.Article
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, resource.url.toUri())
                context.startActivity(intent)
                }
            .padding(vertical = 8.dp)
    )
     {
        Icon(
            imageVector = icon,
            contentDescription = resource.type,
            tint = AccentBlue,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = resource.title,
            style = MaterialTheme.typography.bodyLarge,
            color = PrimaryText
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PlanScreenPreview() {
    // Create a dummy plan for the preview
    val dummyPlan = LearningPlanResponse(
        planTitle = "Your Custom Plan for Jetpack Compose" ,
        modules = listOf(
            LearningModule(
                week = 1 ,
                topic = "The Basics of Compose" ,
                resources = listOf(
                    Resource(title = "Thinking in Compose" , url = "" , type = "Article") ,
                    Resource(title = "Layouts in Compose" , url = "" , type = "Video")
                )
            ) ,
            LearningModule(
                week = 2 ,
                topic = "State Management" ,
                resources = listOf(
                    Resource(title = "State and Recomposition" , url = "" , type = "Article") ,
                    Resource(title = "Understanding ViewModels" , url = "" , type = "Video")
                )
            )
        )
    )

    AuraTheme {
        PlanScreen(plan = dummyPlan)
    }
}