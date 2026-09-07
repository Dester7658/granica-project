package com.granica.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.granica.app.data.model.CrossingDto
import com.granica.app.data.repository.GranicaRepository
import com.granica.app.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CrossingDetailViewModel(
    private val repository: GranicaRepository,
    private val crossingId: String,
    private val countryCode: String
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<CrossingDto>>(UiState.Loading)
    val state: StateFlow<UiState<CrossingDto>> = _state.asStateFlow()

    val isFavorite: StateFlow<Boolean> = repository.observeIsFavorite(crossingId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = try {
                UiState.Success(repository.getCrossing(crossingId))
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun toggleFavorite() {
        val crossing = (state.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            repository.toggleFavorite(crossing, countryCode, isFavorite.value)
        }
    }

    class Factory(
        private val repository: GranicaRepository,
        private val crossingId: String,
        private val countryCode: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CrossingDetailViewModel(repository, crossingId, countryCode) as T
    }
}
