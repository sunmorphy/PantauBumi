package com.andikas.pantaubumi.util

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun formatTime(isoString: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(isoString)
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(date!!)
    } catch (e: Exception) {
        "10:30"
    }
}
