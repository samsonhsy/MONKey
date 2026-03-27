package com.monkey.focus_app.ui.navigation

enum class Screen() {
    WARNING,
    NOVICE
}

sealed class NavigationItem(val route: String){
    object Warning : NavigationItem(Screen.WARNING.name)
    object Novice : NavigationItem(Screen.NOVICE.name)
}