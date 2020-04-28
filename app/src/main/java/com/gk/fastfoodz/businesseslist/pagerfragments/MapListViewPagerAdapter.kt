package com.gk.fastfoodz.businesseslist.pagerfragments

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

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