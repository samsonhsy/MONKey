package com.monkey.focus_app.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monkey.focus_app.ui.theme.MONKeyTheme
import java.util.Locale.getDefault

class WarningActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MONKeyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Warning(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Warning(modifier: Modifier = Modifier) {
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
                getDefault()
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
                    text = "Yes, I am leaving",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            onClick = { println("hello") },
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

@Preview(showBackground = true)
@Composable
fun WarningPreview() {
    MONKeyTheme {
        Warning()
    }
}