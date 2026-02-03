package com.example.geofenceapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.geofenceapp.ui.history.VisitHistoryScreen
import com.example.geofenceapp.ui.map.MapScreen
import com.example.geofenceapp.ui.theme.GeoFenceAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ✅ Runtime permission request
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                101
            )
        }

        setContent {
            GeoFenceAppTheme {



                var showHistory by remember { mutableStateOf(false) }


                if (showHistory) {
                    VisitHistoryScreen(
                        onBack = { showHistory = false }
                    )
                } else {
                    MapScreen(
                        onOpenHistory = { showHistory = true }
                    )
                }
            }
        }
    }
}
