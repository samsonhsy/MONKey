package com.monkey.focus_app.ui.focustag

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.monkey.focus_app.ui.navigation.MainRoute
import com.monkey.focus_app.ui.theme.MONKeyTheme

private val mindfulShades = listOf(
    Color(0xFFFE9F4C), Color(0xFFE35D6A), Color(0xFFFFEB3B),
    Color(0xFF38C8C2), Color(0xFF5DD39E), Color(0xFFBA68C8),
    Color(0xFF9FA8DA),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FocusTagEditScreen(
    navController: NavController,
    tagId: String,
) {
    val isCreate = tagId == "new"
    var tagName by remember { mutableStateOf(if (isCreate) "" else "Untitled") }
    var description by remember { mutableStateOf(if (isCreate) "" else "No description") }
    var selectedColor by remember { mutableStateOf(mindfulShades.first()) }
    val restrictedCount = if (isCreate) 0 else 7

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Edit Tag",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            FilledTonalButton(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text("Save")
            }
        }

        OutlinedTextField(
            value = tagName,
            onValueChange = { tagName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Tag Name") },
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Description") },
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "TAG COLOR",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                mindfulShades.forEach { shade ->
                    val isActive = shade == selectedColor
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                else Color.Transparent
                            )
                            .border(
                                width = if (isActive) 2.dp else 0.dp,
                                color = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(shade)
                                .border(
                                    width = if (isActive) 2.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.background,
                                    shape = CircleShape,
                                )
                        )
                        IconButton(
                            onClick = { selectedColor = shade },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Pick color",
                                tint = if (isActive) MaterialTheme.colorScheme.onPrimary else Color.Transparent,
                            )
                        }
                    }
                }
            }
        }

        OutlinedCard(
            onClick = { navController.navigate(MainRoute.FocusTagRestrictApps.create(tagId)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = "Configure apps",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Column {
                        Text(
                            text = "Configure Apps",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "$restrictedCount apps restricted",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Go to app restriction",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        TextButton(onClick = { navController.popBackStack() }) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete tag",
                tint = Color(0xFFFF8A80),
            )
            Text(
                text = "Delete Tag",
                color = Color(0xFFFF8A80),
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FocusTagEditPreviewDark() {
    MONKeyTheme(darkTheme = true) {
        val nav = androidx.navigation.compose.rememberNavController()
        FocusTagEditScreen(navController = nav, tagId = "1")
    }
}
