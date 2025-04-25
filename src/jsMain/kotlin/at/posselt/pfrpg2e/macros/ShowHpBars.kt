package at.posselt.pfrpg2e.macros

import at.posselt.pfrpg2e.app.awaitablePrompt
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.kingdom.structures.isStructure
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.takeIfInstance
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.Game
import com.foundryvtt.core.documents.Actor
import com.foundryvtt.core.documents.TokenDocument
import com.foundryvtt.core.game
import com.foundryvtt.pf2e.actor.PF2ENpc
import js.objects.recordOf
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject

enum class DisplayBars(val foundryValue: Int) : ValueEnum, Translatable {
    NEVER_DISPLAYED(0),
    WHEN_CONTROLLED(10),
    HOVERED_BY_OWNER(20),
    HOVERED_BY_ANYONE(30),
    ALWAYS_FOR_OWNER(40),
    ALWAYS_FOR_EVERYONE(50);


    companion object {
        fun fromString(value: String) = fromCamelCase<DisplayBars>(value)
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "displayBars.$value"
}

private suspend fun TokenDocument.changeDisplayBarsTo(value: DisplayBars) {
    update(recordOf("displayBars" to value.foundryValue)).await()
}

private inline fun <reified T : Actor> TokenDocument.getActor(): T? {
    val tokenActor = actor
    return if (tokenActor is T && actorLink) {
        tokenActor
    } else if (tokenActor is T) {
        tokenActor.parent
            ?.takeIfInstance<TokenDocument>()
            ?.baseActor
            ?.takeIfInstance<T>()
    } else {
        null
    }
}

@JsPlainObject
external interface DisplayBarsData {
    val displayBars: String
}


suspend fun Game.showAllNpcHpBars() {
    awaitablePrompt<DisplayBarsData, Unit>(
        title = t("macros.showAllNpcHpBars.title"),
        templatePath = "components/forms/form.hbs",
        buttonLabel = t("applications.apply"),
        width = 400,
        templateContext = recordOf(
            "description" to t("macros.showAllNpcHpBars.description"),
            "formRows" to formContext(
                Select.fromEnum<DisplayBars>(
                    name = "displayBars",
                    value = DisplayBars.HOVERED_BY_OWNER,
                )
            )
        )
    ) { data, _ ->
        game.scenes.contents
            .flatMap { it.tokens.contents.toList() }
            .filter { it.getActor<PF2ENpc>() != null && !it.isStructure() }
            .forEach { token -> token.changeDisplayBarsTo(DisplayBars.fromString(data.displayBars)!!) }
    }
}