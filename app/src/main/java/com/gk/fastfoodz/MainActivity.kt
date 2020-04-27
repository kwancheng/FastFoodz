package com.gk.fastfoodz

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
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
        val appBarConfiguration = AppBarConfiguration.Builder(R.id.loaderFragment, R.id.businessListingFragment).build()
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray)
    {
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
class FFLocationManager(private val mainActivity: MainActivity): LifecycleObserver {
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
            mainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
        val isPermissionGranted =  permissionStatus == PackageManager.PERMISSION_GRANTED

        if (isPermissionGranted) {
            startLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(
                mainActivity, arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionRequestCode)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun stopLocation() {}

    private fun startLocationUpdates() {
        // If the user did not give permission to location, these methods will be dormant or return
        // nulls. When we detect a non functioning location client, we will return a single initialLocation
        locationClient.lastLocation.addOnCompleteListener{ it ->
            val loc = it.result?.let {location ->
                if (it.isSuccessful) {
                    location
                } else {
                    initialLocation
                }
            } ?: initialLocation

            mainActivity.viewModel.update(loc)

            // Design Note: After attempting to retrieve the last location we begin requesting
            // location updates from the OS. If we don't have permission this request would remain
            // dormant and not deliver any location updates. Normally we would try to re-prompt the
            // user or instruct the user to enable location services.
            val lr = LocationRequest()
            lr.apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 0
                fastestInterval = 0
            }

            locationClient.requestLocationUpdates(
                lr,
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        val location = locationResult?.locations?.first()?.let { loc -> loc } ?: return

                        lastReceivedLocation?.let { lastReceivedLocation ->
                            if (lastReceivedLocation.latitude == location.latitude &&
                                lastReceivedLocation.longitude == location.longitude) return
                        }

                        // new location and its not the same as the last received location
                        lastReceivedLocation = location
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
                                lastReceivedLocation = null
                            }
                        }
                    }
                },
                Looper.myLooper()
            )
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
        if (requestCode != locationPermissionRequestCode) { return }

        // Design Note: this method call startLocationUpdates regardless of the permission status
        // returned from requesting the the location permission to the user. see startLocationUpdates
        // for an explanation. Normally we will have code here to re-prompt user of how critical
        // the permission is to the functioning of the app.
        startLocationUpdates()
    }
}