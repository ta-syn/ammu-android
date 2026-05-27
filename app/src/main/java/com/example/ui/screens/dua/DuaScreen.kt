package com.example.ui.screens.dua

import android.content.Context
import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import android.speech.tts.TextToSpeech
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.BackHandler
import com.example.ui.components.BanglaHeading
import com.example.ui.components.BanglaText
import com.example.ui.components.Radius
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldAccent

data class DuaCategory(val id: String, val title: String, val icon: String, val count: Int)

val duaCategories = listOf(
    DuaCategory("morning", "সকালের আমল", "🌅", 15),
    DuaCategory("evening", "সন্ধ্যার আমল", "🌇", 12),
    DuaCategory("sleep", "ঘুমানোর দোয়া", "😴", 5),
    DuaCategory("wakeup", "ঘুম থেকে উঠার দোয়া", "☀️", 3),
    DuaCategory("food", "খাওয়ার আগে ও পরে", "🍽️", 4),
    DuaCategory("prayer", "নামাজের দোয়া", "🕌", 20),
    DuaCategory("sickness", "রোগ-শোকের দোয়া", "🤒", 8),
    DuaCategory("travel", "সফরের দোয়া", "✈️", 6),
    DuaCategory("rain", "বৃষ্টির দোয়া", "🌧️", 3),
    DuaCategory("parents", "পিতামাতার জন্য দোয়া", "👨‍👩‍👧", 4),
    DuaCategory("children", "সন্তানের জন্য দোয়া", "👶", 3),
    DuaCategory("special", "বিশেষ দোয়া (কদর, জুমুআ)", "✨", 10),
    DuaCategory("tawbah", "তাওবার দোয়া", "🤲", 7),
    DuaCategory("rizq", "রিজিকের দোয়া", "🤲", 5)
)

data class Dua(
    val id: String,
    val categoryId: String,
    val title: String,
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val reference: String
)



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuaScreen() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf<DuaCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    var isTtsReady by remember { mutableStateOf(false) }
    val tts = remember {
        var ttsInstance: TextToSpeech? = null
        ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = ttsInstance?.setLanguage(Locale("bn", "BD"))
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    isTtsReady = true
                }
            }
        }
        ttsInstance
    }

    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    val tabs = listOf("দোয়া সমূহ", "বুকমার্ক")

    BackHandler(enabled = selectedCategory != null) {
        selectedCategory = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Warm theme background
    ) {
        if (selectedCategory != null) {
            // Detailed View
            TopAppBar(
                title = { BanglaHeading(text = selectedCategory!!.title, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { selectedCategory = null }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
            DuaList(
                categoryId = selectedCategory!!.id,
                onPlayAudio = { text ->
                    if (isTtsReady) {
                        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                }
            )
        } else {
            // Main View
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            Box(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { BanglaText(text = "দোয়া খুঁজুন...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.full),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    singleLine = true
                )
            }

            if (searchQuery.isNotEmpty()) {
                // Search Results
                val filtered = allDuas.filter { 
                    it.title.contains(searchQuery, ignoreCase = true) ||
                    it.translation.contains(searchQuery, ignoreCase = true)
                }
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    if (filtered.isEmpty()) {
                        item {
                            BanglaText(text = "কোনো দোয়া পাওয়া যায়নি।", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        items(filtered) { dua ->
                            DuaCard(
                                dua = dua,
                                onPlayAudio = { text ->
                                    if (isTtsReady) {
                                        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                                    }
                                }
                            )
                        }
                    }
                }
            } else if (selectedTabIndex == 0) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                        DailyDuaWidget(modifier = Modifier.padding(bottom = 16.dp))
                    }
                    items(duaCategories) { category ->
                        CategoryCard(category = category, onClick = { selectedCategory = category })
                    }
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            } else {
                // Bookmarks Tab
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.FavoriteBorder, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primaryContainer)
                    Spacer(modifier = Modifier.height(16.dp))
                    BanglaHeading(text = "কোনো বুকমার্ক নেই", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun CategoryCard(category: DuaCategory, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(Radius.lg),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 2.dp,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = category.icon, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(12.dp))
            BanglaHeading(text = category.title, fontSize = 16.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(4.dp))
            BanglaText(text = "${category.count} টি দোয়া", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DailyDuaWidget(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(Radius.lg),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = GoldLight, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                BanglaHeading(text = "আজকের দোয়া", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "رَبِّ زِدْنِي عِلْمًا",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            BanglaText(
                text = "হে আমার প্রতিপালক! আমার জ্ঞান বৃদ্ধি করুন।",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun DuaList(categoryId: String, onPlayAudio: (String) -> Unit) {
    val duas = allDuas.filter { it.categoryId == categoryId }
    
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (categoryId == "morning" || categoryId == "evening") {
            item {
                Button(
                    onClick = { /* Start routine */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    BanglaText(text = "আমল শুরু করুন", color = Color.White)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        items(duas) { dua ->
            DuaCard(
                dua = dua,
                onPlayAudio = onPlayAudio
            )
        }
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun DuaCard(dua: Dua, onPlayAudio: (String) -> Unit) {
    val context = LocalContext.current
    var isLiked by remember { mutableStateOf(false) }
    
    Surface(
        shape = RoundedCornerShape(Radius.lg),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            BanglaHeading(text = dua.title, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = dua.arabic,
                fontSize = 28.sp,
                lineHeight = 44.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Right,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BanglaText(
                text = "উচ্চারণ: ${dua.transliteration}",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            BanglaText(
                text = "অর্থ: ${dua.translation}",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BanglaText(
                    text = dua.reference,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row {
                    IconButton(onClick = { onPlayAudio(dua.translation) }) {
                        Icon(Icons.Filled.PlayCircle, contentDescription = "Play", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { isLiked = !isLiked }) {
                        Icon(
                            if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, 
                            contentDescription = "Favorite", 
                            tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("দোয়া", "${dua.title}\n\nআরবি: ${dua.arabic}\n\nউচ্চারণ: ${dua.transliteration}\n\nঅর্থ: ${dua.translation}\n\nসূত্র: ${dua.reference}")
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "দোয়া কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, dua.title)
                            putExtra(Intent.EXTRA_TEXT, "${dua.title}\n\nআরবি: ${dua.arabic}\n\nউচ্চারণ: ${dua.transliteration}\n\nঅর্থ: ${dua.translation}\n\nসূত্র: ${dua.reference}")
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "শেয়ার করুন"))
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
