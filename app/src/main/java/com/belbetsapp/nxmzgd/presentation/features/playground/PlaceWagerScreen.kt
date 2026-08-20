package com.belbetsapp.nxmzgd.presentation.features.playground

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import com.belbetsapp.nxmzgd.R
import com.belbetsapp.nxmzgd.data.api.FixtureItem
import com.belbetsapp.nxmzgd.data.repo.FootballRepository
import com.belbetsapp.nxmzgd.data.store.AppStorage
import androidx.compose.ui.graphics.Brush
import com.belbetsapp.nxmzgd.presentation.theme.BrandPrimary
import com.belbetsapp.nxmzgd.presentation.theme.BrandPrimaryDark
import com.belbetsapp.nxmzgd.presentation.theme.BrandPrimarySoft
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
import java.util.UUID

@Composable
fun PlaceWagerScreen(
    fixtureId: Long,
    storage: AppStorage,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val balance by storage.playgroundBalanceFlow.collectAsState(initial = AppStorage.DEFAULT_BALANCE)
    val historyText by storage.playgroundHistoryFlow.collectAsState(initial = "")

    var fixture by remember { mutableStateOf<FixtureItem?>(null) }
    var loading by remember { mutableStateOf(true) }
    var pick by remember { mutableStateOf<WagerPick?>(null) }
    var stakeText by remember { mutableStateOf("") }
    var placedDialog by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    LaunchedEffect(fixtureId) {
        loading = true
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getDefault() }.format(Date())
        val all = FootballRepository.getLiveFixtures() + FootballRepository.getFixturesByDate(today)
        fixture = all.firstOrNull { it.id == fixtureId }
        loading = false
    }

    val accent = MaterialTheme.colorScheme.primary

    Column(modifier = Modifier.fillMaxSize().background(SurfaceSoft)) {
        TopBar(onBack = onBack)

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = accent)
            }
            return@Column
        }

        val fx = fixture
        if (fx == null) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.match_not_found), color = TextSecondary)
            }
            return@Column
        }

        val (oH, oD, oA) = remember(fx.id) { syntheticOdds(fx.id) }
        val canBet = fx.isLive || fx.isUpcoming

        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            MatchHeader(fx)

            if (canBet) {
                Text(
                    stringResource(R.string.wager_choose_outcome),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = accent
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OddsButton(stringResource(R.string.wager_home), oH, pick == WagerPick.HOME, Modifier.weight(1f)) { pick = WagerPick.HOME }
                    OddsButton(stringResource(R.string.wager_draw), oD, pick == WagerPick.DRAW, Modifier.weight(1f)) { pick = WagerPick.DRAW }
                    OddsButton(stringResource(R.string.wager_away), oA, pick == WagerPick.AWAY, Modifier.weight(1f)) { pick = WagerPick.AWAY }
                }

                StakeInput(
                    value = stakeText,
                    onChange = { stakeText = it; errorMsg = "" },
                    balance = balance
                )

                QuickStakeRow(balance = balance) { v -> stakeText = v.toString(); errorMsg = "" }

                ReturnSummary(stake = stakeText.toIntOrNull() ?: 0, odds = when (pick) {
                    WagerPick.HOME -> oH
                    WagerPick.DRAW -> oD
                    WagerPick.AWAY -> oA
                    null -> 0.0
                })

                if (errorMsg.isNotEmpty()) {
                    Text(errorMsg, color = accent, style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = {
                        val pickValue = pick
                        val stake = stakeText.toIntOrNull() ?: 0
                        when {
                            pickValue == null -> errorMsg = context.getString(R.string.wager_select_outcome)
                            stake <= 0 -> errorMsg = context.getString(R.string.wager_enter_stake)
                            stake > balance -> errorMsg = context.getString(R.string.wager_not_enough, balance)
                            else -> {
                                val odds = when (pickValue) {
                                    WagerPick.HOME -> oH
                                    WagerPick.DRAW -> oD
                                    WagerPick.AWAY -> oA
                                }
                                val record = WagerRecord(
                                    id = UUID.randomUUID().toString(),
                                    fixtureId = fx.id,
                                    homeName = fx.homeName,
                                    awayName = fx.awayName,
                                    leagueName = fx.leagueName,
                                    pick = pickValue,
                                    odds = odds,
                                    stake = stake,
                                    placedAtMs = System.currentTimeMillis(),
                                    status = WagerStatus.PENDING
                                )
                                val existing = WagerSerializer.decode(historyText)
                                scope.launch {
                                    storage.savePlaygroundBalance(balance - stake)
                                    storage.savePlaygroundHistory(WagerSerializer.encode(existing + record))
                                    placedDialog = true
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    enabled = pick != null && (stakeText.toIntOrNull() ?: 0) > 0
                ) {
                    Icon(Icons.Default.Casino, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.wager_place), fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(vertical = 4.dp))
                }

                Text(
                    text = stringResource(R.string.virtual_disclaimer),
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceCard)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.match_finished_no_bet),
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    if (placedDialog) {
        AlertDialog(
            onDismissRequest = { placedDialog = false; onBack() },
            confirmButton = {
                TextButton(onClick = { placedDialog = false; onBack() }) {
                    Text(stringResource(R.string.got_it), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            title = { Text(stringResource(R.string.wager_placed), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary) },
            text = { Text(stringResource(R.string.wager_saved_message)) }
        )
    }
}

@Composable
private fun TopBar(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(BrandPrimaryDark, BrandPrimary)))
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = Color.White)
            }
            Text(
                stringResource(R.string.wager_screen_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun MatchHeader(fx: FixtureItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceCard)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "${fx.country} • ${fx.leagueName}",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontWeight = FontWeight.SemiBold
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            TeamSide(name = fx.homeName, logo = fx.homeLogo, modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                val score = when {
                    fx.homeGoals != null && fx.awayGoals != null -> "${fx.homeGoals} : ${fx.awayGoals}"
                    else -> stringResource(R.string.versus_label)
                }
                Text(score, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                val statusText = when {
                    fx.isLive -> stringResource(R.string.live_elapsed, fx.elapsed?.toString().orEmpty())
                    fx.isFinished -> stringResource(R.string.full_time)
                    else -> fx.date.substringAfter('T').substring(0, 5)
                }
                val color = if (fx.isLive) LiveGreen else TextSecondary
                Text(statusText, style = MaterialTheme.typography.labelMedium, color = color)
            }
            TeamSide(name = fx.awayName, logo = fx.awayLogo, modifier = Modifier.weight(1f), reverse = true)
        }
    }
}

@Composable
private fun TeamSide(name: String, logo: String?, modifier: Modifier = Modifier, reverse: Boolean = false) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape).background(SurfaceSoft),
            contentAlignment = Alignment.Center
        ) {
            if (!logo.isNullOrBlank()) {
                AsyncImage(model = logo, contentDescription = null, modifier = Modifier.size(44.dp))
            } else {
                Icon(Icons.Default.SportsSoccer, null, tint = TextMuted, modifier = Modifier.size(28.dp))
            }
        }
        Spacer(modifier = Modifier.size(6.dp))
        Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 2)
    }
}

@Composable
private fun OddsButton(
    label: String,
    odds: Double,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val accent = MaterialTheme.colorScheme.primary
    val bg = if (selected) accent else SurfaceCard
    val fg = if (selected) Color.White else TextPrimary
    val sub = if (selected) Color.White.copy(0.9f) else TextSecondary

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = sub, fontWeight = FontWeight.SemiBold)
        Text(
            text = "%.2f".format(odds),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = fg
        )
    }
}

@Composable
private fun StakeInput(value: String, onChange: (String) -> Unit, balance: Int) {
    val accent = MaterialTheme.colorScheme.primary
    OutlinedTextField(
        value = value,
        onValueChange = { txt -> if (txt.all { it.isDigit() } && txt.length <= 7) onChange(txt) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.wager_stake), color = TextSecondary) },
        placeholder = { Text(stringResource(R.string.balance_coins, balance), color = TextMuted) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accent,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            cursorColor = accent,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        )
    )
}

@Composable
private fun QuickStakeRow(balance: Int, onPick: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(10, 50, 100, balance / 2, balance).distinct().filter { it > 0 }.take(5).forEach { v ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BrandPrimarySoft)
                    .clickable { onPick(v) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (v == balance) stringResource(R.string.max_label) else v.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ReturnSummary(stake: Int, odds: Double) {
    val potential = (stake * odds).toInt()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.wager_potential), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            Text(
                text = stringResource(R.string.potential_coins, potential),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(stringResource(R.string.wager_profit), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            Text(
                text = "+${(potential - stake).coerceAtLeast(0)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LiveGreen
            )
        }
    }
}
