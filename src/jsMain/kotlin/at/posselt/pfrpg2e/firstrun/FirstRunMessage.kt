package at.posselt.pfrpg2e.firstrun

import at.posselt.pfrpg2e.data.checks.RollMode
import at.posselt.pfrpg2e.settings.pfrpg2eKingdomCampingWeather
import at.posselt.pfrpg2e.utils.bindChatClick
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.buildUuid
import at.posselt.pfrpg2e.utils.isFirstGM
import at.posselt.pfrpg2e.utils.postChatMessage
import at.posselt.pfrpg2e.utils.postChatTemplate
import com.foundryvtt.core.Game
import com.foundryvtt.core.ui.TextEditor
import js.objects.recordOf
import kotlinx.coroutines.await

suspend fun showFirstRunMessage(game: Game) {
    val settings = game.settings.pfrpg2eKingdomCampingWeather
    if (!settings.getDisableFirstRunMessage() && game.isFirstGM()) {
        postChatTemplate(
            templatePath = "chatmessages/firstrun.hbs",
            rollMode = RollMode.GMROLL,
            templateContext = recordOf(
                "manual" to TextEditor.enrichHTML(buildUuid("Compendium.pf2e-kingmaker-tools.kingmaker-tools-journals.JournalEntry.iAQCUYEAq4Dy8uCY"))
                    .await(),
                "camping" to TextEditor.enrichHTML(
                    buildUuid(
                        "Compendium.pf2e-kingmaker-tools.kingmaker-tools-macros.Macro.GXeKz3qKlsoxcaTg",
                        "Camping"
                    )
                ).await(),
                "kingdom" to TextEditor.enrichHTML(
                    buildUuid(
                        "Compendium.pf2e-kingmaker-tools.kingmaker-tools-macros.Macro.1LmPW2OlHgJvedY8",
                        "Kingdom"
                    )
                ).await(),
                "license" to TextEditor.enrichHTML(
                    buildUuid(
                        "Compendium.pf2e-kingmaker-tools.kingmaker-tools-journals.JournalEntry.8DyhRcPn7d8hlC1y",
                        "Licenses"
                    )
                ).await(),
                "upgrading" to TextEditor.enrichHTML(
                    buildUuid(
                        "Compendium.pf2e-kingmaker-tools.kingmaker-tools-journals.JournalEntry.wz1mIWMxDJVsMIUd",
                        "Upgrading Notices"
                    )
                ).await(),
            )
        )
    }
    bindChatClick(".km-disable-firstrun-message") { _, _ ->
        buildPromise {
            settings.setDisableFirstRunMessage(true)
            postChatMessage("Disabled Kingdom Building, Camping & Weather first run message")
        }
    }
}