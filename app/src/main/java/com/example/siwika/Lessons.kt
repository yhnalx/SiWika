package com.example.siwika

// --- Data Structures ---
data class Flashcard(val front: String, val back: String)
data class Lesson(val title: String, val flashcards: List<Flashcard>)

// --- Centralized Lesson Data ---
val allLessons = listOf(
    Lesson(
        title = "Lesson 1: The Alphabet",
        flashcards = listOf(
            Flashcard("Lesson 1/A.png", "A"),
            Flashcard("Lesson 1/B.png", "B"),
            Flashcard("Lesson 1/C.png", "C"),
            Flashcard("Lesson 1/D.png", "D"),
            Flashcard("Lesson 1/E.png", "E"),
            Flashcard("Lesson 1/F.png", "F"),
            Flashcard("Lesson 1/G.png", "G"),
            Flashcard("Lesson 1/H.png", "H"),
            Flashcard("Lesson 1/I.png", "I"),
            Flashcard("Lesson 1/J.png", "J"),
            Flashcard("Lesson 1/K.png", "K"),
            Flashcard("Lesson 1/L.png", "L"),
            Flashcard("Lesson 1/M.png", "M"),
            Flashcard("Lesson 1/N.png", "N"),
            Flashcard("Lesson 1/O.png", "O"),
            Flashcard("Lesson 1/P.png", "P"),
            Flashcard("Lesson 1/Q.png", "Q"),
            Flashcard("Lesson 1/R.png", "R"),
            Flashcard("Lesson 1/S.png", "S"),
            Flashcard("Lesson 1/T.png", "T"),
            Flashcard("Lesson 1/U.png", "U"),
            Flashcard("Lesson 1/V.png", "V"),
            Flashcard("Lesson 1/W.png", "W"),
            Flashcard("Lesson 1/X.png", "X"),
            Flashcard("Lesson 1/Y.png", "Y"),
            Flashcard("Lesson 1/Z.png", "Z")
        )
    ),
    Lesson(
        title = "Lesson 2: Basic Greetings",
        flashcards = listOf(
            Flashcard("Lesson 2/Good Afternoon.png", "Good afternoon"),
            Flashcard("Lesson 2/Good Morning.png", "Good morning"),
            Flashcard("Lesson 2/Good Evening.png", "Good evening"),
            Flashcard("Lesson 2/Good Night_.png", "Good Night"),
            Flashcard("Lesson 2/Hello.png", "Hello"),
            Flashcard("Lesson 2/Hi_Goodbye.png", "Hi/Goodbye"),
            Flashcard("Lesson 2/See You.png", "See you"),
            Flashcard("Lesson 2/Take Care.png", "Take care")
        )
    ),
    Lesson(
        title = "Lesson 3: Conversation Starters",
        flashcards = listOf(
            Flashcard("Lesson 3/How_are_you.png", "How are you"),
            Flashcard("Lesson 3/I_am_fine.png", "I am Fine"),
            Flashcard("Lesson 3/I_dont_understand.png", "I dont understand"),
            Flashcard("Lesson 3/I_understand.png", "I understand"),
            Flashcard("Lesson 3/Sorry.png", "Sorry"),
            Flashcard("Lesson 3/Thank You.png", "Thank you"),
            Flashcard("Lesson 3/Welcome.png", "Welcome"),
            Flashcard("Lesson 3/What.png", "What"),
            Flashcard("Lesson 3/When.png", "When"),
            Flashcard("Lesson 3/Where.png", "Where"),
            Flashcard("Lesson 3/Which.png", "Which"),
            Flashcard("Lesson 3/Who.png", "Who"),
            Flashcard("Lesson 3/Why.png", "Why")
        )
    )
)
