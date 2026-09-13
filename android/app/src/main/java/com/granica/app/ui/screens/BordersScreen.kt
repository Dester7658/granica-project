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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.granica.app.data.model.BorderSummaryDto
import com.granica.app.ui.UiState
import com.granica.app.ui.components.GranicaHeader
import com.granica.app.ui.countryFlagEmoji
import com.granica.app.ui.viewmodel.BordersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BordersScreen(
    countryName: String,
    countryCode: String,
    viewModel: BordersViewModel,
    onBorderClick: (BorderSummaryDto) -> Unit,
    onBack: () -> Unit,
    localizedOtherCountryName: (String, String) -> String = { _, fallback -> fallback }
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            GranicaHeader(
                title = "${countryFlagEmoji(countryCode)}  $countryName",
                subtitle = "Выберите границу",
                onBack = onBack
            )
        }
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

            is UiState.Success -> if (s.data.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Пока нет проверенных камер для границ этой страны",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Мы добавляем только реальные, проверенные источники — эта страна ещё в очереди на добавление.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(s.data) { border ->
                        BorderCard(
                            countryCode = countryCode,
                            border = border,
                            localizedOtherCountryName = localizedOtherCountryName(border.otherCountryCode, border.otherCountryName),
                            onClick = { onBorderClick(border) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BorderCard(countryCode: String, border: BorderSummaryDto, localizedOtherCountryName: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "${countryFlagEmoji(countryCode)} ↔ ${countryFlagEmoji(border.otherCountryCode)}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "$localizedOtherCountryName · ${border.crossingCount} переходов",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
