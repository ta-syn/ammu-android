package com.example.ui.screens.recipe

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DatabaseProvider
import com.example.data.local.entity.Recipe
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

class RecipeViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = DatabaseProvider.getDatabase(application).ammuDao()

    val favoriteRecipes: StateFlow<List<Recipe>> = dao.getFavoriteRecipes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allRecipes: StateFlow<List<Recipe>> = dao.getAllRecipes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        // Prepopulate with default popular Bangladeshi recipes if database is empty
        viewModelScope.launch {
            dao.getAllRecipes().collect { currentRecipes ->
                if (currentRecipes.isEmpty()) {
                    dao.insertRecipe(
                        Recipe(
                            title = "মুরগির রোস্ট",
                            ingredientsList = "মুরগি, দই, পেঁয়াজ, এলাচ, ঘি, গরম মসলা",
                            steps = "১. প্রথমে মুরগি ধুয়ে দই ও মসলা দিয়ে মেরিনেট করে রাখুন ৩০ মিনিট।\n২. কড়াইতে ঘি গরম করে পেঁয়াজ কুচি ভেজে বেরেস্তা করে তুলে রাখুন।\n৩. ওই ঘিয়েই মেরিনেট করা মুরগি দিয়ে ভালো করে কষিয়ে রান্না করুন।\n৪. সেদ্ধ হয়ে গ্রেভি ঘন হয়ে এলে বেরেস্তা ছড়িয়ে নামিয়ে নিন।",
                            prepTime = "৪৫ মিনিট",
                            isFavorite = false,
                            isDiabetesFriendly = false,
                            category = "meat"
                        )
                    )
                    dao.insertRecipe(
                        Recipe(
                            title = "মিক্সড ভেজিটেবল",
                            ingredientsList = "গাজর, পেঁপে, মটরশুঁটি, পটল, কাঁচামরিচ, তেল, ফোড়ন",
                            steps = "১. সবজিগুলো সমান সাইজে কেটে ধুয়ে পরিষ্কার করে নিন।\n২. কড়াইতে সামান্য তেল দিয়ে সবজিগুলো সামান্য লবন দিয়ে সেদ্ধ করতে দিন।\n৩. সেদ্ধ হয়ে এলে কড়াইতে তেল ও ফোড়ন দিয়ে সবজিগুলো হালকা ভেজে নামিয়ে নিন।",
                            prepTime = "২৫ মিনিট",
                            isFavorite = false,
                            isDiabetesFriendly = true,
                            category = "veg"
                        )
                    )
                }
                throw kotlinx.coroutines.CancellationException()
            }
        }
    }

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generatedRecipe = MutableStateFlow<Recipe?>(null)
    val generatedRecipe: StateFlow<Recipe?> = _generatedRecipe.asStateFlow()

    private val _generationError = MutableStateFlow<String?>(null)
    val generationError: StateFlow<String?> = _generationError.asStateFlow()

    fun clearGeneratedRecipe() {
        _generatedRecipe.value = null
        _generationError.value = null
    }

    fun saveRecipe(title: String, ingredients: String, steps: String, prepTime: String, diabetesFriendly: Boolean) {
        viewModelScope.launch {
            val recipe = Recipe(
                title = title,
                ingredientsList = ingredients,
                steps = steps,
                prepTime = prepTime,
                isFavorite = true,
                isDiabetesFriendly = diabetesFriendly
            )
            dao.insertRecipe(recipe)
        }
    }

    fun generateRecipe(ingredients: String) {
        if (ingredients.isBlank()) return
        _isGenerating.value = true
        _generationError.value = null
        _generatedRecipe.value = null

        viewModelScope.launch(Dispatchers.IO) {
            val apiKey = com.example.BuildConfig.OPENROUTER_API_KEY
            if (apiKey.isBlank()) {
                _generationError.value = "এপিআই কী (API Key) পাওয়া যায়নি।"
                _isGenerating.value = false
                return@launch
            }

            val systemPrompt = """
                তুমি একজন পেশাদার শেফ এবং রেসিপি সহকারী। ব্যবহারকারী তোমাকে কিছু উপকরণের নাম দেবে।
                তুমি সেগুলো ব্যবহার করে একটি অত্যন্ত সুস্বাদু রেসিপি তৈরি করবে।
                উত্তরটি শুধুমাত্র এবং শুধুমাত্র নিচের JSON ফরম্যাটে দেবে, অন্য কোনো অতিরিক্ত লেখা বা ব্যাখ্যা ছাড়াই:
                {
                  "title": "রেসিপির নাম",
                  "prepTime": "রান্নার সময় (যেমন: ৩০ মিনিট)",
                  "ingredients": "প্রয়োজনীয় উপকরণের তালিকা কমা দিয়ে আলাদা করে",
                  "steps": "ধাপ ১\nধাপ ২\nধাপ ৩...",
                  "isDiabetesFriendly": true বা false
                }
                সম্পূর্ণ উত্তরটি বাংলায় হতে হবে এবং JSON বৈধ হতে হবে।
            """.trimIndent()

            val requestMessages = listOf(
                com.example.ui.screens.chat.OpenRouterMessage("system", systemPrompt),
                com.example.ui.screens.chat.OpenRouterMessage("user", "উপকরণ: $ingredients")
            )

            var attemptSuccess = false
            val models = listOf(
                "google/gemma-3-27b-it:free",
                "google/gemma-2-9b-it:free",
                "qwen/qwen-2.5-72b-instruct:free",
                "meta-llama/llama-3.1-8b-instruct:free",
                "microsoft/phi-3-mini-128k-instruct:free"
            )
            
            for (model in models) {
                if (attemptSuccess) break
                try {
                    val request = com.example.ui.screens.chat.OpenRouterRequest(
                        model = model,
                        messages = requestMessages,
                        stream = false,
                        response_format = null
                    )

                    val response = com.example.ui.screens.chat.OpenRouterClient.service.getChatCompletions(
                        auth = "Bearer $apiKey",
                        request = request
                    )

                    val content = response.choices?.firstOrNull()?.message?.content
                    if (!content.isNullOrBlank()) {
                        var cleanContent = content.trim()
                        if (cleanContent.startsWith("```json")) {
                            cleanContent = cleanContent.removePrefix("```json")
                            if (cleanContent.endsWith("```")) {
                                cleanContent = cleanContent.removeSuffix("```")
                            }
                            cleanContent = cleanContent.trim()
                        } else if (cleanContent.startsWith("```")) {
                            cleanContent = cleanContent.removePrefix("```")
                            if (cleanContent.endsWith("```")) {
                                cleanContent = cleanContent.removeSuffix("```")
                            }
                            cleanContent = cleanContent.trim()
                        }

                        val json = org.json.JSONObject(cleanContent)
                        val title = json.getString("title")
                        val prepTime = json.getString("prepTime")
                        val ingrList = json.getString("ingredients")
                        val steps = json.getString("steps")
                        val isDiabetes = json.optBoolean("isDiabetesFriendly", false)

                        _generatedRecipe.value = Recipe(
                            title = title,
                            ingredientsList = ingrList,
                            steps = steps,
                            prepTime = prepTime,
                            isFavorite = false,
                            isDiabetesFriendly = isDiabetes
                        )
                        attemptSuccess = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (!attemptSuccess) {
                _generationError.value = "রেসিপি তৈরি করা যায়নি। অনুগ্রহ করে আবার চেষ্টা করুন।"
            }
            _isGenerating.value = false
        }
    }

    fun toggleFavoriteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            val updated = recipe.copy(isFavorite = !recipe.isFavorite)
            dao.insertRecipe(updated)
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            dao.deleteRecipe(recipe)
        }
    }
}
