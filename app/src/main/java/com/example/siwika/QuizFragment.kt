package com.example.siwika

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import kotlinx.coroutines.launch
import java.io.IOException

// --- Data Structure ---
data class QuizQuestion(
    val prompt: String,
    val signContent: String, // Can be an emoji or an asset path
    val options: List<String>,
    val correctAnswer: String
)

class QuizFragment : Fragment() {
    private var lessonTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            lessonTitle = it.getString(ARG_LESSON_TITLE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    QuizScreen(
                        lessonTitle = lessonTitle ?: "",
                        onExit = { parentFragmentManager.popBackStack() }
                    )
                }
            }
        }
    }

    companion object {
        private const val ARG_LESSON_TITLE = "lessonTitle"

        fun newInstance(lessonTitle: String) = QuizFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_LESSON_TITLE, lessonTitle)
            }
        }
    }
}

@Composable
fun QuizScreen(lessonTitle: String, onExit: () -> Unit) {
    val questions = QuizBank.questions[lessonTitle] ?: emptyList()

    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No quiz available for this lesson yet.", textAlign = TextAlign.Center)
        }
        return
    }

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var lives by remember { mutableStateOf(3) }
    var isQuizFinished by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val shakeOffset = remember { Animatable(0f) }

    fun triggerShake() {
        scope.launch {
            repeat(4) {
                shakeOffset.animateTo(15f, animationSpec = tween(50, easing = LinearEasing))
                shakeOffset.animateTo(-15f, animationSpec = tween(50, easing = LinearEasing))
            }
            shakeOffset.animateTo(0f, animationSpec = tween(50))
        }
    }

    if (isQuizFinished) {
        QuizSuccessScreen(
            livesRemaining = lives,
            totalQuestions = questions.size,
            onFinish = onExit
        )
    } else {
        Column(
            modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding()
        ) {
            Row(
                modifier = Modifier.padding(16.dp).offset(x = shakeOffset.value.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onExit) { Icon(Icons.Default.Close, contentDescription = "Exit", tint = Color.Gray) }
                LinearProgressIndicator(
                    progress = { (currentQuestionIndex).toFloat() / questions.size },
                    modifier = Modifier.weight(1f).height(12.dp).clip(CircleShape),
                    color = Color(0xFF58CC02), trackColor = Color(0xFFE5E5E5)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = if (shakeOffset.value != 0f) Color.Magenta else Color.Red, modifier = Modifier.size(if (shakeOffset.value != 0f) 28.dp else 24.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(lives.toString(), fontWeight = FontWeight.Black, color = Color.Red, fontSize = 20.sp)
                }
            }

            Text(
                text = questions[currentQuestionIndex].prompt,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF4B4B4B),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                QuizContent(content = questions[currentQuestionIndex].signContent)
            }

            Column(modifier = Modifier.padding(24.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val options = questions[currentQuestionIndex].options
                for (i in 0 until 2) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        for (j in 0 until 2) {
                            val optionIndex = i * 2 + j
                            if (optionIndex < options.size) {
                                val option = options[optionIndex]
                                Box(modifier = Modifier.weight(1f)) {
                                    QuizOptionButton(
                                        text = option,
                                        onClick = {
                                            if (option == questions[currentQuestionIndex].correctAnswer) {
                                                if (currentQuestionIndex < questions.size - 1) {
                                                    currentQuestionIndex++
                                                } else {
                                                    isQuizFinished = true
                                                }
                                            } else {
                                                if (lives > 1) {
                                                    lives--
                                                    triggerShake()
                                                } else {
                                                    lives = 0
                                                    triggerShake()
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizContent(content: String) {
    val context = LocalContext.current
    val bitmap = remember(content) {
        if (content.contains("/")) { // Simple check for a path
            try {
                context.assets.open(content).use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            } catch (e: IOException) {
                null
            }
        } else {
            null
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Quiz sign",
            modifier = Modifier.padding(16.dp)
        )
    } else {
        Text(text = content, fontSize = 140.sp)
    }
}

@Composable
fun QuizSuccessScreen(livesRemaining: Int, totalQuestions: Int, onFinish: () -> Unit) {
    Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp).navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Text("🎊", fontSize = 100.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Quiz Complete!", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFF4B4B4B))
            Text("You're a SiWika superstar!", fontSize = 18.sp, color = Color.Gray, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(48.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard("HEARTS", livesRemaining.toString(), Color(0xFFFF4B4B), Modifier.weight(1f))
                StatCard("SCORE", "$totalQuestions/$totalQuestions", Color(0xFFFBB040), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.weight(1f))

            DuolingoButton(
                text = "CONTINUE",
                subText = "",
                color = Color(0xFF58CC02),
                shadowColor = Color(0xFF46A302),
                onClick = onFinish
            )
        }
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = BorderStroke(2.dp, color.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontWeight = FontWeight.Black, fontSize = 12.sp, color = color)
            Text(value, fontWeight = FontWeight.Black, fontSize = 24.sp, color = color)
        }
    }
}

@Composable
fun QuizOptionButton(text: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedOffset by animateDpAsState(if (isPressed) 0.dp else (-6).dp, label = "")

    Box(
        modifier = Modifier.fillMaxWidth().height(80.dp).duolingoShadowQuiz(color = Color(0xFFE5E5E5), borderRadius = 16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxSize().offset(y = animatedOffset).clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(2.dp, Color(0xFFE5E5E5)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = text, color = Color(0xFF4B4B4B), fontWeight = FontWeight.Black, fontSize = 22.sp)
            }
        }
    }
}

fun Modifier.duolingoShadowQuiz(color: Color, borderRadius: Dp = 24.dp) = this.drawBehind {
    val shadowHeight = 6.dp.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(0f, shadowHeight),
        size = size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(borderRadius.toPx())
    )
}