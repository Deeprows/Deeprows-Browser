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

    private val baseUrl =
        "https://news.google.com/rss/search"

    suspend fun getLatestNews(
        limit: Int = 4
    ): List<NewsArticle> =
        withContext(Dispatchers.IO) {

            fetchGoogleNews(
                query = "latest news",
                limit = limit
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

        val articles =
            mutableListOf<NewsArticle>()

        var connection:
                HttpURLConnection? = null

        try {

            val encodedQuery =
                java.net.URLEncoder.encode(
                    query,
                    "UTF-8"
                )

            val url =
                "$baseUrl?q=$encodedQuery&hl=en-US&gl=US&ceid=US:en"

            connection =
                URL(url)
                    .openConnection() as HttpURLConnection

            connection.requestMethod = "GET"

            connection.connectTimeout = 15000

            connection.readTimeout = 15000

            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0"
            )

            connection.setRequestProperty(
                "Accept",
                "application/rss+xml, application/xml, text/xml"
            )

            val responseCode =
                connection.responseCode

            if (
                responseCode !in 200..299
            ) {
                return emptyList()
            }

            val inputStream =
                connection.inputStream

            val parser =
                Xml.newPullParser()

            parser.setInput(
                inputStream,
                null
            )

            var eventType =
                parser.eventType

            var insideItem = false

            var title = ""

            var link = ""

            var source = ""

            var pubDate = ""

            var description = ""

            var imageUrl = ""

            while (
                eventType !=
                XmlPullParser.END_DOCUMENT &&
                articles.size < limit
            ) {

                when (eventType) {

                    XmlPullParser.START_TAG -> {

                        when (
                            parser.name
                                .lowercase()
                        ) {

                            "item" -> {

                                insideItem = true

                                title = ""
                                link = ""
                                source = ""
                                pubDate = ""
                                description = ""
                                imageUrl = ""
                            }

                            "title" -> {

                                if (insideItem) {

                                    title =
                                        parser.nextText()
                                            .trim()
                                }
                            }

                            "link" -> {

                                if (insideItem) {

                                    link =
                                        parser.nextText()
                                            .trim()
                                }
                            }

                            "source" -> {

                                if (insideItem) {

                                    source =
                                        parser.nextText()
                                            .trim()
                                }
                            }

                            "pubdate" -> {

                                if (insideItem) {

                                    pubDate =
                                        parser.nextText()
                                            .trim()
                                }
                            }

                            "description" -> {

                                if (insideItem) {

                                    description =
                                        parser.nextText()
                                            .trim()

                                    if (
                                        imageUrl.isBlank()
                                    ) {

                                        imageUrl =
                                            extractImageFromHtml(
                                                description
                                            )
                                    }
                                }
                            }

                            "thumbnail" -> {

                                if (insideItem) {

                                    imageUrl =
                                        parser
                                            .getAttributeValue(
                                                null,
                                                "url"
                                            )
                                            ?: imageUrl
                                }
                            }

                            "content" -> {

                                if (insideItem) {

                                    val mediaUrl =
                                        parser
                                            .getAttributeValue(
                                                null,
                                                "url"
                                            )

                                    if (
                                        !mediaUrl.isNullOrBlank()
                                    ) {

                                        imageUrl =
                                            mediaUrl
                                    }
                                }
                            }

                            "enclosure" -> {

                                if (insideItem) {

                                    val enclosureUrl =
                                        parser
                                            .getAttributeValue(
                                                null,
                                                "url"
                                            )

                                    if (
                                        !enclosureUrl.isNullOrBlank()
                                    ) {

                                        imageUrl =
                                            enclosureUrl
                                    }
                                }
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {

                        if (
                            parser.name.equals(
                                "item",
                                ignoreCase = true
                            )
                        ) {

                            insideItem = false

                            if (
                                title.isNotBlank() &&
                                link.isNotBlank()
                            ) {

                                articles.add(
                                    NewsArticle(
                                        title =
                                            cleanText(
                                                title
                                            ),

                                        link =
                                            link,

                                        source =
                                            cleanText(
                                                source
                                            ),

                                        pubDate =
                                            pubDate,

                                        description =
                                            cleanText(
                                                description
                                            ),

                                        imageUrl =
                                            imageUrl
                                    )
                                )
                            }
                        }
                    }
                }

                eventType =
                    parser.next()
            }

            inputStream.close()

        } catch (
            exception: Exception
        ) {

            exception.printStackTrace()

            return emptyList()

        } finally {

            connection?.disconnect()
        }

        return articles
    }

    private fun extractImageFromHtml(
        html: String
    ): String {

        if (html.isBlank()) {
            return ""
        }

        val patterns =
            listOf(

                Regex(
                    """<img[^>]+src=["']([^"']+)["']""",
                    RegexOption.IGNORE_CASE
                ),

                Regex(
                    """<img[^>]+src=([^ >]+)""",
                    RegexOption.IGNORE_CASE
                ),

                Regex(
                    """<img[^>]+data-src=["']([^"']+)["']""",
                    RegexOption.IGNORE_CASE
                )
            )

        for (pattern in patterns) {

            val match =
                pattern.find(html)

            if (match != null) {

                return match
                    .groupValues[1]
                    .replace(
                        "&amp;",
                        "&"
                    )
                    .trim()
            }
        }

        return ""
    }

    private fun cleanText(
        text: String
    ): String {

        return android.text.Html
            .fromHtml(
                text,
                android.text.Html.FROM_HTML_MODE_LEGACY
            )
            .toString()
            .trim()
    }
}
