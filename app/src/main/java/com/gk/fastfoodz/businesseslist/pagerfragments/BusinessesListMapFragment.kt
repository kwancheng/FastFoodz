package com.gk.fastfoodz.businesseslist.pagerfragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.gk.fastfoodz.*
import com.gk.fastfoodz.businesseslist.BusinessesListFragmentDirections
import com.gk.fastfoodz.databinding.BusinessesListMapFragmentBinding
import com.gk.fastfoodz.network.Business
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.log10


class BusinessesListMapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private val EQUATOR_LENGTH = 40075004

    companion object {
        fun newInstance() = BusinessesListMapFragment()
    }

    private lateinit var mainActivityViewModel: MainActivityViewModel
    private lateinit var viewModel: BusinessesListMapViewModel
    private lateinit var binding: BusinessesListMapFragmentBinding

    private lateinit var googleMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(BusinessesListMapViewModel::class.java)
        binding = DataBindingUtil.inflate<BusinessesListMapFragmentBinding>(
            inflater,
            R.layout.businesses_list_map_fragment,
            container,
            false
        )

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.onResume()
        binding.mapView.getMapAsync(this)



        return binding.root
    }

    /**
     * We need to remove the zoom level, right after the first zoom adjustment. Declaring the
     * observer as a field is the only way to access the instance
     */
    private val latestLocationObserver: Observer<Location> = Observer {
        if (activity == null || activity !is MainActivity) return@Observer
        val mainActivity = activity as MainActivity

        val thisFragment = this //this@BusinessesListMapFragment
        mainActivity.viewModel.latestLocation.removeObserver(thisFragment.latestLocationObserver)

        val metrics = resources.displayMetrics
        val mapWidth = binding.mapView.width / metrics.density
        val latAdjustment = cos(PI * it.latitude / 180.0)
        val arg = EQUATOR_LENGTH * mapWidth * latAdjustment / (SEARCH_RADIUS_METERS * 256.0)
        val zoomLevel = log10(arg) / log10(2.0)

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
            LatLng(it.latitude, it.longitude),
            zoomLevel.toFloat()
        ))

        val permissionStatus = ContextCompat.checkSelfPermission(
            requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
        val hasLocationPermission = permissionStatus == PackageManager.PERMISSION_GRANTED

        if (hasLocationPermission) {
            this.googleMap.isMyLocationEnabled = true
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity == null || activity !is MainActivity) return

        val mainActivity = activity as MainActivity
        mainActivityViewModel = mainActivity.viewModel
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.let { it ->
            this.googleMap = it
            this.googleMap.apply {
                isMyLocationEnabled = activity?.let { activity ->
                    if (activity is MainActivity) {
                        activity.viewModel.isLocationEnabled.value?.let{it} ?: false
                    } else {
                        false
                    }
                } ?: false
            }

            this.googleMap.setOnMarkerClickListener(this)

            binding.root.viewTreeObserver.addOnPreDrawListener( object: ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    if (binding.root.viewTreeObserver.isAlive)
                        binding.root.viewTreeObserver.removeOnPreDrawListener(this)

                    mainActivityViewModel.latestLocation.observe(viewLifecycleOwner, latestLocationObserver)

                    mainActivityViewModel.businesses.observe(viewLifecycleOwner, Observer { businesses ->
                        val bm = BitmapFactory.decodeResource(resources, R.drawable.mappin)
                        businesses.forEach {
                            val lat = it.coordinate?.latitude
                            val lng = it.coordinate?.longitude
                            if (lat != null && lng != null) {
                                val marker = googleMap.addMarker(MarkerOptions()
                                    .position(LatLng(lat, lng))
                                    .icon(BitmapDescriptorFactory.fromBitmap(bm))
                                )
                                marker.tag = it
                            }
                        }
                    })

                    return true
                }
            })
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        return (marker?.tag as Business).let {
            val direction = BusinessesListFragmentDirections
                .actionBusinessesListFragmentToBusinessDetailFragment(it)

            findNavController().navigate(direction)
            true
        } ?: false
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