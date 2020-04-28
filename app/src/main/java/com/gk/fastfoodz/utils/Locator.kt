package com.gk.fastfoodz.utils

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.gk.fastfoodz.MainActivity
import com.gk.fastfoodz.network.YelpNetwork
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class Locator(
    private val mainActivity: MainActivity,
    private val defaultLocation: Location
) : LifecycleObserver, LocationCallback() {
    private val locationPermissionRequestCode = 1

    private val locationClient: FusedLocationProviderClient
    private var lastReceivedLocation: Location? = null

    private val searchJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + searchJob)

    init {
        mainActivity.lifecycle.addObserver(this)
        locationClient = FusedLocationProviderClient(mainActivity)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun start() {
        val permissionStatus = ContextCompat.checkSelfPermission(
            mainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
        val permissionGranted = permissionStatus == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            mainActivity.viewModel.updateIsLocationEnabled(true)
            monitorLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(
                mainActivity,
                arrayOf<String>(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                locationPermissionRequestCode
            )
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun stop() {}

    /**
     * This method duplicates the method signature in the Activity when permissions are requested
     */
    fun onRequestPermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResult: IntArray
    ) {
        if (requestCode != locationPermissionRequestCode) { return }

        val permissionGranted = grantResult.firstOrNull()?.let {
            it == PackageManager.PERMISSION_GRANTED
        } ?: false

        mainActivity.viewModel.updateIsLocationEnabled(permissionGranted)

        // Design Note: this method call startLocationUpdates regardless of the permission status
        // returned from requesting the the location permission to the user. see startLocationUpdates
        // for an explanation. Normally we will have code here to re-prompt user of how critical
        // the permission is to the functioning of the app.
        monitorLocationUpdates()
    }

    private fun monitorLocationUpdates() {
        locationClient.lastLocation.addOnCompleteListener { locationTask ->
            var loc = defaultLocation
            if (locationTask.isSuccessful && locationTask.result != null)
                loc = locationTask.result!!
            mainActivity.viewModel.updateLocation(loc)
        }

        val lr = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationClient.requestLocationUpdates(lr, this, Looper.myLooper())
    }

    // Location Callback Overrides
    override fun onLocationResult(locationResult: LocationResult?) {
        val loc = locationResult?.locations?.first()?.let {it} ?: return

        lastReceivedLocation?.let { lrl ->
            if (lrl.latitude == loc.latitude &&
                lrl.longitude == loc.longitude) return
        }

        // new location is not the same as last received location, we notify all listeners
        lastReceivedLocation = loc

        scope.launch {
            YelpNetwork.yelpService.searchBusinesses(
                "burrito",
                19312.1, // This is 12 miles in meters
                loc.latitude,
                loc.longitude
            ) ?.let {
                mainActivity.viewModel.updateBusinesses(it)
                mainActivity.viewModel.updateInitialized(true)
            } ?: run {
                lastReceivedLocation = null
            }
        }
    }
}