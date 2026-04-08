package com.andikas.pantaubumi.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.andikas.pantaubumi.data.remote.PantauBumiApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

enum class NetworkStatus {
    Available, Unavailable
}

@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: PantauBumiApi
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val status: Flow<NetworkStatus> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                launch {
                    if (checkHealth()) {
                        send(NetworkStatus.Available)
                    } else {
                        send(NetworkStatus.Unavailable)
                    }
                }
            }

            override fun onLost(network: Network) {
                trySend(NetworkStatus.Unavailable)
            }

            override fun onUnavailable() {
                trySend(NetworkStatus.Unavailable)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)

        launch {
            if (isNetworkAvailable() && checkHealth()) {
                send(NetworkStatus.Available)
            } else {
                send(NetworkStatus.Unavailable)
            }
        }

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
        .flowOn(Dispatchers.IO)

    private fun isNetworkAvailable(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private suspend fun checkHealth(): Boolean {
        return try {
            val response = api.checkHealth()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
