package com.gk.fastfoodz

import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.gk.fastfoodz.databinding.MainActivityBinding
import com.gk.fastfoodz.utils.Locator

const val LOG_TAG = "fastfoodz-gk"

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
    }
}
