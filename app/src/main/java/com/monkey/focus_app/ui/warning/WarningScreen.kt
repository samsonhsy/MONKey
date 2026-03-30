package com.monkey.focus_app.ui.warning

import android.content.Intent
import android.text.TextUtils.equals
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AppShortcut
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.monkey.focus_app.ui.theme.MONKeyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Warning(modifier: Modifier = Modifier, navController: NavHostController, modeName: String) {
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
                onClick = { navController.navigate(modeName) },
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

@Composable
fun Novice(modifier: Modifier = Modifier, navController: NavHostController) {
    Surface(
        color = MaterialTheme.colorScheme.background,
    ) {
        var text: String by remember { mutableStateOf("") }
        val unlockPhrase = "I have decided not to focus and be addicted to my cell phone again."
        val context = LocalContext.current
        Column(
            modifier = modifier
                .padding(10.dp, 0.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
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
                label = { Text("Enter here") }
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
                        val intent = Intent(context, MainActivity::class.java)
                        context.startActivity(intent)
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
fun Bhikkhu(modifier: Modifier, navController: NavHostController) {
    Surface(
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(50.dp, 0.dp),
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
            Shaking()
            Spacer(
                modifier = Modifier.weight(0.3f)
            )
        }
    }
}

@Composable
fun Shaking() {
    var i = 0
    var currentProgress by remember { mutableFloatStateOf(0f) }
    val shakeState = rememberShakingState(
        strength = ShakingState.Strength.Custom((i * 23 / 100).toFloat() + 17f),
        direction = ShakingState.Direction.Up_THEN_DOWN
    )
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        scope.launch {
            while (i < 100) {
                shakeState.shake(
                    animationDuration = (i + 1) * 400 / 100
                )
                currentProgress = (i.toFloat() / 100)
                delay(100)
                i += 1
            }
        }
    }

    Icon(
        Icons.Outlined.AppShortcut,
        contentDescription = "Shaking Phone",
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp - (i * 250 / 100).dp)
            .shakable(state = shakeState)
    )
    Spacer(
        modifier = Modifier.size(50.dp)
    )
    LinearProgressIndicator(
        progress = { currentProgress },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
    )
}

@Preview(showBackground = true)
@Composable
fun WarningPreviewLight() {
    MONKeyTheme {
        Warning(
            modifier = Modifier,
            navController = rememberNavController(),
            modeName = NavigationItem.Warning.route
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
            modeName = NavigationItem.Warning.route
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NovicePreviewLight() {
    MONKeyTheme {
        Novice(modifier = Modifier, navController = rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun NovicePreviewDark() {
    MONKeyTheme(darkTheme = true) {
        Novice(modifier = Modifier, navController = rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun BhikkhuPreviewLight() {
    MONKeyTheme {
        Bhikkhu(modifier = Modifier, navController = rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun BhikkhuPreviewDark() {
    MONKeyTheme(darkTheme = true) {
        Bhikkhu(modifier = Modifier, navController = rememberNavController())
    }
}