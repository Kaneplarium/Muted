package kaneplarium.focustimer.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import kaneplarium.focustimer.manager.FocusManager
import kaneplarium.focustimer.model.FocusDuration
import kaneplarium.focustimer.service.FocusTimerService
import kotlinx.coroutines.flow.StateFlow

class FocusTimerViewModel : ViewModel() {

    val selectedDuration: StateFlow<FocusDuration?> = FocusManager.selectedDuration
    val remainingTime: StateFlow<Long> = FocusManager.remainingTime
    val isRunning: StateFlow<Boolean> = FocusManager.isRunning
    val isCompleted: StateFlow<Boolean> = FocusManager.isCompleted

    fun selectDuration(duration: FocusDuration) {
        FocusManager.selectDuration(duration)
    }

    fun startTimer(context: Context, onMute: () -> Unit, onUnmute: () -> Unit) {
        FocusManager.startTimer(
            onMute = onMute,
            onUnmute = {
                onUnmute()
                stopService(context)
            }
        )
        startService(context)
    }

    fun stopTimer(context: Context, onUnmute: () -> Unit) {
        FocusManager.stopTimer()
        onUnmute()
        stopService(context)
    }

    private fun startService(context: Context) {
        try {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = FocusTimerService.ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopService(context: Context) {
        try {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = FocusTimerService.ACTION_STOP
            }
            context.startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun acknowledgeCompletion() = FocusManager.acknowledgeCompletion()
}
