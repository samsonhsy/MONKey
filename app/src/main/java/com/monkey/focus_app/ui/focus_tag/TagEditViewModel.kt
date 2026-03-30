package com.monkey.focus_app.ui.focus_tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monkey.focus_app.data.AppRepository
import com.monkey.focus_app.data.db.entity.Tag
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val defaultTagPalette = listOf(
    "#FE9F4C",
    "#E35D6A",
    "#FFEB3B",
    "#38C8C2",
    "#5DD39E",
    "#BA68C8",
    "#9FA8DA",
)

data class TagEditUiState(
    val isLoading: Boolean = true,
    val isCreateMode: Boolean = true,
    val tagId: Int? = null,
    val name: String = "",
    val description: String = "",
    val selectedColorHex: String = defaultTagPalette.first(),
    val availableColors: List<String> = defaultTagPalette,
    val restrictedAppCount: Int = 0,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface TagEditEffect {
    data object SaveSuccess : TagEditEffect
    data class NavigateToRestrictApps(val id: String) : TagEditEffect
    data class ShowMessage(val text: String) : TagEditEffect
}

class TagEditViewModel(
    private val repository: AppRepository,
    private val tagIdArg: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagEditUiState())
    val uiState: StateFlow<TagEditUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<TagEditEffect>()
    val effect: SharedFlow<TagEditEffect> = _effect.asSharedFlow()

    private val isCreateMode = tagIdArg == "new"

    init {
        _uiState.value = _uiState.value.copy(isCreateMode = isCreateMode)
        if (isCreateMode) {
            _uiState.value = _uiState.value.copy(isLoading = false)
        } else {
            loadExistingTag()
        }
    }

    private fun loadExistingTag() {
        val id = tagIdArg.toIntOrNull()
        if (id == null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Invalid tag id"
            )
            return
        }

        viewModelScope.launch {
            val tag = repository.getTagsById(id)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isCreateMode = false,
                tagId = tag.tagID,
                name = tag.tagName,
                selectedColorHex = tag.colorHex,
                restrictedAppCount = tag.packageNames.size,
                errorMessage = null
            )
        }
    }

    fun onNameChanged(value: String) {
        _uiState.value = _uiState.value.copy(name = value)
    }

    fun onDescriptionChanged(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun onColorSelected(hex: String) {
        _uiState.value = _uiState.value.copy(selectedColorHex = hex)
    }

    fun onConfigureAppsClicked() {
        viewModelScope.launch {
            val target = _uiState.value.tagId?.toString() ?: "new"
            _effect.emit(TagEditEffect.NavigateToRestrictApps(target))
        }
    }

    fun onSaveClicked() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            viewModelScope.launch { _effect.emit(TagEditEffect.ShowMessage("Tag name is required")) }
            return
        }

        _uiState.value = state.copy(isSaving = true)
        viewModelScope.launch {
            try {
                val existingPackages = if (state.isCreateMode) {
                    emptyList()
                } else {
                    repository.getTagsById(state.tagId ?: return@launch).packageNames
                }

                val tag = Tag(
                    tagID = state.tagId ?: 0,
                    tagName = state.name.trim(),
                    colorHex = state.selectedColorHex,
                    packageNames = existingPackages
                )

                if (state.isCreateMode) {
                    repository.insertAllTag(tag)
                } else {
                    repository.updateAllTag(tag)
                }

                _uiState.value = _uiState.value.copy(isSaving = false)
                _effect.emit(TagEditEffect.SaveSuccess)
            } catch (throwable: Throwable) {
                _uiState.value = _uiState.value.copy(isSaving = false)
                _effect.emit(TagEditEffect.ShowMessage(throwable.message ?: "Failed to save tag"))
            }
        }
    }

}
