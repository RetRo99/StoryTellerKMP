package  com.retro99.base.ui.compose

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

sealed interface TextWrapper {
    data class Text(
        val string: String,
    ) : TextWrapper

    data class Resource(
        val resource: StringResource,
        val args: List<Any>,
    ) : TextWrapper {
        constructor(resource: StringResource, vararg args: Any) : this(resource, args.toList())
    }
}

@Composable
fun stringTextWrapper(wrapper: TextWrapper): String {
    return when (wrapper) {
        is TextWrapper.Resource -> stringResource(wrapper.resource, *wrapper.args.toTypedArray())
        is TextWrapper.Text -> wrapper.string
    }
}