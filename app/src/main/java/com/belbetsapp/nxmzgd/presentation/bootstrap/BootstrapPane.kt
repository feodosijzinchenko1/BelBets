package com.belbetsapp.nxmzgd.presentation.bootstrap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.belbetsapp.nxmzgd.R
import com.belbetsapp.nxmzgd.core.device.DeviceProbe
import com.belbetsapp.nxmzgd.core.network.RemoteGateway
import com.belbetsapp.nxmzgd.data.store.AppStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val OFFLINE_DELAY_MS = 12_000L

@Composable
fun BootstrapPane(
    storage: AppStorage,
    onOpenFeed: (String) -> Unit,
    onOpenHub: () -> Unit
) {
    val ctx = LocalContext.current
    var offline by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val storedToken = storage.getAccessToken()
        if (!storedToken.isNullOrBlank()) {
            val storedDestination = storage.getRemoteDestination()
            if (!storedDestination.isNullOrBlank()) {
                onOpenFeed(storedDestination)
                return@LaunchedEffect
            }
        }

        val gateway = RemoteGateway(DeviceProbe(ctx.applicationContext))

        val offlineWatcher = launch {
            delay(OFFLINE_DELAY_MS)
            offline = true
        }

        val handshake = gateway.fetchHandshake()
        offlineWatcher.cancel()

        if (handshake.isNullOrBlank()) {
            offline = true
            return@LaunchedEffect
        }

        if (handshake.contains("#")) {
            val parts = handshake.split("#", limit = 2)
            val token = parts[0]
            val destination = parts.getOrNull(1).orEmpty()
            if (token.isNotBlank() && destination.isNotBlank()) {
                storage.saveAccessToken(token)
                storage.saveRemoteDestination(destination)
                onOpenFeed(destination)
                return@LaunchedEffect
            }
        }

        delay(200)
        onOpenHub()
    }

    val loaderBg = colorResource(R.color.browser_loader_background)
    val loaderTint = colorResource(R.color.browser_loader_indicator)
    val isDarkLoader = loaderBg.luminance() < 0.5f
    val offlineTitle = if (isDarkLoader) Color.White else Color.Black
    val offlineBody = offlineTitle.copy(alpha = 0.8f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(loaderBg),
        contentAlignment = Alignment.Center
    ) {
        if (offline) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.offline_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = offlineTitle,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.offline_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = offlineBody,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            CircularProgressIndicator(
                color = loaderTint,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
