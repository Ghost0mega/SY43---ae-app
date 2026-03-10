package com.example.sy43___ae_app

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
import com.example.sy43___ae_app.ui.theme.SY43aeappTheme

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

// Il est préférable de créer le client en dehors du composant pour pouvoir le réutiliser
val ktorClient = HttpClient(CIO)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

@Composable
fun ColorTestBlock(modifier: Modifier = Modifier) {

    var apiResponse by remember { mutableStateOf("Chargement des données API...") }

    // 2. Lancement de la requête réseau au démarrage de l'écran
    LaunchedEffect(Unit) {
        try {
            // Requête GET vers une API de test (JSONPlaceholder)
            val response: HttpResponse = ktorClient.get("https://ae.utbm.fr/api/user")
            apiResponse = response.bodyAsText() // On lit le corps de la réponse en texte
        } catch (e: Exception) {
            apiResponse = "Erreur de connexion : ${e.localizedMessage}"
        }
    }

    val colors = listOf(
        Triple("Primary", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary),
        Triple("Secondary", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.onSecondary),
        Triple("Tertiary", MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.onTertiary),
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