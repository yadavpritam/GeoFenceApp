package com.example.geofenceapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.geofenceapp.data.dao.GeofenceDao
import com.example.geofenceapp.data.dao.VisitDao
import com.example.geofenceapp.data.entity.GeofenceEntity
import com.example.geofenceapp.data.entity.VisitEntity

@Database(
    entities = [GeofenceEntity::class, VisitEntity::class],
    version = 2
)
abstract class GeoDatabase : RoomDatabase() {

    abstract fun geofenceDao(): GeofenceDao
    abstract fun visitDao(): VisitDao

    companion object {
        @Volatile
        private var INSTANCE: GeoDatabase? = null

        fun getInstance(context: Context): GeoDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    GeoDatabase::class.java,
                    "geofence_db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
