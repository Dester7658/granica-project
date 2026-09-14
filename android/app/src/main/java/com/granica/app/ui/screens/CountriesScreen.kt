package com.granica.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.granica.app.AppSettings
import com.granica.app.data.model.CountryDto
import com.granica.app.ui.UiState
import com.granica.app.ui.countryFlagEmoji
import com.granica.app.ui.viewmodel.CountriesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountriesScreen(
    settings: AppSettings,
    viewModel: CountriesViewModel,
    onCountryClick: (CountryDto) -> Unit,
    localizedCountryName: (String, String) -> String = { code, fallback -> fallback }
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("🟢", "🟡", "🔵").forEach { dot ->
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.22f),
                                modifier = Modifier.size(18.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(dot, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                    Text(
                        "BorderWatcher",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.18f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("?", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
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

            is UiState.Success -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Welcome",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
                }
                item {
                    Text(
                        "Travel with BorderWatcher and share\nyour experiences with us!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 34.sp
                    )
                }
                item {
                    Surface(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                        shape = RoundedCornerShape(26.dp),
                        color = Color(0xFFE7EAF4)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Where do you start from?",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Text(
                        "Favorites",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                items(s.data) { country ->
                    CountryCard(
                        settings = settings,
                        country = country,
                        countryName = localizedCountryName(country.code, country.name),
                        onClick = { onCountryClick(country) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CountryCard(
    settings: AppSettings,
    country: CountryDto,
    countryName: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFEEF3FF), Color(0xFFE9F8F6))))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(countryFlagEmoji(country.code), style = MaterialTheme.typography.headlineSmall)
                }
                Column(modifier = Modifier.padding(start = 14.dp)) {
                    Text(
                        countryName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Vignette",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                "☆",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

