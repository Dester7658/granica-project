package com.granica.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.granica.app.data.model.CrossingDto
import com.granica.app.ui.UiState
import com.granica.app.ui.components.GranicaHeader
import com.granica.app.ui.viewmodel.CrossingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrossingsScreen(
    title: String,
    viewModel: CrossingsViewModel,
    onCrossingClick: (CrossingDto) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { GranicaHeader(title = title, subtitle = "Переходы и камеры", onBack = onBack) }
    ) { padding ->
        when (val s = state) {
            is UiState.Loading -> Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) { CircularProgressIndicator() }

            is UiState.Error -> ErrorState(
                message = s.message,
                modifier = Modifier.fillMaxSize().padding(padding),
                onRetry = { viewModel.load() }
            )

            is UiState.Success -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(s.data) { crossing ->
                    CrossingCard(crossing = crossing, onClick = { onCrossingClick(crossing) })
                }
            }
        }
    }
}

@Composable
private fun CrossingCard(crossing: CrossingDto, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(crossing.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${crossing.cameras.size} камер",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            crossing.waitMinutes?.let { WaitBadge(it) }
        }
    }
}

@Composable
fun WaitBadge(minutes: Int) {
    val color = when {
        minutes <= 10 -> Color(0xFF2E7D32)
        minutes <= 30 -> Color(0xFFF9A825)
        else -> Color(0xFFC62828)
    }
    Surface(color = color, shape = RoundedCornerShape(10.dp)) {
        Text(
            "$minutes мин",
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

