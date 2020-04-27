package com.gk.fastfoodz.loader

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.gk.fastfoodz.MainActivityViewModel

import com.gk.fastfoodz.R
import com.gk.fastfoodz.databinding.LoaderFragmentBinding

class LoaderFragment : Fragment() {

    companion object {
        fun newInstance() = LoaderFragment()
    }

    private lateinit var viewModel: LoaderViewModel
    private lateinit var binding: LoaderFragmentBinding
    private lateinit var mainActivityViewModel: MainActivityViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(LoaderViewModel::class.java)
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.loader_fragment,
            container,
            false
        )
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.let {
            mainActivityViewModel = ViewModelProvider(it).get(MainActivityViewModel::class.java)
            mainActivityViewModel.initialized.observe(viewLifecycleOwner, Observer {initialized ->
                if (initialized) {
                    findNavController().navigate(
                        LoaderFragmentDirections.actionLoaderFragmentToBusinessListingFragment())
                }
            })
        }
    }
}
