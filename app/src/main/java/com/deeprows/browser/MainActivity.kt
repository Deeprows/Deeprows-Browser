package com.deeprows.browser

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
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
            Color.rgb(7, 9, 13)

        window.navigationBarColor =
            Color.rgb(7, 9, 13)

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

        setupWebView()
        setupControls()

        showHomePage()

        loadLatestNews()
    }

    private fun setupWebView() {

        webView.settings.apply {

            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            mediaPlaybackRequiresUserGesture =
                false
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
            R.id.siteSportyBet
        ).setOnClickListener {

            openWebsite(
                "https://www.sportybet.com"
            )
        }

        findViewById<View>(
            R.id.siteBet9ja
        ).setOnClickListener {

            openWebsite(
                "https://www.bet9ja.com"
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
    }

    private fun loadLatestNews() {

        newsList.removeAllViews()

        val loading =
            TextView(this)

        loading.text =
            "Loading latest news..."

        loading.setTextColor(
            Color.rgb(
                141,
                150,
                165
            )
        )

        loading.textSize = 14f

        newsList.addView(
            loading
        )

        activityScope.launch {

            val articles =
                newsRepository.getLatestNews()

            newsList.removeAllViews()

            if (articles.isEmpty()) {

                showEmptyMessage(
                    "Unable to load latest news.\n\n" +
                            "Check your internet connection " +
                            "and tap refresh."
                )

                return@launch
            }

            articles.forEach { article ->

                val card =
                    createNewsCard(
                        article
                    )

                newsList.addView(card)

                if (
                    !article.imageUrl.isNullOrBlank()
                ) {

                    loadNewsImage(
                        article.imageUrl,
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

        card.setPadding(
            10,
            10,
            10,
            10
        )

        card.setBackgroundColor(
            Color.rgb(
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
            10
        )

        card.layoutParams = params

        val image =
            ImageView(this)

        image.tag =
            article.link

        image.layoutParams =
            LinearLayout.LayoutParams(
                dp(110),
                dp(78)
            )

        image.scaleType =
            ImageView.ScaleType.CENTER_CROP

        image.setBackgroundColor(
            Color.rgb(
                27,
                32,
                41
            )
        )

        card.addView(image)

        val content =
            LinearLayout(this)

        content.orientation =
            LinearLayout.VERTICAL

        content.setPadding(
            12,
            2,
            4,
            2
        )

        content.layoutParams =
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )

        val title =
            TextView(this)

        title.text =
            article.title

        title.setTextColor(
            Color.WHITE
        )

        title.textSize = 14f

        title.maxLines = 3

        title.ellipsize =
            android.text.TextUtils.TruncateAt.END

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
                    141,
                    150,
                    165
                )
            )

            source.textSize = 11f

            source.maxLines = 1

            source.ellipsize =
                android.text.TextUtils.TruncateAt.END

            val sourceParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

            sourceParams.topMargin =
                dp(7)

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
        imageUrl: String?,
        card: LinearLayout
    ) {

        if (imageUrl.isNullOrBlank()) {
            return
        }

        activityScope.launch(
            Dispatchers.IO
        ) {

            val bitmap =
                downloadImage(
                    imageUrl
                )

            if (bitmap != null) {

                launch(Dispatchers.Main) {

                    val image =
                        card.getChildAt(
                            0
                        ) as? ImageView

                    if (
                        image != null &&
                        image.tag ==
                        card.getChildAt(
                            1
                        )
                    ) {
                        image.setImageBitmap(
                            bitmap
                        )
                    } else if (
                        image != null
                    ) {
                        image.setImageBitmap(
                            bitmap
                        )
                    }
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

    private fun showEmptyMessage(
        message: String
    ) {

        val textView =
            TextView(this)

        textView.text =
            message

        textView.setTextColor(
            Color.rgb(
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

        newsList.addView(
            textView
        )
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

        val url = if (
            input.startsWith(
                "http://"
            ) ||
            input.startsWith(
                "https://"
            )
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
