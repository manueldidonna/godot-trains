package com.manueldidonna.godottrains.domain

import android.annotation.SuppressLint
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@SuppressLint("MissingPermission")
class CheckNetworkStatusUseCase @Inject constructor(
    private val connectivityManager: ConnectivityManager
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun isConnectedToNetwork(): Flow<Boolean> {
        return combine(
            isNetworkTransportAvailable(NetworkCapabilities.TRANSPORT_CELLULAR),
            isNetworkTransportAvailable(NetworkCapabilities.TRANSPORT_WIFI),
        ) { cellular, wifi -> cellular || wifi }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun isNetworkTransportAvailable(transportType: Int): Flow<Boolean> {
        return callbackFlow {
            send(networkCapabilities?.hasTransport(transportType) ?: false)
            val networkStatusCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.d("Network Status", "$transportType available")
                    trySend(true)
                }

                override fun onLost(network: Network) {
                    Log.d("Network Status", "$transportType lost")
                    trySend(false)
                }
            }
            val request = NetworkRequest.Builder().addTransportType(transportType).build()
            connectivityManager.registerNetworkCallback(request, networkStatusCallback)

            awaitClose {
                connectivityManager.unregisterNetworkCallback(networkStatusCallback)
            }
        }
    }

    private inline val networkCapabilities: NetworkCapabilities?
        get() = connectivityManager.activeNetwork?.let(connectivityManager::getNetworkCapabilities)
}
