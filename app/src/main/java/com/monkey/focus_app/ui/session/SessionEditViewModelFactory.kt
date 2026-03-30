package com.monkey.focus_app.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.monkey.focus_app.data.AppRepository

class SessionEditViewModelFactory(
    private val repository: AppRepository,
    private val sessionIdArg: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionEditViewModel::class.java)) {
            return SessionEditViewModel(repository, sessionIdArg) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
