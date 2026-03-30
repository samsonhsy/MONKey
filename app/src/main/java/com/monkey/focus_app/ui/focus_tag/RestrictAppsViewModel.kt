package com.monkey.focus_app.ui.focus_tag

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monkey.focus_app.data.AppRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RestrictAppUi(
    val packageName: String,
    val appName: String,
    val category: String,
    val logoColor: Color,
)

data class RestrictAppsUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val apps: List<RestrictAppUi> = emptyList(),
    val selectedPackages: Set<String> = emptySet(),
    val errorMessage: String? = null,
)

sealed interface RestrictAppsEffect {
    data object SaveSuccess : RestrictAppsEffect
    data class ShowMessage(val text: String) : RestrictAppsEffect
}

private val demoApps = listOf(
    RestrictAppUi("com.instagram.android", "Instagram", "Social", Color(0xFFF2746B)),
    RestrictAppUi("com.zhiliaoapp.musically", "TikTok", "Entertainment", Color(0xFF38C8C2)),
    RestrictAppUi("com.reddit.frontpage", "Reddit", "Social", Color(0xFFFE9F4C)),
    RestrictAppUi("com.google.android.youtube", "YouTube", "Video", Color(0xFFE35D6A)),
    RestrictAppUi("com.discord", "Discord", "Communication", Color(0xFF9FA8DA)),
    RestrictAppUi("com.facebook.katana", "Facebook", "Social", Color(0xFF4FB2F8)),
    RestrictAppUi("com.twitter.android", "X", "Social", Color(0xFF90A4AE)),
)

class RestrictAppsViewModel(
    private val repository: AppRepository,
    private val tagIdArg: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RestrictAppsUiState())
    val uiState: StateFlow<RestrictAppsUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<RestrictAppsEffect>()
    val effect: SharedFlow<RestrictAppsEffect> = _effect.asSharedFlow()

    private val tagId: Int? = tagIdArg.toIntOrNull()

    init {
        loadInitialState()
    }

    private fun loadInitialState() {
        val id = tagId
        if (id == null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Invalid tag id"
            )
            return
        }

        viewModelScope.launch {
            try {
                val tag = repository.getTagsById(id)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    apps = filterApps(query = ""),
                    selectedPackages = tag.packageNames.toSet(),
                    errorMessage = null
                )
            } catch (throwable: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = throwable.message ?: "Failed to load apps"
                )
            }
        }
    }

    fun onQueryChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            query = value,
            apps = filterApps(value)
        )
    }

    fun onTogglePackage(packageName: String, checked: Boolean) {
        val current = _uiState.value.selectedPackages
        val updated = if (checked) current + packageName else current - packageName
        _uiState.value = _uiState.value.copy(selectedPackages = updated)
    }

    fun onConfirmSelection() {
        val id = tagId
        if (id == null) {
            viewModelScope.launch {
                _effect.emit(RestrictAppsEffect.ShowMessage("Invalid tag id"))
            }
            return
        }

        viewModelScope.launch {
            try {
                val existing = repository.getTagsById(id)
                val updated = existing.copy(packageNames = _uiState.value.selectedPackages.toList().sorted())
                repository.updateAllTag(updated)
                _effect.emit(RestrictAppsEffect.SaveSuccess)
            } catch (throwable: Throwable) {
                _effect.emit(RestrictAppsEffect.ShowMessage(throwable.message ?: "Failed to save app restrictions"))
            }
        }
    }

    private fun filterApps(query: String): List<RestrictAppUi> {
        return demoApps.filter {
            it.appName.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true) ||
                it.packageName.contains(query, ignoreCase = true)
        }
    }
}
