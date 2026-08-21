package com.belbetsapp.nxmzgd.presentation.policy

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.belbetsapp.nxmzgd.R

class PolicyPaneActivity : ComponentActivity() {

    private lateinit var stage: WebView
    private lateinit var loadIndicator: ProgressBar
    private var firstLoad = true
    private var fileCallback: ValueCallback<Array<Uri>>? = null
    private lateinit var pickerLauncher: ActivityResultLauncher<Intent>

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val destination = intent.getStringExtra(EXTRA_ADDRESS).orEmpty()
        if (destination.isBlank()) {
            finish()
            return
        }

        pickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val cb = fileCallback ?: return@registerForActivityResult
            val data = result.data
            val results: Array<Uri>? = when {
                result.resultCode != RESULT_OK || data == null -> null
                data.clipData != null -> Array(data.clipData!!.itemCount) { i -> data.clipData!!.getItemAt(i).uri }
                data.dataString != null -> arrayOf(Uri.parse(data.dataString))
                else -> null
            }
            cb.onReceiveValue(results)
            fileCallback = null
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(this@PolicyPaneActivity, R.color.brand_teal))
            fitsSystemWindows = true
        }

        val topStrip = FrameLayout(this).apply {
            setBackgroundColor(ContextCompat.getColor(this@PolicyPaneActivity, R.color.brand_teal))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                resources.displayMetrics.density.let { (it * 56).toInt() }
            )
        }

        val backBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_revert)
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(Color.WHITE)
            layoutParams = FrameLayout.LayoutParams(
                resources.displayMetrics.density.let { (it * 48).toInt() },
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.START or Gravity.CENTER_VERTICAL
            )
            setOnClickListener { finish() }
        }
        topStrip.addView(backBtn)

        val content = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            setBackgroundColor(Color.BLACK)
        }

        stage = WebView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.WHITE)
            tuneSettings(this)
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, link: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, link, favicon)
                    if (firstLoad) loadIndicator.visibility = View.VISIBLE
                }
                override fun onPageFinished(view: WebView?, link: String?) {
                    super.onPageFinished(view, link)
                    if (firstLoad) {
                        loadIndicator.visibility = View.GONE
                        firstLoad = false
                    }
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onShowFileChooser(
                    view: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    fileCallback?.onReceiveValue(null)
                    fileCallback = filePathCallback
                    val intent = fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "*/*"
                    }
                    return try {
                        pickerLauncher.launch(intent)
                        true
                    } catch (_: Exception) {
                        fileCallback = null
                        false
                    }
                }
            }
            CookieManager.getInstance().also { cm ->
                cm.setAcceptCookie(true)
                cm.setAcceptThirdPartyCookies(this, true)
            }
            setDownloadListener { address, _, _, _, _ ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(address))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } catch (_: Exception) {
                }
            }
            loadUrl(destination)
        }

        loadIndicator = ProgressBar(this).apply {
            isIndeterminate = true
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
            indeterminateTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
        }

        content.addView(stage)
        content.addView(loadIndicator)

        root.addView(topStrip)
        root.addView(content)
        setContentView(root)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun tuneSettings(view: WebView) {
        val s: WebSettings = view.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.databaseEnabled = true
        s.loadWithOverviewMode = true
        s.useWideViewPort = true
        s.setSupportZoom(true)
        s.builtInZoomControls = true
        s.displayZoomControls = false
        s.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        s.cacheMode = WebSettings.LOAD_NO_CACHE
        s.userAgentString = s.userAgentString.replace("; wv", "").replace("Version/4.0", "")
    }

    override fun onPause() {
        CookieManager.getInstance().flush()
        super.onPause()
    }

    override fun onDestroy() {
        CookieManager.getInstance().flush()
        stage.stopLoading()
        stage.destroy()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_ADDRESS = "extra_address"
        const val POLICY_LINK = "https://sghfdva.top/api-belbets/privacy-policy/"
    }
}
