package com.example.ui.screens.hadith

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BanglaHeading
import com.example.ui.components.BanglaText
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary

data class Hadith(
    val id: Int,
    val arabic: String?,
    val bengali: String,
    val source: String,
    val topic: String
)

val hadithList = listOf(
    Hadith(
        id = 1,
        arabic = "جَاءَ رَجُلٌ إِلَى رَسُولِ اللَّهِ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ فَقَالَ يَا رَسُولَ اللَّهِ مَنْ أَحَقُّ النَّاسِ بِحُسْنِ صَحَابَتِي قَالَ أُمُّكَ قَالَ ثُمَّ مَنْ قَالَ ثُمَّ أُمُّكَ قَالَ ثُمَّ مَنْ قَالَ ثُمَّ أُمُّكَ قَالَ ثُمَّ مَنْ قَالَ ثُمَّ أَبُوكَ",
        bengali = "এক ব্যক্তি রাসুলুল্লাহ (সা.)-এর কাছে এসে জিজ্ঞেস করল, আমার থেকে সদাচরণ পাওয়ার সবচেয়ে বেশি অধিকারী কে? তিনি বললেন, তোমার মা। লোকটি জিজ্ঞেস করল, তারপর কে? তিনি বললেন, তোমার মা। লোকটি জিজ্ঞেস করল, তারপর কে? তিনি বললেন, তোমার মা। লোকটি পুনরায় জিজ্ঞেস করল, তারপর কে? তিনি বললেন, তোমার বাবা।",
        source = "সহীহ বুখারী ও মুসলিম",
        topic = "মা-বাবা"
    ),
    Hadith(
        id = 2,
        arabic = "خِيَارُكُمْ أَحَاسِنُكُمْ أَخْلاقًا",
        bengali = "তোমাদের মধ্যে সর্বোত্তম ওই ব্যক্তি, যার চরিত্র সবচেয়ে সুন্দর।",
        source = "সহীহ বুখারী",
        topic = "আখলাক ও চরিত্র"
    ),
    Hadith(
        id = 3,
        arabic = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى",
        bengali = "সমস্ত কাজের ফলাফল নিয়তের ওপর নির্ভরশীল। প্রত্যেক মানুষ তাই পাবে যার সে নিয়ত করেছে।",
        source = "সহীহ বুখারী ও মুসলিম",
        topic = "নিয়ত ও ইখলাস"
    ),
    Hadith(
        id = 4,
        arabic = "مَنْ لا يَرْحَمِ النَّاسَ لا يَرْحَمْهُ اللَّهُ",
        bengali = "যে ব্যক্তি মানুষের প্রতি দয়া করে না, আল্লাহও তার প্রতি দয়া করেন না।",
        source = "সহীহ বুখারী",
        topic = "দয়া ও দাক্ষিণ্য"
    ),
    Hadith(
        id = 5,
        arabic = "مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الآخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُتْ",
        bengali = "যে ব্যক্তি আল্লাহ ও শেষ বিচার দিনের প্রতি বিশ্বাস রাখে, সে যেন ভালো কথা বলে অথবা নীরব থাকে।",
        source = "সহীহ বুখারী ও মুসলিম",
        topic = "জিহ্বা হেফাজত"
    ),
    Hadith(
        id = 6,
        arabic = "أَقْرَبُ مَا يَكُونُ الْعَبْدُ مِنْ رَبِّهِ وَهُوَ سَاجِدٌ فَأَكْثِرُوا الدُّعَاءَ",
        bengali = "বান্দা সিজদারত অবস্থায় তার প্রতিপালকের সবচেয়ে নিকটবর্তী হয়। সুতরাং তোমরা বেশি বেশি দুয়া করো।",
        source = "সহীহ মুসলিম",
        topic = "নামাজ ও সিজদা"
    ),
    Hadith(
        id = 7,
        arabic = "الدُّعَاءُ هُوَ الْعِبَادَةُ",
        bengali = "দোয়া বা প্রার্থনা হলো ইবাদতের মূল ভিত্তি।",
        source = "সুনানে তিরমিযী",
        topic = "দোয়া"
    ),
    Hadith(
        id = 8,
        arabic = "إِنَّ أَوَّلَ مَا يُحَاسَبُ بِهِ الْعَبْدُ يَوْمَ الْقِيَامَةِ مِنْ عَمَلِهِ صَلاتُهُ",
        bengali = "কিয়ামতের দিন বান্দার আমলসমূহের মধ্যে সর্বপ্রথম সালাতের (নামাজের) হিসাব নেওয়া হবে।",
        source = "সুনানে আবু দাউদ",
        topic = "নামাজ ও সিজদা"
    ),
    Hadith(
        id = 9,
        arabic = "التَّائِبُ مِنَ الذَّنْبِ كَمَنْ لاَ ذَنْبَ لَهُ",
        bengali = "পাপ থেকে তওবাকারী ব্যক্তি এমন নিষ্পাপ হয়ে যায় যার কোন পাপই নেই।",
        source = "সুনানে ইবনে মাজাহ",
        topic = "তওবা"
    )
)

val topicsList = listOf("সব", "মা-বাবা", "আখলাক ও চরিত্র", "নিয়ত ও ইখলাস", "দয়া ও দাক্ষিণ্য", "নামাজ ও সিজদা", "দোয়া", "তওবা")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithScreen(onBack: () -> Unit = {}) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTopic by remember { mutableStateOf("সব") }
    
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val filteredHadiths = remember(searchQuery, selectedTopic) {
        hadithList.filter { hadith ->
            val matchesTopic = selectedTopic == "সব" || hadith.topic == selectedTopic
            val matchesQuery = searchQuery.isBlank() || 
                hadith.bengali.contains(searchQuery, ignoreCase = true) ||
                (hadith.arabic?.contains(searchQuery, ignoreCase = true) == true) ||
                hadith.source.contains(searchQuery, ignoreCase = true)
            matchesTopic && matchesQuery
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { BanglaHeading(text = "সহীহ হাদিস সংগ্রহ 📜", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { BanglaText("হাদিস অনুসন্ধান করুন...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )

            // Topic Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(topicsList) { topic ->
                    val isSelected = topic == selectedTopic
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTopic = topic },
                        label = { BanglaText(topic, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            if (filteredHadiths.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    BanglaText(
                        text = "কোন হাদিস পাওয়া যায়নি। অন্য কিছু লিখে খুঁজুন।",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredHadiths) { hadith ->
                        HadithCard(
                            hadith = hadith,
                            onCopy = {
                                val textToCopy = "${hadith.bengali}\n— ${hadith.source}\n(শেয়ারড ফ্রম আম্মু অ্যাপ)"
                                clipboardManager.setText(AnnotatedString(textToCopy))
                                Toast.makeText(context, "হাদিসটি কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                            },
                            onShare = {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "${hadith.bengali}\n\n— ${hadith.source}\n(শেয়ারড ফ্রম আম্মু অ্যাপ)")
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "হাদিস শেয়ার করুন")
                                context.startActivity(shareIntent)
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HadithCard(
    hadith: Hadith,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Card Header with Topic Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    BanglaText(
                        text = hadith.topic,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                BanglaText(
                    text = hadith.source,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Arabic Text if present
            if (!hadith.arabic.isNullOrBlank()) {
                Text(
                    text = hadith.arabic,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    lineHeight = 32.sp
                )
            }

            // Bengali Translation
            Row {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(64.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                BanglaText(
                    text = "\"${hadith.bengali}\"",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(8.dp))

            // Copy and Share actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "কপি করুন",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "শেয়ার করুন",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
