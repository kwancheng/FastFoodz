package com.gk.fastfoodz.businessdetail

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gk.fastfoodz.LOG_TAG
import com.gk.fastfoodz.R
import com.gk.fastfoodz.databinding.BusinessDetailFragmentBinding
import com.gk.fastfoodz.network.Business
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.picasso.Picasso

class BusinessDetailFragment : Fragment(), OnMapReadyCallback {
    companion object {
        fun newInstance() = BusinessDetailFragment()
    }

    private lateinit var viewModel: BusinessDetailViewModel
    private lateinit var binding: BusinessDetailFragmentBinding
    private lateinit var googleMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(BusinessDetailViewModel::class.java)
        binding = DataBindingUtil.inflate(inflater, R.layout.business_detail_fragment, container, false)
        binding.viewModel = viewModel

        val business = arguments?.getParcelable<Business>("business")?.let {business ->
            viewModel.business = business
            business
        }

        business?.photoUrl?.let { photoUrl ->
            Picasso.get().load(photoUrl).into(binding.venueImage)
        }

        // Initializing the Map
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.onResume()
        binding.mapView.getMapAsync(this)

        try {
            MapsInitializer.initialize(requireActivity().applicationContext)
        } catch(e: Exception) {
            Log.e(LOG_TAG, "Error Initializing Map: $e")
            Toast.makeText(this.context, "Map Initialization Failed", Toast.LENGTH_SHORT).show()
        }

        binding.callButton.setOnClickListener {
            viewModel.business?.phone?.let { phone ->
                if (phone.isEmpty()) {
                    Toast.makeText(requireContext(), "No Phone Number on File", Toast.LENGTH_SHORT).show()
                    return@let
                }

                val dialIntent = Intent(Intent.ACTION_DIAL)
                dialIntent.data = Uri.parse("tel:" + phone)
                startActivity(dialIntent)
            } ?: run {
                Toast.makeText(requireContext(), "No Phone Number on File", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            this.googleMap = googleMap

            val latitude = viewModel?.business?.coordinate?.latitude
            val longitude = viewModel?.business?.coordinate?.longitude

            if (latitude != null && longitude != null) {
                val homelatLng = LatLng(latitude, longitude)
                val zoomLevel = 17f

                this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(homelatLng, zoomLevel))
                this.googleMap.addMarker(MarkerOptions().position(homelatLng))
            }

            val permissionStatus = ContextCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
            val hasLocationPermission = permissionStatus == PackageManager.PERMISSION_GRANTED

            if (hasLocationPermission) {
                this.googleMap.isMyLocationEnabled = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}