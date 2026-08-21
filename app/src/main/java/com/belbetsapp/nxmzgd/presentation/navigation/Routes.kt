package com.belbetsapp.nxmzgd.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Stadium
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Stadium
import com.belbetsapp.nxmzgd.R
import androidx.compose.ui.graphics.vector.ImageVector

sealed class RootRoute(val path: String) {
    data object Hub : RootRoute("hub")
    data object AssistantChat : RootRoute("assistant/{topicId}") {
        fun build(topicId: String) = "assistant/$topicId"
    }
    data object LeagueTable : RootRoute("table/{leagueId}/{season}/{name}") {
        fun build(leagueId: Int, season: Int, name: String) = "table/$leagueId/$season/$name"
    }
    data object PlaceWager : RootRoute("wager/{fixtureId}") {
        fun build(fixtureId: Long) = "wager/$fixtureId"
    }
}

enum class HubTab(
    val labelRes: Int,
    val selected: ImageVector,
    val unselected: ImageVector
) {
    Matches(R.string.tab_matches, Icons.Filled.SportsSoccer, Icons.Outlined.SportsSoccer),
    Assistant(R.string.tab_assistant, Icons.Filled.Psychology, Icons.Outlined.Psychology),
    Playground(R.string.tab_playground, Icons.Filled.Stadium, Icons.Outlined.Stadium),
    Rankings(R.string.tab_rankings, Icons.AutoMirrored.Filled.ShowChart, Icons.AutoMirrored.Outlined.ShowChart),
    Account(R.string.tab_account, Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle)
}
