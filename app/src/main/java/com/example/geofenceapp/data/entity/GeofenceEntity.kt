package com.example.geofenceapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "geofences")
data class GeofenceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val lat: Double,
    val longitude: Double,
    val radius: Double,
    val createdAt: Long = System.currentTimeMillis()
)