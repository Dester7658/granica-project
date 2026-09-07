package com.granica.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.granica.app.ui.navigation.GranicaNavGraph
import com.granica.app.ui.theme.GranicaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as GranicaApp).repository
        setContent {
            GranicaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GranicaNavGraph(repository = repository)
                }
            }
        }
    }
}
