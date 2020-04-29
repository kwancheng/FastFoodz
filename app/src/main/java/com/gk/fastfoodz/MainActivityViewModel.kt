package com.gk.fastfoodz

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gk.fastfoodz.network.Business

class MainActivityViewModel: ViewModel() {
    // Indicates the loading status
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    fun updateIsLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    // Indicates if location permission is given by the user
    private val _isLocationEnabled = MutableLiveData<Boolean>()
    val isLocationEnabled: LiveData<Boolean>
        get() = _isLocationEnabled

    fun updateIsLocationEnabled(enabled: Boolean) {
        _isLocationEnabled.value = enabled
    }

    // Indicates the latest known user location
    private val _latestLocation = MutableLiveData<Location>()
    val latestLocation: LiveData<Location>
        get() = _latestLocation

    fun updateLocation(location: Location) {
        _latestLocation.value = location
    }

    // The latest list of businesses
    private val _businesses = MutableLiveData<List<Business>>()
    val businesses: LiveData<List<Business>>
        get() = _businesses

    fun updateBusinesses(businesses: List<Business>) {
        _businesses.value = businesses
    }

    // This indicates if the app has completed initialization
    private val _initialized = MutableLiveData<Boolean>()
    val initialized : LiveData<Boolean>
        get() = _initialized

    fun updateInitialized(initialized: Boolean) {
        _initialized.value = initialized
    }

    // Indicates if there was an error retrieving the list of businesses
    private val _errorRetrievingBusinesses = MutableLiveData<Boolean>()
    val errorRetrievingBusinesses : LiveData<Boolean>
        get() = _errorRetrievingBusinesses

    fun updateErrorRetrievingBusinesses( errorOccurred: Boolean) {
        _errorRetrievingBusinesses.value = errorOccurred
    }
}