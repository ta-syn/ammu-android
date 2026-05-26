package com.example.ui.screens.family

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.FamilyMember
import com.example.ui.components.BanglaHeading
import com.example.ui.components.BanglaText
import com.example.ui.theme.GreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyScreen(viewModel: FamilyViewModel = viewModel()) {
    val familyMembers by viewModel.familyMembers.collectAsState()
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GreenPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Member", tint = Color.White)
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
                BanglaHeading(text = "পরিবার ও যোগাযোগ 👨‍👩‍👧‍👦", fontSize = 28.sp)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Emergency SOS Section
            item {
                val emergencyContacts = familyMembers.filter { it.isEmergencyContact }
                EmergencySOSCard(contacts = emergencyContacts) { phone ->
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                    context.startActivity(intent)
                }
            }

            // Status Check-in
            item {
                StatusCheckInCard()
            }

            // Family Members Grid (Horizontal scroll style or list)
            item {
                Spacer(modifier = Modifier.height(16.dp))
                BanglaHeading("পরিবারের সদস্য", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(familyMembers) { member ->
                FamilyMemberCard(member = member) { action, phone ->
                    when(action) {
                        "call" -> {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        }
                        "sms" -> {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone"))
                            context.startActivity(intent)
                        }
                        "whatsapp" -> {
                            try {
                                val url = "https://api.whatsapp.com/send?phone=$phone"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Default back to sms or ignore
                            }
                        }
                    }
                }
            }

            // Memory Timeline placeholder
            item {
                Spacer(modifier = Modifier.height(16.dp))
                MemoryTimelinePreview()
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    if (showAddDialog) {
        AddFamilyMemberDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, relation, phone, isEmerg, dob ->
                viewModel.addFamilyMember(name, relation, phone, isEmerg, dob, "", 0xFF2196F3)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun EmergencySOSCard(contacts: List<FamilyMember>, onCall: (String) -> Unit) {
    val isDark = isSystemInDarkTheme()
    val sosBg = if (isDark) Color(0xFF3B1D21) else Color(0xFFFFEBEE)
    val sosRed = if (isDark) Color(0xFFFF8A80) else Color(0xFFD32F2F)
    Surface(
        color = sosBg,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BanglaHeading("জরুরি সাহায্য", color = sosRed, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(8.dp))
            BanglaText("জরুরি প্রয়োজনে নিচে চাপ দিন", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (contacts.isNotEmpty()) {
                        onCall(contacts.first().phone)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = sosRed),
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(30.dp)
            ) {
                Icon(Icons.Filled.Call, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                BanglaHeading("জরুরি কল করুন", color = Color.White, fontSize = 20.sp)
            }
            
            if (contacts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(contacts) { contact ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { onCall(contact.phone) }
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(24.dp).background(Color(contact.avatarColor), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(contact.name.take(1), color = Color.White, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                BanglaText(contact.name, color = sosRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusCheckInCard() {
    var checkedIn by remember { mutableStateOf(false) }
    
    Surface(
        color = if (checkedIn) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                BanglaHeading(if (checkedIn) "আজ চেক-ইন করা হয়েছে" else "আপনি কেমন আছেন?", fontSize = 18.sp, color = if (checkedIn) GreenPrimary else MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                BanglaText("পরিবারের সবাইকে জানাতে নিচে চাপ দিন", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Button(
                onClick = { checkedIn = true },
                colors = ButtonDefaults.buttonColors(containerColor = if (checkedIn) MaterialTheme.colorScheme.outline else GreenPrimary),
                enabled = !checkedIn
            ) {
                Icon(if (checkedIn) Icons.Filled.Check else Icons.Filled.FavoriteBorder, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                BanglaText(if (checkedIn) "ভালো আছি" else "আমি ভালো আছি")
            }
        }
    }
}

@Composable
fun FamilyMemberCard(member: FamilyMember, onContact: (String, String) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(56.dp).background(Color(member.avatarColor), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(member.name.take(1), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BanglaHeading(member.name, fontSize = 18.sp)
                        if (member.isEmergencyContact) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Filled.Warning, contentDescription = "Emergency", tint = sosRed, modifier = Modifier.size(16.dp))
                        }
                    }
                    BanglaText(member.relation, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
            }
            
            if (member.notes.isNotBlank() || member.birthDate.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        if (member.notes.isNotBlank()) {
                            BanglaText("নোট: ${member.notes}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (member.birthDate.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Cake, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFFF06292))
                                Spacer(modifier = Modifier.width(4.dp))
                                BanglaText("জন্মদিন: ${member.birthDate}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { onContact("call", member.phone) }, modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)) {
                    Icon(Icons.Filled.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { onContact("sms", member.phone) }, modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)) {
                    Icon(Icons.Filled.Message, contentDescription = "Message", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { onContact("whatsapp", member.phone) }, modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)) {
                    Icon(Icons.Filled.Chat, contentDescription = "WhatsApp", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun MemoryTimelinePreview() {
    Surface(
        color = Color(0xFFFFF3E0),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = Color(0xFFEF6C00))
                    Spacer(modifier = Modifier.width(8.dp))
                    BanglaHeading("পারিবারিক স্মৃতি", fontSize = 18.sp, color = Color(0xFFEF6C00))
                }
                TextButton(onClick = {}) { BanglaText("সব দেখুন") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            BanglaText("গত ঈদের কিছু চমৎকার মুহূর্ত", fontSize = 14.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f).height(100.dp).background(Color.LightGray, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Image, contentDescription = null, tint = Color.White)
                }
                Box(modifier = Modifier.weight(1f).height(100.dp).background(Color.LightGray, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Image, contentDescription = null, tint = Color.White)
                }
                Box(modifier = Modifier.weight(1f).height(100.dp).background(Color.LightGray, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    BanglaText("+৩", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddFamilyMemberDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, Boolean, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isEmergency by remember { mutableStateOf(false) }
    var dob by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { BanglaHeading("সদস্য যোগ করুন", fontSize = 20.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { BanglaText("নাম") }, singleLine = true)
                OutlinedTextField(value = relation, onValueChange = { relation = it }, label = { BanglaText("সম্পর্ক (যেমন: ছেলে/মেয়ে)") }, singleLine = true)
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { BanglaText("মোবাইল নম্বর") }, singleLine = true)
                OutlinedTextField(value = dob, onValueChange = { dob = it }, label = { BanglaText("জন্মদিন (যেমন: ১২ অক্টোবর)") }, singleLine = true)
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { isEmergency = !isEmergency }) {
                    Checkbox(checked = isEmergency, onCheckedChange = { isEmergency = it })
                    BanglaText("জরুরি কন্টাক্ট হিসেবে সেট করুন")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, relation, phone, isEmergency, dob) },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                enabled = name.isNotBlank() && phone.isNotBlank()
            ) {
                BanglaText("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { BanglaText("বাতিল") }
        }
    )
}
