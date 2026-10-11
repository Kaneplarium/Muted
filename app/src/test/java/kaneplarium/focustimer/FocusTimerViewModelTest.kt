package kaneplarium.focustimer

import kaneplarium.focustimer.manager.FocusManager
import kaneplarium.focustimer.model.FocusDuration
import kaneplarium.focustimer.ui.viewmodel.FocusTimerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FocusTimerViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: FocusTimerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = FocusTimerViewModel()
        FocusManager.stopTimer()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testDurationSelection() {
        assertNull(viewModel.selectedDuration.value)

        viewModel.selectDuration(FocusDuration.MIN_30)
        assertEquals(FocusDuration.MIN_30, viewModel.selectedDuration.value)
        assertEquals(FocusDuration.MIN_30.durationMillis, viewModel.remainingTime.value)

        viewModel.selectDuration(FocusDuration.MIN_60)
        assertEquals(FocusDuration.MIN_60, viewModel.selectedDuration.value)
        assertEquals(FocusDuration.MIN_60.durationMillis, viewModel.remainingTime.value)
    }

    @Test
    fun testStartTimerAndStop() {
        var muteCalled = false
        var unmuteCalled = false
        viewModel.selectDuration(FocusDuration.MIN_15)
        FocusManager.startTimer(
            onMute = { muteCalled = true },
            onUnmute = { unmuteCalled = true }
        )

        assertTrue(viewModel.isRunning.value)
        assertTrue(muteCalled)

        FocusManager.stopTimer()
        assertFalse(viewModel.isRunning.value)
        assertTrue(unmuteCalled)
    }
}
