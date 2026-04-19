package com.example.sy43___ae_app.DataBase.ApiServices

import io.ktor.client.request.HttpRequestBuilder

interface ApiService {
    suspend fun request(
        request : String,
        block: HttpRequestBuilder.() -> Unit
    ): String
}