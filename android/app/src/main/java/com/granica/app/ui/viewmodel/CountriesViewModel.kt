package com.granica.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.granica.app.data.model.CountryDto
import com.granica.app.data.repository.GranicaRepository
import com.granica.app.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CountriesViewModel(private val repository: GranicaRepository) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<CountryDto>>>(UiState.Loading)
    val state: StateFlow<UiState<List<CountryDto>>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = try {
                UiState.Success(repository.getCountries())
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    class Factory(private val repository: GranicaRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CountriesViewModel(repository) as T
    }
}
