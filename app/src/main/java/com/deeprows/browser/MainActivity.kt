package com.deeprows.browser

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var addressBar: EditText
    private lateinit var loadingBar: ProgressBar
    private lateinit var homePage: ScrollView

    private lateinit var newsList: LinearLayout
    private lateinit var gistList: LinearLayout

    private val newsRepository = NewsRepository()

    private val activityJob = Job()
    private val activityScope =
        CoroutineScope(Dispatchers.Main + activityJob)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor =
            android.graphics.Color.rgb(7, 9, 13)

        window.navigationBarColor =
            android.graphics.Color.rgb(7, 9, 13)

        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        addressBar = findViewById(R.id.addressBar)
        loadingBar = findViewById(R.id.loadingBar)
        homePage = findViewById(R.id.homePage)

        newsList = findViewById(R.id.newsList)
        gistList = findViewById(R.id.gistList)

        setupWebView()
        setupControls()

        showHomePage()

        loadHomeContent()
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

                loadingBar.visibility = View.VISIBLE

                if (!url.isNullOrEmpty()) {
                    addressBar.setText(url)
                }
            }

            override fun onPageFinished(
                view: WebView,
                url: String?
            ) {
                super.onPageFinished(view, url)

                loadingBar.visibility = View.GONE

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

                loadingBar.visibility =
                    if (newProgress >= 100) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
            }
        }
    }

    private fun setupControls() {

        val goButton: ImageButton =
            findViewById(R.id.goButton)

        val backButton: ImageButton =
            findViewById(R.id.backButton)

        val forwardButton: ImageButton =
            findViewById(R.id.forwardButton)

        val refreshButton: ImageButton =
            findViewById(R.id.refreshButton)

        val homeButton: ImageButton =
            findViewById(R.id.homeButton)

        goButton.setOnClickListener {
            loadAddress()
        }

        addressBar.setOnEditorActionListener {
                _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_GO) {
                loadAddress()
                true
            } else {
                false
            }
        }

        backButton.setOnClickListener {

            if (
                webView.visibility == View.VISIBLE &&
                webView.canGoBack()
            ) {
                webView.goBack()
            }
        }

        forwardButton.setOnClickListener {

            if (
                webView.visibility == View.VISIBLE &&
                webView.canGoForward()
            ) {
                webView.goForward()
            }
        }

        refreshButton.setOnClickListener {

            if (webView.visibility == View.VISIBLE) {
                webView.reload()
            } else {
                loadHomeContent()
            }
        }

        homeButton.setOnClickListener {
            showHomePage()
        }

        findViewById<View>(R.id.siteFacebook)
            .setOnClickListener {
                openWebsite("https://www.facebook.com")
            }

        findViewById<View>(R.id.siteInstagram)
            .setOnClickListener {
                openWebsite("https://www.instagram.com")
            }

        findViewById<View>(R.id.siteSportyBet)
            .setOnClickListener {
                openWebsite("https://www.sportybet.com")
            }

        findViewById<View>(R.id.siteBet9ja)
            .setOnClickListener {
                openWebsite("https://www.bet9ja.com")
            }

        findViewById<View>(R.id.siteDeeprowss)
            .setOnClickListener {
                openWebsite("https://deeprowss.com")
            }
    }

    private fun loadHomeContent() {

        loadLatestNews()
        loadGist()
    }

    private fun loadLatestNews() {

        newsList.removeAllViews()

        val loading = TextView(this)

        loading.text = "Loading latest news..."
        loading.setTextColor(
            android.graphics.Color.rgb(141, 150, 165)
        )
        loading.textSize = 14f

        newsList.addView(loading)

        activityScope.launch {

            val articles =
                newsRepository.getLatestNews()

            newsList.removeAllViews()

            if (articles.isEmpty()) {

                showEmptyMessage(
                    newsList,
                    "Unable to load latest news. Tap refresh to try again."
                )

                return@launch
            }

            articles.forEach { article ->

                newsList.addView(
                    createNewsItem(article)
                )
            }
        }
    }

    private fun loadGist() {

        gistList.removeAllViews()

        val loading = TextView(this)

        loading.text = "Loading latest gist..."
        loading.setTextColor(
            android.graphics.Color.rgb(141, 150, 165)
        )
        loading.textSize = 14f

        gistList.addView(loading)

        activityScope.launch {

            val articles =
                newsRepository.getGist()

            gistList.removeAllViews()

            if (articles.isEmpty()) {

                showEmptyMessage(
                    gistList,
                    "Unable to load latest gist. Tap refresh to try again."
                )

                return@launch
            }

            articles.forEach { article ->

                gistList.addView(
                    createNewsItem(article)
                )
            }
        }
    }

    private fun createNewsItem(
        article: NewsArticle
    ): TextView {

        val item = TextView(this)

        item.text =
            if (article.source.isNotBlank()) {
                "${article.title}\n\n${article.source}"
            } else {
                article.title
            }

        item.setTextColor(
            android.graphics.Color.WHITE
        )

        item.textSize = 15f

        item.setPadding(
            16,
            16,
            16,
            16
        )

        item.setBackgroundColor(
            android.graphics.Color.rgb(
                16,
                20,
                27
            )
        )

        val params =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        params.setMargins(
            0,
            0,
            0,
            8
        )

        item.layoutParams = params

        item.setOnClickListener {
            openWebsite(article.link)
        }

        return item
    }

    private fun showEmptyMessage(
        container: LinearLayout,
        message: String
    ) {

        val textView = TextView(this)

        textView.text = message

        textView.setTextColor(
            android.graphics.Color.rgb(
                141,
                150,
                165
            )
        )

        textView.textSize = 14f

        textView.setPadding(
            0,
            8,
            0,
            8
        )

        container.addView(textView)
    }

    private fun openWebsite(url: String) {

        homePage.visibility = View.GONE

        webView.visibility = View.VISIBLE

        webView.loadUrl(url)
    }

    private fun showHomePage() {

        homePage.visibility = View.VISIBLE

        webView.visibility = View.GONE

        loadingBar.visibility = View.GONE

        addressBar.setText("")

        addressBar.hint =
            "Search or enter website"
    }

    private fun loadAddress() {

        val input =
            addressBar.text.toString().trim()

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

            "https://www.google.com/search?q=" +
                    input.replace(" ", "+")
        }

        openWebsite(url)
    }

    override fun onBackPressed() {

        if (webView.visibility == View.VISIBLE) {

            if (webView.canGoBack()) {

                webView.goBack()

            } else {

                showHomePage()
            }

        } else {

            super.onBackPressed()
        }
    }

    override fun onDestroy() {

        activityJob.cancel()

        super.onDestroy()
    }
}
