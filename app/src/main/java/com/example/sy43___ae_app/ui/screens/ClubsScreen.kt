package com.example.sy43___ae_app.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sy43___ae_app.Back.DataBase.dataBaseManager
import com.example.sy43___ae_app.Back.FrontDTO.ClubUI
import com.example.sy43___ae_app.Back.FrontDTO.MemberUI
import com.example.sy43___ae_app.ui.utils.UrlImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ClubsScreen - Displays a list of all clubs and their details
 */
@Composable
fun ClubsScreen(modifier: Modifier = Modifier, db: dataBaseManager?) {
    var clubs by remember { mutableStateOf<List<ClubUI>>(emptyList()) }
    var selectedClub by remember { mutableStateOf<ClubUI?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    var hasLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(db) {
        if (db == null) {
            hasLoaded = false
            return@LaunchedEffect
        }

        try {
            val result = withContext(Dispatchers.IO) {
                db.repository.getAllClubs()
            }
            clubs = result.sortedBy { it.name }
            errorMessage = ""
        } catch (e: Exception) {
            Log.e("CLUBS_DEBUG", "Error loading clubs", e)
            errorMessage = e.localizedMessage ?: "Erreur inconnue"
        } finally {
            hasLoaded = true
        }
    }

    when {
        db == null && !hasLoaded -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Chargement des clubs...", style = MaterialTheme.typography.titleMedium)
            }
        }
        errorMessage.isNotBlank() -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = errorMessage, style = MaterialTheme.typography.bodyMedium)
            }
        }
        clubs.isEmpty() && hasLoaded -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Aucun club disponible", style = MaterialTheme.typography.titleMedium)
            }
        }
        else -> {
            AnimatedContent(
                targetState = selectedClub,
                transitionSpec = {
                    if (targetState != null) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it / 2 } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it / 2 } + fadeOut()
                    }
                },
                label = "ClubTransition"
            ) { targetClub ->
                if (targetClub != null) {
                    ClubDetailView(
                        club = targetClub,
                        onBackClick = { selectedClub = null },
                        modifier = modifier
                    )
                } else {
                    ClubsListView(
                        clubs = clubs,
                        onClubClick = { selectedClub = it },
                        modifier = modifier
                    )
                }
            }
        }
    }
}

@Composable
private fun ClubsListView(
    clubs: List<ClubUI>,
    onClubClick: (ClubUI) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(clubs) { club ->
            ClubCard(club = club, onClick = { onClubClick(club) })
        }
    }
}

@Composable
private fun ClubCard(club: ClubUI, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            UrlImage(
                url = club.logo,
                contentDescription = "Logo de ${club.name}",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = club.name, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = club.shortDescription ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun ClubDetailView(club: ClubUI, onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour"
                )
            }
            Text(text = club.name, style = MaterialTheme.typography.headlineMedium)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    UrlImage(
                        url = club.logo,
                        contentDescription = "Logo de ${club.name}",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                }
            }

            item {
                Text(
                    text = club.shortDescription ?: "",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (club.url.isNotBlank()) {
                item {
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(club.url))
                        context.startActivity(intent)
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Language, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Site web")
                        }
                    }
                }
            }

            if (club.members.isNotEmpty()) {
                item {
                    Text(
                        text = "Membres du bureau",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(club.members) { member ->
                    MemberCard(member = member)
                }
            }
        }
    }
}

@Composable
private fun MemberCard(member: MemberUI) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${member.first_name} ${member.last_name}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            if (member.nickname.isNotBlank()) {
                Text(text = "(${member.nickname})", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
