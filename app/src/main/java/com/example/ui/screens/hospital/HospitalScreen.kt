package com.example.ui.screens.hospital

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.Appointment
import com.example.ui.components.BanglaHeading
import com.example.ui.components.BanglaText
import com.example.ui.theme.GreenPrimary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalScreen(viewModel: HospitalViewModel = viewModel()) {
    val appointments by viewModel.appointments.collectAsState()
    
    var showAddAppointmentSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddAppointmentSheet = true },
                containerColor = GreenPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Appointment", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                EmergencySection()
            }
            
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocalHospital, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    BanglaHeading(text = "নিকটস্থ সেবাসমূহ", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickActionCard("হাসপাতাল", "🏥", Modifier.weight(1f)) {
                        val gmmIntentUri = Uri.parse("geo:0,0?q=hospitals")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(mapIntent)
                        } else {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/hospitals")))
                        }
                    }
                    QuickActionCard("ফার্মেসি", "💊", Modifier.weight(1f)) {
                        val gmmIntentUri = Uri.parse("geo:0,0?q=pharmacies")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(mapIntent)
                        } else {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/pharmacies")))
                        }
                    }
                    QuickActionCard("ডাক্তার", "👨‍⚕️", Modifier.weight(1f)) {
                        // Normally would show directory filter, but shortcut to maps for now to find clinics
                        val gmmIntentUri = Uri.parse("geo:0,0?q=clinics")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(mapIntent)
                        } else {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/clinics")))
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                BanglaHeading(text = "অ্যাপয়েন্টমেন্ট ট্র্যাকার", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            if (appointments.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            BanglaText("কোনো অ্যাপয়েন্টমেন্ট নেই", color = Color.Gray)
                        }
                    }
                }
            } else {
                items(appointments) { appointment ->
                    AppointmentCard(appointment, onDelete = { viewModel.deleteAppointment(appointment) })
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                TeleConsultationSection()
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    if (showAddAppointmentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddAppointmentSheet = false },
            sheetState = sheetState
        ) {
            AddAppointmentForm(
                onSave = { docName, spec, hosp, time, reason ->
                    viewModel.addAppointment(docName, spec, hosp, time, reason, null)
                    coroutineScope.launch {
                        sheetState.hide()
                        showAddAppointmentSheet = false
                    }
                },
                onCancel = {
                    coroutineScope.launch {
                        sheetState.hide()
                        showAddAppointmentSheet = false
                    }
                }
            )
        }
    }
}

@Composable
fun EmergencySection() {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val emergencyBg = if (isDark) Color(0xFF3B1D21) else Color(0xFFFFEBEE)
    val emergencyRed = if (isDark) Color(0xFFFF8A80) else Color.Red
    Surface(
        color = emergencyBg,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = emergencyRed)
                Spacer(modifier = Modifier.width(8.dp))
                BanglaHeading(text = "জরুরি অবস্থায়", color = emergencyRed, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EmergencyButton("অ্যাম্বুলেন্স", "৯৯৯", Modifier.weight(1f)) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:999"))
                    context.startActivity(intent)
                }
                EmergencyButton("ফায়ার সার্ভিস", "৯৯৯", Modifier.weight(1f)) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:999"))
                    context.startActivity(intent)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EmergencyButton("জাতীয় হাসপাতাল", "১০৬৫৫", Modifier.weight(1f)) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:10655"))
                    context.startActivity(intent)
                }
                EmergencyButton("পুলিশ", "৯৯৯", Modifier.weight(1f)) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:999"))
                    context.startActivity(intent)
                }
            }
        }
    }
}

@Composable
fun EmergencyButton(title: String, number: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val emergencyRed = if (isDark) Color(0xFFFF8A80) else Color.Red
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BanglaText(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Call, contentDescription = null, tint = emergencyRed, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                BanglaText(text = number, fontSize = 16.sp, color = emergencyRed, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun QuickActionCard(title: String, emoji: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 1.dp,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(8.dp))
            BanglaText(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun AppointmentCard(appointment: Appointment, onDelete: () -> Unit) {
    val sdfDate = SimpleDateFormat("dd MMM yyyy", Locale("bn", "BD"))
    val sdfTime = SimpleDateFormat("hh:mm a", Locale("bn", "BD"))
    val dateObj = Date(appointment.appointmentDate)
    
    // Future vs Past
    val isFuture = appointment.appointmentDate > System.currentTimeMillis()
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Colored stripe or icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(if (isFuture) GreenPrimary.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BanglaText(text = SimpleDateFormat("dd", Locale("bn", "BD")).format(dateObj), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (isFuture) GreenPrimary else Color.Gray)
                    BanglaText(text = SimpleDateFormat("MMM", Locale("bn", "BD")).format(dateObj), fontSize = 12.sp, color = if (isFuture) GreenPrimary else Color.Gray)
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                BanglaHeading(text = appointment.doctorName, fontSize = 16.sp)
                BanglaText(text = "${appointment.specialty} • ${appointment.hospitalName}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AccessTime, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.width(4.dp))
                    BanglaText(text = sdfTime.format(dateObj), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                if (!appointment.reason.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    BanglaText(text = "কারণ: ${appointment.reason}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun TeleConsultationSection() {
    val context = LocalContext.current
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            BanglaHeading(text = "টেলিমেডিসিন সেবা", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    BanglaText(text = "Praava Health", fontWeight = FontWeight.Bold)
                    BanglaText(text = "ভিডিও কলের মাধ্যমে ডাক্তার", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:10648"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    BanglaText("কল করুন")
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    BanglaText(text = "Doctorola", fontWeight = FontWeight.Bold)
                    BanglaText(text = "ডাক্তার ও হাসপাতাল বুকিং", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:09606707070"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    BanglaText("যোগাযোগ")
                }
            }
        }
    }
}

@Composable
fun AddAppointmentForm(
    onSave: (String, String, String, Long, String) -> Unit,
    onCancel: () -> Unit
) {
    var doctorName by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var hospitalName by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    var selectedCalendar by remember { 
        mutableStateOf(Calendar.getInstance().apply { 
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
        }) 
    }
    
    val sdfDate = SimpleDateFormat("dd MMMM, yyyy", Locale("bn", "BD"))
    val sdfTime = SimpleDateFormat("hh:mm a", Locale("bn", "BD"))

    val datePickerDialog = remember {
        val currentYear = selectedCalendar.get(Calendar.YEAR)
        val currentMonth = selectedCalendar.get(Calendar.MONTH)
        val currentDay = selectedCalendar.get(Calendar.DAY_OF_MONTH)
        
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedCalendar = Calendar.getInstance().apply {
                    timeInMillis = selectedCalendar.timeInMillis
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
            },
            currentYear,
            currentMonth,
            currentDay
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
        }
    }

    val timePickerDialog = remember {
        val currentHour = selectedCalendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = selectedCalendar.get(Calendar.MINUTE)
        
        android.app.TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                selectedCalendar = Calendar.getInstance().apply {
                    timeInMillis = selectedCalendar.timeInMillis
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                }
            },
            currentHour,
            currentMinute,
            false
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BanglaHeading(text = "নতুন অ্যাপয়েন্টমেন্ট", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = doctorName,
            onValueChange = { doctorName = it },
            label = { BanglaText("ডাক্তারের নাম (যেমন: ডা. আহমেদ)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = specialty,
            onValueChange = { specialty = it },
            label = { BanglaText("বিভাগ (যেমন: হৃদরোগ, চর্মরোগ)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = hospitalName,
            onValueChange = { hospitalName = it },
            label = { BanglaText("হাসপাতাল বা চেম্বার") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = reason,
            onValueChange = { reason = it },
            label = { BanglaText("দেখানোর কারণ (ঐচ্ছিক)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Pickers row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { datePickerDialog.show() },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BanglaText("তারিখ নির্বাচন", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    BanglaText(sdfDate.format(selectedCalendar.time), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { timePickerDialog.show() },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BanglaText("সময় নির্বাচন", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    BanglaText(sdfTime.format(selectedCalendar.time), fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                    onSave(doctorName, specialty, hospitalName, selectedCalendar.timeInMillis, reason)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                enabled = doctorName.isNotEmpty() && hospitalName.isNotEmpty()
            ) {
                BanglaText("সংরক্ষণ করুন")
            }
        }
    }
}
