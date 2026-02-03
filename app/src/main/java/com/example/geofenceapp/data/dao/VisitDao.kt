package com.example.geofenceapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.geofenceapp.data.entity.VisitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitDao {

    @Insert
    suspend fun insertVisit(visit: VisitEntity)

    @Query("SELECT * FROM visits ORDER BY id DESC")
    fun getAllVisits(): Flow<List<VisitEntity>>
}