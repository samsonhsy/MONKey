package com.monkey.focus_app.ui.navigation

enum class Screen() {
    WARNING,
    UNLOCK
}

sealed class NavigationItem(val route: String){
    object Warning : NavigationItem(Screen.WARNING.name)
    object Unlock : NavigationItem(Screen.UNLOCK.name)
}