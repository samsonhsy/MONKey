package com.monkey.focus_app.ui.focus_tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.monkey.focus_app.data.AppRepository

class TagEditViewModelFactory(
    private val repository: AppRepository,
    private val tagIdArg: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TagEditViewModel::class.java)) {
            return TagEditViewModel(repository, tagIdArg) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
