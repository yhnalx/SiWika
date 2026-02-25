package com.example.siwika

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    SettingsScreen()
                }
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("SiWikaProgress", Context.MODE_PRIVATE) }

    var showResetDialog by remember { mutableStateOf(false) }
    var showChangeNameDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    // --- Dialogs ---

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = { Text("Reset Progress", fontWeight = FontWeight.Black) },
            text = { Text("Are you sure? This will wipe your streaks and points forever.") },
            confirmButton = {
                TextButton(onClick = {
                    sharedPreferences.edit().clear().apply()
                    showResetDialog = false
                    Toast.makeText(context, "Progress Reset", Toast.LENGTH_SHORT).show()
                }) {
                    Text("YES, RESET", color = Color(0xFFFF4B4B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("CANCEL", color = Color.Gray)
                }
            }
        )
    }

    if (showChangeNameDialog) {
        AlertDialog(
            onDismissRequest = { showChangeNameDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = { Text("Change Name", fontWeight = FontWeight.Black) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    placeholder = { Text("Enter new name") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.padding(top = 8.dp)
                )
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1CB0F6)),
                    shape = RoundedCornerShape(12.dp),
                    onClick = {
                        if (newName.isNotBlank()) {
                            sharedPreferences.edit().putString("USER_NAME", newName).apply()
                            showChangeNameDialog = false
                            Toast.makeText(context, "Name Updated", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) { Text("SAVE", fontWeight = FontWeight.Bold) }
            }
        )
    }

    // --- Main Layout ---

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7)) // Neutral background to make cards pop
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        item {
            Text(
                text = "Settings",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF4B4B4B),
                modifier = Modifier.padding(top = 32.dp, bottom = 24.dp)
            )
        }

        // Account Section
        item {
            SettingsCategoryLabel("ACCOUNT")
            SettingsCard {
                SettingsItem(
                    icon = "👤",
                    title = "Change Name",
                    subtitle = "Update how we call you",
                    onClick = { showChangeNameDialog = true }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Info Section
        item {
            SettingsCategoryLabel("APP INFO")
            SettingsCard {
                SettingsItem(
                    icon = "ℹ️",
                    title = "About SiWika",
                    subtitle = "Version 1.0.4 - Alpha Build",
                    onClick = { /* Navigate to Info */ }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Danger Zone
        item {
            SettingsCategoryLabel("DANGER ZONE")
            SettingsCard {
                SettingsItem(
                    icon = "⚠️",
                    title = "Reset All Progress",
                    subtitle = "Delete all streak and history",
                    titleColor = Color(0xFFFF4B4B), // Red for warning
                    onClick = { showResetDialog = true },
                    showDivider = false
                )
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}

// --- Helper UI Components ---

@Composable
fun SettingsCategoryLabel(label: String) {
    Text(
        text = label,
        fontSize = 13.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFFAFAFAF),
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(2.dp, Color(0xFFE5E5E5)),
        content = content
    )
}

@Composable
fun SettingsItem(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    titleColor: Color = Color(0xFF4B4B4B),
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 24.sp, modifier = Modifier.padding(end = 16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = titleColor
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Text(
                text = "❯",
                color = Color(0xFFCECECE),
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 1.dp,
                color = Color(0xFFF1F1F1)
            )
        }
    }
}