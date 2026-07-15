package com.kilotakip.app.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kilotakip.app.ui.theme.GradientEnd
import com.kilotakip.app.ui.theme.GradientStart
import com.kilotakip.app.ui.theme.OrangeAccent
import com.kilotakip.app.ui.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -50 }
                ) {
                    GreetingCard(uiState.userName, uiState.motivationalMessage)
                }
            }

            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(700, 200)) + scaleIn(tween(500, 200), initialScale = 0.8f)
                ) {
                    ProgressCard(
                        currentWeight = uiState.lastWeight,
                        targetWeight = uiState.targetWeightKg,
                        progress = uiState.progressPercent,
                        remainingKg = uiState.remainingKg,
                        healthyMin = uiState.healthyWeightMinKg,
                        healthyMax = uiState.healthyWeightMaxKg
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(500, 400)) + slideInVertically(tween(500, 400)) { 50 }
                ) {
                    WeightChangeCard(lastWeight = uiState.lastWeight, weightChange = uiState.weightChange)
                }
            }

            if (uiState.entries.isNotEmpty()) {
                item {
                    Text(
                        "Son Kayıtlar",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                items(uiState.entries.take(5)) { entry ->
                    WeightEntryItem(
                        weight = entry.weightKg,
                        note = entry.note,
                        date = entry.recordedAt
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }

        ExtendedFloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Kilo Ekle")
        }

        if (showAddDialog) {
            AddWeightDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { weight, note ->
                    viewModel.addWeight(weight, note)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
private fun GreetingCard(name: String, message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            GradientStart.copy(alpha = 0.1f),
                            GradientEnd.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "Merhaba, ${name.split(" ").firstOrNull() ?: ""}!",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProgressCard(
    currentWeight: Double?,
    targetWeight: Double?,
    progress: Float,
    remainingKg: Double?,
    healthyMin: Double?,
    healthyMax: Double?
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Hedefe Yolculuk", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(20.dp))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val trackColor = MaterialTheme.colorScheme.surfaceVariant

                Canvas(modifier = Modifier.size(180.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val arcSize = size.width - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    drawArc(
                        color = trackColor,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    drawArc(
                        brush = Brush.sweepGradient(listOf(GradientStart, GradientEnd)),
                        startAngle = 135f,
                        sweepAngle = 270f * animatedProgress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (currentWeight != null) {
                        Text(
                            text = "%.1f".format(currentWeight),
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "kg",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Icon(
                            Icons.Filled.FitnessCenter,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Kayıt yok",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Hedef", targetWeight?.let { "%.1f kg".format(it) } ?: "-")
                StatItem("Kalan", remainingKg?.let { "%.1f kg".format(it) } ?: "-")
                StatItem("İlerleme", "%${(animatedProgress * 100).toInt()}")
            }

            if (healthyMin != null && healthyMax != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Sağlıklı kilo aralığın: %.1f - %.1f kg".format(healthyMin, healthyMax),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun WeightChangeCard(lastWeight: Double?, weightChange: Double?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                weightChange == null -> MaterialTheme.colorScheme.surfaceVariant
                weightChange <= 0 -> SuccessGreen.copy(alpha = 0.1f)
                else -> OrangeAccent.copy(alpha = 0.1f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            weightChange == null -> MaterialTheme.colorScheme.surfaceVariant
                            weightChange <= 0 -> SuccessGreen.copy(alpha = 0.2f)
                            else -> OrangeAccent.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        weightChange == null -> Icons.Filled.TrendingFlat
                        weightChange < 0 -> Icons.Filled.TrendingDown
                        weightChange > 0 -> Icons.Filled.TrendingUp
                        else -> Icons.Filled.EmojiEvents
                    },
                    contentDescription = null,
                    tint = when {
                        weightChange == null -> MaterialTheme.colorScheme.onSurfaceVariant
                        weightChange <= 0 -> SuccessGreen
                        else -> OrangeAccent
                    }
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = when {
                        weightChange == null -> "İlk kaydını bekliyor"
                        weightChange < 0 -> "%.1f kg verdin!".format(-weightChange)
                        weightChange > 0 -> "+%.1f kg artış".format(weightChange)
                        else -> "Değişim yok"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Son değişim",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WeightEntryItem(weight: Double, note: String?, date: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("%.1f kg".format(weight), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    note?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Text(
                formatDate(date),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDate(epochMillis: Long): String {
    return SimpleDateFormat("dd MMM, HH:mm", Locale("tr")).format(Date(epochMillis))
}
