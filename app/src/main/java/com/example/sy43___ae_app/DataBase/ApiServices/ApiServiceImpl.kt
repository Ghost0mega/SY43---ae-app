package com.example.sy43___ae_app.DataBase.ApiServices

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import com.example.sy43___ae_app.BuildConfig


open class ApiServiceImpl(val client : HttpClient) : ApiService {

    override suspend fun request(
        request : String,
        block: HttpRequestBuilder.() -> Unit
    ): String {
        return try {
            val response: HttpResponse = client.get("https://ae.utbm.fr/api/$request") {
                header("X-APIKey", BuildConfig.ApiKey)
                block()
            }
            response.bodyAsText()

        } catch (e: Exception) {
            "Erreur de connexion : ${e.localizedMessage}"
        }
    }

    protected fun refresh(){

    }

    /*
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

    // plus tard
    suspend fun fetchData() {
        // client.get()
    }

    */
    /*
    *   generic method to send request
    *   @param https://ae.utbm.fr/api/ + request
    *   Exemple for block :
    *    request("news/date") {
    *        parameter("after", "2024-01-01")
    *        parameter("is_publish", true)
    *    }
    *
     */

}


