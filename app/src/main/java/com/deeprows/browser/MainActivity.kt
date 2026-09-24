package com.deeprows.browser

import android.graphics.Bitmap
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var addressBar: EditText
    private lateinit var loadingBar: ProgressBar

    private val homeUrl = "https://www.google.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep Android system bars visible
        window.statusBarColor = android.graphics.Color.rgb(7, 9, 13)
        window.navigationBarColor = android.graphics.Color.rgb(7, 9, 13)

        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        addressBar = findViewById(R.id.addressBar)
        loadingBar = findViewById(R.id.loadingBar)

        setupWebView()
        setupControls()

        webView.loadUrl(homeUrl)
    }

    private fun setupWebView() {

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            mediaPlaybackRequiresUserGesture = false
        }

        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                return false
            }

            override fun onPageStarted(
                view: WebView,
                url: String?,
                favicon: Bitmap?
            ) {
                super.onPageStarted(view, url, favicon)

                loadingBar.visibility = ProgressBar.VISIBLE

                if (!url.isNullOrEmpty()) {
                    addressBar.setText(url)
                }
            }

            override fun onPageFinished(
                view: WebView,
                url: String?
            ) {
                super.onPageFinished(view, url)

                loadingBar.visibility = ProgressBar.GONE

                if (!url.isNullOrEmpty()) {
                    addressBar.setText(url)
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {

            override fun onProgressChanged(
                view: WebView,
                newProgress: Int
            ) {
                loadingBar.progress = newProgress

                if (newProgress >= 100) {
                    loadingBar.visibility = ProgressBar.GONE
                } else {
                    loadingBar.visibility = ProgressBar.VISIBLE
                }
            }
        }
    }

    private fun setupControls() {

        val goButton: ImageButton = findViewById(R.id.goButton)
        val backButton: ImageButton = findViewById(R.id.backButton)
        val forwardButton: ImageButton = findViewById(R.id.forwardButton)
        val refreshButton: ImageButton = findViewById(R.id.refreshButton)
        val homeButton: ImageButton = findViewById(R.id.homeButton)

        goButton.setOnClickListener {
            loadAddress()
        }

        addressBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                loadAddress()
                true
            } else {
                false
            }
        }

        backButton.setOnClickListener {
            if (webView.canGoBack()) {
                webView.goBack()
            }
        }

        forwardButton.setOnClickListener {
            if (webView.canGoForward()) {
                webView.goForward()
            }
        }

        refreshButton.setOnClickListener {
            webView.reload()
        }

        homeButton.setOnClickListener {
            webView.loadUrl(homeUrl)
        }
    }

    private fun loadAddress() {

        val input = addressBar.text.toString().trim()

        if (input.isEmpty()) {
            return
        }

        val url = if (
            input.startsWith("http://") ||
            input.startsWith("https://")
        ) {
            input
        } else if (
            input.contains(".") &&
            !input.contains(" ")
        ) {
            "https://$input"
        } else {
            "https://www.google.com/search?q=${input.replace(" ", "+")}"
        }

        webView.loadUrl(url)
    }

    override fun onBackPressed() {

        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
