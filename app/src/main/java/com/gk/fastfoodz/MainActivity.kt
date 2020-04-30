package com.gk.fastfoodz

import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.gk.fastfoodz.databinding.MainActivityBinding
import com.gk.fastfoodz.utils.Locator

const val LOG_TAG = "fastfoodz-gk"
//const val SEARCH_RADIUS_METERS = 19312.1 // AKA 12 Miles
const val SEARCH_RADIUS_METERS = 1000.0

class MainActivity : AppCompatActivity() {
    /**
     * Fast Foodz's default location if user's location cannot be found
     */
    private val initialLocation: Location
        get() {
            return Location("").apply {
                latitude = 40.758896
                longitude = -73.985130
            }
        }

    lateinit var viewModel: MainActivityViewModel
    private lateinit var binding: MainActivityBinding
    private lateinit var locator: Locator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)
        locator = Locator(this, initialLocation)

        viewModel.errorRetrievingBusinesses.observe(this, Observer{
            if (it) {
                val text = "Failed To Retrieve Businesses"
                val duration = Toast.LENGTH_SHORT

                Toast.makeText(this, text, duration).show()
            }
        })

        val navController = this.findNavController(R.id.nav_host_fragment_container)
        NavigationUI.setupActionBarWithNavController(this, navController)

        supportActionBar
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        locator.onRequestPermissionResult(requestCode, permissions, grantResults)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment_container).navigateUp()
    }
}
