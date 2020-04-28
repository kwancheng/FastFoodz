package com.gk.fastfoodz.businesseslist.pagerfragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import com.gk.fastfoodz.R
import com.gk.fastfoodz.databinding.BusinessesListRecylerFragmentBinding

class BusinessesListRecylerFragment : Fragment() {

    companion object {
        fun newInstance() = BusinessesListRecylerFragment()
    }

    private lateinit var viewModel: BusinessesListRecylerViewModel
    private lateinit var binding: BusinessesListRecylerFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<BusinessesListRecylerFragmentBinding>(
            inflater,
            R.layout.businesses_list_recyler_fragment,
            container,
            false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(BusinessesListRecylerViewModel::class.java)
    }

}
