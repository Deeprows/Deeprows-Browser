package com.deeprows.browser

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var addressBar: EditText
    private lateinit var loadingBar: ProgressBar
    private lateinit var homePage: ScrollView
    private lateinit var newsList: LinearLayout
    private lateinit var sportNewsList: LinearLayout

    private val newsRepository =
        NewsRepository()

    private val activityJob =
        Job()

    private val activityScope =
        CoroutineScope(
            Dispatchers.Main + activityJob
        )

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        window.statusBarColor =
            Color.rgb(11, 18, 32)

        window.navigationBarColor =
            Color.rgb(11, 18, 32)

        setContentView(
            R.layout.activity_main
        )

        webView =
            findViewById(R.id.webView)

        addressBar =
            findViewById(R.id.addressBar)

        loadingBar =
            findViewById(R.id.loadingBar)

        homePage =
            findViewById(R.id.homePage)

        newsList =
            findViewById(R.id.newsList)

        sportNewsList =
            findViewById(R.id.sportNewsList)

        setupWebView()
        setupControls()

        showHomePage()

        loadLatestNews()
        loadSportNews()
    }

    private fun setupWebView() {

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            mediaPlaybackRequiresUserGesture = false
        }

        webView.webViewClient =
            object : WebViewClient() {

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

                    super.onPageStarted(
                        view,
                        url,
                        favicon
                    )

                    loadingBar.visibility =
                        View.VISIBLE

                    if (!url.isNullOrEmpty()) {
                        addressBar.setText(url)
                    }
                }

                override fun onPageFinished(
                    view: WebView,
                    url: String?
                ) {

                    super.onPageFinished(
                        view,
                        url
                    )

                    loadingBar.visibility =
                        View.GONE

                    if (!url.isNullOrEmpty()) {
                        addressBar.setText(url)
                    }
                }
            }

        webView.webChromeClient =
            object : WebChromeClient() {

                override fun onProgressChanged(
                    view: WebView,
                    newProgress: Int
                ) {

                    loadingBar.progress =
                        newProgress

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

        val goButton =
            findViewById<ImageButton>(
                R.id.goButton
            )

        val backButton =
            findViewById<ImageButton>(
                R.id.backButton
            )

        val forwardButton =
            findViewById<ImageButton>(
                R.id.forwardButton
            )

        val refreshButton =
            findViewById<ImageButton>(
                R.id.refreshButton
            )

        val homeButton =
            findViewById<ImageButton>(
                R.id.homeButton
            )

        goButton.setOnClickListener {
            loadAddress()
        }

        addressBar.setOnEditorActionListener {
                _, actionId, _ ->

            if (
                actionId ==
                EditorInfo.IME_ACTION_GO
            ) {

                loadAddress()

                true

            } else {

                false
            }
        }

        backButton.setOnClickListener {

            if (
                webView.visibility ==
                View.VISIBLE &&
                webView.canGoBack()
            ) {
                webView.goBack()
            }
        }

        forwardButton.setOnClickListener {

            if (
                webView.visibility ==
                View.VISIBLE &&
                webView.canGoForward()
            ) {
                webView.goForward()
            }
        }

        refreshButton.setOnClickListener {

            if (
                webView.visibility ==
                View.VISIBLE
            ) {
                webView.reload()
            } else {
                loadLatestNews()
                loadSportNews()
            }
        }

        homeButton.setOnClickListener {
            showHomePage()
        }

        findViewById<View>(
            R.id.siteFacebook
        ).setOnClickListener {
            openWebsite(
                "https://www.facebook.com"
            )
        }

        findViewById<View>(
            R.id.siteInstagram
        ).setOnClickListener {
            openWebsite(
                "https://www.instagram.com"
            )
        }

        findViewById<View>(
            R.id.siteJobs
        ).setOnClickListener {
            openWebsite(
                "https://www.indeed.com"
            )
        }

        findViewById<View>(
            R.id.siteScholarships
        ).setOnClickListener {
            openWebsite(
                "https://www.scholarships.com"
            )
        }

        findViewById<View>(
            R.id.siteX
        ).setOnClickListener {
            openWebsite(
                "https://x.com"
            )
        }

        findViewById<View>(
            R.id.siteDeeprowss
        ).setOnClickListener {
            openWebsite(
                "https://deeprowss.com"
            )
        }

        findViewById<View>(
            R.id.deeprowssPromo
        ).setOnClickListener {
            openWebsite(
                "https://deeprowss.com"
            )
        }

        findViewById<View>(
            R.id.moreNewsButton
        ).setOnClickListener {
            openWebsite(
                "https://news.google.com"
            )
        }

        findViewById<View>(
            R.id.moreSportNewsButton
        ).setOnClickListener {
            openWebsite(
                "https://news.google.com/search?q=sports"
            )
        }
    }

    private fun loadLatestNews() {

        showLoading(
            newsList,
            "Loading latest news..."
        )

        activityScope.launch {

            val articles =
                newsRepository.getLatestNews(
                    limit = 4
                )

            newsList.removeAllViews()

            if (articles.isEmpty()) {

                showEmptyMessage(
                    newsList,
                    "Unable to load latest news."
                )

                return@launch
            }

            articles.forEach { article ->

                val card =
                    createNewsCard(
                        article
                    )

                newsList.addView(card)

                article.imageUrl?.let {

                    loadNewsImage(
                        it,
                        card
                    )
                }
            }
        }
    }

    private fun loadSportNews() {

        showLoading(
            sportNewsList,
            "Loading sport news..."
        )

        activityScope.launch {

            val articles =
                newsRepository.getSportNews(
                    limit = 4
                )

            sportNewsList.removeAllViews()

            if (articles.isEmpty()) {

                showEmptyMessage(
                    sportNewsList,
                    "Unable to load sport news."
                )

                return@launch
            }

            articles.forEach { article ->

                val card =
                    createNewsCard(
                        article
                    )

                sportNewsList.addView(card)

                article.imageUrl?.let {

                    loadNewsImage(
                        it,
                        card
                    )
                }
            }
        }
    }

    private fun createNewsCard(
        article: NewsArticle
    ): LinearLayout {

        val card =
            LinearLayout(this)

        card.orientation =
            LinearLayout.HORIZONTAL

        card.gravity =
            Gravity.CENTER_VERTICAL

        card.setPadding(
            dp(7),
            dp(7),
            dp(9),
            dp(7)
        )

        card.setBackgroundColor(
            Color.rgb(
                22,
                34,
                53
            )
        )

        val params =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(94)
            )

        params.setMargins(
            0,
            0,
            0,
            dp(8)
        )

        card.layoutParams = params

        val image =
            ImageView(this)

        image.layoutParams =
            LinearLayout.LayoutParams(
                dp(110),
                dp(80)
            )

        image.scaleType =
            ImageView.ScaleType.CENTER_CROP

        image.setBackgroundColor(
            Color.rgb(
                30,
                43,
                62
            )
        )

        card.addView(image)

        val content =
            LinearLayout(this)

        content.orientation =
            LinearLayout.VERTICAL

        content.gravity =
            Gravity.CENTER_VERTICAL

        content.setPadding(
            dp(11),
            0,
            dp(3),
            0
        )

        content.layoutParams =
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )

        val title =
            TextView(this)

        title.text =
            article.title

        title.setTextColor(
            Color.WHITE
        )

        title.textSize =
            13.5f

        title.typeface =
            Typeface.DEFAULT_BOLD

        title.maxLines =
            3

        title.ellipsize =
            TextUtils.TruncateAt.END

        content.addView(title)

        if (
            article.source.isNotBlank()
        ) {

            val source =
                TextView(this)

            source.text =
                article.source

            source.setTextColor(
                Color.rgb(
                    132,
                    148,
                    169
                )
            )

            source.textSize =
                10.5f

            source.maxLines =
                1

            source.ellipsize =
                TextUtils.TruncateAt.END

            val sourceParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

            sourceParams.topMargin =
                dp(5)

            source.layoutParams =
                sourceParams

            content.addView(source)
        }

        card.addView(content)

        card.setOnClickListener {

            openWebsite(
                article.link
            )
        }

        return card
    }

    private fun loadNewsImage(
        imageUrl: String,
        card: LinearLayout
    ) {

        activityScope.launch(
            Dispatchers.IO
        ) {

            val bitmap =
                downloadImage(
                    imageUrl
                )

            if (bitmap != null) {

                launch(
                    Dispatchers.Main
                ) {

                    val image =
                        card.getChildAt(
                            0
                        ) as? ImageView

                    image?.setImageBitmap(
                        bitmap
                    )
                }
            }
        }
    }

    private fun downloadImage(
        imageUrl: String
    ): Bitmap? {

        var connection:
                HttpURLConnection? = null

        return try {

            val url =
                URL(imageUrl)

            connection =
                url.openConnection()
                    as HttpURLConnection

            connection.connectTimeout =
                10000

            connection.readTimeout =
                10000

            connection.instanceFollowRedirects =
                true

            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0"
            )

            connection.setRequestProperty(
                "Accept",
                "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8"
            )

            connection.connect()

            if (
                connection.responseCode
                !in 200..299
            ) {
                return null
            }

            connection.inputStream.use {
                BitmapFactory.decodeStream(it)
            }

        } catch (
            e: Exception
        ) {

            null

        } finally {

            connection?.disconnect()
        }
    }

    private fun showLoading(
        container: LinearLayout,
        message: String
    ) {

        container.removeAllViews()

        val text =
            TextView(this)

        text.text =
            message

        text.setTextColor(
            Color.rgb(
                132,
                148,
                169
            )
        )

        text.textSize =
            13f

        text.setPadding(
            dp(10),
            dp(10),
            dp(10),
            dp(10)
        )

        container.addView(text)
    }

    private fun showEmptyMessage(
        container: LinearLayout,
        message: String
    ) {

        val text =
            TextView(this)

        text.text =
            message

        text.setTextColor(
            Color.rgb(
                132,
                148,
                169
            )
        )

        text.textSize =
            13f

        text.setPadding(
            dp(10),
            dp(10),
            dp(10),
            dp(10)
        )

        container.addView(text)
    }

    private fun openWebsite(
        url: String
    ) {

        homePage.visibility =
            View.GONE

        webView.visibility =
            View.VISIBLE

        webView.loadUrl(url)
    }

    private fun showHomePage() {

        homePage.visibility =
            View.VISIBLE

        webView.visibility =
            View.GONE

        loadingBar.visibility =
            View.GONE

        addressBar.setText("")

        addressBar.hint =
            "Search or enter website"
    }

    private fun loadAddress() {

        val input =
            addressBar.text
                .toString()
                .trim()

        if (input.isEmpty()) {
            return
        }

        val url =
            if (
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
                        input.replace(
                            " ",
                            "+"
                        )
            }

        openWebsite(url)
    }

    private fun dp(
        value: Int
    ): Int {

        return (
            value *
                    resources.displayMetrics.density
            ).toInt()
    }

    override fun onBackPressed() {

        if (
            webView.visibility ==
            View.VISIBLE
        ) {

            if (
                webView.canGoBack()
            ) {

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
