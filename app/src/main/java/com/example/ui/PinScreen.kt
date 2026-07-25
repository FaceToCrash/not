package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.ui.theme.AccentRose
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

import com.example.ui.components.StyledUserIconBadge

@Composable
fun PinScreen(
    onPinSuccess: () -> Unit,
    correctPin: String = "9812",
    onWrongPinEntered: ((failedAttempts: Int, wrongPin: String) -> Unit)? = null
) {
    val context = LocalContext.current
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var failedAttempts by remember { mutableStateOf(0) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun handleKeyInput(key: String) {
        if (enteredPin.length < 4) {
            val newPin = enteredPin + key
            enteredPin = newPin
            errorMessage = null

            if (newPin.length == 4) {
                val wrongPinAttempt = newPin
                if (newPin == correctPin) {
                    failedAttempts = 0
                    onPinSuccess()
                } else {
                    failedAttempts += 1
                    enteredPin = ""
                    errorMessage = if (failedAttempts >= 3) {
                        "Hatalı PIN! ($failedAttempts. deneme). Güvenlik kaydı ve fotoğraflar alındı!"
                    } else {
                        "Hatalı PIN! Lütfen tekrar deneyin."
                    }
                    onWrongPinEntered?.invoke(failedAttempts, wrongPinAttempt)
                }
            }
        }
    }

    fun handleBackspace() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            errorMessage = null
        }
    }

    fun handleClear() {
        enteredPin = ""
        errorMessage = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkBackground,
                        Color(0xFF141622),
                        DarkBackground
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // App Header 3D User Style Icon
            StyledUserIconBadge(size = 80.dp, shapeRadius = 22.dp)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Akıllı Not Defteri",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Güvenli notlarınıza erişmek için PIN kodunu girin",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // PIN Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < enteredPin.length
                    val sizeScale by animateFloatAsState(if (isFilled) 1.2f else 1.0f, label = "dot")

                    Box(
                        modifier = Modifier
                            .scale(sizeScale)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) PrimaryPurple else DarkSurfaceVariant
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (isFilled) SecondaryCyan else DarkCardBorder,
                                shape = CircleShape
                            )
                    )
                }
            }

            // Error Message
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.height(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = AccentRose,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Keypad Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(280.dp)
            ) {
                val padKeys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "DEL")
                )

                for (row in padKeys) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (key in row) {
                            KeypadButton(
                                key = key,
                                onClick = {
                                    when (key) {
                                        "DEL" -> handleBackspace()
                                        "C" -> handleClear()
                                        else -> handleKeyInput(key)
                                    }
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = SecondaryCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Gözlerden Uzak • 256-Bit Şifreli Özel Not Kalkanı",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                )
            }
        }
    }
}

@Composable
private fun KeypadButton(
    key: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = when (key) {
            "DEL", "C" -> DarkSurfaceVariant.copy(alpha = 0.5f)
            else -> DarkSurface
        },
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = DarkCardBorder
        ),
        modifier = Modifier
            .size(68.dp)
            .testTag("pin_key_$key")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (key == "DEL") {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Sil",
                    tint = TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = key,
                    fontSize = if (key == "C") 18.sp else 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when (key) {
                        "C" -> AccentRose
                        else -> TextPrimary
                    }
                )
            }
        }
    }
}
