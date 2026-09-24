package com.deeprows.browser

import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

data class NewsArticle(
    val title: String,
    val link: String,
    val source: String,
    val published: String
)

class NewsRepository {

    private val baseUrl = "https://news.google.com/rss/search"

    /**
     * Get latest general news.
     *
     * gl=EG targets Egypt.
     * Change this later when we add the user's region selector.
     */
    suspend fun getLatestNews(): List<NewsArticle> =
        fetchNews(
            query = "latest news",
            country = "EG"
        )

    /**
     * Get trending/gist content.
     *
     * This currently searches for Nigeria/Africa/trending topics.
     * We can make this region-aware later.
     */
    suspend fun getGist(): List<NewsArticle> =
        fetchNews(
            query = "Nigeria Africa trending entertainment",
            country = "NG"
        )

    private suspend fun fetchNews(
        query: String,
        country: String
    ): List<NewsArticle> = withContext(Dispatchers.IO) {

        val encodedQuery = URLEncoder.encode(
            query,
            "UTF-8"
        )

        val urlString =
            "$baseUrl" +
            "?q=$encodedQuery" +
            "&hl=en" +
            "&gl=$country" +
            "&ceid=$country:en"

        var connection: HttpURLConnection? = null

        try {

            val url = URL(urlString)

            connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.setRequestProperty(
                "User-Agent",
                "DeeprowsBrowser/1.0"
            )

            connection.connect()

            if (connection.responseCode !in 200..299) {
                return@withContext emptyList()
            }

            connection.inputStream.use { inputStream ->

                val parser = Xml.newPullParser()

                parser.setInput(
                    inputStream,
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

        val articles = mutableListOf<NewsArticle>()

        var eventType = parser.eventType

        var insideItem = false

        var title = ""
        var link = ""
        var source = ""
        var published = ""

        while (
            eventType != XmlPullParser.END_DOCUMENT &&
            articles.size < 10
        ) {

            when (eventType) {

                XmlPullParser.START_TAG -> {

                    when (parser.name.lowercase()) {

                        "item" -> {

                            insideItem = true

                            title = ""
                            link = ""
                            source = ""
                            published = ""
                        }

                        "title" -> {

                            if (insideItem) {

                                title = parser.nextText().trim()
                            }
                        }

                        "link" -> {

                            if (insideItem) {

                                link = parser.nextText().trim()
                            }
                        }

                        "source" -> {

                            if (insideItem) {

                                source = parser.nextText().trim()
                            }
                        }

                        "pubdate" -> {

                            if (insideItem) {

                                published = parser.nextText().trim()
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
                                    title = cleanText(title),
                                    link = link,
                                    source = cleanText(source),
                                    published = published
                                )
                            )
                        }

                        insideItem = false
                    }
                }
            }

            eventType = parser.next()
        }

        return articles
    }

    private fun cleanText(
        value: String
    ): String {

        return value
            .replace("\n", " ")
            .replace("\r", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
```
