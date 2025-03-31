package at.posselt.pfrpg2e.app

import at.posselt.pfrpg2e.app.forms.FileInput
import at.posselt.pfrpg2e.app.forms.formContext
import com.foundryvtt.core.ui
import js.objects.JsPlainObject
import js.objects.recordOf
import kotlinx.coroutines.await
import org.w3c.dom.HTMLInputElement
import org.w3c.fetch.Response

@JsPlainObject
@Suppress("unused")
private external interface FilePickerData {
    val file: String
}


suspend fun jsonFilePicker(
    title: String,
    label: String = "File",
    accept: List<String> = listOf("application/json"),
    help: String? = null,
): String = awaitablePrompt<FilePickerData, String>(
    title = title,
    buttonLabel = "Upload",
    templatePath = "components/forms/form.hbs",
    templateContext = recordOf(
        "formRows" to formContext(
            FileInput(
                name = "file",
                label = label,
                accept = accept,
                help = help,
            )
        )
    ),
) { data, form ->
    val input = form.querySelector("input") as HTMLInputElement
    val file = input.files?.item(0)
    if (file == null) {
        val msg = "Please select a file!"
        ui.notifications.error(msg)
        throw IllegalArgumentException(msg)
    }
    Response(file).text().await()
}
