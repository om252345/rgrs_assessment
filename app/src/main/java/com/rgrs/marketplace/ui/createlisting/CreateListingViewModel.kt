package com.rgrs.marketplace.ui.createlisting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rgrs.marketplace.data.sync.NetworkObserver
import com.rgrs.marketplace.domain.ListingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateListingViewModel @Inject constructor(
    val listingRepository: ListingRepository,
    networkObserver: NetworkObserver
) : ViewModel() {

    private val _formFields = MutableStateFlow(ListingFormFields())
    val formFields = _formFields

    val isOnline = networkObserver.isOnline.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true
    )

    fun onTitleChanged(title: String) {
        _formFields.value = _formFields.value.copy(title = title)
        validateForm()
    }

    fun onPriceChanged(price: Double) {
        _formFields.value = _formFields.value.copy(price = price)
        validateForm()
    }

    fun onLocalImagePathChanged(localImagePath: String?) {
        _formFields.value = _formFields.value.copy(localImagePath = localImagePath)
        validateForm()
    }

    fun validateForm() {
        val titleError = if (_formFields.value.title.isEmpty()) {
            "Title is required"
        } else {
            null
        }
        val priceError = if (_formFields.value.price <= 0) {
            "Price must be greater than zero"
        } else {
            null
        }
        val localImagePathError = if (_formFields.value.localImagePath.isNullOrEmpty()) {
            "Image is required"
        } else {
            null
        }
        val isFormValid = titleError == null && priceError == null && localImagePathError == null
        _formFields.value = _formFields.value.copy(
            titleError = titleError,
            priceError = priceError,
            localImagePathError = localImagePathError,
            isFormValid = isFormValid
        )
    }

    fun createListing(title: String, price: Double, localImagePath: String?) {
        viewModelScope.launch {
            listingRepository.createListing(title, price, localImagePath)
        }
    }
}

data class ListingFormFields(
    val title: String = "",
    val price: Double = 0.0,
    val localImagePath: String? = null,
    val titleError: String? = null,
    val priceError: String? = null,
    val localImagePathError: String? = null,
    val isFormValid: Boolean = false
)
