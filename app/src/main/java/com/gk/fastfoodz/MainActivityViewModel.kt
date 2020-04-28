package com.gk.fastfoodz

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gk.fastfoodz.network.Business

class MainActivityViewModel: ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    fun updateIsLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    private val _isLocationEnabled = MutableLiveData<Boolean>()
    val isLocationEnabled: LiveData<Boolean>
        get() = _isLocationEnabled

    fun updateIsLocationEnabled(enabled: Boolean) {
        _isLocationEnabled.value = enabled
    }

    private val _latestLocation = MutableLiveData<Location>()
    val latestLocation: LiveData<Location>
        get() = _latestLocation

    fun updateLocation(location: Location) {
        _latestLocation.value = location
    }

    private val _businesses = MutableLiveData<List<Business>>()
    val businesses: LiveData<List<Business>>
        get() = _businesses

    fun updateBusinesses(businesses: List<Business>) {
        _businesses.value = businesses
    }

    //// This indicates if the app has completed initialization
    private val _initialized = MutableLiveData<Boolean>()
    val initialized : LiveData<Boolean>
        get() = _initialized

    fun updateInitialized(initialized: Boolean) {
        _initialized.value = initialized
    }
}