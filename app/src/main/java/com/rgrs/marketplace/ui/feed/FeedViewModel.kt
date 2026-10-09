package com.rgrs.marketplace.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rgrs.marketplace.domain.Listing
import com.rgrs.marketplace.domain.ListingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(val listingRepository: ListingRepository): ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun loadListings() {
        viewModelScope.launch {
            try {
                listingRepository.getListings().collect {
                    _uiState.value = UiState.Success(it)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun toggleFavorite(id: Long) {
        viewModelScope.launch {
            listingRepository.toggleFavorite(id)
        }
    }
}

sealed class UiState {
    object Loading: UiState()
    data class Success(val listings: List<Listing>): UiState()
    data class Error(val message: String): UiState()
}