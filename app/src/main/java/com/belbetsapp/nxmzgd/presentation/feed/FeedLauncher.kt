package com.belbetsapp.nxmzgd.presentation.feed

import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.belbetsapp.nxmzgd.R

@Composable
fun FeedLauncher(
    destination: String
) {
    val context = LocalContext.current

    LaunchedEffect(destination) {
        val intent = Intent(context, FeedActivity::class.java)
        intent.putExtra(FeedActivity.EXTRA_DESTINATION, destination)
        context.startActivity(intent)

        if (context is Activity) {
            @Suppress("DEPRECATION")
            context.overridePendingTransition(
                R.anim.feed_fade_in,
                R.anim.feed_fade_out
            )
            context.finish()
            @Suppress("DEPRECATION")
            context.overridePendingTransition(
                R.anim.feed_fade_in,
                R.anim.feed_fade_out
            )
        }
    }
}
