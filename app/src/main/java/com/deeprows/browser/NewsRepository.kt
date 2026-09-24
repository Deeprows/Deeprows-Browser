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
    val published: String
)

class NewsRepository {

    private val baseUrl =
        "https://news.google.com/rss/search"

    suspend fun getLatestNews(): List<NewsArticle> =
        withContext(Dispatchers.IO) {

            val query =
                URLEncoder.encode(
                    "latest news",
                    "UTF-8"
                )

            val urlString =
                "$baseUrl?q=$query" +
                "&hl=en-US" +
                "&gl=US" +
                "&ceid=US:en"

            var connection: HttpURLConnection? = null

            try {

                val connectionUrl =
                    URL(urlString)

                connection =
                    connectionUrl.openConnection()
                        as HttpURLConnection

                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.instanceFollowRedirects = true

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

                if (responseCode !in 200..299) {
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

                    parseFeed(parser)
                }

            } catch (e: Exception) {

                e.printStackTrace()

                emptyList()

            } finally {

                connection?.disconnect()
            }
        }

    private fun parseFeed(
        parser: XmlPullParser
    ): List<NewsArticle> {

        val articles =
            mutableListOf<NewsArticle>()

        var eventType =
            parser.eventType

        var insideItem = false

        var title = ""
        var link = ""
        var source = ""
        var published = ""

        while (
            eventType !=
            XmlPullParser.END_DOCUMENT &&
            articles.size < 10
        ) {

            when (eventType) {

                XmlPullParser.START_TAG -> {

                    val tag =
                        parser.name.lowercase()

                    when (tag) {

                        "item" -> {

                            insideItem = true

                            title = ""
                            link = ""
                            source = ""
                            published = ""
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

                                published =
                                    parser.nextText()
                                        .trim()
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
                                        cleanText(title),

                                    link =
                                        link,

                                    source =
                                        cleanText(source),

                                    published =
                                        published
                                )
                            )
                        }

                        insideItem = false
                    }
                }
            }

            eventType =
                parser.next()
        }

        return articles
    }

    private fun cleanText(
        value: String
    ): String {

        return value
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
