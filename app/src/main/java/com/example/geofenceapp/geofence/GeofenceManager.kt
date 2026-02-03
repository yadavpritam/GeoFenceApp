package com.example.geofenceapp.geofence

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class GeofenceManager(
    private val context: Context
) {

    /* ------------------------ Client ------------------------------- */

    private val geofencingClient: GeofencingClient =
        LocationServices.getGeofencingClient(context)

    /* --------------------- PendingIntent --------------------------- */

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceReceiver::class.java)

        PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_GEOFENCE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /* ----------------------- Public API ---------------------------- */

    @SuppressLint("MissingPermission")
    fun addGeofence(
        id: String,
        latitude: Double,
        longitude: Double,
        radius: Float
    ) {
        val geofence = Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(latitude, longitude, radius)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or
                        Geofence.GEOFENCE_TRANSITION_EXIT
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(
            request,
            geofencePendingIntent
        )
    }

    fun removeGeofence(id: String) {
        geofencingClient.removeGeofences(listOf(id))
    }

    /* ------------------------ Constants ---------------------------- */

    companion object {
        private const val REQUEST_CODE_GEOFENCE = 1001
    }
}
