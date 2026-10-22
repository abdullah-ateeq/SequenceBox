package com.example.sequencebox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.sequencebox.ui.SequenceBoxViewModel
import com.example.sequencebox.ui.navigation.SequenceBoxNavGraph
import com.example.sequencebox.ui.theme.SequenceBoxTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SequenceBoxViewModel by viewModels {
        SequenceBoxViewModel.provideFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsState()
            SequenceBoxTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor
            ) {
                SequenceBoxNavGraph(viewModel = viewModel)
            }
        }
    }
}
