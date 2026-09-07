package com.granica.app.data.repository

import com.granica.app.data.api.GranicaApiService
import com.granica.app.data.local.FavoriteCrossingDao
import com.granica.app.data.local.FavoriteCrossingEntity
import com.granica.app.data.model.BorderDto
import com.granica.app.data.model.BorderSummaryDto
import com.granica.app.data.model.CountryDto
import com.granica.app.data.model.CrossingDto
import kotlinx.coroutines.flow.Flow

class GranicaRepository(
    private val api: GranicaApiService,
    private val favoritesDao: FavoriteCrossingDao
) {
    suspend fun getCountries(): List<CountryDto> = api.getCountries()

    suspend fun getBorders(countryCode: String): List<BorderSummaryDto> = api.getBorders(countryCode)

    suspend fun getBorder(borderId: String): BorderDto = api.getBorder(borderId)

    suspend fun getCrossing(id: String): CrossingDto = api.getCrossing(id)

    fun observeFavorites(): Flow<List<FavoriteCrossingEntity>> = favoritesDao.observeAll()

    fun observeIsFavorite(crossingId: String): Flow<Boolean> =
        favoritesDao.observeIsFavorite(crossingId)

    suspend fun toggleFavorite(crossing: CrossingDto, countryCode: String, isCurrentlyFavorite: Boolean) {
        if (isCurrentlyFavorite) {
            favoritesDao.deleteById(crossing.id)
        } else {
            favoritesDao.insert(
                FavoriteCrossingEntity(
                    crossingId = crossing.id,
                    name = crossing.name,
                    countryCode = countryCode
                )
            )
        }
    }
}
