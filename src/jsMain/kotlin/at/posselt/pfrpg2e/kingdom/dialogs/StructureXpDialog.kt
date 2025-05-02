package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.app.prompt
import at.posselt.pfrpg2e.data.kingdom.structures.StructureTrait
import at.posselt.pfrpg2e.kingdom.structures.getImportedStructures
import at.posselt.pfrpg2e.utils.asAnyObject
import com.foundryvtt.core.Game
import js.objects.JsPlainObject


@Suppress("unused")
@JsPlainObject
private external interface StructureXpDialogContext {
    val formRows: Array<FormElementContext>
}

@JsPlainObject
private external interface StructureXpDialogData {
    val structure: String
}

suspend fun structureXpDialog(
    game: Game,
    onOk: suspend (Int) -> Unit,
) {
    val importedStructures = game.getImportedStructures()
    val options = importedStructures
        .map { SelectOption(value = it.id, label = it.name) }
    prompt<StructureXpDialogData, Unit>(
        title = "Gain XP From Built Structure",
        templateContext = StructureXpDialogContext(
            formRows = arrayOf(
                Select(
                    name = "structure",
                    label = "Structure",
                    options = options,
                ).toContext()
            )
        ).asAnyObject(),
        templatePath = "components/forms/form.hbs",
    ) { data ->
        val structure = importedStructures
            .find { it.id == data.structure }
        if (structure != null) {
            val isEdifice = StructureTrait.EDIFICE in structure.traits
            val rp = structure.construction.rp
            val baseXp = 5 + (rp / 10) * 5
            val xp = if (isEdifice) {
                baseXp * 2
            } else {
                baseXp
            }
            onOk(xp)
        }
    }
}
