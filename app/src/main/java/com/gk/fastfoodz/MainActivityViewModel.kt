package com.gk.fastfoodz

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gk.fastfoodz.network.Business

class MainActivityViewModel : ViewModel() {
    private val _latestGpsLocation = MutableLiveData<Location>()
    val latestGpsLocation : LiveData<Location>
        get() = _latestGpsLocation

    fun update(location: Location) {
        val currentLocation = _latestGpsLocation.value
        if (currentLocation == null) {
            Log.i("fastfoodz", "Location Updated")
            _latestGpsLocation.value = location
        } else if (currentLocation.latitude != location.latitude ||
                currentLocation.longitude != location.longitude) {
            Log.i("fastfoodz", "Location Updated")
            _latestGpsLocation.value = location
        }
    }

    //// This indicates if the app has completed initialization
    private val _initialized = MutableLiveData<Boolean>()
    val initialized : LiveData<Boolean>
        get() = _initialized

    fun update(initialized: Boolean) {
        _initialized.value = initialized
    }

    //// newly retrieved businesses
    private val _businesses = MutableLiveData<List<Business>>()
    val businesses: LiveData<List<Business>>
        get() = _businesses

    fun update(businesses: List<Business>) {
        _businesses.value = businesses
    }

    private val _hasLocationPermission = MutableLiveData<Boolean>()
    val hasLocationPermission : LiveData<Boolean>
        get() = _hasLocationPermission
    fun updateHasLocationPermission( hasPermission: Boolean) {
        _hasLocationPermission.value = hasPermission
    }
}