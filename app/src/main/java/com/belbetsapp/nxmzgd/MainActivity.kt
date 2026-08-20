package com.belbetsapp.nxmzgd

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.belbetsapp.nxmzgd.data.store.AppStorage
import com.belbetsapp.nxmzgd.presentation.bootstrap.BootstrapPane
import com.belbetsapp.nxmzgd.presentation.features.assistant.AssistantChatScreen
import com.belbetsapp.nxmzgd.presentation.features.playground.PlaceWagerScreen
import com.belbetsapp.nxmzgd.presentation.features.rankings.LeagueTableScreen
import com.belbetsapp.nxmzgd.presentation.feed.FeedLauncher
import com.belbetsapp.nxmzgd.presentation.hub.HubTabs
import com.belbetsapp.nxmzgd.presentation.navigation.RootRoute
import com.belbetsapp.nxmzgd.presentation.policy.PolicyPaneActivity
import com.belbetsapp.nxmzgd.presentation.theme.BelBetsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val storage = AppStorage(applicationContext)

        setContent {
            BelBetsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RootNavGraph(storage)
                }
            }
        }
    }
}

@Composable
private fun RootNavGraph(storage: AppStorage) {
    val navController = rememberNavController()
    val ctx = LocalContext.current
    var feedDestination by remember { mutableStateOf<String?>(null) }

    if (feedDestination != null) {
        FeedLauncher(destination = feedDestination!!)
        return
    }

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            BootstrapPane(
                storage = storage,
                onOpenFeed = { destination -> feedDestination = destination },
                onOpenHub = {
                    navController.navigate(RootRoute.Hub.path) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable(RootRoute.Hub.path) {
            BackHandler {}
            HubTabs(
                storage = storage,
                rootNav = navController,
                onOpenPolicy = {
                    val intent = Intent(ctx, PolicyPaneActivity::class.java).apply {
                        putExtra(PolicyPaneActivity.EXTRA_ADDRESS, PolicyPaneActivity.POLICY_LINK)
                    }
                    ctx.startActivity(intent)
                }
            )
        }

        composable(
            route = RootRoute.AssistantChat.path,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType })
        ) { backStack ->
            val id = backStack.arguments?.getString("topicId").orEmpty()
            AssistantChatScreen(topicId = id, onBack = { navController.popBackStack() })
        }

        composable(
            route = RootRoute.LeagueTable.path,
            arguments = listOf(
                navArgument("leagueId") { type = NavType.IntType },
                navArgument("season") { type = NavType.IntType },
                navArgument("name") { type = NavType.StringType }
            )
        ) { backStack ->
            val args = backStack.arguments!!
            LeagueTableScreen(
                leagueId = args.getInt("leagueId"),
                season = args.getInt("season"),
                leagueName = args.getString("name").orEmpty(),
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = RootRoute.PlaceWager.path,
            arguments = listOf(navArgument("fixtureId") { type = NavType.LongType })
        ) { backStack ->
            PlaceWagerScreen(
                fixtureId = backStack.arguments?.getLong("fixtureId") ?: 0L,
                storage = storage,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
