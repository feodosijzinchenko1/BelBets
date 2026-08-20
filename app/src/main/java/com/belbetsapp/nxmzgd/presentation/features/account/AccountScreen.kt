package com.belbetsapp.nxmzgd.presentation.features.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.belbetsapp.nxmzgd.R
import com.belbetsapp.nxmzgd.data.store.AppStorage
import com.belbetsapp.nxmzgd.presentation.components.ScreenHeader
import com.belbetsapp.nxmzgd.presentation.theme.SurfaceCard
import com.belbetsapp.nxmzgd.presentation.theme.SurfaceSoft
import com.belbetsapp.nxmzgd.presentation.theme.TextPrimary
import com.belbetsapp.nxmzgd.presentation.theme.TextSecondary

@Composable
fun AccountScreen(storage: AppStorage, onOpenPolicy: () -> Unit = {}) {
    val username by storage.usernameFlow.collectAsState(initial = stringResource(R.string.default_username))
    val accent = MaterialTheme.colorScheme.primary
    var nameDraft by remember(username) { mutableStateOf(username) }

    Column(modifier = Modifier.fillMaxSize().background(SurfaceSoft)) {
        ScreenHeader(
            title = stringResource(R.string.account_title),
            subtitle = stringResource(R.string.account_subtitle),
            icon = Icons.Default.Person
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AccountCard(username = username, accent = accent)

            SectionCard(icon = Icons.Default.Person, title = stringResource(R.string.display_name), accent = accent) {
                OutlinedTextField(
                    value = nameDraft,
                    onValueChange = { if (it.length <= 24) nameDraft = it },
                    label = { Text(stringResource(R.string.your_name), color = TextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
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
                LaunchedEffect(nameDraft) {
                    if (nameDraft.isNotBlank() && nameDraft != username) {
                        storage.saveUsername(nameDraft)
                    }
                }
            }

            PolicyRow(accent = accent, onClick = onOpenPolicy)
        }
    }
}

@Composable
private fun PolicyRow(accent: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PrivacyTip, null, tint = accent, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(R.string.privacy_policy),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                stringResource(R.string.privacy_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Text(stringResource(R.string.open_action), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = accent)
    }
}

@Composable
private fun AccountCard(username: String, accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, null, tint = accent, modifier = Modifier.size(34.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(username, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            Text(stringResource(R.string.local_profile_hint), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
private fun SectionCard(
    icon: ImageVector,
    title: String,
    accent: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accent)
        }
        content()
    }
}
