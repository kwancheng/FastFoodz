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
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gk.fastfoodz.MainActivity
import com.gk.fastfoodz.MainActivityViewModel
import com.gk.fastfoodz.R
import com.gk.fastfoodz.businesseslist.pagerfragments.BusinessesListMapFragment
import com.gk.fastfoodz.businesseslist.pagerfragments.BusinessesListRecylerFragment
import com.gk.fastfoodz.databinding.BusinessesListFragmentBinding
import com.google.android.material.tabs.TabLayoutMediator

class BusinessesListFragment : Fragment() {
    private val DEFAULT_TAB = 0

    private lateinit var mainActivityViewModel: MainActivityViewModel
    private lateinit var binding: BusinessesListFragmentBinding
    private lateinit var mapFragment: BusinessesListMapFragment
    private lateinit var listFragment: BusinessesListRecylerFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

    /**
     * this observer is removed after a successful initialization.
     */
    private val initializationObserver: Observer<Boolean> = Observer {initialized ->
        if (activity==null || activity !is MainActivity) return@Observer
        val mainActivity = activity as MainActivity

        if (initialized) {
            val thisFragment = this
            mainActivity.viewModel.initialized.removeObserver(thisFragment.initializationObserver)

            binding.tabs.selectTab(binding.tabs.getTabAt(DEFAULT_TAB))
            showProgressSpinner(false)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mainActivityViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        mainActivityViewModel.initialized.observe(viewLifecycleOwner, initializationObserver)
    }

    // View Configuration
    private fun showProgressSpinner(show: Boolean){
        binding.maplistToggle.visibility = if (show) View.GONE else View.VISIBLE
        binding.progressOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }
}

class MapListViewPagerAdapter (
    fragment: Fragment,
    private val mapFragment: BusinessesListMapFragment,
    private val listFragment: BusinessesListRecylerFragment
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return if (position == 1)
            listFragment
        else
            mapFragment
    }
}