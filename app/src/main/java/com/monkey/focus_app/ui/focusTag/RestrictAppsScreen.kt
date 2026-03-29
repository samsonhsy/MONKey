package com.monkey.focus_app.ui.focustag

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.monkey.focus_app.ui.theme.MONKeyTheme

private data class RestrictAppUi(
    val packageName: String,
    val appName: String,
    val category: String,
    val logoColor: Color,
)

private val demoApps = listOf(
    RestrictAppUi("com.instagram.android", "Instagram", "Social", Color(0xFFF2746B)),
    RestrictAppUi("com.zhiliaoapp.musically", "TikTok", "Entertainment", Color(0xFF38C8C2)),
    RestrictAppUi("com.reddit.frontpage", "Reddit", "Social", Color(0xFFFE9F4C)),
    RestrictAppUi("com.google.android.youtube", "YouTube", "Video", Color(0xFFE35D6A)),
    RestrictAppUi("com.discord", "Discord", "Communication", Color(0xFF9FA8DA)),
    RestrictAppUi("com.facebook.katana", "Facebook", "Social", Color(0xFF4FB2F8)),
    RestrictAppUi("com.twitter.android", "X", "Social", Color(0xFF90A4AE)),
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RestrictAppsScreen(
    navController: NavController,
    tagId: String,
) {
    var query by remember { mutableStateOf("") }
    var selectedPackages by remember { mutableStateOf(setOf<String>()) }

    val filteredApps = remember(query) {
        demoApps.filter {
            it.appName.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true) ||
                it.packageName.contains(query, ignoreCase = true)
        }
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Confirm selection")
                    Text(
                        text = "Confirm Selection",
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface)
                }
                Text(
                    text = "Restrict Apps",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search apps") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                shape = MaterialTheme.shapes.medium,
                singleLine = true,
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    val isSelected = selectedPackages.contains(app.packageName)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(14.dp),
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(app.logoColor, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = app.appName.take(1),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = app.appName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = app.category,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                )
                            }
                        }
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                selectedPackages = if (checked) {
                                    selectedPackages + app.packageName
                                } else {
                                    selectedPackages - app.packageName
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RestrictAppsPreviewDark() {
    MONKeyTheme(darkTheme = true) {
        val nav = androidx.navigation.compose.rememberNavController()
        RestrictAppsScreen(navController = nav, tagId = "1")
    }
}

@Preview(showBackground = true)
@Composable
private fun RestrictAppsPreviewLight() {
    MONKeyTheme(darkTheme = false) {
        val nav = androidx.navigation.compose.rememberNavController()
        RestrictAppsScreen(navController = nav, tagId = "1")
    }
}
