package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.ActiveScreen
import com.example.ui.AllNotesScreen
import com.example.ui.AppDrawerContent
import com.example.ui.ChatScreen
import com.example.ui.DashboardScreen
import com.example.ui.MainViewModel
import com.example.ui.PinScreen
import com.example.ui.theme.AkilliNotTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AkilliNotTheme {
                AkilliNotApp()
            }
        }
    }
}

@Composable
fun AkilliNotApp(viewModel: MainViewModel = viewModel()) {
    val isUnlocked by viewModel.isUnlocked.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val welcomeSummary by viewModel.welcomeSummary.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val isFirestoreConnected by viewModel.isFirestoreConnected.collectAsState()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val weeklyNarrativeReport by viewModel.weeklyNarrativeReport.collectAsState()
    val isGeneratingReport by viewModel.isGeneratingReport.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    if (!isUnlocked) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        PinScreen(
            onPinSuccess = {
                viewModel.unlockApp()
            },
            onWrongPinEntered = { failedAttempts, wrongPin ->
                viewModel.recordWrongPinAttempt(context, lifecycleOwner, failedAttempts, wrongPin)
            }
        )
    } else {
        val categories = remember(notes) {
            notes.map { it.category }.distinct().sorted()
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AppDrawerContent(
                    currentScreen = currentScreen,
                    categories = categories,
                    selectedCategory = selectedCategoryFilter,
                    totalNotesCount = notes.size,
                    isFirestoreConnected = isFirestoreConnected,
                    onNavigateTo = { screen ->
                        viewModel.navigateTo(screen)
                        scope.launch { drawerState.close() }
                    },
                    onSelectCategoryFilter = { cat ->
                        viewModel.setCategoryFilter(cat)
                    },
                    onLockApp = {
                        viewModel.lockApp()
                        scope.launch { drawerState.close() }
                    }
                )
            }
        ) {
            when (currentScreen) {
                ActiveScreen.CHAT -> {
                    ChatScreen(
                        messages = chatMessages,
                        welcomeSummary = welcomeSummary,
                        isProcessing = isProcessing,
                        isFirestoreConnected = isFirestoreConnected,
                        onSendMessage = { text, imageUri -> viewModel.sendMessage(text, imageUri) },
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigateToNotes = { viewModel.navigateTo(ActiveScreen.ALL_NOTES) },
                        onNavigateToDashboard = { viewModel.navigateTo(ActiveScreen.DASHBOARD) },
                        onLockApp = { viewModel.lockApp() }
                    )
                }
                ActiveScreen.ALL_NOTES -> {
                    AllNotesScreen(
                        notes = notes,
                        selectedCategory = selectedCategoryFilter,
                        searchQuery = searchQuery,
                        onSelectCategory = { cat -> viewModel.setCategoryFilter(cat) },
                        onSearchQueryChange = { query -> viewModel.setSearchQuery(query) },
                        onDeleteNote = { noteId -> viewModel.deleteNote(noteId) },
                        onNavigateBack = { viewModel.navigateTo(ActiveScreen.CHAT) }
                    )
                }
                ActiveScreen.DASHBOARD -> {
                    val analytics = remember(notes) { viewModel.computeAnalyticsSummary() }
                    DashboardScreen(
                        analytics = analytics,
                        weeklyNarrativeReport = weeklyNarrativeReport,
                        isGeneratingReport = isGeneratingReport,
                        onGenerateReportClick = { viewModel.generateWeeklyReport() },
                        onNavigateBack = { viewModel.navigateTo(ActiveScreen.CHAT) }
                    )
                }
            }
        }
    }
}
