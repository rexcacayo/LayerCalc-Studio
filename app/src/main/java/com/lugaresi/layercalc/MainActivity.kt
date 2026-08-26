package com.lugaresi.layercalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.lugaresi.layercalc.ui.screens.DashboardScreen
import com.lugaresi.layercalc.ui.screens.welcome.WelcomeScreen
import com.lugaresi.layercalc.ui.theme.LayerCalcStudioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LayerCalcStudioTheme {
                // Control de navegación entre la Portada 3D y el Dashboard
                var showDashboard by remember { mutableStateOf(false) }

                if (!showDashboard) {
                    WelcomeScreen(
                        onEnterClick = { showDashboard = true }
                    )
                } else {
                    DashboardScreen()
                }
            }
        }
    }
}