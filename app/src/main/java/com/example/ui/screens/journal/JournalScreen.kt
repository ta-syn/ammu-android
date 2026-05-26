package com.example.ui.screens.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.JournalEntry
import com.example.ui.components.BanglaHeading
import com.example.ui.components.BanglaText
import com.example.ui.theme.GreenPrimary
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(viewModel: JournalViewModel = viewModel()) {
    val entries by viewModel.entries.collectAsState()
    var showWriteEntryDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showWriteEntryDialog = true },
                containerColor = GreenPrimary
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "Write Entry", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                BanglaHeading(text = "আপনার ডায়েরি 📓", fontSize = 28.sp)
                BanglaText("আজ কেমন ছিলেন? কিছু লিখুন...", color = Color.Gray, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Mood shortcut row
            item {
                Text("আজকের মেজাজ:", fontSize = 16.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                MoodSelectorRow(selectedMood = "", onMoodSelected = { showWriteEntryDialog = true })
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Entries List
            if (entries.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        BanglaText("এখনও কোনো স্মৃতি লেখা হয়নি।", color = Color.Gray)
                    }
                }
            } else {
                
                val groupedEntries = entries.groupBy { entry ->
                    val cal = Calendar.getInstance().apply { timeInMillis = entry.createdAt }
                    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("bn", "BD"))
                    monthFormat.format(cal.time)
                }
                
                groupedEntries.forEach { (month, monthEntries) ->
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        BanglaHeading(month, fontSize = 20.sp, color = GreenPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(monthEntries) { entry ->
                        JournalEntryCard(entry = entry, onDelete = { viewModel.deleteEntry(entry) })
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    if (showWriteEntryDialog) {
        WriteEntryDialog(
            onDismiss = { showWriteEntryDialog = false },
            onSave = { title, content, mood ->
                viewModel.addEntry(title, content, mood)
                showWriteEntryDialog = false
            }
        )
    }
}

@Composable
fun MoodSelectorRow(selectedMood: String, onMoodSelected: (String) -> Unit) {
    val moods = listOf(
        Pair("😊", "খুশি"),
        Pair("😐", "ঠিকঠাক"),
        Pair("😔", "মন খারাপ"),
        Pair("🤲", "কৃতজ্ঞ"),
        Pair("🤒", "অসুস্থ")
    )
    
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(moods) { (emoji, label) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onMoodSelected(emoji) }
                    .background(if (selectedMood == emoji) Color(0xFFE8F5E9) else Color.Transparent)
                    .padding(8.dp)
            ) {
                Text(emoji, fontSize = 32.sp)
                Spacer(modifier = Modifier.height(4.dp))
                BanglaText(label, fontSize = 12.sp, color = Color.DarkGray)
            }
        }
    }
}

@Composable
fun JournalEntryCard(entry: JournalEntry, onDelete: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM, EEEE - hh:mm a", Locale("bn", "BD"))
    val dateString = dateFormat.format(Date(entry.createdAt))

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (entry.mood?.isNotBlank() == true) {
                        Text(entry.mood, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    BanglaText(dateString, color = Color.Gray, fontSize = 12.sp)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = Color.LightGray)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            if (entry.title?.isNotBlank() == true) {
                BanglaHeading(entry.title, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
            }
            BanglaText(
                text = entry.content,
                fontSize = 15.sp,
                color = Color.DarkGray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteEntryDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf("😊") }
    var showAiHints by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                BanglaHeading("নতুন ডায়েরি", fontSize = 20.sp)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                MoodSelectorRow(selectedMood = selectedMood, onMoodSelected = { selectedMood = it })
                
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { BanglaText("শিরোনাম (ঐচ্ছিক)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { BanglaText("আজ আপনার দিন কেমন কাটল?") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp, max = 300.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE8F5E9))
                        .clickable { showAiHints = !showAiHints }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = GreenPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    BanglaText("AI সাহায্য করুক ✨", color = GreenPrimary, fontWeight = FontWeight.Bold)
                }

                if (showAiHints) {
                    Spacer(modifier = Modifier.height(12.dp))
                    BanglaText("যা নিয়ে লিখতে পারেন:", fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    val hints = listOf("আজকের একটি ভালো মুহূর্ত কী ছিল?", "কী আপনাকে সবচেয়ে বেশি খুশি করেছে আজ?", "আজকের একটি কৃতজ্ঞতা (আলহামদুলিল্লাহ)")
                    hints.forEach { hint ->
                        Surface(
                            color = Color(0xFFF9FAFB),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { content = "$content\n$hint\n- " }
                        ) {
                            BanglaText(hint, modifier = Modifier.padding(12.dp), fontSize = 14.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, content, selectedMood) },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                enabled = content.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                BanglaText("সংরক্ষণ করুন", color = Color.White)
            }
        }
    )
}
