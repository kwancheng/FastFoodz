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
import com.google.android.gms.maps.MapsInitializer
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity(), LifecycleObserver {
    internal lateinit var viewModel: MainActivityViewModel
    private lateinit var ffLocationManager: FFLocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        ffLocationManager = FFLocationManager(this)
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
        ffLocationManager.onRequestPermissionResult(requestCode, permissions, grantResults)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.navHostFragment)

        Log.i("fastfoodz", "Current Destination ${navController.currentDestination}")

        return navController.navigateUp()
    }
}

/***
 * Organizes all the location stuff into this class.
 */
class FFLocationManager(private val mainActivity: MainActivity) : LifecycleObserver, LocationCallback() {
    private var searchJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + searchJob)
    internal val locationPermissionRequestCode = 1

    /**
     * Fast Foodz's default location if user's location cannot be found
     */
    private val initialLocation: Location
        get() {
            val loc = Location("")
            loc.latitude = 40.758896
            loc.longitude = -73.985130
            return loc
        }

    private var locationClient: FusedLocationProviderClient
    private var lastReceivedLocation: Location? = null

    init {
        mainActivity.lifecycle.addObserver(this)
        locationClient = FusedLocationProviderClient(mainActivity)
    }

    /**
     * When the activity starts, we initialize the Fused Location Provider
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun startLocation() {
        val permissionStatus = ContextCompat.checkSelfPermission(
            mainActivity, Manifest.permission.ACCESS_FINE_LOCATION
        )
        val isPermissionGranted = permissionStatus == PackageManager.PERMISSION_GRANTED
        mainActivity.viewModel.updateHasLocationPermission(isPermissionGranted)

        if (isPermissionGranted) {
            startLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(
                mainActivity, arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionRequestCode
            )
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun stopLocation() {
        locationClient.removeLocationUpdates(this)
    }

    private fun startLocationUpdates() {
        locationClient.lastLocation.addOnCompleteListener { it ->
            val loc = it.result?.let { location ->
                if (it.isSuccessful) {
                    location
                } else {
                    initialLocation
                }
            } ?: initialLocation

            mainActivity.viewModel.update(loc)

            val lr = LocationRequest()
            lr.apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 10000
            }

            locationClient.requestLocationUpdates(
                lr,
                this,
                Looper.myLooper()
            )
        }
    }

    override fun onLocationResult(locationResult: LocationResult?) {
        Log.i("fastfoodz", "Location Updated")
        val location =
            locationResult?.locations?.first()?.let { loc -> loc } ?: return

        lastReceivedLocation?.let { lastReceivedLocation ->
            if (lastReceivedLocation.latitude == location.latitude &&
                lastReceivedLocation.longitude == location.longitude
            ) return
        }

        // new location and its not the same as the last received location
        lastReceivedLocation = location
        Log.i("fastfoodz", "Querying")
        fetchBusinesses(location)
    }

    private fun fetchBusinesses(location: Location) {
        scope.launch {
            val businesses = YelpNetwork.yelpService.searchBusinesses(
                "burrito",
                19312.1,
                location.latitude,
                location.longitude
            )
            if (businesses != null) {
                mainActivity.viewModel.update(businesses)
                mainActivity.viewModel.update(true)
            } else {
                val text = "Failed to Retrieve Businesses"
                val duration = Toast.LENGTH_SHORT

                val toast = Toast.makeText(mainActivity, text, duration)
                toast.show()
                lastReceivedLocation = null
            }
        }
    }

    /**
     * This duplicates the method signature in the Activity when permissions are requested.
     */
    internal fun onRequestPermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // We only care for permission requested by this class.
        if (requestCode != locationPermissionRequestCode) {
            return
        }

        val permissionGranted = grantResults.firstOrNull()?.let {
            it == PackageManager.PERMISSION_GRANTED
        } ?: false

        mainActivity.viewModel.updateHasLocationPermission(permissionGranted)

        if (permissionGranted) {
            startLocationUpdates()
        } else {
            mainActivity.viewModel.update(initialLocation)
            fetchBusinesses(initialLocation)
        }
    }
}