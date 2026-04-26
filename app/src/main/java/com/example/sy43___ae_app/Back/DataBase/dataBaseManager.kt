package com.example.sy43___ae_app.Back.DataBase

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.mutableStateOf
import com.example.sy43___ae_app.Back.Network.ApiServices.ApiService
import com.example.sy43___ae_app.Back.Network.ApiServices.ApiServiceImpl
import com.example.sy43___ae_app.Back.Network.ApiServices.Services.clubService
import com.example.sy43___ae_app.Back.Network.ApiServices.Services.newService
import com.example.sy43___ae_app.Back.Network.NetworkManager
import com.example.sy43___ae_app.MainActivity
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.datetime.LocalTime
import org.jetbrains.exposed.v1.jdbc.replace
import java.time.LocalDateTime

class dataBaseManager(
    client: HttpClient,
    val diskDB : Database,
    val networkManager: NetworkManager
    //, ramDB : Database
) {
    val apiService: ApiService = ApiServiceImpl(client, networkManager)
    val clubService = clubService(apiService)
    val newService = newService(apiService)
    val repository = Repository()

    companion object {
        // On stocke l'instance ici pour y accéder partout
        var instance by mutableStateOf<dataBaseManager?>(null)
            private set
        @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
        suspend fun init(activity: MainActivity) {
            withContext(Dispatchers.IO)
            {
                Log.d("DB_Loggin", "init Start")
                // Dans ton code, remplace l'URL de connexion

                // Stock disque dur
                val dbPath = activity.filesDir.absolutePath + "/ae_database"
                val diskDB = Database.Companion.connect(
                    "jdbc:h2:file:$dbPath;MODE=MYSQL;DB_CLOSE_DELAY=-1",
                    driver = "org.h2.Driver"
                )

                // Stock en Ram
                //val ramDB = Database.connect("jdbc:h2:mem:AE;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

                transaction(diskDB) {
                    SchemaUtils.create(
                        DbMetadata,
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

                val networkManager = NetworkManager(activity.applicationContext)
                val newManager = dataBaseManager(client, diskDB, networkManager)

                newManager.refresh()

                Log.d("DB_Loggin", "init End")
                withContext(Dispatchers.Main) {
                    instance = newManager
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    suspend fun refresh(){

        newRefresh()
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    suspend fun newRefresh() {
        Log.d("DB_Loggin", "newRefresh Start")
        val now = LocalDate.now().toString()
        try {
            withContext(Dispatchers.IO) {
                var newDTO = newService.getNews(now);

                if (newDTO.isEmpty()) {
                    if (!networkManager.isNetworkAvailable()) {
                        Log.d("DB_Loggin", "No internet")
                        return@withContext
                    }

                    if (networkManager.hasActiveInternetConnection()) {
                        newDTO = newService.getNews(now)
                    }
                }

                if (newDTO.isEmpty()) {
                    Log.d("DB_Loggin", "No news")
                    return@withContext
                }
                transaction(diskDB) {
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

                        DbMetadata.replace {
                            it[key] = "New"
                            it[lastUpdate] = LocalDateTime.now()
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