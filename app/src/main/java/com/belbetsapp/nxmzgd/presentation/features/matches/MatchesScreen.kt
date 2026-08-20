package com.belbetsapp.nxmzgd.presentation.features.matches

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.belbetsapp.nxmzgd.data.api.FixtureItem
import com.belbetsapp.nxmzgd.data.repo.FootballRepository
import com.belbetsapp.nxmzgd.presentation.navigation.RootRoute
import com.belbetsapp.nxmzgd.presentation.components.ScreenHeader
import com.belbetsapp.nxmzgd.presentation.theme.AccentGold
import com.belbetsapp.nxmzgd.presentation.theme.BrandPrimary
import com.belbetsapp.nxmzgd.presentation.theme.BrandPrimarySoft
import com.belbetsapp.nxmzgd.presentation.theme.DividerGray
import com.belbetsapp.nxmzgd.presentation.theme.LiveGreen
import com.belbetsapp.nxmzgd.presentation.theme.SurfaceCard
import com.belbetsapp.nxmzgd.presentation.theme.SurfaceSoft
import com.belbetsapp.nxmzgd.presentation.theme.TextMuted
import com.belbetsapp.nxmzgd.presentation.theme.TextPrimary
import com.belbetsapp.nxmzgd.presentation.theme.TextSecondary
import androidx.compose.ui.draw.shadow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun MatchesScreen(rootNav: NavHostController) {
    var live by remember { mutableStateOf<List<FixtureItem>>(emptyList()) }
    var today by remember { mutableStateOf<List<FixtureItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    suspend fun load(force: Boolean) {
        val now = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getDefault() }.format(Date())
        live = FootballRepository.getLiveFixtures(forceRefresh = force)
        today = FootballRepository.getFixturesByDate(now, forceRefresh = force)
    }

    LaunchedEffect(Unit) {
        loading = true
        load(false)
        loading = false
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            try {
                live = FootballRepository.getLiveFixtures(forceRefresh = true)
            } catch (_: Exception) {}
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(SurfaceSoft)) {
        ScreenHeader(
            title = stringResource(R.string.matches_title),
            subtitle = stringResource(R.string.matches_subtitle),
            icon = Icons.Default.SportsSoccer,
            trailing = {
                IconButton(
                    onClick = {
                        if (!refreshing) {
                            refreshing = true
                            scope.launch {
                                try { load(true) } finally { refreshing = false }
                            }
                        }
                    }
                ) {
                    if (refreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh), tint = Color.White)
                    }
                }
            }
        )

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                StatsRow(liveCount = live.size, todayCount = today.size)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                SectionLabel(
                    text = stringResource(R.string.section_live),
                    accent = LiveGreen,
                    badge = live.size.toString()
                )
            }

            if (live.isEmpty()) {
                item { EmptyBlock(text = stringResource(R.string.no_live_matches)) }
            } else {
                items(live, key = { "live_${it.id}" }) { fx ->
                    FixtureCard(fx, onClick = { rootNav.navigate(RootRoute.PlaceWager.build(fx.id)) })
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionLabel(
                    text = stringResource(R.string.section_today),
                    accent = MaterialTheme.colorScheme.primary,
                    badge = today.size.toString()
                )
            }

            if (today.isEmpty()) {
                item { EmptyBlock(text = stringResource(R.string.no_today_matches)) }
            } else {
                items(today.take(40), key = { "today_${it.id}" }) { fx ->
                    FixtureCard(fx, onClick = { rootNav.navigate(RootRoute.PlaceWager.build(fx.id)) })
                }
            }
        }
    }
}

@Composable
private fun StatsRow(liveCount: Int, todayCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatTile(label = stringResource(R.string.stat_live), value = liveCount.toString(), color = LiveGreen, modifier = Modifier.weight(1f))
        StatTile(label = stringResource(R.string.stat_today), value = todayCount.toString(), color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
        StatTile(label = stringResource(R.string.stat_leagues), value = FootballRepository.featuredLeagues.size.toString(), color = AccentGold, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatTile(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(top = 12.dp, bottom = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(value, fontWeight = FontWeight.ExtraBold, color = color, style = MaterialTheme.typography.headlineMedium)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SectionLabel(text: String, accent: Color, badge: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .background(accent, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = accent
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = badge,
            style = MaterialTheme.typography.labelMedium,
            color = TextMuted
        )
    }
}

@Composable
private fun EmptyBlock(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun FixtureCard(fx: FixtureItem, onClick: (() -> Unit)? = null) {
    val mod = Modifier
        .fillMaxWidth()
        .shadow(3.dp, RoundedCornerShape(20.dp))
        .clip(RoundedCornerShape(20.dp))
        .background(SurfaceCard)
        .let { if (onClick != null) it.clickable { onClick() } else it }
        .padding(16.dp)

    Column(mod, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!fx.leagueLogo.isNullOrBlank()) {
                AsyncImage(
                    model = fx.leagueLogo,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = "${fx.country} • ${fx.leagueName}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.weight(1f))
            StatusPill(fx)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            TeamBlock(fx.homeName, fx.homeLogo, modifier = Modifier.weight(1f))
            ScoreBlock(fx)
            TeamBlock(fx.awayName, fx.awayLogo, modifier = Modifier.weight(1f), reverse = true)
        }
    }
}

@Composable
private fun TeamBlock(name: String, logo: String?, modifier: Modifier = Modifier, reverse: Boolean = false) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (reverse) Arrangement.End else Arrangement.Start
    ) {
        if (reverse) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(8.dp))
            LogoBox(logo)
        } else {
            LogoBox(logo)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LogoBox(logo: String?) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(SurfaceSoft),
        contentAlignment = Alignment.Center
    ) {
        if (!logo.isNullOrBlank()) {
            AsyncImage(model = logo, contentDescription = null, modifier = Modifier.size(26.dp))
        } else {
            Icon(Icons.Default.SportsSoccer, null, tint = TextMuted, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun ScoreBlock(fx: FixtureItem) {
    val score = when {
        fx.homeGoals != null && fx.awayGoals != null -> "${fx.homeGoals} : ${fx.awayGoals}"
        else -> formatKickoff(fx.date)
    }
    val color = when {
        fx.isLive -> LiveGreen
        fx.isFinished -> TextSecondary
        else -> MaterialTheme.colorScheme.primary
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
        Text(score, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = color)
    }
}

@Composable
private fun StatusPill(fx: FixtureItem) {
    val (label, bg, fg) = when {
        fx.isLive -> {
            val elapsedTxt = fx.elapsed?.let { "${it}'" } ?: stringResource(R.string.status_live)
            Triple("● $elapsedTxt", LiveGreen.copy(alpha = 0.12f), LiveGreen)
        }
        fx.isFinished -> Triple(stringResource(R.string.status_ft), DividerGray, TextSecondary)
        else -> Triple(stringResource(R.string.status_soon), BrandPrimarySoft, BrandPrimary)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = fg, fontWeight = FontWeight.Bold)
    }
}

private fun formatKickoff(iso: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
        val out = SimpleDateFormat("HH:mm", Locale.US).apply { timeZone = TimeZone.getDefault() }
        out.format(parser.parse(iso) ?: return iso)
    } catch (_: Exception) {
        iso.substringAfter('T').substring(0, 5)
    }
}
