package com.deeprows.browser

import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

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

        val articles =
            mutableListOf<NewsArticle>()

        var connection:
                HttpURLConnection? = null

        try {

            val encodedQuery =
                URLEncoder.encode(
                    query,
                    "UTF-8"
                )

            val urlString =
                "$googleNewsUrl" +
                        "?q=$encodedQuery" +
                        "&hl=en-US" +
                        "&gl=US" +
                        "&ceid=US:en"

            connection =
                URL(urlString)
                    .openConnection()
                        as HttpURLConnection

            connection.requestMethod = "GET"

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

                            if (
                                tag == "item"
                            ) {

                                insideItem = true

                                title = ""
                                link = ""
                                source = ""
                                pubDate = ""
                                description = ""

                                imageCandidates.clear()
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
                                                parser.nextText()
                                                    .trim()
                                        }
                                    }

                                    "link" -> {

                                        if (
                                            link.isEmpty()
                                        ) {

                                            link =
                                                parser.nextText()
                                                    .trim()
                                        }
                                    }

                                    "source" -> {

                                        if (
                                            source.isEmpty()
                                        ) {

                                            source =
                                                parser.nextText()
                                                    .trim()
                                        }
                                    }

                                    "pubdate" -> {

                                        if (
                                            pubDate.isEmpty()
                                        ) {

                                            pubDate =
                                                parser.nextText()
                                                    .trim()
                                        }
                                    }

                                    "description" -> {

                                        if (
                                            description.isEmpty()
                                        ) {

                                            description =
                                                parser.nextText()
                                                    .trim()
                                        }
                                    }

                                    "media:content",
                                    "media:thumbnail",
                                    "content",
                                    "thumbnail",
                                    "enclosure" -> {

                                        val url =
                                            getImageUrl(
                                                parser
                                            )

                                        if (
                                            !url.isNullOrBlank()
                                        ) {

                                            imageCandidates.add(
                                                url
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

                                insideItem = false

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
                                                ),

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

    private fun getImageUrl(
        parser: XmlPullParser
    ): String? {

        val possibleAttributes =
            arrayOf(
                "url",
                "href",
                "src"
            )

        for (
            attribute in possibleAttributes
        ) {

            val value =
                parser.getAttributeValue(
                    null,
                    attribute
                )

            if (
                !value.isNullOrBlank() &&
                (
                    value.startsWith("http://") ||
                    value.startsWith("https://")
                )
            ) {

                return value
            }
        }

        return null
    }

    private fun findImageUrl(
        description: String,
        candidates: List<String>
    ): String {

        for (
            candidate in candidates
        ) {

            if (
                isValidUrl(candidate)
            ) {

                return decodeEntities(
                    candidate
                )
            }
        }

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
                isValidUrl(image)
            ) {

                return image
            }
        }

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
                isValidUrl(image)
            ) {

                return image
            }
        }

        return ""
    }

    private fun isValidUrl(
        url: String
    ): Boolean {

        return (
            url.startsWith("http://") ||
            url.startsWith("https://")
        )
    }

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

    private fun cleanText(
        text: String
    ): String {

        return decodeEntities(
            text
                .replace(
                    Regex("<[^>]*>"),
                    ""
                )
                .replace(
                    Regex("\\s+"),
                    " "
                )
                .trim()
        )
    }
}
