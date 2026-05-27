package com.example.ui.screens.family

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.FamilyMember
import com.example.ui.components.BanglaHeading
import com.example.ui.components.BanglaText

val AVATAR_COLORS = listOf(
    0xFF2196F3, // Classic Blue
    0xFF4CAF50, // Islamic Green
    0xFFE91E63, // Soft Pink
    0xFF9C27B0, // Royal Purple
    0xFFFF9800, // Vibrant Orange
    0xFF00BCD4, // Teal Blue
    0xFF9E9D24, // Lime Green
    0xFF795548  // Warm Brown
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyScreen(viewModel: FamilyViewModel = viewModel()) {
    val familyMembers by viewModel.familyMembers.collectAsState()
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var memberToEdit by remember { mutableStateOf<FamilyMember?>(null) }
    var memberToDelete by remember { mutableStateOf<FamilyMember?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    memberToEdit = null
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Member", tint = MaterialTheme.colorScheme.onPrimary)
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
                    if (phone.isNotBlank()) {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                        context.startActivity(intent)
                    }
                }
            }

            // Status Check-in
            item {
                StatusCheckInCard()
            }

            // Family Members list/grid
            if (familyMembers.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    BanglaHeading("পরিবারের সদস্য", fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                items(familyMembers) { member ->
                    FamilyMemberCard(
                        member = member,
                        onContact = { action, phone ->
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
                        },
                        onEdit = {
                            memberToEdit = member
                            showAddDialog = true
                        },
                        onDelete = {
                            memberToDelete = member
                        }
                    )
                }
            } else {
                // Family list empty state
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("👨‍👩‍👧‍👦", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            BanglaHeading("কোনো সদস্য নেই", fontSize = 20.sp, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(8.dp))
                            BanglaText(
                                "আপনার পরিবারের সদস্যদের যোগ করুন যাতে জরুরি মুহূর্তে সহজে যোগাযোগ করতে পারেন।",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    memberToEdit = null
                                    showAddDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                BanglaText("সদস্য যোগ করুন", color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    if (showAddDialog) {
        AddFamilyMemberDialog(
            memberToEdit = memberToEdit,
            onDismiss = {
                showAddDialog = false
                memberToEdit = null
            },
            onSave = { name, relation, phone, isEmerg, dob, notes, color ->
                viewModel.addFamilyMember(
                    id = memberToEdit?.id ?: 0,
                    name = name,
                    relation = relation,
                    phone = phone,
                    isEmergency = isEmerg,
                    birthDate = dob,
                    notes = notes,
                    color = color
                )
                showAddDialog = false
                memberToEdit = null
            }
        )
    }

    if (memberToDelete != null) {
        AlertDialog(
            onDismissRequest = { memberToDelete = null },
            title = { BanglaHeading("সদস্য বাদ দিন", fontSize = 20.sp) },
            text = {
                BanglaText("আপনি কি নিশ্চিতভাবে '${memberToDelete?.name}' কে আপনার পরিবারের তালিকা থেকে বাদ দিতে চান?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        memberToDelete?.let { viewModel.deleteMember(it) }
                        memberToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    BanglaText("হ্যাঁ, বাদ দিন", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToDelete = null }) {
                    BanglaText("বাতিল")
                }
            }
        )
    }
}

@Composable
fun EmergencySOSCard(contacts: List<FamilyMember>, onCall: (String) -> Unit) {
    val isDark = isSystemInDarkTheme()
    val sosBg = if (isDark) Color(0xFF3B1D21) else Color(0xFFFFEBEE)
    val sosRed = if (isDark) Color(0xFFFF8A80) else Color(0xFFD32F2F)
    val context = LocalContext.current
    
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
            
            if (contacts.isEmpty()) {
                BanglaText(
                    "জরুরি মুহূর্তে দ্রুত যোগাযোগ করার জন্য সদস্য যোগ করার সময় 'জরুরি কন্টাক্ট' অপশনটি চালু করুন।",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        Toast.makeText(context, "অনুগ্রহ করে প্রথমে একজন সদস্য যোগ করে তাকে জরুরি কন্টাক্ট হিসেবে সেট করুন", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = sosRed.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Icon(Icons.Filled.Call, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.width(8.dp))
                    BanglaHeading("জরুরি কল (সেট করা নেই)", color = Color.White.copy(alpha = 0.6f), fontSize = 18.sp)
                }
            } else {
                BanglaText("জরুরি প্রয়োজনে নিচে চাপ দিন", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        onCall(contacts.first().phone)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = sosRed),
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Icon(Icons.Filled.Call, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    BanglaHeading("জরুরি কল করুন", color = Color.White, fontSize = 20.sp)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(contacts) { contact ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { onCall(contact.phone) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(Color(contact.avatarColor), CircleShape),
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
    val primaryColor = MaterialTheme.colorScheme.primary
    
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
                BanglaHeading(
                    if (checkedIn) "আজ চেক-ইন করা হয়েছে" else "আপনি কেমন আছেন?",
                    fontSize = 18.sp,
                    color = if (checkedIn) primaryColor else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                BanglaText("পরিবারের সবাইকে জানাতে নিচে চাপ দিন", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Button(
                onClick = { checkedIn = true },
                colors = ButtonDefaults.buttonColors(containerColor = if (checkedIn) MaterialTheme.colorScheme.outline else primaryColor),
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
fun FamilyMemberCard(
    member: FamilyMember,
    onContact: (String, String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val sosRed = if (isDark) Color(0xFFFF8A80) else Color(0xFFD32F2F)
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(member.avatarColor), CircleShape),
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
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit Member",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete Member",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(22.dp)
                    )
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
                            if (member.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFamilyMemberDialog(
    memberToEdit: FamilyMember? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Boolean, String, String, Long) -> Unit
) {
    var name by remember { mutableStateOf(memberToEdit?.name ?: "") }
    var relation by remember { mutableStateOf(memberToEdit?.relation ?: "") }
    var phone by remember { mutableStateOf(memberToEdit?.phone ?: "") }
    var isEmergency by remember { mutableStateOf(memberToEdit?.isEmergencyContact ?: false) }
    var dob by remember { mutableStateOf(memberToEdit?.birthDate ?: "") }
    var notes by remember { mutableStateOf(memberToEdit?.notes ?: "") }
    var selectedColor by remember { mutableStateOf(memberToEdit?.avatarColor ?: AVATAR_COLORS.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BanglaHeading(if (memberToEdit == null) "সদস্য যোগ করুন" else "তথ্য সংশোধন করুন", fontSize = 20.sp)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { BanglaText("নাম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = relation,
                    onValueChange = { relation = it },
                    label = { BanglaText("সম্পর্ক (যেমন: ছেলে/মেয়ে/মা/বাবা)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { BanglaText("মোবাইল নম্বর") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    label = { BanglaText("জন্মদিন (যেমন: ১২ অক্টোবর)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { BanglaText("নোট/বিশেষ দ্রষ্টব্য (ঐচ্ছিক)") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                BanglaText("অ্যাভাটার রঙ নির্বাচন করুন", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AVATAR_COLORS.forEach { colorVal ->
                        val color = Color(colorVal)
                        val isSelected = selectedColor == colorVal
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = colorVal }
                                .then(
                                    if (isSelected) {
                                        Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    } else {
                                        Modifier
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isEmergency = !isEmergency }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(checked = isEmergency, onCheckedChange = { isEmergency = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    BanglaText("জরুরি কন্টাক্ট হিসেবে সেট করুন")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, relation, phone, isEmergency, dob, notes, selectedColor) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = name.isNotBlank() && phone.isNotBlank() && relation.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                BanglaText(if (memberToEdit == null) "সংরক্ষণ করুন" else "আপডেট করুন", color = Color.White)
            }
        }
    )
}
