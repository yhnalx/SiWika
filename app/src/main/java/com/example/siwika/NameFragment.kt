package com.example.siwika

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment

class NameFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    NameEntryScreen(onNameSaved = {
                        parentFragmentManager.beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .replace(R.id.frameLayout, HomeFragment())
                            .commit()
                    })
                }
            }
        }
    }

    @Composable
    fun NameEntryScreen(onNameSaved: () -> Unit) {
        var name by remember { mutableStateOf("") }
        val isNameValid = name.trim().length >= 2

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp)
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Mascot/Avatar Placeholder
            Text(
                text = "👋",
                fontSize = 80.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Hello! I'm SiWika.",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF4B4B4B)
            )

            Text(
                text = "What should I call you?",
                fontSize = 18.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Custom Styled TextField
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter your name") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1CB0F6),
                    unfocusedBorderColor = Color(0xFFE5E5E5),
                    cursorColor = Color(0xFF1CB0F6)
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            // Our Signature Duolingo Style Button
            DuolingoButton(
                text = "CONTINUE",
                color = if (isNameValid) Color(0xFF58CC02) else Color(0xFFE5E5E5),
                shadowColor = if (isNameValid) Color(0xFF46A302) else Color(0xFFAFAFAF),
                onClick = {
                    if (isNameValid) {
                        saveNameToPrefs(name.trim())
                        onNameSaved()
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    private fun saveNameToPrefs(name: String) {
        val sharedPref = requireActivity().getSharedPreferences("SiWikaProgress", Context.MODE_PRIVATE)
        sharedPref.edit().putString("USER_NAME", name).apply()
    }
}