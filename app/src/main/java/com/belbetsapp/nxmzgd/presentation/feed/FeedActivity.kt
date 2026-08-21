package com.belbetsapp.nxmzgd.presentation.feed

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.CookieManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import com.belbetsapp.nxmzgd.R

class FeedActivity : ComponentActivity() {

    companion object {
        const val EXTRA_DESTINATION = "feed_target_destination"
        private const val PICKER_CODE = 7842
    }

    private lateinit var feedSurface: WebView
    private lateinit var bootPulse: ProgressBar
    private var pickRelay: ValueCallback<Array<Uri>>? = null
    private var firstPaintOnly = true

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        val targetDestination = intent.getStringExtra(EXTRA_DESTINATION) ?: run {
            finish()
            return
        }

        buildShell()
        tuneFeedSurface()
        feedSurface.loadUrl(targetDestination)
        wireBackStack()
    }

    private fun buildShell() {
        val surfaceColor = ContextCompat.getColor(this, R.color.browser_surface_background)
        val loaderColor = ContextCompat.getColor(this, R.color.browser_surface_loader)

        val rootPane = RelativeLayout(this).apply {
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(surfaceColor)
        }

        feedSurface = WebView(this).apply {
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
            id = View.generateViewId()
            setBackgroundColor(surfaceColor)
        }

        bootPulse = ProgressBar(this).apply {
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.CENTER_IN_PARENT)
            }
            indeterminateTintList = ColorStateList.valueOf(loaderColor)
            visibility = View.VISIBLE
        }

        rootPane.addView(feedSurface)
        rootPane.addView(bootPulse)
        setContentView(rootPane)

        applyImmersive()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun tuneFeedSurface() {
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(feedSurface, true)
        }

        feedSurface.apply {
            isFocusable = true
            isFocusableInTouchMode = true
        }

        feedSurface.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            loadWithOverviewMode = false
            useWideViewPort = true
            builtInZoomControls = false
            displayZoomControls = false
            setSupportZoom(false)
            allowFileAccess = true
            allowContentAccess = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            javaScriptCanOpenWindowsAutomatically = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mediaPlaybackRequiresUserGesture = false
            loadsImagesAutomatically = true
            blockNetworkImage = false
            setSupportMultipleWindows(false)
            safeBrowsingEnabled = false
            userAgentString = userAgentString.replace("; wv", "").replace("Version/4.0 ", "")
        }

        feedSurface.setDownloadListener { link, userAgent, contentDisposition, mimeType, _ ->
            pullRemoteFile(link, userAgent, contentDisposition, mimeType)
        }

        feedSurface.webViewClient = makeClient()
        feedSurface.webChromeClient = makeChrome()
    }

    private fun makeClient(): WebViewClient {
        return object : WebViewClient() {
            override fun onPageStarted(view: WebView?, link: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, link, favicon)
                if (firstPaintOnly) {
                    bootPulse.visibility = View.VISIBLE
                }
            }

            override fun onPageFinished(view: WebView?, link: String?) {
                super.onPageFinished(view, link)
                if (firstPaintOnly) {
                    firstPaintOnly = false
                    bootPulse.visibility = View.GONE
                }
                CookieManager.getInstance().flush()
            }

            override fun doUpdateVisitedHistory(view: WebView?, link: String?, isReload: Boolean) {
                CookieManager.getInstance().flush()
                super.doUpdateVisitedHistory(view, link, isReload)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val uri = request?.url ?: return false
                val scheme = uri.scheme ?: return false

                if (scheme in listOf("http", "https")) {
                    return false
                }

                return try {
                    val outbound = if (scheme == "intent") {
                        Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME)
                    } else {
                        Intent(Intent.ACTION_VIEW, uri)
                    }

                    openOutside(view?.context ?: return true, outbound)
                    true
                } catch (_: Exception) {
                    true
                }
            }

            override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
                if (!isFinishing && !isDestroyed) {
                    recreate()
                }
                return true
            }
        }
    }

    private fun makeChrome(): WebChromeClient {
        return object : WebChromeClient() {
            override fun onShowFileChooser(
                view: WebView?,
                callback: ValueCallback<Array<Uri>>?,
                params: FileChooserParams?
            ): Boolean {
                pickRelay?.onReceiveValue(null)
                pickRelay = callback

                val acceptTypes = params?.acceptTypes ?: arrayOf("*/*")
                val mimeType = acceptTypes.firstOrNull()?.takeIf { it.isNotEmpty() } ?: "*/*"

                val picker = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = mimeType
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }

                return try {
                    @Suppress("DEPRECATION")
                    startActivityForResult(picker, PICKER_CODE)
                    true
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(this@FeedActivity, getString(R.string.file_manager_missing), Toast.LENGTH_SHORT).show()
                    callback?.onReceiveValue(null)
                    pickRelay = null
                    false
                }
            }
        }
    }

    private fun wireBackStack() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (feedSurface.canGoBack()) {
                    feedSurface.goBack()
                }
            }
        })
    }

    private fun pullRemoteFile(
        downloadLink: String,
        userAgent: String,
        contentDisposition: String,
        mimeType: String
    ) {
        try {
            val request = DownloadManager.Request(Uri.parse(downloadLink))
            request.setMimeType(mimeType)

            val cookies = CookieManager.getInstance().getCookie(downloadLink)
            if (!cookies.isNullOrEmpty()) {
                request.addRequestHeader("Cookie", cookies)
            }
            request.addRequestHeader("User-Agent", userAgent)

            val fileName = URLUtil.guessFileName(downloadLink, contentDisposition, mimeType)
            request.setTitle(fileName)
            request.setDescription(getString(R.string.download_description))
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            Toast.makeText(this, getString(R.string.download_started, fileName), Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(this, getString(R.string.download_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openOutside(context: Context, intent: Intent): Boolean {
        return try {
            if (context !is ComponentActivity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    @Suppress("DEPRECATION")
    private fun applyImmersive() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICKER_CODE) {
            settlePickerResult(resultCode, data)
        }
    }

    private fun settlePickerResult(resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            val uris = mutableListOf<Uri>()

            data?.data?.let { uri -> uris.add(uri) }

            data?.clipData?.let { clipData ->
                for (i in 0 until clipData.itemCount) {
                    clipData.getItemAt(i).uri?.let { uri -> uris.add(uri) }
                }
            }

            pickRelay?.onReceiveValue(uris.toTypedArray())
        } else {
            pickRelay?.onReceiveValue(null)
        }

        pickRelay = null
    }

    override fun onPause() {
        super.onPause()
        CookieManager.getInstance().flush()
    }
}
