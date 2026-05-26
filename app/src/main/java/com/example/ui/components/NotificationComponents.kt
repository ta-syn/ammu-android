package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AppNotification
import com.example.ui.theme.GreenPrimary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationBell(
    notifications: List<AppNotification>,
    onNotificationClick: () -> Unit
) {
    val unreadCount = notifications.count { !it.isRead }
    
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable { onNotificationClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Notifications,
            contentDescription = "Notifications",
            tint = GreenPrimary
        )
        if (unreadCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp)
                    .size(18.dp)
                    .background(Color.Red, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = unreadCount.toString(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSheet(
    notifications: List<AppNotification>,
    onDismiss: () -> Unit,
    onMarkAsRead: (Int) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onClearAll: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BanglaHeading("নোটিফিকেশন", fontSize = 20.sp)
                Row {
                    TextButton(onClick = { onMarkAllAsRead() }) {
                        BanglaText("সব পড়া হয়েছে", color = GreenPrimary)
                    }
                    TextButton(onClick = { onClearAll() }) {
                        BanglaText("সব মুছুন", color = Color.Red)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            if (notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    BanglaText("কোনো নতুন নোটিফিকেশন নেই", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(notifications) { notification ->
                        NotificationItem(
                            notification = notification,
                            onClick = { onMarkAsRead(notification.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: AppNotification, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale("bn", "BD"))
    val timeStr = dateFormat.format(Date(notification.createdAt))

    Surface(
        color = if (notification.isRead) Color(0xFFF9FAFB) else Color(0xFFE8F5E9),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = if (notification.isRead) 0.dp else 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                BanglaHeading(notification.title, fontSize = 16.sp, color = if (notification.isRead) Color.DarkGray else Color.Black)
                if (!notification.isRead) {
                    Box(modifier = Modifier.size(8.dp).background(GreenPrimary, CircleShape))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            BanglaText(notification.message, fontSize = 14.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(8.dp))
            BanglaText(timeStr, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun InAppNotificationBanner(
    notification: AppNotification?,
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = notification != null,
        enter = androidx.compose.animation.slideInVertically(initialOffsetY = { -it }),
        exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { -it })
    ) {
        if (notification != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        onClick()
                        onDismiss()
                    },
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).background(GreenPrimary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Notifications, contentDescription = null, tint = GreenPrimary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        BanglaHeading(notification.title, fontSize = 16.sp)
                        BanglaText(notification.message, fontSize = 14.sp, color = Color.DarkGray)
                    }
                }
            }
            
            // Auto dismiss effect
            LaunchedEffect(notification) {
                kotlinx.coroutines.delay(4000)
                onDismiss()
            }
        }
    }
}
