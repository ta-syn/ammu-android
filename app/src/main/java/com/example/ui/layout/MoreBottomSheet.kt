package com.example.ui.layout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BanglaHeading

data class MoreItem(val id: String, val title: String, val iconEmoji: String)

val moreItems = listOf(
    MoreItem("quran", "কোরআন", "📖"),
    MoreItem("hadith", "হাদিস", "📜"),
    MoreItem("tasbih", "তাসবিহ", "📿"),
    MoreItem("qibla", "কিবলা", "🧭"),
    MoreItem("dua", "দোয়া", "🤲"),
    MoreItem("medicine", "ওষুধ", "💊"),
    MoreItem("hospital", "হাসপাতাল", "🏥"),
    MoreItem("recipe", "রেসিপি", "🍳"),
    MoreItem("shopping", "বাজার", "🛒"),
    MoreItem("expense", "খরচ", "৳"),
    MoreItem("news", "খবর", "📰"),
    MoreItem("family", "পরিবার", "👨‍👩‍👧‍👦"),
    MoreItem("journal", "ডায়েরি", "📓"),
    MoreItem("settings", "সেটিংস", "⚙️")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreBottomSheet(
    onDismissRequest: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            BanglaHeading(
                text = "সব ফিচার",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.onSurface
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                items(moreItems) { item ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismissRequest()
                                if (item.id == "quran") {
                                    onNavigate("quran")
                                } else if (item.id == "tasbih") {
                                    onNavigate("tasbih")
                                } else if (item.id == "dua") {
                                    onNavigate("dua")
                                } else if (item.id == "medicine") {
                                    onNavigate("medicine")
                                } else if (item.id == "hospital") {
                                    onNavigate("hospital")
                                } else if (item.id == "recipe") {
                                    onNavigate("recipe")
                                } else if (item.id == "news") {
                                    onNavigate("news")
                                } else if (item.id == "shopping") {
                                    onNavigate("shopping")
                                } else if (item.id == "family") {
                                    onNavigate("family")
                                } else if (item.id == "journal") {
                                    onNavigate("journal")
                                } else if (item.id == "settings") {
                                    onNavigate("settings")
                                }
                            }
                    ) {
                        Text(
                            text = item.iconEmoji,
                            fontSize = 32.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
