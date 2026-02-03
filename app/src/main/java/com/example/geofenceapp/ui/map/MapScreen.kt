package com.example.geofenceapp.ui.map

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.geofenceapp.data.GeoDatabase
import com.example.geofenceapp.data.entity.GeofenceEntity
import com.example.geofenceapp.data.entity.VisitEntity
import com.example.geofenceapp.geofence.GeofenceManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch


@Composable
fun MapScreen(
    onOpenHistory: () -> Unit,
) {
    // ───────────── State ─────────────
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var geofenceName by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf(50f) }

    var geofenceToDelete by remember { mutableStateOf<GeofenceEntity?>(null) }

    // ───────────── Dependencies ─────────────
    val context = LocalContext.current
    val database = remember { GeoDatabase.getInstance(context) }
    val geofenceDao = database.geofenceDao()
    val geofenceManager = remember { GeofenceManager(context) }
    val scope = rememberCoroutineScope()

    val geofences by geofenceDao.getAllGeofences()
        .collectAsState(initial = emptyList())

    val cameraState = rememberCameraPositionState()
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    LaunchedEffect(Unit) {

        // 1️⃣ Permission check
        if (
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return@LaunchedEffect
        }

        // 2️⃣ Location (GPS) ON/OFF check
        if (!isLocationEnabled(context)) {
            openLocationSettings(context)
            return@LaunchedEffect
        }

        // 3️⃣ Location ON → live location lo
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        cameraState.position = CameraPosition.fromLatLngZoom(
                            LatLng(it.latitude, it.longitude),
                            16f
                        )
                    }
                }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }


    // ───────────── UI ─────────────
    Column(modifier = Modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.weight(1f),
            cameraPositionState = cameraState,
            onMapLongClick = {
                selectedLocation = it
                showAddDialog = true
            }
        ) {
            geofences.forEach { geofence ->
                val center = LatLng(geofence.lat, geofence.longitude)

                // 🔵 Circle
                Circle(
                    center = center,
                    radius = geofence.radius,
                    strokeColor = Color(0xFF1A73E8),
                    fillColor = Color(0x331A73E8),
                    strokeWidth = 4f
                )

                // 📍 CENTER MARKER (DELETE ENABLED ✅)
                Marker(
                    state = MarkerState(center),
                    onClick = {
                        geofenceToDelete = geofence
                        showDeleteDialog = true
                        true
                    }
                )

                // ✍️ Label outside circle
                val labelPosition = LatLng(
                    geofence.lat + (geofence.radius / 111000.0),
                    geofence.longitude
                )

                val labelState = rememberMarkerState(position = labelPosition)

                Marker(
                    state = labelState,
                    title = geofence.name,
                    alpha = 0f
                )

                LaunchedEffect(geofence.id, cameraState.isMoving) {
                    if (!cameraState.isMoving) {
                        labelState.showInfoWindow()
                    }
                }

            }


        }

        Button(
            onClick = onOpenHistory,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text("View Visit History")
        }
    }

    // ───────────── Add Geofence Dialog ─────────────
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Geofence") },
            text = {
                Column {
                    OutlinedTextField(
                        value = geofenceName,
                        onValueChange = { geofenceName = it },
                        label = { Text("Location Name") }
                    )

                    Spacer(Modifier.height(16.dp))

                    Text("Radius: ${radius.toInt()} meters")
                    Slider(
                        value = radius,
                        onValueChange = { radius = it },
                        valueRange = 10f..50f
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val location = selectedLocation ?: return@TextButton

                    val nameToSave = geofenceName.trim()
                    if (nameToSave.isBlank()) return@TextButton

                    geofenceManager.addGeofence(
                        id = nameToSave,
                        lat = location.latitude,
                        lng = location.longitude,
                        radius = radius
                    )

                    scope.launch {
                        geofenceDao.insert(
                            GeofenceEntity(
                                name = nameToSave,   // ✅ FIXED
                                lat = location.latitude,
                                longitude = location.longitude,
                                radius = radius.toDouble()
                            )
                        )

                        database.visitDao().insertVisit(
                            VisitEntity(
                                geofenceName = nameToSave,   // ✅ FIXED
                                entryTime = System.currentTimeMillis() - 300_000,
                                exitTime = System.currentTimeMillis(),
                                durationMillis = 300_000
                            )
                        )
                    }

                    geofenceName = ""        // ✅ ab safe hai
                    showAddDialog = false
                }) {
                    Text("Save")
                }

            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ───────────── Delete Confirmation ─────────────
    if (showDeleteDialog && geofenceToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Remove Geofence") },
            text = { Text("Do you want to remove this geofence?") },
            confirmButton = {
                TextButton(onClick = {
                    val geofence = geofenceToDelete!!

                    scope.launch {
                        geofenceDao.deleteGeofence(geofence.id)
                    }

                    geofenceManager.removeGeofence(geofence.name)

                    geofenceToDelete = null
                    showDeleteDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    geofenceToDelete = null
                    showDeleteDialog = false
                }) {
                    Text("No")
                }
            }
        )
    }
}

fun isLocationEnabled(context: Context): Boolean {
    val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

fun openLocationSettings(context: Context) {
    context.startActivity(
        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    )
}
