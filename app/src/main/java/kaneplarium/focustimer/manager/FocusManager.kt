package kaneplarium.focustimer.manager

import kaneplarium.focustimer.model.FocusDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object FocusManager {

    private val scope = CoroutineScope(Dispatchers.Main)

    val selectedDuration = MutableStateFlow<FocusDuration?>(null)
    val remainingTime = MutableStateFlow(FocusDuration.MIN_15.durationMillis)
    val isRunning = MutableStateFlow(false)
    val isCompleted = MutableStateFlow(false)

    private var timerJob: Job? = null
    private var unmuterCallback: (() -> Unit)? = null

    fun selectDuration(duration: FocusDuration) {
        if (isRunning.value) return
        selectedDuration.value = duration
        remainingTime.value = duration.durationMillis
    }

    fun startTimer(onMute: () -> Unit, onUnmute: () -> Unit) {
        if (isRunning.value) return
        val currentDuration = selectedDuration.value ?: return

        this.unmuterCallback = onUnmute
        remainingTime.value = currentDuration.durationMillis
        isCompleted.value = false
        isRunning.value = true

        onMute()

        timerJob = scope.launch {
            while (remainingTime.value > 0) {
                delay(1000L)
                remainingTime.update { (it - 1000L).coerceAtLeast(0L) }
            }
            isRunning.value = false
            isCompleted.value = true
            unmuterCallback?.invoke()
            unmuterCallback = null
            remainingTime.value = currentDuration.durationMillis
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        isRunning.value = false
        selectedDuration.value = null
        unmuterCallback?.invoke()
        unmuterCallback = null
    }

    fun acknowledgeCompletion() {
        isCompleted.value = false
        val currentDuration = selectedDuration.value ?: FocusDuration.MIN_15
        remainingTime.value = currentDuration.durationMillis
    }
}
