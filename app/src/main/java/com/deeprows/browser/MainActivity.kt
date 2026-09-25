package com.deeprows.browser

import android.app.AlertDialog
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
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

    // =========================================================
// OPEN TABS
// =========================================================

data class BrowserTab(
    val id: Int,
    var title: String,
    var url: String
)

private val openTabs =
    mutableListOf<BrowserTab>()

private var activeTabId = 0

private var nextTabId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        setupCategoryLinks()
        setupCategoryLogos()
        setupSettings()

        showHomePage()

        loadLatestNews()
        loadSportNews()

        hideSystemNavigationBar()
    }

    // =========================================================
    // SYSTEM NAVIGATION BAR
    // =========================================================

    private fun hideSystemNavigationBar() {

        if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.R
        ) {

            window.insetsController?.let { controller ->

                controller.hide(
                    WindowInsets.Type.navigationBars()
                )

                controller.systemBarsBehavior =
                    WindowInsetsController
                        .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
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

        // Hide search/address bar after website finishes loading
        addressBar.visibility =
            View.GONE

        findViewById<View>(
            R.id.goButton
        ).visibility =
            View.GONE
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

        if (
            settingsPage.visibility ==
            View.VISIBLE
        ) {

            showHomePage()

        } else if (
            webView.visibility ==
            View.VISIBLE &&
            webView.canGoBack()
        ) {

            webView.goBack()

        } else {

            showHomePage()
        }
    }

    findViewById<View>(
        R.id.forwardButton
    ).setOnClickListener {

        if (
            webView.visibility ==
            View.VISIBLE &&
            webView.canGoForward()
        ) {

            webView.goForward()
        }
    }
    
    // =====================================================
// REFRESH
// =====================================================

findViewById<View>(
    R.id.refreshPageButton
).setOnClickListener {

    if (
        webView.visibility ==
        View.VISIBLE
    ) {

        webView.reload()

    } else {

        Toast.makeText(
            this,
            "Open a website first",
            Toast.LENGTH_SHORT
        ).show()
    }
}

    // =====================================================
    // OPEN PAGES
    // =====================================================

  findViewById<View>(
    R.id.refreshButton
).setOnClickListener {

    showOpenTabs()
}

    // =====================================================
    // HOME
    // =====================================================

    findViewById<View>(
        R.id.homeButton
    ).setOnClickListener {

        showHomePage()
    }

    // =====================================================
    // BOOKMARK
    // =====================================================

    findViewById<View>(
        R.id.bookmarkButton
    ).setOnClickListener {

        saveCurrentBookmark()
    }

    // =====================================================
    // MENU
    // =====================================================

       findViewById<View>(
        R.id.menuButton
    ).setOnClickListener {

        showSettings()
    }

    // =====================================================
    // NEWS BUTTONS
    // =====================================================

    findViewById<View>(
        R.id.moreNewsButton
    ).setOnClickListener {

        openWebsite(
            "https://news.google.com/"
        )
    }

        findViewById<View>(
        R.id.moreSportNewsButton
    ).setOnClickListener {

        openWebsite(
            "https://news.google.com/search?q=football"
        )
    }
}

// =========================================================
// OPEN TABS
// =========================================================

private fun showOpenTabs() {

    if (openTabs.isEmpty()) {

        Toast.makeText(
            this,
            "No open tabs",
            Toast.LENGTH_SHORT
        ).show()

        return
    }

    val tabNames =
        openTabs.map { tab ->

            if (tab.id == activeTabId) {
                "✓ ${tab.title}"
            } else {
                tab.title
            }

        }.toTypedArray()

    AlertDialog.Builder(this)
        .setTitle("Open Tabs (${openTabs.size})")
        .setItems(tabNames) { _, which ->

            val selectedTab =
                openTabs[which]

            activeTabId =
                selectedTab.id

            webView.visibility =
                View.VISIBLE

            homePage.visibility =
                View.GONE

            settingsPage.visibility =
                View.GONE

            addressBar.visibility =
                View.GONE

            findViewById<View>(
                R.id.goButton
            ).visibility =
                View.GONE

            webView.loadUrl(
                selectedTab.url
            )
        }
        .setNegativeButton(
            "Close",
            null
        )
        .show()
}


// =========================================================
// CATEGORY LINKS
// =========================================================

private fun setupCategoryLinks() {

        // =====================================================
        // WATCH FOOTBALL / LATEST MOVIES
        // =====================================================

        findViewById<View>(
            R.id.siteWatchFootball
        ).setOnClickListener {

            openWebsite(
                "https://deeprowss.com/"
            )
        }

        findViewById<View>(
            R.id.siteLatestMovies
        ).setOnClickListener {

            openWebsite(
                "https://deeprowss.com/"
            )
        }

        // =====================================================
        // SOCIAL & VIDEO
        // =====================================================

        findViewById<View>(
            R.id.siteFacebook
        ).setOnClickListener {

            openWebsite(
                "https://www.facebook.com/"
            )
        }

        findViewById<View>(
            R.id.siteTikTok
        ).setOnClickListener {

            openWebsite(
                "https://www.tiktok.com/"
            )
        }

        findViewById<View>(
            R.id.siteYouTube
        ).setOnClickListener {

            openWebsite(
                "https://www.youtube.com/"
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
            R.id.siteDailymotion
        ).setOnClickListener {

            openWebsite(
                "https://www.dailymotion.com/"
            )
        }

        // =====================================================
        // MESSAGING
        // =====================================================

        findViewById<View>(
            R.id.siteWhatsApp
        ).setOnClickListener {

            openWebsite(
                "https://web.whatsapp.com/"
            )
        }

        findViewById<View>(
            R.id.siteSnapchat
        ).setOnClickListener {

            openWebsite(
                "https://www.snapchat.com/"
            )
        }

        findViewById<View>(
            R.id.siteTelegram
        ).setOnClickListener {

            openWebsite(
                "https://web.telegram.org/"
            )
        }

        // =====================================================
        // JOBS
        // =====================================================

        findViewById<View>(
            R.id.siteLinkedInJobs
        ).setOnClickListener {

            openWebsite(
                "https://www.linkedin.com/jobs/"
            )
        }

        findViewById<View>(
            R.id.siteIndeed
        ).setOnClickListener {

            openWebsite(
                "https://www.indeed.com/"
            )
        }

        findViewById<View>(
            R.id.siteGlassdoor
        ).setOnClickListener {

            openWebsite(
                "https://www.glassdoor.com/"
            )
        }

        findViewById<View>(
            R.id.siteZipRecruiter
        ).setOnClickListener {

            openWebsite(
                "https://www.ziprecruiter.com/"
            )
        }

        findViewById<View>(
            R.id.siteBayt
        ).setOnClickListener {

            openWebsite(
                "https://www.bayt.com/"
            )
        }

        findViewById<View>(
            R.id.siteJooble
        ).setOnClickListener {

            openWebsite(
                "https://jooble.org/"
            )
        }

        findViewById<View>(
            R.id.siteMonster
        ).setOnClickListener {

            openWebsite(
                "https://www.monster.com/"
            )
        }

        findViewById<View>(
            R.id.siteJobStreet
        ).setOnClickListener {

            openWebsite(
                "https://www.jobstreet.com/"
            )
        }

        findViewById<View>(
            R.id.siteWellfound
        ).setOnClickListener {

            openWebsite(
                "https://wellfound.com/jobs"
            )
        }

        // =====================================================
        // SPORTS
        // =====================================================

        findViewById<View>(
            R.id.siteESPN
        ).setOnClickListener {

            openWebsite(
                "https://www.espn.com/"
            )
        }

        findViewById<View>(
            R.id.siteBBCSport
        ).setOnClickListener {

            openWebsite(
                "https://www.bbc.com/sport"
            )
        }

        findViewById<View>(
            R.id.siteSkySports
        ).setOnClickListener {

            openWebsite(
                "https://www.skysports.com/"
            )
        }

        findViewById<View>(
            R.id.siteGoal
        ).setOnClickListener {

            openWebsite(
                "https://www.goal.com/"
            )
        }

        findViewById<View>(
            R.id.siteAthletic
        ).setOnClickListener {

            openWebsite(
                "https://www.nytimes.com/athletic/"
            )
        }

        findViewById<View>(
            R.id.siteCBSSports
        ).setOnClickListener {

            openWebsite(
                "https://www.cbssports.com/"
            )
        }

        findViewById<View>(
            R.id.siteFoxSports
        ).setOnClickListener {

            openWebsite(
                "https://www.foxsports.com/"
            )
        }

        findViewById<View>(
            R.id.siteSportingNews
        ).setOnClickListener {

            openWebsite(
                "https://www.sportingnews.com/"
            )
        }

        findViewById<View>(
            R.id.siteEurosport
        ).setOnClickListener {

            openWebsite(
                "https://www.eurosport.com/"
            )
        }

        findViewById<View>(
            R.id.siteSportsIllustrated
        ).setOnClickListener {

            openWebsite(
                "https://www.si.com/"
            )
        }

        // =====================================================
        // NEWS
        // =====================================================

        findViewById<View>(
            R.id.siteBBCNews
        ).setOnClickListener {

            openWebsite(
                "https://www.bbc.com/news"
            )
        }

        findViewById<View>(
            R.id.siteReuters
        ).setOnClickListener {

            openWebsite(
                "https://www.reuters.com/"
            )
        }

        findViewById<View>(
            R.id.siteAP
        ).setOnClickListener {

            openWebsite(
                "https://apnews.com/"
            )
        }

        findViewById<View>(
            R.id.siteCNN
        ).setOnClickListener {

            openWebsite(
                "https://www.cnn.com/"
            )
        }

        findViewById<View>(
            R.id.siteAlJazeera
        ).setOnClickListener {

            openWebsite(
                "https://www.aljazeera.com/"
            )
        }

        findViewById<View>(
            R.id.siteGuardian
        ).setOnClickListener {

            openWebsite(
                "https://www.theguardian.com/international"
            )
        }

        findViewById<View>(
            R.id.siteNYTimes
        ).setOnClickListener {

            openWebsite(
                "https://www.nytimes.com/"
            )
        }

        findViewById<View>(
            R.id.siteSkyNews
        ).setOnClickListener {

            openWebsite(
                "https://news.sky.com/"
            )
        }

        findViewById<View>(
            R.id.siteFrance24
        ).setOnClickListener {

            openWebsite(
                "https://www.france24.com/en/"
            )
        }

        findViewById<View>(
            R.id.siteDW
        ).setOnClickListener {

            openWebsite(
                "https://www.dw.com/en/"
            )
        }

        // =====================================================
        // SCHOLARSHIPS & SPONSORSHIPS
        // =====================================================

        findViewById<View>(
            R.id.siteChevening
        ).setOnClickListener {

            openWebsite(
                "https://www.chevening.org/"
            )
        }

        findViewById<View>(
            R.id.siteErasmus
        ).setOnClickListener {

            openWebsite(
                "https://erasmus-plus.ec.europa.eu/opportunities/individuals/students/erasmus-mundus-joint-masters"
            )
        }

        findViewById<View>(
            R.id.siteDAAD
        ).setOnClickListener {

            openWebsite(
                "https://www.daad.de/en/studying-in-germany/scholarships/"
            )
        }

        findViewById<View>(
            R.id.siteCommonwealth
        ).setOnClickListener {

            openWebsite(
                "https://cscuk.fcdo.gov.uk/scholarships-filter-search/"
            )
        }

        findViewById<View>(
            R.id.siteMastercard
        ).setOnClickListener {

            openWebsite(
                "https://mastercardfdn.org/all/scholars/"
            )
        }

        findViewById<View>(
            R.id.siteSwedishInstitute
        ).setOnClickListener {

            openWebsite(
                "https://si.se/en/apply/scholarships/"
            )
        }

        findViewById<View>(
            R.id.siteOpportunityDesk
        ).setOnClickListener {

            openWebsite(
                "https://opportunitydesk.org/"
            )
        }

        findViewById<View>(
            R.id.siteScholarshipPositions
        ).setOnClickListener {

            openWebsite(
                "https://www.scholarshippositions.com/"
            )
        }

        findViewById<View>(
            R.id.siteStudyportals
        ).setOnClickListener {

            openWebsite(
                "https://www.mastersportal.com/scholarships/"
            )
        }

        findViewById<View>(
            R.id.siteAfricanUnion
        ).setOnClickListener {

            openWebsite(
                "https://au.int/"
            )
        }
    }

// =========================================================
// CATEGORY LOGOS
// =========================================================

private fun setupCategoryLogos() {

    val logos = mapOf(

        // Social & Video
        R.id.siteFacebook to "facebook.com",
        R.id.siteTikTok to "tiktok.com",
        R.id.siteYouTube to "youtube.com",
        R.id.siteX to "x.com",
        R.id.siteDailymotion to "dailymotion.com",

        // Messaging
        R.id.siteWhatsApp to "whatsapp.com",
        R.id.siteSnapchat to "snapchat.com",
        R.id.siteTelegram to "telegram.org",

        // Jobs
        R.id.siteLinkedInJobs to "linkedin.com",
        R.id.siteIndeed to "indeed.com",
        R.id.siteGlassdoor to "glassdoor.com",
        R.id.siteZipRecruiter to "ziprecruiter.com",
        R.id.siteBayt to "bayt.com",
        R.id.siteJooble to "jooble.org",
        R.id.siteMonster to "monster.com",
        R.id.siteJobStreet to "jobstreet.com",
        R.id.siteWellfound to "wellfound.com",

        // Sports
        R.id.siteESPN to "espn.com",
        R.id.siteBBCSport to "bbc.com",
        R.id.siteSkySports to "skysports.com",
        R.id.siteGoal to "goal.com",
        R.id.siteAthletic to "nytimes.com",
        R.id.siteCBSSports to "cbssports.com",
        R.id.siteFoxSports to "foxsports.com",
        R.id.siteSportingNews to "sportingnews.com",
        R.id.siteEurosport to "eurosport.com",
        R.id.siteSportsIllustrated to "si.com",

        // News
        R.id.siteBBCNews to "bbc.com",
        R.id.siteReuters to "reuters.com",
        R.id.siteAP to "apnews.com",
        R.id.siteCNN to "cnn.com",
        R.id.siteAlJazeera to "aljazeera.com",
        R.id.siteGuardian to "theguardian.com",
        R.id.siteNYTimes to "nytimes.com",
        R.id.siteSkyNews to "sky.com",
        R.id.siteFrance24 to "france24.com",
        R.id.siteDW to "dw.com",

        // Scholarships & Sponsorships
        R.id.siteChevening to "chevening.org",
        R.id.siteErasmus to "erasmus-plus.ec.europa.eu",
        R.id.siteDAAD to "daad.de",
        R.id.siteCommonwealth to "cscuk.fcdo.gov.uk",
        R.id.siteMastercard to "mastercardfdn.org",
        R.id.siteSwedishInstitute to "si.se",
        R.id.siteOpportunityDesk to "opportunitydesk.org",
        R.id.siteScholarshipPositions to "scholarshippositions.com",
        R.id.siteStudyportals to "mastersportal.com",
        R.id.siteAfricanUnion to "au.int"
    )

    logos.forEach { (id, domain) ->

        val textView =
            findViewById<android.widget.TextView>(id)

        loadWebsiteLogo(
            textView,
            domain
        )
    }

    // Watch Football and Latest Movies
    // are LinearLayouts, so they are handled separately.

    loadWebsiteLogoToImage(
        R.id.siteWatchFootball,
        "deeprowss.com"
    )

    loadWebsiteLogoToImage(
        R.id.siteLatestMovies,
        "deeprowss.com"
    )
}

private fun loadWebsiteLogo(
    textView: android.widget.TextView,
    domain: String
) {

    kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.Dispatchers.IO
    ).launch {

        try {

            val logoUrl =
                "https://www.google.com/s2/favicons?domain=$domain&sz=128"

            val connection =
                java.net.URL(
                    logoUrl
                ).openConnection()

            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.connect()

            val input =
                connection.getInputStream()

            val bitmap =
                android.graphics.BitmapFactory
                    .decodeStream(input)

            input.close()

            if (bitmap != null) {

                runOnUiThread {

                    val density =
                        resources.displayMetrics.density

                    val size =
                        (30 * density).toInt()

                    val drawable =
                        android.graphics.drawable.BitmapDrawable(
                            resources,
                            bitmap
                        )

                    drawable.setBounds(
                        0,
                        0,
                        size,
                        size
                    )

                    textView.setCompoundDrawables(
                        null,
                        drawable,
                        null,
                        null
                    )

                    textView.compoundDrawablePadding =
                        (7 * density).toInt()

                    textView.gravity =
                        android.view.Gravity.CENTER

                    textView.includeFontPadding =
                        false

                    textView.setPadding(
                        (4 * density).toInt(),
                        (8 * density).toInt(),
                        (4 * density).toInt(),
                        (8 * density).toInt()
                    )
                }
            }

        } catch (_: Exception) {

            // Keep the website name visible
        }
    }
}

private fun loadWebsiteLogoToImage(
    containerId: Int,
    domain: String
) {

    kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.Dispatchers.IO
    ).launch {

        try {

            val logoUrl =
                "https://www.google.com/s2/favicons?domain=$domain&sz=128"

            val connection =
                java.net.URL(
                    logoUrl
                ).openConnection()

            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.connect()

            val input =
                connection.getInputStream()

            val bitmap =
                android.graphics.BitmapFactory
                    .decodeStream(input)

            input.close()

            if (bitmap != null) {

                runOnUiThread {

                    val container =
                        findViewById<android.widget.LinearLayout>(
                            containerId
                        )

                    val imageView =
                        container.getChildAt(0)

                            as? android.widget.ImageView

                    imageView?.setImageBitmap(
                        bitmap
                    )
                }
            }

        } catch (_: Exception) {

            // Keep the existing icon if logo fails
        }
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

    addressBar.visibility =
        View.VISIBLE

    findViewById<View>(
        R.id.goButton
    ).visibility =
        View.VISIBLE
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

    val existingTab =
        openTabs.find {
            it.id == activeTabId
        }

    if (existingTab == null) {

        val newTab =
            BrowserTab(
                id = nextTabId++,
                title = "New Tab",
                url = url
            )

        openTabs.add(newTab)

        activeTabId =
            newTab.id

    } else {

        existingTab.url =
            url
    }

    updateTabsCount()

    webView.loadUrl(url)
}
   
    private fun updateTabsCount() {

    findViewById<android.widget.TextView>(
        R.id.pagesCount
    ).text =
        openTabs.size.toString()
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
