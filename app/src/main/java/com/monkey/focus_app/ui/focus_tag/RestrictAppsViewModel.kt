package com.monkey.focus_app.ui.focus_tag

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
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
    val icon: Drawable?,
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

class RestrictAppsViewModel(
    private val repository: AppRepository,
    private val tagIdArg: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RestrictAppsUiState())
    val uiState: StateFlow<RestrictAppsUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<RestrictAppsEffect>()
    val effect: SharedFlow<RestrictAppsEffect> = _effect.asSharedFlow()

    private val tagId: Int? = tagIdArg.toIntOrNull()
    private var allApps: List<RestrictAppUi> = emptyList()

    init {
        loadInitialSelection()
    }

    private fun loadInitialSelection() {
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
                    selectedPackages = tag.packageNames.toSet(),
                    errorMessage = null
                )
            } catch (throwable: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = throwable.message ?: "Failed to load selected apps"
                )
            }
        }
    }

    fun loadInstalledApps(packageManager: PackageManager) {
        viewModelScope.launch {
            try {
                val apps = getInstalledApplicationsCompat(packageManager)
                    .map { appInfo ->
                        RestrictAppUi(
                            packageName = appInfo.packageName,
                            appName = packageManager.getApplicationLabel(appInfo).toString(),
                            icon = appInfo.loadIcon(packageManager)
                        )
                    }
                    .sortedBy { it.appName.lowercase() }

                allApps = apps

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    apps = filterApps(_uiState.value.query),
                    errorMessage = null
                )
            } catch (throwable: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = throwable.message ?: "Failed to load installed apps"
                )
                _effect.emit(RestrictAppsEffect.ShowMessage("Failed to load apps"))
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
        if (query.isBlank()) return allApps
        return allApps.filter {
            it.appName.contains(query, ignoreCase = true) ||
                it.packageName.contains(query, ignoreCase = true)
        }
    }

    private fun getInstalledApplicationsCompat(pm: PackageManager): List<ApplicationInfo> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledApplications(0)
        }
    }
}
