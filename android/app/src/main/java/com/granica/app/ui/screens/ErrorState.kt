package com.granica.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.granica.app.R

@Composable
fun ErrorState(message: String, modifier: Modifier = Modifier, onRetry: () -> Unit) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.error_loading))
        Text(message)
        Button(onClick = onRetry, modifier = Modifier.padding(top = 12.dp)) {
            Text(stringResource(R.string.retry))
        }
    }
}
