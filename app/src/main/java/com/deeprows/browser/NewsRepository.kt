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

    private val googleNewsUrl =
        "https://news.google.com/rss/search"

    suspend fun getLatestNews(
        limit: Int = 4
    ): List<NewsArticle> =
        withContext(Dispatchers.IO) {
            fetchGoogleNews(
                "latest news",
                limit
            )
        }

    suspend fun getSportNews(
        limit: Int = 4
    ): List<NewsArticle> =
        withContext(Dispatchers.IO) {
            fetchGoogleNews(
                "football",
                limit
            )
        }

    private fun fetchGoogleNews(
        query: String,
        limit: Int
    ): List<NewsArticle> {

        val articles = mutableListOf<NewsArticle>()

        try {

            val encodedQuery =
                java.net.URLEncoder.encode(
                    query,
                    "UTF-8"
                )

            val urlString =
                "$googleNewsUrl?q=$encodedQuery" +
                        "&hl=en-US" +
                        "&gl=US" +
                        "&ceid=US:en"

            val connection =
                URL(urlString).openConnection()
                        as HttpURLConnection

            connection.requestMethod = "GET"

            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Android) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36"
            )

            connection.setRequestProperty(
                "Accept",
                "application/rss+xml, application/xml, text/xml"
            )

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                connection.disconnect()
                return emptyList()
            }

            connection.inputStream.use { inputStream ->

                val parser =
                    Xml.newPullParser()

                parser.setInput(
                    inputStream,
                    "UTF-8"
                )

                var eventType =
                    parser.eventType

                var insideItem = false

                var title = ""
                var link = ""
                var source = ""
                var pubDate = ""
                var description = ""

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

                            if (tag == "item") {

                                insideItem = true

                                title = ""
                                link = ""
                                source = ""
                                pubDate = ""
                                description = ""

                                imageCandidates.clear()
                            }

                            if (insideItem) {

                                when (tag) {

                                    "title" -> {
                                        if (title.isEmpty()) {
                                            title =
                                                parser.nextText()
                                                    .trim()
                                        }
                                    }

                                    "link" -> {
                                        if (link.isEmpty()) {
                                            link =
                                                parser.nextText()
                                                    .trim()
                                        }
                                    }

                                    "source" -> {
                                        if (source.isEmpty()) {
                                            source =
                                                parser.nextText()
                                                    .trim()
                                    }
                                    }

                                    "pubdate" -> {
                                        if (pubDate.isEmpty()) {
                                            pubDate =
                                                parser.nextText()
                                                    .trim()
                                        }
                                    }

                                    "description" -> {
                                        if (description.isEmpty()) {
                                            description =
                                                parser.nextText()
                                                    .trim()
                                        }
                                    }

                                    "media:content",
                                    "media:thumbnail",
                                    "thumbnail",
                                    "enclosure" -> {

                                        val image =
                                            parser.getAttributeValue(
                                                null,
                                                "url"
                                            )

                                        if (
                                            !image.isNullOrBlank()
                                        ) {
                                            imageCandidates.add(
                                                image
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        XmlPullParser.END_TAG -> {

                            if (
                                parser.name
                                    ?.equals(
                                        "item",
                                        ignoreCase = true
                                    ) == true
                            ) {

                                insideItem = false

                                val imageUrl =
                                    findImageUrl(
                                        description,
                                        imageCandidates
                                    )

                                if (
                                    title.isNotBlank() &&
                                    link.isNotBlank()
                                ) {

                                    articles.add(
                                        NewsArticle(
                                            title =
                                                cleanText(title),

                                            link =
                                                link,

                                            source =
                                                cleanText(source),

                                            pubDate =
                                                cleanText(pubDate),

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
                                    articles.size >= limit
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

            connection.disconnect()

        } catch (
            e: Exception
        ) {

            e.printStackTrace()
        }

        return articles
    }

    private fun findImageUrl(
        description: String,
        candidates: List<String>
    ): String {

        // 1. Prefer media/enclosure thumbnail
        candidates.forEach { url ->

            if (
                url.startsWith("http://") ||
                url.startsWith("https://")
            ) {
                return url
            }
        }

        // 2. Look for normal HTML image
        val imageRegex =
            Regex(
                """<img[^>]+src=["']([^"']+)["']""",
                RegexOption.IGNORE_CASE
            )

        val imageMatch =
            imageRegex.find(description)

        if (
            imageMatch != null
        ) {

            val image =
                imageMatch.groupValues[1]

            if (
                image.startsWith("http://") ||
                image.startsWith("https://")
            ) {
                return image
            }
        }

        // 3. Look for lazy-loaded images
        val lazyRegex =
            Regex(
                """(?:data-src|data-original|data-lazy-src)=["']([^"']+)["']""",
                RegexOption.IGNORE_CASE
            )

        val lazyMatch =
            lazyRegex.find(description)

        if (
            lazyMatch != null
        ) {

            val image =
                lazyMatch.groupValues[1]

            if (
                image.startsWith("http://") ||
                image.startsWith("https://")
            ) {
                return image
            }
        }

        return ""
    }

    private fun cleanText(
        text: String
    ): String {

        return text
            .replace(
                Regex("<[^>]*>"),
                ""
            )
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
            .replace(
                Regex("\\s+"),
                " "
            )
            .trim()
    }
}
```

After replacing it, **build and test the app first**. If the cards still show the placeholder instead of images, the next thing to fix is `addNewsCard()` in `MainActivity.kt`, because the repository may be receiving the image URL correctly but the `ImageView` may not be displaying it.
