package com.example.siwika

object QuizBank {
    val questions = mapOf(
        "Lesson 1: The Alphabet" to listOf(
            QuizQuestion("What letter is this?", "Lesson 1/A.png", listOf("A", "B", "C", "D"), "A"),
            QuizQuestion("Which sign is for the letter 'B'?", "Lesson 1/B.png", listOf("E", "B", "C", "D"), "B"),
            QuizQuestion("This is the sign for which letter?", "Lesson 1/C.png", listOf("A", "B", "C", "E"), "C"),
            QuizQuestion("Select the correct letter for this sign.", "Lesson 1/D.png", listOf("D", "A", "B", "C"), "D"),
            QuizQuestion("What letter does this sign represent?", "Lesson 1/E.png", listOf("A", "E", "B", "C"), "E")
        ),
        "Lesson 2: Basic Greetings" to listOf(
            QuizQuestion("What does this sign mean?", "Lesson 2/Hello.png", listOf("Hello", "Goodbye", "See you", "Thank you"), "Hello"),
            QuizQuestion("Which sign means 'Good morning'?", "Lesson 2/Good Morning.png", listOf("Good morning", "Good evening", "Good afternoon", "Hi"), "Good morning"),
            QuizQuestion("What is this greeting?", "Lesson 2/Good Afternoon.png", listOf("Good afternoon", "Good night", "Take care", "Welcome"), "Good afternoon"),
            QuizQuestion("How would you sign 'Take care'?", "Lesson 2/Take Care.png", listOf("See you", "Take care", "Hi/Goodbye", "Sorry"), "Take care")
        ),
        "Lesson 3: Conversation Starters" to listOf(
            QuizQuestion("How do you sign 'I understand'?", "Lesson 3/I understand.png", listOf("I understand", "I dont understand", "How are you", "I am fine"), "I understand"),
            QuizQuestion("What is the sign for 'Welcome'?", "Lesson 3/Welcome.png", listOf("Thank you", "Welcome", "Sorry", "Please"), "Welcome"),
            QuizQuestion("Which sign means 'Where'?", "Lesson 3/Where.png", listOf("What", "When", "Where", "Who"), "Where"),
            QuizQuestion("How would you ask 'Why'?", "Lesson 3/Why.png", listOf("Why", "Which", "What", "How"), "Why")
        )
    )
}
