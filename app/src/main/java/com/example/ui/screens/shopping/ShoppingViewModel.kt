package com.example.ui.screens.shopping

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DatabaseProvider
import com.example.data.local.entity.ShoppingList
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import java.util.UUID

data class ShoppingItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val quantity: String,
    val unit: String,
    val isChecked: Boolean = false,
    val category: String = "সাধারণ"
)

data class ParsedShoppingList(
    val id: Int,
    val title: String,
    val items: List<ShoppingItem>,
    val updatedAt: Long
)

class ShoppingViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = DatabaseProvider.getDatabase(application).ammuDao()
    
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, ShoppingItem::class.java)
    private val adapter: JsonAdapter<List<ShoppingItem>> = moshi.adapter(listType)

    val activeLists: StateFlow<List<ParsedShoppingList>> = dao.getAllShoppingLists()
        .map { lists ->
            lists.map { dbList ->
                val items = try {
                    if (dbList.itemsJson.isNotBlank()) adapter.fromJson(dbList.itemsJson) ?: emptyList()
                    else emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
                ParsedShoppingList(dbList.id, dbList.title, items, dbList.updatedAt)
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generatedItems = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val generatedItems: StateFlow<List<ShoppingItem>> = _generatedItems.asStateFlow()

    private val _generationError = MutableStateFlow<String?>(null)
    val generationError: StateFlow<String?> = _generationError.asStateFlow()

    fun clearGeneratedItems() {
        _generatedItems.value = emptyList()
        _generationError.value = null
    }

    fun createList(title: String) {
        viewModelScope.launch {
            val newList = ShoppingList(
                userId = "default_user",
                title = title,
                itemsJson = "[]",
                isShared = false
            )
            dao.insertShoppingList(newList)
        }
    }

    fun deleteList(id: Int) {
        viewModelScope.launch {
            val dbItem = ShoppingList(id, "default_user", "", "", false)
            dao.deleteShoppingList(dbItem)
        }
    }

    fun addItemToList(listId: Int, name: String, quantity: String, unit: String, category: String = "সাধারণ") {
        viewModelScope.launch {
            val currentList = getListByIdBlocking(listId)
            if (currentList != null) {
                val currentItems = parseItems(currentList.itemsJson).toMutableList()
                currentItems.add(ShoppingItem(name = name, quantity = quantity, unit = unit, category = category))
                val newJson = adapter.toJson(currentItems)
                
                dao.insertShoppingList(currentList.copy(itemsJson = newJson, updatedAt = System.currentTimeMillis()))
            }
        }
    }

    fun toggleItemCheck(listId: Int, itemId: String) {
        viewModelScope.launch {
            val currentList = getListByIdBlocking(listId)
            if (currentList != null) {
                val currentItems = parseItems(currentList.itemsJson).toMutableList()
                val index = currentItems.indexOfFirst { it.id == itemId }
                if (index != -1) {
                    val item = currentItems[index]
                    currentItems[index] = item.copy(isChecked = !item.isChecked)
                    val newJson = adapter.toJson(currentItems)
                    dao.insertShoppingList(currentList.copy(itemsJson = newJson, updatedAt = System.currentTimeMillis()))
                }
            }
        }
    }

    fun deleteItem(listId: Int, itemId: String) {
        viewModelScope.launch {
            val currentList = getListByIdBlocking(listId)
            if (currentList != null) {
                val currentItems = parseItems(currentList.itemsJson).toMutableList()
                currentItems.removeAll { it.id == itemId }
                val newJson = adapter.toJson(currentItems)
                dao.insertShoppingList(currentList.copy(itemsJson = newJson, updatedAt = System.currentTimeMillis()))
            }
        }
    }

    fun updateItemQuantity(listId: Int, itemId: String, newQuantity: String) {
        viewModelScope.launch {
            val currentList = getListByIdBlocking(listId)
            if (currentList != null) {
                val currentItems = parseItems(currentList.itemsJson).toMutableList()
                val index = currentItems.indexOfFirst { it.id == itemId }
                if (index != -1) {
                    currentItems[index] = currentItems[index].copy(quantity = newQuantity)
                    val newJson = adapter.toJson(currentItems)
                    dao.insertShoppingList(currentList.copy(itemsJson = newJson, updatedAt = System.currentTimeMillis()))
                }
            }
        }
    }

    private suspend fun getListByIdBlocking(id: Int): ShoppingList? {
        return dao.getShoppingListById(id)
    }

    private fun parseItems(json: String): List<ShoppingItem> {
        return try {
            if (json.isNotBlank()) adapter.fromJson(json) ?: emptyList()
            else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun generateShoppingList(mealQuery: String) {
        if (mealQuery.isBlank()) return
        _isGenerating.value = true
        _generationError.value = null
        _generatedItems.value = emptyList()

        viewModelScope.launch(Dispatchers.IO) {
            val apiKey = com.example.BuildConfig.OPENROUTER_API_KEY
            if (apiKey.isBlank()) {
                _generationError.value = "এপিআই কী (API Key) পাওয়া যায়নি।"
                _isGenerating.value = false
                return@launch
            }

            val systemPrompt = """
                তুমি একজন রান্নার বাজার তালিকা সহকারী। ব্যবহারকারী তোমাকে একটি পদের নাম বা মিল প্ল্যান দেবে (যেমন: ৪ জনের জন্য খিচুড়ি ও মাংস ভুনা)।
                তুমি রেসিপি অনুযায়ী কোন কোন উপকরণ কতটুকু লাগবে তার একটি বাজার তালিকা তৈরি করবে।
                উত্তরটি শুধুমাত্র এবং শুধুমাত্র নিচের JSON ফরম্যাটে দেবে, অন্য কোনো অতিরিক্ত লেখা বা ব্যাখ্যা ছাড়াই:
                [
                  {
                    "name": "উপকরণের নাম",
                    "quantity": "পরিমাণ",
                    "unit": "একক",
                    "category": "ক্যাটাগরি"
                  }
                ]
                ক্যাটাগরি অবশ্যই নিচের একটি হতে হবে: "মাছ/মাংস", "সবজি", "শস্য", "মসলা", "সাধারণ"।
                সম্পূর্ণ উত্তরটি বাংলায় হতে হবে এবং JSON বৈধ হতে হবে।
            """.trimIndent()

            val requestMessages = listOf(
                com.example.ui.screens.chat.OpenRouterMessage("system", systemPrompt),
                com.example.ui.screens.chat.OpenRouterMessage("user", "রান্না: $mealQuery")
            )

            var attemptSuccess = false
            val models = listOf("google/gemma-3-27b-it:free", "meta-llama/llama-3-8b-instruct:free")

            for (model in models) {
                if (attemptSuccess) break
                try {
                    val request = com.example.ui.screens.chat.OpenRouterRequest(
                        model = model,
                        messages = requestMessages,
                        stream = false
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

                        val jsonArray = org.json.JSONArray(cleanContent)
                        val items = mutableListOf<ShoppingItem>()
                        for (i in 0 until jsonArray.length()) {
                            val obj = jsonArray.getJSONObject(i)
                            items.add(
                                ShoppingItem(
                                    name = obj.getString("name"),
                                    quantity = obj.optString("quantity", ""),
                                    unit = obj.optString("unit", ""),
                                    category = obj.optString("category", "সাধারণ")
                                )
                            )
                        }
                        _generatedItems.value = items
                        attemptSuccess = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (!attemptSuccess) {
                _generationError.value = "বাজার তালিকা তৈরি করা যায়নি। অনুগ্রহ করে আবার চেষ্টা করুন।"
            }
            _isGenerating.value = false
        }
    }
}
