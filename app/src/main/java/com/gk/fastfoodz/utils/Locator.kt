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
import com.gk.fastfoodz.SEARCH_RADIUS_METERS
import com.gk.fastfoodz.network.YelpNetwork
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.*

/**
 * Locator combines the retrieval of users's GPS and performing a search against Yelp
 */
class Locator(
    private val mainActivity: MainActivity,
    private val defaultLocation: Location
) : LifecycleObserver, LocationCallback() {
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val LOCATION_UPDATE_INTERVAL_MSEC : Long = 10000
    private val QUERY_COOLDOWN_MSEC: Long = 10000

    private val locationClient: FusedLocationProviderClient
    private var lastReceivedLocation: Location? = null
    private var queryInProgress = false

    private val searchJob = Job()
    private val searchScope = CoroutineScope(Dispatchers.Main + searchJob)

    // Prevents spamming of Yelp's API in case request location updates returns quickly.
    private val cooldownJob = Job()
    private val cooldownScope = CoroutineScope(Dispatchers.Main + cooldownJob)

    init {
        mainActivity.lifecycle.addObserver(this)
        locationClient = FusedLocationProviderClient(mainActivity)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun start() {
        val permissionStatus = ContextCompat.checkSelfPermission(
            mainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
        val permissionGranted = permissionStatus == PackageManager.PERMISSION_GRANTED

        mainActivity.viewModel.updateIsLocationEnabled(permissionGranted)

        if (permissionGranted) {
            monitorLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(
                mainActivity,
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun stop() {
        locationClient.removeLocationUpdates(this)
    }

    /**
     * This method duplicates the method signature in the Activity when permissions are requested
     */
    fun onRequestPermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResult: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) { return }

        val permissionGranted = grantResult.firstOrNull()?.let {
            it == PackageManager.PERMISSION_GRANTED
        } ?: false

        mainActivity.viewModel.updateIsLocationEnabled(permissionGranted)

        monitorLocationUpdates()
    }

    private fun monitorLocationUpdates() {
        val hasPermission = mainActivity.viewModel.isLocationEnabled.value ?: false
        if (!hasPermission) {
            fetchBusinesses(defaultLocation)
            return
        }

        locationClient.lastLocation.addOnCompleteListener { locationTask ->
            var loc = defaultLocation
            if (locationTask.isSuccessful && locationTask.result != null)
                loc = locationTask.result!!
            fetchBusinesses(loc)
        }

        val lr = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = LOCATION_UPDATE_INTERVAL_MSEC
        }

        locationClient.requestLocationUpdates(lr, this, Looper.myLooper())
    }

    private fun fetchBusinesses(location: Location) {
        queryInProgress = true
        searchScope.launch {
            YelpNetwork.YelpService.searchBusinesses(
                "burgers, pizza, mexican, chinese",
                SEARCH_RADIUS_METERS,
                location.latitude,
                location.longitude
            ) ?.let {
                mainActivity.viewModel.updateBusinesses(it)
                mainActivity.viewModel.updateInitialized(true) // we have our first data
                mainActivity.viewModel.updateLocation(location)
                lastReceivedLocation = location
            } ?: run {
                mainActivity.viewModel.updateErrorRetrievingBusinesses(true)
            }

            cooldownScope.launch {
                delay(QUERY_COOLDOWN_MSEC)
                queryInProgress = false
            }
        }
    }

    // Location Callback Overrides
    override fun onLocationResult(locationResult: LocationResult?) {
        val loc = locationResult?.locations?.first()?.let {it} ?: return

        if (queryInProgress) {
            return // we ignore location updates until the query is complete
        }

        lastReceivedLocation?.let { lrl ->
            // Duplicate locations we do not want to run another query on the same location
            if (lrl.latitude == loc.latitude &&
                lrl.longitude == loc.longitude) return
        }

        fetchBusinesses(loc)
    }
}