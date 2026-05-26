package com.example.ui.screens.qibla

import android.Manifest
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BanglaHeading
import com.example.ui.components.BanglaText
import com.example.ui.theme.GoldLight
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlin.math.roundToInt

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QiblaScreen(viewModel: QiblaViewModel = viewModel()) {
    val lifecycleOwner = LocalLifecycleOwner.current

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

    // Smooth animation for compass
    val animatedAzimuth by animateFloatAsState(
        targetValue = azimuth,
        animationSpec = tween(durationMillis = 200),
        label = "azimuth"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Dark space-like background
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        BanglaHeading(text = "কিবলা কম্পাস", color = Color.White, fontSize = 28.sp)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = GoldLight, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            BanglaText(text = locationName, color = Color.LightGray)
        }
        
        BanglaText(text = "মক্কা মুকাররমা: ${toBengaliNumber(distance.toString())} কি.মি.", color = Color.Gray, fontSize = 14.sp)

        if (!locationPermissions.allPermissionsGranted) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { locationPermissions.launchMultiplePermissionRequest() },
                colors = ButtonDefaults.buttonColors(containerColor = GoldLight, contentColor = Color.Black)
            ) {
                BanglaText(text = "লোকেশন পারমিশন দিন")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Compass UI
        Box(
            modifier = Modifier
                .size(320.dp)
                .background(Color(0xFF1E293B), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Outer Ring
            Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                val radius = size.width / 2
                drawCircle(
                    color = Color.LightGray.copy(alpha = 0.2f),
                    radius = radius,
                    style = Stroke(width = 4.dp.toPx())
                )
                
                // Tick marks
                for (i in 0 until 360 step 15) {
                    val angle = Math.toRadians(i.toDouble() - 90)
                    val isCardinal = i % 90 == 0
                    val length = if (isCardinal) 20.dp.toPx() else 10.dp.toPx()
                    val strokeW = if (isCardinal) 4.dp.toPx() else 2.dp.toPx()
                    
                    val startX = center.x + (radius - length) * Math.cos(angle).toFloat()
                    val startY = center.y + (radius - length) * Math.sin(angle).toFloat()
                    val endX = center.x + radius * Math.cos(angle).toFloat()
                    val endY = center.y + radius * Math.sin(angle).toFloat()
                    
                    drawLine(
                        color = if (i == 0) Color.Red else Color.Gray,
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
                    Text("N", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                }

                // Qibla Indicator Arrow
                Canvas(modifier = Modifier.fillMaxSize()) {
                    rotate(qiblaDirection) {
                        val path = Path().apply {
                            moveTo(center.x, center.y - (size.height / 2) + 20.dp.toPx())
                            lineTo(center.x - 15.dp.toPx(), center.y - (size.height / 2) + 50.dp.toPx())
                            lineTo(center.x + 15.dp.toPx(), center.y - (size.height / 2) + 50.dp.toPx())
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
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Drawing a simple Kaaba rect if SVG not available
                        Canvas(modifier = Modifier.size(24.dp)) {
                            drawRect(color = Color.DarkGray)
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

        Spacer(modifier = Modifier.weight(1f))
        
        // Qibla direction info
        Surface(
            color = Color(0xFF1E293B).copy(alpha = 0.8f),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BanglaText(text = "কিবলা দিক", color = Color.White)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = toBengaliNumber(qiblaDirection.roundToInt().toString()),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldLight
                    )
                    Text(text = "°", fontSize = 24.sp, color = GoldLight)
                }
                
                val currentDirectionDiff = Math.abs((animatedAzimuth - qiblaDirection + 540) % 360 - 180)
                if (currentDirectionDiff < 5f) {
                    Spacer(modifier = Modifier.height(8.dp))
                    BanglaText(text = "আপনি সঠিক দিকে আছেন!", color = Color.Green, fontSize = 14.sp)
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
