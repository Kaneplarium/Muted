package kaneplarium.focustimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kaneplarium.focustimer.navigation.NavRoute
import kaneplarium.focustimer.ui.TimerScreen
import kaneplarium.focustimer.ui.theme.FocusTimerTheme
import kaneplarium.focustimer.ui.viewmodel.FocusTimerViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FocusTimerTheme {
                val backStack = rememberNavBackStack(NavRoute.Timer)
                val decorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
                    rememberViewModelStoreNavEntryDecorator<NavKey>()
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavDisplay(
                        backStack = backStack,
                        entryDecorators = decorators
                    ) { key ->
                        when (key) {
                            is NavRoute.Timer -> NavEntry(key) {
                                val viewModel = viewModel<FocusTimerViewModel>()
                                TimerScreen(viewModel = viewModel)
                            }
                            else -> error("Unknown route: $key")
                        }
                    }
                }
            }
        }
    }
}
