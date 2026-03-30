package com.monkey.focus_app.ui.focus_tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.monkey.focus_app.data.AppRepository

class TagListViewModelFactory(
    private val repository: AppRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TagListViewModel::class.java)) {
            return TagListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
