package com.example.sy43___ae_app.DataBase
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime
import java.time.LocalDateTime
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.upsert
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.Duration


class dataBaseManager(client: HttpClient) {
    val apiService: ApiService = ApiServiceImpl(client)
    val clubService = clubService(apiService)
    val newService = newService(apiService)
    val repository = Repository()

    companion object {
        // On stocke l'instance ici pour y accéder partout
        var instance by mutableStateOf<dataBaseManager?>(null)
            private set

        suspend fun init(activity: MainActivity) {
            withContext(Dispatchers.IO) {
                Log.d("DB_Loggin", "init Start")
                Database.connect("jdbc:h2:mem:AE;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

                transaction {
                    SchemaUtils.create(
                        Clubs,
                        News,
                        NewsPagination,
                        Members
                    )
                }

                val client = HttpClient(CIO) {
                    install(ContentNegotiation) {
                        json(Json { ignoreUnknownKeys = true })
                    }
                }
                val newManager = dataBaseManager(client)
                newManager.refresh()

                Log.d("DB_Loggin", "init End")
                withContext(Dispatchers.Main) {
                    instance = newManager
                }
            }
        }
    }

    suspend fun refresh(){
        newRefresh()
    }

    suspend fun newRefresh(){
        Log.d("DB_Loggin", "newRefresh Start")
        val now = LocalDate.now().toString()
        val newDTO = newService.getNews(now)
        try {
            withContext(Dispatchers.IO) {
                transaction {
                    newDTO.forEach { result ->
                        Clubs.upsert {
                            it[id] = result.news.club.id
                            it[name] = result.news.club.name
                            it[logo] = result.news.club.logo
                            it[url] = result.news.club.url
                            it[shortDescription] = result.news.club.shortDescription
                        }

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
                            it[newsDetailId] = result.news.id

                            // Format date to french hours
                            it[startDate] = ZonedDateTime.parse(result.startDate)
                                .withZoneSameInstant(ZoneId.of("Europe/Paris"))
                                .toLocalDateTime()
                            it[endDate] = ZonedDateTime.parse(result.endDate)
                                .withZoneSameInstant(ZoneId.of("Europe/Paris"))
                                .toLocalDateTime()

                        }
                    }
                }
            }
        } catch(e : Exception){
            Log.d("DB_Loggin", "newRefresh ERROR : $e")
        }

        Log.d("DB_Loggin", "newRefresh End")
    }
}