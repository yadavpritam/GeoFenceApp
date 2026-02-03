package com.example.geofenceapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.geofenceapp.data.entity.GeofenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GeofenceDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(geofenceEntity: GeofenceEntity)

    @Query("SELECT * FROM geofences")
    fun getAllGeofences(): Flow<List<GeofenceEntity>>

    @Query("DELETE FROM geofences WHERE id = :id")
    suspend fun deleteGeofence(id: Int)

}