package com.example.sy43___ae_app

import NewsDateResult
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sy43___ae_app.DataBase.ApiServices.ApiServiceImpl
import com.example.sy43___ae_app.DataBase.ApiServices.Services.newService
import com.example.sy43___ae_app.DataBase.dataBase
import com.example.sy43___ae_app.ui.theme.SY43aeappTheme
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation


import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

// Il est préférable de créer le client en dehors du composant pour pouvoir le réutiliser

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataBase.init(this)

        // UI
        enableEdgeToEdge()
        setContent {
            SY43aeappTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ColorTestBlock(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@SuppressLint("RememberReturnType")
@Composable
fun ColorTestBlock(modifier: Modifier = Modifier) {

    var apiResponse by remember { mutableStateOf("Chargement des données API...") }
    var newsList by remember { mutableStateOf<List<NewsDateResult>>(emptyList()) }

    // test faudra bouger ca hors du front
    val client = remember {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
    val apiService = remember { ApiServiceImpl(client) }
    val newService = remember { newService(apiService) }

    // Appelle une seule fois pour pas faire crash le front
    LaunchedEffect(Unit) {
        try {
            val results = newService.getNews("2026-04-17")
            newsList = results
            apiResponse = "Données récupérées : ${results.joinToString(separator = ", ") {
                    it.news.title
            }} news"

        } catch (e: Exception) {
            apiResponse = "Erreur : ${e.localizedMessage}"
        }
    }


    LaunchedEffect(Unit) {

    }

    val colors = listOf(
        Triple("Primary", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary),
        Triple(
            "Secondary",
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.onSecondary
        ),
        Triple(
            "Tertiary",
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.onTertiary
        ),
    )

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Résultat de l'API :",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = apiResponse,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        colors.forEach { (label, bgColor, textColor) ->
            Box(
                modifier = Modifier
                    .size(width = 200.dp, height = 60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = textColor,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SY43aeappTheme {
        ColorTestBlock()
    }
}