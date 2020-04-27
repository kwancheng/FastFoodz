package com.gk.fastfoodz.network

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Business(
    val id: String?,
    val name: String?,
    val formattedAddress: String?,
    val coordinate: Coordinate?,
    val price: String?,
    val reviewExcerpt: String?
): Parcelable

@Parcelize
data class Coordinate(
    val latitude: Double?,
    val longitude: Double?
): Parcelable
