package kaneplarium.focustimer.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kaneplarium.focustimer.model.FocusDuration
import kaneplarium.focustimer.ui.viewmodel.FocusTimerViewModel
import kaneplarium.focustimer.util.FocusSoundManager

@Composable
fun TimerScreen(
    viewModel: FocusTimerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundManager = remember(context) { FocusSoundManager(context) }

    val selectedDuration by viewModel.selectedDuration.collectAsStateWithLifecycle()
    val remainingTime by viewModel.remainingTime.collectAsStateWithLifecycle()
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()
    val isCompleted by viewModel.isCompleted.collectAsStateWithLifecycle()

    BackHandler(enabled = isRunning) {
        // Allow back
    }

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Muted",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            val totalMillis = selectedDuration?.durationMillis ?: (15 * 60 * 1000L)
            val elapsedMillis = totalMillis - remainingTime
            val forwardProgress =
                if (totalMillis > 0) elapsedMillis.toFloat() / totalMillis.toFloat() else 0f

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DashedProgressBar(progress = forwardProgress)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isRunning) "Handy ist stummgeschaltet" else "Bereit",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            if (!isRunning) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Wähle eine Zeit zum Starten",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FocusDuration.entries.forEach { duration ->
                            val isSelected = selectedDuration == duration
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.selectDuration(duration)
                                    viewModel.startTimer(
                                        context = context,
                                        onMute = { soundManager.mutePhone() },
                                        onUnmute = { soundManager.unmuteAndSet50PercentVolume() }
                                    )
                                },
                                label = { Text(duration.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF4CAF50),
                                    selectedLabelColor = Color.White
                                ),
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Button to minimize app (no icons)
                    Button(
                        onClick = {
                            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_HOME)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(homeIntent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = "App minimieren",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    // Button to stop timer (no icons)
                    Button(
                        onClick = {
                            viewModel.stopTimer(
                                context = context,
                                onUnmute = { soundManager.unmuteAndSet50PercentVolume() }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = "Abbrechen",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }

    // Completion Dialog
    if (isCompleted) {
        AlertDialog(
            onDismissRequest = { viewModel.acknowledgeCompletion() },
            icon = {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = "Fokuszeit geschafft! 🎉",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "Die Zeit ist abgelaufen. Dein Handy wurde wieder laut gestellt und die Lautstärke auf 50% gesetzt.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.acknowledgeCompletion() }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun DashedProgressBar(
    progress: Float, // 0.0 to 1.0 running forwards
    modifier: Modifier = Modifier,
    totalSegments: Int = 20
) {
    val activeSegments = (progress * totalSegments).toInt().coerceIn(0, totalSegments)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until totalSegments) {
            val isActive = i < activeSegments
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(18.dp)
                    .background(
                        color = if (isActive)
                            Color(0xFF4CAF50)
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}
