package com.example.siwika

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import java.util.Calendar

class HomeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    DashboardContent(
                        onNavigateToLearning = {
                            parentFragmentManager.beginTransaction()
                                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                                .replace(R.id.frameLayout, LearningFragment())
                                .addToBackStack(null)
                                .commit()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardContent(onNavigateToLearning: () -> Unit) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("SiWikaProgress", Context.MODE_PRIVATE) }
    val userName = sharedPref.getString("USER_NAME", "Mary") ?: "Mary"

    // --- Streak Logic ---
    val streakState = remember { mutableStateOf(0) }
    val weekMaskState = remember { mutableStateOf("0000000") }

    LaunchedEffect(key1 = Unit) {
        val today = Calendar.getInstance()
        val lastVisitDay = sharedPref.getLong("last_visit_day", 0)
        var currentStreak = sharedPref.getInt("streak_count", 0)
        var currentWeekMask = sharedPref.getString("week_mask", "0000000") ?: "0000000"

        val lastVisitCal = Calendar.getInstance().apply { timeInMillis = lastVisitDay }

        if (!isSameDay(today, lastVisitCal)) {
            val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            if (isSameDay(lastVisitCal, yesterday)) {
                // Continued streak
                currentStreak++
                currentWeekMask = currentWeekMask.drop(1) + '1'
            } else {
                // Broken streak
                currentStreak = 1
                currentWeekMask = "0000001"
            }
            with(sharedPref.edit()) {
                putLong("last_visit_day", today.timeInMillis)
                putInt("streak_count", currentStreak)
                putString("week_mask", currentWeekMask)
                apply()
            }
        }
        streakState.value = currentStreak
        weekMaskState.value = currentWeekMask
    }

    // Calculate overall progress across all lessons
    val completedLessons = allLessons.count {
        sharedPref.getBoolean("learning_finished_${it.title}", false)
    }
    val totalProgress = if (allLessons.isNotEmpty()) completedLessons.toFloat() / allLessons.size else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBB040))
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Text("Hi, $userName!", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text("Ready to level up today?", fontSize = 18.sp, color = Color.White.copy(alpha = 0.9f))
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF7F7F7),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                SectionHeader("Your Progress")
                StreakCard(streakCount = streakState.value, weekMask = weekMaskState.value)

                SectionHeader("Continue Learning")
                ModuleCard(
                    title = "Learning Modules",
                    progress = totalProgress,
                    iconText = "📚",
                    onClick = onNavigateToLearning
                )

                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

@Composable
fun ModuleCard(
    title: String,
    progress: Float,
    iconText: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = ""
    )
    val cardOffset by animateDpAsState(if (isPressed) 0.dp else (-6).dp, label = "")

    Box(
        modifier = Modifier
            .padding(bottom = 6.dp)
            .duolingoShadow(Color(0xFFE5E5E5), borderRadius = 24.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = cardOffset)
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(2.dp, Color(0xFFE5E5E5))
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF4B4B4B))
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = animatedProgress,
                        modifier = Modifier.fillMaxWidth().height(14.dp).clip(CircleShape),
                        color = Color(0xFFFBB040),
                        trackColor = Color(0xFFE5E5E5)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${(progress * 100).toInt()}% COMPLETE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBB040))
                }
                Spacer(modifier = Modifier.width(20.dp))
                Text(iconText, fontSize = 48.sp)
            }
        }
    }
}

@Composable
fun DuolingoButton(text: String, subText: String = "", color: Color, shadowColor: Color, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedOffset by animateDpAsState(if (isPressed) 0.dp else (-6).dp, label = "")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .padding(bottom = 6.dp)
            .duolingoShadow(color = shadowColor, borderRadius = 20.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = animatedOffset)
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
            colors = CardDefaults.cardColors(containerColor = color),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                if (subText.isNotEmpty()) {
                    Text(subText, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(title.uppercase(), fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp)
}

@Composable
fun StreakCard(streakCount: Int, weekMask: String) {
    Box(modifier = Modifier.duolingoShadow(Color(0xFFE5941A))) {
        Card(
            modifier = Modifier.fillMaxWidth().offset(y = (-4).dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCC80)),
            border = BorderStroke(2.dp, Color.White.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔥", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("$streakCount DAY STREAK", fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val days = listOf("S", "M", "T", "W", "T", "F", "S")
                    // weekMask has the most recent day at the end
                    val todayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1 // Sunday is 1, so maps to 0-6

                    days.forEachIndexed { index, day ->
                        // Determine the status of the day relative to today
                        val dayStatusIndex = (weekMask.length - 1) - (todayIndex - index).let { if (it < 0) it + 7 else it } % 7
                        val status = if (dayStatusIndex >= 0 && dayStatusIndex < weekMask.length) weekMask[dayStatusIndex] else '0'
                        val isPastDay = index < todayIndex

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier.size(38.dp).background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    status == '1' -> Icon(Icons.Default.Check, null, tint = Color(0xFFFBB040), modifier = Modifier.size(20.dp))
                                    isPastDay -> Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(day, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

fun Modifier.duolingoShadow(color: Color = Color(0xFFE5941A), borderRadius: Dp = 24.dp) = this.drawBehind {
    val shadowHeight = 6.dp.toPx()
    drawRoundRect(color = color, topLeft = Offset(0f, shadowHeight), size = size, cornerRadius = CornerRadius(borderRadius.toPx()))
}