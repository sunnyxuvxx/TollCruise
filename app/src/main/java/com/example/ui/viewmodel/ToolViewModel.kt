package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.FavoriteTool
import com.example.data.model.HistoryItem
import com.example.data.model.UserBilling
import com.example.data.repository.ToolRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ToolViewModel(private val repository: ToolRepository) : ViewModel() {

    // Billing Flow from Repository (maps null to a default UserBilling object)
    val userBilling: StateFlow<UserBilling> = repository.userBilling
        .map { it ?: UserBilling() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserBilling()
        )

    // UI Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Selected Category
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Selected Active Tool ID (null = showing home dashboard)
    private val _activeToolId = MutableStateFlow<String?>(null)
    val activeToolId: StateFlow<String?> = _activeToolId.asStateFlow()

    // History Flow from Repository
    val historyItems: StateFlow<List<HistoryItem>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Favorite Tools Map Flow for fast lookup
    val favoriteToolIds: StateFlow<Set<String>> = repository.favoriteTools
        .map { list -> list.filter { it.isFavorite }.map { it.toolId }.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setActiveTool(toolId: String?) {
        _activeToolId.value = toolId
    }

    fun addHistory(toolId: String, toolName: String, actionType: String, details: String) {
        viewModelScope.launch {
            repository.insertHistory(
                HistoryItem(
                    toolId = toolId,
                    toolName = toolName,
                    actionType = actionType,
                    details = details
                )
            )
            // Auto-deduct 1 processing credit if it is a developer utility run and user is on Free Tier
            if (toolId != "billing") {
                val currentBilling = userBilling.value
                if (currentBilling.subscriptionPlan == "Free" && currentBilling.credits > 0) {
                    val updated = currentBilling.copy(credits = currentBilling.credits - 1)
                    repository.updateUserBilling(updated)
                }
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun toggleFavorite(toolId: String, isCurrentlyFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(toolId, !isCurrentlyFavorite)
        }
    }

    fun deductCredit(toolName: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        viewModelScope.launch {
            val currentBilling = userBilling.value
            if (currentBilling.subscriptionPlan != "Free") {
                // Unlimited access, does NOT deduct credits
                onSuccess()
            } else if (currentBilling.credits > 0) {
                val updatedBilling = currentBilling.copy(credits = currentBilling.credits - 1)
                repository.updateUserBilling(updatedBilling)
                addHistory(
                    toolId = "billing",
                    toolName = toolName,
                    actionType = "Execution",
                    details = "Used 1 processing token (Remaining: ${updatedBilling.credits})"
                )
                onSuccess()
            } else {
                onFailure()
            }
        }
    }

    fun purchaseSubscription(plan: String, price: Double, paymentMethod: String) {
        viewModelScope.launch {
            val currentBilling = userBilling.value
            val expiry = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000) // 30 days
            val updatedBilling = currentBilling.copy(
                subscriptionPlan = plan,
                subscriptionExpiry = expiry
            )
            repository.updateUserBilling(updatedBilling)
            addHistory(
                toolId = "billing",
                toolName = "Billing Suite",
                actionType = "Subscription",
                details = "Subscribed to $plan ($$price/mo) via $paymentMethod"
            )
        }
    }

    fun purchaseCredits(addedCredits: Int, price: Double, paymentMethod: String) {
        viewModelScope.launch {
            val currentBilling = userBilling.value
            val updatedBilling = currentBilling.copy(
                credits = currentBilling.credits + addedCredits
            )
            repository.updateUserBilling(updatedBilling)
            addHistory(
                toolId = "billing",
                toolName = "Billing Suite",
                actionType = "Top-up",
                details = "Refilled wallet with $addedCredits tokens ($$price) via $paymentMethod"
            )
        }
    }

    fun cancelSubscription() {
        viewModelScope.launch {
            val currentBilling = userBilling.value
            val updatedBilling = currentBilling.copy(
                subscriptionPlan = "Free",
                subscriptionExpiry = 0L
            )
            repository.updateUserBilling(updatedBilling)
            addHistory(
                toolId = "billing",
                toolName = "Billing Suite",
                actionType = "Subscription",
                details = "Cancelled subscription, set plan to Free tier"
            )
        }
    }
}

class ToolViewModelFactory(private val repository: ToolRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ToolViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ToolViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
