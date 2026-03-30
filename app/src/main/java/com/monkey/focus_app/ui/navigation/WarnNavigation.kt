package com.monkey.focus_app.ui.navigation

enum class Screen() {
    WARNING,
    NOVICE,
    BHIKKHU
}

sealed class NavigationItem(val route: String){
    object Warning : NavigationItem(Screen.WARNING.name)
    object Novice : NavigationItem(Screen.NOVICE.name)
    object Bhikkhu : NavigationItem(Screen.BHIKKHU.name)
}