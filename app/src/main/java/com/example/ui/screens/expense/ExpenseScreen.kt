package com.example.ui.screens.expense

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.ExpenseRecord
import com.example.ui.components.BanglaHeading
import com.example.ui.components.BanglaText
import com.example.ui.theme.GreenPrimary
import com.example.ui.utils.toBengaliNumber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(viewModel: ExpenseViewModel = viewModel()) {
    val expenses by viewModel.expenses.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = GreenPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Expense")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header & Total
                item {
                    val total = expenses.sumOf { it.amount }
                    BanglaHeading("আপনার মাসিক খরচ", fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = GreenPrimary,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            BanglaText("এই মাসে মোট খরচ", color = Color.White.copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.height(4.dp))
                            BanglaHeading("${toBengaliNumber(total.toInt().toString())} ৳", fontSize = 36.sp, color = Color.White)
                        }
                    }
                }

                // AI Analysis & Budget Alert
                item {
                    val total = expenses.sumOf { it.amount }
                    val budget = 15000.0
                    val percent = (total / budget) * 100

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            BanglaHeading("💡 খরচের রিয়েল-টাইম বিশ্লেষণ", fontSize = 16.sp, color = GreenPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            BanglaText("গত মাসের তুলনায় বাজার খরচ ১৫% বেশি হয়েছে। সাশ্রয়ী হতে বাজারে যাওয়ার আগে তালিকা তৈরি করুন।", fontSize = 14.sp)
                            
                            if (percent >= 80) {
                                val isDark = isSystemInDarkTheme()
                                val warnBg = if (isDark) Color(0xFF3B1D21) else Color(0xFFFFEBEE)
                                val warnRed = if (isDark) Color(0xFFFF8A80) else Color.Red
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(warnBg, RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Warning, contentDescription = null, tint = warnRed)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    BanglaText(
                                        "সতর্কতা: আপনার নির্ধারিত বাজেটের ${toBengaliNumber(percent.toInt().toString())}% শেষ হয়েছে।",
                                        color = warnRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Breakdown Chart
                item {
                    if (expenses.isNotEmpty()) {
                        ExpenseChart(expenses)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    BanglaHeading("লেনদেন সমূহ", fontSize = 18.sp)
                }

                // Group by date
                val grouped = expenses.groupBy { it.recordedAt }
                grouped.forEach { (date, dailyExpenses) ->
                    item {
                        val displayDate = when (date) {
                            getCurrentDate() -> "আজ"
                            getYesterdayDate() -> "গতকাল"
                            else -> formatDateBn(date)
                        }
                        BanglaText(displayDate, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp))
                    }
                    items(dailyExpenses) { expense ->
                        ExpenseItem(expense, onDelete = { viewModel.deleteExpense(expense) })
                    }
                }
                
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (showAddSheet) {
        ExpenseBottomSheet(
            onDismiss = { showAddSheet = false },
            onSave = { category, amount, description ->
                viewModel.addExpense(category, amount, description)
                showAddSheet = false
            }
        )
    }
}

@Composable
fun ExpenseChart(expenses: List<ExpenseRecord>) {
    val total = expenses.sumOf { it.amount }
    val byCategory = expenses.groupBy { it.category }.mapValues { it.value.sumOf { e -> e.amount } }
    
    val categories = listOf(
        Pair("বাজার", Color(0xFF4CAF50)),
        Pair("ওষুধ", Color(0xFF2196F3)),
        Pair("বিদ্যুৎ/গ্যাস", Color(0xFFFF9800)),
        Pair("যাতায়াত", Color(0xFF9C27B0)),
        Pair("অন্যান্য", Color(0xFF607D8B))
    )

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    var startAngle = -90f
                    categories.forEach { (name, color) ->
                        val catTotal = byCategory[name] ?: 0.0
                        if (catTotal > 0 && total > 0) {
                            val sweep = (catTotal / total).toFloat() * 360f
                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(width = 30f, cap = StrokeCap.Butt)
                            )
                            startAngle += sweep
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                categories.forEach { (name, color) ->
                    val catTotal = byCategory[name] ?: 0.0
                    if (catTotal > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                            Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            BanglaText(name, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            BanglaText("${toBengaliNumber(catTotal.toInt().toString())} ৳", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseItem(expense: ExpenseRecord, onDelete: () -> Unit) {
    val icons = mapOf(
        "বাজার" to "🛒",
        "ওষুধ" to "💊",
        "বিদ্যুৎ/গ্যাস" to "💡",
        "যাতায়াত" to "🚌",
        "অন্যান্য" to "🛍️"
    )

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(icons[expense.category] ?: "🛍️", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                BanglaHeading(expense.category, fontSize = 16.sp)
                if (!expense.description.isNullOrBlank()) {
                    BanglaText(expense.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            BanglaHeading("${toBengaliNumber(expense.amount.toInt().toString())} ৳", fontSize = 16.sp, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseBottomSheet(
    onDismiss: () -> Unit,
    onSave: (String, Double, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("বাজার") }
    
    val categories = listOf("বাজার", "ওষুধ", "বিদ্যুৎ/গ্যাস", "যাতায়াত", "অন্যান্য")
    val icons = listOf("🛒", "💊", "💡", "🚌", "🛍️")

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp)
        ) {
            BanglaHeading("নতুন খরচ যোগ করুন", fontSize = 20.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { BanglaText("পরিমাণ (৳)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            BanglaText("ক্যাটাগরি", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                categories.forEachIndexed { index, cat ->
                    val isSelected = selectedCategory == cat
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(if (isSelected) GreenPrimary else MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(icons[index], fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        BanglaText(cat, fontSize = 12.sp, color = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { BanglaText("বিবরণ (ঐচ্ছিক)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { 
                    val amt = amount.toDoubleOrNull()
                    if (amt != null && amt > 0) {
                        onSave(selectedCategory, amt, description)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                BanglaText("সংরক্ষণ করুন", fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

// ---------------- Date Formatter Helpers ---------------- //

private fun getCurrentDate(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}

private fun getYesterdayDate(): String {
    val msInDay = 1000 * 60 * 60 * 24
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(System.currentTimeMillis() - msInDay))
}

private fun formatDateBn(isoTime: String): String {
    try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = parser.parse(isoTime) ?: return isoTime
        val formatter = SimpleDateFormat("dd MMMM, yyyy", Locale("bn", "BD"))
        return formatter.format(date)
    } catch (e: Exception) {
        return isoTime
    }
}
