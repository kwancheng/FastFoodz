package com.gk.fastfoodz.businessdetail

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.gk.fastfoodz.R
import com.gk.fastfoodz.databinding.BusinessDetailFragmentBinding

class BusinessDetailFragment : Fragment() {
    companion object {
        fun newInstance() = BusinessDetailFragment()
    }

    private lateinit var viewModel: BusinessDetailViewModel
    private lateinit var binding: BusinessDetailFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.business_detail_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(BusinessDetailViewModel::class.java)
    }
}