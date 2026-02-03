package com.example.geofenceapp.ui.map

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@Composable
fun MapScreen(
    onOpenHistory: () -> Unit
) {
    /* ----------------------------- State ----------------------------- */

    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var geofenceName by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf(50f) }

    var geofenceToDelete by remember { mutableStateOf<GeofenceEntity?>(null) }

    /* -------------------------- Dependencies -------------------------- */

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val database = remember { GeoDatabase.getInstance(context) }
    val geofenceDao = database.geofenceDao()
    val geofenceManager = remember { GeofenceManager(context) }

    val geofences by geofenceDao.getAllGeofences()
        .collectAsState(initial = emptyList())

    val cameraState = rememberCameraPositionState()
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    /* ----------------------- Location Handling ------------------------ */

    LaunchedEffect(Unit) {
        if (
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return@LaunchedEffect

        if (!isLocationEnabled(context)) {
            openLocationSettings(context)
            return@LaunchedEffect
        }

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    cameraState.position = CameraPosition.fromLatLngZoom(
                        LatLng(it.latitude, it.longitude),
                        16f
                    )
                }
            }
        } catch (_: SecurityException) {
        }
    }

    /* ------------------------------ UI ------------------------------- */

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

                Circle(
                    center = center,
                    radius = geofence.radius,
                    strokeColor = Color(0xFF1A73E8),
                    fillColor = Color(0x331A73E8),
                    strokeWidth = 4f
                )

                Marker(
                    state = MarkerState(center),
                    onClick = {
                        geofenceToDelete = geofence
                        showDeleteDialog = true
                        true
                    }
                )

                val labelPosition = LatLng(
                    geofence.lat + (geofence.radius / 111_000.0),
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

    /* ------------------------- Add Dialog ---------------------------- */

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

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Radius: ${radius.toInt()} meters")
                    Slider(
                        value = radius,
                        onValueChange = { radius = it },
                        valueRange = 10f..50f
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val location = selectedLocation ?: return@TextButton
                        val name = geofenceName.trim()
                        if (name.isBlank()) return@TextButton

                        geofenceManager.addGeofence(
                            id = name,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            radius = radius
                        )

                        scope.launch {
                            geofenceDao.insert(
                                GeofenceEntity(
                                    name = name,
                                    lat = location.latitude,
                                    longitude = location.longitude,
                                    radius = radius.toDouble()
                                )
                            )

                            database.visitDao().insertVisit(
                                VisitEntity(
                                    geofenceName = name,
                                    entryTime = System.currentTimeMillis(),
                                    exitTime = System.currentTimeMillis(),
                                    durationMillis = 0
                                )
                            )
                        }

                        geofenceName = ""
                        showAddDialog = false
                    }
                ) {
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

    /* ----------------------- Delete Dialog --------------------------- */

    if (showDeleteDialog && geofenceToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Remove Geofence") },
            text = { Text("Do you want to remove this geofence?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val geofence = geofenceToDelete ?: return@TextButton

                        scope.launch {
                            geofenceDao.deleteGeofence(geofence.id)
                        }

                        geofenceManager.removeGeofence(geofence.name)

                        geofenceToDelete = null
                        showDeleteDialog = false
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        geofenceToDelete = null
                        showDeleteDialog = false
                    }
                ) {
                    Text("No")
                }
            }
        )
    }
}

/* ----------------------- Helper Functions ---------------------------- */

fun isLocationEnabled(context: Context): Boolean {
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

fun openLocationSettings(context: Context) {
    context.startActivity(
        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    )
}
