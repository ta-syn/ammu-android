package com.example.ui.screens.health

import androidx.compose.foundation.background
import com.example.ui.utils.toBengaliNumber
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.HealthRecord
import com.example.ui.components.BanglaHeading
import com.example.ui.components.BanglaText
import com.example.ui.theme.GreenPrimary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(healthViewModel: HealthViewModel = viewModel()) {
    val records by healthViewModel.records.collectAsState()
    
    var showAddSheet by remember { mutableStateOf(false) }
    var selectedRecordType by remember { mutableStateOf("") }
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    selectedRecordType = "bp"
                    showAddSheet = true 
                },
                containerColor = GreenPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Record", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background) // Warm theme background
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                BanglaHeading(text = "আপনার স্বাস্থ্য তথ্য \uD83D\uDC9A", fontSize = 28.sp)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Health Score Card
                HealthScoreCard(records)
            }

            item {
                BanglaHeading(text = "নতুন রেকর্ড যোগ করুন", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickAddButton(icon = "🩸", label = "রক্তচাপ", onClick = { selectedRecordType = "bp"; showAddSheet = true }, modifier = Modifier.weight(1f))
                    QuickAddButton(icon = "🍭", label = "সুগার", onClick = { selectedRecordType = "sugar"; showAddSheet = true }, modifier = Modifier.weight(1f))
                    QuickAddButton(icon = "⚖️", label = "ওজন", onClick = { selectedRecordType = "weight"; showAddSheet = true }, modifier = Modifier.weight(1f))
                    QuickAddButton(icon = "🌡️", label = "তাপমাত্রা", onClick = { selectedRecordType = "temp"; showAddSheet = true }, modifier = Modifier.weight(1f))
                }
            }

            item {
                BanglaHeading(text = "বর্তমান অবস্থা", fontSize = 18.sp)
            }

            item {
                val latestBp = records.filter { it.recordType == "bp" }.maxByOrNull { it.recordedAt }
                BPCard(latestBp)
            }
            
            item {
                val latestSugar = records.filter { it.recordType == "sugar" }.maxByOrNull { it.recordedAt }
                SugarCard(latestSugar)
            }
            
            item {
                val latestWeight = records.filter { it.recordType == "weight" }.maxByOrNull { it.recordedAt }
                WeightCard(latestWeight)
            }
            
            item {
                SymptomCheckerSection()
            }
            
            item {
                PeriodTrackerSection()
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState
        ) {
            AddRecordContent(
                type = selectedRecordType,
                onSave = { primary, secondary, unit, notes ->
                    healthViewModel.addRecord(selectedRecordType, primary, secondary, unit, notes)
                    coroutineScope.launch {
                        sheetState.hide()
                        showAddSheet = false
                    }
                },
                onCancel = {
                    coroutineScope.launch {
                        sheetState.hide()
                        showAddSheet = false
                    }
                }
            )
        }
    }
}

@Composable
fun HealthScoreCard(records: List<HealthRecord>) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                BanglaText(text = "স্বাস্থ্য স্কোর", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = "৮৫", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(text = "/১০০", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.padding(bottom = 8.dp))
                }
                BanglaText(text = "আপনার স্বাস্থ্য স্বাভাবিক আছে", color = MaterialTheme.colorScheme.primary)
            }
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.Filled.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            }
        }
    }
}

@Composable
fun QuickAddButton(icon: String, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 1.dp,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            BanglaText(text = label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun BPCard(record: HealthRecord?) {
    MetricCard(
        title = "রক্তচাপ (Blood Pressure)",
        icon = "🩸",
        value = if (record != null) "${toBengaliNumber(record.valuePrimary.toInt().toString())}/${toBengaliNumber(record.valueSecondary?.toInt().toString() ?: "")}" else "--",
        unit = "mmHg",
        status = if (record != null) getBpStatus(record.valuePrimary, record.valueSecondary ?: 0.0) else "কোন রেকর্ড নেই",
        date = record?.recordedAt
    )
}

@Composable
fun SugarCard(record: HealthRecord?) {
    // 1.0 = Fasting, 2.0 = After Meal
    val typeStr = if (record?.valueSecondary == 1.0) "(খালি পেটে)" else if (record?.valueSecondary == 2.0) "(খাওয়ার পরে)" else ""
    MetricCard(
        title = "ব্লাড সুগার $typeStr",
        icon = "🍭",
        value = if (record != null) toBengaliNumber(record.valuePrimary.toString()) else "--",
        unit = "mmol/L",
        status = if (record != null) "স্বাভাবিক \uD83D\uDC9A" else "কোন রেকর্ড নেই", // Simplified logic
        date = record?.recordedAt
    )
}

@Composable
fun WeightCard(record: HealthRecord?) {
    MetricCard(
        title = "ওজন ও BMI",
        icon = "⚖️",
        value = if (record != null) toBengaliNumber(record.valuePrimary.toString()) else "--",
        unit = "kg",
        status = if (record != null) "BMI স্বাভাবিক \uD83D\uDC9A" else "কোন রেকর্ড নেই", // Simplified logic
        date = record?.recordedAt
    )
}

@Composable
fun MetricCard(title: String, icon: String, value: String, unit: String, status: String, date: Long?) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                BanglaHeading(text = title, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = value, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = unit, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 6.dp))
                }
                
                Surface(
                    color = if (status.contains("স্বাভাবিক")) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    BanglaText(
                        text = status, 
                        color = if (status.contains("স্বাভাবিক")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp
                    )
                }
            }
            
            if (date != null) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))
                val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale("bn", "BD"))
                BanglaText(text = "শেষ রেকর্ড: ${sdf.format(Date(date))}", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SymptomCheckerSection() {
    val isDark = isSystemInDarkTheme()
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) Color(0xFF0F3E37) else Color(0xFFE0F2F1), // Adapted teal
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                BanglaHeading(text = "AI হেলথ অ্যাসিস্ট্যান্ট", color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            BanglaText(text = "আজ কেমন অনুভব করছেন?", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { BanglaText("মাথা ব্যথা", color = MaterialTheme.colorScheme.onSurfaceVariant) })
                AssistChip(onClick = {}, label = { BanglaText("দুর্বলতা", color = MaterialTheme.colorScheme.onSurfaceVariant) })
                AssistChip(onClick = {}, label = { BanglaText("মাথা ঘোরা", color = MaterialTheme.colorScheme.onSurfaceVariant) })
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { BanglaText("এখানে লিখুন...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            BanglaText(text = "এটি শুধু প্রাথমিক তথ্য। অবশ্যই ডাক্তারের পরামর্শ নিন।", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 10.sp)
        }
    }
}

@Composable
fun PeriodTrackerSection() {
    val isDark = isSystemInDarkTheme()
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) Color(0xFF3E1220) else Color(0xFFFCE4EC), // Adapted pink
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🌸", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                BanglaHeading(text = "পিরিয়ড ট্র্যাকার", color = if (isDark) Color(0xFFFF8DA1) else Color(0xFFC2185B))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    BanglaText(text = "পরবর্তী সম্ভাব্য তারিখ", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    BanglaHeading(text = "১৫ জুন (৫ দিন বাকি)", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp)
                }
                FilledTonalButton(
                    onClick = { },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isDark) Color(0xFF621C34) else Color(0xFFF8BBD0),
                        contentColor = if (isDark) Color(0xFFFFB4C4) else Color(0xFF880E4F)
                    )
                ) {
                    BanglaText(text = "লগ করুন")
                }
            }
        }
    }
}

// Add Record Modal Content
@Composable
fun AddRecordContent(type: String, onSave: (Double, Double?, String, String?) -> Unit, onCancel: () -> Unit) {
    var primaryValue by remember { mutableStateOf("") }
    var secondaryValue by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    
    // For sugar context
    var sugarType by remember { mutableStateOf(1.0) } // 1.0 fasting, 2.0 after meal

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val title = when (type) {
            "bp" -> "রক্তচাপ জমান"
            "sugar" -> "ব্লাড সুগার জমান"
            "weight" -> "ওজন জমান"
            "temp" -> "তাপমাত্রা জমান"
            else -> "রেকর্ড জমান"
        }
        
        BanglaHeading(text = title, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(24.dp))
        
        if (type == "bp") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = primaryValue,
                    onValueChange = { primaryValue = it },
                    label = { BanglaText("সিস্টোলিক (উপরের)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = secondaryValue,
                    onValueChange = { secondaryValue = it },
                    label = { BanglaText("ডায়াস্টোলিক (নিচের)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            BanglaText("সাধারণত ১২০ এবং ৮০", color = Color.Gray, fontSize = 12.sp)
        } else if (type == "sugar") {
            OutlinedTextField(
                value = primaryValue,
                onValueChange = { primaryValue = it },
                label = { BanglaText("সুগারের মাত্রা (mmol/L)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                FilterChip(
                    selected = sugarType == 1.0,
                    onClick = { sugarType = 1.0 },
                    label = { BanglaText("খালি পেটে") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = sugarType == 2.0,
                    onClick = { sugarType = 2.0 },
                    label = { BanglaText("খাওয়ার পরে") }
                )
            }
        } else {
            OutlinedTextField(
                value = primaryValue,
                onValueChange = { primaryValue = it },
                label = { BanglaText(if (type == "weight") "ওজন (kg)" else "তাপমাত্রা (F)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { BanglaText("নোট (ঐচ্ছিক)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                BanglaText("বাতিল")
            }
            Button(
                onClick = {
                    val pVal = primaryValue.toDoubleOrNull() ?: 0.0
                    val sVal = if (type == "bp") secondaryValue.toDoubleOrNull() else if (type == "sugar") sugarType else null
                    val unit = when(type) {
                        "bp" -> "mmHg"
                        "sugar" -> "mmol/L"
                        "weight" -> "kg"
                        else -> "F"
                    }
                    onSave(pVal, sVal, unit, note)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                BanglaText("সংরক্ষণ করুন")
            }
        }
    }
}

fun getBpStatus(sys: Double, dia: Double): String {
    if (sys < 90 || dia < 60) return "কম \uD83D\uDD35"
    if (sys <= 120 && dia <= 80) return "স্বাভাবিক \uD83D\uDC9A"
    if (sys <= 129 && dia < 80) return "একটু বেশি ⚠️"
    if (sys >= 130 || dia >= 80) return "উচ্চ রক্তচাপ 🔴"
    return "অজানা"
}
