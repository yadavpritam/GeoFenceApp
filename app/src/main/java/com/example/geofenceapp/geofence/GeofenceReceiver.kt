package com.example.geofenceapp.geofence

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.geofenceapp.data.GeoDatabase
import com.example.geofenceapp.data.entity.VisitEntity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        Log.d(TAG, "GeofenceReceiver triggered")

        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) return

        val transition = event.geofenceTransition
        val geofenceId = event.triggeringGeofences
            ?.firstOrNull()
            ?.requestId
            ?: return

        val preferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val currentTime = System.currentTimeMillis()

        when (transition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                preferences.edit()
                    .putLong(entryKey(geofenceId), currentTime)
                    .apply()

                showNotification(
                    context,
                    "Entered $geofenceId"
                )
            }

            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                val entryTime =
                    preferences.getLong(entryKey(geofenceId), -1)

                if (entryTime != -1L) {
                    val duration = currentTime - entryTime

                    saveVisit(
                        context = context,
                        geofenceId = geofenceId,
                        entryTime = entryTime,
                        exitTime = currentTime,
                        duration = duration
                    )

                    preferences.edit()
                        .remove(entryKey(geofenceId))
                        .apply()

                    showNotification(
                        context,
                        "Exited $geofenceId after ${duration / 1000}s"
                    )
                }
            }
        }
    }

    /* ------------------------ Data -------------------------------- */

    private fun saveVisit(
        context: Context,
        geofenceId: String,
        entryTime: Long,
        exitTime: Long,
        duration: Long
    ) {
        val database = GeoDatabase.getInstance(context)

        CoroutineScope(Dispatchers.IO).launch {
            database.visitDao().insertVisit(
                VisitEntity(
                    geofenceName = geofenceId,
                    entryTime = entryTime,
                    exitTime = exitTime,
                    durationMillis = duration
                )
            )
        }
    }

    /* --------------------- Notifications --------------------------- */

    private fun showNotification(
        context: Context,
        message: String
    ) {
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Geofence Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentTitle("Geofence Alert")
            .setContentText(message)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    /* ------------------------ Utils ------------------------------- */

    private fun entryKey(id: String): String = "entry_$id"

    companion object {
        private const val TAG = "GeofenceReceiver"
        private const val CHANNEL_ID = "geofence_channel"
        private const val PREFS_NAME = "geo_prefs"
    }
}
