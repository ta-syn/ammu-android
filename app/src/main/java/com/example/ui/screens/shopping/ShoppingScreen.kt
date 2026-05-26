package com.example.ui.screens.shopping

import android.app.Activity
import android.content.Intent
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BanglaHeading
import com.example.ui.components.BanglaText
import com.example.ui.theme.GreenPrimary
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(viewModel: ShoppingViewModel = viewModel()) {
    val allLists by viewModel.activeLists.collectAsState()
    var selectedListId by remember { mutableIntStateOf(-1) }
    
    // Automatically create a default list if empty
    LaunchedEffect(allLists) {
        if (allLists.isEmpty()) {
            viewModel.createList("সাপ্তাহিক বাজার")
        } else if (selectedListId == -1) {
            selectedListId = allLists.first().id
        }
    }

    val selectedList = allLists.find { it.id == selectedListId }

    var isMarketMode by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Market mode forces screen brightness and wake lock
    DisposableEffect(isMarketMode) {
        val activity = context as? Activity
        if (isMarketMode) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            val params = activity?.window?.attributes
            params?.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
            activity?.window?.attributes = params
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            val params = activity?.window?.attributes
            params?.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            activity?.window?.attributes = params
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            val params = activity?.window?.attributes
            params?.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            activity?.window?.attributes = params
        }
    }

    var showAiSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { BanglaHeading("বাজার তালিকা", fontSize = 20.sp) },
                actions = {
                    IconButton(onClick = { isMarketMode = !isMarketMode }) {
                        Icon(
                            if (isMarketMode) Icons.Filled.WbTwilight else Icons.Filled.WbSunny,
                            contentDescription = "Market Mode",
                            tint = if (isMarketMode) Color(0xFFF9A825) else Color.Gray
                        )
                    }
                    IconButton(onClick = {
                        val text = selectedList?.items?.joinToString("\n") { "✓ ${it.name} ${it.quantity} ${it.unit}" } ?: ""
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "বাজার তালিকা:\n$text")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedList != null) {
                FloatingActionButton(
                    onClick = { showAiSheet = true },
                    containerColor = GreenPrimary
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = "AI", tint = Color.White)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isMarketMode) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // Lists tabs
            LazyRow(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allLists) { list ->
                    FilterChip(
                        selected = list.id == selectedListId,
                        onClick = { selectedListId = list.id },
                        label = { BanglaText(list.title, fontSize = if (isMarketMode) 16.sp else 14.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.createList("নতুন তালিকা") },
                        label = { BanglaText("+ নতুন", color = GreenPrimary) }
                    )
                }
            }

            if (selectedList != null) {
                // Add Quick Item
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable { showAddDialog = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.AddCircleOutline, contentDescription = null, tint = GreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        BanglaText("দ্রুত যোগ করুন (প্রোডাক্টের নাম)", color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Items list grouped by category or check status
                val items = selectedList.items
                val pendingItems = items.filter { !it.isChecked }
                val checkedItems = items.filter { it.isChecked }
                
                val groupedPending = pendingItems.groupBy { it.category }

                LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                    groupedPending.forEach { (category, categoryItems) ->
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(color = Color(0xFFE3F2FD), shape = RoundedCornerShape(8.dp)) {
                                BanglaText(
                                    text = category,
                                    fontSize = if (isMarketMode) 18.sp else 14.sp,
                                    color = Color(0xFF1976D2),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        items(categoryItems) { item ->
                            ShoppingItemRow(
                                item = item,
                                isMarketMode = isMarketMode,
                                onCheckToggle = { viewModel.toggleItemCheck(selectedList.id, item.id) },
                                onDelete = { viewModel.deleteItem(selectedList.id, item.id) }
                            )
                        }
                    }

                    if (checkedItems.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            BanglaText("নেওয়া হয়েছে (${checkedItems.size})", color = Color.Gray, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(checkedItems) { item ->
                            ShoppingItemRow(
                                item = item,
                                isMarketMode = isMarketMode,
                                onCheckToggle = { viewModel.toggleItemCheck(selectedList.id, item.id) },
                                onDelete = { viewModel.deleteItem(selectedList.id, item.id) }
                            )
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }
    }

    if (showAddDialog && selectedList != null) {
        QuickAddDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, quantity, unit, category ->
                viewModel.addItemToList(selectedList.id, name, quantity, unit, category)
                showAddDialog = false
            }
        )
    }

    if (showAiSheet && selectedList != null) {
        ModalBottomSheet(
            onDismissRequest = { showAiSheet = false },
            sheetState = sheetState
        ) {
            AiShoppingAssistant(
                viewModel = viewModel,
                onClose = { showAiSheet = false },
                onAddItems = { items ->
                    items.forEach {
                        viewModel.addItemToList(selectedList.id, it.name, it.quantity, it.unit, it.category)
                    }
                    showAiSheet = false
                }
            )
        }
    }
}

@Composable
fun ShoppingItemRow(
    item: ShoppingItem,
    isMarketMode: Boolean,
    onCheckToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onCheckToggle)
    ) {
        Row(
            modifier = Modifier.padding(if (isMarketMode) 20.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { onCheckToggle() },
                colors = CheckboxDefaults.colors(checkedColor = GreenPrimary)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                BanglaText(
                    text = item.name,
                    fontSize = if (isMarketMode) 22.sp else 16.sp,
                    fontWeight = if (isMarketMode) FontWeight.Bold else FontWeight.Normal,
                    color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                )
                if (item.quantity.isNotBlank() || item.unit.isNotBlank()) {
                    BanglaText(
                        text = "${item.quantity} ${item.unit}",
                        fontSize = if (isMarketMode) 16.sp else 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Close, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun QuickAddDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { BanglaHeading("নতুন পণ্য", fontSize = 20.sp) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { BanglaText("নাম (যেমন: গরুর মাংস ১ কেজি)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                val quickChips = listOf("পিঁয়াজ", "রসুন", "আদা", "মরিচ", "লবণ", "তেল", "ডিম", "চাল")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickChips) { chip ->
                        AssistChip(
                            onClick = { name = chip },
                            label = { BanglaText(chip) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    // Simple parse if user types "গরুর মাংস ১ কেজি"
                    // Real app would use AI or Regex
                    onAdd(name, "", "", "সাধারণ") 
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                BanglaText("যোগ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                BanglaText("বাতিল")
            }
        }
    )
}

// ---------------- AI Generator Simulation ---------------- //

@Composable
fun AiShoppingAssistant(
    viewModel: ShoppingViewModel,
    onClose: () -> Unit,
    onAddItems: (List<ShoppingItem>) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var step by remember { mutableIntStateOf(0) }
    
    val isGenerating by viewModel.isGenerating.collectAsState()
    val generatedItems by viewModel.generatedItems.collectAsState()
    val error by viewModel.generationError.collectAsState()

    LaunchedEffect(step) {
        if (step == 1 && query.isNotBlank()) {
            viewModel.generateShoppingList(query)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearGeneratedItems()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = GreenPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            BanglaHeading("রান্নার জন্য AI সাহায্য", fontSize = 22.sp, color = GreenPrimary)
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        if (step == 0) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { BanglaText("কী রান্না করবেন লিখুন...") },
                placeholder = { BanglaText("যেমন: :মুরগি ভুনা আর খিচুড়ি (৪ জনের জন্য)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { if (query.isNotBlank()) step = 1 },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                BanglaText("উপকরণ তালিকা তৈরি করুন")
            }
        } else if (isGenerating) {
            CircularProgressIndicator(color = GreenPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            BanglaText("আপনার জন্য উপকরণ তালিকা তৈরি হচ্ছে...")
        } else if (error != null) {
            BanglaText(text = error!!, color = Color.Red)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onClose) { BanglaText("বন্ধ করুন") }
        } else if (generatedItems.isNotEmpty()) {
            BanglaText("নিচের উপকরণগুলো লাগবে:", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Color(0xFFF9FAFB),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    generatedItems.forEach { item ->
                        val qtyStr = if (item.quantity.isNotBlank()) " - ${item.quantity} ${item.unit}" else ""
                        BanglaText("• ${item.name}$qtyStr", fontSize = 14.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = onClose, modifier = Modifier.weight(1f)) {
                    BanglaText("বাতিল")
                }
                Button(
                    onClick = { onAddItems(generatedItems) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    BanglaText("পুরো তালিকায় যোগ করুন")
                }
            }
        }
    }
}
