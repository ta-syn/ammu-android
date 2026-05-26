package com.example.ui.screens.news

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DatabaseProvider
import com.example.data.local.entity.NewsArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

class NewsViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = DatabaseProvider.getDatabase(application).ammuDao()

    val allNews: StateFlow<List<NewsArticle>> = dao.getAllNews()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            // Check if DB is empty, then fetch latest news
            val currentNews = dao.getAllNews().first()
            if (currentNews.isEmpty()) {
                fetchNews()
            }
        }
    }

    fun fetchNews() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://www.bbc.com/bengali/index.xml")
                    .header("User-Agent", "Mozilla/5.0 AmmuApp/1.0")
                    .build()
                
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        loadOfflineFallback()
                        return@launch
                    }
                    val body = response.body?.string()
                    if (body.isNullOrBlank()) {
                        loadOfflineFallback()
                        return@launch
                    }
                    
                    val articles = parseRssXml(body)
                    if (articles.isNotEmpty()) {
                        dao.insertNewsArticles(articles)
                    } else {
                        loadOfflineFallback()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                loadOfflineFallback()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun parseRssXml(xml: String): List<NewsArticle> {
        val articles = mutableListOf<NewsArticle>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val xpp = factory.newPullParser()
            xpp.setInput(StringReader(xml))
            
            var eventType = xpp.eventType
            var currentTitle = ""
            var currentSummary = ""
            var currentLink = ""
            var currentPubDate = System.currentTimeMillis()
            var insideItem = false
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = xpp.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (tagName.equals("item", ignoreCase = true)) {
                            insideItem = true
                            currentTitle = ""
                            currentSummary = ""
                            currentLink = ""
                            currentPubDate = System.currentTimeMillis()
                        } else if (insideItem) {
                            if (tagName.equals("title", ignoreCase = true)) {
                                currentTitle = xpp.nextText().trim()
                            } else if (tagName.equals("description", ignoreCase = true)) {
                                currentSummary = xpp.nextText().trim()
                            } else if (tagName.equals("link", ignoreCase = true)) {
                                currentLink = xpp.nextText().trim()
                            } else if (tagName.equals("pubDate", ignoreCase = true)) {
                                val pubDateStr = xpp.nextText().trim()
                                currentPubDate = parseDate(pubDateStr)
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (tagName.equals("item", ignoreCase = true)) {
                            insideItem = false
                            if (currentTitle.isNotBlank()) {
                                val summaryClean = stripHtml(currentSummary)
                                val finalSummary = if (summaryClean.length > 200) {
                                    summaryClean.take(197) + "..."
                                } else {
                                    summaryClean
                                }
                                articles.add(
                                    NewsArticle(
                                        id = UUID.nameUUIDFromBytes(currentLink.toByteArray()).toString(),
                                        title = currentTitle,
                                        summary = finalSummary,
                                        source = "BBC বাংলা",
                                        publishedAt = currentPubDate,
                                        category = "দেশ",
                                        url = currentLink
                                    )
                                )
                            }
                        }
                    }
                }
                eventType = xpp.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return articles
    }

    private fun parseDate(dateStr: String): Long {
        return try {
            val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
            sdf.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            try {
                val sdf2 = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US)
                sdf2.parse(dateStr)?.time ?: System.currentTimeMillis()
            } catch (e2: Exception) {
                System.currentTimeMillis()
            }
        }
    }

    private fun stripHtml(html: String): String {
        return html.replace(Regex("<[^>]*>"), "").trim()
    }

    private suspend fun loadOfflineFallback() {
        val currentNews = dao.getAllNews().first()
        if (currentNews.isEmpty()) {
            val fallbackArticles = listOf(
                NewsArticle(
                    id = "fallback_1",
                    title = "বাংলাদেশ দলের নতুন সাফল্য: সিরিজ জয়",
                    summary = "বাংলাদেশ ক্রিকেট দল সর্বশেষ সিরিজে দুর্দান্ত পারফরম্যান্স দেখিয়ে সিরিজ জয় নিশ্চিত করেছে। দলের সবাই ভালো খেলেছে।",
                    source = "প্রথম আলো",
                    publishedAt = System.currentTimeMillis() - 3600000,
                    category = "খেলাধুলা"
                ),
                NewsArticle(
                    id = "fallback_2",
                    title = "ডায়াবেটিস নিয়ন্ত্রণে নতুন গবেষণা",
                    summary = "স্বাস্থ্য বিশেষজ্ঞরা জানিয়েছেন, নিয়মিত হাঁটা এবং সঠিক খাদ্যাভ্যাস ডায়াবেটিস নিয়ন্ত্রণে সবচেয়ে বেশি কার্যকর।",
                    source = "데일리 স্টার",
                    publishedAt = System.currentTimeMillis() - 7200000,
                    category = "স্বাস্থ্য"
                ),
                NewsArticle(
                    id = "fallback_3",
                    title = "রমজানে বাজার দর নিয়ন্ত্রণে সরকার",
                    summary = "আসন্ন রমজান উপলক্ষে নিত্যপ্রয়োজনীয় দ্রব্যের বাজার নিয়ন্ত্রণ রাখতে সরকার বিশেষ টাস্কফোর্স গঠন করেছে।",
                    source = "বাংলা ট্রিবিউন",
                    publishedAt = System.currentTimeMillis() - 10800000,
                    category = "দেশ"
                )
            )
            dao.insertNewsArticles(fallbackArticles)
        }
    }
}
