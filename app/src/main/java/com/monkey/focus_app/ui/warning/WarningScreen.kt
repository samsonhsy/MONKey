package com.monkey.focus_app.ui.warning

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.TextUtils.equals
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.monkey.focus_app.MainActivity
import com.monkey.focus_app.ui.navigation.NavigationItem
import com.monkey.focus_app.ui.theme.BrandOrange
import com.monkey.focus_app.ui.theme.MONKeyTheme
import com.monkey.focus_app.ui.theme.TextGreyLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Warning(modifier: Modifier = Modifier, navController: NavHostController) {
    val context = LocalContext.current
    Surface(
        color = MaterialTheme.colorScheme.background,
    )
    {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(
                modifier = Modifier.weight(0.2f)
            )
            Icon(
                Icons.Outlined.Warning,
                contentDescription = "Warning",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
            Text(
                text = ("Are you sure that you want to leave focus mode? \n You will lose your reward if the time is not yet the end time.").uppercase(
                ),
                modifier = Modifier.padding(50.dp, 0.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(
                Modifier.weight(0.2f)
            )
            Button(
                content = {
                    Text(
                        text = "Stay Focus",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                onClick = {
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(50.dp, 0.dp)
                    .height(50.dp)
            )
            Spacer(
                Modifier.weight(0.05f)
            )
            Button(
                content = {
                    Text(
                        text = "Yes, I am leaving",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                onClick = { navController.navigate(NavigationItem.Unlock.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(50.dp, 0.dp)
                    .height(50.dp)
            )
            Spacer(
                modifier = Modifier.weight(0.3f)
            )
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Unlock(modifier: Modifier, navController: NavHostController, unlockLevel: Int) {
    var currentStep = remember { mutableIntStateOf(0) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                StepProgressBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 50.dp),
                    numberOfSteps = 1,
                    currentStep = currentStep.intValue
                )
            }
        }
    ) {
        if (currentStep.intValue == 0) {
            Novice(modifier = modifier, unlockLevel = unlockLevel, currentStep = currentStep)
        } else if (currentStep.intValue == 1) {
            Bhikkhu(modifier = modifier)
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
                modifier = Modifier.absoluteOffset(0.dp, (-20).dp),
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
                modifier = Modifier.absoluteOffset(0.dp, (-20).dp),
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
fun Novice(modifier: Modifier, unlockLevel: Int, currentStep: MutableIntState) {
    Surface(
        color = MaterialTheme.colorScheme.background,
    ) {
        var text: String by remember { mutableStateOf("") }
        val unlockPhrase = "I have decided not to focus and be addicted to my cell phone again."
        val context = LocalContext.current
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
                    append("Type ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(unlockPhrase)
                    }
                    append(" to unlock.")
                },
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 10.dp),
                value = text,
                onValueChange = { newText: String ->
                    text = newText
                },
                label = { Text("Enter here") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = ProgressIndicatorDefaults.linearTrackColor,
                    unfocusedContainerColor = ProgressIndicatorDefaults.linearTrackColor
                )
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                onClick = {
                    if (equals(
                            text,
                            unlockPhrase
                        )
                    ) {
                        if (unlockLevel == 2) {
                            currentStep.intValue++
                        } else {
                            val intent = Intent(context, MainActivity::class.java)
                            context.startActivity(intent)
                        }
                    }
                }
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
fun Bhikkhu(modifier: Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.background,
    ) {
        val context = LocalContext.current
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
            Shaking(context = context)
            Spacer(
                modifier = Modifier.weight(0.3f)
            )
        }
    }
}

@Composable
fun Shaking(context: Context) {
    var i by remember { mutableIntStateOf(0) }
    var currentProgress by remember { mutableFloatStateOf(0f) }
    val shakeState = rememberShakingState(
        strength = ShakingState.Strength.Strong,
        direction = ShakingState.Direction.Up_THEN_DOWN
    )
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        scope.launch {
            while (i < 100) {
                shakeState.shake(
                    animationDuration = 100
                )
                currentProgress = (i.toFloat() / 100)
                delay(100)
                i += 1
            }
            delay(1000)
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }

    Icon(
        Icons.Outlined.AppShortcut,
        contentDescription = "Shaking Phone",
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .shakable(state = shakeState)
    )
    Spacer(
        modifier = Modifier.size(30.dp)
    )
    if (i < 100) {
        Text(
            text = i.toString(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            color = BrandOrange
        )
    } else {
        Text(
            text = "Done!",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            color = BrandOrange
        )
    }
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
            .padding(2.dp) // optional inner padding for a gap
    ) {
        LinearProgressIndicator(
            progress = { currentProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WarningPreviewLight() {
    MONKeyTheme {
        Warning(
            modifier = Modifier,
            navController = rememberNavController(),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WarningPreviewDark() {
    MONKeyTheme(darkTheme = true) {
        Warning(
            modifier = Modifier,
            navController = rememberNavController(),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NovicePreviewLight() {
    val currentStep = remember { mutableIntStateOf(0) }
    MONKeyTheme {
        Novice(
            modifier = Modifier,
            currentStep = currentStep,
            unlockLevel = 2,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NovicePreviewDark() {
    MONKeyTheme(darkTheme = true) {
        val currentStep = remember { mutableIntStateOf(0) }
        Novice(
            modifier = Modifier,
            currentStep = currentStep,
            unlockLevel = 2,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BhikkhuPreviewLight() {
    MONKeyTheme {
        Bhikkhu(modifier = Modifier)
    }
}

@Preview(showBackground = true)
@Composable
fun BhikkhuPreviewDark() {
    MONKeyTheme(darkTheme = true) {
        Bhikkhu(modifier = Modifier)
    }
}

@Preview(showBackground = true)
@Composable
fun UnlockPreviewLight() {
    MONKeyTheme(darkTheme = false) {
        Unlock(
            modifier = Modifier,
            navController = rememberNavController(),
            unlockLevel = 1,
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
            unlockLevel = 2,
        )
    }
}