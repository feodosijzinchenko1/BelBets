package com.belbetsapp.nxmzgd.presentation.features.rankings

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.belbetsapp.nxmzgd.R
import com.belbetsapp.nxmzgd.data.api.LeagueInfo
import com.belbetsapp.nxmzgd.data.repo.FootballRepository
import com.belbetsapp.nxmzgd.presentation.navigation.RootRoute
import com.belbetsapp.nxmzgd.presentation.components.ScreenHeader
import androidx.compose.ui.draw.shadow
import com.belbetsapp.nxmzgd.presentation.theme.SurfaceCard
import com.belbetsapp.nxmzgd.presentation.theme.SurfaceSoft
import com.belbetsapp.nxmzgd.presentation.theme.TextMuted
import com.belbetsapp.nxmzgd.presentation.theme.TextPrimary
import com.belbetsapp.nxmzgd.presentation.theme.TextSecondary

@Composable
fun RankingsScreen(rootNav: NavHostController) {
    val leagues = FootballRepository.featuredLeagues

    Column(modifier = Modifier.fillMaxSize().background(SurfaceSoft)) {
        ScreenHeader(
            title = stringResource(R.string.rankings_title),
            subtitle = stringResource(R.string.rankings_subtitle),
            icon = Icons.Default.EmojiEvents
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(leagues) { league ->
                LeagueRow(league) {
                    rootNav.navigate(
                        RootRoute.LeagueTable.build(
                            league.id,
                            league.season,
                            league.name.replace("/", " ")
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun LeagueRow(league: LeagueInfo, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceCard)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(SurfaceSoft),
            contentAlignment = Alignment.Center
        ) {
            if (!league.logo.isNullOrBlank()) {
                AsyncImage(model = league.logo, contentDescription = null, modifier = Modifier.size(34.dp))
            } else {
                Icon(Icons.Default.Public, null, tint = TextMuted)
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = league.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "${league.country} • ${league.season}/${league.season + 1}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextMuted
        )
    }
}
