package com.example.sy43___ae_app.Back.Network.ApiServices

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import androidx.annotation.RequiresPermission
import io.ktor.client.request.HttpRequestBuilder

interface ApiService {
    suspend fun request(
        request : String,
        block: HttpRequestBuilder.() -> Unit
    ): String?
}