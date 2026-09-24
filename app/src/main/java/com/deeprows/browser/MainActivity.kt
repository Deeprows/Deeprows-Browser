package com.deeprows.browser

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ScrollView
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.view.WindowInsets
import android.view.WindowInsetsController

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var homePage: ScrollView
    private lateinit var settingsPage: ScrollView

    private lateinit var addressBar: android.widget.EditText
    private lateinit var loadingBar: android.widget.ProgressBar

    private lateinit var dataSavingSwitch: Switch
    private lateinit var adBlockingSwitch: Switch

    private val preferences by lazy {
        getSharedPreferences(
            "deeprows_browser",
            MODE_PRIVATE
        )
    }

    private val newsRepository =
        NewsRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemNavigationBar()

        setContentView(R.layout.activity_main)

        webView =
            findViewById(R.id.webView)

        homePage =
            findViewById(R.id.homePage)

        settingsPage =
            findViewById(R.id.settingsPage)

        addressBar =
            findViewById(R.id.addressBar)

        loadingBar =
            findViewById(R.id.loadingBar)

        dataSavingSwitch =
            findViewById(R.id.dataSavingSwitch)

        adBlockingSwitch =
            findViewById(R.id.adBlockingSwitch)

        setupWebView()
        setupControls()
        setupSettings()

        showHomePage()

        loadLatestNews()
        loadSportNews()
    }
private fun hideSystemNavigationBar() {

    if (android.os.Build.VERSION.SDK_INT >=
        android.os.Build.VERSION_CODES.R
    ) {

        window.insetsController?.let { controller ->

            controller.hide(
                WindowInsets.Type.navigationBars()
            )

            controller.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

    } else {

        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }
}

    // =========================================================
    // WEBVIEW
    // =========================================================

    private fun setupWebView() {

        webView.settings.apply {

            javaScriptEnabled = true

            domStorageEnabled = true

            loadWithOverviewMode = true

            useWideViewPort = true

            mediaPlaybackRequiresUserGesture = false

            allowFileAccess = true

            allowContentAccess = true

            blockNetworkImage =
                preferences.getBoolean(
                    "data_saving",
                    false
                )
        }

        webView.webViewClient =
            object : WebViewClient() {

                override fun onPageStarted(
                    view: WebView?,
                    url: String?,
                    favicon: Bitmap?
                ) {

                    loadingBar.visibility =
                        View.VISIBLE

                    if (
                        !url.isNullOrBlank() &&
                        (
                            url.startsWith("http://") ||
                            url.startsWith("https://")
                        )
                    ) {
                        addressBar.setText(url)
                        saveHistory(url)
                    }
                }

                override fun onPageFinished(
                    view: WebView?,
                    url: String?
                ) {

                    loadingBar.visibility =
                        View.GONE

                    if (
                        url != null &&
                        (
                            url.startsWith("http://") ||
                            url.startsWith("https://")
                        )
                    ) {
                        addressBar.setText(url)
                    }
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {

                    return false
                }
            }

        webView.setOnScrollChangeListener {
            _, _, _, _, _ ->
        }
    }

    // =========================================================
    // CONTROLS
    // =========================================================

    private fun setupControls() {

        findViewById<View>(
            R.id.goButton
        ).setOnClickListener {

            openAddress()
        }

        addressBar.setOnEditorActionListener {
                _, _, _ ->

            openAddress()

            true
        }

        findViewById<View>(
            R.id.backButton
        ).setOnClickListener {

            if (settingsPage.visibility == View.VISIBLE) {

                showHomePage()

            } else if (webView.canGoBack()) {

                webView.goBack()

            } else {

                showHomePage()
            }
        }

        findViewById<View>(
            R.id.forwardButton
        ).setOnClickListener {

            if (webView.canGoForward()) {

                webView.goForward()
            }
        }

        findViewById<View>(
            R.id.refreshButton
        ).setOnClickListener {

            if (webView.visibility == View.VISIBLE) {

                webView.reload()

            } else {

                loadLatestNews()
                loadSportNews()
            }
        }

        findViewById<View>(
            R.id.homeButton
        ).setOnClickListener {

            showHomePage()
        }

        findViewById<View>(
            R.id.menuButton
        ).setOnClickListener {

            showSettings()
        }

        findViewById<View>(
            R.id.siteFacebook
        ).setOnClickListener {

            openWebsite(
                "https://www.facebook.com/"
            )
        }

        findViewById<View>(
            R.id.siteInstagram
        ).setOnClickListener {

            openWebsite(
                "https://www.instagram.com/"
            )
        }

        findViewById<View>(
            R.id.siteJobs
        ).setOnClickListener {

            openWebsite(
                "https://www.indeed.com/"
            )
        }

        findViewById<View>(
            R.id.siteScholarships
        ).setOnClickListener {

            openWebsite(
                "https://www.scholarships.com/"
            )
        }

        findViewById<View>(
            R.id.siteX
        ).setOnClickListener {

            openWebsite(
                "https://x.com/"
            )
        }

        findViewById<View>(
            R.id.siteDeeprowss
        ).setOnClickListener {

            openWebsite(
                "https://deeprowss.com/"
            )
        }

        // =====================================================
        // DEEPROWSS PROMO SECTION
        // =====================================================

        findViewById<View>(
            R.id.deeprowssPromo
        ).setOnClickListener {

            openWebsite(
                "https://deeprowss.com/"
            )
        }
    }

    // =========================================================
    // SETTINGS
    // =========================================================

    private fun setupSettings() {

        dataSavingSwitch.isChecked =
            preferences.getBoolean(
                "data_saving",
                false
            )

        adBlockingSwitch.isChecked =
            preferences.getBoolean(
                "ad_blocking",
                false
            )

        dataSavingSwitch.setOnCheckedChangeListener {
                _, enabled ->

            preferences.edit()
                .putBoolean(
                    "data_saving",
                    enabled
                )
                .apply()

            webView.settings.blockNetworkImage =
                enabled

            Toast.makeText(
                this,
                if (enabled)
                    "Data Saving enabled"
                else
                    "Data Saving disabled",
                Toast.LENGTH_SHORT
            ).show()
        }

        adBlockingSwitch.setOnCheckedChangeListener {
                _, enabled ->

            preferences.edit()
                .putBoolean(
                    "ad_blocking",
                    enabled
                )
                .apply()

            Toast.makeText(
                this,
                if (enabled)
                    "Ad Blocking enabled"
                else
                    "Ad Blocking disabled",
                Toast.LENGTH_SHORT
            ).show()
        }

        findViewById<View>(
            R.id.historyButton
        ).setOnClickListener {

            showHistory()
        }

        findViewById<View>(
            R.id.bookmarksButton
        ).setOnClickListener {

            showBookmarks()
        }

        findViewById<View>(
            R.id.offlinePagesButton
        ).setOnClickListener {

            showOfflinePages()
        }

        findViewById<View>(
            R.id.clearCacheButton
        ).setOnClickListener {

            webView.clearCache(true)

            Toast.makeText(
                this,
                "Browser cache cleared",
                Toast.LENGTH_SHORT
            ).show()
        }

        findViewById<View>(
            R.id.settingsBackButton
        ).setOnClickListener {

            showHomePage()
        }
    }

    // =========================================================
    // HOME / SETTINGS / WEB
    // =========================================================

    private fun showHomePage() {

        homePage.visibility =
            View.VISIBLE

        webView.visibility =
            View.GONE

        settingsPage.visibility =
            View.GONE

        loadingBar.visibility =
            View.GONE
    }

    private fun showSettings() {

        homePage.visibility =
            View.GONE

        webView.visibility =
            View.GONE

        settingsPage.visibility =
            View.VISIBLE

        loadingBar.visibility =
            View.GONE
    }

    private fun openWebsite(
        url: String
    ) {

        settingsPage.visibility =
            View.GONE

        homePage.visibility =
            View.GONE

        webView.visibility =
            View.VISIBLE

        webView.loadUrl(url)
    }

    // =========================================================
    // ADDRESS BAR
    // =========================================================

    private fun openAddress() {

        var text =
            addressBar.text
                .toString()
                .trim()

        if (text.isBlank()) {
            return
        }

        if (
            !text.startsWith("http://") &&
            !text.startsWith("https://")
        ) {

            if (
                text.contains(".") &&
                !text.contains(" ")
            ) {

                text =
                    "https://$text"

            } else {

                text =
                    "https://www.google.com/search?q=" +
                            Uri.encode(text)
            }
        }

        openWebsite(text)
    }

    // =========================================================
    // HISTORY
    // =========================================================

    private fun saveHistory(
        url: String
    ) {

        if (
            url.isBlank() ||
            url == "about:blank" ||
            (
                !url.startsWith("http://") &&
                !url.startsWith("https://")
            )
        ) {
            return
        }

        val history =
            preferences.getStringSet(
                "history",
                emptySet()
            )?.toMutableList()
                ?: mutableListOf()

        history.remove(url)

        history.add(
            0,
            url
        )

        val limited =
            history
                .take(50)
                .toSet()

        preferences.edit()
            .putStringSet(
                "history",
                limited
            )
            .apply()
    }

    private fun showHistory() {

        val history =
            preferences.getStringSet(
                "history",
                emptySet()
            )?.toList()
                ?: emptyList()

        if (history.isEmpty()) {

            AlertDialog.Builder(this)
                .setTitle("History")
                .setMessage(
                    "No browsing history yet."
                )
                .setPositiveButton(
                    "OK",
                    null
                )
                .show()

            return
        }

        AlertDialog.Builder(this)
            .setTitle("History")
            .setItems(
                history.toTypedArray()
            ) { _, which ->

                openWebsite(
                    history[which]
                )
            }
            .setNegativeButton(
                "Clear History"
            ) { _, _ ->

                preferences.edit()
                    .remove("history")
                    .apply()

                Toast.makeText(
                    this,
                    "History cleared",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setPositiveButton(
                "Close",
                null
            )
            .show()
    }

    // =========================================================
    // BOOKMARKS
    // =========================================================

    private fun saveCurrentBookmark() {

        val url =
            webView.url

        if (
            url.isNullOrBlank() ||
            (
                !url.startsWith("http://") &&
                !url.startsWith("https://")
            )
        ) {

            Toast.makeText(
                this,
                "Open a website first",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val bookmarks =
            preferences.getStringSet(
                "bookmarks",
                emptySet()
            )?.toMutableSet()
                ?: mutableSetOf()

        bookmarks.add(url)

        preferences.edit()
            .putStringSet(
                "bookmarks",
                bookmarks
            )
            .apply()

        Toast.makeText(
            this,
            "Page bookmarked",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showBookmarks() {

        val bookmarks =
            preferences.getStringSet(
                "bookmarks",
                emptySet()
            )?.toList()
                ?: emptyList()

        if (bookmarks.isEmpty()) {

            AlertDialog.Builder(this)
                .setTitle("Bookmarks")
                .setMessage(
                    "No bookmarks saved yet."
                )
                .setPositiveButton(
                    "OK",
                    null
                )
                .show()

            return
        }

        AlertDialog.Builder(this)
            .setTitle("Bookmarks")
            .setItems(
                bookmarks.toTypedArray()
            ) { _, which ->

                openWebsite(
                    bookmarks[which]
                )
            }
            .setNegativeButton(
                "Clear All"
            ) { _, _ ->

                preferences.edit()
                    .remove("bookmarks")
                    .apply()

                Toast.makeText(
                    this,
                    "Bookmarks cleared",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setPositiveButton(
                "Close",
                null
            )
            .show()
    }

    // =========================================================
    // OFFLINE PAGES
    // =========================================================

    private fun getOfflineDirectory(): File {

        val directory =
            File(
                filesDir,
                "offline_pages"
            )

        if (!directory.exists()) {
            directory.mkdirs()
        }

        return directory
    }

    private fun saveOfflinePage() {

        val url =
            webView.url

        if (
            url.isNullOrBlank() ||
            url == "about:blank" ||
            (
                !url.startsWith("http://") &&
                !url.startsWith("https://")
            )
        ) {

            Toast.makeText(
                this,
                "Open a website first",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (webView.progress < 100) {

            Toast.makeText(
                this,
                "Wait until the page finishes loading",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val title =
            webView.title
                ?.trim()
                ?.ifBlank {
                    "Offline Page"
                }
                ?: "Offline Page"

        val safeTitle =
            title
                .replace(
                    Regex("[^A-Za-z0-9 _-]"),
                    ""
                )
                .replace(
                    Regex("\\s+"),
                    "_"
                )
                .take(50)
                .ifBlank {
                    "Offline_Page"
                }

        val timestamp =
            SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.US
            ).format(
                Date()
            )

        val file =
            File(
                getOfflineDirectory(),
                "${safeTitle}_$timestamp.mht"
            )

        Toast.makeText(
            this,
            "Saving offline page...",
            Toast.LENGTH_SHORT
        ).show()

        webView.saveWebArchive(
            file.absolutePath,
            false
        ) { savedPath ->

            runOnUiThread {

                if (
                    !savedPath.isNullOrBlank() &&
                    File(savedPath).exists()
                ) {

                    Toast.makeText(
                        this,
                        "Offline page saved",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {

                    Toast.makeText(
                        this,
                        "Unable to save offline page",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun showOfflinePages() {

        val directory =
            getOfflineDirectory()

        val files =
            directory
                .listFiles { file ->

                    file.isFile &&
                            file.extension.equals(
                                "mht",
                                ignoreCase = true
                            )
                }
                ?.sortedByDescending {
                    it.lastModified()
                }
                ?: emptyList()

        val builder =
            AlertDialog.Builder(this)
                .setTitle("Offline Pages")

        if (files.isEmpty()) {

            builder.setMessage(
                "No offline pages saved yet."
            )

        } else {

            val names =
                files.map { file ->

                    file.nameWithoutExtension
                        .replace("_", " ")

                }.toTypedArray()

            builder.setItems(
                names
            ) { _, which ->

                openOfflinePage(
                    files[which]
                )
            }
        }

        builder.setPositiveButton(
            "Save Current"
        ) { _, _ ->

            saveOfflinePage()
        }

        if (files.isNotEmpty()) {

            builder.setNeutralButton(
                "Clear All"
            ) { _, _ ->

                clearOfflinePages()
            }
        }

        builder.setNegativeButton(
            "Close",
            null
        )

        builder.show()
    }

    private fun openOfflinePage(
        file: File
    ) {

        if (!file.exists()) {

            Toast.makeText(
                this,
                "Offline page no longer exists",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        settingsPage.visibility =
            View.GONE

        homePage.visibility =
            View.GONE

        webView.visibility =
            View.VISIBLE

        loadingBar.visibility =
            View.GONE

        addressBar.setText(
            "Offline: " +
                    file.nameWithoutExtension
                        .replace("_", " ")
        )

        webView.loadUrl(
            Uri.fromFile(file).toString()
        )
    }

    private fun clearOfflinePages() {

        val directory =
            getOfflineDirectory()

        val files =
            directory.listFiles()

        var deleted = 0

        files?.forEach { file ->

            if (
                file.isFile &&
                file.extension.equals(
                    "mht",
                    ignoreCase = true
                )
            ) {

                if (file.delete()) {
                    deleted++
                }
            }
        }

        Toast.makeText(
            this,
            "$deleted offline page(s) deleted",
            Toast.LENGTH_SHORT
        ).show()
    }

    // =========================================================
    // NEWS
    // =========================================================

    private fun loadLatestNews() {

        val newsList =
            findViewById<android.widget.LinearLayout>(
                R.id.newsList
            )

        newsList.removeAllViews()

        kotlinx.coroutines.CoroutineScope(
            kotlinx.coroutines.Dispatchers.Main
        ).launch {

            val articles =
                newsRepository.getLatestNews(4)

            articles.forEach { article ->

                addNewsCard(
                    newsList,
                    article
                )
            }
        }
    }

    private fun loadSportNews() {

        val sportNewsList =
            findViewById<android.widget.LinearLayout>(
                R.id.sportNewsList
            )

        sportNewsList.removeAllViews()

        kotlinx.coroutines.CoroutineScope(
            kotlinx.coroutines.Dispatchers.Main
        ).launch {

            val articles =
                newsRepository.getSportNews(4)

            articles.forEach { article ->

                addNewsCard(
                    sportNewsList,
                    article
                )
            }
        }
    }

    private fun addNewsCard(
        container: android.widget.LinearLayout,
        article: NewsArticle
    ) {
        val card =
            android.widget.LinearLayout(this)

        card.orientation =
            android.widget.LinearLayout.HORIZONTAL

        card.setPadding(
            14,
            14,
            14,
            14
        )

        card.setBackgroundColor(
            android.graphics.Color.parseColor(
                "#151A22"
            )
        )

        val cardParams =
            android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )

        cardParams.setMargins(
            0,
            0,
            0,
            10
        )

        card.layoutParams =
            cardParams

        val imageView =
            android.widget.ImageView(this)

        val imageParams =
            android.widget.LinearLayout.LayoutParams(
                105,
                85
            )

        imageParams.setMargins(
            0,
            0,
            14,
            0
        )

        imageView.layoutParams =
            imageParams

        imageView.scaleType =
            android.widget.ImageView.ScaleType.CENTER_CROP

        imageView.setBackgroundColor(
            android.graphics.Color.parseColor(
                "#1B2029"
            )
        )

        val textContainer =
            android.widget.LinearLayout(this)

        textContainer.orientation =
            android.widget.LinearLayout.VERTICAL

        textContainer.layoutParams =
            android.widget.LinearLayout.LayoutParams(
                0,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )

        val title =
            android.widget.TextView(this)

        title.text =
            article.title

        title.setTextColor(
            android.graphics.Color.WHITE
        )

        title.textSize =
            15f

        title.setTypeface(
            null,
            android.graphics.Typeface.BOLD
        )

        title.maxLines =
            3

        title.ellipsize =
            android.text.TextUtils.TruncateAt.END

        val source =
            android.widget.TextView(this)

        source.text =
            if (article.source.isNotBlank()) {
                article.source
            } else {
                "Google News"
            }

        source.setTextColor(
            android.graphics.Color.parseColor(
                "#FF1744"
            )
        )

        source.textSize =
            12f

        source.setPadding(
            0,
            8,
            0,
            0
        )

        textContainer.addView(
            title
        )

        textContainer.addView(
            source
        )

        card.addView(
            imageView
        )

        card.addView(
            textContainer
        )

        card.setOnClickListener {

            openWebsite(
                article.link
            )
        }

        container.addView(
            card
        )

        if (
            article.imageUrl.isNotBlank()
        ) {

            kotlinx.coroutines.CoroutineScope(
                kotlinx.coroutines.Dispatchers.IO
            ).launch {

                try {

                    val connection =
                        java.net.URL(
                            article.imageUrl
                        ).openConnection()

                    connection.connect()

                    val input =
                        connection.getInputStream()

                    val bitmap =
                        android.graphics.BitmapFactory
                            .decodeStream(input)

                    input.close()

                    runOnUiThread {

                        if (bitmap != null) {

                            imageView.setImageBitmap(
                                bitmap
                            )
                        }
                    }

                } catch (_: Exception) {

                    // Keep placeholder
                }
            }
        }
    }
}
