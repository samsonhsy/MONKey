package com.monkey.focus_app.ui.warning

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AppShortcut
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.monkey.focus_app.ui.theme.BrandOrange
import com.monkey.focus_app.ui.theme.MONKeyTheme
import com.monkey.focus_app.ui.theme.TextGreyLight

//@Composable
//fun Warning(modifier: Modifier = Modifier,
//            navController: NavHostController,
//            onStayFocus: () -> Unit,
//            onLeaveFocus: () -> Unit) {
//    Surface(
//        color = MaterialTheme.colorScheme.background,
//    )
//    {
//        Column(
//            modifier = Modifier.fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            Spacer(
//                modifier = Modifier.weight(0.2f)
//            )
//            Icon(
//                Icons.Outlined.Warning,
//                contentDescription = "Warning",
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(250.dp)
//            )
//            Text(
//                text = ("Are you sure that you want to leave focus mode? \n You will lose your reward if the time is not yet the end time.").uppercase(
//                ),
//                modifier = Modifier.padding(50.dp, 0.dp),
//                textAlign = TextAlign.Center,
//                style = MaterialTheme.typography.headlineSmall
//            )
//            Spacer(
//                Modifier.weight(0.2f)
//            )
//            Button(
//                content = {
//                    Text(
//                        text = "Stay Focus",
//                        style = MaterialTheme.typography.headlineSmall
//                    )
//                },
//                onClick = onStayFocus,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(50.dp, 0.dp)
//                    .height(50.dp)
//            )
//            Spacer(
//                Modifier.weight(0.05f)
//            )
//            Button(
//                content = {
//                    Text(
//                        text = "Yes, I am leaving",
//                        style = MaterialTheme.typography.headlineSmall
//                    )
//                },
//                onClick = onLeaveFocus,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(50.dp, 0.dp)
//                    .height(50.dp)
//            )
//            Spacer(
//                modifier = Modifier.weight(0.3f)
//            )
//        }
//    }
//}

@Composable
fun WarningEntryScreen(
    blockedPackage: String,
    unlockLevel: String,
    onBackToFocus: () -> Unit,
    onUnlock: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.2f))
            Icon(
                Icons.Outlined.Warning,
                contentDescription = "Warning",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
            Text(
                text = ("BLOCKED: $blockedPackage\nMode: $unlockLevel").uppercase(),
                modifier = Modifier.padding(50.dp, 0.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.weight(0.2f))
            Button(
                content = {
                    Text(
                        text = "Back to Focus",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                onClick = onBackToFocus,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(50.dp, 0.dp)
                    .height(50.dp)
            )
            Spacer(modifier = Modifier.weight(0.05f))
            Button(
                content = {
                    Text(
                        text = "Unlock",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                onClick = onUnlock,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(50.dp, 0.dp)
                    .height(50.dp)
            )
            Spacer(modifier = Modifier.weight(0.3f))
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Unlock(modifier: Modifier,
           navController: NavHostController,
           state: WarningUiState,
           onTextChanged: (String) -> Unit,
           onSubmit: () -> Unit,
           onShakeStep: () -> Unit,
           unlockPhrase: String,
           unlockLevel: String,
           onCancel: () -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (unlockLevel.uppercase() == "BHIKKHU"){
                    StepProgressBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 50.dp),
                        numberOfSteps = 1,
                        currentStep = state.currentStep
                    )
                }
            }
        }
    ) {
        if (state.currentStep == 0) {
            Novice(
                modifier = modifier,
                typedText = state.typedText,
                onTextChanged = onTextChanged,
                onSubmit = onSubmit,
                unlockPhrase = unlockPhrase,
            )
        } else {
            Bhikkhu(
                modifier = modifier,
                shakeCount = state.shakeCount,
                shakeProgress = state.shakeProgress,
                onShakeStep = onShakeStep
            )
        }
    }
}

/***
Copyright 2020 MakeItEasyDev

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 ***/

@Composable
fun Step(
    modifier: Modifier = Modifier,
    isComplete: Boolean,
    isCurrent: Boolean,
    currentStep: Int
) {
    val color = if (isComplete || isCurrent) BrandOrange else TextGreyLight
    val innerCircleColor = if (isComplete) BrandOrange else TextGreyLight

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (currentStep == 0) {
            HorizontalDivider(
                modifier = Modifier
                    .align(
                        Alignment.CenterEnd
                    )
                    .width(100.dp),
                thickness = 5.dp,
                color = color
            )
            Text(
                modifier = Modifier.absoluteOffset(0.dp, (-22).dp),
                text = "Novice",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = BrandOrange
            )
        }

        if (currentStep == 1) {

            HorizontalDivider(
                modifier = Modifier
                    .align(
                        Alignment.CenterStart
                    )
                    .width(100.dp),
                thickness = 5.dp,
                color = color
            )
            Text(
                modifier = Modifier.absoluteOffset(0.dp, (-22).dp),
                text = "Bhikkhu",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
        }


        //Stage step circle
        Canvas(
            modifier = Modifier
                .size(25.dp)
                .align(Alignment.Center)
                .border(
                    shape = CircleShape,
                    width = 5.dp,
                    color = color
                ),
            onDraw = {
                drawCircle(
                    color = innerCircleColor
                )
            }
        )
    }
}

@Composable
fun StepProgressBar(
    modifier: Modifier = Modifier,
    numberOfSteps: Int,
    currentStep: Int
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (step in 0..numberOfSteps) {
            Step(
                isComplete = step < currentStep,
                isCurrent = step == currentStep,
                modifier = Modifier.weight(1f),
                currentStep = step
            )
        }

    }
}

@Composable
fun Novice(
    modifier: Modifier,
    typedText: String,
    onTextChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    unlockPhrase: String
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(25.dp, 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(
                modifier = Modifier.weight(0.3f)
            )
            Text(
                text = buildAnnotatedString {
                    append("Type\n ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("\"" + unlockPhrase + "\"" )
                    }
                    append(" \nto unlock.")
                },
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 10.dp),
                value = typedText,
                onValueChange = onTextChanged,
                label = { Text("Enter here") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = ProgressIndicatorDefaults.linearTrackColor,
                    unfocusedContainerColor = ProgressIndicatorDefaults.linearTrackColor
                )
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                onClick = onSubmit
            ) {
                Text(
                    text = "Enter",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Spacer(
                modifier = Modifier.weight(0.3f)
            )
        }
    }
}

@Composable
fun Bhikkhu(
    modifier: Modifier,
    shakeCount: Int,
    shakeProgress: Float,
    onShakeStep: () -> Unit
) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val detector = ShakeDetector(context) {
            onShakeStep()
        }
        detector.start()

        onDispose {
            detector.stop()
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(25.dp, 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(
                modifier = Modifier.weight(0.3f)
            )
            Text(
                text = "Shake your cell phone for 100 times to unlock",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(
                modifier = Modifier.weight(0.1f)
            )
            Icon(
                Icons.Outlined.AppShortcut,
                contentDescription = "Shake Phone",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
            Spacer(
                modifier = Modifier.size(30.dp)
            )
            Text(
                text = if (shakeCount < 100) "${shakeCount}/100" else "Done!",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = BrandOrange
            )
            Spacer(
                modifier = Modifier.size(15.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(
                        color = ProgressIndicatorDefaults.linearTrackColor,
                        shape = RoundedCornerShape(50)
                    )
                    .padding(2.dp)
            ) {
                LinearProgressIndicator(
                    progress = { shakeProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                )
            }
            Spacer(
                modifier = Modifier.weight(0.3f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WarningEntryScreenPreviewLight() {
    MONKeyTheme {
        WarningEntryScreen(
            blockedPackage = "",
            unlockLevel = "NOVICE",
            onBackToFocus = {},
            onUnlock = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WarningEntryScreenPreviewDark() {
    MONKeyTheme(darkTheme = true) {
        WarningEntryScreen(
            blockedPackage = "",
            unlockLevel = "NOVICE",
            onBackToFocus = {},
            onUnlock = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NovicePreviewLight() {
    MONKeyTheme {
        Novice(
            modifier = Modifier,
            typedText = "",
            onTextChanged = {},
            onSubmit = {},
            unlockPhrase = "I have decided not to focus and be addicted to my cell phone again."
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NovicePreviewDark() {
    MONKeyTheme(darkTheme = true) {
        Novice(
            modifier = Modifier,
            typedText = "",
            onTextChanged = {},
            onSubmit = {},
            unlockPhrase = "I have decided not to focus and be addicted to my cell phone again."
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BhikkhuPreviewLight() {
    MONKeyTheme {
        Bhikkhu(
            modifier = Modifier,
            shakeCount = 0,
            shakeProgress = 0f,
            onShakeStep = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BhikkhuPreviewDark() {
    MONKeyTheme(darkTheme = true) {
        Bhikkhu(
            modifier = Modifier,
            shakeCount = 0,
            shakeProgress = 0f,
            onShakeStep = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UnlockPreviewLight() {
    MONKeyTheme(darkTheme = false) {
        Unlock(
            modifier = Modifier,
            navController = rememberNavController(),
            state = WarningUiState(),
            onTextChanged = {},
            onSubmit = {},
            onShakeStep = {},
            unlockPhrase = "I have decided not to focus and be addicted to my cell phone again.",
            unlockLevel = "Novice",
            onCancel = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UnlockPreviewDark() {
    MONKeyTheme(darkTheme = true) {
        Unlock(
            modifier = Modifier,
            navController = rememberNavController(),
            state = WarningUiState(currentStep = 1, shakeCount = 50, shakeProgress = 0.5f),
            onTextChanged = {},
            onSubmit = {},
            onShakeStep = {},
            unlockPhrase = "I have decided not to focus and be addicted to my cell phone again.",
            unlockLevel = "BHIKKHU",
            onCancel = {}
        )
    }
}