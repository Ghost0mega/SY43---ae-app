package com.example.sy43___ae_app.DataBase
import java.time.LocalDate
import com.example.sy43___ae_app.MainActivity
import org.jetbrains.exposed.v1.jdbc.Database

import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import com.example.sy43___ae_app.DataBase.ApiServices.ApiService
import com.example.sy43___ae_app.DataBase.ApiServices.ApiServiceImpl
import com.example.sy43___ae_app.DataBase.ApiServices.Services.clubService
import com.example.sy43___ae_app.DataBase.ApiServices.Services.newService
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.jdbc.upsert


class dataBase {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    val apiService: ApiService = ApiServiceImpl(client)
    val clubService = clubService(apiService)
    val newService = newService(apiService)
    companion object  {
        fun init(activity: MainActivity) {
            Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")

            transaction {

            }

        }
    }

    suspend fun refresh(){
        newRefresh()
    }

    suspend fun newRefresh(){
        val now = LocalDate.now().toString()
        val newDTO = newService.getNews(now)

        withContext(Dispatchers.IO) {
            transaction {
                newDTO.forEach { result ->
                    News.upsert {
                        it[id] = result.news.id
                        it[title] = result.news.title
                        it[summary] = result.news.summary
                        it[isPublished] = result.news.isPublished
                        it[url] = result.news.url
                        it[clubId] = result.news.club.id
                    }

                    NewsPagination.upsert {
                        it[id] = result.id
                        it[startDate] = LocalDateTime.parse(result.startDate)
                        it[endDate] = LocalDateTime.parse(result.startDate)
                    }

                    Clubs.upsert {
                        it[id] = result.news.club.id
                        it[name] = result.news.club.name
                        it[logo] = result.news.club.logo
                        it[url] = result.news.club.url
                        it[shortDescription] = result.news.club.short_description
                    }
                }
            }
        }
    }
}