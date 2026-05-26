package com.example.ui.screens.medicine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.Medicine
import com.example.data.local.entity.MedicineLog
import com.example.ui.components.BanglaHeading
import com.example.ui.components.BanglaText
import com.example.ui.theme.GreenPrimary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineScreen(viewModel: MedicineViewModel = viewModel()) {
    val activeMedicines by viewModel.activeMedicines.collectAsState()
    val recentLogs by viewModel.recentLogs.collectAsState()
    
    var showAddSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = GreenPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Medicine", tint = Color.White)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.MedicalServices, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    BanglaHeading(text = "আপনার ওষুধের তালিকা \uD83D\uDC8A", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    BanglaText(text = "আপনার স্বাস্থ্য তথ্য সম্পূর্ণ গোপনীয়", color = Color.Gray, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            if (activeMedicines.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.InvertColorsOff, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            BanglaHeading("কোনো ওষুধ যোগ নেই", fontSize = 18.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            BanglaText("আপনার ওষুধের তালিকা শুরু করুন", color = Color.Gray)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showAddSheet = true },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                BanglaText("ওষুধ যোগ করুন", color = Color.White)
                            }
                        }
                    }
                }
            } else {
                item {
                    BanglaHeading(text = "সক্রিয় ওষুধসমূহ", fontSize = 18.sp)
                }
                items(activeMedicines) { medicine ->
                    val logsForThisDesc = recentLogs.filter { it.medicineId == medicine.id }.sortedBy { it.scheduledAt }
                    // Filter upcoming logs
                    val upcomingLog = logsForThisDesc.firstOrNull { it.status == "upcoming" }
                    MedicineCard(
                        medicine = medicine,
                        upcomingLog = upcomingLog,
                        onTakeMedicine = { logToTake ->
                            if (logToTake != null) {
                                viewModel.markLogAsTaken(logToTake)
                            } else {
                                viewModel.addManualLogForMedicine(medicine.id, 0)
                            }
                        },
                        onDeleteMedicine = { viewModel.deleteMedicine(medicine) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                BanglaHeading(text = "আজকের শিডিউল", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            if (recentLogs.isEmpty()) {
                 item {
                    BanglaText("আজ কোনো শিডিউল নেই", color = Color.Gray)
                }
            } else {
                items(recentLogs.take(5)) { log ->
                    // Simplified joined mapping
                    val medMatch = activeMedicines.find { it.id == log.medicineId }
                    if (medMatch != null) {
                         ScheduleItem(log, medMatch) {
                            viewModel.markLogAsTaken(log)
                         }
                    }
                }
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
            AddMedicineForm(
                onSave = { name, bName, dosage, freq, times, start ->
                    viewModel.addMedicine(name, bName, dosage, freq, times, start, null, null)
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
fun MedicineCard(
    medicine: Medicine,
    upcomingLog: MedicineLog?,
    onTakeMedicine: (MedicineLog?) -> Unit,
    onDeleteMedicine: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BanglaHeading(text = medicine.medicineName, fontSize = 18.sp, color = GreenPrimary)
                    if (!medicine.banglaName.isNullOrEmpty()) {
                        BanglaText(text = medicine.banglaName, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = GreenPrimary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        BanglaText(
                            text = "${medicine.dosage} - ${medicine.frequency}", 
                            color = GreenPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = onDeleteMedicine) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (upcomingLog != null) {
                    val sdf = SimpleDateFormat("hh:mm a", Locale("bn", "BD"))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        BanglaText(text = "পরবর্তী: ${sdf.format(Date(upcomingLog.scheduledAt))}", color = Color.Gray)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = GreenPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        BanglaText(text = "আজকের সব ডোজ সম্পন্ন", color = GreenPrimary)
                    }
                }
                
                Button(
                    onClick = { onTakeMedicine(upcomingLog) },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    BanglaText("খেয়েছি", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ScheduleItem(log: MedicineLog, medicine: Medicine, onTake: () -> Unit) {
    val sdf = SimpleDateFormat("hh:mm a", Locale("bn", "BD"))
    val isTaken = log.status == "taken"
    
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
            BanglaText(text = sdf.format(Date(log.scheduledAt)), fontSize = 12.sp, color = if(isTaken) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface)
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(if (isTaken) GreenPrimary else Color(0xFFFFA500), CircleShape)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isTaken) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = if (isTaken) 0.dp else 1.dp,
            modifier = Modifier.weight(1f).let { 
                if (!isTaken) it.clickable(onClick = onTake) else it 
            }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    BanglaText(
                        text = medicine.medicineName, 
                        fontWeight = FontWeight.Bold, 
                        color = if (isTaken) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface 
                    )
                    BanglaText(
                        text = medicine.dosage, 
                        fontSize = 12.sp, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant 
                    )
                }
                if (isTaken) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Taken", tint = GreenPrimary)
                } else {
                    Icon(Icons.Filled.RadioButtonUnchecked, contentDescription = "Upcoming", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun AddMedicineForm(
    onSave: (String, String?, String, String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    var medicineName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("১বার") }
    
    val frequencies = listOf("১বার", "২বার", "৩বার", "প্রয়োজনে")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BanglaHeading(text = "নতুন ওষুধ যোগ করুন", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = medicineName,
            onValueChange = { medicineName = it },
            label = { BanglaText("ওষুধের নাম (যেমন: Napa 500mg)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = dosage,
            onValueChange = { dosage = it },
            label = { BanglaText("মাত্রা (যেমন: ১টি, ৫ মিলি)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        BanglaText("দিনে কতবার?", modifier = Modifier.align(Alignment.Start), color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            frequencies.forEach { freq ->
                FilterChip(
                    selected = frequency == freq,
                    onClick = { frequency = freq },
                    label = { BanglaText(freq) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenPrimary.copy(alpha = 0.2f),
                        selectedLabelColor = GreenPrimary
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // OCR Scanner Placeholder
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.DocumentScanner, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    BanglaText("প্রেসক্রিপশন স্ক্যান করুন", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    BanglaText("AI এর সাহায্যে তথ্য পূরণ করুন", fontSize = 12.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                BanglaText("বাতিল")
            }
            Button(
                onClick = {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val dateStr = sdf.format(Date())
                    onSave(medicineName, null, dosage, frequency, "[]", dateStr)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                enabled = medicineName.isNotEmpty() && dosage.isNotEmpty()
            ) {
                BanglaText("সংরক্ষণ করুন")
            }
        }
    }
}
