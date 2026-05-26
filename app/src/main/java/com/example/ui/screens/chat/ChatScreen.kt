package com.example.ui.screens.chat

import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BanglaHeading
import com.example.ui.components.BanglaText
import com.example.ui.theme.GreenPrimary
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    val messages = viewModel.messages
    val currentMode by viewModel.currentMode.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto scroll to bottom
    LaunchedEffect(messages.size, messages.lastOrNull()?.text?.length) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Mode Selector
        ModeSelector(currentMode) { viewModel.setMode(it) }

        // Free tier UI
        FreeTierBanner()

        // Empty State / Suggestions
        if (messages.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.ChatBubble, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                BanglaHeading("আস-সালামু আলাইকুম!", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                BanglaText("আজকে আপনাকে কীভাবে সাহায্য করতে পারি?", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Suggestions { viewModel.sendMessage(it) }
        } else {
            // Chat History
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageBubble(message)
                }
                if (isTyping) {
                    item(key = "typing") {
                        TypingIndicator()
                    }
                }
            }
        }

        // Input Area
        ChatInputArea(
            text = inputText,
            onTextChanged = { inputText = it },
            onSend = {
                viewModel.sendMessage(inputText)
                inputText = ""
            }
        )
        
        Spacer(modifier = Modifier.navigationBarsPadding().height(80.dp))
    }
}

@Composable
fun ModeSelector(currentMode: ChatMode, onModeSelected: (ChatMode) -> Unit) {
    ScrollableTabRow(
        selectedTabIndex = ChatMode.values().indexOf(currentMode),
        edgePadding = 16.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        ChatMode.values().forEachIndexed { index, mode ->
            Tab(
                selected = currentMode == mode,
                onClick = { onModeSelected(mode) },
                text = {
                    Text(
                        text = mode.title,
                        fontWeight = if (currentMode == mode) FontWeight.Bold else FontWeight.Normal,
                        color = if (currentMode == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }
}

@Composable
fun FreeTierBanner() {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BanglaText(text = "আজকের ২০টি বার্তার মধ্যে ১৫টি ব্যবহার করেছেন", fontSize = 12.sp)
                BanglaText(text = "প্রিমিয়াম নিন →", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { 15f / 20f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    var isAppeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isAppeared = true
    }
    val scale by animateFloatAsState(
        targetValue = if (isAppeared) 1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "bubble_scale"
    )

    val isUser = message.isUser
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bgColor = if (isUser) GreenPrimary else MaterialTheme.colorScheme.surface
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isUser) 16.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 16.dp
    )

    Column(
        modifier = Modifier.fillMaxWidth().scale(scale),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!isUser) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.DarkMode, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Surface(
                shape = shape,
                color = bgColor,
                shadowElevation = if (isUser) 0.dp else 1.dp,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                BanglaText(
                    text = message.text,
                    color = textColor,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.padding(start = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BanglaText(text = "আম্মু অ্যাসিস্ট্যান্ট লিখছে...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun Suggestions(onSuggestionClick: (String) -> Unit) {
    val suggestions = listOf("নামাজের নিয়ম বলো", "আজ কী রান্না করব?", "মাথা ব্যথা কমানোর উপায়")
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(suggestions) { suggestion ->
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                onClick = { onSuggestionClick(suggestion) }
            ) {
                BanglaText(text = suggestion, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontSize = 14.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputArea(text: String, onTextChanged: (String) -> Unit, onSend: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = { if (it.length <= 2000) onTextChanged(it) },
                placeholder = { BanglaText(text = "কিছু লিখুন... (সর্বোচ্চ ২০০০ অক্ষর)", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                ),
                trailingIcon = {
                    IconButton(onClick = { /* TODO Voice input */ }) {
                        Icon(Icons.Filled.Mic, contentDescription = "Voice Input", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            AnimatedVisibility(
                visible = text.isNotBlank(),
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Surface(
                    shape = CircleShape,
                    color = GreenPrimary,
                    modifier = Modifier.size(52.dp),
                    onClick = onSend
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}
