package at.posselt.pfrpg2e.app

import at.posselt.pfrpg2e.app.forms.FileInput
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.ui
import js.objects.recordOf
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLInputElement
import org.w3c.fetch.Response

@JsPlainObject
@Suppress("unused")
private external interface FilePickerData {
    val file: String
}


suspend fun jsonFilePicker(
    title: String,
    label: String? = null,
    accept: List<String> = listOf("application/json"),
    help: String? = null,
): String = awaitablePrompt<FilePickerData, String>(
    title = title,
    buttonLabel = t("applications.jsonFilePicker.upload"),
    templatePath = "components/forms/form.hbs",
    templateContext = recordOf(
        "formRows" to formContext(
            FileInput(
                name = "file",
                label = label ?: t("applications.jsonFilePicker.file"),
                accept = accept,
                help = help,
            )
        )
    ),
) { _, form ->
    val input = form.querySelector("input") as HTMLInputElement
    val file = input.files?.item(0)
    if (file == null) {
        val msg = t("applications.jsonFilePicker.selectFile")
        ui.notifications.error(msg)
        throw IllegalArgumentException(msg)
    }
    Response(file).text().await()
}
