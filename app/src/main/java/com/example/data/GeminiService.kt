package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val apiKey: String
        get() = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

    suspend fun processUserMessage(
        userInput: String,
        existingNotes: List<Note>
    ): IntentResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Smart local fallback if API key is not configured yet
            return@withContext fallbackProcessMessage(userInput, existingNotes)
        }

        try {
            val notesContext = buildNotesContextString(existingNotes)
            val prompt = """
                Sistem Rolü: Sen akıllı bir not defteri asistanısın. Kullanıcının Türkçe girdiği girdiyi analiz et.
                
                Mevcut Kullanıcı Notları Geçmişi:
                $notesContext
                
                Kullanıcı Girdisi: "$userInput"
                
                Görev:
                1. Bu girdi yeni bir NOT (bilgi kaydetme, günlük, olay, duyuru, yapılan iş) mu yoksa geçmiş notlar hakkında bir SORU/İSTEK mi?
                2. NOT ise:
                   - Uygun bir kategori belirle (örneğin: İş, Okul, Sağlık, Kişisel, Oyun Projeleri, Finans, Alışveriş vb.)
                   - 2-4 kısa etiket üret.
                   - Geçmiş notlar arasında bu notla bağlantılı/ilişkili olan bir not var mı kontrol et. Bağlantılı ise o notun ID'sini 'relatedNoteIds' listesine ekle ve 'connectionNotice' kısmında Türkçe açıklama yap (Örn: "Bu, 12 Temmuz'daki doktor notunla bağlantılı olabilir").
                   - 'responseText' değerini "Not edildi 📝" yap.
                3. SORU/İSTEK ise:
                   - Mevcut kullanıcı notlarını inceleyip sohbet havasında doğal ve açıklayıcı Türkçe bir yanıt hazırla.
                   - 'isNote' değerini false yap.
                
                Yalnızca geçerli bir JSON nesnesi döndür:
                {
                  "isNote": true/false,
                  "category": "Kategori Adı",
                  "tags": ["etiket1", "etiket2"],
                  "relatedNoteIds": ["id1"],
                  "connectionNotice": "Bağlantı bildirimi metni veya null",
                  "responseText": "Not edildi 📝 veya Soru Yanıtı"
                }
            """.trimIndent()

            val jsonPayload = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.3)
                })
            }

            val requestUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val body = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(requestUrl)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful || responseString.isBlank()) {
                return@withContext fallbackProcessMessage(userInput, existingNotes)
            }

            val jsonResponse = JSONObject(responseString)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val contentObj = firstCandidate?.optJSONObject("content")
            val partsArr = contentObj?.optJSONArray("parts")
            val rawText = partsArr?.optJSONObject(0)?.optString("text") ?: ""

            val parsedJson = JSONObject(rawText)
            val isNote = parsedJson.optBoolean("isNote", true)
            val category = parsedJson.optString("category", "Kişisel")
            
            val tagsList = mutableListOf<String>()
            val tagsArr = parsedJson.optJSONArray("tags")
            if (tagsArr != null) {
                for (i in 0 until tagsArr.length()) {
                    tagsList.add(tagsArr.getString(i))
                }
            }

            val relatedIdsList = mutableListOf<String>()
            val relArr = parsedJson.optJSONArray("relatedNoteIds")
            if (relArr != null) {
                for (i in 0 until relArr.length()) {
                    relatedIdsList.add(relArr.getString(i))
                }
            }

            val notice = if (parsedJson.isNull("connectionNotice")) null else parsedJson.optString("connectionNotice")
            val respText = parsedJson.optString("responseText", if (isNote) "Not edildi 📝" else "Anlaşıldı.")

            IntentResult(
                isNote = isNote,
                category = category,
                tags = tagsList,
                relatedNoteIds = relatedIdsList,
                connectionNotice = if (notice.isNullOrBlank()) null else notice,
                responseText = respText
            )
        } catch (e: Exception) {
            e.printStackTrace()
            fallbackProcessMessage(userInput, existingNotes)
        }
    }

    suspend fun generateWelcomeSummary(existingNotes: List<Note>): String = withContext(Dispatchers.IO) {
        if (existingNotes.isEmpty()) {
            return@withContext "Hoş geldin! İlk notunu yazarak başlayabilirsin 🚀"
        }
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            val topCategory = existingNotes.groupBy { it.category }.maxByOrNull { it.value.size }?.key ?: "Genel"
            return@withContext "Hoş geldin! Toplam ${existingNotes.size} notun var. En çok $topCategory kategorisinde not yazmışsın."
        }

        try {
            val notesContext = buildNotesContextString(existingNotes.take(15))
            val prompt = """
                Sen kullanıcıya özel akıllı not defteri karşılama asistanısın.
                Aşağıdaki son notlara bakarak kullanıcıya bugün için 1-2 cümlelik sıcak, motive edici ve özetleyici bir "Hoş geldin" mesajı yaz (Türkçe).
                Örnek: "Hoş geldin! Bu hafta en çok Sağlık ve Okul konularında notlar almışsın."
                
                Notlar:
                $notesContext
            """.trimIndent()

            val jsonPayload = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
            }

            val requestUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val body = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(requestUrl)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""
            if (response.isSuccessful && responseString.isNotBlank()) {
                val jsonResponse = JSONObject(responseString)
                val text = jsonResponse.optJSONArray("candidates")
                    ?.optJSONObject(0)?.optJSONObject("content")
                    ?.optJSONArray("parts")?.optJSONObject(0)?.optString("text")
                if (!text.isNullOrBlank()) {
                    return@withContext text.trim()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val topCategory = existingNotes.groupBy { it.category }.maxByOrNull { it.value.size }?.key ?: "Genel"
        "Hoş geldin! Toplam ${existingNotes.size} notun var. En çok $topCategory kategorisinde yazmışsın."
    }

    suspend fun generateWeeklyNarrativeReport(existingNotes: List<Note>): String = withContext(Dispatchers.IO) {
        if (existingNotes.isEmpty()) {
            return@withContext "Henüz yeterli not birikmedi. Not ekledikçe burada dönem analizi ve hikayen oluşacak."
        }
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Bu dönem toplam ${existingNotes.size} not kaydedildi. Ağırlıklı olarak ${existingNotes.firstOrNull()?.category ?: "Genel"} konularına odaklandın."
        }

        try {
            val notesContext = buildNotesContextString(existingNotes.take(30))
            val prompt = """
                Kullanıcının notlarına dayanarak bir "Dönem Anlatı Raporu" (Hikaye Formatında Özet) yaz.
                Dili içten, profesyonel ve ilgi çekici olsun. Paragraflar halinde 2-3 kısa paragraf yaz.
                Kullanıcının neler yaptığını, hangi alanlara odaklandığını ve dikkate değer noktaları anlat.
                
                Notlar:
                $notesContext
            """.trimIndent()

            val jsonPayload = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
            }

            val requestUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val body = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(requestUrl)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""
            if (response.isSuccessful && responseString.isNotBlank()) {
                val jsonResponse = JSONObject(responseString)
                val text = jsonResponse.optJSONArray("candidates")
                    ?.optJSONObject(0)?.optJSONObject("content")
                    ?.optJSONArray("parts")?.optJSONObject(0)?.optString("text")
                if (!text.isNullOrBlank()) {
                    return@withContext text.trim()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        "Bu dönem toplam ${existingNotes.size} not kaydedildi. Ağırlıklı olarak ${existingNotes.groupBy { it.category }.maxByOrNull { it.value.size }?.key ?: "Genel"} konularına odaklandın."
    }

    private fun buildNotesContextString(notes: List<Note>): String {
        if (notes.isEmpty()) return "Henüz kayıtlı not yok."
        val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("tr", "TR"))
        return notes.joinToString("\n") { n ->
            "ID: ${n.id} | Tarih: ${sdf.format(Date(n.timestamp))} | Kategori: ${n.category} | Etiketler: ${n.tags.joinToString(", ")} | Metin: \"${n.originalText}\""
        }
    }

    private fun fallbackProcessMessage(userInput: String, existingNotes: List<Note>): IntentResult {
        val lower = userInput.lowercase(Locale("tr", "TR"))
        val isQuestion = lower.contains("?") || lower.startsWith("ne zaman") || lower.startsWith("kaç") ||
                lower.startsWith("nerede") || lower.startsWith("hangi") || lower.contains("hatırlat") ||
                lower.contains("listele") || lower.contains("söyle") || lower.contains("nedir")

        if (isQuestion) {
            val matchingNotes = existingNotes.filter { note ->
                userInput.split(" ").any { word -> word.length > 3 && note.originalText.lowercase().contains(word) }
            }
            val answer = if (matchingNotes.isNotEmpty()) {
                val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("tr", "TR"))
                "Notlarına baktım: ${matchingNotes.joinToString("; ") { "\"${it.originalText}\" (${sdf.format(Date(it.timestamp))})" }}"
            } else if (existingNotes.isNotEmpty()) {
                "Notların arasında tam olarak bununla eşleşen bir kayıt bulamadım. Mevcut notların toplamı: ${existingNotes.size} adet."
            } else {
                "Henüz kayıtlı hiç notun yok."
            }

            return IntentResult(
                isNote = false,
                responseText = answer
            )
        } else {
            // Determine category
            val cat = when {
                lower.contains("doktor") || lower.contains("ilaç") || lower.contains("tahlil") || lower.contains("ağrı") || lower.contains("hastane") || lower.contains("sağlık") -> "Sağlık"
                lower.contains("ders") || lower.contains("okul") || lower.contains("sınav") || lower.contains("ödev") || lower.contains("kurs") -> "Okul"
                lower.contains("toplantı") || lower.contains("proje") || lower.contains("iş") || lower.contains("müşteri") || lower.contains("kod") -> "İş"
                lower.contains("oyun") || lower.contains("unity") || lower.contains("unreal") || lower.contains("level") -> "Oyun Projeleri"
                lower.contains("para") || lower.contains("fatura") || lower.contains("banka") || lower.contains("maaş") || lower.contains("tl") -> "Finans"
                else -> "Kişisel"
            }

            val tags = userInput.split(" ")
                .filter { it.length > 3 }
                .take(3)
                .map { it.lowercase() }

            // Related notes check
            val related = existingNotes.filter { n ->
                n.category == cat || tags.any { tag -> n.originalText.lowercase().contains(tag) }
            }

            val connectionNotice = if (related.isNotEmpty()) {
                val sdf = SimpleDateFormat("d MMMM", Locale("tr", "TR"))
                "Bu, ${sdf.format(Date(related.first().timestamp))}'deki \"${related.first().originalText.take(25)}...\" notunla bağlantılı olabilir."
            } else null

            return IntentResult(
                isNote = true,
                category = cat,
                tags = if (tags.isNotEmpty()) tags else listOf("genel"),
                relatedNoteIds = related.map { it.id }.take(2),
                connectionNotice = connectionNotice,
                responseText = "Not edildi 📝"
            )
        }
    }

    private fun String?.isNullFrancoOrBlank(): Boolean {
        return this == null || this.isBlank() || this == "null"
    }
}
