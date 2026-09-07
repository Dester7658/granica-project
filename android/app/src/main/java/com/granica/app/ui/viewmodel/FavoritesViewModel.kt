package com.granica.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.granica.app.data.local.FavoriteCrossingEntity
import com.granica.app.data.repository.GranicaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class FavoritesViewModel(repository: GranicaRepository) : ViewModel() {

    val favorites: StateFlow<List<FavoriteCrossingEntity>> = repository.observeFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    class Factory(private val repository: GranicaRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            FavoritesViewModel(repository) as T
    }
}
