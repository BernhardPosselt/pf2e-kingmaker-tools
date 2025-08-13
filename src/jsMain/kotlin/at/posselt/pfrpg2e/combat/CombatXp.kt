package at.posselt.pfrpg2e.combat

import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.SectionContext
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.app.prompt
import at.posselt.pfrpg2e.macros.PointsForPlayer
import at.posselt.pfrpg2e.macros.updateHeroPoints
import at.posselt.pfrpg2e.macros.updateXP
import at.posselt.pfrpg2e.settings.pfrpg2eKingdomCampingWeather
import at.posselt.pfrpg2e.takeIfInstance
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.isFirstGM
import at.posselt.pfrpg2e.utils.postChatTemplate
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.Game
import com.foundryvtt.core.documents.onPreDeleteCombat
import com.foundryvtt.core.helpers.TypedHooks
import com.foundryvtt.pf2e.actor.PF2ECharacter
import js.array.toTypedArray
import js.objects.Record
import js.objects.recordOf
import kotlinx.browser.document
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement

@JsPlainObject
external interface CombatXpData {
    val xp: Int
}

fun registerCombatXpHooks(game: Game) {
    TypedHooks.onPreDeleteCombat { d, _, _ ->
        if (game.isFirstGM() && game.settings.pfrpg2eKingdomCampingWeather.getEnableAfterCombatDialog()) {
            buildPromise {
                val xp = document.querySelector("#combat .award .value")
                    ?.takeIfInstance<HTMLElement>()
                    ?.innerText
                    ?.split(" ")
                    ?.firstOrNull()
                    ?.toIntOrNull()
                    ?: 0
                val players = d.combatants.contents
                    .mapNotNull { it.actor }
                    .filterIsInstance<PF2ECharacter>()
                    .filter { it.alliance == "party" }

                prompt<CombatXpData, Unit>(
                    title = t("macros.combatXp.title"),
                    templatePath = "components/forms/form.hbs",
                    templateContext = recordOf(
                        "sections" to arrayOf(
                            SectionContext(
                                legend = t("applications.xp"),
                                formRows = formContext(
                                    NumberInput(
                                        name = "xp",
                                        label = t("macros.xp.amount"),
                                        value = xp,
                                        stacked = false,
                                    ),
                                )
                            ),
                            SectionContext(
                                legend = t("applications.heroPoints"),
                                formRows = formContext(
                                    NumberInput(
                                        name = "heroPoints.all",
                                        label = t("macros.heroPoints.all"),
                                        value = 0,
                                        stacked = false,
                                    ),
                                    *players.map {
                                        NumberInput(
                                            name = "heroPoints.players.${it.uuid}",
                                            label = it.name,
                                            value = 0,
                                            stacked = false,
                                        )
                                    }.toTypedArray()
                                )
                            ),
                        )
                    )
                ) { data ->
                    if (data.xp > 0) {
                        updateXP(players.toTypedArray(), data.xp)
                    }
                    val record = data.unsafeCast<Record<String, Int>>()
                    val allHeroPoints = record["heroPoints.all"] ?: 0
                    val points = players.asSequence()
                        .map {
                            val points = allHeroPoints + (record["heroPoints.players.${it.uuid}"] ?: 0)
                            PointsForPlayer(player = it, points = points)
                        }
                        .filter { it.points > 0 }
                        .toTypedArray()
                    if (points.isNotEmpty()) {
                        updateHeroPoints(points)
                        postChatTemplate(
                            "chatmessages/hero-point-result.hbs", recordOf(
                                "points" to points.map {
                                    recordOf(
                                        "points" to it.points,
                                        "name" to it.player.name,
                                    )
                                }.toTypedArray()
                            )
                        )
                    }
                }
            }
        }
    }
}