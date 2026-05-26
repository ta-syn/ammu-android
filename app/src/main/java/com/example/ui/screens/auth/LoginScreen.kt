package com.example.ui.screens.auth

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.Facebook
import com.example.network.supabaseClient
import com.example.ui.components.*
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var isGoogleLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val sessionStatus by supabaseClient.auth.sessionStatus.collectAsState()

    LaunchedEffect(sessionStatus) {
        if (sessionStatus is SessionStatus.Authenticated) {
            onLoginSuccess()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(bottomStart = Radius.xl, bottomEnd = Radius.xl)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🌙",
                        fontSize = 48.sp,
                        modifier = Modifier.padding(bottom = Spacing.sm)
                    )
                    BanglaHeading(
                        text = "আম্মুর অ্যাপে স্বাগতম",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = Spacing.lg),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    BanglaText(
                        text = "আপনার নিজের যত্ন নিতে শুরু করুন",
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Login Card
            CardBase(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BanglaText(
                        text = "লগইন করুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Spacing.sm),
                        textAlign = TextAlign.Center
                    )

                    if (errorMessage.isNotEmpty()) {
                        BanglaText(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = Spacing.sm),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    // ── Google Login Button ──────────────────────────────
                    AppButton(
                        text = if (isGoogleLoading) "অপেক্ষা করুন..." else "🔵  Google দিয়ে লগইন করুন",
                        onClick = {
                            isGoogleLoading = true
                            errorMessage = ""
                            scope.launch {
                                try {
                                    val url = supabaseClient.auth.getOAuthUrl(
                                        provider = Google,
                                        redirectUrl = "app://supabase.login"
                                    )
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()))
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    errorMessage = "ত্রুটি হয়েছে: ${e.message}"
                                } finally {
                                    isGoogleLoading = false
                                }
                            }
                        },
                        enabled = !isGoogleLoading,
                        containerColor = Color(0xFF4285F4),
                        contentColor = Color.White
                    )

                    // ── Divider ──────────────────────────────────────────
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        BanglaText(
                            text = "  অথবা  ",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 13.sp
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }

                    // ── Facebook Login Button (Coming Soon) ──────────────
                    AppButton(
                        text = "🔵  Facebook দিয়ে লগইন করুন",
                        onClick = {
                            Toast.makeText(
                                context,
                                "Coming Soon! 🚀",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        enabled = true,
                        containerColor = Color(0xFF1877F2).copy(alpha = 0.5f),
                        contentColor = Color.White
                    )

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    AppTextButton(
                        text = "নতুন অ্যাকাউন্ট তৈরি করুন",
                        onClick = onNavigateToRegister,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
