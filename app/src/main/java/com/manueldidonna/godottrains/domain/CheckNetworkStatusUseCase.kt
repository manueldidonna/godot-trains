package com.manueldidonna.godottrains.domain

import android.annotation.SuppressLint
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class CheckNetworkStatusUseCase @Inject constructor(
    private val connectivityManager: ConnectivityManager
) {

    @SuppressLint("MissingPermission")
    @OptIn(ExperimentalCoroutinesApi::class)
    fun isConnectedToNetwork(): Flow<Boolean> {
        return callbackFlow {
            send(getCurrentNetworkStatus())
            val networkStatusCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onUnavailable() {
                    trySend(false)
                }

                override fun onAvailable(network: Network) {
                    trySend(true)
                }

                override fun onLost(network: Network) {
                    trySend(false)
                }
            }

            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager.registerNetworkCallback(request, networkStatusCallback)

            awaitClose {
                connectivityManager.unregisterNetworkCallback(networkStatusCallback)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentNetworkStatus(): Boolean {
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}
