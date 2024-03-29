package com.gk.fastfoodz.network

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import okhttp3.OkHttpClient
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object YelpNetwork {
    private val httpClient = OkHttpClient
        .Builder()
        .addInterceptor {
            val original = it.request()
            val requestBuilder = original.newBuilder()
                .addHeader(
                    "Authorization",
                    "Bearer JUm62geSGXZpBdXwuSkgZjogxICFWW17X--eMjbyPAaTJ3zVcH-Y9Y0Vsm2GVN8s_yi9jStiuPI0xICBVuBlZ9KdLUs2gqFU0x-3QoP0YKwvgtv7o5qQAkL2s--iXnYx"
                )
            it.proceed(requestBuilder.build())
        }
        .build()

    private val apolloClient = ApolloClient.builder()
        .serverUrl("https://api.yelp.com/v3/graphql")
        .okHttpClient(httpClient)
        .build()

    object YelpService {
        suspend fun searchBusinesses(
            categories: String,
            radius: Double,
            latitude: Double,
            longitude: Double
        ): List<Business>? =
            suspendCoroutine { continuation ->
                val yelpSearchQuery = YelpSearchQuery(
                    categories,
                    radius,
                    latitude,
                    longitude,
                    "distance"
                )

                apolloClient.query(yelpSearchQuery)
                    .enqueue(object : ApolloCall.Callback<YelpSearchQuery.Data>() {
                        override fun onResponse(response: Response<YelpSearchQuery.Data>) {
                            val businesses = response.data()?.search?.business ?: return

                            val retList = mutableListOf<Business>()

                            for (business in businesses) {
                                val categories = business?.categories?.let { categories ->
                                    val catList = mutableListOf<String>()
                                    for (category in categories) {
                                        category.alias?.let {
                                            catList.add(it)
                                        }
                                    }
                                    catList
                                }

                                val item = Business(
                                    business?.id,
                                    business?.name,
                                    business?.location?.formatted_address,
                                    Coordinate(
                                        business?.coordinates?.latitude,
                                        business?.coordinates?.longitude
                                    ),
                                    business?.distance,
                                    business?.price,
                                    business?.reviews?.firstOrNull()?.text,
                                    business?.photos?.firstOrNull(),
                                    business?.phone,
                                    categories
                                )
                                retList.add(item)
                            }

                            continuation.resume(retList)
                        }

                        override fun onFailure(e: ApolloException) {
                            continuation.resume(null)
                        }
                    })
            }
    }
}