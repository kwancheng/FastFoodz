package com.gk.fastfoodz.businesslisting

import android.util.Log
import androidx.lifecycle.ViewModel

class BusinessListingViewModel : ViewModel() {
    init {
        Log.i("BusinessListingViewModel", "BusinessListingViewModel created!")
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("BusinessListingViewModel", "BusinessListingViewModel destroyed!")
    }
}
