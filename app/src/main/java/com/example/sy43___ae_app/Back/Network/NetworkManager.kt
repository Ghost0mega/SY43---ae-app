package com.example.sy43___ae_app.Back.Network

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.annotation.RequiresPermission
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class NetworkManager(val context : Context) {
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun hasActiveInternetConnection(): Boolean {
        if (isNetworkAvailable()) {
            try {
                val urlc = (URL("https://ae.utbm.fr/").openConnection()) as HttpURLConnection
                urlc.setRequestProperty("User-Agent", "Test")
                urlc.setRequestProperty("Connection", "close")
                urlc.setConnectTimeout(1500)
                urlc.connect()
                return (urlc.getResponseCode() == 200)
            } catch (e: IOException) {
                Log.e("ApiService", "Error checking internet connection", e)
            }
        } else {
            Log.d("ApiService", "No network available!")
        }
        return false
    }
}