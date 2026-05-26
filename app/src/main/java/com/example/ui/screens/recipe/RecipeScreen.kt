package com.example.ui.screens.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.Recipe
import com.example.ui.components.BanglaHeading
import com.example.ui.components.BanglaText
import com.example.ui.theme.GreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(viewModel: RecipeViewModel = viewModel()) {
    val favoriteRecipes by viewModel.favoriteRecipes.collectAsState()
    
    var showAiRecipeSheet by remember { mutableStateOf(false) }
    var ingredientQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var viewRecipeDetail by remember { mutableStateOf<Recipe?>(null) }

    if (viewRecipeDetail != null) {
        RecipeDetailScreen(recipe = viewRecipeDetail!!, onBack = { viewRecipeDetail = null })
        return
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAiRecipeSheet = true },
                containerColor = GreenPrimary,
                icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White) },
                text = { BanglaText("AI রেসিপি", color = Color.White) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFF9F2)) // warm, appetizing background
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                BanglaHeading(text = "আজ কী রান্না করবেন? \uD83C\uDF73", fontSize = 28.sp)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Search Input area
            item {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        BanglaText(text = "আপনার কাছে কী কী আছে লিখুন", color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = ingredientQuery,
                            onValueChange = { ingredientQuery = it },
                            placeholder = { BanglaText("যেমন: মুরগি, আলু, দই...") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { if (ingredientQuery.isNotEmpty()) showAiRecipeSheet = true }) {
                                    Icon(Icons.Filled.Search, contentDescription = null, tint = GreenPrimary)
                                }
                            },
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFFF9FAFB),
                                focusedContainerColor = Color(0xFFF9FAFB),
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        val chips = listOf("মুরগি", "ডাল", "আলু", "ডিম", "মাছ", "মাংস")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(chips) { chip ->
                                AssistChip(
                                    onClick = { ingredientQuery = if (ingredientQuery.isEmpty()) chip else "$ingredientQuery, $chip" },
                                    label = { BanglaText(chip) }
                                )
                            }
                        }
                    }
                }
            }

            // Categories
            item {
                Spacer(modifier = Modifier.height(8.dp))
                BanglaHeading(text = "আপনার জন্য বাছাইকৃত", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val categories = listOf(
                        "সকালের নাশতা" to "🍳",
                        "দুপুরের খাবার" to "🍛",
                        "রাতের খাবার" to "🍲",
                        "ইফতার" to "🧆",
                        "স্বাস্থ্যকর" to "🥗",
                        "ডায়াবেটিস-বান্ধব" to "❤️"
                    )
                    items(categories) { category ->
                        CategoryCard(title = category.first, emoji = category.second)
                    }
                }
            }
            
            // Bangladeshi Curated List
            item {
                Spacer(modifier = Modifier.height(16.dp))
                BanglaHeading(text = "জনপ্রিয় বাংলা রেসিপি", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            val mockRecipes = listOf(
                Recipe(0, "মুরগির রোস্ট", "মুরগি, দই, পেঁয়াজ, এলাচ", "১. মেরিনেট করুন\n২. পেঁয়াজ ভাজুন\n৩. রান্না করুন", "৪৫ মিনিট", false, false, "meat"),
                Recipe(0, "মিক্সড ভেজিটেবল", "গাজর, পেঁপে, মটরশুঁটি", "১. সবজি কাটুন\n২. সেদ্ধ করুন\n৩. তেলে ভাজুন", "২৫ মিনিট", false, true, "veg")
            )
            
            items(mockRecipes) { recipe ->
                RecipeCard(recipe) {
                    viewRecipeDetail = recipe
                }
            }

            if (favoriteRecipes.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    BanglaHeading(text = "সংরক্ষিত রেসিপি সমূহ", fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                items(favoriteRecipes) { recipe ->
                    RecipeCard(recipe) {
                        viewRecipeDetail = recipe
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                MealPlannerSection()
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                RamadanSpecialSection()
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    if (showAiRecipeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAiRecipeSheet = false },
            sheetState = sheetState
        ) {
            AiRecipeGenerator(
                viewModel = viewModel,
                ingredients = ingredientQuery,
                onClose = { showAiRecipeSheet = false },
                onSave = { title, ingr, steps, time, isDiab ->
                    viewModel.saveRecipe(title, ingr, steps, time, isDiab)
                    showAiRecipeSheet = false
                    ingredientQuery = ""
                }
            )
        }
    }
}

@Composable
fun CategoryCard(title: String, emoji: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier.clickable { }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            BanglaText(text = title, fontSize = 12.sp)
        }
    }
}

@Composable
fun RecipeCard(recipe: Recipe, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFFFFF3E0), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = if (recipe.category == "veg" || recipe.isDiabetesFriendly) "🥗" else "🍲", fontSize = 32.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                BanglaHeading(text = recipe.title, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AccessTime, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    BanglaText(text = recipe.prepTime, fontSize = 12.sp, color = Color.Gray)
                    if (recipe.isDiabetesFriendly) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) {
                            BanglaText("ডায়াবেটিস-বান্ধব", fontSize = 10.sp, color = GreenPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                BanglaText(text = recipe.ingredientsList, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun MealPlannerSection() {
    Surface(
        color = Color(0xFFE3F2FD),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = Color(0xFF1976D2))
                Spacer(modifier = Modifier.width(8.dp))
                BanglaHeading(text = "সাপ্তাহিক মিল প্ল্যানার", color = Color(0xFF1976D2), fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            BanglaText(text = "সারা সপ্তাহের রান্নার রুটিন তৈরি করুন এবং স্বয়ংক্রিয়ভাবে বাজারের তালিকা পান।", color = Color.DarkGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))) {
                BanglaText("প্ল্যান দেখুন")
            }
        }
    }
}

@Composable
fun RamadanSpecialSection() {
    Surface(
        color = Color(0xFFF3E5F5),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🌙", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                BanglaHeading(text = "রমজান স্পেশাল", color = Color(0xFF6A1B9A), fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            BanglaText(text = "ইফতার এবং সেহরির জন্য স্বাস্থ্যকর রেসিপি।", color = Color.DarkGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {}, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6A1B9A))) {
                    BanglaText("ইফতার")
                }
                OutlinedButton(onClick = {}, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6A1B9A))) {
                    BanglaText("সেহরি")
                }
            }
        }
    }
}

// ---------------- Detail Screen ---------------- //

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(recipe: Recipe, onBack: () -> Unit) {
    var isTimerRunning by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { BanglaHeading(recipe.title, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(if (recipe.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, contentDescription = "Favorite", tint = Color.Red)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Timer, contentDescription = null, tint = GreenPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        BanglaText(recipe.prepTime)
                    }
                    if (recipe.isDiabetesFriendly) {
                        Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) {
                            BanglaText("ডায়াবেটিস-বান্ধব", fontSize = 12.sp, color = GreenPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
            }
            
            item {
                BanglaHeading(text = "উপকরণ", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(8.dp))
                val ingredients = recipe.ingredientsList.split(",").map { it.trim() }
                ingredients.forEach { ingr ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        var checked by remember { mutableStateOf(false) }
                        Checkbox(checked = checked, onCheckedChange = { checked = it })
                        BanglaText(ingr, color = if(checked) Color.Gray else Color.Black)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
            }
            
            item {
                BanglaHeading(text = "প্রস্তুতপ্রণালী", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            val stepList = recipe.steps.split("\n").filter { it.isNotBlank() }
            items(stepList.size) { index ->
                Surface(
                    color = Color(0xFFF9FAFB),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        BanglaHeading(text = "ধাপ ${index + 1}", fontSize = 16.sp, color = GreenPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        BanglaText(text = stepList[index], fontSize = 16.sp)
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = Color(0xFFE65100))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            BanglaHeading(text = "টিপস:", fontSize = 14.sp)
                            BanglaText(text = "রান্না চলার সময় স্ক্রিন অন থাকবে।", fontSize = 12.sp, color = Color.DarkGray)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// ---------------- AI Generator Simulation ---------------- //

@Composable
fun AiRecipeGenerator(
    viewModel: RecipeViewModel,
    ingredients: String,
    onClose: () -> Unit,
    onSave: (String, String, String, String, Boolean) -> Unit
) {
    val isGenerating by viewModel.isGenerating.collectAsState()
    val generatedRecipe by viewModel.generatedRecipe.collectAsState()
    val error by viewModel.generationError.collectAsState()
    
    LaunchedEffect(ingredients) {
        if (ingredients.isNotBlank()) {
            viewModel.generateRecipe(ingredients)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearGeneratedRecipe()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BanglaHeading(text = "AI রেসিপি জেনারেটর", fontSize = 22.sp, color = GreenPrimary)
        Spacer(modifier = Modifier.height(24.dp))
        
        if (ingredients.isBlank()) {
            BanglaText("আপনি কোনো উপকরণের নাম দেননি।", color = Color.Red)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onClose) { BanglaText("বন্ধ করুন") }
        } else if (isGenerating) {
            CircularProgressIndicator(color = GreenPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            BanglaText("আপনার উপকরণ দিয়ে রেসিপি তৈরি হচ্ছে...")
        } else if (error != null) {
            BanglaText(error!!, color = Color.Red)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onClose) { BanglaText("বন্ধ করুন") }
        } else if (generatedRecipe != null) {
            val recipe = generatedRecipe!!
            BanglaText("আপনার জন্য একটি রেসিপি তৈরি করা হয়েছে!", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(
                color = Color(0xFFF9FAFB),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    BanglaHeading(recipe.title)
                    BanglaText("সময়: ${recipe.prepTime}", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    BanglaText("উপকরণ:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    BanglaText(recipe.ingredientsList, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    BanglaText("প্রস্তুতপ্রণালী:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    BanglaText(recipe.steps, fontSize = 14.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = onClose, modifier = Modifier.weight(1f)) {
                    BanglaText("বাতিল")
                }
                Button(
                    onClick = {
                        onSave(
                            recipe.title,
                            recipe.ingredientsList,
                            recipe.steps,
                            recipe.prepTime,
                            recipe.isDiabetesFriendly
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    BanglaText("সংরক্ষণ করুন")
                }
            }
        }
    }
}
