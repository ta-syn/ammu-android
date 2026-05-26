package com.example.ui.screens.qibla

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import com.example.ui.utils.toBengaliNumber
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BanglaHeading
import com.example.ui.components.BanglaText
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlin.math.roundToInt

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QiblaScreen(viewModel: QiblaViewModel = viewModel()) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var showCalibrationGuide by remember { mutableStateOf(false) }

    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.startSensors()
                if (locationPermissions.allPermissionsGranted) {
                    viewModel.fetchLocation()
                }
            } else if (event == Lifecycle.Event.ON_STOP) {
                viewModel.stopSensors()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            viewModel.fetchLocation()
        }
    }

    val azimuth by viewModel.azimuth.collectAsState()
    val qiblaDirection by viewModel.qiblaDirection.collectAsState()
    val distance by viewModel.distance.collectAsState()
    val locationName by viewModel.locationName.collectAsState()
    val hasSensors by viewModel.hasSensors.collectAsState()

    // Smooth animation for compass
    val animatedAzimuth by animateFloatAsState(
        targetValue = azimuth,
        animationSpec = tween(durationMillis = 200),
    )

    val diff = ((qiblaDirection - animatedAzimuth + 360) % 360).roundToInt()
    val isAligned = diff < 6 || diff > 354

    val context = LocalContext.current
    LaunchedEffect(isAligned) {
        if (isAligned) {
            try {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(80)
                }
            } catch (e: Exception) {
                // Ignore if permission or hardware fails
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C1917)) // Clean premium dark Islamic theme background
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        BanglaHeading(text = "কিবলা কম্পাস 🧭", color = Color.White, fontSize = 26.sp)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = GoldLight, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            BanglaText(text = locationName, color = Color.LightGray)
        }
        
        BanglaText(text = "মক্কা মুকাররমা: ${toBengaliNumber(distance.toString())} কি.মি.", color = Color.Gray, fontSize = 14.sp)

        // Sensor Availability warning
        if (!hasSensors) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        BanglaText(
                            text = "ম্যাগনেটিক সেন্সর পাওয়া যায়নি!",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        BanglaText(
                            text = "আপনার ডিভাইসে দিকনির্ণয় করার কম্পাস সেন্সর নেই। অনুগ্রহ করে আনুমানিক ২৯২° কোণ মেপে কিবলা নির্ধারণ করুন।",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        if (!locationPermissions.allPermissionsGranted) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { locationPermissions.launchMultiplePermissionRequest() },
                colors = ButtonDefaults.buttonColors(containerColor = GoldLight, contentColor = Color.Black)
            ) {
                BanglaText(text = "সঠিক দূরত্বের জন্য লোকেশন পারমিশন দিন")
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))

        // Compass UI
        Box(
            modifier = Modifier
                .size(280.dp)
                .background(if (isAligned) Color(0xFF0C2B1C) else Color(0xFF132A26), CircleShape), // Dark green nested circle, turns darker emerald when aligned
            contentAlignment = Alignment.Center
        ) {
            // Outer Ring
            Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                val radius = size.width / 2
                drawCircle(
                    color = if (isAligned) GreenLight.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.1f),
                    radius = radius,
                    style = Stroke(width = if (isAligned) 6.dp.toPx() else 4.dp.toPx())
                )
                
                // Tick marks
                for (i in 0 until 360 step 15) {
                    val angle = Math.toRadians(i.toDouble() - 90)
                    val isCardinal = i % 90 == 0
                    val length = if (isCardinal) 18.dp.toPx() else 8.dp.toPx()
                    val strokeW = if (isCardinal) 3.dp.toPx() else 1.5.dp.toPx()
                    
                    val startX = center.x + (radius - length) * Math.cos(angle).toFloat()
                    val startY = center.y + (radius - length) * Math.sin(angle).toFloat()
                    val endX = center.x + radius * Math.cos(angle).toFloat()
                    val endY = center.y + radius * Math.sin(angle).toFloat()
                    
                    drawLine(
                        color = if (i == 0) Color.Red else Color.Gray.copy(alpha = 0.5f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                }
            }
            
            // Rotating internal compass
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(-animatedAzimuth), // Rotate opposite to device to keep North pointing Up
                contentAlignment = Alignment.Center
            ) {
                // North Indicator inside
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                    Text("N", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                }

                // South/East/West labels
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Text("S", color = Color.White.copy(alpha = 0.5f), fontSize = 16.sp, modifier = Modifier.align(Alignment.BottomCenter))
                    Text("E", color = Color.White.copy(alpha = 0.5f), fontSize = 16.sp, modifier = Modifier.align(Alignment.CenterEnd))
                    Text("W", color = Color.White.copy(alpha = 0.5f), fontSize = 16.sp, modifier = Modifier.align(Alignment.CenterStart))
                }

                // Qibla Indicator Arrow
                Canvas(modifier = Modifier.fillMaxSize()) {
                    rotate(qiblaDirection) {
                        val path = Path().apply {
                            moveTo(center.x, center.y - (size.height / 2) + 20.dp.toPx())
                            lineTo(center.x - 12.dp.toPx(), center.y - (size.height / 2) + 45.dp.toPx())
                            lineTo(center.x + 12.dp.toPx(), center.y - (size.height / 2) + 45.dp.toPx())
                            close()
                        }
                        drawPath(path, color = GoldLight)
                    }
                }
                
                // Center Icon / Kaaba
                Surface(
                    shape = CircleShape,
                    color = Color.Black,
                    border = BorderStroke(2.dp, GoldLight),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Drawing a simple Kaaba rect if SVG not available
                        Canvas(modifier = Modifier.size(20.dp)) {
                            drawRect(color = Color(0xFF1E1E1E))
                            // Gold strip
                            drawRect(
                                color = GoldLight,
                                topLeft = Offset(0f, size.height * 0.2f),
                                size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.15f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))

        // Compass degree, bearing and deviation guide info
        
        
        Surface(
            color = if (isAligned) Color(0xFF0D3C26) else Color(0xFF1E292B), // Green if aligned, grey-blue otherwise
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BanglaText(text = "কিবলার কোণ: ${toBengaliNumber(qiblaDirection.roundToInt().toString())}°", color = Color.LightGray)
                Spacer(modifier = Modifier.height(4.dp))
                
                if (isAligned) {
                    BanglaHeading(text = "আপনি সঠিক দিকে আছেন! 🕋", color = GreenLight, fontSize = 18.sp)
                } else {
                    val angleToTurn = if (diff <= 180) diff else 360 - diff
                    val turnDirection = if (diff <= 180) "ডানে" else "বামে"
                    BanglaHeading(
                        text = "আরেকটু $turnDirection ${toBengaliNumber(angleToTurn.toString())}° ঘুরুন",
                        color = GoldLight,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Expandable figure-8 calibration guide box
        Surface(
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showCalibrationGuide = !showCalibrationGuide }
                .padding(vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.ScreenRotation, contentDescription = null, tint = GoldLight, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    BanglaText(text = "কম্পাস কাজ না করলে করণীয়...", color = Color.White, fontSize = 13.sp)
                }
                
                AnimatedVisibility(visible = showCalibrationGuide) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 8.dp))
                        BanglaText(
                            text = "১. ফোনটি ফ্ল্যাট বা সোজা সমতলে রাখুন।\n২. ফোনটিকে বাতাসে ইংরেজি '৮' (8) অক্ষরের মতো করে ৩-৪ বার ঘুরিয়ে ক্যালিব্রেট করুন।\n৩. আপনার আশেপাশের লোহা বা ইলেকট্রনিক্স ডিভাইস থেকে দূরে থাকুন।",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
