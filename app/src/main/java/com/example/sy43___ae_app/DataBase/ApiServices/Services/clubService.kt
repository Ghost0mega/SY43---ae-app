package com.example.sy43___ae_app.DataBase.ApiServices.Services

import NewsDateResult
import NewsPaginationResponse
import com.example.sy43___ae_app.DataBase.ApiServices.ApiService
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.serialization.json.Json

class clubService(private val apiService: ApiService) {

    /*
     *
     *
     */

    suspend fun getClubByID(id: Int): ArrayList<NewsDateResult> {
        val apiResponse: String = apiService.request("club/$id"){}
        val pagination = Json.decodeFromString<NewsPaginationResponse>(apiResponse)
        return pagination.results
    }

}