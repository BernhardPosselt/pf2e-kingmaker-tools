package at.posselt.pfrpg2e.app

import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.t
import at.posselt.pfrpg2e.utils.tpl
import com.foundryvtt.core.*
import com.foundryvtt.core.applications.api.*
import com.foundryvtt.core.applications.ux.FormDataExtended
import js.objects.ReadonlyRecord
import js.objects.unsafeJso
import kotlinx.coroutines.await
import org.w3c.dom.HTMLFormElement

enum class PromptType(val i18nKey: String, val icon: String? = null) {
    ROLL("applications.dialog.roll", "fa-solid fa-dice-d20"),
    OK("applications.dialog.ok"),
}

suspend fun confirm(message: String) =
    try {
        DialogV2.confirm(
            ConfirmOptions(content = message)
        ).await().unsafeCast<Boolean>()
    } catch (_: Throwable) {
        false
    }

/**
 * Typesafe wrapper around the insanity that is DialogV2
 *
 * How await is implemented:
 * * If you click close, a JS Error is being thrown
 * * If a button is clicked:
 *    the value returned from said button's callback is being returned if it has a callback
 *    otherwise the action string of the button is being returned
 *    otherwise undefined is being returned
 */
suspend fun <I, O> awaitablePrompt(
    title: String,
    buttonLabel: String? = null,
    templatePath: String,
    templateContext: Any = unsafeJso(),
    promptType: PromptType = PromptType.OK,
    width: Int? = undefined,
    submit: suspend (I, HTMLFormElement) -> O,
): O {
    val content = tpl(templatePath, templateContext)
    val button = DialogV2Button(
        action = "ok",
        label = buttonLabel ?: t(promptType.i18nKey),
        default = true,
        icon = promptType.icon,
    ) { ev, button, dialog ->
        val form = button.form!!
        val data = FormDataExtended<I>(form)
        buildPromise {
            submit(data.`object`, form)
        }
    }

    @Suppress("UNCHECKED_CAST")
    return DialogV2.prompt(
        PromptOptions(
            content = content,
            classes = arrayOf("km-dialog-form"),
            window = Window(title = title),
            ok = button,
            position = ApplicationPosition(width = width)
        )
    ).await() as O
}

suspend fun <I, O> prompt(
    title: String,
    buttonLabel: String? = null,
    templatePath: String,
    templateContext: Any = unsafeJso(),
    promptType: PromptType = PromptType.OK,
    width: Int? = undefined,
    submit: suspend (I) -> O,
) {
    val content = tpl(templatePath, templateContext)
    val button = DialogV2Button(
        action = "ok",
        label = buttonLabel ?: t(promptType.i18nKey),
        default = true,
        icon = promptType.icon,
    ) { ev, button, dialog ->
        val data = FormDataExtended<I>(button.form!!)
        buildPromise {
            submit(data.`object`)
        }
    }
    DialogV2.prompt(
        PromptOptions(
            content = content,
            classes = arrayOf("km-dialog-form"),
            window = Window(title = title),
            ok = button,
            rejectClose = false,
            position = ApplicationPosition(width = width)
        )
    )
}

data class WaitButton<T, R>(
    val label: String,
    val action: String? = null,
    val icon: String? = undefined,
    val callback: suspend (data: T, action: String) -> R,
)

suspend fun <I, O> wait(
    title: String,
    templatePath: String,
    templateContext: ReadonlyRecord<String, Any?> = unsafeJso(),
    buttons: List<WaitButton<I, O>>,
) {
    val content = tpl(templatePath, templateContext)
    val v2Buttons = buttons.mapIndexed { index, button ->
        val action = button.action ?: button.label
        DialogV2Button(
            action = action,
            label = button.label,
            icon = button.icon,
            callback = { ev, btn, dialog ->
                val data = FormDataExtended<I>(btn.form!!)
                buildPromise {
                    button.callback(data.`object`, action)
                }
            },
            default = index == buttons.size - 1,
        )
    }.toTypedArray()
    DialogV2.wait(
        WaitOptions(
            content = content,
            classes = arrayOf("km-dialog-form"),
            window = Window(title = title),
            buttons = v2Buttons,
            rejectClose = false,
        )
    )
}