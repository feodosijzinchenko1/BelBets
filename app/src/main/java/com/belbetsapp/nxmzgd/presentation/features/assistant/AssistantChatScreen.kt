package com.belbetsapp.nxmzgd.presentation.features.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.belbetsapp.nxmzgd.R
import com.belbetsapp.nxmzgd.data.api.AiClient
import com.belbetsapp.nxmzgd.presentation.theme.SurfaceCard
import com.belbetsapp.nxmzgd.presentation.theme.SurfaceSoft
import com.belbetsapp.nxmzgd.presentation.theme.TextPrimary
import com.belbetsapp.nxmzgd.presentation.theme.TextSecondary
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isUser: Boolean)

@Composable
fun AssistantChatScreen(topicId: String, onBack: () -> Unit) {
    val topics = assistantTopics()
    val topic = remember(topicId, topics) { topics.find { it.id == topicId } ?: topics.first() }
    val ai = remember { AiClient() }
    val scope = rememberCoroutineScope()
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var sending by remember { mutableStateOf(false) }
    var started by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(SurfaceSoft)) {
        TopBar(title = topic.title, onBack = onBack)
        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            if (!started) {
                StartBox(topic = topic, loading = sending) {
                    started = true
                    sending = true
                    scope.launch {
                        val reply = ai.ask(topic.starter, topic.system)
                        messages.add(ChatMessage(reply, isUser = false))
                        sending = false
                    }
                }
            } else {
                ChatBody(messages = messages, loading = sending)
            }
        }
        InputBar(
            value = input,
            onValueChange = { input = it },
            enabled = started && !sending,
            onSend = {
                val q = input.trim()
                if (q.isEmpty() || sending) return@InputBar
                messages.add(ChatMessage(q, isUser = true))
                input = ""
                sending = true
                scope.launch {
                    val reply = ai.ask(q, topic.system)
                    messages.add(ChatMessage(reply, isUser = false))
                    sending = false
                }
            }
        )
    }
}

@Composable
private fun TopBar(title: String, onBack: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(accent)
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
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun StartBox(topic: AssistantTopic, loading: Boolean, onStart: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(28.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.size(82.dp).clip(CircleShape).background(topic.accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(topic.icon, null, tint = topic.accent, modifier = Modifier.size(42.dp))
            }
            Text(topic.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            Text(topic.subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.padding(horizontal = 8.dp))
            Spacer(modifier = Modifier.size(4.dp))
            Button(
                onClick = onStart,
                enabled = !loading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (loading) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                }
                Text(stringResource(R.string.start_conversation), fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

@Composable
private fun ChatBody(messages: List<ChatMessage>, loading: Boolean) {
    val state = rememberLazyListState()
    LaunchedEffect(messages.size, loading) {
        if (messages.isNotEmpty()) state.animateScrollToItem(messages.size - 1)
    }
    LazyColumn(
        state = state,
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(messages) { msg -> Bubble(msg) }
        if (loading) {
            item { LoadingRow() }
        }
    }
}

@Composable
private fun Bubble(message: ChatMessage) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val maxWidth = screenWidth * 0.82f
    val accent = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!message.isUser) {
            Box(
                modifier = Modifier.size(30.dp).clip(CircleShape).background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SmartToy, null, tint = accent, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Box(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .clip(
                    RoundedCornerShape(
                        topStart = if (message.isUser) 16.dp else 4.dp,
                        topEnd = if (message.isUser) 4.dp else 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )
                )
                .background(if (message.isUser) accent else SurfaceCard)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (message.isUser) Color.White else TextPrimary
            )
        }
        if (message.isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier.size(30.dp).clip(CircleShape).background(accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun LoadingRow() {
    val accent = MaterialTheme.colorScheme.primary
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(30.dp).clip(CircleShape).background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.SmartToy, null, tint = accent, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = accent, strokeWidth = 2.dp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(R.string.thinking), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun InputBar(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    onSend: () -> Unit
) {
    val accent = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(R.string.ask_coach), color = TextSecondary) },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accent,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = SurfaceSoft,
                unfocusedContainerColor = SurfaceSoft,
                cursorColor = accent,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onSend,
            enabled = value.isNotBlank() && enabled,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (value.isNotBlank() && enabled) accent else accent.copy(alpha = 0.35f))
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, stringResource(R.string.send), tint = Color.White)
        }
    }
}
