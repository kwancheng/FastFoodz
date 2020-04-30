package com.gk.fastfoodz.businesseslist.pagerfragments

import android.content.Context
import android.graphics.Color
import android.media.Image
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.gk.fastfoodz.LOG_TAG
import com.gk.fastfoodz.MainActivityViewModel
import com.gk.fastfoodz.R
import com.gk.fastfoodz.businesseslist.BusinessesListFragmentDirections
import com.gk.fastfoodz.databinding.BusinessItemViewBinding
import com.gk.fastfoodz.databinding.BusinessesListRecylerFragmentBinding
import com.gk.fastfoodz.network.Business

class BusinessesListRecylerFragment : Fragment() {
    companion object {
        fun newInstance() = BusinessesListRecylerFragment()
    }

    private lateinit var viewModel: BusinessesListRecylerViewModel
    private lateinit var binding: BusinessesListRecylerFragmentBinding
    private lateinit var adapter: BusinessItemListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(BusinessesListRecylerViewModel::class.java)
        binding = DataBindingUtil.inflate<BusinessesListRecylerFragmentBinding>(
            inflater,
            R.layout.businesses_list_recyler_fragment,
            container,
            false)

        adapter = BusinessItemListAdapter(requireContext()) { viewModel, view ->
            val direction = BusinessesListFragmentDirections
                .actionBusinessesListFragmentToBusinessDetailFragment(viewModel.business)

            findNavController().navigate(direction)
        }

        binding.listView.adapter = adapter

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val mainModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        mainModel.businesses.observe(this.viewLifecycleOwner, Observer {
            adapter.businesses = it
        })

    }
}

data class BusinessItemViewHolder(val binding: BusinessItemViewBinding): RecyclerView.ViewHolder(binding.root)

data class BusinessItemViewModel(val business: Business, val context: Context) {
    val infoTextDecorated: SpannableString
        get() {
            var basePrice = ""
            val priceLength = business.price?.length?.let { it } ?: 0

            business.price?.firstOrNull()?.let { priceChar ->
                for (i in 0 until 4) {
                    basePrice += priceChar
                }
            }

            var distanceString = ""
            business.distance?.let {distanceMeters ->
                val distanceMiles = distanceMeters * 0.000621371192
                distanceString = "%.2f miles".format(distanceMiles)
            }

            val components = mutableListOf<String>()
            if (basePrice.isNotEmpty()) components.add(basePrice)
            if (distanceString.isNotEmpty()) components.add(distanceString)

            val finalStr = components.joinToString(" • ")

            val spanner = SpannableString(finalStr)

            if (priceLength > 0) {
                spanner.setSpan(
                    ForegroundColorSpan(context.getColor(R.color.pickleGreen)),
                    0,
                    priceLength - 1,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
            }
            return spanner
        }
}

class BusinessItemListAdapter(
    private val context: Context,
    private val onClickCallback: ((BusinessItemViewModel, View)->Unit)?
): RecyclerView.Adapter<BusinessItemViewHolder>() {
    var businesses: List<Business> = listOf<Business>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = businesses.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusinessItemViewHolder {
        val binding = DataBindingUtil.inflate<BusinessItemViewBinding>(
            LayoutInflater.from(parent.context),
            R.layout.business_item_view,
            parent,
            false
        )

        return BusinessItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BusinessItemViewHolder, position: Int) {
        val item = businesses[position]
        holder.binding.viewModel = BusinessItemViewModel(item, context)

        holder.binding.root.setOnClickListener {
            holder.binding.viewModel?.let { viewModel ->
                onClickCallback?.invoke(viewModel, holder.binding.root)
            }
        }

        // Binding via the view model results in a distorted icon. so we programmatically set it.
        var retId: Int? = null

        item.categoriesAliases?.let {categories ->
            for (category in categories) {
                retId = when (category) {
                    "burgers" -> R.drawable.category_burger
                    "chinese" -> R.drawable.category_chinese
                    "pizza" -> R.drawable.category_pizza
                    "mexican" -> R.drawable.category_mexican
                    else -> null

                }
                if (retId != null) break
            }
        }

        retId?.let {
            holder.binding.categoryImage.setImageResource(it)
            holder.binding.categoryImage.setColorFilter(R.color.deepIndigo)
        }

    }
}