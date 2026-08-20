package com.belbetsapp.nxmzgd.presentation.features.rankings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.belbetsapp.nxmzgd.R
import com.belbetsapp.nxmzgd.data.api.StandingRow
import com.belbetsapp.nxmzgd.data.repo.FootballRepository
import com.belbetsapp.nxmzgd.presentation.theme.BrandPrimary
import com.belbetsapp.nxmzgd.presentation.theme.BrandPrimaryDark
import com.belbetsapp.nxmzgd.presentation.theme.BrandPrimarySoft
import com.belbetsapp.nxmzgd.presentation.theme.DividerGray
import com.belbetsapp.nxmzgd.presentation.theme.SurfaceCard
import com.belbetsapp.nxmzgd.presentation.theme.SurfaceSoft
import com.belbetsapp.nxmzgd.presentation.theme.TextMuted
import com.belbetsapp.nxmzgd.presentation.theme.TextPrimary
import com.belbetsapp.nxmzgd.presentation.theme.TextSecondary

@Composable
fun LeagueTableScreen(
    leagueId: Int,
    season: Int,
    leagueName: String,
    onBack: () -> Unit
) {
    var loading by remember { mutableStateOf(true) }
    var rows by remember { mutableStateOf<List<StandingRow>>(emptyList()) }

    LaunchedEffect(leagueId, season) {
        loading = true
        rows = FootballRepository.getStandings(leagueId, season)
        loading = false
    }

    Column(modifier = Modifier.fillMaxSize().background(SurfaceSoft)) {
        TopBackHeader(
            title = leagueName.ifBlank { stringResource(R.string.standings) },
            subtitle = stringResource(R.string.season_label, season, season + 1),
            onBack = onBack
        )

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Column
        }

        if (rows.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.standings_unavailable),
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item { HeaderRow() }
            items(rows) { row -> StandingRowItem(row) }
        }
    }
}

@Composable
private fun TopBackHeader(title: String, subtitle: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(BrandPrimaryDark, BrandPrimary)))
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = Color.White)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            Icon(Icons.Default.EmojiEvents, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.padding(end = 12.dp))
        }
    }
}

@Composable
private fun HeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("#", modifier = Modifier.width(28.dp), style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(R.string.table_team), modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold)
        StatCell(stringResource(R.string.table_played), width = 28)
        StatCell(stringResource(R.string.table_won), width = 28)
        StatCell(stringResource(R.string.table_draw), width = 28)
        StatCell(stringResource(R.string.table_lost), width = 28)
        StatCell(stringResource(R.string.table_points), width = 38)
    }
}

@Composable
private fun StatCell(text: String, width: Int) {
    Box(
        modifier = Modifier.width(width.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StandingRowItem(row: StandingRow) {
    val accent = MaterialTheme.colorScheme.primary
    val rankBg = when {
        row.rank <= 4 -> accent.copy(alpha = 0.12f)
        row.rank <= 6 -> BrandPrimarySoft.copy(alpha = 0.7f)
        else -> SurfaceSoft
    }
    val rankColor = if (row.rank <= 4) accent else TextSecondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(rankBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "${row.rank}",
                color = rankColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier.size(26.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!row.teamLogo.isNullOrBlank()) {
                AsyncImage(model = row.teamLogo, contentDescription = null, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            row.teamName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            maxLines = 1
        )
        ValueCell(row.played.toString(), 28)
        ValueCell(row.win.toString(), 28)
        ValueCell(row.draw.toString(), 28)
        ValueCell(row.lose.toString(), 28)
        Box(
            modifier = Modifier.width(38.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "${row.points}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = accent
            )
        }
    }
}

@Composable
private fun ValueCell(text: String, width: Int) {
    Box(
        modifier = Modifier.width(width.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}
