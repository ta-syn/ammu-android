package com.example.ui.screens.dua

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

val mockDuas = listOf(
    Dua(
        id = "d1",
        categoryId = "morning",
        title = "সকাল ও সন্ধ্যার শ্রেষ্ঠ দোয়া (সায়্যিদুল ইস্তিগফার)",
        arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ خَلَقْتَنِي وَأَنَا عَبْدُكَ وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ وَأَبُوءُ لَكَ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
        transliteration = "আল্লাহুম্মা আনতা রাব্বী, লা-ইলাহা ইল্লা আনতা, খালাক্বতানী ওয়া আনা আ’বদুকা, ওয়া আনা আ’লা আ’হদিকা ওয়া ওয়া’দিকা মাসতাত্বা’তু, আউ’যূবিকা মিন শাররি মা ছানা’তু। আবূ-উ লাকা বিনি’মাতিকা আ’লাইয়া ওয়া আবূ-উ লাকা বিযামবী, ফাগফিরলী। ফাইন্নাহু লা ইয়াগফিরুয যুনূবা ইল্লা আনতা।",
        translation = "হে আল্লাহ! আপনি আমার রব্ব। আপনি ছাড়া আর কোনো হক্ব ইলাহ নেই। আপনি আমাকে সৃষ্টি করেছেন এবং আমি আপনার বান্দা। আমি আমার সাধ্যমতো আপনার (তাওহীদের) অঙ্গীকার ও প্রতিশ্রুতির ওপর আছি। আমি আমার কৃতকর্মের অনিষ্ট থেকে আপনার কাছে আশ্রয় চাই। আপনি আমাকে যত নিআমত দিয়েছেন সেগুলোর ও আমার পাপগুলোর স্বীকৃতি দিচ্ছি। অতএব, আপনি আমাকে ক্ষমা করে দিন। পরিশেষে আপনি ছাড়া আর কেউ পাপসমূহ ক্ষমা করতে পারে না।",
        reference = "বুখারী: ৬৩০৬"
    ),
    Dua(
        id = "d1_2",
        categoryId = "morning",
        title = "বিপদ-আপদ থেকে সুরক্ষার দোয়া",
        arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
        transliteration = "বিসমিল্লাহিল্লাযী লা ইয়াদুররু মা‘আসমিহী শাইউন ফিল আরদি ওয়া লা ফিস সামায়ি ওয়া হুওআস সামী‘উল ‘আলীম।",
        translation = "আল্লাহর নামে, যাঁর নামের বরকতে আসমান ও জমিনের কোনো কিছুই কোনো ক্ষতি করতে পারে না, আর তিনি সর্বশ্রোতা, সর্বজ্ঞ।",
        reference = "আবু দাউদ: ৫০৮৮"
    ),
    Dua(
        id = "d2_2",
        categoryId = "evening",
        title = "সন্ধ্যায় অমঙ্গল থেকে আশ্রয় চাওয়ার দোয়া",
        arabic = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
        transliteration = "আউযু বিকালিমাতিলাহিত তাম্মাতী মিন শাররী মা খালাক্ব।",
        translation = "আমি আল্লাহর নিখুঁত বাণীসমূহের সাহায্যে তাঁর সৃষ্টির সকল অনিষ্ট হতে আশ্রয় প্রার্থনা করছি।",
        reference = "মুসলিম: ২৭০৮"
    ),
    Dua(
        id = "d2",
        categoryId = "sleep",
        title = "ঘুমানোর দোয়া",
        arabic = "اللَّهُمَّ بِاسْمِكَ أَمُوتُ وَأَحْيَا",
        transliteration = "আল্লাহুম্মা বি-ইসমিকা আমূতু ওয়া আহ্ইয়া।",
        translation = "হে আল্লাহ! আপনার নামেই আমি মরণশীল (ঘুমাই) এবং আপনার নামেই জীবন্ত (জাগ্রত) হই।",
        reference = "বুখারী: ৬৩১২"
    ),
    Dua(
        id = "d2_3",
        categoryId = "sleep",
        title = "ঘুমানোর পূর্বে আয়াতুল কুরসি",
        arabic = "اللَّهُ لَا إِلَهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْমٌ...",
        transliteration = "আল্লাহু লা ইলাহা ইল্লা হুয়াল হাইয়ুল কাইয়ুম...",
        translation = "আল্লাহ, তিনি ছাড়া কোনো সত্য ইলাহ নেই। তিনি চিরঞ্জীব, সবকিছুর ধারক। তাঁকে তন্দ্রা ও নিদ্রা স্পর্শ করে না...",
        reference = "সূরা আল-বাকারাহ: ২৫৫"
    ),
    Dua(
        id = "d3_wakeup",
        categoryId = "wakeup",
        title = "ঘুম থেকে ওঠার দোয়া",
        arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
        transliteration = "আলহামদু লিল্লাহিল্লাযী আহ ইয়ানা বা’দা মা আমাতানা ওয়া ইলাইহিন নুশূর।",
        translation = "সকল প্রশংসা আল্লাহর জন্য, যিনি আমাদেরকে মৃত (ঘুমন্ত) করার পর জীবিত (জাগ্রত) করলেন এবং তাঁর দিকেই আমাদের ফিরে যেতে হবে।",
        reference = "বুখারী: ৬৩১৪"
    ),
    Dua(
        id = "d4_food",
        categoryId = "food",
        title = "খাবার খাওয়ার শুরুর দোয়া",
        arabic = "بِسْمِ اللَّهِ",
        transliteration = "বিসমিল্লাহ।",
        translation = "আল্লাহর নামে (শুরু করছি)।",
        reference = "আবু দাউদ: ৩৭৬৭"
    ),
    Dua(
        id = "d4_2_food",
        categoryId = "food",
        title = "খাবার খাওয়ার শেষের দোয়া",
        arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنَا وَسَقَانَا وَجَعَلَنَا مُسْلِمِينَ",
        transliteration = "আলহামদু লিল্লাহিল্লাযী আতআমানা ওয়া সাক্বানা ওয়া জাআলানা মুসলিমীন।",
        translation = "সব প্রশংসা আল্লাহর জন্য, যিনি আমাদের আহার করিয়েছেন, পান করিয়েছেন এবং মুসলমান বানিয়েছেন।",
        reference = "আবু দাউদ: ৩৮৫০"
    ),
    Dua(
        id = "d5_prayer",
        categoryId = "prayer",
        title = "নামাজ শেষে ক্ষমা ও শান্তির দোয়া",
        arabic = "اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ",
        transliteration = "আল্লাহুম্মা আনতাস সালামু ওয়া মিনকাস সালামু তাবারকতা ইয়া যাল জালালি ওয়াল ইকরাম।",
        translation = "হে আল্লাহ! আপনিই শান্তি এবং আপনার থেকেই শান্তি আসে। হে মহিমাময় ও মহানুভব! আপনি বরকতময়।",
        reference = "মুসলিম: ৫৯১"
    ),
    Dua(
        id = "d6_sickness",
        categoryId = "sickness",
        title = "রোগীর আরোগ্যের জন্য দোয়া",
        arabic = "أَذْهِبِ الْبَاسَ رَبَّ النَّاسِ اشْفِ وَأَنْتَ الشَّافِي لَا شِفَاءَ إِلَّا شِفَاؤُكَ شِفَاءً لَا يُغَادِرُ سَقَمًا",
        transliteration = "আযহিবিল বা’সা রব্বান নাসি ইশফি ওয়া আনতাশ শাফি, লা শিফাআ ইল্লা শিফাউকা শিফাআন লা ইয়ুগাদিরু সাক্বামা।",
        translation = "হে মানুষের প্রতিপালক! কষ্ট দূর করে দিন, রোগমুক্ত করুন। আপনিই আরোগ্যদানকারী, আপনার আরোগ্য ছাড়া কোনো আরোগ্য নেই। এমন আরোগ্য দিন যা কোনো রোগকে অবশিষ্ট রাখে না।",
        reference = "বুখারী: ৫৭৫০"
    ),
    Dua(
        id = "d7_travel",
        categoryId = "travel",
        title = "যানবাহনে চড়ার দোয়া",
        arabic = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ",
        transliteration = "সুবহানাল্লাযী সাখখারা লানা হাযা ওয়া মা কুন্না লাহূ মুক্বরিনীন, ওয়া ইন্না ইলা রব্বিনা লামুনক্বালিবূন।",
        translation = "পবিত্র সেই সত্তা যিনি একে আমাদের বশীভূত করে দিয়েছেন, অথচ আমরা একে বশীভূত করতে পারতাম না। আর অবশ্যই আমরা আমাদের প্রতিপালকের দিকে ফিরে যাব।",
        reference = "সূরা আয-যুখরুফ: ১৩-১৪"
    ),
    Dua(
        id = "d8_rain",
        categoryId = "rain",
        title = "বৃষ্টির সময় পড়ার দোয়া",
        arabic = "اللَّهُمَّ صَيِّبًا نَافِعًا",
        transliteration = "আল্লাহুম্মা সয়্যিবান নাফিআ।",
        translation = "হে আল্লাহ! আমাদের ওপর উপকারী বৃষ্টি বর্ষণ করুন।",
        reference = "বুখারী: ১০৩২"
    ),
    Dua(
        id = "d3",
        categoryId = "parents",
        title = "পিতামাতার জন্য দোয়া",
        arabic = "رَّبِّ ارْحَمْهُمَا كَمَا رَبَّيَانِي صَغِيرًا",
        transliteration = "রাব্বির হামহুমা কামা রাব্বাইয়্যানি সাগিরা।",
        translation = "হে আমার প্রতিপালক, তাদের উভয়ের প্রতি রহম কর, যেমন তারা আমাকে শৈশবকালে লালন-পালন করেছেন।",
        reference = "সূরা বনী ইসরাঈল: ২৪"
    ),
    Dua(
        id = "d10_children",
        categoryId = "children",
        title = "সন্তানদের ক্ষতি থেকে রক্ষার দোয়া",
        arabic = "أُعِيذُكُمَا بِكَلِمَاتِ اللَّهِ التَّامَّةِ مِنْ كُلِّ شَيْطَانٍ وَهَامَّةٍ وَمِنْ كُلِّ عَيْنٍ لَامَّةٍ",
        transliteration = "উয়ীযুকুমা বিকালিমাতিলাহিত তাম্মাতি মিন কুল্লি শয়তানিও ওয়াহাম্মাহ, ওয়ামিন কুল্লি আইনিল লাম্মাহ।",
        translation = "আমি তোমাদের দুজনকে আল্লাহর নিখুঁত বাньої আশ্রয়ে সঁপে দিচ্ছি সকল শয়তান, বিষাক্ত কীট এবং কুদৃষ্টির অনিষ্ট থেকে।",
        reference = "বুখারী: ৩৩৭১"
    ),
    Dua(
        id = "d11_special",
        categoryId = "special",
        title = "শবে কদরের দোয়া",
        arabic = "اللَّهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي",
        transliteration = "আল্লাহুম্মা ইন্নাকা আফুউউন তুহিব্বুল আফওয়া ফাফু আন্নি।",
        translation = "হে আল্লাহ! নিশ্চয়ই আপনি ক্ষমাশীল, ক্ষমাকে ভালোবাসেন। অতএব আমাকে ক্ষমা করুন।",
        reference = "তিরমিযী: ৩৫১৩"
    ),
    Dua(
        id = "d12_tawbah",
        categoryId = "tawbah",
        title = "ইউনুস আলাইহিস সালামের দোয়া (তাওবাহ ও বিপদ মুক্তির দোয়া)",
        arabic = "لَّا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ إِنِّي كُنتُ مِنَ الظَّالِمِينَ",
        transliteration = "লা ইলাহা ইল্লা আনতা সুবহানাকা ইন্নি কুনতু মিনায যলিমীন।",
        translation = "আপনি ছাড়া আর কোনো সত্য মাবুদ নেই, আপনি মহাপবিত্র! নিশ্চয়ই আমি অপরাধীদের অন্তর্ভুক্ত ছিলাম।",
        reference = "সূরা আল-অম্বিয়া: ৮৭"
    ),
    Dua(
        id = "d13_rizq",
        categoryId = "rizq",
        title = "জ্ঞান ও হালাল রিজিক অর্জনের দোয়া",
        arabic = "اللَّهُمَّ إِنِّই أَسْأَلُكَ عِلْمًا نَافِعًا وَرِزْقًا طَيِّبًا وَعَمَلًا مُتَقَبَّلًا",
        transliteration = "আল্লাহুম্মা ইন্নি আসআলুকা ইলমান নাফিআ, ওয়া রিযক্বান তয়্যিবা, ওয়া আমালান মুক্বাব্বালা।",
        translation = "হে আল্লাহ! আমি আপনার নিকট উপকারী জ্ঞান, পবিত্র রিজিক এবং কবুলযোগ্য আমল প্রার্থনা করছি।",
        reference = "ইবনে মাজাহ: ৯২৫"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuaScreen() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf<DuaCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    val tabs = listOf("দোয়া সমূহ", "বুকমার্ক")

    BackHandler(enabled = selectedCategory != null) {
        selectedCategory = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFCF9F2)) // Warm cream background
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
                    containerColor = Color(0xFFFCF9F2),
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
            DuaList(categoryId = selectedCategory!!.id)
        } else {
            // Main View
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color(0xFFFCF9F2),
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
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )
            }

            if (searchQuery.isNotEmpty()) {
                // Search Results
                val filtered = mockDuas.filter { 
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
                            DuaCard(dua = dua)
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
        color = Color.White,
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
fun DuaList(categoryId: String) {
    val duas = mockDuas.filter { it.categoryId == categoryId }
    
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
            DuaCard(dua = dua)
        }
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun DuaCard(dua: Dua) {
    var isLiked by remember { mutableStateOf(false) }
    
    Surface(
        shape = RoundedCornerShape(Radius.lg),
        color = Color.White,
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
                    IconButton(onClick = { /* Play audio */ }) {
                        Icon(Icons.Filled.PlayCircle, contentDescription = "Play", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { isLiked = !isLiked }) {
                        Icon(
                            if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, 
                            contentDescription = "Favorite", 
                            tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { /* Copy */ }) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
