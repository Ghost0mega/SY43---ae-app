package com.example.sy43___ae_app.DataBase.ApiServices.Services

import com.example.sy43___ae_app.DataBase.ApiServices.NetworkDTO.NewsDateResult
import com.example.sy43___ae_app.DataBase.ApiServices.NetworkDTO.NewsPaginationResponse
import io.ktor.client.request.parameter
import com.example.sy43___ae_app.DataBase.ApiServices.ApiService
import kotlinx.serialization.json.Json


class newService(private val apiService: ApiService) {

    /*
     * Date format years-month-day
     *
     */
    suspend fun getNews(afterDate: String): ArrayList<NewsDateResult> {
        val apiResponse: String = apiService.request("news/date") {
            parameter("after", afterDate)
            parameter("is_publish", true)
        }
        val pagination = Json.decodeFromString<NewsPaginationResponse>(apiResponse)
        return pagination.results
    }
}