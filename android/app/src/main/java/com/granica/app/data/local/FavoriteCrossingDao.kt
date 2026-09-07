package com.granica.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteCrossingDao {

    @Query("SELECT * FROM favorite_crossings ORDER BY addedAtMillis DESC")
    fun observeAll(): Flow<List<FavoriteCrossingEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_crossings WHERE crossingId = :crossingId)")
    fun observeIsFavorite(crossingId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteCrossingEntity)

    @Delete
    suspend fun delete(favorite: FavoriteCrossingEntity)

    @Query("DELETE FROM favorite_crossings WHERE crossingId = :crossingId")
    suspend fun deleteById(crossingId: String)
}
