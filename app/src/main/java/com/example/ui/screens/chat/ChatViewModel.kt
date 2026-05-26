package com.example.ui.screens.chat

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class ChatMode(val title: String, val systemPrompt: String) {
    GENERAL("সাধারণ", "তুমি 'আম্মু অ্যাসিস্ট্যান্ট' — একজন সহানুভূতিশীল, জ্ঞানী এবং ধার্মিক AI সহকারী যে বাংলাদেশি মায়েদের সাহায্য করে। সবসময় বাংলায় কথা বলো। উত্তর সহজ ও স্পষ্ট রাখো। ধর্মীয় বিষয়ে সঠিক ইসলামিক তথ্য দাও। স্বাস্থ্য বিষয়ে সাধারণ পরামর্শ দাও কিন্তু ডাক্তার দেখাতে বলো। সবসময় সম্মানজনক ভাষা ব্যবহার করো — 'আপনি' দিয়ে সম্বোধন করো। ক্ষতিকর ও অনুপযুক্ত কন্টেন্ট ফিল্টার করো। কোনোভাবেই সরাসরি চিকিৎসা বা ডাক্তারি প্রেসক্রিপশন দিবে না।"),
    ISLAMIC("ইসলামিক", "তুমি 'আম্মু অ্যাসিস্ট্যান্ট' — একজন সহানুভূতিশীল, জ্ঞানী এবং ধার্মিক AI সহকারী। তুমি এখন ইসলামিক মোডে আছো। ইসলামের আলোকে, কুরআন ও হাদিসের রেফারেন্স দিয়ে বাংলাদেশি মায়েদের সাহায্য করো। সর্বদা শুদ্ধ বাংলায় ও সম্মানজনকভাবে 'আপনি' বলে সম্বোধন করো।"),
    HEALTH("স্বাস্থ্য", "তুমি 'আম্মু অ্যাসিস্ট্যান্ট'। তুমি এখন স্বাস্থ্য মোডে আছো। মায়েদের স্বাস্থ্য ও পুষ্টি সম্পর্কে সাধারণ এবং নিরাপদ পরামর্শ দাও। তবে মনে করিয়ে দাও যে তুমি ডাক্তার নও, এবং গুরুতর বিষয়ে অবশ্যই চিকিৎসকের পরামর্শ নিতে হবে। সর্বদা শুদ্ধ বাংলায় ও সম্মানজনকভাবে কথা বলো। কোনোভাবেই সরাসরি চিকিৎসা, রোগ নির্ণয় বা ডাক্তারি ওষুধ সেবনের প্রেসক্রিপশন দিবে না; সর্বদা ডাক্তারের কাছে যাওয়ার পরামর্শ দেবে।"),
    RECIPE("রেসিপি", "তুমি 'আম্মু অ্যাসিস্ট্যান্ট'। তুমি এখন রেসিপি মোডে আছো। বাংলাদেশি রান্নার রেসিপি, টিপস এবং স্বাস্থ্যকর খাবারের ধারণা দাও। একদম সহজ ধাপে, দেশি উপাদানের সাহায্যে কীভাবে রান্না করা যায় তা শিখিয়ে দাও। সর্বদা শুদ্ধ বাংলায় ও সম্মানজনকভাবে কথা বলো।")
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val isAnimating: Boolean = false
)

class ChatViewModel : ViewModel() {

    private val _messages = mutableStateListOf<ChatMessage>()
    val messages: List<ChatMessage> get() = _messages

    private val _currentMode = MutableStateFlow(ChatMode.GENERAL)
    val currentMode: StateFlow<ChatMode> = _currentMode

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping

    fun setMode(mode: ChatMode) {
        _currentMode.value = mode
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        _messages.add(ChatMessage(text = text, isUser = true))
        _isTyping.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val aiMessageId = java.util.UUID.randomUUID().toString()
            var currentText = ""
            val apiKey = BuildConfig.OPENROUTER_API_KEY
            
            val requestMessages = mutableListOf<OpenRouterMessage>()
            requestMessages.add(OpenRouterMessage("system", _currentMode.value.systemPrompt))
            
            _messages.filter { !it.isAnimating }.takeLast(10).forEach { msg ->
                requestMessages.add(OpenRouterMessage(if (msg.isUser) "user" else "assistant", msg.text))
            }
            
            val models = listOf(
                "openrouter/free",
                "google/gemma-2-9b-it:free",
                "qwen/qwen-2.5-72b-instruct:free",
                "meta-llama/llama-3-8b-instruct:free"
            )
            
            var success = false
            for (model in models) {
                if (success) break
                try {
                    val req = OpenRouterRequest(model = model, messages = requestMessages, stream = true)
                    streamModelResponse(apiKey, req, aiMessageId) { text -> currentText = text }
                    success = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            if (!success) {
                _isTyping.value = false
                currentText = "সমস্যা হচ্ছে — একটু পরে আবার চেষ্টা করুন।"
                val index = _messages.indexOfFirst { it.id == aiMessageId }
                if (index != -1) {
                    _messages[index] = _messages[index].copy(text = currentText)
                } else {
                    _messages.add(ChatMessage(id = aiMessageId, text = currentText, isUser = false))
                }
            }
            
            val finalIndex = _messages.indexOfFirst { it.id == aiMessageId }
            if (finalIndex != -1) {
                _messages[finalIndex] = _messages[finalIndex].copy(isAnimating = false)
            }
        }
    }

    private suspend fun streamModelResponse(
        apiKey: String,
        req: OpenRouterRequest,
        aiMessageId: String,
        onProgress: (String) -> Unit
    ) {
        val response = OpenRouterClient.service.streamChatCompletions("Bearer $apiKey", request = req)
        
        _isTyping.value = false
        val existingIndex = _messages.indexOfFirst { it.id == aiMessageId }
        if (existingIndex == -1) {
            _messages.add(ChatMessage(id = aiMessageId, text = "", isUser = false, isAnimating = true))
        }

        var currentText = ""
        response.byteStream().bufferedReader().use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line!!.startsWith("data: ")) {
                    val data = line!!.removePrefix("data: ")
                    if (data == "[DONE]") break
                    
                    try {
                        val jsonObject = org.json.JSONObject(data)
                        val choices = jsonObject.optJSONArray("choices")
                        if (choices != null && choices.length() > 0) {
                            val firstChoice = choices.getJSONObject(0)
                            val delta = firstChoice.optJSONObject("delta")
                            val content = delta?.optString("content")
                            if (!content.isNullOrEmpty()) {
                                currentText += content
                                onProgress(currentText)
                                val index = _messages.indexOfFirst { it.id == aiMessageId }
                                if (index != -1) {
                                    _messages[index] = _messages[index].copy(text = currentText)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        
        if (currentText.isEmpty()) {
            throw Exception("Empty response from AI model")
        }
    }
}

