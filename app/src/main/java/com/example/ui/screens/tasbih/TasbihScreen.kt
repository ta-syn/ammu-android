package com.example.ui.screens.tasbih

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    TasbihType("لَا إِلَهَ إِلَّا اللهُ", "লা ইলাহা ইল্লাল্লাহ", 100),
    TasbihType("صَلَّى اللهُ عَلَيْهِ وَسَلَّمَ", "দরুদ শরীফ", 100)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihScreen() {
    val context = LocalContext.current
    var selectedTasbih by remember { mutableStateOf(tasbihTypes.first()) }
    var count by remember { mutableIntStateOf(0) }
    var completedRounds by remember { mutableIntStateOf(0) }
    
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
            tasbihTypes.forEachIndexed { index, tasbih ->
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
            verticalArrangement = Arrangement.Center
        ) {
            
            // Current Text
            Text(
                text = selectedTasbih.titleAr,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            BanglaText(
                text = selectedTasbih.titleBn,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            // Main Counter Button
            var isTapped by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(
                targetValue = if (isAnimatingTarget) 1.05f else if (isTapped) 0.95f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "scale"
            )

            // Progress outline calculation
            val progress = count.toFloat() / selectedTasbih.target

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(260.dp)
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

                // Fill Track
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 12.dp,
                    trackColor = Color.Transparent
                )

                // The Button
                Surface(
                    shape = CircleShape,
                    color = if (count == selectedTasbih.target) GreenPrimary else MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    onClick = {
                        if (count < selectedTasbih.target) {
                            count++
                            coroutineScope.launch {
                                isTapped = true
                                delay(50)
                                isTapped = false
                            }
                            val vibrationEffect = VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
                            vibrator.vibrate(vibrationEffect)
                            
                            if (count == selectedTasbih.target) {
                                completedRounds++
                                coroutineScope.launch {
                                    isAnimatingTarget = true
                                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100), -1))
                                    delay(300)
                                    isAnimatingTarget = false
                                    delay(800)
                                    count = 0
                                }
                            }
                        }
                    },
                    modifier = Modifier.size(220.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = toBengaliNumber(count.toString()),
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (count == selectedTasbih.target) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Count info and reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    BanglaText(text = "লক্ষ্য: ${toBengaliNumber(selectedTasbih.target.toString())}", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    BanglaText(text = "সম্পন্ন রাউন্ড: ${toBengaliNumber(completedRounds.toString())}", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                }

                FilledTonalButton(onClick = { count = 0 }) {
                    BanglaText(text = "রিসেট")
                }
            }
        }
    }
}

fun toBengaliNumber(number: String): String {
    val bengaliDigits = arrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    val builder = StringBuilder()
    for (char in number) {
        if (char in '0'..'9') {
            builder.append(bengaliDigits[char - '0'])
        } else {
            builder.append(char)
        }
    }
    return builder.toString()
}
