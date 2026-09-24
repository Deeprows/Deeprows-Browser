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
    val published: String,
    val imageUrl: String?
)

class NewsRepository {

    private val baseUrl =
        "https://news.google.com/rss/search"

    suspend fun getLatestNews(
        limit: Int = 4
    ): List<NewsArticle> =
        getFeed(
            query = "latest news",
            limit = limit
        )

    suspend fun getSportNews(
        limit: Int = 4
    ): List<NewsArticle> =
        getFeed(
            query = "football OR soccer OR sports",
            limit = limit
        )

    private suspend fun getFeed(
        query: String,
        limit: Int
    ): List<NewsArticle> =
        withContext(Dispatchers.IO) {

            val encodedQuery =
                URLEncoder.encode(
                    query,
                    "UTF-8"
                )

            val urlString =
                "$baseUrl" +
                        "?q=$encodedQuery" +
                        "&hl=en-US" +
                        "&gl=US" +
                        "&ceid=US:en"

            var connection:
                    HttpURLConnection? = null

            try {

                val url =
                    URL(urlString)

                connection =
                    url.openConnection()
                        as HttpURLConnection

                connection.requestMethod =
                    "GET"

                connection.connectTimeout =
                    15000

                connection.readTimeout =
                    15000

                connection.instanceFollowRedirects =
                    true

                connection.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0"
                )

                connection.setRequestProperty(
                    "Accept",
                    "application/rss+xml, application/xml, text/xml, */*"
                )

                val responseCode =
                    connection.responseCode

                if (
                    responseCode !in 200..299
                ) {
                    return@withContext emptyList()
                }

                connection.inputStream.use { input ->

                    val parser =
                        Xml.newPullParser()

                    parser.setFeature(
                        XmlPullParser.FEATURE_PROCESS_NAMESPACES,
                        false
                    )

                    parser.setInput(
                        input,
                        "UTF-8"
                    )

                    parseFeed(
                        parser,
                        limit
                    )
                }

            } catch (
                e: Exception
            ) {

                e.printStackTrace()

                emptyList()

            } finally {

                connection?.disconnect()
            }
        }

    private fun parseFeed(
        parser: XmlPullParser,
        limit: Int
    ): List<NewsArticle> {

        val articles =
            mutableListOf<NewsArticle>()

        var eventType =
            parser.eventType

        var insideItem =
            false

        var title = ""
        var link = ""
        var source = ""
        var published = ""
        var description = ""
        var imageUrl: String? = null

        while (
            eventType !=
            XmlPullParser.END_DOCUMENT &&
            articles.size < limit
        ) {

            when (eventType) {

                XmlPullParser.START_TAG -> {

                    val tag =
                        parser.name
                            .lowercase()

                    when {

                        tag == "item" -> {

                            insideItem =
                                true

                            title = ""
                            link = ""
                            source = ""
                            published = ""
                            description = ""
                            imageUrl = null
                        }

                        tag == "title" &&
                                insideItem -> {

                            title =
                                parser.nextText()
                                    .trim()
                        }

                        tag == "link" &&
                                insideItem -> {

                            link =
                                parser.nextText()
                                    .trim()
                        }

                        tag == "source" &&
                                insideItem -> {

                            source =
                                parser.nextText()
                                    .trim()
                        }

                        tag == "pubdate" &&
                                insideItem -> {

                            published =
                                parser.nextText()
                                    .trim()
                        }

                        tag == "description" &&
                                insideItem -> {

                            description =
                                parser.nextText()
                                    .trim()

                            if (
                                imageUrl.isNullOrBlank()
                            ) {

                                imageUrl =
                                    extractImageFromHtml(
                                        description
                                    )
                            }
                        }

                        (
                            tag == "media:content" ||
                            tag == "media:thumbnail" ||
                            tag == "enclosure"
                        ) && insideItem -> {

                            val mediaUrl =
                                parser.getAttributeValue(
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
                }

                XmlPullParser.END_TAG -> {

                    if (
                        parser.name.equals(
                            "item",
                            ignoreCase = true
                        )
                    ) {

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

                                    published =
                                        published,

                                    imageUrl =
                                        imageUrl
                                )
                            )
                        }

                        insideItem =
                            false
                    }
                }
            }

            eventType =
                parser.next()
        }

        return articles
    }

    private fun extractImageFromHtml(
        html: String
    ): String? {

        val patterns =
            listOf(
                Regex(
                    """<img[^>]+src=["']([^"']+)["']"""
                ),
                Regex(
                    """<img[^>]+src=([^ >]+)"""
                )
            )

        for (
            pattern in patterns
        ) {

            val match =
                pattern.find(
                    html
                )

            if (
                match != null
            ) {

                val image =
                    match.groupValues[1]
                        .replace(
                            "&amp;",
                            "&"
                        )
                        .trim()

                if (
                    image.startsWith(
                        "http://"
                    ) ||
                    image.startsWith(
                        "https://"
                    )
                ) {

                    return image
                }
            }
        }

        return null
    }

    private fun cleanText(
        value: String
    ): String {

        return value
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
                "\n",
                " "
            )
            .replace(
                "\r",
                " "
            )
            .replace(
                Regex("\\s+"),
                " "
            )
            .trim()
    }
}
