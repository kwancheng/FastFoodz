package com.gk.fastfoodz.network

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Business(
    val id: String?,
    val name: String?,
    val formattedAddress: String?,
    val coordinate: Coordinate?,
    val distance: Double?,
    val price: String?,
    val reviewExcerpt: String?,
    val photoUrl: String?,
    val phone: String?,
    val categoriesAliases: List<String>?
): Parcelable

@Parcelize
data class Coordinate(
    val latitude: Double?,
    val longitude: Double?
): Parcelable

