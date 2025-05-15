package com.example.mygwent

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mygwent.data.Card
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CardViewModel : ViewModel() {
    private val repository = CardRepository()

    // Estados de carga
    sealed class LoadingState {
        object Idle : LoadingState()
        object LoadingCards : LoadingState()
        data class LoadingPage(val page: Int) : LoadingState()
        data class Error(val message: String) : LoadingState()
    }

    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.Idle)
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    // Datos
    private val _allCards = MutableStateFlow<List<Card>>(emptyList())
    val allCards: StateFlow<List<Card>> = _allCards.asStateFlow()

    private val _currentPageCards = MutableStateFlow<List<Card>>(emptyList())
    val currentPageCards: StateFlow<List<Card>> = _currentPageCards.asStateFlow()

    // Paginación
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _totalPages = MutableStateFlow(0)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    private val pageSize = 50

    init {
        loadAllCards()
    }

    fun loadAllCards() {
        _loadingState.value = LoadingState.LoadingCards
        viewModelScope.launch {
            try {
                val cards = repository.getCards()
                _allCards.value = cards
                _totalPages.value = calculateTotalPages(cards.size)
                updateCurrentPageCards()
                _loadingState.value = LoadingState.Idle
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error("Error loading cards: ${e.message}")
                Log.e("CardViewModel", "Error loading cards", e)
            }
        }
    }

    fun loadPage(page: Int) {
        if (page < 0 || page >= _totalPages.value) return

        _loadingState.value = LoadingState.LoadingPage(page)
        viewModelScope.launch {
            try {
                _currentPage.value = page
                updateCurrentPageCards()
                _loadingState.value = LoadingState.Idle
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error("Error loading page $page: ${e.message}")
                Log.e("CardViewModel", "Error loading page", e)
            }
        }
    }

    // Métodos auxiliares
    fun clearError() {
        if (_loadingState.value is LoadingState.Error) {
            _loadingState.value = LoadingState.Idle
        }
    }

    fun retryLastAction() {
        when (val state = _loadingState.value) {
            is LoadingState.Error -> {
                when (state.message) {
                    "Error loading cards" -> loadAllCards()
                    else -> loadPage(_currentPage.value)
                }
            }
            else -> {}
        }
    }

    private fun calculateTotalPages(totalItems: Int): Int {
        return if (totalItems == 0) 0 else (totalItems - 1) / pageSize + 1
    }

    private fun updateCurrentPageCards() {
        val allCards = _allCards.value
        val start = _currentPage.value * pageSize
        val end = minOf(start + pageSize, allCards.size)

        _currentPageCards.value = if (start < allCards.size) {
            allCards.subList(start, end)
        } else {
            emptyList()
        }
    }

    fun prevPage() {
        if (_currentPage.value > 0) {
            loadPage(_currentPage.value - 1)
        }
    }

    fun nextPage() {
        if (_currentPage.value < _totalPages.value - 1) {
            loadPage(_currentPage.value + 1)
        }
    }
}