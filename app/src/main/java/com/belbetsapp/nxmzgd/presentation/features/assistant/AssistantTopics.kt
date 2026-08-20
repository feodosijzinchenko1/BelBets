package com.belbetsapp.nxmzgd.presentation.features.assistant

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Shield
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.belbetsapp.nxmzgd.R

data class AssistantTopic(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color,
    val starter: String,
    val system: String
)

@Composable
fun assistantTopics(): List<AssistantTopic> = listOf(
    AssistantTopic(
        id = "match_analyst",
        title = stringResource(R.string.topic_match_analyst_title),
        subtitle = stringResource(R.string.topic_match_analyst_subtitle),
        icon = Icons.Default.Analytics,
        accent = Color(0xFF0D9488),
        starter = "Give me a short tactical preview of the most interesting soccer match happening today, including key players to watch.",
        system = "You are a tactical soccer analyst. Give concise, structured tactical insights for soccer matches. Use clear bullet points when useful. Keep answers under 7 sentences. Answer in English only."
    ),
    AssistantTopic(
        id = "team_strategy",
        title = stringResource(R.string.topic_team_strategy_title),
        subtitle = stringResource(R.string.topic_team_strategy_subtitle),
        icon = Icons.Default.Insights,
        accent = Color(0xFFC2185B),
        starter = "Compare the strengths and weaknesses of a 4-3-3 versus a 3-5-2 in modern soccer. Be specific and practical.",
        system = "You are a senior soccer tactics coach. Explain formations, pressing schemes, set-piece routines and game-management with practical detail. Stay concise, max 7 sentences. Answer in English only."
    ),
    AssistantTopic(
        id = "betting_edu",
        title = stringResource(R.string.topic_prediction_title),
        subtitle = stringResource(R.string.topic_prediction_subtitle),
        icon = Icons.Default.Shield,
        accent = Color(0xFF0F766E),
        starter = "Explain how to read pre-match statistics (xG, form, head-to-head) to build a balanced prediction. Educational only.",
        system = "You are an educator on sports analytics and soccer prediction methodology. You discuss statistics, value, probability and bankroll discipline as educational topics only. Never instruct the user to place real bets. Keep answers under 7 sentences. Answer in English only."
    ),
    AssistantTopic(
        id = "history",
        title = stringResource(R.string.topic_history_title),
        subtitle = stringResource(R.string.topic_history_subtitle),
        icon = Icons.Default.History,
        accent = Color(0xFF5E35B1),
        starter = "Tell me about a legendary soccer match that changed how the game was played.",
        system = "You are a soccer historian. Share captivating stories about clubs, players, World Cups and tactical revolutions. Keep answers concise, max 7 sentences. Answer in English only."
    ),
    AssistantTopic(
        id = "learn",
        title = stringResource(R.string.topic_academy_title),
        subtitle = stringResource(R.string.topic_academy_subtitle),
        icon = Icons.AutoMirrored.Filled.MenuBook,
        accent = Color(0xFF1976D2),
        starter = "Explain VAR step by step for a new fan: what it checks, when it intervenes and what it cannot do.",
        system = "You are a friendly soccer tutor for fans of all levels. Explain rules, terminology, leagues and competitions clearly. Avoid jargon when possible. Keep answers under 7 sentences. Answer in English only."
    )
)
