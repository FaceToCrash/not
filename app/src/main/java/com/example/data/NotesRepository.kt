package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class NotesRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("akilli_not_prefs", Context.MODE_PRIVATE)

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _isFirestoreConnected = MutableStateFlow(false)
    val isFirestoreConnected: StateFlow<Boolean> = _isFirestoreConnected.asStateFlow()

    init {
        // Load initial local notes
        loadNotesFromLocal()
        // Start Firestore listener if available
        initFirestoreListener()
    }

    private fun initFirestoreListener() {
        val fs = firestore ?: return
        try {
            fs.collection("notes")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        _isFirestoreConnected.value = false
                        return@addSnapshotListener
                    }
                    _isFirestoreConnected.value = true
                    val remoteNotes = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.id
                            val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            val originalText = doc.getString("originalText") ?: ""
                            val category = doc.getString("category") ?: "Kişisel"
                            @Suppress("UNCHECKED_CAST")
                            val tags = (doc.get("tags") as? List<String>) ?: emptyList()
                            @Suppress("UNCHECKED_CAST")
                            val relatedNoteIds = (doc.get("relatedNoteIds") as? List<String>) ?: emptyList()
                            val wordCount = (doc.getLong("wordCount") ?: originalText.split("\\s+".toRegex()).size.toLong()).toInt()
                            val hourOfDay = (doc.getLong("hourOfDay") ?: 12).toInt()
                            val dateFormatted = doc.getString("dateFormatted") ?: formatDate(timestamp)
                            val imageUri = doc.getString("imageUri")

                            Note(
                                id = id,
                                timestamp = timestamp,
                                originalText = originalText,
                                category = category,
                                tags = tags,
                                relatedNoteIds = relatedNoteIds,
                                wordCount = wordCount,
                                hourOfDay = hourOfDay,
                                dateFormatted = dateFormatted,
                                imageUri = imageUri
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }

                    if (remoteNotes.isNotEmpty()) {
                        _notes.value = remoteNotes
                        saveNotesToLocal(remoteNotes)
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
            _isFirestoreConnected.value = false
        }
    }

    fun addNote(
        originalText: String,
        category: String,
        tags: List<String>,
        relatedNoteIds: List<String>,
        imageUri: String? = null
    ): Note {
        val timestamp = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val wordCount = originalText.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
        val dateFormatted = formatDate(timestamp)
        val id = UUID.randomUUID().toString()

        val newNote = Note(
            id = id,
            timestamp = timestamp,
            originalText = originalText,
            category = category,
            tags = tags,
            relatedNoteIds = relatedNoteIds,
            wordCount = wordCount,
            hourOfDay = hour,
            dateFormatted = dateFormatted,
            imageUri = imageUri
        )

        // Update local memory and cache
        val currentList = _notes.value.toMutableList()
        currentList.add(0, newNote)
        _notes.value = currentList
        saveNotesToLocal(currentList)

        // Push to Firestore if available
        firestore?.let { fs ->
            try {
                val docData = hashMapOf(
                    "timestamp" to timestamp,
                    "originalText" to originalText,
                    "category" to category,
                    "tags" to tags,
                    "relatedNoteIds" to relatedNoteIds,
                    "wordCount" to wordCount,
                    "hourOfDay" to hour,
                    "dateFormatted" to dateFormatted,
                    "imageUri" to imageUri
                )
                fs.collection("notes").document(id).set(docData)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return newNote
    }

    fun deleteNote(noteId: String) {
        val currentList = _notes.value.toMutableList()
        currentList.removeAll { it.id == noteId }
        _notes.value = currentList
        saveNotesToLocal(currentList)

        firestore?.let { fs ->
            try {
                fs.collection("notes").document(noteId).delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadNotesFromLocal() {
        val jsonString = prefs.getString("local_notes_json", null) ?: return
        try {
            val jsonArr = JSONArray(jsonString)
            val list = mutableListOf<Note>()
            for (i in 0 until jsonArr.length()) {
                val obj = jsonArr.getJSONObject(i)
                val id = obj.optString("id")
                val timestamp = obj.optLong("timestamp")
                val originalText = obj.optString("originalText")
                val category = obj.optString("category")
                
                val tagsList = mutableListOf<String>()
                val tArr = obj.optJSONArray("tags")
                if (tArr != null) {
                    for (j in 0 until tArr.length()) tagsList.add(tArr.getString(j))
                }

                val relList = mutableListOf<String>()
                val rArr = obj.optJSONArray("relatedNoteIds")
                if (rArr != null) {
                    for (j in 0 until rArr.length()) relList.add(rArr.getString(j))
                }

                val wordCount = obj.optInt("wordCount", 0)
                val hourOfDay = obj.optInt("hourOfDay", 12)
                val dateFormatted = obj.optString("dateFormatted", formatDate(timestamp))
                val imageUri = if (obj.has("imageUri") && !obj.isNull("imageUri")) obj.getString("imageUri") else null

                list.add(
                    Note(
                        id = id,
                        timestamp = timestamp,
                        originalText = originalText,
                        category = category,
                        tags = tagsList,
                        relatedNoteIds = relList,
                        wordCount = wordCount,
                        hourOfDay = hourOfDay,
                        dateFormatted = dateFormatted,
                        imageUri = imageUri
                    )
                )
            }
            _notes.value = list.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveNotesToLocal(list: List<Note>) {
        try {
            val jsonArr = JSONArray()
            for (note in list) {
                val obj = JSONObject().apply {
                    put("id", note.id)
                    put("timestamp", note.timestamp)
                    put("originalText", note.originalText)
                    put("category", note.category)
                    put("tags", JSONArray(note.tags))
                    put("relatedNoteIds", JSONArray(note.relatedNoteIds))
                    put("wordCount", note.wordCount)
                    put("hourOfDay", note.hourOfDay)
                    put("dateFormatted", note.dateFormatted)
                    put("imageUri", note.imageUri ?: JSONObject.NULL)
                }
                jsonArr.put(obj)
            }
            prefs.edit().putString("local_notes_json", jsonArr.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("tr", "TR"))
        return sdf.format(Date(timestamp))
    }
}
