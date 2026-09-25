package com.deeprows.browser

import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.net.HttpURLConnection
import java.net.URL

data class NewsArticle(
    val title: String,
    val link: String,
    val source: String,
    val pubDate: String,
    val description: String,
    val imageUrl: String
)

class NewsRepository {

    // =========================================================
    // NEWS SOURCES
    // =========================================================

    private val latestNewsUrl =
        "https://feeds.bbci.co.uk/news/rss.xml"

    private val sportNewsUrl =
        "https://feeds.bbci.co.uk/sport/rss.xml"

    // Current Google Trends RSS endpoint
    private val googleTrendsUrl =
        "https://trends.google.com/trending/rss?geo=EG"

    // =========================================================
    // LATEST NEWS
    // =========================================================

    suspend fun getLatestNews(
        limit: Int = 4
    ): List<NewsArticle> =
        withContext(Dispatchers.IO) {

            fetchRssFeed(
                latestNewsUrl,
                limit
            )
        }

    // =========================================================
    // SPORT NEWS
    // =========================================================

    suspend fun getSportNews(
        limit: Int = 4
    ): List<NewsArticle> =
        withContext(Dispatchers.IO) {

            fetchRssFeed(
                sportNewsUrl,
                limit
            )
        }

    // =========================================================
    // GOOGLE TRENDS
    // =========================================================

    suspend fun getGoogleTrends(
        limit: Int = 10
    ): List<NewsArticle> =
        withContext(Dispatchers.IO) {

            fetchGoogleTrends(
                limit
            )
        }

    // =========================================================
    // GENERIC RSS READER
    // =========================================================

    private fun fetchRssFeed(
        feedUrl: String,
        limit: Int
    ): List<NewsArticle> {

        val articles =
            mutableListOf<NewsArticle>()

        var connection:
                HttpURLConnection? = null

        try {

            connection =
                URL(feedUrl)
                    .openConnection()
                        as HttpURLConnection

            connection.requestMethod =
                "GET"

            connection.connectTimeout =
                20000

            connection.readTimeout =
                20000

            connection.instanceFollowRedirects =
                true

            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 15; Mobile) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) " +
                        "Chrome/153.0.0.0 Mobile Safari/537.36"
            )

            connection.setRequestProperty(
                "Accept",
                "application/rss+xml, application/xml, text/xml, */*"
            )

            connection.setRequestProperty(
                "Cache-Control",
                "no-cache"
            )

            if (
                connection.responseCode !=
                HttpURLConnection.HTTP_OK
            ) {

                return emptyList()
            }

            connection.inputStream.use { inputStream ->

                val parser =
                    Xml.newPullParser()

                parser.setFeature(
                    XmlPullParser.FEATURE_PROCESS_NAMESPACES,
                    false
                )

                parser.setInput(
                    inputStream,
                    "UTF-8"
                )

                var eventType =
                    parser.eventType

                var insideItem =
                    false

                var title =
                    ""

                var link =
                    ""

                var source =
                    ""

                var pubDate =
                    ""

                var description =
                    ""

                val imageCandidates =
                    mutableListOf<String>()

                while (
                    eventType !=
                    XmlPullParser.END_DOCUMENT
                ) {

                    when (eventType) {

                        XmlPullParser.START_TAG -> {

                            val tag =
                                parser.name
                                    ?.lowercase()
                                    ?: ""

                            // -------------------------------------------------
                            // ITEM START
                            // -------------------------------------------------

                            if (
                                tag == "item"
                            ) {

                                insideItem =
                                    true

                                title =
                                    ""

                                link =
                                    ""

                                source =
                                    ""

                                pubDate =
                                    ""

                                description =
                                    ""

                                imageCandidates.clear()
                            }

                            if (
                                insideItem
                            ) {

                                when (tag) {

                                    // -----------------------------------------
                                    // TITLE
                                    // -----------------------------------------

                                    "title" -> {

                                        if (
                                            title.isEmpty()
                                        ) {

                                            title =
                                                safeNextText(
                                                    parser
                                                )
                                        }
                                    }

                                    // -----------------------------------------
                                    // LINK
                                    // -----------------------------------------

                                    "link" -> {

                                        if (
                                            link.isEmpty()
                                        ) {

                                            link =
                                                safeNextText(
                                                    parser
                                                )
                                        }
                                    }

                                    // -----------------------------------------
                                    // SOURCE
                                    // -----------------------------------------

                                    "source" -> {

                                        if (
                                            source.isEmpty()
                                        ) {

                                            source =
                                                safeNextText(
                                                    parser
                                                )
                                        }
                                    }

                                    // -----------------------------------------
                                    // DATE
                                    // -----------------------------------------

                                    "pubdate",
                                    "published",
                                    "updated" -> {

                                        if (
                                            pubDate.isEmpty()
                                        ) {

                                            pubDate =
                                                safeNextText(
                                                    parser
                                                )
                                        }
                                    }

                                    // -----------------------------------------
                                    // DESCRIPTION
                                    // -----------------------------------------

                                    "description",
                                    "content:encoded" -> {

                                        if (
                                            description.isEmpty()
                                        ) {

                                            description =
                                                safeNextText(
                                                    parser
                                                )
                                        }
                                    }

                                    // -----------------------------------------
                                    // IMAGES
                                    // -----------------------------------------

                                    "media:content",
                                    "media:thumbnail",
                                    "enclosure",
                                    "thumbnail",
                                    "content" -> {

                                        val imageUrl =
                                            getImageUrl(
                                                parser
                                            )

                                        if (
                                            !imageUrl.isNullOrBlank()
                                        ) {

                                            imageCandidates.add(
                                                imageUrl
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        XmlPullParser.END_TAG -> {

                            val tag =
                                parser.name
                                    ?.lowercase()
                                    ?: ""

                            if (
                                tag == "item"
                            ) {

                                insideItem =
                                    false

                                if (
                                    title.isNotBlank() &&
                                    link.isNotBlank()
                                ) {

                                    val imageUrl =
                                        findImageUrl(
                                            description,
                                            imageCandidates
                                        )

                                    articles.add(
                                        NewsArticle(
                                            title =
                                                cleanText(
                                                    title
                                                ),

                                            link =
                                                link.trim(),

                                            source =
                                                cleanText(
                                                    source
                                                ).ifBlank {
                                                    detectSource(
                                                        feedUrl
                                                    )
                                                },

                                            pubDate =
                                                cleanText(
                                                    pubDate
                                                ),

                                            description =
                                                cleanText(
                                                    description
                                                ),

                                            imageUrl =
                                                imageUrl
                                        )
                                    )
                                }

                                if (
                                    articles.size >=
                                    limit
                                ) {

                                    break
                                }
                            }
                        }
                    }

                    eventType =
                        parser.next()
                }
            }

        } catch (
            e: Exception
        ) {

            e.printStackTrace()

        } finally {

            connection?.disconnect()
        }

        return articles
    }

    // =========================================================
    // GOOGLE TRENDS RSS
    // =========================================================

    private fun fetchGoogleTrends(
        limit: Int
    ): List<NewsArticle> {

        val articles =
            mutableListOf<NewsArticle>()

        var connection:
                HttpURLConnection? = null

        try {

            connection =
                URL(googleTrendsUrl)
                    .openConnection()
                        as HttpURLConnection

            connection.requestMethod =
                "GET"

            connection.connectTimeout =
                20000

            connection.readTimeout =
                20000

            connection.instanceFollowRedirects =
                true

            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 15; Mobile) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) " +
                        "Chrome/153.0.0.0 Mobile Safari/537.36"
            )

            connection.setRequestProperty(
                "Accept",
                "application/rss+xml, application/xml, text/xml, */*"
            )

            connection.setRequestProperty(
                "Cache-Control",
                "no-cache"
            )

            if (
                connection.responseCode !=
                HttpURLConnection.HTTP_OK
            ) {

                return emptyList()
            }

            connection.inputStream.use { inputStream ->

                val parser =
                    Xml.newPullParser()

                parser.setFeature(
                    XmlPullParser.FEATURE_PROCESS_NAMESPACES,
                    false
                )

                parser.setInput(
                    inputStream,
                    "UTF-8"
                )

                var eventType =
                    parser.eventType

                var insideItem =
                    false

                var title =
                    ""

                var link =
                    ""

                var description =
                    ""

                var pubDate =
                    ""

                var imageUrl =
                    ""

                var source =
                    "Google Trends"

                while (
                    eventType !=
                    XmlPullParser.END_DOCUMENT
                ) {

                    when (eventType) {

                        XmlPullParser.START_TAG -> {

                            val tag =
                                parser.name
                                    ?.lowercase()
                                    ?: ""

                            if (
                                tag == "item"
                            ) {

                                insideItem =
                                    true

                                title =
                                    ""

                                link =
                                    ""

                                description =
                                    ""

                                pubDate =
                                    ""

                                imageUrl =
                                    ""
                            }

                            if (
                                insideItem
                            ) {

                                when (tag) {

                                    "title" -> {

                                        if (
                                            title.isEmpty()
                                        ) {

                                            title =
                                                safeNextText(
                                                    parser
                                                )
                                        }
                                    }

                                    "link" -> {

                                        if (
                                            link.isEmpty()
                                        ) {

                                            link =
                                                safeNextText(
                                                    parser
                                                )
                                        }
                                    }

                                    "description" -> {

                                        if (
                                            description.isEmpty()
                                        ) {

                                            description =
                                                safeNextText(
                                                    parser
                                                )
                                        }
                                    }

                                    "pubdate" -> {

                                        if (
                                            pubDate.isEmpty()
                                        ) {

                                            pubDate =
                                                safeNextText(
                                                    parser
                                                )
                                        }
                                    }

                                    // Google Trends image
                                    "ht:picture" -> {

                                        if (
                                            imageUrl.isEmpty()
                                        ) {

                                            imageUrl =
                                                getElementText(
                                                    parser
                                                )
                                        }
                                    }

                                    // Google Trends news image
                                    "ht:news_item_picture" -> {

                                        if (
                                            imageUrl.isEmpty()
                                        ) {

                                            imageUrl =
                                                getElementText(
                                                    parser
                                                )
                                        }
                                    }
                                }
                            }
                        }

                        XmlPullParser.END_TAG -> {

                            val tag =
                                parser.name
                                    ?.lowercase()
                                    ?: ""

                            if (
                                tag == "item"
                            ) {

                                insideItem =
                                    false

                                if (
                                    title.isNotBlank()
                                ) {

                                    articles.add(
                                        NewsArticle(
                                            title =
                                                cleanText(
                                                    title
                                                ),

                                            link =
                                                link.trim(),

                                            source =
                                                source,

                                            pubDate =
                                                cleanText(
                                                    pubDate
                                                ),

                                            description =
                                                cleanText(
                                                    description
                                                ),

                                            imageUrl =
                                                imageUrl.trim()
                                        )
                                    )
                                }

                                if (
                                    articles.size >=
                                    limit
                                ) {

                                    break
                                }
                            }
                        }
                    }

                    eventType =
                        parser.next()
                }
            }

        } catch (
            e: Exception
        ) {

            e.printStackTrace()

        } finally {

            connection?.disconnect()
        }

        return articles
    }

    // =========================================================
    // IMAGE URL
    // =========================================================

    private fun getImageUrl(
        parser: XmlPullParser
    ): String? {

        val attributes =
            arrayOf(
                "url",
                "href",
                "src"
            )

        for (
            attribute in attributes
        ) {

            val value =
                parser.getAttributeValue(
                    null,
                    attribute
                )

            if (
                !value.isNullOrBlank() &&
                (
                    value.startsWith(
                        "http://"
                    ) ||
                    value.startsWith(
                        "https://"
                    )
                )
            ) {

                return value
            }
        }

        return null
    }

    // =========================================================
    // FIND IMAGE
    // =========================================================

    private fun findImageUrl(
        description: String,
        candidates: List<String>
    ): String {

        // First try RSS image fields.
        for (
            candidate in candidates
        ) {

            if (
                isValidUrl(
                    candidate
                )
            ) {

                return decodeEntities(
                    candidate
                )
            }
        }

        // Try HTML image inside description.
        val imageRegex =
            Regex(
                """<img[^>]+(?:src|data-src|data-original|data-lazy-src)=["']([^"']+)["']""",
                RegexOption.IGNORE_CASE
            )

        val imageMatch =
            imageRegex.find(
                description
            )

        if (
            imageMatch != null
        ) {

            val image =
                decodeEntities(
                    imageMatch
                        .groupValues[1]
                        .trim()
                )

            if (
                isValidUrl(
                    image
                )
            ) {

                return image
            }
        }

        // Try normal image URL.
        val urlRegex =
            Regex(
                """https?://[^\s"'<>]+?\.(?:jpg|jpeg|png|webp)(?:\?[^\s"'<>]*)?""",
                RegexOption.IGNORE_CASE
            )

        val urlMatch =
            urlRegex.find(
                description
            )

        if (
            urlMatch != null
        ) {

            val image =
                decodeEntities(
                    urlMatch.value
                )

            if (
                isValidUrl(
                    image
                )
            ) {

                return image
            }
        }

        return ""
    }

    // =========================================================
    // SAFE XML TEXT
    // =========================================================

    private fun safeNextText(
        parser: XmlPullParser
    ): String {

        return try {

            parser.nextText()
                .trim()

        } catch (
            e: Exception
        ) {

            ""
        }
    }

    private fun getElementText(
        parser: XmlPullParser
    ): String {

        return try {

            parser.nextText()
                .trim()

        } catch (
            e: Exception
        ) {

            ""
        }
    }

    // =========================================================
    // SOURCE
    // =========================================================

    private fun detectSource(
        url: String
    ): String {

        return when {

            url.contains(
                "bbci.co.uk"
            ) ->
                "BBC News"

            url.contains(
                "google.com"
            ) ->
                "Google Trends"

            else ->
                "News"
        }
    }

    // =========================================================
    // URL VALIDATION
    // =========================================================

    private fun isValidUrl(
        url: String
    ): Boolean {

        return (
            url.startsWith(
                "http://"
            ) ||
            url.startsWith(
                "https://"
            )
        )
    }

    // =========================================================
    // HTML ENTITIES
    // =========================================================

    private fun decodeEntities(
        text: String
    ): String {

        return text
            .replace(
                "&amp;",
                "&"
            )
            .replace(
                "&quot;",
                "\""
            )
            .replace(
                "&#39;",
                "'"
            )
            .replace(
                "&apos;",
                "'"
            )
            .replace(
                "&lt;",
                "<"
            )
            .replace(
                "&gt;",
                ">"
            )
            .trim()
    }

    // =========================================================
    // CLEAN TEXT
    // =========================================================

    private fun cleanText(
        text: String
    ): String {

        return decodeEntities(
            text
                .replace(
                    Regex(
                        "<[^>]*>"
                    ),
                    ""
                )
                .replace(
                    Regex(
                        "\\s+"
                    ),
                    " "
                )
                .trim()
        )
    }
}
