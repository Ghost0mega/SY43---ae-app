package com.example.sy43___ae_app.Back.Network.ApiServices

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.sy43___ae_app.Back.Network.ApiServices.NetworkDTO.NewsPaginationResponse
import com.example.sy43___ae_app.Back.Network.NetworkManager
import com.example.sy43___ae_app.BuildConfig
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


open class ApiServiceImpl(
    val client : HttpClient,
    val networkManager : NetworkManager,
) : ApiService {

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override suspend fun request(
        path: String,
        block: HttpRequestBuilder.() -> Unit
    ): String? {
        // Test connectivité
        if (!networkManager.isNetworkAvailable()) {
            Log.d("Api_Request", "No Internet")
            return null
        }

        return try {
            val response = client.get("https://ae.utbm.fr/api/$path") {
                header("X-APIKey", BuildConfig.ApiKey)
                block()
            }
            response.body()

        } catch (e: Exception) {
            Log.e("Api_Request", "Error: ${e.localizedMessage}")
            null
        }
    }
}



