package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

import com.example.ui.components.StyledIconTile
import com.example.ui.components.StyledUserIconBadge

enum class ActiveScreen {
    CHAT,
    ALL_NOTES,
    DASHBOARD
}

@Composable
fun AppDrawerContent(
    currentScreen: ActiveScreen,
    categories: List<String>,
    selectedCategory: String?,
    totalNotesCount: Int,
    isFirestoreConnected: Boolean,
    onNavigateTo: (ActiveScreen) -> Unit,
    onSelectCategoryFilter: (String?) -> Unit,
    onLockApp: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = DarkSurface,
        drawerContentColor = TextPrimary,
        modifier = Modifier.width(300.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Drawer Header with User Styled 3D Icon Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(PrimaryPurple.copy(alpha = 0.35f), SecondaryCyan.copy(alpha = 0.2f))
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StyledUserIconBadge(size = 48.dp, shapeRadius = 14.dp)

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Akıllı Not AI",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "$totalNotesCount Not Kayıtlı",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SecondaryCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Menu Navigation Items
            DrawerMenuItem(
                label = "Sohbet & Not Al",
                icon = Icons.Default.Chat,
                isSelected = currentScreen == ActiveScreen.CHAT,
                onClick = { onNavigateTo(ActiveScreen.CHAT) },
                tag = "drawer_nav_chat"
            )

            DrawerMenuItem(
                label = "Tüm Notlar",
                icon = Icons.Default.Bookmark,
                isSelected = currentScreen == ActiveScreen.ALL_NOTES && selectedCategory == null,
                onClick = {
                    onSelectCategoryFilter(null)
                    onNavigateTo(ActiveScreen.ALL_NOTES)
                },
                tag = "drawer_nav_notes"
            )

            DrawerMenuItem(
                label = "İstatistik & Dashboard",
                icon = Icons.Default.Analytics,
                isSelected = currentScreen == ActiveScreen.DASHBOARD,
                onClick = { onNavigateTo(ActiveScreen.DASHBOARD) },
                tag = "drawer_nav_dashboard"
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = DarkCardBorder)
            Spacer(modifier = Modifier.height(16.dp))

            // Categories Filter Section
            Text(
                text = "KATEGORİYE GÖRE FİLTRELE",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            categories.forEach { category ->
                val isCatSelected = currentScreen == ActiveScreen.ALL_NOTES && selectedCategory == category
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isCatSelected) PrimaryPurple.copy(alpha = 0.2f) else Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            onSelectCategoryFilter(category)
                            onNavigateTo(ActiveScreen.ALL_NOTES)
                        }
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = if (isCatSelected) PrimaryPurpleLight else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isCatSelected) PrimaryPurpleLight else TextPrimary,
                                fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(20.dp))

            // Lock App Bottom Action
            HorizontalDivider(color = DarkCardBorder)
            Spacer(modifier = Modifier.height(12.dp))

            DrawerMenuItem(
                label = "Uygulamayı Kilitle",
                icon = Icons.Default.Lock,
                isSelected = false,
                onClick = onLockApp,
                tag = "drawer_lock_app"
            )
        }
    }
}

@Composable
private fun DrawerMenuItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    NavigationDrawerItem(
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            )
        },
        icon = {
            StyledIconTile(
                imageVector = icon,
                contentDescription = null,
                size = 32.dp,
                shapeRadius = 10.dp,
                accentColor = if (isSelected) SecondaryCyan else TextSecondary,
                containerColor = if (isSelected) DarkSurfaceVariant else Color.Transparent
            )
        },
        selected = isSelected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = DarkSurfaceVariant,
            selectedIconColor = SecondaryCyan,
            selectedTextColor = TextPrimary,
            unselectedContainerColor = Color.Transparent,
            unselectedIconColor = TextSecondary,
            unselectedTextColor = TextPrimary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
    )
}
