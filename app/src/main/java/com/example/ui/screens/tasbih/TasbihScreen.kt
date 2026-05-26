package com.example.ui.screens.tasbih

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BanglaHeading
import com.example.ui.components.BanglaText
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GreenPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class TasbihType(val titleAr: String, val titleBn: String, val target: Int)

val tasbihTypes = listOf(
    TasbihType("سُبْحَانَ اللهِ", "সুবহানাল্লাহ", 33),
    TasbihType("الْحَمْدُ لِلَّهِ", "আলহামদুলিল্লাহ", 33),
    TasbihType("اللهُ أَكْبَرُ", "আল্লাহু আকবার", 34),
    TasbihType("أَسْتَغْفِرُ اللَّهَ", "আস্তাগফিরুল্লাহ", 100),
    TasbihType("لَا إِلَهَ إِلَّا اللهُ", "লা ইলাহা ইল্লাল্লাহ", 100),
    TasbihType("لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ", "লা হাওলা...", 100),
    TasbihType("صَلَّى اللهُ عَلَيْهِ وَسَلَّمَ", "দরুদ শরীফ", 100)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihScreen() {
    val context = LocalContext.current
    var selectedTasbih by remember { mutableStateOf(tasbihTypes.first()) }
    var count by remember { mutableIntStateOf(0) }
    var completedRounds by remember { mutableIntStateOf(0) }
    
    // Custom target & settings
    var customTargetEnabled by remember { mutableStateOf(false) }
    var customTargetValue by remember { mutableStateOf("") }
    var isUnlimited by remember { mutableStateOf(false) }
    var isVibrationEnabled by remember { mutableStateOf(true) }
    
    // Dialogs
    var showCustomTargetDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    
    val activeTarget = when {
        isUnlimited -> Int.MAX_VALUE
        customTargetEnabled -> customTargetValue.toIntOrNull() ?: 100
        else -> selectedTasbih.target
    }

    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    val coroutineScope = rememberCoroutineScope()
    var isAnimatingTarget by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Types selector
        ScrollableTabRow(
            selectedTabIndex = tasbihTypes.indexOf(selectedTasbih),
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            tasbihTypes.forEach { tasbih ->
                Tab(
                    selected = selectedTasbih == tasbih,
                    onClick = { 
                        selectedTasbih = tasbih
                        count = 0
                    },
                    text = {
                        BanglaText(
                            text = tasbih.titleBn,
                            fontWeight = if (selectedTasbih == tasbih) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTasbih == tasbih) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Control Settings Row (Vibration & Target Options)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vibration Toggle Button
                IconButton(onClick = { isVibrationEnabled = !isVibrationEnabled }) {
                    Icon(
                        imageVector = if (isVibrationEnabled) Icons.Filled.Vibration else Icons.Filled.VolumeMute,
                        contentDescription = "Vibration",
                        tint = if (isVibrationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Target selection pills
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Standard Target
                    SuggestionChip(
                        onClick = {
                            customTargetEnabled = false
                            isUnlimited = false
                            count = 0
                        },
                        label = { BanglaText("ডিফল্ট (${selectedTasbih.target})", fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (!customTargetEnabled && !isUnlimited) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                    )
                    // Custom Target
                    SuggestionChip(
                        onClick = { showCustomTargetDialog = true },
                        label = { BanglaText(if (customTargetEnabled) "কাস্টম ($customTargetValue)" else "কাস্টম...", fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (customTargetEnabled && !isUnlimited) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                    )
                    // Unlimited Target
                    SuggestionChip(
                        onClick = {
                            isUnlimited = true
                            customTargetEnabled = false
                            count = 0
                        },
                        label = { BanglaText("অসীম ∞", fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (isUnlimited) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                // Current Arabic and Bengali title
                Text(
                    text = selectedTasbih.titleAr,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )
                BanglaText(
                    text = selectedTasbih.titleBn,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(36.dp))

                // Main Counter Circle
                var isTapped by remember { mutableStateOf(false) }
                val scale by animateFloatAsState(
                    targetValue = if (isAnimatingTarget) 1.05f else if (isTapped) 0.95f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "scale"
                )

                val progress = if (isUnlimited) 0.5f else count.toFloat() / activeTarget

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(240.dp)
                        .scale(scale)
                ) {
                    // Background Track
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        strokeWidth = 12.dp,
                        trackColor = Color.Transparent,
                    )

                    // Fill Track (only show if not in unlimited mode)
                    if (!isUnlimited) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 12.dp,
                            trackColor = Color.Transparent
                        )
                    }

                    // Count Button Surface
                    Surface(
                        shape = CircleShape,
                        color = if (count == activeTarget && !isUnlimited) GreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        shadowElevation = 6.dp,
                        onClick = {
                            if (count < activeTarget) {
                                count++
                                coroutineScope.launch {
                                    isTapped = true
                                    delay(50)
                                    isTapped = false
                                }
                                
                                // Trigger haptic vibration if enabled
                                if (isVibrationEnabled) {
                                    val vibrationEffect = VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
                                    vibrator.vibrate(vibrationEffect)
                                }
                                
                                if (count == activeTarget && !isUnlimited) {
                                    completedRounds++
                                    coroutineScope.launch {
                                        isAnimatingTarget = true
                                        if (isVibrationEnabled) {
                                            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100), -1))
                                        }
                                        delay(300)
                                        isAnimatingTarget = false
                                        delay(800)
                                        count = 0
                                    }
                                }
                            }
                        },
                        modifier = Modifier.size(200.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = toBengaliNumber(count.toString()),
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (count == activeTarget && !isUnlimited) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Info bar and Reset actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val targetText = if (isUnlimited) "অসীম (∞)" else toBengaliNumber(activeTarget.toString())
                    BanglaText(text = "লক্ষ্য: $targetText", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    BanglaText(text = "সম্পন্ন রাউন্ড: ${toBengaliNumber(completedRounds.toString())}", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                }

                FilledTonalButton(
                    onClick = { showResetDialog = true },
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    BanglaText(text = "রিসেট")
                }
            }
        }
    }

    // Dialog: Custom Target Setup
    if (showCustomTargetDialog) {
        AlertDialog(
            onDismissRequest = { showCustomTargetDialog = false },
            title = { BanglaHeading("কাস্টম লক্ষ্য সেট করুন", fontSize = 20.sp) },
            text = {
                Column {
                    BanglaText("আপনি কতবার তসবিহ পড়তে চান সংখ্যাটি লিখুন:")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customTargetValue,
                        onValueChange = { customTargetValue = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { BanglaText("যেমন: ১০০, ৫০০, ১০০০") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = customTargetValue.toIntOrNull()
                        if (num != null && num > 0) {
                            customTargetEnabled = true
                            isUnlimited = false
                            count = 0
                            showCustomTargetDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    BanglaText("ওকে")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTargetDialog = false }) {
                    BanglaText("বাতিল")
                }
            }
        )
    }

    // Dialog: Reset Confirmation
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { BanglaHeading("আপনি কি নিশ্চিত?", fontSize = 20.sp) },
            text = { BanglaText("রিসেট করলে আপনার বর্তমান গণনা এবং সম্পন্ন রাউন্ডের সংখ্যা শূন্য (০) হয়ে যাবে।") },
            confirmButton = {
                Button(
                    onClick = {
                        count = 0
                        completedRounds = 0
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    BanglaText("হ্যাঁ, রিসেট করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    BanglaText("বাতিল")
                }
            }
        )
    }
}
