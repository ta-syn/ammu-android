package com.example.ui.screens.hadith

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class HadithLibraryUiState {
    object Idle : HadithLibraryUiState()
    object Loading : HadithLibraryUiState()
    data class Success(
        val name: String,
        val hadiths: List<HadithDto>,
        val currentSection: Int,
        val metadata: HadithMetadataDto?
    ) : HadithLibraryUiState()
    data class Error(val message: String) : HadithLibraryUiState()
}

data class HadithBookInfo(
    val id: String,
    val bengaliName: String,
    val editionName: String,
    val maxSections: Int
)

class HadithViewModel : ViewModel() {
    val books = listOf(
        HadithBookInfo("bukhari", "সহীহ বুখারী", "ben-bukhari", 97),
        HadithBookInfo("muslim", "সহীহ মুসলিম", "ben-muslim", 56),
        HadithBookInfo("tirmidhi", "জামে আত-তিরমিজী", "ben-tirmidhi", 49),
        HadithBookInfo("abudawud", "সুনান আবু দাউদ", "ben-abudawud", 43),
        HadithBookInfo("nasai", "সুনান নাসায়ী", "ben-nasai", 49),
        HadithBookInfo("ibnmajah", "সুনান ইবনে মাজাহ", "ben-ibnmajah", 37),
        HadithBookInfo("malik", "মুয়াত্তা ইমাম মালিক", "ben-malik", 61),
        HadithBookInfo("nawawi", "৪০ হাদিস (ইমাম নববী)", "ben-nawawi", 1)
    )

    private val _uiState = MutableStateFlow<HadithLibraryUiState>(HadithLibraryUiState.Idle)
    val uiState: StateFlow<HadithLibraryUiState> = _uiState

    private val _selectedBook = MutableStateFlow(books[0])
    val selectedBook: StateFlow<HadithBookInfo> = _selectedBook

    private val _currentSection = MutableStateFlow(1)
    val currentSection: StateFlow<Int> = _currentSection

    init {
        loadSection(books[0], 1)
    }

    fun selectBook(book: HadithBookInfo) {
        _selectedBook.value = book
        _currentSection.value = 1
        loadSection(book, 1)
    }

    fun nextSection() {
        val current = _currentSection.value
        val max = _selectedBook.value.maxSections
        if (current < max) {
            val next = current + 1
            _currentSection.value = next
            loadSection(_selectedBook.value, next)
        }
    }

    fun prevSection() {
        val current = _currentSection.value
        if (current > 1) {
            val prev = current - 1
            _currentSection.value = prev
            loadSection(_selectedBook.value, prev)
        }
    }

    fun loadSection(book: HadithBookInfo, section: Int) {
        viewModelScope.launch {
            _uiState.value = HadithLibraryUiState.Loading
            try {
                val response = HadithApiClient.service.getHadithSection(book.editionName, section)
                val hadiths = response.hadiths
                if (!hadiths.isNullOrEmpty()) {
                    _uiState.value = HadithLibraryUiState.Success(
                        name = book.bengaliName,
                        hadiths = hadiths,
                        currentSection = section,
                        metadata = response.metadata
                    )
                } else {
                    _uiState.value = HadithLibraryUiState.Error("কোন হাদিস পাওয়া যায়নি।")
                }
            } catch (e: Exception) {
                _uiState.value = HadithLibraryUiState.Error("হাদিস লোড করা যায়নি। আপনার ইন্টারনেট সংযোগ চেক করুন।")
            }
        }
    }

    fun searchHadithByNumber(number: Int) {
        val book = _selectedBook.value
        viewModelScope.launch {
            _uiState.value = HadithLibraryUiState.Loading
            try {
                val response = HadithApiClient.service.getSingleHadith(book.editionName, number)
                val hadiths = response.hadiths
                if (!hadiths.isNullOrEmpty()) {
                    _uiState.value = HadithLibraryUiState.Success(
                        name = book.bengaliName,
                        hadiths = hadiths,
                        currentSection = 0, // 0 indicates single hadith search mode
                        metadata = response.metadata
                    )
                } else {
                    _uiState.value = HadithLibraryUiState.Error("হাদিস নম্বর $number পাওয়া যায়নি।")
                }
            } catch (e: Exception) {
                _uiState.value = HadithLibraryUiState.Error("হাদিস নম্বর $number লোড করা যায়নি। সঠিক নম্বর দিন অথবা ইন্টারনেট সংযোগ চেক করুন।")
            }
        }
    }
}
