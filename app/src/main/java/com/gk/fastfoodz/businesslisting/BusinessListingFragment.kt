package com.gk.fastfoodz.businesslisting

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.gk.fastfoodz.MainActivityViewModel

import com.gk.fastfoodz.R
import com.gk.fastfoodz.databinding.BusinessListingFragmentBinding

class BusinessListingFragment : Fragment() {
    private lateinit var mainActivityViewModel : MainActivityViewModel

    private lateinit var viewModel: BusinessListingViewModel
    private lateinit var binding: BusinessListingFragmentBinding
    private lateinit var adapter: BusinessCardListingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(BusinessListingViewModel::class.java)
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.business_listing_fragment,
            container,
            false
        )
        binding.lifecycleOwner = viewLifecycleOwner

        adapter = BusinessCardListingAdapter { model, rootview ->
            val direction = BusinessListingFragmentDirections.
            actionBusinessListingFragmentToBusinessDetailsFragment(model.business, model.business.name ?: "")

            val extras = FragmentNavigatorExtras(
                rootview to rootview.transitionName
            )

            findNavController().navigate(direction, extras)
        }
        binding.businessList.adapter = adapter

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainActivityViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        mainActivityViewModel.businesses.observe(viewLifecycleOwner, Observer {businesses ->
            adapter.businesses = businesses
        })

        mainActivityViewModel.businesses.value?.let { businesses ->
            adapter.businesses = businesses
        }

    }
    
}