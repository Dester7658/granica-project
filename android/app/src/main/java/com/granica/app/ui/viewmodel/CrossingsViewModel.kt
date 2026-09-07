package com.granica.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.granica.app.data.model.CrossingDto
import com.granica.app.data.repository.GranicaRepository
import com.granica.app.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CrossingsViewModel(
    private val repository: GranicaRepository,
    private val borderId: String
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<CrossingDto>>>(UiState.Loading)
    val state: StateFlow<UiState<List<CrossingDto>>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = try {
                UiState.Success(repository.getBorder(borderId).crossings)
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    class Factory(
        private val repository: GranicaRepository,
        private val borderId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CrossingsViewModel(repository, borderId) as T
    }
}
