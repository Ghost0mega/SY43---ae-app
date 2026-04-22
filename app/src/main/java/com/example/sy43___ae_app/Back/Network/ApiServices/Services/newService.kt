package com.example.sy43___ae_app.Back.Network.ApiServices.Services

import android.util.Log
import com.example.sy43___ae_app.Back.Network.ApiServices.NetworkDTO.NewsDateResult
import com.example.sy43___ae_app.Back.Network.ApiServices.NetworkDTO.NewsPaginationResponse
import io.ktor.client.request.parameter
import com.example.sy43___ae_app.Back.Network.ApiServices.ApiService
import kotlinx.serialization.json.Json


class newService(private val apiService: ApiService) {

    /*
     * Date format years-month-day
     *
     */
    suspend fun getNews(afterDate: String): List<NewsDateResult> {
        val apiResponse = apiService.request("news/date") {
            parameter("after", afterDate)
            parameter("is_publish", true)
        }

        return try {
            if (apiResponse != null) {
                Json.decodeFromString<NewsPaginationResponse>(apiResponse).results
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("NewService", "Erreur parsing JSON", e)
            emptyList()
        }
    }
}