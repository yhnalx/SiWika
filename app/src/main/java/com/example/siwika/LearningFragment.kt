package com.example.siwika

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import java.io.IOException

class LearningFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    LearningRouter(
                        onExit = { parentFragmentManager.popBackStack() },
                        onNavigateToQuiz = { lessonTitle ->
                            parentFragmentManager.beginTransaction()
                                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                                .replace(R.id.frameLayout, QuizFragment.newInstance(lessonTitle))
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
fun LearningRouter(onExit: () -> Unit, onNavigateToQuiz: (String) -> Unit) {
    var currentScreen by remember { mutableStateOf("selection") } // "selection", "learning"
    var selectedLesson by remember { mutableStateOf<Lesson?>(null) }
    var showModeSelector by remember { mutableStateOf(false) }

    when (currentScreen) {
        "selection" -> {
            LessonSelectionScreen(
                lessons = allLessons,
                onLessonSelected = {
                    selectedLesson = it
                    showModeSelector = true
                },
                onExit = onExit
            )
        }
        "learning" -> {
            LearningScreen(
                lesson = selectedLesson!!,
                onExit = { currentScreen = "selection" },
                onNavigateToQuiz = { onNavigateToQuiz(selectedLesson!!.title) }
            )
        }
    }

    if (showModeSelector && selectedLesson != null) {
        LessonModeSelector(
            lesson = selectedLesson!!,
            onDismiss = { showModeSelector = false },
            onStartLearning = {
                showModeSelector = false
                currentScreen = "learning"
            },
            onStartQuiz = {
                showModeSelector = false
                onNavigateToQuiz(selectedLesson!!.title)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonModeSelector(
    lesson: Lesson,
    onDismiss: () -> Unit,
    onStartLearning: () -> Unit,
    onStartQuiz: () -> Unit
) {
    val context = LocalContext.current
    val isQuizUnlocked = remember {
        context.getSharedPreferences("SiWikaProgress", Context.MODE_PRIVATE)
            .getBoolean("learning_finished_${lesson.title}", false)
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(lesson.title, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(32.dp))

            DuolingoButton(text = "LEARNING MODE", subText = "Review and flip cards", color = Color(0xFF58CC02), shadowColor = Color(0xFF46A302), onClick = onStartLearning)

            Spacer(modifier = Modifier.height(16.dp))

            val quizButtonColor = if (isQuizUnlocked) Color(0xFF1CB0F6) else Color.Gray
            val quizShadowColor = if (isQuizUnlocked) Color(0xFF1899D6) else Color.DarkGray

            DuolingoButton(
                text = "QUIZ MODE",
                subText = "Test your knowledge!",
                color = quizButtonColor,
                shadowColor = quizShadowColor,
                onClick = {
                    if(isQuizUnlocked) onStartQuiz() else Toast.makeText(context, "Finish learning mode first!", Toast.LENGTH_SHORT).show()
                }
            )
            if (!isQuizUnlocked) {
                Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.White, modifier = Modifier.align(Alignment.End).offset(y = (-58).dp, x = (-32).dp))
            }
        }
    }
}

@Composable
fun LessonSelectionScreen(lessons: List<Lesson>, onLessonSelected: (Lesson) -> Unit, onExit: () -> Unit) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("SiWikaProgress", Context.MODE_PRIVATE) }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding().padding(top = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
            IconButton(onClick = onExit) { Icon(Icons.Default.Close, contentDescription = "Exit", tint = Color.Gray) }
            Text("Learning Modules", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF4B4B4B))
        }
        LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(lessons) { lesson ->
                val isCompleted = sharedPref.getBoolean("learning_finished_${lesson.title}", false)
                LessonCard(lesson = lesson, isCompleted = isCompleted, onClick = { onLessonSelected(lesson) })
            }
        }
    }
}

@Composable
fun LessonCard(lesson: Lesson, isCompleted: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E5E5))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(lesson.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B4B4B))
                Text("${lesson.flashcards.size} Cards", color = Color.Gray)
            }
            if(isCompleted) {
                Icon(Icons.Default.Check, contentDescription = "Completed", tint = Color(0xFF58CC02), modifier = Modifier.size(28.dp))
            } else {
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFFFBB040))
            }
        }
    }
}

@Composable
fun LearningScreen(lesson: Lesson, onExit: () -> Unit, onNavigateToQuiz: () -> Unit) {
    var currentIndex by remember { mutableStateOf(0) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding()
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onExit) { Icon(Icons.Default.Close, contentDescription = "Exit Lesson", tint = Color.Gray) }
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / lesson.flashcards.size },
                    modifier = Modifier.weight(1f).height(12.dp).clip(CircleShape),
                    color = Color(0xFF58CC02), trackColor = Color(0xFFE5E5E5)
                )
            }

            Box(modifier = Modifier.weight(1f).padding(24.dp), contentAlignment = Alignment.Center) {
                FlippableFlashcard(
                    frontAssetPath = lesson.flashcards[currentIndex].front,
                    backText = lesson.flashcards[currentIndex].back,
                    key = currentIndex
                )
            }

            Row(modifier = Modifier.padding(24.dp).navigationBarsPadding(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { if (currentIndex > 0) currentIndex-- }, enabled = currentIndex > 0, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F1F1), contentColor = Color(0xFF4B4B4B))) { Text("BACK", fontWeight = FontWeight.Bold) }
                Button(
                    onClick = {
                        if (currentIndex < lesson.flashcards.size - 1) {
                            currentIndex++
                        } else {
                            showSuccessDialog = true
                            context.getSharedPreferences("SiWikaProgress", Context.MODE_PRIVATE).edit {
                                putBoolean("learning_finished_${lesson.title}", true)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBB040))
                ) { Text(if (currentIndex == lesson.flashcards.size - 1) "FINISH" else "NEXT", fontWeight = FontWeight.Bold) }
            }
        }

        if (showSuccessDialog) {
            QuizPromptSuccessOverlay(onStartQuiz = onNavigateToQuiz, onNotNow = onExit)
        }
    }
}

@Composable
fun QuizPromptSuccessOverlay(onStartQuiz: () -> Unit, onNotNow: () -> Unit) {
    Surface(color = Color.Black.copy(alpha = 0.7f), modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🎉", fontSize = 80.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Card(shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Are you ready to take on the quiz?!", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF4B4B4B), textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(32.dp))
                    DuolingoButton(text = "BRING IT ON!", subText = "", color = Color(0xFF58CC02), shadowColor = Color(0xFF46A302), onClick = onStartQuiz)
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = onNotNow) { Text("NOT NOW", color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 1.sp) }
                }
            }
        }
    }
}

@Composable
fun FlippableFlashcard(frontAssetPath: String, backText: String, key: Any) {
    val context = LocalContext.current
    var rotated by remember(key) { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow), label = ""
    )

    val bitmap = remember(frontAssetPath) {
        try {
            context.assets.open(frontAssetPath).use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: IOException) {
            null
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .graphicsLayer { 
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { rotated = !rotated },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(4.dp, Color(0xFFFBB040))
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (rotation <= 90f) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = backText, // Use the back text for accessibility
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    // Fallback to text if image loading fails or if it's not a path
                    Text(text = frontAssetPath, fontSize = if (frontAssetPath.length > 1) 40.sp else 100.sp, textAlign = TextAlign.Center)
                }
            } else {
                Text(text = backText, fontSize = 32.sp, fontWeight = FontWeight.Black, modifier = Modifier.graphicsLayer { rotationY = 180f }, textAlign = TextAlign.Center)
            }
        }
    }
}