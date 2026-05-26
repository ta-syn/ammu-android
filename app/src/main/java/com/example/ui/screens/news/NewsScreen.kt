package com.example.ui.screens.news

import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.NewsArticle
import com.example.ui.components.BanglaHeading
import com.example.ui.components.BanglaText
import com.example.ui.theme.GreenPrimary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(viewModel: NewsViewModel = viewModel()) {
    val allNews by viewModel.allNews.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val context = LocalContext.current
    val mainHandler = remember { android.os.Handler(android.os.Looper.getMainLooper()) }
    var activeTtsId by remember { mutableStateOf<String?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    val tts = remember {
        var ttsInstance: TextToSpeech? = null
        ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Ensure Bengali is available. If not, this might fallback to default.
                // Depending on device, "bn_BD" or "bn_IN" might be supported.
                val result = ttsInstance?.setLanguage(Locale("bn", "BD"))
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    isTtsReady = true
                }
            }
        }
        ttsInstance?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                mainHandler.post {
                    activeTtsId = utteranceId
                }
            }
            override fun onDone(utteranceId: String?) {
                mainHandler.post {
                    if (activeTtsId == utteranceId) {
                        activeTtsId = null
                    }
                }
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                mainHandler.post {
                    if (activeTtsId == utteranceId) {
                        activeTtsId = null
                    }
                }
            }
            override fun onError(utteranceId: String?, errorCode: Int) {
                mainHandler.post {
                    if (activeTtsId == utteranceId) {
                        activeTtsId = null
                    }
                }
            }
            override fun onStop(utteranceId: String?, interrupted: Boolean) {
                mainHandler.post {
                    if (activeTtsId == utteranceId) {
                        activeTtsId = null
                    }
                }
            }
        })
        ttsInstance
    }

    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    val categories = listOf("সব", "দেশ", "আন্তর্জাতিক", "স্বাস্থ্য", "ধর্ম", "বিনোদন", "খেলাধুলা")
    var selectedCategory by remember { mutableStateOf("সব") }
    
    var showAiSummary by remember { mutableStateOf(true) }

    val filteredNews = if (selectedCategory == "সব") {
        allNews
    } else {
        allNews.filter { it.category == selectedCategory }
    }

    val todayDate = SimpleDateFormat("dd MMMM, EEEE", Locale("bn", "BD")).format(Date())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.fetchNews() },
                containerColor = GreenPrimary
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                BanglaText(text = todayDate, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                BanglaHeading(text = "আজকের খবর 📰", fontSize = 28.sp)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // AI Summary section
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAiSummary = !showAiSummary }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                Spacer(modifier = Modifier.width(8.dp))
                                BanglaHeading(text = "আজকের সংক্ষিপ্ত সংবাদ", fontSize = 18.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Icon(
                                if (showAiSummary) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        AnimatedVisibility(visible = showAiSummary) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                BanglaText("আজকের শীর্ষ খবরের সারাংশ:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Spacer(modifier = Modifier.height(4.dp))
                                BanglaText(
                                    "বাংলাদেশ ক্রিকেট দল সিরিজে দুর্দান্ত জয় পেয়েছে। ডায়াবেটিস নিয়ন্ত্রণে হাঁটার গুরুত্ব নিয়ে নতুন গবেষণা প্রকাশ হয়েছে। পাশাপাশি রমজানে দ্রব্যমূল্য নিয়ন্ত্রণে সরকার বিশেষ টাস্কফোর্স গঠন করেছে।",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                val isSummaryPlaying = activeTtsId == "ai_summary"
                                val summaryText = "আজকের শীর্ষ খবরের সারাংশ: বাংলাদেশ ক্রিকেট দল সিরিজে দুর্দান্ত জয় পেয়েছে। ডায়াবেটিস নিয়ন্ত্রণে হাঁটার গুরুত্ব নিয়ে নতুন গবেষণা প্রকাশ হয়েছে। পাশাপাশি রমজানে দ্রব্যমূল্য নিয়ন্ত্রণে সরকার বিশেষ টাস্কফোর্স গঠন করেছে।"
                                Button(
                                    onClick = { 
                                        if (isSummaryPlaying) {
                                            tts?.stop()
                                            activeTtsId = null
                                        } else if (isTtsReady) {
                                            tts?.stop()
                                            val params = android.os.Bundle().apply {
                                                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ai_summary")
                                            }
                                            tts?.speak(summaryText, TextToSpeech.QUEUE_FLUSH, params, "ai_summary")
                                            activeTtsId = "ai_summary"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSummaryPlaying) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                                    ),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(
                                        if (isSummaryPlaying) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    BanglaText(if (isSummaryPlaying) "থামুন" else "শুনুন", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Categories Row
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { BanglaText(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GreenPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                }
            } else if (filteredNews.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        BanglaText("কোনো খবর পাওয়া যায়নি", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(filteredNews) { article ->
                    val isCurrentPlaying = activeTtsId == article.id
                    NewsArticleCard(
                        article = article,
                        isTtsReady = isTtsReady,
                        isCurrentPlaying = isCurrentPlaying,
                        onTtsToggle = {
                            if (isCurrentPlaying) {
                                tts?.stop()
                                activeTtsId = null
                            } else if (isTtsReady) {
                                tts?.stop()
                                val params = android.os.Bundle().apply {
                                    putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, article.id)
                                }
                                tts?.speak(article.summary, TextToSpeech.QUEUE_FLUSH, params, article.id)
                                activeTtsId = article.id
                            }
                        }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun NewsArticleCard(
    article: NewsArticle,
    isTtsReady: Boolean,
    isCurrentPlaying: Boolean,
    onTtsToggle: () -> Unit
) {
    val context = LocalContext.current

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(8.dp)) {
                    BanglaText(article.category, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
                
                val timeAgo = calculateTimeAgo(article.publishedAt)
                BanglaText("$timeAgo • ${article.source}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            BanglaHeading(text = article.title, fontSize = 18.sp)
            
            Spacer(modifier = Modifier.height(8.dp))
            BanglaText(
                text = article.summary,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onTtsToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCurrentPlaying) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        if (isCurrentPlaying) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    BanglaText(if (isCurrentPlaying) "থামুন" else "শুনুন", fontSize = 12.sp, color = Color.White)
                }
                
                TextButton(
                    onClick = {
                        // In real app, open WebView or Browser
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.prothomalo.com"))
                        context.startActivity(webIntent)
                    }
                ) {
                    BanglaText("বিস্তারিত পড়ুন", color = GreenPrimary, fontSize = 14.sp)
                    Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = GreenPrimary)
                }
            }
        }
    }
}

fun calculateTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val hours = diff / (1000 * 60 * 60)
    if (hours < 1) return "কিছুক্ষণ আগে"
    return "$hours ঘণ্টা আগে"
}
