package com.igrupos.core.util

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object GeocoderUtil {

    suspend fun geocode(context: Context, address: String, province: String): Pair<Double, Double>? {
        if (address.isBlank() && province.isBlank()) return null
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val fullAddress = listOfNotNull(
                    address.ifBlank { null },
                    province.ifBlank { null }
                ).joinToString(", ")
                val results = geocoder.getFromLocationName(fullAddress, 1)
                results?.firstOrNull()?.let { it.latitude to it.longitude }
            } catch (_: Exception) {
                null
            }
        }
    }

    suspend fun reverseGeocode(context: Context, lat: Double, lng: Double): String? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val results = geocoder.getFromLocation(lat, lng, 1)
                results?.firstOrNull()?.getAddressLine(0)
            } catch (_: Exception) {
                null
            }
        }
    }
}
