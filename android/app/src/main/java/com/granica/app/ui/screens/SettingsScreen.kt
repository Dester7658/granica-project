package com.granica.app.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.granica.app.AppSettings
import com.granica.app.ui.components.GranicaHeader

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var disableCameras by remember { mutableStateOf(settings.disableCameras) }
    var darkMode by remember { mutableStateOf(settings.darkModeEnabled) }
    var selectedLanguage by remember { mutableStateOf(settings.selectedLanguage) }
    var pendingLanguage by remember { mutableStateOf(settings.selectedLanguage) }
    var pendingDisableCameras by remember { mutableStateOf(settings.disableCameras) }
    var pendingDarkMode by remember { mutableStateOf(settings.darkModeEnabled) }
    val hasChanges = pendingLanguage != selectedLanguage || pendingDisableCameras != disableCameras || pendingDarkMode != darkMode

    Scaffold(
        topBar = {
            GranicaHeader(
                title = settings.uiText("settings"),
                subtitle = settings.uiText("settings_subtitle"),
                onBack = onBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(settings.uiText("toggle_cameras"), style = MaterialTheme.typography.titleMedium)
                            Text(
                                settings.uiText("cameras_hint"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = pendingDisableCameras,
                            onCheckedChange = { pendingDisableCameras = it }
                        )
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(settings.uiText("dark_mode"), style = MaterialTheme.typography.titleMedium)
                            Text(
                                settings.uiText("dark_mode_hint"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = pendingDarkMode,
                            onCheckedChange = { pendingDarkMode = it }
                        )
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(settings.uiText("language"), style = MaterialTheme.typography.titleMedium)
                        Text(
                            settings.uiText("language_hint"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(settings.availableLanguages()) { language ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    onClick = {
                        pendingLanguage = language.code
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(language.label)
                        if (pendingLanguage == language.code) {
                            Text("✓", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        selectedLanguage = pendingLanguage
                        disableCameras = pendingDisableCameras
                        darkMode = pendingDarkMode
                        settings.selectedLanguage = pendingLanguage
                        settings.disableCameras = pendingDisableCameras
                        settings.darkModeEnabled = pendingDarkMode
                        settings.applySelectedLanguage(context)
                        (context as? Activity)?.recreate()
                    },
                    enabled = hasChanges,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasChanges) MaterialTheme.colorScheme.primary else Color.Gray,
                        disabledContainerColor = Color.Gray
                    )
                ) {
                    Text(settings.uiText("apply"))
                }
            }
        }
    }
}
