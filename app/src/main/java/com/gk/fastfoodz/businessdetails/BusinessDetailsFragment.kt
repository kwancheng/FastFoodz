package com.gk.fastfoodz.businessdetails

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil

import com.gk.fastfoodz.R
import com.gk.fastfoodz.databinding.BusinessDetailsFragmentBinding
import com.gk.fastfoodz.network.Business
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.business_details_fragment.view.*
import java.lang.Exception

class BusinessDetailsFragment : Fragment(), OnMapReadyCallback {

    companion object {
        fun newInstance() = BusinessDetailsFragment()
    }

    private lateinit var viewModel: BusinessDetailsViewModel
    private lateinit var binding: BusinessDetailsFragmentBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var business: Business

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(BusinessDetailsViewModel::class.java)
        binding = DataBindingUtil.inflate(inflater, R.layout.business_details_fragment, container, false)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.onResume()
        binding.mapView.getMapAsync(this)

        arguments?.getParcelable<Business>("business")?.let {
            this.business = it
            binding.addressLabel.text = business.formattedAddress
            binding.infoLabel.text = "${business.price} • ${business?.reviewExcerpt}"

            binding.mapView.transitionName = business.id
        }

        try {
            MapsInitializer.initialize(activity!!.applicationContext)
        } catch (e: Exception) {
            Log.e("fastfoodz", "$e")
        }

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.let {
            this.googleMap = it

            val latitude = business?.coordinate?.latitude
            val longitude = business?.coordinate?.longitude

            if (latitude != null && longitude != null) {
                val homelatLng = LatLng(latitude, longitude)
                val zoomLevel = 17f

                this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(homelatLng, zoomLevel))
                this.googleMap.addMarker(MarkerOptions().position(homelatLng))
            }

            this.googleMap.isMyLocationEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.explode)
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
        activity?.title = business.name
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
