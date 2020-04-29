package com.gk.fastfoodz

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.gk.fastfoodz.network.YelpNetwork
import com.google.android.gms.location.*
import kotlinx.coroutines.*

const val LOG_TAG = "fastfoodz-log"
const val SEARCH_RADIUS_METERS = 19312.1 // AKA 12 Miles

class MainActivity : AppCompatActivity(), LifecycleObserver {
    private val initialLocation: Location
        get() {
            return Location("").apply {
                latitude = 40.758896
                longitude = -73.985130
            }
        }

    internal lateinit var viewModel: MainActivityViewModel
    private lateinit var locator: Locator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        locator = Locator(this, initialLocation)
        lifecycle.addObserver(this)

        val navController = this.findNavController(R.id.navHostFragment)
        val appBarConfiguration =
            AppBarConfiguration.Builder(R.id.loaderFragment, R.id.businessListingFragment).build()
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        locator.onRequestPermissionResult(requestCode, permissions, grantResults)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.navHostFragment)

        Log.i(LOG_TAG, "Current Destination ${navController.currentDestination}")

        return navController.navigateUp()
    }
}

/***
 * Organizes all the location stuff into this class.
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

        mainActivity.viewModel.updateHasLocationPermission(permissionGranted)

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

        mainActivity.viewModel.updateHasLocationPermission(permissionGranted)

        monitorLocationUpdates()
    }

    private fun monitorLocationUpdates() {
        val permissionStatus = ContextCompat.checkSelfPermission(
            mainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
        val permissionGranted = permissionStatus == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            fetchBusinesses(defaultLocation)
            return
        }

        locationClient.lastLocation.addOnCompleteListener { locationTask ->
            var loc = defaultLocation
            if (locationTask.isSuccessful && locationTask.result != null) {
                loc = locationTask.result!!
            }
            fetchBusinesses(loc)
        }

        val lr = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = LOCATION_UPDATE_INTERVAL_MSEC
        }

        locationClient.requestLocationUpdates(lr, this, Looper.myLooper())
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

    fun fetchBusinesses(location: Location) {
        queryInProgress = true
        searchScope.launch {
            YelpNetwork.yelpService.searchBusinesses(
                "burrito",
                SEARCH_RADIUS_METERS,
                location.latitude,
                location.longitude
            ) ?.let {
                mainActivity.viewModel.update(it)
                mainActivity.viewModel.update(true) // we have our first data
                lastReceivedLocation = location
            } ?: run {
                // result is null. Implies an error
                // we don't reset lastReceivedLocation on errors. Only on successful queries
                val text = "Failed To Retrieve Businesses"
                val duration = Toast.LENGTH_SHORT

                val toast = Toast.makeText(mainActivity, text, duration)
                toast.show()
            }

            cooldownScope.launch {
                delay(QUERY_COOLDOWN_MSEC)
                queryInProgress = false
            }
        }
    }
}