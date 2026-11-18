package com.example.alchemygarden

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alchemygarden.ui.navigation.GameMainScaffold
import com.example.alchemygarden.ui.theme.AlchemyGardenTheme
import com.example.alchemygarden.ui.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            AlchemyGardenTheme {
                val gameViewModel: GameViewModel = viewModel()
                GameMainScaffold(viewModel = gameViewModel)
            }
        }
    }
}
