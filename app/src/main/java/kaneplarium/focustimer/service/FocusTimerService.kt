package kaneplarium.focustimer.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kaneplarium.focustimer.manager.FocusManager
import kaneplarium.focustimer.util.FocusSoundManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FocusTimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private var notificationUpdateJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_START) {
            startForegroundServiceWithProgress()
        } else if (action == ACTION_STOP) {
            val soundManager = FocusSoundManager(applicationContext)
            soundManager.unmuteAndSet50PercentVolume()
            FocusManager.stopTimer()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return START_STICKY
    }

    private fun startForegroundServiceWithProgress() {
        val initialNotification = buildNotification("Handy ist stummgeschaltet", 0f)
        try {
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(
                    NOTIFICATION_ID,
                    initialNotification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, initialNotification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        notificationUpdateJob?.cancel()
        notificationUpdateJob = serviceScope.launch {
            FocusManager.remainingTime.collectLatest { millis ->
                val totalMillis = FocusManager.selectedDuration.value?.durationMillis ?: (15 * 60 * 1000L)
                val elapsedMillis = totalMillis - millis
                val forwardProgress = if (totalMillis > 0) elapsedMillis.toFloat() / totalMillis.toFloat() else 0f
                updateNotification("Handy ist stummgeschaltet", forwardProgress)
            }
        }
    }

    private fun buildNotification(text: String, progress: Float): android.app.Notification {
        val stopIntent = Intent(this, FocusTimerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val progressInt = (progress * 100).toInt().coerceIn(0, 100)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Muted")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setProgress(100, progressInt, false)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Abbrechen",
                stopPendingIntent
            )
            .build()
    }

    private fun updateNotification(text: String, progress: Float) {
        val notification = buildNotification(text, progress)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        notificationUpdateJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Focus Timer Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "focus_timer_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "ACTION_START_FOCUS"
        const val ACTION_STOP = "ACTION_STOP_FOCUS"
    }
}
