package com.example.ui.screens.quran

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BanglaHeading
import com.example.ui.components.BanglaText
import com.example.ui.components.Radius

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahReaderScreen(surahId: Int, onBack: () -> Unit, viewModel: SurahReaderViewModel = viewModel()) {
    LaunchedEffect(surahId) {
        viewModel.fetchSurah(surahId)
    }

    // Stop audio when leaving screen
    DisposableEffect(Unit) {
        onDispose { viewModel.stopAudio() }
    }

    val uiState by viewModel.uiState.collectAsState()
    val audioState by viewModel.audioState.collectAsState()

    var showTranslation by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()

    val currentPlayingIndex = when (val s = audioState) {
        is AudioState.Playing -> s.ayahIndex
        is AudioState.Paused  -> s.ayahIndex
        else -> null
    }
    val isAudioLoading = audioState is AudioState.Loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (uiState is SurahReaderUiState.Success) {
                        BanglaHeading(
                            text = (uiState as SurahReaderUiState.Success).surah.englishName,
                            fontSize = 20.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.stopAudio()
                        onBack()
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showTranslation = !showTranslation }) {
                        Icon(
                            if (showTranslation) Icons.Filled.Translate else Icons.Filled.VisibilityOff,
                            contentDescription = "Toggle Translation"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            if (uiState is SurahReaderUiState.Success && currentPlayingIndex != null) {
                val surah = (uiState as SurahReaderUiState.Success).surah
                val ayahs = (uiState as SurahReaderUiState.Success).ayahs
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    AudioPlayerBar(
                        surahName = surah.name,
                        currentAyah = currentPlayingIndex + 1,
                        totalAyahs = surah.numberOfAyahs,
                        isPlaying = audioState is AudioState.Playing,
                        isLoading = isAudioLoading,
                        onPlayPause = {
                            val ayah = ayahs.getOrNull(currentPlayingIndex) ?: return@AudioPlayerBar
                            viewModel.toggleAyahAudio(currentPlayingIndex, ayah.audioUrl)
                        },
                        onPrev = {
                            val prevIndex = (currentPlayingIndex - 1).coerceAtLeast(0)
                            val ayah = ayahs.getOrNull(prevIndex) ?: return@AudioPlayerBar
                            viewModel.toggleAyahAudio(prevIndex, ayah.audioUrl)
                        },
                        onNext = {
                            val nextIndex = (currentPlayingIndex + 1).coerceAtMost(ayahs.size - 1)
                            val ayah = ayahs.getOrNull(nextIndex) ?: return@AudioPlayerBar
                            viewModel.toggleAyahAudio(nextIndex, ayah.audioUrl)
                        },
                        onClose = { viewModel.stopAudio() }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (uiState) {
                is SurahReaderUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is SurahReaderUiState.Error -> {
                    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        BanglaText(text = (uiState as SurahReaderUiState.Error).message, color = Color.Red)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.fetchSurah(surahId) }) {
                            BanglaText(text = "আবার চেষ্টা করুন", color = Color.White)
                        }
                    }
                }
                is SurahReaderUiState.Success -> {
                    val surah = (uiState as SurahReaderUiState.Success).surah
                    val ayahs = (uiState as SurahReaderUiState.Success).ayahs

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                BanglaHeading(
                                    text = surah.englishNameTranslation,
                                    fontSize = 28.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                BanglaText(
                                    text = "${if (surah.revelationType == "Meccan") "মাক্কী" else "মাদানী"} • ${surah.numberOfAyahs} আয়াত",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                if (surah.number != 1 && surah.number != 9) {
                                    Text(
                                        text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                }
                            }
                        }

                        itemsIndexed(ayahs) { index, ayah ->
                            val isPlaying = audioState is AudioState.Playing && (audioState as AudioState.Playing).ayahIndex == index
                            val isPaused = audioState is AudioState.Paused && (audioState as AudioState.Paused).ayahIndex == index
                            val isThisLoading = isAudioLoading && currentPlayingIndex == index

                            AyahItem(
                                ayah = ayah,
                                isPlaying = isPlaying,
                                isPaused = isPaused,
                                isLoading = isThisLoading,
                                showTranslation = showTranslation,
                                onPlay = { viewModel.toggleAyahAudio(index, ayah.audioUrl) }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        }

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun AyahItem(
    ayah: AyahUi,
    isPlaying: Boolean,
    isPaused: Boolean,
    isLoading: Boolean,
    showTranslation: Boolean,
    onPlay: () -> Unit
) {
    val bgColor = if (isPlaying || isPaused)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    else MaterialTheme.colorScheme.background

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    BanglaText(
                        text = "${ayah.number}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Play/Pause toggle button
                IconButton(onClick = onPlay) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = when {
                                isPlaying -> Icons.Filled.PauseCircle
                                isPaused  -> Icons.Filled.PlayCircle
                                else      -> Icons.Filled.PlayCircleFilled
                            },
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = if (isPlaying || isPaused)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                IconButton(onClick = { /* Bookmark TODO */ }) {
                    Icon(
                        Icons.Filled.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Arabic text
        Text(
            text = ayah.arabic,
            style = LocalTextStyle.current.copy(
                fontSize = 32.sp,
                lineHeight = 52.sp,
                textAlign = TextAlign.Right
            ),
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        )

        if (showTranslation && ayah.translationBn.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            BanglaText(
                text = ayah.translationBn,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 28.sp
            )
        }
    }
}

@Composable
fun AudioPlayerBar(
    surahName: String,
    currentAyah: Int,
    totalAyahs: Int,
    isPlaying: Boolean,
    isLoading: Boolean,
    onPlayPause: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 16.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            LinearProgressIndicator(
                progress = { currentAyah.toFloat() / totalAyahs.toFloat() },
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = surahName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    BanglaText(
                        text = "আয়াত $currentAyah / $totalAyahs",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPrev) {
                        Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous")
                    }
                    IconButton(onClick = onPlayPause) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Icon(
                                        if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }
                    IconButton(onClick = onNext) {
                        Icon(Icons.Filled.SkipNext, contentDescription = "Next")
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
