package com.example.ui

import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ChatMessage
import com.example.data.MessageSender
import com.example.data.Note
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryPurpleLight
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    welcomeSummary: String?,
    isProcessing: Boolean,
    isFirestoreConnected: Boolean,
    onSendMessage: (String, String?) -> Unit,
    onOpenDrawer: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onLockApp: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri?.toString()
    }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                inputText = if (inputText.isNotBlank()) "$inputText $spokenText" else spokenText
            }
        }
    }

    fun launchSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Notunuzu sesli olarak söyleyin...")
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (_: Exception) {}
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimaryPurple.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = PrimaryPurpleLight,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Kişisel Akıllı Not",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (isFirestoreConnected) AccentGreen else AccentAmber)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isFirestoreConnected) "Cloud Sync Aktif" else "Yerel Güvenli Mod",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("hamburger_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menü",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToDashboard) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Dashboard",
                            tint = SecondaryCyan
                        )
                    }
                    IconButton(onClick = onNavigateToNotes) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Notlar",
                            tint = PrimaryPurpleLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Welcome Summary Banner (if available)
                if (!welcomeSummary.isNullOrBlank()) {
                    item {
                        WelcomeSummaryCard(summaryText = welcomeSummary)
                    }
                }

                items(messages, key = { it.id }) { msg ->
                    ChatMessageItem(message = msg)
                }

                if (isProcessing) {
                    item {
                        ProcessingIndicatorItem()
                    }
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }
            }

            // Recommendation Chips Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    AssistChip(
                        onClick = { launchSpeechInput() },
                        label = { Text("🎙️ Sesli Not Konuş", fontSize = 12.sp, color = SecondaryCyan) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = DarkSurfaceVariant),
                        border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = SecondaryCyan)
                    )
                }
                item {
                    AssistChip(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        label = { Text("📸 Fotoğraf Ekle", fontSize = 12.sp, color = SecondaryCyan) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = DarkSurfaceVariant),
                        border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = DarkCardBorder)
                    )
                }
                item {
                    AssistChip(
                        onClick = { inputText = "1. Öncelikli Not: " },
                        label = { Text("1️⃣ 1 Maddelik Not", fontSize = 12.sp, color = PrimaryPurpleLight) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = DarkSurfaceVariant),
                        border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = PrimaryPurple)
                    )
                }
                item {
                    AssistChip(
                        onClick = { inputText = "1. \n2. " },
                        label = { Text("2️⃣ 2 Adımlı Hedef", fontSize = 12.sp, color = PrimaryPurpleLight) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = DarkSurfaceVariant),
                        border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = PrimaryPurple)
                    )
                }
                item {
                    AssistChip(
                        onClick = { inputText = "1. \n2. \n3. \n4. \n5. " },
                        label = { Text("5️⃣ 5 Maddelik Liste", fontSize = 12.sp, color = PrimaryPurpleLight) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = DarkSurfaceVariant),
                        border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = PrimaryPurple)
                    )
                }
                item {
                    AssistChip(
                        onClick = { inputText = "Bugünkü ajandam:" },
                        label = { Text("📅 Günlük Ajanda", fontSize = 12.sp, color = TextPrimary) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = DarkSurfaceVariant),
                        border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = DarkCardBorder)
                    )
                }
                item {
                    AssistChip(
                        onClick = { inputText = "Harika bir proje fikri: " },
                        label = { Text("💡 Fikir Kaydet", fontSize = 12.sp, color = TextPrimary) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = DarkSurfaceVariant),
                        border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = DarkCardBorder)
                    )
                }
                item {
                    AssistChip(
                        onClick = { inputText = "İlaç ve sağlık notu: " },
                        label = { Text("💊 Sağlık & İlaç", fontSize = 12.sp, color = TextPrimary) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = DarkSurfaceVariant),
                        border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = DarkCardBorder)
                    )
                }
                item {
                    AssistChip(
                        onClick = { inputText = "Alışveriş listesi: " },
                        label = { Text("🛒 Alışveriş", fontSize = 12.sp, color = TextPrimary) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = DarkSurfaceVariant),
                        border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = DarkCardBorder)
                    )
                }
            }

            // Attached Photo Preview Card (if selected)
            selectedImageUri?.let { uri ->
                Surface(
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple),
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Seçilen Fotoğraf",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Fotoğraf Eklendi",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { selectedImageUri = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Kaldır",
                                tint = TextMuted
                            )
                        }
                    }
                }
            }

            // Input Bar at bottom
            Surface(
                color = DarkSurface,
                tonalElevation = 6.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        modifier = Modifier.testTag("attach_photo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Fotoğraf Ekle",
                            tint = if (selectedImageUri != null) PrimaryPurpleLight else TextSecondary
                        )
                    }

                    IconButton(
                        onClick = { launchSpeechInput() },
                        modifier = Modifier.testTag("speech_input_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Sesle Konuş / Not Yaz",
                            tint = SecondaryCyan
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                "Fotoğraflı not veya soru yazın...",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant,
                            disabledContainerColor = DarkSurfaceVariant,
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    val canSend = (inputText.isNotBlank() || selectedImageUri != null) && !isProcessing
                    IconButton(
                        onClick = {
                            if (canSend) {
                                val textToSend = inputText.trim()
                                val imageToSend = selectedImageUri
                                inputText = ""
                                selectedImageUri = null
                                onSendMessage(textToSend, imageToSend)
                            }
                        },
                        enabled = canSend,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (canSend) Brush.linearGradient(
                                    listOf(
                                        PrimaryPurple,
                                        SecondaryCyan
                                    )
                                )
                                else Brush.linearGradient(
                                    listOf(
                                        DarkSurfaceVariant,
                                        DarkSurfaceVariant
                                    )
                                )
                            )
                            .testTag("send_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Gönder",
                            tint = if (canSend) Color.White else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeSummaryCard(summaryText: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DarkSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = SecondaryCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Günün Özeti & Karşılama",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SecondaryCyan
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = summaryText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatMessageItem(message: ChatMessage) {
    val isUser = message.sender == MessageSender.USER
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) PrimaryPurple else DarkSurface,
                border = if (isUser) null else androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Attached Photo in Chat Bubble
                    if (!message.imageUri.isNullOrBlank()) {
                        AsyncImage(
                            model = message.imageUri,
                            contentDescription = "Not Fotoğrafı",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (message.text.isNotBlank()) {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isUser) Color.White else TextPrimary,
                                lineHeight = 20.sp
                            )
                        )
                    }

                    // Connection notice banner (if note was created and related note found)
                    if (!message.connectionNotice.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SecondaryCyan.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SecondaryCyan.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    tint = SecondaryCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = message.connectionNotice,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SecondaryCyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }

                    // Attached Note Preview Card (if message created a note)
                    message.noteRef?.let { note ->
                        Spacer(modifier = Modifier.height(10.dp))
                        NoteCardPreviewInBubble(note = note)
                    }

                    // Timestamp
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatTime(message.timestamp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isUser) Color.White.copy(alpha = 0.7f) else TextMuted,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NoteCardPreviewInBubble(note: Note) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DarkSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            if (!note.imageUri.isNullOrBlank()) {
                AsyncImage(
                    model = note.imageUri,
                    contentDescription = "Not Fotoğrafı",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Category Chip
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PrimaryPurple.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = note.category,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PrimaryPurpleLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = note.dateFormatted,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Tags
            if (note.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    note.tags.forEach { tag ->
                        Text(
                            text = "#$tag",
                            fontSize = 11.sp,
                            color = SecondaryCyan,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProcessingIndicatorItem() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = PrimaryPurpleLight
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Gemini notu analiz ediyor...",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
