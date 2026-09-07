package com.granica.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_crossings")
data class FavoriteCrossingEntity(
    @PrimaryKey val crossingId: String,
    val name: String,
    val countryCode: String,
    val addedAtMillis: Long = System.currentTimeMillis()
)
