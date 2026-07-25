package com.example.data

enum class MessageSender {
    USER,
    GEMINI,
    SYSTEM
}

data class Note(
    val id: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val originalText: String = "",
    val category: String = "Kişisel",
    val tags: List<String> = emptyList(),
    val relatedNoteIds: List<String> = emptyList(),
    val wordCount: Int = 0,
    val hourOfDay: Int = 0,
    val dateFormatted: String = "",
    val imageUri: String? = null
)

data class ChatMessage(
    val id: String = "",
    val sender: MessageSender = MessageSender.USER,
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val noteRef: Note? = null,
    val connectionNotice: String? = null,
    val imageUri: String? = null
)

data class IntentResult(
    val isNote: Boolean,
    val category: String = "Kişisel",
    val tags: List<String> = emptyList(),
    val relatedNoteIds: List<String> = emptyList(),
    val connectionNotice: String? = null,
    val responseText: String = ""
)

data class DailySummary(
    val dateString: String,
    val summary: String,
    val dominantCategory: String,
    val noteCount: Int
)

data class AnalyticsSummary(
    val totalNotesAllTime: Int,
    val totalNotesThisMonth: Int,
    val totalNotesThisWeek: Int,
    val totalNotesToday: Int,
    val categoryCounts: Map<String, Int>,
    val activityByDay: List<Pair<String, Int>>,
    val topTags: List<Pair<String, Int>>,
    val averageNoteLength: Int,
    val totalWordCount: Int,
    val hourlyActivity: List<Int>, // 24 items
    val currentStreak: Int,
    val longestStreak: Int,
    val linkedNotesCount: Int,
    val mostConnectedNotes: List<Note>,
    val dailySummaries: List<DailySummary>
)
