package com.gk.fastfoodz.businesseslist

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.gk.fastfoodz.MainActivityViewModel
import com.gk.fastfoodz.R
import com.gk.fastfoodz.businesseslist.pagerfragments.BusinessesListMapFragment
import com.gk.fastfoodz.businesseslist.pagerfragments.BusinessesListRecylerFragment
import com.gk.fastfoodz.businesseslist.pagerfragments.MapListViewPagerAdapter
import com.gk.fastfoodz.databinding.BusinessesListFragmentBinding
import com.google.android.material.tabs.TabLayoutMediator

class BusinessesListFragment : Fragment() {
    companion object {
        fun newInstance() = BusinessesListFragment()
    }

    private lateinit var mainActivityViewModel: MainActivityViewModel
    private lateinit var viewModel: BusinessesListViewModel
    private lateinit var binding: BusinessesListFragmentBinding
    private lateinit var mapFragment: BusinessesListMapFragment
    private lateinit var listFragment: BusinessesListRecylerFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(BusinessesListViewModel::class.java)
        binding = DataBindingUtil.inflate(inflater, R.layout.businesses_list_fragment, container, false)

        mapFragment = BusinessesListMapFragment.newInstance()
        listFragment = BusinessesListRecylerFragment.newInstance()

        val adapter = MapListViewPagerAdapter(this, mapFragment, listFragment)
        binding.mapListPager.adapter = adapter
        binding.mapListPager.isUserInputEnabled = false

        binding.tabs.setSelectedTabIndicatorColor(Color.TRANSPARENT)

        TabLayoutMediator(binding.tabs, binding.mapListPager, false, false) { tab, position ->
            tab.text = if (position == 1) "List" else "Map"
        }.attach()

        showProgressSpinner(true)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mainActivityViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)

        mainActivityViewModel.initialized.observe(viewLifecycleOwner, Observer{ initialized ->
            if (!initialized) { return@Observer }
            showProgressSpinner(false)
        })
    }

    // View Configuration
    private fun showProgressSpinner(show: Boolean){
        binding.maplistToggle.visibility = if (show) View.GONE else View.VISIBLE
        binding.progressOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }
}