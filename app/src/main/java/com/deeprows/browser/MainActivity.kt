package com.deeprows.browser

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.webkit.CookieManager
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

data class HomeSite(
    val name: String,
    val url: String
)

data class HomeSubCategory(
    val title: String,
    val sites: List<HomeSite>
)

data class HomeCategory(
    val title: String,
    val subCategories: List<HomeSubCategory>
)

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
        setupDynamicHomepage()
        setupSettings()

createNotificationChannel()
requestNotificationPermission()
showNotification(
    "Deeprows Browser",
    "Notifications are working!"
)

applyAppTheme()

showHomePage()

        loadLatestNews()
loadSportNews()
loadGoogleTrends()

        hideSystemNavigationBar()
    }
    
private fun setupDynamicHomepage() {

    val container =
        findViewById<LinearLayout>(R.id.siteCategoriesContainer)

    container.removeAllViews()

    val categories = listOf(

        HomeCategory(
            "🤖 AI TOOLS",
            listOf(

                HomeSubCategory(
                    "🎬 AI VIDEO",
                    listOf(
                        HomeSite("Kling AI", "https://klingai.com/"),
                        HomeSite("Hailuo AI", "https://hailuoai.video/"),
                        HomeSite("Pika", "https://pika.art/"),
                        HomeSite("Runway", "https://runwayml.com/"),
                        HomeSite("Luma Dream Machine", "https://lumalabs.ai/dream-machine"),
                        HomeSite("PixVerse", "https://pixverse.ai/"),
                        HomeSite("Vidu", "https://www.vidu.com/"),
                        HomeSite("CapCut", "https://www.capcut.com/"),
                        HomeSite("Canva", "https://www.canva.com/"),
                        HomeSite("Krea", "https://www.krea.ai/")
                    )
                ),

                HomeSubCategory(
                    "🖼️ AI IMAGE",
                    listOf(
                        HomeSite("Microsoft Designer", "https://designer.microsoft.com/"),
                        HomeSite("Leonardo AI", "https://leonardo.ai/"),
                        HomeSite("Ideogram", "https://ideogram.ai/"),
                        HomeSite("Adobe Firefly", "https://firefly.adobe.com/"),
                        HomeSite("Google Gemini", "https://gemini.google.com/"),
                        HomeSite("Canva AI", "https://www.canva.com/ai-image-generator/"),
                        HomeSite("Playground AI", "https://playground.com/"),
                        HomeSite("Krea AI", "https://www.krea.ai/"),
                        HomeSite("Freepik AI", "https://www.freepik.com/ai/image-generator"),
                        HomeSite("Craiyon", "https://www.craiyon.com/")
                    )
                ),

                HomeSubCategory(
                    "🎵 AI AUDIO / MUSIC / VOICE",
                    listOf(
                        HomeSite("ElevenLabs", "https://elevenlabs.io/"),
                        HomeSite("Suno", "https://suno.com/"),
                        HomeSite("Udio", "https://udio.com/"),
                        HomeSite("Murf AI", "https://murf.ai/"),
                        HomeSite("PlayHT", "https://play.ht/"),
                        HomeSite("Speechify", "https://speechify.com/"),
                        HomeSite("AIVA", "https://www.aiva.ai/"),
                        HomeSite("Soundraw", "https://soundraw.io/"),
                        HomeSite("Adobe Podcast", "https://podcast.adobe.com/"),
                        HomeSite("TTSMaker", "https://ttsmaker.com/")
                    )
                )
            )
        ),

        HomeCategory(
            "🎓 EDUCATION",
            listOf(

                HomeSubCategory(
                    "📚 FREE ONLINE COURSES",
                    listOf(
                        HomeSite("MIT OpenCourseWare", "https://ocw.mit.edu/"),
                        HomeSite("OpenLearn", "https://www.open.edu/openlearn/"),
                        HomeSite("edX", "https://www.edx.org/"),
                        HomeSite("Coursera", "https://www.coursera.org/"),
                        HomeSite("Open Yale Courses", "https://oyc.yale.edu/"),
                        HomeSite("NPTEL", "https://nptel.ac.in/")
                    )
                ),

                HomeSubCategory(
                    "🎓 SCHOLARSHIPS & SPONSORSHIPS",
                    listOf(
                        HomeSite("Chevening", "https://www.chevening.org/"),
                        HomeSite("Erasmus+", "https://erasmus-plus.ec.europa.eu/"),
                        HomeSite("DAAD", "https://www.daad.de/en/studying-in-germany/scholarships/"),
                        HomeSite("Commonwealth", "https://cscuk.fcdo.gov.uk/scholarships-filter-search/"),
                        HomeSite("Mastercard Foundation", "https://mastercardfdn.org/all/scholars/"),
                        HomeSite("Swedish Institute", "https://si.se/en/apply/scholarships/"),
                        HomeSite("Opportunity Desk", "https://opportunitydesk.org/"),
                        HomeSite("Scholarship Positions", "https://www.scholarshippositions.com/"),
                        HomeSite("Studyportals", "https://www.mastersportal.com/scholarships/"),
                        HomeSite("African Union", "https://au.int/")
                    )
                )
            )
        ),

        HomeCategory(
            "💼 JOBS & CAREERS",
            listOf(
                HomeSubCategory(
                    "💼 JOB SITES",
                    listOf(
                        HomeSite("LinkedIn Jobs", "https://www.linkedin.com/jobs/"),
                        HomeSite("Indeed", "https://www.indeed.com/"),
                        HomeSite("Glassdoor", "https://www.glassdoor.com/"),
                        HomeSite("ZipRecruiter", "https://www.ziprecruiter.com/"),
                        HomeSite("Bayt", "https://www.bayt.com/"),
                        HomeSite("Jooble", "https://jooble.org/"),
                        HomeSite("Monster", "https://www.monster.com/"),
                        HomeSite("JobStreet", "https://www.jobstreet.com/"),
                        HomeSite("Wellfound", "https://wellfound.com/jobs")
                    )
                )
            )
        ),

        HomeCategory(
            "🎬 ENTERTAINMENT",
            listOf(

                HomeSubCategory(
                    "🎌 ANIME",
                    listOf(
                        HomeSite("Miruro", "https://www.miruro.tv/"),
                        HomeSite("AnimePahe", "https://animepahe.ru/"),
                        HomeSite("KickAssAnime", "https://kaa.to/")
                    )
                ),

                HomeSubCategory(
                    "🎨 CARTOONS",
                    listOf(
                        HomeSite("WatchCartoonOnline", "https://www.wco.tv/"),
                        HomeSite("SuperCartoons", "https://www.supercartoons.net/"),
                        HomeSite("Japanese Animated Film Classics", "https://animation.filmarchives.jp/")
                    )
                ),

                HomeSubCategory(
                    "🇰🇷 ASIAN / K-DRAMA",
                    listOf(
                        HomeSite("AsianCrush", "https://www.asiancrush.com/"),
                        HomeSite("OnDemandChina", "https://www.ondemandchina.com/"),
                        HomeSite("Einthusan", "https://einthusan.tv/")
                    )
                ),

                HomeSubCategory(
                    "🎞️ CLASSICS",
                    listOf(
                        HomeSite("Internet Archive", "https://archive.org/"),
                        HomeSite("WikiFlix", "https://wikiflix.toolforge.org/"),
                        HomeSite("NASA+", "https://plus.nasa.gov/")
                    )
                )
            )
        ),

        HomeCategory(
            "⚽ SPORTS",
            listOf(

                HomeSubCategory(
                    "⚽ SPORTS",
                    listOf(
                        HomeSite("ESPN", "https://www.espn.com/"),
                        HomeSite("BBC Sport", "https://www.bbc.com/sport"),
                        HomeSite("Sky Sports", "https://www.skysports.com/"),
                        HomeSite("Goal", "https://www.goal.com/"),
                        HomeSite("The Athletic", "https://www.nytimes.com/athletic/"),
                        HomeSite("CBS Sports", "https://www.cbssports.com/"),
                        HomeSite("FOX Sports", "https://www.foxsports.com/"),
                        HomeSite("Sporting News", "https://www.sportingnews.com/"),
                        HomeSite("Eurosport", "https://www.eurosport.com/"),
                        HomeSite("Sports Illustrated", "https://www.si.com/")
                    )
                ),

                HomeSubCategory(
                    "📼 SPORTS REPLAYS",
                    listOf(
                        HomeSite("Footballia", "https://footballia.online/"),
                        HomeSite("FullRaces", "https://fullraces.com/"),
                        HomeSite("HooFoot", "https://hoofoot.com/")
                    )
                )
            )
        ),

        HomeCategory(
            "📰 NEWS",
            listOf(
                HomeSubCategory(
                    "📰 NEWS SITES",
                    listOf(
                        HomeSite("BBC News", "https://www.bbc.com/news"),
                        HomeSite("Reuters", "https://www.reuters.com/"),
                        HomeSite("AP News", "https://apnews.com/"),
                        HomeSite("CNN", "https://www.cnn.com/"),
                        HomeSite("Al Jazeera", "https://www.aljazeera.com/"),
                        HomeSite("The Guardian", "https://www.theguardian.com/international"),
                        HomeSite("New York Times", "https://www.nytimes.com/"),
                        HomeSite("Sky News", "https://news.sky.com/"),
                        HomeSite("France 24", "https://www.france24.com/en/"),
                        HomeSite("DW", "https://www.dw.com/en/")
                    )
                )
            )
        ),

        HomeCategory(
            "📺 IPTV",
            listOf(

                HomeSubCategory(
                    "🛠️ IPTV TOOLS",
                    listOf(
                        HomeSite("Awesome IPTV", "https://github.com/iptv-org/awesome-iptv"),
                        HomeSite("IPTV Playlists", "https://iptv-org.github.io/"),
                        HomeSite("M3Unator", "https://m3unator.com/"),
                        HomeSite("M3U4U", "https://m3u4u.com/"),
                        HomeSite("M3U8DL-RE", "https://github.com/nilaoda/N_m3u8DL-RE")
                    )
                ),

                HomeSubCategory(
                    "▶️ IPTV PLAYERS",
                    listOf(
                        HomeSite("IPTVnator", "https://github.com/4gray/iptvnator"),
                        HomeSite("ynoTV", "https://ynotv.com/"),
                        HomeSite("Open TV", "https://opentv.app/"),
                        HomeSite("LivePush", "https://livepush.io/"),
                        HomeSite("Jellyfin", "https://jellyfin.org/")
                    )
                )
            )
        ),

        HomeCategory(
            "📱 ANDROID TV",
            listOf(
                HomeSubCategory(
                    "📱 ANDROID TV APPS",
                    listOf(
                        HomeSite("SmartTube", "https://github.com/yuliskov/SmartTube"),
                        HomeSite("TiviMate", "https://tivimate.com/"),
                        HomeSite("Downloader", "https://www.aftvnews.com/downloader/"),
                        HomeSite("CloudStream", "https://cloudstream3.com/"),
                        HomeSite("Nova Video Player", "https://github.com/nova-video-player/aos-AVP")
                    )
                )
            )
        ),

        HomeCategory(
            "📱 SOCIAL MEDIA",
            listOf(
                HomeSubCategory(
                    "📱 SOCIAL PLATFORMS",
                    listOf(
                        HomeSite("Facebook", "https://www.facebook.com/"),
                        HomeSite("TikTok", "https://www.tiktok.com/"),
                        HomeSite("YouTube", "https://www.youtube.com/"),
                        HomeSite("X", "https://x.com/"),
                        HomeSite("Dailymotion", "https://www.dailymotion.com/")
                    )
                )
            )
        ),
                HomeCategory(
            "💬 MESSAGING",
            listOf(
                HomeSubCategory(
                    "💬 MESSAGING APPS",
                    listOf(
                        HomeSite("WhatsApp", "https://web.whatsapp.com/"),
                        HomeSite("Snapchat", "https://www.snapchat.com/"),
                        HomeSite("Telegram", "https://web.telegram.org/")
                    )
                )
            )
        )
    )

    categories.forEach { category ->

        addMainCategoryHeader(
            container,
            category.title
        )

        category.subCategories.forEach { subCategory ->

            addSubCategoryHeader(
                container,
                subCategory.title
            )

            addSiteGrid(
                container,
                subCategory.sites
            )
        }
    }
}

private fun addMainCategoryHeader(
    container: LinearLayout,
    title: String
) {
    val titleView = TextView(this)

    titleView.text = title
    titleView.textSize = 21f
    titleView.setTypeface(null, Typeface.BOLD)
    titleView.setTextColor(Color.WHITE)

    titleView.setPadding(
        dp(8),
        dp(18),
        dp(8),
        dp(10)
    )

    container.addView(titleView)
}
private fun addSubCategoryHeader(
    container: LinearLayout,
    title: String
) {
    val titleView = TextView(this)

    titleView.text = title
    titleView.textSize = 15f
    titleView.setTypeface(null, Typeface.BOLD)
    titleView.setTextColor(Color.LTGRAY)

    titleView.setPadding(
        dp(12),
        dp(8),
        dp(8),
        dp(6)
    )

    container.addView(titleView)
}

private fun addSiteGrid(
    container: LinearLayout,
    sites: List<HomeSite>
) {

    val grid = GridLayout(this)

    grid.columnCount = 3
    grid.layoutParams =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

    sites.forEach { site ->

        val card = TextView(this)

        card.text = site.name
        card.textSize = 12f
        card.gravity = Gravity.CENTER
        card.setTextColor(Color.WHITE)

        card.setPadding(
            dp(5),
            dp(8),
            dp(5),
            dp(8)
        )

        val background = GradientDrawable()
        background.cornerRadius = dp(14).toFloat()
        background.setColor(Color.rgb(21, 26, 34))

        card.background = background

        val params =
            GridLayout.LayoutParams().apply {

                width = 0
                height = dp(82)

                columnSpec =
                    GridLayout.spec(
                        GridLayout.UNDEFINED,
                        1f
                    )

                setMargins(
                    dp(4),
                    dp(4),
                    dp(4),
                    dp(4)
                )
            }

        card.layoutParams = params

        card.setOnClickListener {
            openWebsite(site.url)
        }

        grid.addView(card)
    }

    container.addView(grid)
}

private fun dp(value: Int): Int {
    return (value * resources.displayMetrics.density).toInt()
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

           loadWithOverviewMode = false

useWideViewPort = false

            mediaPlaybackRequiresUserGesture = false

           userAgentString =
    "Mozilla/5.0 (Linux; Android 15; Mobile) " +
    "AppleWebKit/537.36 (KHTML, like Gecko) " +
    "Chrome/153.0.0.0 Mobile Safari/537.36"

setSupportZoom(true)
builtInZoomControls = true
displayZoomControls = false

            allowFileAccess = true

            allowContentAccess = true

            blockNetworkImage =
                preferences.getBoolean(
                    "data_saving",
                    false
                )
        }
CookieManager
    .getInstance()
    .setAcceptCookie(true)

CookieManager
    .getInstance()
    .setAcceptThirdPartyCookies(
        webView,
        true
    )
        
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
        // Update active tab information
val activeTab =
    openTabs.find {
        it.id == activeTabId
    }

if (activeTab != null) {

    activeTab.url =
        url

    activeTab.title =
        view?.title
            ?.trim()
            ?.ifBlank {
                "New Tab"
            }
            ?: "New Tab"
}

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

   findViewById<View>(
    R.id.moreTrendsButton
).setOnClickListener {

    openWebsite(
        "https://trends.google.com/trending"
    )
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
// THEME
// =====================================================

findViewById<View>(
    R.id.themeButton
).setOnClickListener {
    showThemeSelector()
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

    val container =
        android.widget.LinearLayout(this)

    container.orientation =
        android.widget.LinearLayout.VERTICAL

    container.setPadding(
        20,
        10,
        20,
        10
    )

    val dialog =
        AlertDialog.Builder(this)
            .setTitle(
                "Open Tabs (${openTabs.size})"
            )
            .setView(container)
            .setNegativeButton(
                "Close",
                null
            )
            .create()

    fun refreshTabList() {

        container.removeAllViews()

        dialog.setTitle(
            "Open Tabs (${openTabs.size})"
        )

        openTabs.forEach { tab ->

            val row =
                android.widget.LinearLayout(this)

            row.orientation =
                android.widget.LinearLayout.HORIZONTAL

            row.gravity =
                android.view.Gravity.CENTER_VERTICAL

            row.setPadding(
                12,
                12,
                8,
                12
            )

            row.setBackgroundColor(
                android.graphics.Color.parseColor(
                    if (tab.id == activeTabId)
                        "#26364F"
                    else
                        "#182437"
                )
            )

            val rowParams =
                android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )

            rowParams.setMargins(
                0,
                0,
                0,
                8
            )

            row.layoutParams =
                rowParams

            val title =
                android.widget.TextView(this)

            title.text =
                if (tab.id == activeTabId) {
                    "✓ ${tab.title}"
                } else {
                    tab.title
                }

            title.setTextColor(
                android.graphics.Color.WHITE
            )

            title.textSize =
                14f

            title.maxLines =
                1

            title.ellipsize =
                android.text.TextUtils.TruncateAt.END

            title.layoutParams =
                android.widget.LinearLayout.LayoutParams(
                    0,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )

            val closeButton =
                android.widget.TextView(this)

            closeButton.text =
                "✕"

            closeButton.gravity =
                android.view.Gravity.CENTER

            closeButton.setTextColor(
                android.graphics.Color.WHITE
            )

            closeButton.textSize =
                18f

            closeButton.setPadding(
                16,
                8,
                16,
                8
            )

            row.addView(
                title
            )

            row.addView(
                closeButton
            )

            // Open this tab
            title.setOnClickListener {

                activeTabId =
                    tab.id

                homePage.visibility =
                    View.GONE

                settingsPage.visibility =
                    View.GONE

                webView.visibility =
                    View.VISIBLE

                addressBar.visibility =
                    View.GONE

                findViewById<View>(
                    R.id.goButton
                ).visibility =
                    View.GONE

                webView.loadUrl(
                    tab.url
                )

                dialog.dismiss()
            }

            // Close this tab
            closeButton.setOnClickListener {

                val wasActive =
                    tab.id == activeTabId

                openTabs.remove(
                    tab
                )

                if (openTabs.isEmpty()) {

                    activeTabId =
                        0

                    updateTabsCount()

                    dialog.dismiss()

                    showHomePage()

                    return@setOnClickListener
                }

                if (wasActive) {

                    val newActiveTab =
                        openTabs.last()

                    activeTabId =
                        newActiveTab.id

                    webView.loadUrl(
                        newActiveTab.url
                    )
                }

                updateTabsCount()

                refreshTabList()
            }

            container.addView(
                row
            )
        }
    }

    refreshTabList()

    dialog.show()
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
// NOTIFICATIONS
// =========================================================

private val notificationChannelId =
    "deeprows_browser_notifications"

private fun createNotificationChannel() {

    if (
        android.os.Build.VERSION.SDK_INT >=
        android.os.Build.VERSION_CODES.O
    ) {

        val channel =
            NotificationChannel(
                notificationChannelId,
                "Deeprows Browser",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {

                description =
                    "Notifications from Deeprows Browser"
            }

        val notificationManager =
            getSystemService(
                NotificationManager::class.java
            )

        notificationManager.createNotificationChannel(
            channel
        )
    }
}

private fun requestNotificationPermission() {

    if (
        android.os.Build.VERSION.SDK_INT >=
        android.os.Build.VERSION_CODES.TIRAMISU
    ) {

        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS
                ),
                1001
            )
        }
    }
}

private fun showNotification(
    title: String,
    message: String
) {

    if (
        android.os.Build.VERSION.SDK_INT >=
        android.os.Build.VERSION_CODES.TIRAMISU
    ) {

        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
    }

    val notification =
        NotificationCompat.Builder(
            this,
            notificationChannelId
        )
            .setSmallIcon(
                R.drawable.deeprows_logo
            )
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(
                NotificationCompat.PRIORITY_DEFAULT
            )
            .setAutoCancel(true)
            .build()

    NotificationManagerCompat
        .from(this)
        .notify(
            System.currentTimeMillis().toInt(),
            notification
        )
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
        R.id.downloadsButton
    ).setOnClickListener {

        try {

            val intent =
                android.content.Intent(
                    android.content.Intent.ACTION_VIEW
                )

            intent.data =
                android.net.Uri.parse(
                    "content://downloads/my_downloads"
                )

            startActivity(intent)

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Unable to open Downloads",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    findViewById<View>(
        R.id.shareButton
    ).setOnClickListener {

        val currentUrl =
            webView.url

        if (
            currentUrl.isNullOrBlank()
        ) {

            Toast.makeText(
                this,
                "No webpage to share",
                Toast.LENGTH_SHORT
            ).show()

            return@setOnClickListener
        }

        val shareIntent =
            android.content.Intent(
                android.content.Intent.ACTION_SEND
            ).apply {

                type = "text/plain"

                putExtra(
                    android.content.Intent.EXTRA_TEXT,
                    currentUrl
                )

                putExtra(
                    android.content.Intent.EXTRA_SUBJECT,
                    webView.title
                        ?: "Deeprows Browser"
                )
            }

        startActivity(
            android.content.Intent.createChooser(
                shareIntent,
                "Share page"
            )
        )
    }

    findViewById<View>(
        R.id.translateButton
    ).setOnClickListener {

        val currentUrl =
            webView.url

        if (
            currentUrl.isNullOrBlank()
        ) {

            Toast.makeText(
                this,
                "No webpage to translate",
                Toast.LENGTH_SHORT
            ).show()

            return@setOnClickListener
        }

        val translateUrl =
            "https://translate.google.com/translate" +
                    "?sl=auto&tl=en&u=" +
                    android.net.Uri.encode(
                        currentUrl
                    )

        openWebsite(
            translateUrl
        )
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

  // =========================================================
// OPEN WEBSITE
// =========================================================

private fun openWebsite(
    url: String
) {

    settingsPage.visibility =
        View.GONE

    homePage.visibility =
        View.GONE

    webView.visibility =
        View.VISIBLE

    addressBar.visibility =
        View.GONE

    findViewById<View>(
        R.id.goButton
    ).visibility =
        View.GONE

    // Create a new tab
    val newTab =
        BrowserTab(
            id = nextTabId++,
            title = "New Tab",
            url = url
        )

    openTabs.add(
        newTab
    )

    activeTabId =
        newTab.id

    updateTabsCount()

    webView.loadUrl(
        url
    )
}  

// =========================================================
// UPDATE TABS COUNT
// =========================================================

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

private fun loadGoogleTrends() {

    val trendsList =
        findViewById<android.widget.LinearLayout>(
            R.id.trendsList
        )

    trendsList.removeAllViews()

    kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.Dispatchers.Main
    ).launch {

        val trends =
            newsRepository.getGoogleTrends(10)

        if (trends.isEmpty()) {

            val emptyText =
                android.widget.TextView(this@MainActivity)

            emptyText.text =
                "Unable to load Google Trends"

            emptyText.setTextColor(
                android.graphics.Color.LTGRAY
            )

            emptyText.textSize =
                13f

            emptyText.setPadding(
                8,
                12,
                8,
                12
            )

            trendsList.addView(
                emptyText
            )

            return@launch
        }

        trends.forEachIndexed { index, trend ->

            val trendRow =
                android.widget.LinearLayout(
                    this@MainActivity
                )

            trendRow.orientation =
                android.widget.LinearLayout.HORIZONTAL

            trendRow.gravity =
                android.view.Gravity.CENTER_VERTICAL

            trendRow.setPadding(
                8,
                10,
                8,
                10
            )

            trendRow.setBackgroundColor(
                getThemeSurface2Color()
            )

            val number =
                android.widget.TextView(
                    this@MainActivity
                )

            number.text =
                "${index + 1}"

            number.setTextColor(
                getThemeAccentColor()
            )

            number.textSize =
                14f

            number.gravity =
                android.view.Gravity.CENTER

            val numberParams =
                android.widget.LinearLayout.LayoutParams(
                    32,
                    48
                )

            trendRow.addView(
                number,
                numberParams
            )

            val textContainer =
                android.widget.LinearLayout(
                    this@MainActivity
                )

            textContainer.orientation =
                android.widget.LinearLayout.VERTICAL

            textContainer.layoutParams =
                android.widget.LinearLayout.LayoutParams(
                    0,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )

            val title =
                android.widget.TextView(
                    this@MainActivity
                )

            title.text =
                trend.title

            title.setTextColor(
                android.graphics.Color.WHITE
            )

            title.textSize =
                14f

            title.maxLines =
                2

            title.ellipsize =
                android.text.TextUtils.TruncateAt.END

            textContainer.addView(
                title
            )

            val source =
                android.widget.TextView(
                    this@MainActivity
                )

            source.text =
                if (trend.source.isNotBlank()) {
                    trend.source
                } else {
                    "Google Trends"
                }

            source.setTextColor(
                getThemeAccentColor()
            )

            source.textSize =
                10f

            source.setPadding(
                0,
                4,
                0,
                0
            )

            textContainer.addView(
                source
            )

            trendRow.addView(
                textContainer
            )

            trendRow.setOnClickListener {

                if (
                    trend.link.isNotBlank()
                ) {

                    openWebsite(
                        trend.link
                    )
                }
            }

            val rowParams =
                android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )

            rowParams.setMargins(
                0,
                0,
                0,
                5
            )

            trendsList.addView(
                trendRow,
                rowParams
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
        getThemeSurfaceColor()
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
        getThemeSurface2Color()
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
            "News"
        }

    source.setTextColor(
        getThemeAccentColor()
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
    // =========================================================
// THEME SELECTOR
// =========================================================

private fun showThemeSelector() {

    val themes = arrayOf(
        "Midnight",
        "Deeprowss Red",
        "Purple Night",
        "Ocean",
        "Emerald",
        "Light",
        "AMOLED"
    )

    val currentTheme =
        preferences.getString(
            "app_theme",
            "Midnight"
        )

    var selectedIndex =
        themes.indexOf(currentTheme)

    if (selectedIndex < 0) {
        selectedIndex = 0
    }

    AlertDialog.Builder(this)
        .setTitle("Choose Theme")
        .setSingleChoiceItems(
            themes,
            selectedIndex
        ) { dialog, which ->

           preferences.edit()
    .putString(
        "app_theme",
        themes[which]
    )
    .apply()

applyAppTheme()

Toast.makeText(
                this,
                "${themes[which]} selected",
                Toast.LENGTH_SHORT
            ).show()

            dialog.dismiss()
        }
        .setNegativeButton(
            "Cancel",
            null
        )
        .show()
}
// =========================================================
// APPLY THEME
// =========================================================

private fun applyAppTheme() {

    val theme =
        preferences.getString(
            "app_theme",
            "Midnight"
        )

    val backgroundColor: Int
    val surfaceColor: Int
    val surface2Color: Int
    val textColor: Int
    val mutedColor: Int
    val accentColor: Int

    when (theme) {

        "Deeprowss Red" -> {

            backgroundColor =
                android.graphics.Color.parseColor("#18080D")

            surfaceColor =
                android.graphics.Color.parseColor("#35101B")

            surface2Color =
                android.graphics.Color.parseColor("#4A1423")

            textColor =
                android.graphics.Color.WHITE

            mutedColor =
                android.graphics.Color.parseColor("#D7A7B5")

            accentColor =
                android.graphics.Color.parseColor("#FF1744")
        }

        "Purple Night" -> {

            backgroundColor =
                android.graphics.Color.parseColor("#120B1C")

            surfaceColor =
                android.graphics.Color.parseColor("#27163D")

            surface2Color =
                android.graphics.Color.parseColor("#382052")

            textColor =
                android.graphics.Color.WHITE

            mutedColor =
                android.graphics.Color.parseColor("#C9B9D9")

            accentColor =
                android.graphics.Color.parseColor("#B45CFF")
        }

        "Ocean" -> {

            backgroundColor =
                android.graphics.Color.parseColor("#06141C")

            surfaceColor =
                android.graphics.Color.parseColor("#0D2A3A")

            surface2Color =
                android.graphics.Color.parseColor("#123B50")

            textColor =
                android.graphics.Color.WHITE

            mutedColor =
                android.graphics.Color.parseColor("#A9C8D6")

            accentColor =
                android.graphics.Color.parseColor("#00B8D4")
        }

        "Emerald" -> {

            backgroundColor =
                android.graphics.Color.parseColor("#071710")

            surfaceColor =
                android.graphics.Color.parseColor("#103022")

            surface2Color =
                android.graphics.Color.parseColor("#174631")

            textColor =
                android.graphics.Color.WHITE

            mutedColor =
                android.graphics.Color.parseColor("#A9CDBA")

            accentColor =
                android.graphics.Color.parseColor("#00D084")
        }

        "Light" -> {

            backgroundColor =
                android.graphics.Color.parseColor("#F4F6F8")

            surfaceColor =
                android.graphics.Color.WHITE

            surface2Color =
                android.graphics.Color.parseColor("#E8EDF2")

            textColor =
                android.graphics.Color.parseColor("#111827")

            mutedColor =
                android.graphics.Color.parseColor("#667085")

            accentColor =
                android.graphics.Color.parseColor("#E91E4D")
        }

        "AMOLED" -> {

            backgroundColor =
                android.graphics.Color.BLACK

            surfaceColor =
                android.graphics.Color.parseColor("#080808")

            surface2Color =
                android.graphics.Color.parseColor("#111111")

            textColor =
                android.graphics.Color.WHITE

            mutedColor =
                android.graphics.Color.parseColor("#999999")

            accentColor =
                android.graphics.Color.parseColor("#FF1744")
        }

        else -> {

            backgroundColor =
                android.graphics.Color.parseColor("#111B2D")

            surfaceColor =
                android.graphics.Color.parseColor("#182437")

            surface2Color =
                android.graphics.Color.parseColor("#22314A")

            textColor =
                android.graphics.Color.WHITE

            mutedColor =
                android.graphics.Color.parseColor("#9AA9BE")

            accentColor =
                android.graphics.Color.parseColor("#FF1744")
        }
    }

    // =====================================================
    // MAIN BACKGROUNDS
    // =====================================================

    findViewById<View>(
        android.R.id.content
    ).setBackgroundColor(
        backgroundColor
    )

    homePage.setBackgroundColor(
        backgroundColor
    )

    settingsPage.setBackgroundColor(
        backgroundColor
    )

    webView.setBackgroundColor(
        backgroundColor
    )

    // =====================================================
    // SEARCH BAR
    // =====================================================

    findViewById<View>(
        R.id.addressBar
    ).setBackgroundColor(
        surface2Color
    )

    // =====================================================
    // SETTINGS THEME BUTTON
    // =====================================================

    findViewById<View>(
        R.id.themeButton
    ).setBackgroundColor(
        surfaceColor
    )


    // =====================================================
    // TEXT COLORS
    // =====================================================

    val rootView =
        findViewById<android.view.ViewGroup>(
            R.id.homePage
        )

    applyTextColors(
        rootView,
        textColor
    )

    val settingsRoot =
        findViewById<android.view.ViewGroup>(
            R.id.settingsPage
        )

    applyTextColors(
        settingsRoot,
        textColor
    )
    applyCardTheme(
    rootView,
    surfaceColor,
    surface2Color,
    textColor,
    accentColor
)

applyCardTheme(
    settingsRoot,
    surfaceColor,
    surface2Color,
    textColor,
    accentColor
)

    // =====================================================
    // THEME ACCENT
    // =====================================================

    findViewById<View>(
        R.id.goButton
    ).setBackgroundColor(
        accentColor
    )

    findViewById<View>(
        R.id.themeButton
    ).setBackgroundColor(
        surfaceColor
    )
}
private fun applyTextColors(
    parent: android.view.ViewGroup,
    color: Int
) {

    for (
        index in 0 until parent.childCount
    ) {

        val child =
            parent.getChildAt(index)

        when (child) {

            is android.widget.TextView -> {

                child.setTextColor(
                    color
                )
            }

            is android.view.ViewGroup -> {

                applyTextColors(
                    child,
                    color
                )
            }
        }
    }
}

private fun getThemeSurfaceColor(): Int {

    return when (
        preferences.getString(
            "app_theme",
            "Midnight"
        )
    ) {

        "Deeprowss Red" ->
            android.graphics.Color.parseColor(
                "#35101B"
            )

        "Purple Night" ->
            android.graphics.Color.parseColor(
                "#27163D"
            )

        "Ocean" ->
            android.graphics.Color.parseColor(
                "#0D2A3A"
            )

        "Emerald" ->
            android.graphics.Color.parseColor(
                "#103022"
            )

        "Light" ->
            android.graphics.Color.WHITE

        "AMOLED" ->
            android.graphics.Color.parseColor(
                "#080808"
            )

        else ->
            android.graphics.Color.parseColor(
                "#182437"
            )
    }
}

private fun getThemeSurface2Color(): Int {

    return when (
        preferences.getString(
            "app_theme",
            "Midnight"
        )
    ) {

        "Deeprowss Red" ->
            android.graphics.Color.parseColor(
                "#4A1423"
            )

        "Purple Night" ->
            android.graphics.Color.parseColor(
                "#382052"
            )

        "Ocean" ->
            android.graphics.Color.parseColor(
                "#123B50"
            )

        "Emerald" ->
            android.graphics.Color.parseColor(
                "#174631"
            )

        "Light" ->
            android.graphics.Color.parseColor(
                "#E8EDF2"
            )

        "AMOLED" ->
            android.graphics.Color.parseColor(
                "#111111"
            )

        else ->
            android.graphics.Color.parseColor(
                "#22314A"
            )
    }
}

private fun getThemeAccentColor(): Int {

    return when (
        preferences.getString(
            "app_theme",
            "Midnight"
        )
    ) {

        "Purple Night" ->
            android.graphics.Color.parseColor(
                "#B45CFF"
            )

        "Ocean" ->
            android.graphics.Color.parseColor(
                "#00B8D4"
            )

        "Emerald" ->
            android.graphics.Color.parseColor(
                "#00D084"
            )

        else ->
            android.graphics.Color.parseColor(
                "#FF1744"
            )
    }
}
private fun applyCardTheme(
    parent: android.view.ViewGroup,
    surfaceColor: Int,
    surface2Color: Int,
    textColor: Int,
    accentColor: Int
) {

    for (index in 0 until parent.childCount) {

        val child =
            parent.getChildAt(index)

        /*
         * Website cards
         *
         * IDs beginning with "site" are the
         * homepage website cards.
         */
        if (child.id != View.NO_ID) {

            val resourceName =
                try {
                    resources.getResourceEntryName(
                        child.id
                    )
                } catch (e: Exception) {
                    ""
                }

            if (
                resourceName.startsWith(
                    "site",
                    ignoreCase = true
                )
            ) {

                val drawable =
                    android.graphics.drawable.GradientDrawable()

                drawable.shape =
                    android.graphics.drawable.GradientDrawable.RECTANGLE

                drawable.setColor(
                    surfaceColor
                )

                drawable.cornerRadius =
                    16f * resources.displayMetrics.density

                child.background =
                    drawable
            }
        }

        /*
         * Keep text colors consistent
         * with the selected theme.
         */
        when (child) {

            is android.widget.TextView -> {

                child.setTextColor(
                    textColor
                )
            }
        }

        /*
         * Continue through child layouts.
         */
        if (child is android.view.ViewGroup) {

            applyCardTheme(
                child,
                surfaceColor,
                surface2Color,
                textColor,
                accentColor
            )
        }
    }
}
}
