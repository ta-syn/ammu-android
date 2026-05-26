package com.example.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BanglaHeading
import com.example.ui.components.BanglaText
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var currentStep by remember { mutableStateOf(1) }

    val nextStep: () -> Unit = {
        if (currentStep < 6) {
            currentStep++
        } else {
            onFinish()
        }
    }

    val prevStep: () -> Unit = {
        if (currentStep > 1) {
            currentStep--
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF013E37),
                        Color(0xFF025C52)
                    )
                )
            )
    ) {
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                slideInHorizontally { width -> if (targetState > initialState) width else -width } + fadeIn() with
                slideOutHorizontally { width -> if (targetState > initialState) -width else width } + fadeOut()
            },
            label = "OnboardingTransition"
        ) { step ->
            when (step) {
                1 -> WelcomeScreenStep(onNext = nextStep)
                2 -> ProfileSetupStep(onNext = nextStep, onSkip = nextStep)
                3 -> IslamicPreferencesStep(onNext = nextStep, onSkip = nextStep)
                4 -> NotificationSetupStep(onNext = nextStep, onSkip = nextStep)
                5 -> FamilySetupStep(onNext = nextStep, onSkip = nextStep)
                6 -> ReadyStep(onFinish = onFinish)
            }
        }

        // Pagination Dots
        if (currentStep < 6) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                for (i in 1..5) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (i == currentStep) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (i == currentStep) Color(0xFFFFEFB3) else Color.White.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun WelcomeScreenStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🌙", fontSize = 80.sp, modifier = Modifier.padding(bottom = 16.dp))
        BanglaHeading("আস-সালামু আলাইকুম!", fontSize = 32.sp, color = Color(0xFFFFEFB3), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        BanglaHeading("আম্মু অ্যাপে আপনাকে স্বাগতম", fontSize = 24.sp, color = Color.White, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        BanglaText("আপনার জীবনকে আরও সহজ করতে আমরা এখানে", color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center)
        
        Spacer(modifier = Modifier.height(64.dp))
        
        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEFB3)),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            BanglaText("শুরু করুন →", color = Color(0xFF013E37), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProfileSetupStep(onNext: () -> Unit, onSkip: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("ঢাকা") }
    
    val healthConditions = listOf("ডায়াবেটিস", "উচ্চ রক্তচাপ", "হৃদরোগ", "অন্যান্য", "কোনোটি নয়")
    val selectedConditions = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        BanglaHeading("আপনার পরিচয় দিন", fontSize = 28.sp, color = Color(0xFFFFEFB3))
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { BanglaText("আপনার নাম") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                focusedBorderColor = Color(0xFFFFEFB3),
                unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                focusedLabelColor = Color(0xFFFFEFB3)
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { BanglaText("বয়স") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                focusedBorderColor = Color(0xFFFFEFB3),
                unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                focusedLabelColor = Color(0xFFFFEFB3)
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        BanglaText("স্বাস্থ্য পরিস্থিতি (একাধিক নির্বাচন করা যেতে পারে)", color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        
        healthConditions.forEach { condition ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        if (condition == "কোনোটি নয়") {
                            selectedConditions.clear()
                            selectedConditions.add(condition)
                        } else {
                            selectedConditions.remove("কোনোটি নয়")
                            if (selectedConditions.contains(condition)) {
                                selectedConditions.remove(condition)
                            } else {
                                selectedConditions.add(condition)
                            }
                        }
                    }
            ) {
                Checkbox(
                    checked = selectedConditions.contains(condition),
                    onCheckedChange = null,
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFFEFB3), checkmarkColor = Color(0xFF013E37), uncheckedColor = Color.White)
                )
                Spacer(modifier = Modifier.width(8.dp))
                BanglaText(condition, color = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEFB3)),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            BanglaText("পরবর্তী ধাপ", color = Color(0xFF013E37), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            BanglaText("এড়িয়ে যান", color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun IslamicPreferencesStep(onNext: () -> Unit, onSkip: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        BanglaHeading("আপনার ইসলামিক পছন্দ", fontSize = 28.sp, color = Color(0xFFFFEFB3))
        Spacer(modifier = Modifier.height(16.dp))
        BanglaText("সঠিক নামাজের সময় নির্ধারণের জন্য এই তথ্যগুলো প্রয়োজন", color = Color.White.copy(alpha = 0.8f))
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Surface(color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                BanglaText("মাযহাব / হিসাব পদ্ধতি", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                BanglaText("হানাফি / করাচি (ডিফল্ট: বাংলাদেশ)", color = Color(0xFFFFEFB3))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Surface(color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                BanglaText("আসরের ওয়াক্তের হিসাব", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                BanglaText("হানাফি (বস্তুর দ্বিগুণ ছায়া)", color = Color(0xFFFFEFB3))
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEFB3)),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            BanglaText("পরবর্তী ধাপ", color = Color(0xFF013E37), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            BanglaText("এড়িয়ে যান", color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun NotificationSetupStep(onNext: () -> Unit, onSkip: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🔔", fontSize = 80.sp, modifier = Modifier.padding(bottom = 16.dp))
        BanglaHeading("নোটিফিকেশন চালু করুন", fontSize = 28.sp, color = Color(0xFFFFEFB3), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        BanglaText("নামাজের সময় মনে করিয়ে দেব", color = Color.White, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        BanglaText("ওষুধ খাওয়ার সময় জানাব", color = Color.White, textAlign = TextAlign.Center)
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEFB3)),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            BanglaText("অনুমতি দিন", color = Color(0xFF013E37), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            BanglaText("পরে সেট আপ করুন", color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun FamilySetupStep(onNext: () -> Unit, onSkip: () -> Unit) {
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        BanglaHeading("পরিবারের একজনকে যোগ করুন", fontSize = 28.sp, color = Color(0xFFFFEFB3))
        Spacer(modifier = Modifier.height(8.dp))
        BanglaText("জরুরি অবস্থায় কাকে জানাব?", color = Color.White.copy(alpha = 0.8f))
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = contactName,
            onValueChange = { contactName = it },
            label = { BanglaText("যোগাযোগের ব্যক্তির নাম") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                focusedBorderColor = Color(0xFFFFEFB3),
                unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                focusedLabelColor = Color(0xFFFFEFB3)
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = contactPhone,
            onValueChange = { contactPhone = it },
            label = { BanglaText("ফোন নম্বর") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                focusedBorderColor = Color(0xFFFFEFB3),
                unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                focusedLabelColor = Color(0xFFFFEFB3)
            )
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEFB3)),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            BanglaText("সংরক্ষণ করুন", color = Color(0xFF013E37), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            BanglaText("এড়িয়ে যান", color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun ReadyStep(onFinish: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎉", fontSize = 80.sp, modifier = Modifier.padding(bottom = 16.dp))
        BanglaHeading("সব প্রস্তুত!", fontSize = 36.sp, color = Color(0xFFFFEFB3), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        BanglaText("আপনার প্রোফাইল সেট আপ করা হয়েছে।", color = Color.White, textAlign = TextAlign.Center, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        BanglaText("এখন থেকে আম্মু অ্যাপ হবে আপনার প্রতিদিনের সঙ্গী।", color = Color.White, textAlign = TextAlign.Center, fontSize = 16.sp)
        
        Spacer(modifier = Modifier.height(64.dp))
        
        Button(
            onClick = onFinish,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEFB3)),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            BanglaText("আম্মু অ্যাপ শুরু করুন", color = Color(0xFF013E37), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
