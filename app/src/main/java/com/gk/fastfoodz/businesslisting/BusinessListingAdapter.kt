package com.gk.fastfoodz.businesslisting

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.gk.fastfoodz.R
import com.gk.fastfoodz.databinding.BusinessCardItemViewBinding
import com.gk.fastfoodz.network.Business

class BusinessCardListingAdapter(private val onClickCallback: ((BusinessCardViewModel, View)->Unit)?) : RecyclerView.Adapter<BusinessCardViewHolder>() {
    var businesses: List<Business> = listOf<Business>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int {
        return businesses.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusinessCardViewHolder {
        val binding = DataBindingUtil.inflate<BusinessCardItemViewBinding>(
            LayoutInflater.from(parent.context),
            R.layout.business_card_item_view,
            parent,
            false
        )

        return BusinessCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BusinessCardViewHolder, position: Int) {
        when (holder.binding) {
            is BusinessCardItemViewBinding -> {
                val item = businesses[position]
                holder.binding.viewModel = BusinessCardViewModel(item)

                ViewCompat.setTransitionName(holder.binding.root, item.id)

                holder.binding.root.setOnClickListener {
                    holder.binding.viewModel?.let { businessCardViewModel ->
                        onClickCallback?.invoke(businessCardViewModel, holder.binding.root)
                    }
                }
            }
        }
        holder.binding.executePendingBindings()
    }
}

class BusinessCardViewHolder(val binding: ViewDataBinding): RecyclerView.ViewHolder(binding.root)

class BusinessCardViewModel(val business: Business) {
    val infoText: String
        get() {
            return "${business.price} • ${business.reviewExcerpt}"
        }
}
