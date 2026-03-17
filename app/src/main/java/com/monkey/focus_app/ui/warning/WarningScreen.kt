package com.monkey.focus_app.ui.warning

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.monkey.focus_app.ui.NavigationItem

class WarningScreen() {
    @Composable
    fun Warning(modifier: Modifier = Modifier, navController: NavHostController, modeName: String) {
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

    @Composable
    fun Novice(modifier: Modifier = Modifier, navController: NavHostController){
        Text(modifier = modifier,
            text = "Novice")
    }
}