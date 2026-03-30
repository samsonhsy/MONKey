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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class TagListItemUi(
    val id: Int,
    val title: String,
    val subtitle: String,
    val colorHex: String,
    val appCount: Int,
)

data class TagListUiState(
    val isLoading: Boolean = true,
    val tags: List<TagListItemUi> = emptyList(),
    val isDeleteMode: Boolean = false,
    val pendingDeleteTagId: Int? = null,
    val errorMessage: String? = null,
)

sealed interface TagListEffect {
    data object NavigateToCreate : TagListEffect
    data class NavigateToEdit(val id: Int) : TagListEffect
    data class ShowMessage(val text: String) : TagListEffect
}

class TagListViewModel(
    private val repository: AppRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagListUiState())
    val uiState: StateFlow<TagListUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<TagListEffect>()
    val effect: SharedFlow<TagListEffect> = _effect.asSharedFlow()

    init {
        observeTags()
    }

    private fun observeTags() {
        viewModelScope.launch {
            repository.getAllTag()
                .catch { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Failed to load tags"
                    )
                }
                .collect { tags ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        tags = tags.map { it.toUi() },
                        errorMessage = null
                    )
                }
        }
    }

    fun onAddClicked() {
        viewModelScope.launch {
            _effect.emit(TagListEffect.NavigateToCreate)
        }
    }

    fun onEditClicked(id: Int) {
        viewModelScope.launch {
            _effect.emit(TagListEffect.NavigateToEdit(id))
        }
    }

    fun onToggleDeleteMode() {
        _uiState.value = _uiState.value.copy(
            isDeleteMode = !_uiState.value.isDeleteMode,
            pendingDeleteTagId = null
        )
    }

    fun onRequestDelete(tagId: Int) {
        _uiState.value = _uiState.value.copy(pendingDeleteTagId = tagId)
    }

    fun onDismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(pendingDeleteTagId = null)
    }

    fun onConfirmDelete() {
        viewModelScope.launch {
            val id = _uiState.value.pendingDeleteTagId ?: return@launch
            val target = repository.getTagsById(id)
            repository.deleteTag(target)
            _uiState.value = _uiState.value.copy(pendingDeleteTagId = null)
            _effect.emit(TagListEffect.ShowMessage("Tag deleted"))
        }
    }

    private fun Tag.toUi(): TagListItemUi {
        val cleanName = tagName.trim()
        val cleanSubtitle = tagSubtitle.trim()
        val subtitle = cleanSubtitle.ifEmpty {
            "No subtitle"
        }

        return TagListItemUi(
            id = tagID,
            title = cleanName.ifEmpty { "Untitled" },
            subtitle = subtitle,
            colorHex = colorHex,
            appCount = packageNames.size
        )
    }
}
