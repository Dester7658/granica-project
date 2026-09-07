package com.granica.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.granica.app.ui.theme.GradientEnd
import com.granica.app.ui.theme.GradientStart

/** Reusable gradient header used instead of a flat grey TopAppBar on every screen. */
@Composable
fun GranicaHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(GradientStart, GradientEnd)))
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
                }
            }
            Column(modifier = Modifier.padding(start = if (onBack == null) 0.dp else 4.dp)) {
                Text(title, color = Color.White, style = MaterialTheme.typography.headlineSmall)
                if (subtitle != null) {
                    Text(subtitle, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        actions()
    }
}
