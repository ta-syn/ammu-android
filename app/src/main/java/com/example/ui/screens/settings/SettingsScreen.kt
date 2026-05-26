package com.example.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BanglaHeading
import com.example.ui.components.BanglaText
import com.example.ui.theme.GreenPrimary
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val db = remember { com.example.data.local.DatabaseProvider.getDatabase(context) }
    val profile by db.ammuDao().getProfile().collectAsState(initial = null)

    val primaryColor = when (state.colorTheme) {
        0 -> GreenPrimary
        1 -> Color(0xFF1976D2)
        2 -> Color(0xFF9C27B0)
        else -> GreenPrimary
    }

    val scaffoldBgColor = if (state.highContrast) Color.Black else Color(0xFFF9FAFB)
    val cardBgColor = if (state.highContrast) Color.DarkGray else Color.White
    val textColor = if (state.highContrast) Color.White else Color.Black
    val subtitleColor = if (state.highContrast) Color.LightGray else Color.Gray

    val fontScale = when (state.fontSize) {
        0 -> 0.85f
        1 -> 1.0f
        2 -> 1.15f
        3 -> 1.3f
        else -> 1.0f
    }

    // A helper to wrap BanglaText with scaling
    @Composable
    fun ScaledBanglaText(
        text: String,
        baseSize: Int = 16,
        color: Color = textColor,
        fontWeight: FontWeight = FontWeight.Normal,
        textAlign: TextAlign? = null,
        modifier: Modifier = Modifier
    ) {
        BanglaText(
            text = text,
            fontSize = (baseSize * fontScale).sp,
            color = color,
            fontWeight = fontWeight,
            textAlign = textAlign,
            modifier = modifier
        )
    }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(scaffoldBgColor)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                BanglaHeading(text = "সেটিংস", fontSize = (28 * fontScale).sp, color = textColor)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 1. Profile Section
            item {
                SettingsSection("প্রোফাইল", cardBgColor, textColor) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(primaryColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!profile?.avatarUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = profile!!.avatarUrl,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                val initial = (profile?.fullName ?: "আ").take(1)
                                Text(initial, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            ScaledBanglaText(profile?.fullName ?: "ব্যবহারকারী", baseSize = 20, fontWeight = FontWeight.Bold)
                            if (!profile?.phone.isNullOrEmpty()) {
                                ScaledBanglaText(profile!!.phone!!, baseSize = 14, color = subtitleColor)
                            } else {
                                ScaledBanglaText("+৮৮০ ১৭১২-৩৪৫৬৭৮", baseSize = 14, color = subtitleColor)
                            }
                            ScaledBanglaText(profile?.locationCity ?: "Dhaka", baseSize = 14, color = subtitleColor)
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = primaryColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                ScaledBanglaText(
                                    "প্রিমিয়াম",
                                    baseSize = 12,
                                    color = primaryColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        IconButton(onClick = { /* Edit Profile */ }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit Profile", tint = primaryColor)
                        }
                    }
                }
            }

            // 2. Accessibility
            item {
                SettingsSection("অ্যাক্সেসিবিলিটি (সহজ ব্যবহার)", cardBgColor, textColor) {
                    Column {
                        ScaledBanglaText("ফন্টের আকার", baseSize = 16, fontWeight = FontWeight.SemiBold)
                        Slider(
                            value = state.fontSize.toFloat(),
                            onValueChange = { viewModel.updateFontSize(it.toInt()) },
                            valueRange = 0f..3f,
                            steps = 2,
                            colors = SliderDefaults.colors(
                                thumbColor = primaryColor,
                                activeTrackColor = primaryColor
                            )
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            ScaledBanglaText("ছোট", baseSize = 12)
                            ScaledBanglaText("অনেক বড়", baseSize = 12)
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 12.dp))
                        
                        ToggleSettingItem(
                            title = "উচ্চ কনট্রাস্ট (High Contrast)",
                            subtitle = "চোখের আরামের জন্য কালো ব্যাকগ্রাউন্ড",
                            icon = Icons.Filled.Contrast,
                            checked = state.highContrast,
                            onCheckedChange = { viewModel.toggleHighContrast(it) },
                            textColor = textColor,
                            subtitleColor = subtitleColor,
                            primaryColor = primaryColor,
                            fontScale = fontScale
                        )
                        ToggleSettingItem(
                            title = "বড় বাটন মোড",
                            subtitle = "বাটনগুলো বড় করে দেখাবে",
                            icon = Icons.Filled.TouchApp,
                            checked = state.largeButtonMode,
                            onCheckedChange = { viewModel.toggleLargeButtonMode(it) },
                            textColor = textColor,
                            subtitleColor = subtitleColor,
                            primaryColor = primaryColor,
                            fontScale = fontScale
                        )
                        ToggleSettingItem(
                            title = "অ্যানিমেশন কমান",
                            subtitle = "স্ক্রিন পরিবর্তনের সময় অ্যানিমেশন বন্ধ করুন",
                            icon = Icons.Filled.Animation,
                            checked = state.reduceAnimation,
                            onCheckedChange = { viewModel.toggleReduceAnimation(it) },
                            textColor = textColor,
                            subtitleColor = subtitleColor,
                            primaryColor = primaryColor,
                            fontScale = fontScale
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = primaryColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ScaledBanglaText(
                                "এটি একটি নমুনা টেক্সট। আপনার নির্বাচন অনুযায়ী ফন্ট সাইজ এবং রঙ পরিবর্তন হবে।",
                                baseSize = 16,
                                modifier = Modifier.padding(12.dp),
                                color = textColor
                            )
                        }
                    }
                }
            }

            // 3. Notifications
            item {
                SettingsSection("নোটিফিকেশন (সতর্কবার্তা)", cardBgColor, textColor) {
                    ToggleSettingItem(
                        title = "সব নোটিফিকেশন অন/অফ",
                        icon = Icons.Filled.Notifications,
                        checked = state.masterNotifications,
                        onCheckedChange = { viewModel.toggleMasterNotifications(it) },
                        textColor = textColor,
                        subtitleColor = subtitleColor,
                        primaryColor = primaryColor,
                        fontScale = fontScale
                    )
                    
                    if (state.masterNotifications) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        ToggleSettingItem(
                            title = "নামাজের সময়",
                            checked = state.prayerNotifications,
                            onCheckedChange = { viewModel.toggleNotification("prayer", it) },
                            textColor = textColor,
                            subtitleColor = subtitleColor,
                            primaryColor = primaryColor,
                            fontScale = fontScale
                        )
                        ToggleSettingItem(
                            title = "ওষুধ রিমাইন্ডার",
                            checked = state.medicineReminders,
                            onCheckedChange = { viewModel.toggleNotification("medicine", it) },
                            textColor = textColor,
                            subtitleColor = subtitleColor,
                            primaryColor = primaryColor,
                            fontScale = fontScale
                        )
                        ToggleSettingItem(
                            title = "দৈনিক হাদিস",
                            checked = state.hadithNotifications,
                            onCheckedChange = { viewModel.toggleNotification("hadith", it) },
                            textColor = textColor,
                            subtitleColor = subtitleColor,
                            primaryColor = primaryColor,
                            fontScale = fontScale
                        )
                        ToggleSettingItem(
                            title = "পরিবার চেক-ইন",
                            checked = state.familyCheckIn,
                            onCheckedChange = { viewModel.toggleNotification("family", it) },
                            textColor = textColor,
                            subtitleColor = subtitleColor,
                            primaryColor = primaryColor,
                            fontScale = fontScale
                        )
                    }
                }
            }

            // 4. Appearance
            item {
                SettingsSection("চেহারা", cardBgColor, textColor) {
                    ScaledBanglaText("থিম নির্বাচন করুন", baseSize = 16, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("সিস্টেম", "আলো", "অন্ধকার").forEachIndexed { index, label ->
                            ChoiceChip(
                                text = label,
                                selected = state.theme == index,
                                onClick = { viewModel.setTheme(index) },
                                primaryColor = primaryColor,
                                fontScale = fontScale
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    ScaledBanglaText("রঙের থিম", baseSize = 16, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        listOf(GreenPrimary, Color(0xFF1976D2), Color(0xFF9C27B0)).forEachIndexed { index, color ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(2.dp, if (state.colorTheme == index) textColor else Color.Transparent, CircleShape)
                                    .clickable { viewModel.setColorTheme(index) }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    ToggleSettingItem(
                        title = "বাংলা সংখ্যা ব্যবহার করুন",
                        subtitle = "১২৩ এর বদলে 123",
                        icon = Icons.Filled.Language,
                        checked = state.bengaliNumerals,
                        onCheckedChange = { viewModel.toggleBengaliNumerals(it) },
                        textColor = textColor,
                        subtitleColor = subtitleColor,
                        primaryColor = primaryColor,
                        fontScale = fontScale
                    )
                }
            }

            // 5. Privacy & Security
            item {
                SettingsSection("গোপনীয়তা ও নিরাপত্তা", cardBgColor, textColor) {
                    ToggleSettingItem(
                        title = "ডায়েরি পিন লক",
                        subtitle = "আপনার ডায়েরি সুরক্ষিত রাখুন",
                        icon = Icons.Filled.Lock,
                        checked = state.journalPinLock,
                        onCheckedChange = { viewModel.toggleJournalPinLock(it) },
                        textColor = textColor,
                        subtitleColor = subtitleColor,
                        primaryColor = primaryColor,
                        fontScale = fontScale
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    ActionSettingItem("অ্যাকাউন্ট ডেটা ডাউনলোড করুন", Icons.Filled.Download, textColor, fontScale, onClick = {
                        android.widget.Toast.makeText(context, "ডেটা ডাউনলোড শুরু হয়েছে (JSON)...", android.widget.Toast.LENGTH_SHORT).show()
                    })
                    ActionSettingItem("অ্যাকাউন্ট মুছে ফেলুন", Icons.Filled.DeleteForever, Color.Red, fontScale, onClick = {
                        android.widget.Toast.makeText(context, "সমস্ত ডেটা মুছে ফেলা হচ্ছে...", android.widget.Toast.LENGTH_LONG).show()
                    })
                }
            }

            // 6. Location
            item {
                SettingsSection("অবস্থান", cardBgColor, textColor) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = primaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            ScaledBanglaText("বর্তমান শহর: ঢাকা", baseSize = 16, fontWeight = FontWeight.SemiBold)
                            ScaledBanglaText("নামাজের সময়ের জন্য প্রয়োজনীয়", baseSize = 12, color = subtitleColor)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = { /* Change location */ }) {
                        ScaledBanglaText("অবস্থান পরিবর্তন করুন", baseSize = 14, color = primaryColor)
                    }
                }
            }

            // 7. Premium
            item {
                Surface(
                    color = Color(0xFFFFF8E1),
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = Color(0xFFFFA000), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        ScaledBanglaText("প্রিমিয়াম সাবস্ক্রিপশন", baseSize = 20, fontWeight = FontWeight.Bold, color = Color(0xFFF57C00))
                        Spacer(modifier = Modifier.height(8.dp))
                        ScaledBanglaText("আনলিমিটেড এআই, ফ্যামিলি শেয়ারিং এবং বিজ্ঞাপন মুক্ত অভিজ্ঞতা", baseSize = 14, color = Color.DarkGray, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { /* Upgrade */ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ScaledBanglaText("প্রিমিয়াম নিন মাত্র ৯৯৳/মাস", baseSize = 16, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 8. Help & Support
            item {
                SettingsSection("সাহায্য ও সাপোর্ট", cardBgColor, textColor) {
                    ActionSettingItem("সাহায্য ও প্রশ্নোত্তর (FAQ)", Icons.Filled.HelpOutline, textColor, fontScale)
                    ActionSettingItem("সমস্যা জানান (WhatsApp/Email)", Icons.Filled.SupportAgent, textColor, fontScale, onClick = {
                        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=+880000000000"))
                        context.startActivity(i)
                    })
                    ActionSettingItem("ভিডিও টিউটোরিয়াল", Icons.Filled.PlayCircleOutline, textColor, fontScale)
                }
            }

            // 9. About
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ScaledBanglaText("আম্মু অ্যাপ - সংস্করণ ১.০.০", baseSize = 14, color = subtitleColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(onClick = { android.widget.Toast.makeText(context, "গোপনীয়তা নীতি (Privacy Policy) খোলা হচ্ছে...", android.widget.Toast.LENGTH_SHORT).show() }) { ScaledBanglaText("গোপনীয়তা নীতি", baseSize = 12, color = primaryColor) }
                        TextButton(onClick = { android.widget.Toast.makeText(context, "ব্যবহারের শর্তাবলী (Terms of Service) খোলা হচ্ছে...", android.widget.Toast.LENGTH_SHORT).show() }) { ScaledBanglaText("ব্যবহারের শর্তাবলী", baseSize = 12, color = primaryColor) }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    cardBgColor: Color,
    titleColor: Color,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        BanglaText(
            text = title,
            fontSize = 16.sp,
            color = titleColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Surface(
            color = cardBgColor,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun ChoiceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    primaryColor: Color,
    fontScale: Float
) {
    Surface(
        color = if (selected) primaryColor else Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(if (selected) 0.dp else 1.dp, if (selected) Color.Transparent else Color.Gray),
        modifier = Modifier.clickable { onClick() }
    ) {
        BanglaText(
            text = text,
            color = if (selected) Color.White else Color.Gray,
            fontSize = (14 * fontScale).sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

// A local helper mimicking the Border implementation since border(BorderStroke) isn't exactly used right above without import tweaking
// Using border property is actually part of surface border. Wait, Surface doesn't take BorderStroke param without name. Let's provide manual border modifier instead.
// Correction in the code above: I used border = border(...) which is valid if I use `border = BorderStroke(...)`.

@Composable
fun ToggleSettingItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    textColor: Color,
    subtitleColor: Color,
    primaryColor: Color,
    fontScale: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = primaryColor)
            Spacer(modifier = Modifier.width(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            BanglaText(
                title,
                fontSize = (16 * fontScale).sp,
                color = textColor
            )
            if (subtitle != null) {
                BanglaText(
                    subtitle,
                    fontSize = (12 * fontScale).sp,
                    color = subtitleColor
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = primaryColor
            )
        )
    }
}

@Composable
fun ActionSettingItem(
    title: String,
    icon: ImageVector,
    color: Color,
    fontScale: Float,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color)
        Spacer(modifier = Modifier.width(16.dp))
        BanglaText(title, fontSize = (16 * fontScale).sp, color = color)
    }
}
