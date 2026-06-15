package com.example.sy43___ae_app.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.sy43___ae_app.Back.DataBase.dataBaseManager
import com.example.sy43___ae_app.Back.FrontDTO.ClubUI
import com.example.sy43___ae_app.ui.utils.UrlImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ClubsScreen - Displays a list of all clubs
 *
 * Features:
 * - List view of all clubs with logo, name, and short description
 * - Click on a club to see detailed information
 * - Detailed view shows club members, description, and website link
 * - Back button to return to list view
 * - Handles loading, error, and empty states
 */
@Composable
fun ClubsScreen(modifier: Modifier = Modifier, db: dataBaseManager?) {
    var clubs by remember { mutableStateOf<List<ClubUI>>(emptyList()) }
    var selectedClub by remember { mutableStateOf<ClubUI?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    var hasLoaded by remember { mutableStateOf(false) }

    // Load clubs from database on initialization
    LaunchedEffect(db) {
        if (db == null) {
            hasLoaded = false
            return@LaunchedEffect
        }

        try {
            val result = withContext(Dispatchers.IO) {
                db.repository.getAllClubs()
            }
            clubs = result
            errorMessage = ""
        } catch (e: Exception) {
            Log.e("CLUBS_DEBUG", "Erreur dans le ClubsScreen", e)
            errorMessage = e.localizedMessage ?: "Erreur inconnue"
            clubs = emptyList()
        } finally {
            hasLoaded = true
        }
    }

    when {
        // Loading state
        db == null && !hasLoaded -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Chargement des clubs...", style = MaterialTheme.typography.titleMedium)
            }
        }

        // Error state
        errorMessage.isNotBlank() -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = errorMessage, style = MaterialTheme.typography.bodyMedium)
            }
        }

        // Empty state
        clubs.isEmpty() -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Aucun club disponible", style = MaterialTheme.typography.titleMedium)
            }
        }

        // Show club detail if one is selected
        selectedClub != null -> {
            ClubDetailView(
                club = selectedClub!!,
                onBackClick = { selectedClub = null },
                modifier = modifier
            )
        }

        // Show clubs list
        else -> {
            ClubsListView(
                clubs = clubs,
                onClubClick = { selectedClub = it },
                modifier = modifier
            )
        }
    }
}

/**
 * ClubsListView - Displays a scrollable list of all clubs
 * Each club shows logo, name, and short description
 */
@Composable
private fun ClubsListView(
    clubs: List<ClubUI>,
    onClubClick: (ClubUI) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(clubs.size) { index ->
            val club = clubs[index]
            ClubListItem(club = club, onClick = { onClubClick(club) })
        }
    }
}

/**
 * ClubListItem - Single club card in the list view
 * Shows club logo, name, and short description
 */
@Composable
private fun ClubListItem(
    club: ClubUI,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Club logo
            UrlImage(
                url = club.logo,
                contentDescription = "Logo de ${club.name}",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            // Club info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Club name
                Text(
                    text = club.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.fillMaxWidth()
                )

                // Short description
                if (!club.shortDescription.isNullOrBlank()) {
                    Text(
                        text = club.shortDescription ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }

                // Member count
                Text(
                    text = "${club.members.size} membre${if (club.members.size > 1) "s" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

/**
 * ClubDetailView - Shows detailed information about a selected club
 * Displays club logo, name, description, members list, and website link
 */
@Composable
private fun ClubDetailView(
    club: ClubUI,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.tertiary)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = MaterialTheme.colorScheme.onTertiary
                )
            }

            Text(
                text = "Détails du club",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(48.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Club logo - large
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                UrlImage(
                    url = club.logo,
                    contentDescription = "Logo de ${club.name}",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            // Club name
            Text(
                text = club.name,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            // Club description
            if (!club.shortDescription.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = club.shortDescription ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Website link button
            if (club.url.isNotBlank()) {
                Button(
                    onClick = {
                        // Open URL in browser - implementation depends on context
                        // For now, we'll just show placeholder
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "Visiter le site web",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // Members section
            Text(
                text = "Membres (${club.members.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            // Members list
            if (club.members.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    club.members.forEachIndexed { index, member ->
                        MemberCard(member = member)
                        if (index < club.members.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                thickness = 1.dp
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Aucun membre trouvé",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * MemberCard - Displays information about a club member
 * Shows member name, nickname, and user ID
 */
@Composable
private fun MemberCard(member: com.example.sy43___ae_app.Back.FrontDTO.MemberUI) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Member full name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = member.first_name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Text(
                    text = member.last_name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }

            // Nickname
            if (member.nickname.isNotBlank()) {
                Text(
                    text = "@${member.nickname}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // User ID
            Text(
                text = "ID: ${member.user}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

