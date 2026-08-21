package com.belbetsapp.nxmzgd.presentation.features.playground

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.belbetsapp.nxmzgd.R
import com.belbetsapp.nxmzgd.data.api.FixtureItem
import com.belbetsapp.nxmzgd.data.repo.FootballRepository
import com.belbetsapp.nxmzgd.data.store.AppStorage
import com.belbetsapp.nxmzgd.presentation.features.matches.FixtureCard
import com.belbetsapp.nxmzgd.presentation.navigation.RootRoute
import com.belbetsapp.nxmzgd.presentation.components.ScreenHeader
import com.belbetsapp.nxmzgd.presentation.theme.AccentGold
import com.belbetsapp.nxmzgd.presentation.theme.BrandPrimary
import com.belbetsapp.nxmzgd.presentation.theme.BrandPrimaryDark
import com.belbetsapp.nxmzgd.presentation.theme.LiveGreen
import com.belbetsapp.nxmzgd.presentation.theme.SurfaceCard
import com.belbetsapp.nxmzgd.presentation.theme.SurfaceSoft
import com.belbetsapp.nxmzgd.presentation.theme.TextMuted
import com.belbetsapp.nxmzgd.presentation.theme.TextPrimary
import com.belbetsapp.nxmzgd.presentation.theme.TextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun PlaygroundScreen(storage: AppStorage, rootNav: NavHostController) {
    val scope = rememberCoroutineScope()
    val balance by storage.playgroundBalanceFlow.collectAsState(initial = AppStorage.DEFAULT_BALANCE)
    val historyText by storage.playgroundHistoryFlow.collectAsState(initial = "")
    val records = remember(historyText) { WagerSerializer.decode(historyText) }

    var fixtures by remember { mutableStateOf<List<FixtureItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var showHistory by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }

    suspend fun reload(force: Boolean) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getDefault() }.format(Date())
        val list = (FootballRepository.getLiveFixtures(forceRefresh = force) +
                FootballRepository.getFixturesByDate(today, forceRefresh = force))
            .distinctBy { it.id }
        fixtures = list

        val updated = records.map { r ->
            if (r.status != WagerStatus.PENDING) return@map r
            val fx = list.firstOrNull { it.id == r.fixtureId } ?: return@map r
            if (!fx.isFinished || fx.homeGoals == null || fx.awayGoals == null) return@map r
            val winner = when {
                fx.homeGoals > fx.awayGoals -> WagerPick.HOME
                fx.homeGoals < fx.awayGoals -> WagerPick.AWAY
                else -> WagerPick.DRAW
            }
            val newStatus = if (winner == r.pick) WagerStatus.WON else WagerStatus.LOST
            r.copy(status = newStatus, finalHome = fx.homeGoals, finalAway = fx.awayGoals)
        }
        if (updated != records) {
            val delta = updated.zip(records).sumOf { (n, o) ->
                if (n.status == WagerStatus.WON && o.status == WagerStatus.PENDING) n.potentialReturn else 0
            }
            if (delta != 0) storage.savePlaygroundBalance(balance + delta)
            storage.savePlaygroundHistory(WagerSerializer.encode(updated))
        }
    }

    LaunchedEffect(Unit) {
        loading = true
        reload(false)
        loading = false
    }

    Column(modifier = Modifier.fillMaxSize().background(SurfaceSoft)) {
        ScreenHeader(
            title = stringResource(R.string.playground_title),
            subtitle = stringResource(R.string.playground_subtitle),
            icon = Icons.Default.Casino,
            trailing = {
                IconButton(onClick = { showHistory = true }) {
                    Icon(Icons.Default.History, stringResource(R.string.history), tint = Color.White)
                }
                IconButton(onClick = {
                    scope.launch { reload(true) }
                }) {
                    Icon(Icons.Default.Refresh, stringResource(R.string.refresh), tint = Color.White)
                }
            }
        )

        BalanceBanner(balance = balance, records = records, onReset = { confirmReset = true })

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Column
        }

        val candidates = remember(fixtures) {
            fixtures.filter { it.isLive || it.isUpcoming }.take(40)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.pick_match),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (candidates.isEmpty()) {
                item {
                    EmptyBox(stringResource(R.string.no_playground_fixtures))
                }
            } else {
                items(candidates, key = { it.id }) { fx ->
                    FixtureCard(fx, onClick = { rootNav.navigate(RootRoute.PlaceWager.build(fx.id)) })
                }
            }
        }
    }

    if (showHistory) {
        HistoryDialog(records = records, onDismiss = { showHistory = false })
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text(stringResource(R.string.confirm_reset)) },
            text = { Text(stringResource(R.string.confirm_reset_message)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmReset = false
                    scope.launch { storage.resetPlayground() }
                }) { Text(stringResource(R.string.reset), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun BalanceBanner(balance: Int, records: List<WagerRecord>, onReset: () -> Unit) {
    val pendingCount = records.count { it.status == WagerStatus.PENDING }
    val won = records.count { it.status == WagerStatus.WON }
    val lost = records.count { it.status == WagerStatus.LOST }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(BrandPrimaryDark, BrandPrimary, AccentGold.copy(alpha = 0.85f))))
            .padding(18.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.virtual_balance_label).uppercase(), style = MaterialTheme.typography.labelMedium, color = Color.White.copy(0.85f), fontWeight = FontWeight.Bold)
                    Text(
                        text = "$balance",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(stringResource(R.string.coins), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.85f))
                }
                IconButton(onClick = onReset) {
                    Icon(Icons.Default.RestartAlt, stringResource(R.string.reset), tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MiniStat(stringResource(R.string.stat_pending), pendingCount.toString())
                MiniStat(stringResource(R.string.stat_won), won.toString())
                MiniStat(stringResource(R.string.stat_lost), lost.toString())
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Color.White.copy(0.9f), style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.width(6.dp))
            Text(value, color = Color.White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun EmptyBox(text: String) {
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
private fun HistoryDialog(records: List<WagerRecord>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } },
        title = { Text(stringResource(R.string.bet_history), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary) },
        text = {
            if (records.isEmpty()) {
                Text(stringResource(R.string.no_predictions_yet), color = TextSecondary)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(records.sortedByDescending { it.placedAtMs }) { r -> HistoryRow(r) }
                }
            }
        }
    )
}

@Composable
private fun HistoryRow(r: WagerRecord) {
    val statusColor = when (r.status) {
        WagerStatus.WON -> LiveGreen
        WagerStatus.LOST -> MaterialTheme.colorScheme.primary
        WagerStatus.REFUND -> TextSecondary
        WagerStatus.PENDING -> TextMuted
    }
    val pickLabel = when (r.pick) {
        WagerPick.HOME -> stringResource(R.string.wager_home)
        WagerPick.DRAW -> stringResource(R.string.wager_draw)
        WagerPick.AWAY -> stringResource(R.string.wager_away)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceSoft)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${r.homeName} vs ${r.awayName}",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(r.status.name, style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.Bold)
            }
        }
        Text(
            text = stringResource(R.string.history_entry, pickLabel, r.odds.toString(), r.stake, r.potentialReturn),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        if (r.finalHome != null && r.finalAway != null) {
            Text(
                text = stringResource(R.string.final_score, r.finalHome!!, r.finalAway!!),
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}
