import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.retro99.base.AppInitializer
import com.retro99.parrot.App
import com.retro99.parrot.di.initKoin
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.configureWebResources

@OptIn(ExperimentalComposeUiApi::class, ExperimentalResourceApi::class)
fun main() {
    configureWebResources {
        resourcePathMapping { path -> "./$path" }
    }
    initKoin().koin.getAll<AppInitializer>().forEach { it.initialize() }
    ComposeViewport("composeApp") {
        App()
    }
}
