package com.granica.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FavoriteCrossingEntity::class], version = 1, exportSchema = false)
abstract class GranicaDatabase : RoomDatabase() {

    abstract fun favoriteCrossingDao(): FavoriteCrossingDao

    companion object {
        @Volatile
        private var instance: GranicaDatabase? = null

        fun getInstance(context: Context): GranicaDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    GranicaDatabase::class.java,
                    "granica.db"
                ).build().also { instance = it }
            }
    }
}
