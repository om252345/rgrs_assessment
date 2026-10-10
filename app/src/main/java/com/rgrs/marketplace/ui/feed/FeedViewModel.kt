package com.rgrs.marketplace.ui.feed

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.rgrs.marketplace.data.sync.NetworkObserver
import com.rgrs.marketplace.domain.Listing
import com.rgrs.marketplace.domain.ListingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    val listingRepository: ListingRepository,
    networkObserver: NetworkObserver,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    val isOnline = networkObserver.isOnline.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true
    )

    val isSyncing: StateFlow<Boolean> = combine(
        isOnline,
        listingRepository.hasPendingListings(),
        WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow("SyncWorker")
            .map { workInfos ->
                workInfos.any { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
            }
    ) { online, hasPending, isWorkRunning ->
        Log.v("FeedViewModel", "isSyncing: $online, $hasPending, $isWorkRunning")
        online && (hasPending || isWorkRunning)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    init {
        viewModelScope.launch {
            isOnline.collect { online ->
                if (online) {
                    listingRepository.syncPendingListings()
                }
            }
        }
    }

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
    object Loading : UiState()
    data class Success(val listings: List<Listing>) : UiState()
    data class Error(val message: String) : UiState()
}
