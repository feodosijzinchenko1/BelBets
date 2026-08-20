package com.belbetsapp.nxmzgd.presentation.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.belbetsapp.nxmzgd.data.store.AppStorage
import com.belbetsapp.nxmzgd.presentation.features.account.AccountScreen
import com.belbetsapp.nxmzgd.presentation.features.assistant.AssistantListScreen
import com.belbetsapp.nxmzgd.presentation.features.matches.MatchesScreen
import com.belbetsapp.nxmzgd.presentation.features.playground.PlaygroundScreen
import com.belbetsapp.nxmzgd.presentation.features.rankings.RankingsScreen
import com.belbetsapp.nxmzgd.presentation.navigation.HubTab
import com.belbetsapp.nxmzgd.presentation.theme.BrandPrimary
import com.belbetsapp.nxmzgd.presentation.theme.BrandPrimarySoft
import com.belbetsapp.nxmzgd.presentation.theme.SurfaceSoft
import com.belbetsapp.nxmzgd.presentation.theme.TextMuted

@Composable
fun HubTabs(
    storage: AppStorage,
    rootNav: NavHostController,
    onOpenPolicy: () -> Unit
) {
    var current by rememberSaveable { mutableStateOf(HubTab.Matches) }

    Scaffold(
        containerColor = SurfaceSoft,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(26.dp))
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color.White)
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HubTab.entries.forEach { tab ->
                        val selected = tab == current
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { current = tab }
                                .background(if (selected) BrandPrimarySoft else Color.Transparent)
                                .padding(vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (selected) BrandPrimary else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (selected) tab.selected else tab.unselected,
                                    contentDescription = stringResource(tab.labelRes),
                                    tint = if (selected) Color.White else BrandPrimary.copy(alpha = 0.45f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Text(
                                text = stringResource(tab.labelRes),
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) BrandPrimary else TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = inner.calculateBottomPadding())
                .background(SurfaceSoft)
        ) {
            when (current) {
                HubTab.Matches -> MatchesScreen(rootNav = rootNav)
                HubTab.Assistant -> AssistantListScreen(rootNav = rootNav)
                HubTab.Playground -> PlaygroundScreen(storage = storage, rootNav = rootNav)
                HubTab.Rankings -> RankingsScreen(rootNav = rootNav)
                HubTab.Account -> AccountScreen(storage = storage, onOpenPolicy = onOpenPolicy)
            }
        }
    }
}
