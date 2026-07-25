package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AnalyticsSummary
import com.example.data.DailySummary
import com.example.data.Note
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentGreen
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

import com.example.ui.components.StyledIconTile
import com.example.ui.components.StyledUserIconBadge

private val PaletteColors = listOf(
    PrimaryPurple,
    SecondaryCyan,
    AccentGreen,
    AccentAmber,
    PrimaryPurpleLight,
    AccentRose,
    Color(0xFFE040FB),
    Color(0xFF00B0FF)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    analytics: AnalyticsSummary,
    weeklyNarrativeReport: String?,
    isGeneratingReport: Boolean,
    onGenerateReportClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StyledUserIconBadge(size = 32.dp, shapeRadius = 8.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "İstatistik & Analiz Dashboard",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }
                },
                navigationIcon = {
                    Box(modifier = Modifier.padding(start = 8.dp)) {
                        StyledIconTile(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            size = 36.dp,
                            shapeRadius = 10.dp,
                            accentColor = TextPrimary,
                            onClick = onNavigateBack
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 1. Top Totals Overview Grid
            item {
                Text(
                    text = "📊 Toplam Not İstatistikleri",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SecondaryCyan
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatMetricCard("Tüm Zamanlar", "${analytics.totalNotesAllTime}", PrimaryPurple, Modifier.weight(1f))
                    StatMetricCard("Bu Ay", "${analytics.totalNotesThisMonth}", SecondaryCyan, Modifier.weight(1f))
                    StatMetricCard("Bu Hafta", "${analytics.totalNotesThisWeek}", AccentGreen, Modifier.weight(1f))
                    StatMetricCard("Bugün", "${analytics.totalNotesToday}", AccentAmber, Modifier.weight(1f))
                }
            }

            // 2. Streaks & Word Metrics
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Streak Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = DarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(AccentAmber.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fireplace,
                                    contentDescription = null,
                                    tint = AccentAmber,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Mevcut Seri: ${analytics.currentStreak} gün", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                                Text("En uzun: ${analytics.longestStreak} gün", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                            }
                        }
                    }

                    // Word Count Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = DarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(AccentGreen.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = AccentGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Kelime: ${analytics.totalWordCount}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                                Text("Ort. uzunluk: ${analytics.averageNoteLength} kelime", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                            }
                        }
                    }
                }
            }

            // 3. Category Distribution Donut Chart
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Category, contentDescription = null, tint = PrimaryPurpleLight)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Kategori Dağılımı",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (analytics.categoryCounts.isEmpty()) {
                            Text("Henüz kategori verisi yok", color = TextMuted, fontSize = 12.sp)
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Donut Chart
                                CategoryDonutChart(
                                    categoryCounts = analytics.categoryCounts,
                                    modifier = Modifier.size(130.dp)
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                // Legend
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    val total = analytics.categoryCounts.values.sum().coerceAtLeast(1)
                                    analytics.categoryCounts.entries.take(5).forEachIndexed { idx, entry ->
                                        val color = PaletteColors[idx % PaletteColors.size]
                                        val pct = (entry.value * 100f / total).toInt()
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${entry.key} (%$pct - ${entry.value})",
                                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Activity Over Time Bar Chart
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.BarChart, contentDescription = null, tint = SecondaryCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Zaman İçindeki Aktivite (Son 7 Gün)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        ActivityBarChart(
                            activityData = analytics.activityByDay,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )
                    }
                }
            }

            // 5. Intraday Writing Hours (00-23 Heatmap / Bar chart)
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = AccentAmber)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gün İçi Yazma Saatleri (24 Saat)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        HourlyActivityGrid(
                            hourlyCounts = analytics.hourlyActivity,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp)
                        )
                    }
                }
            }

            // 6. Top Tags Cloud / List
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Label, contentDescription = null, tint = AccentGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "En Sık Kullanılan Etiketler",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (analytics.topTags.isEmpty()) {
                            Text("Henüz etiket verisi yok", color = TextMuted, fontSize = 12.sp)
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                analytics.topTags.forEach { (tag, count) ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = DarkSurfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.3f))
                                    ) {
                                        Text(
                                            text = "#$tag ($count)",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = PrimaryPurpleLight,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 7. Linked Notes Metrics & Most Connected Notes
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = SecondaryCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Bağlantılı Notlar (${analytics.linkedNotesCount} Bağlantı)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (analytics.mostConnectedNotes.isEmpty()) {
                            Text("Diğer notlarla ilişkilendirilmiş not bulunmuyor.", color = TextMuted, fontSize = 12.sp)
                        } else {
                            Text("En Çok Bağlantısı Olan Notlar:", style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary))
                            Spacer(modifier = Modifier.height(6.dp))
                            analytics.mostConnectedNotes.take(3).forEach { note ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = DarkSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "\"${note.originalText.take(45)}...\"",
                                            style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = SecondaryCyan.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "${note.relatedNoteIds.size} bağlantı",
                                                style = MaterialTheme.typography.labelSmall.copy(color = SecondaryCyan, fontSize = 10.sp),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 8. Daily Summary Cards List
            if (analytics.dailySummaries.isNotEmpty()) {
                item {
                    Text(
                        text = "📅 Günlük Otomatik Özetler",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SecondaryCyan
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(analytics.dailySummaries) { daily ->
                    DailySummaryCardItem(daily = daily)
                }
            }

            // 9. Gemini Narrative Report Section
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryPurpleLight)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Gemini Anlatı Raporu",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                )
                            }

                            if (isGeneratingReport) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = PrimaryPurpleLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (weeklyNarrativeReport != null) {
                            Text(
                                text = weeklyNarrativeReport,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextPrimary,
                                    lineHeight = 22.sp
                                )
                            )
                        } else {
                            Text(
                                text = "Gemini sizin için tüm dönemin hikaye formatındaki analiz raporunu oluşturabilir.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun StatMetricCard(title: String, value: String, accentColor: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = accentColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(title, fontSize = 10.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun CategoryDonutChart(
    categoryCounts: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val total = categoryCounts.values.sum().coerceAtLeast(1)

    Canvas(modifier = modifier) {
        var startAngle = -90f
        val strokeWidth = 28.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeftOffset = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
        val arcSize = Size(diameter, diameter)

        categoryCounts.entries.forEachIndexed { idx, entry ->
            val sweepAngle = (entry.value * 360f / total)
            val color = PaletteColors[idx % PaletteColors.size]

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeftOffset,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )

            startAngle += sweepAngle
        }
    }
}

@Composable
private fun ActivityBarChart(
    activityData: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    if (activityData.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Veri yok", color = TextMuted, fontSize = 12.sp)
        }
        return
    }

    val maxVal = activityData.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            activityData.forEach { (day, count) ->
                val barHeightRatio = count.toFloat() / maxVal
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (count > 0) "$count" else "",
                        fontSize = 10.sp,
                        color = SecondaryCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .fillMaxSize(fraction = barHeightRatio.coerceAtLeast(0.08f))
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                if (count > 0) PrimaryPurple else DarkSurfaceVariant
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            activityData.forEach { (day, _) ->
                Text(
                    text = day.take(3),
                    fontSize = 10.sp,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun HourlyActivityGrid(
    hourlyCounts: List<Int>,
    modifier: Modifier = Modifier
) {
    val maxVal = hourlyCounts.maxOrNull()?.coerceAtLeast(1) ?: 1

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        for (hour in 0 until 24) {
            val count = hourlyCounts.getOrElse(hour) { 0 }
            val ratio = count.toFloat() / maxVal

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize(fraction = ratio.coerceAtLeast(0.1f))
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (count > 0) AccentAmber else DarkSurfaceVariant
                        )
                )
            }
        }
    }
}

@Composable
private fun DailySummaryCardItem(daily: DailySummary) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${daily.noteCount}",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPurpleLight,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = daily.dateString,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DarkSurfaceVariant
                    ) {
                        Text(
                            text = daily.dominantCategory,
                            fontSize = 10.sp,
                            color = SecondaryCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = daily.summary,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }
    }
}
