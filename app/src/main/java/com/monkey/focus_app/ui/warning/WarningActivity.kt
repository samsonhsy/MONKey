package com.monkey.focus_app.ui.warning

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.monkey.focus_app.service.focus.FocusActions
import com.monkey.focus_app.service.focus.FocusRuntimeStore
import com.monkey.focus_app.ui.theme.MONKeyTheme

class WarningActivity : ComponentActivity() {

    private val viewModel: WarningViewModel by viewModels {
        viewModelFactory {
            initializer {
                WarningViewModel(
                    appContext = this@WarningActivity.applicationContext,
                    sessionId = intent.getIntExtra(FocusActions.EXTRA_SESSION_ID, -1),
                    blockedPackage = intent.getStringExtra(FocusActions.EXTRA_BLOCKED_PACKAGE).orEmpty(),
                    unlockLevel = intent.getStringExtra(FocusActions.EXTRA_UNLOCK_LEVEL) ?: "NOVICE"
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.onNewBlockedApp(
            sessionId = intent.getIntExtra(FocusActions.EXTRA_SESSION_ID, -1),
            blockedPackage = intent.getStringExtra(FocusActions.EXTRA_BLOCKED_PACKAGE).orEmpty(),
            unlockLevel = intent.getStringExtra(FocusActions.EXTRA_UNLOCK_LEVEL) ?: "NOVICE"
        )
    }

    override fun onStart() {
        super.onStart()
        FocusRuntimeStore.setWarningVisible(true)
    }

    override fun onStop() {
        FocusRuntimeStore.setWarningVisible(false)
        super.onStop()
    }

    override fun onDestroy() {
        FocusRuntimeStore.setWarningVisible(false)
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MONKeyTheme {
                val context = LocalContext.current
                val navController = rememberNavController()
                val state by viewModel.state.collectAsState()

                LaunchedEffect(Unit) {
                    viewModel.effect.collect { effect ->
                        when (effect) {
                            WarningEffect.NavigateToDeviceHome -> {
                                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                    addCategory(Intent.CATEGORY_HOME)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(homeIntent)
                                finish()
                            }
                            WarningEffect.CloseWarning -> finish()
                            WarningEffect.NavigateToUnlock -> {
                                navController.navigate("unlock")
                            }
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "warning_entry"
                    ) {
                        composable("warning_entry") {
                            WarningEntryScreen(
                                blockedPackage = state.blockedPackage,
                                unlockLevel = state.unlockLevel,
                                onBackToFocus = viewModel::onBackToFocusClicked,
                                onUnlock = viewModel::onUnlockClicked
                            )
                        }
                        composable("unlock") {
                            Unlock(
                                modifier = Modifier.padding(innerPadding),
                                navController = navController,
                                state = state,
                                onTextChanged = viewModel::onTypedTextChanged,
                                onSubmit = viewModel::onSubmitUnlock,
                                onShakeStep = viewModel::onShakeStepCompleted,
                                unlockPhrase = viewModel.unlockPhrase,
                                unlockLevel = viewModel.unlockLevel,
                                onCancel = {
                                    viewModel.onCancelUnlock()
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
