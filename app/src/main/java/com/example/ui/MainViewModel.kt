package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AnalyticsSummary
import com.example.data.ChatMessage
import com.example.data.DailySummary
import com.example.data.GeminiService
import com.example.data.MessageSender
import com.example.data.Note
import com.example.data.NotesRepository
import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.example.utils.SecurityCameraHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NotesRepository(application)
    private val geminiService = GeminiService()

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _currentScreen = MutableStateFlow(ActiveScreen.CHAT)
    val currentScreen: StateFlow<ActiveScreen> = _currentScreen.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val notes: StateFlow<List<Note>> = repository.notes
    val isFirestoreConnected: StateFlow<Boolean> = repository.isFirestoreConnected

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _welcomeSummary = MutableStateFlow<String?>(null)
    val welcomeSummary: StateFlow<String?> = _welcomeSummary.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _weeklyNarrativeReport = MutableStateFlow<String?>(null)
    val weeklyNarrativeReport: StateFlow<String?> = _weeklyNarrativeReport.asStateFlow()

    private val _isGeneratingReport = MutableStateFlow(false)
    val isGeneratingReport: StateFlow<Boolean> = _isGeneratingReport.asStateFlow()

    init {
        // Initial welcome chat message
        val initialMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.GEMINI,
            text = "Merhaba! Ben senin Akıllı Not asistanınım. Ne yazarsan yaz, otomatik olarak not mu yoksa soru mu olduğunu ayırt edip kaydeder veya yanıtlarım. 📝",
            timestamp = System.currentTimeMillis()
        )
        _chatMessages.value = listOf(initialMsg)
    }

    fun unlockApp() {
        _isUnlocked.value = true
        // Generate daily welcome summary on unlock
        viewModelScope.launch {
            val currentNotes = notes.value
            val summary = geminiService.generateWelcomeSummary(currentNotes)
            _welcomeSummary.value = summary
        }
    }

    fun lockApp() {
        _isUnlocked.value = false
    }

    fun navigateTo(screen: ActiveScreen) {
        _currentScreen.value = screen
        if (screen == ActiveScreen.DASHBOARD && _weeklyNarrativeReport.value == null) {
            generateWeeklyReport()
        }
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun sendMessage(text: String, imageUri: String? = null) {
        if (text.isBlank() && imageUri == null) return

        val userMsgId = UUID.randomUUID().toString()
        val userMsg = ChatMessage(
            id = userMsgId,
            sender = MessageSender.USER,
            text = text,
            timestamp = System.currentTimeMillis(),
            imageUri = imageUri
        )

        val updatedList = _chatMessages.value.toMutableList()
        updatedList.add(userMsg)
        _chatMessages.value = updatedList

        _isProcessing.value = true

        viewModelScope.launch {
            val currentNotes = notes.value
            val promptText = if (imageUri != null) "$text [Görsel Eklendi]" else text
            val result = geminiService.processUserMessage(promptText, currentNotes)

            var createdNote: Note? = null
            if (result.isNote) {
                createdNote = repository.addNote(
                    originalText = text.ifBlank { "Görsel Notu" },
                    category = result.category,
                    tags = result.tags,
                    relatedNoteIds = result.relatedNoteIds,
                    imageUri = imageUri
                )
            }

            val aiMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = MessageSender.GEMINI,
                text = result.responseText,
                timestamp = System.currentTimeMillis(),
                noteRef = createdNote,
                connectionNotice = result.connectionNotice
            )

            val finalList = _chatMessages.value.toMutableList()
            finalList.add(aiMsg)
            _chatMessages.value = finalList
            _isProcessing.value = false
        }
    }

    fun recordWrongPinAttempt(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        failedAttempts: Int,
        wrongPin: String
    ) {
        viewModelScope.launch {
            val capturedUris = SecurityCameraHelper.captureSecurityPhotos(context, lifecycleOwner)
            val timeFormatted = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            
            val noteText = "🚨 DETAYLI GÜVENLİK İHLALİ VE KAMERA RAPORU\n\n" +
                    "🔑 Denenen Hatalı PIN: [$wrongPin]\n" +
                    "📊 Toplam Başarısız Deneme: $failedAttempts\n" +
                    "🕒 Tarih & Saat: $timeFormatted\n" +
                    "🛡️ Kalkan Durumu: Aktif (256-Bit Kilitli)\n" +
                    "📷 Çekilen Fotoğraf Sayısı: ${capturedUris.size} Adet (Ön ve Arka Kamera)\n" +
                    "📍 İşlem: Otomatik Güvenlik Kaydı İle Not Defterine Eklendi.\n" +
                    "⚠️ Uyarı: İzinsiz erişim denemeleri sistem tarafından anlık loglanmaktadır."

            val primaryPhotoUri = capturedUris.firstOrNull()

            repository.addNote(
                originalText = noteText,
                category = "Güvenlik",
                tags = listOf("Güvenlik", "HatalıPIN", "GirişDenemesi", "PIN:$wrongPin", "KameraKayıt"),
                relatedNoteIds = emptyList(),
                imageUri = primaryPhotoUri
            )

            if (capturedUris.size > 1) {
                repository.addNote(
                    originalText = "📸 Arka Kamera Güvenlik Yakalaması\n• Denenen PIN: [$wrongPin]\n• Tarih: $timeFormatted",
                    category = "Güvenlik",
                    tags = listOf("Güvenlik", "ArkaKamera", "PIN:$wrongPin"),
                    relatedNoteIds = emptyList(),
                    imageUri = capturedUris[1]
                )
            }
        }
    }

    fun addNoteDirectly(
        originalText: String,
        category: String = "Kişisel",
        tags: List<String> = emptyList(),
        imageUri: String? = null
    ) {
        if (originalText.isBlank() && imageUri == null) return
        repository.addNote(
            originalText = originalText.ifBlank { "Fotoğraflı Not" },
            category = category,
            tags = if (tags.isEmpty() && imageUri != null) listOf("Fotoğraf") else tags,
            relatedNoteIds = emptyList(),
            imageUri = imageUri
        )
    }

    fun deleteNote(noteId: String) {
        repository.deleteNote(noteId)
    }

    fun generateWeeklyReport() {
        _isGeneratingReport.value = true
        viewModelScope.launch {
            val currentNotes = notes.value
            val report = geminiService.generateWeeklyNarrativeReport(currentNotes)
            _weeklyNarrativeReport.value = report
            _isGeneratingReport.value = false
        }
    }

    fun computeAnalyticsSummary(): AnalyticsSummary {
        val allNotes = notes.value
        val now = System.currentTimeMillis()

        val calNow = Calendar.getInstance().apply { timeInMillis = now }
        val currentMonth = calNow.get(Calendar.MONTH)
        val currentYear = calNow.get(Calendar.YEAR)

        val totalAllTime = allNotes.size

        val totalThisMonth = allNotes.count { note ->
            val c = Calendar.getInstance().apply { timeInMillis = note.timestamp }
            c.get(Calendar.MONTH) == currentMonth && c.get(Calendar.YEAR) == currentYear
        }

        val weekAgo = now - (7 * 24 * 60 * 60 * 1000L)
        val totalThisWeek = allNotes.count { it.timestamp >= weekAgo }

        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val totalToday = allNotes.count { it.timestamp >= startOfDay }

        // Category Counts
        val catCounts = allNotes.groupBy { it.category }.mapValues { it.value.size }

        // Last 7 days activity
        val sdfDay = SimpleDateFormat("EEE", Locale("tr", "TR"))
        val activityList = mutableListOf<Pair<String, Int>>()
        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                timeInMillis = now - (i * 24 * 60 * 60 * 1000L)
            }
            val dayStart = dayCal.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val dayEnd = dayStart + (24 * 60 * 60 * 1000L) - 1

            val count = allNotes.count { it.timestamp in dayStart..dayEnd }
            val dayName = sdfDay.format(Date(dayStart))
            activityList.add(dayName to count)
        }

        // Top Tags
        val tagMap = mutableMapOf<String, Int>()
        allNotes.forEach { note ->
            note.tags.forEach { tag ->
                tagMap[tag] = (tagMap[tag] ?: 0) + 1
            }
        }
        val topTags = tagMap.entries.sortedByDescending { it.value }.map { it.key to it.value }.take(10)

        // Word counts
        val totalWords = allNotes.sumOf { it.wordCount }
        val avgLength = if (allNotes.isNotEmpty()) totalWords / allNotes.size else 0

        // Hourly Activity (0..23)
        val hourlyArr = IntArray(24)
        allNotes.forEach { note ->
            val hour = note.hourOfDay.coerceIn(0, 23)
            hourlyArr[hour]++
        }

        // Streak computation
        var currentStreak = 0
        var longestStreak = 0
        if (allNotes.isNotEmpty()) {
            val distinctDays = allNotes.map { note ->
                val c = Calendar.getInstance().apply { timeInMillis = note.timestamp }
                "${c.get(Calendar.YEAR)}-${c.get(Calendar.MONTH)}-${c.get(Calendar.DAY_OF_MONTH)}"
            }.distinct()

            var tempStreak = 1
            currentStreak = 1
            longestStreak = 1

            for (i in 0 until distinctDays.size - 1) {
                // Approximate consecutive check
                tempStreak++
                if (tempStreak > longestStreak) longestStreak = tempStreak
            }
            currentStreak = tempStreak.coerceAtMost(allNotes.size)
        }

        // Linked notes
        val linkedNotes = allNotes.filter { it.relatedNoteIds.isNotEmpty() }
        val mostConnected = allNotes.sortedByDescending { it.relatedNoteIds.size }.filter { it.relatedNoteIds.isNotEmpty() }

        // Daily Summaries
        val dailyGroups = allNotes.groupBy { it.dateFormatted }
        val dailySummaries = dailyGroups.map { (dateStr, notesInDay) ->
            val dominantCat = notesInDay.groupBy { it.category }.maxByOrNull { it.value.size }?.key ?: "Genel"
            val textSample = notesInDay.joinToString(" ") { it.originalText }.take(80)
            DailySummary(
                dateString = dateStr,
                summary = "Bu gün $dominantCat ağırlıklı ${notesInDay.size} not kaydedildi: \"$textSample...\"",
                dominantCategory = dominantCat,
                noteCount = notesInDay.size
            )
        }.take(7)

        return AnalyticsSummary(
            totalNotesAllTime = totalAllTime,
            totalNotesThisMonth = totalThisMonth,
            totalNotesThisWeek = totalThisWeek,
            totalNotesToday = totalToday,
            categoryCounts = catCounts,
            activityByDay = activityList,
            topTags = topTags,
            averageNoteLength = avgLength,
            totalWordCount = totalWords,
            hourlyActivity = hourlyArr.toList(),
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            linkedNotesCount = linkedNotes.size,
            mostConnectedNotes = mostConnected,
            dailySummaries = dailySummaries
        )
    }
}
