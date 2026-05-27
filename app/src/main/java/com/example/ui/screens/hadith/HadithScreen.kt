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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
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
        arabic = "الْجَنَّةُ تَحْتَ أَقْدَامِ الأُمَّهَاتِ",
        bengali = "মায়ের পায়ের নিচে সন্তানের বেহেশত বা জান্নাত নিহিত রয়েছে।",
        source = "সুনানে নাসায়ী: ৩১০৪",
        topic = "মা-বাবা"
    ),
    Hadith(
        id = 3,
        arabic = "رِضَى الرَّبِّ فِي رِضَى الْوَالِدِ وَسُخْطُ الرَّبِّ فِي سُخْطِ الْوَالِدِ",
        bengali = "পিতার সন্তুষ্টিতে প্রতিপালকের সন্তুষ্টি এবং পিতার অসন্তুষ্টিতে প্রতিপালকের অসন্তুষ্টি নিহিত রয়েছে।",
        source = "সুনানে তিরমিযী: ১৮৯৯",
        topic = "মা-বাবা"
    ),
    Hadith(
        id = 4,
        arabic = "خِيَارُكُمْ أَحَاسِنُكُمْ أَخْلاقًا",
        bengali = "তোমাদের মধ্যে সর্বোত্তম ওই ব্যক্তি, যার চরিত্র বা আখলাক সবচেয়ে সুন্দর।",
        source = "সহীহ বুখারী",
        topic = "আখলাক ও চরিত্র"
    ),
    Hadith(
        id = 5,
        arabic = "أَكْمَلُ الْمُؤْمِنِينَ إِيمَانًا أَحْسَنُهُمْ خُلُقًا",
        bengali = "ঈমানের দিক থেকে মুমিনদের মধ্যে পূর্ণতম হলো সেই ব্যক্তি, যার চরিত্র সবচেয়ে সুন্দর ও কোমল।",
        source = "সুনানে তিরমিযী: ১১৬২",
        topic = "আখলাক ও চরিত্র"
    ),
    Hadith(
        id = 6,
        arabic = "إِنَّ الصِّدْقَ يَهْدِي إِلَى الْبِرِّ وَإِنَّ الْبِرَّ يَهْدِي إِلَى الْجَنَّةِ",
        bengali = "নিশ্চয়ই সত্যবাদিতা পুণ্য বা নেক কাজের দিকে পরিচালিত করে, আর পুণ্য জান্নাতের দিকে পরিচালিত করে।",
        source = "সহীহ বুখারী: ৬০৯৪",
        topic = "আখলাক ও চরিত্র"
    ),
    Hadith(
        id = 7,
        arabic = "طَلَبُ الْعِلْمِ فَرِيضَةٌ عَلَى كُلِّ مُسْلِمٍ",
        bengali = "দ্বীনি জ্ঞান অর্জন করা প্রত্যেক মুসলমানের (নর-নারী) ওপর আবশ্যিক কর্তব্য বা ফরজ।",
        source = "সুনানে ইবনে মাজাহ: ২২৪",
        topic = "আখলাক ও চরিত্র"
    ),
    Hadith(
        id = 8,
        arabic = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى",
        bengali = "সমস্ত কাজের ফলাফল নিয়তের ওপর নির্ভরশীল। প্রত্যেক মানুষ তার নিয়ত অনুযায়ীই প্রতিফল পাবে।",
        source = "সহীহ বুখারী ও মুসলিম",
        topic = "নিয়ত ও ইখলাস"
    ),
    Hadith(
        id = 9,
        arabic = "إِنَّ اللَّهَ لا يَنْظُرُ إِلَى صُوَرِكُمْ وَأَمْوَالِكُمْ وَلَكِنْ يَنْظُرُ إِلَى قُلُوبِكُمْ وَأَعْمَالِكُمْ",
        bengali = "নিশ্চয়ই আল্লাহ তোমাদের বাহ্যিক অবয়ব এবং ধনের দিকে তাকান না, বরং তিনি তাকান তোমাদের অন্তর ও আমলের বিশুদ্ধতার দিকে।",
        source = "সহীহ মুসলিম: ২৫৬৪",
        topic = "নিয়ত ও ইখলাস"
    ),
    Hadith(
        id = 10,
        arabic = "مَنْ لا يَرْحَمِ النَّاسَ لا يَرْحَمْهُ اللَّهُ",
        bengali = "যে ব্যক্তি সৃষ্টির প্রতি দয়া করে না, পরম দয়াময় আল্লাহও তার প্রতি দয়া করেন না।",
        source = "সহীহ বুখারী",
        topic = "দয়া ও দাক্ষিণ্য"
    ),
    Hadith(
        id = 11,
        arabic = "الرَّاحِمُونَ يَرْحَمُهُمُ الرَّحْمَنُ ارْحَمُوا مَنْ فِي الأَرْضِ يَرْحَمْكُمْ مَنْ فِي السَّمَاءِ",
        bengali = "দয়াশীলদের ওপর পরম দয়াময় আল্লাহ দয়া করেন। তোমরা জমিনে যারা আছে তাদের প্রতি দয়া করো, তাহলে আসমানে যিনি আছেন তিনি তোমাদের প্রতি দয়া করবেন।",
        source = "সুনানে তিরমিযী: ১৯৫৬",
        topic = "দয়া ও দাক্ষিণ্য"
    ),
    Hadith(
        id = 12,
        arabic = "مَثَلُ الْمُؤْمِنِينَ فِي تَوَادِّهِمْ وَتَرَاحُمِهِمْ وَتَعَاطُفِهِمْ مَثَلُ الْجَسَدِ إِذَا اشْتَكَى مِنْهُ عُضْوٌ تَدَاعَى لَهُ سَائِرُ الْجَسَدِ بِالسَّهَرِ وَالْحُمَّى",
        bengali = "মুমিনদের পারস্পরিক ভালোবাসা, দয়া ও সহানুভূতির উদাহরণ একটি দেহের মতো; যখন দেহের কোনো অঙ্গ আক্রান্ত হয়, তখন পুরো দেহ জ্বর ও অনিদ্রায় তার সাথে সাড়া দেয়।",
        source = "সহীহ বুখারী ও মুসলিম",
        topic = "দয়া ও দাক্ষিণ্য"
    ),
    Hadith(
        id = 13,
        arabic = "مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الآخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُতْ",
        bengali = "যে ব্যক্তি আল্লাহ ও শেষ বিচার দিনের প্রতি বিশ্বাস রাখে, সে যেন ভালো কথা বলে অথবা নীরব থাকে।",
        source = "সহীহ বুখারী ও মুসলিম",
        topic = "আখলাক ও চরিত্র"
    ),
    Hadith(
        id = 14,
        arabic = "لا يَدْخُلُ الْجَنَّةَ قَاطِعٌ",
        bengali = "আত্মীয়তার সম্পর্ক ছিন্নকারী ব্যক্তি জান্নাতে প্রবেশ করবে না।",
        source = "সহীহ বুখারী: ৫৯৮৪",
        topic = "দয়া ও দাক্ষিণ্য"
    ),
    Hadith(
        id = 15,
        arabic = "أَقْرَبُ مَا يَكُونُ الْعَبْدُ مِنْ رَبِّهِ وَهُوَ سَاجِدٌ فَأَكْثِرُوا الدُّعَاءَ",
        bengali = "বান্দা সিজদারত অবস্থায় তার প্রতিপালকের সবচেয়ে নিকটবর্তী হয়। সুতরাং তোমরা সিজদায় বেশি বেশি দুয়া করো।",
        source = "সহীহ মুসলিম",
        topic = "নামাজ ও সিজদা"
    ),
    Hadith(
        id = 16,
        arabic = "إِنَّ أَوَّلَ مَا يُحَاسَبُ بِهِ الْعَبْدُ يَوْمَ الْقِيَامَةِ مِنْ عَمَلِهِ صَلاتُهُ",
        bengali = "কিয়ামতের দিন বান্দার আমলসমূহের মধ্যে সর্বপ্রথম সালাতের (নামাজের) হিসাব নেওয়া হবে।",
        source = "সুনানে আবু দাউদ",
        topic = "নামাজ ও সিজদা"
    ),
    Hadith(
        id = 17,
        arabic = "بَيْنَ الرَّজُلِ وَبَيْنَ الشِّرْكِ وَالْكُفْرِ تَرْكُ الصَّلاَةِ",
        bengali = "ব্যক্তি এবং শিরক ও কুফরের মাঝে একমাত্র প্রধান ব্যবধান হলো নামাজ পরিত্যাগ করা।",
        source = "সহীহ মুসলিম: ৮২",
        topic = "নামাজ ও সিজদা"
    ),
    Hadith(
        id = 18,
        arabic = "الدُّعَاءُ هُوَ الْعِبَادَةُ",
        bengali = "দোয়া বা প্রার্থনা হলো ইবাদতের মূল মজ্জা ও ভিত্তি।",
        source = "সুনানে তিরমিযী",
        topic = "দোয়া"
    ),
    Hadith(
        id = 19,
        arabic = "مَنْ لَمْ يَسْأَلِ اللَّهَ يَغْضَبْ عَلَيْهِ",
        bengali = "যে ব্যক্তি আল্লাহর কাছে দুয়া বা প্রার্থনা করে না, আল্লাহ তার ওপর রাগান্বিত হন।",
        source = "সুনানে তিরমিযী: ৩৩৭৩",
        topic = "দোয়া"
    ),
    Hadith(
        id = 20,
        arabic = "التَّائِبُ مِنَ الذَّنْبِ كَمَنْ لاَ ذَنْبَ لَهُ",
        bengali = "আন্তরিকভাবে পাপ থেকে তওবাকারী ব্যক্তি এমন নিষ্পাপ হয়ে যায় যার কোনো পাপই আর থাকে না।",
        source = "সুনানে ইবনে মাজাহ",
        topic = "তওবা"
    ),
    Hadith(
        id = 21,
        arabic = "كُلُّ ابْنِ آدَمَ خَطَّاءٌ وَخَيْرُ الْخَطَّائِينَ التَّوَّابُونَ",
        bengali = "আদমের সকল সন্তানই কমবেশি ভুলকারী/পাপী, আর ভুলকারীদের মধ্যে সর্বোত্তম হলো তওবাকারীরা।",
        source = "সুনানে তিরমিযী: ২৪৯৯",
        topic = "তওবা"
    )
)

val topicsList = listOf("সব", "মা-বাবা", "আখলাক ও চরিত্র", "নিয়ত ও ইখলাস", "দয়া ও দাক্ষিণ্য", "নামাজ ও সিজদা", "দোয়া", "তওবা")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithScreen(onBack: () -> Unit = {}) {
    var activeTab by remember { mutableStateOf(0) } // 0 = Offline Curated, 1 = Online Library
    var searchQuery by remember { mutableStateOf("") }
    var selectedTopic by remember { mutableStateOf("সব") }
    var numberInput by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val viewModel: HadithViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val selectedBook by viewModel.selectedBook.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val currentSection by viewModel.currentSection.collectAsState()

    var isTtsReady by remember { mutableStateOf(false) }
    var playingHadithId by remember { mutableStateOf<Int?>(null) }
    var playingOnlineHadithKey by remember { mutableStateOf<String?>(null) }
    
    val tts = remember {
        var ttsInstance: android.speech.tts.TextToSpeech? = null
        ttsInstance = android.speech.tts.TextToSpeech(context) { status ->
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                val result = ttsInstance?.setLanguage(java.util.Locale("bn", "BD"))
                if (result != android.speech.tts.TextToSpeech.LANG_MISSING_DATA && result != android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED) {
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

    val filteredOfflineHadiths = remember(searchQuery, selectedTopic) {
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
                title = { BanglaHeading(text = "হাদিস সংগ্রহ 📜", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface) },
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
            // Modern Dual Tab Swapper
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val tabs = listOf("বাছাইকৃত হাদিস (অফলাইন)", "হাদিস লাইব্রেরি (অনলাইন)")
                tabs.forEachIndexed { index, modeName ->
                    val isSelected = activeTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable {
                                activeTab = index
                                // Stop voice output when switching tabs
                                tts?.stop()
                                playingHadithId = null
                                playingOnlineHadithKey = null
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        BanglaText(
                            text = modeName,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            if (activeTab == 0) {
                // --- OFFLINE MODE ---
                
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
                        .padding(vertical = 4.dp),
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

                if (filteredOfflineHadiths.isEmpty()) {
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
                        items(filteredOfflineHadiths) { hadith ->
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
                                },
                                onPlayAudio = {
                                    if (playingHadithId == hadith.id) {
                                        tts?.stop()
                                        playingHadithId = null
                                    } else {
                                        if (isTtsReady) {
                                            tts?.stop()
                                            playingHadithId = hadith.id
                                            tts?.speak(hadith.bengali, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
                                        }
                                    }
                                },
                                isPlaying = playingHadithId == hadith.id
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            } else {
                // --- ONLINE MODE ---
                
                // Book Selector LazyRow
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.books) { book ->
                        val isSelected = selectedBook.id == book.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.selectBook(book)
                                numberInput = ""
                            },
                            label = {
                                BanglaText(
                                    text = book.bengaliName,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                // Numeric Hadith Search and Navigation Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = numberInput,
                        onValueChange = { numberInput = it },
                        placeholder = { BanglaText("হাদিস নং (যেমন: ১২৩)...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            val num = numberInput.toIntOrNull()
                            if (num != null) {
                                viewModel.searchHadithByNumber(num)
                            } else {
                                Toast.makeText(context, "সঠিক নম্বর দিন (১ থেকে শুরু করে)", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        BanglaText("খুঁজুন", fontWeight = FontWeight.Bold)
                    }
                }

                // Navigation Controls (Prev / Next Section)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentSection == 0) {
                        // Search Mode navigation back
                        TextButton(
                            onClick = {
                                viewModel.loadSection(selectedBook, 1)
                                numberInput = ""
                            }
                        ) {
                            BanglaText("← মূল অধ্যায়ে ফিরে যান", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Chapter Mode Navigation
                        IconButton(
                            onClick = { viewModel.prevSection() },
                            enabled = currentSection > 1
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Prev Section",
                                tint = if (currentSection > 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            BanglaText(
                                text = "অধ্যায় $currentSection / ${selectedBook.maxSections}",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.nextSection() },
                            enabled = currentSection < selectedBook.maxSections
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowForward,
                                contentDescription = "Next Section",
                                tint = if (currentSection < selectedBook.maxSections) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                    }
                }

                // Dynamic UI States
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when (val state = uiState) {
                        is HadithLibraryUiState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        is HadithLibraryUiState.Error -> {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                BanglaText(
                                    text = state.message,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        if (currentSection == 0) {
                                            val num = numberInput.toIntOrNull() ?: 1
                                            viewModel.searchHadithByNumber(num)
                                        } else {
                                            viewModel.loadSection(selectedBook, currentSection)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    BanglaText("পুনরায় চেষ্টা করুন", color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        }
                        is HadithLibraryUiState.Success -> {
                            val sectionName = state.metadata?.sections?.get(state.currentSection.toString())
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(state.hadiths) { hadith ->
                                    val key = "${selectedBook.id}_${hadith.hadithnumber}"
                                    DynamicHadithCard(
                                        bookName = state.name,
                                        sectionName = sectionName,
                                        hadith = hadith,
                                        onCopy = {
                                            val textToCopy = "${hadith.text}\n\n— সূত্র: ${state.name}, হাদিস নং: ${hadith.hadithnumber} (শেয়ারড ফ্রম আম্মু অ্যাপ)"
                                            clipboardManager.setText(AnnotatedString(textToCopy))
                                            Toast.makeText(context, "হাদিসটি কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                                        },
                                        onShare = {
                                            val sendIntent: Intent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, "${hadith.text}\n\n— সূত্র: ${state.name}, হাদিস নং: ${hadith.hadithnumber} (শেয়ারড ফ্রম আম্মু অ্যাপ)")
                                                type = "text/plain"
                                            }
                                            val shareIntent = Intent.createChooser(sendIntent, "হাদিস শেয়ার করুন")
                                            context.startActivity(shareIntent)
                                        },
                                        onPlayAudio = {
                                            if (playingOnlineHadithKey == key) {
                                                tts?.stop()
                                                playingOnlineHadithKey = null
                                            } else {
                                                if (isTtsReady) {
                                                    tts?.stop()
                                                    playingOnlineHadithKey = key
                                                    tts?.speak(hadith.text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
                                                }
                                            }
                                        },
                                        isPlaying = playingOnlineHadithKey == key
                                    )
                                }
                                item {
                                    Spacer(modifier = Modifier.height(24.dp))
                                }
                            }
                        }
                        else -> {}
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
    onShare: () -> Unit,
    onPlayAudio: () -> Unit,
    isPlaying: Boolean
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPlayAudio) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                        contentDescription = "শুনুন",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
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

@Composable
fun DynamicHadithCard(
    bookName: String,
    sectionName: String?,
    hadith: HadithDto,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onPlayAudio: () -> Unit,
    isPlaying: Boolean
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
                        text = "হাদিস নং ${hadith.hadithnumber}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                BanglaText(
                    text = bookName,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (!sectionName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                BanglaText(
                    text = "অধ্যায়: $sectionName",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            if (!hadith.grades.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    hadith.grades.forEach { gradeDto ->
                        if (!gradeDto.grade.isNullOrBlank()) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${gradeDto.name ?: "মান"}: ${gradeDto.grade}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(80.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = hadith.text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPlayAudio) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                        contentDescription = "শুনুন",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
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
