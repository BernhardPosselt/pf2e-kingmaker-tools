package at.posselt.pfrpg2e.macros

import at.posselt.pfrpg2e.app.confirm
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.app.prompt
import at.posselt.pfrpg2e.utils.postChatMessage
import at.posselt.pfrpg2e.utils.postChatTemplate
import at.posselt.pfrpg2e.utils.typeSafeUpdate
import com.foundryvtt.pf2e.actor.PF2ECharacter
import js.array.toTypedArray
import js.objects.Record
import js.objects.recordOf
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.max
import kotlin.math.min

private enum class AwardMode {
    SUBTRACT,
    ADD,
    SET
}

private data class PointsForPlayer(
    val player: PF2ECharacter,
    val points: Int,
    val mode: AwardMode = AwardMode.ADD
)

/**
 * Sets all hero points to 1
 */
suspend fun resetHeroPointsMacro(actors: Array<PF2ECharacter>) {
    if (confirm("Reset Hero Points to 1?")) {
        val points = actors.map { PointsForPlayer(player = it, 1, AwardMode.SET) }.toTypedArray()
        updateHeroPoints(points)
        postChatMessage("Reset hero point values to 1")
    }
}

private suspend fun updateHeroPoints(points: Array<PointsForPlayer>): Unit = coroutineScope {
    points.map {
        val actor = it.player
        val actualPoints = when (it.mode) {
            AwardMode.SUBTRACT -> max(0, actor.system.resources.heroPoints.value - it.points)
            AwardMode.ADD -> actor.system.resources.heroPoints.value + it.points
            AwardMode.SET -> it.points
        }
        async {
            it.player.typeSafeUpdate {
                system.resources.heroPoints.value = min(3, actualPoints)
            }
        }
    }.awaitAll()
}


suspend fun awardHeroPointsMacro(players: Array<PF2ECharacter>) {
    prompt<Record<String, Int>, Unit>(
        templatePath = "components/forms/form.hbs",
        templateContext = recordOf(
            "formRows" to formContext(
                NumberInput(label = "All", name = "award-all"),
                *players.map { NumberInput(label = it.name, name = it.uuid) }.toTypedArray()
            )
        ),
        title = "Award Hero Points",
    ) { data ->
        val points = players.asSequence()
            .map {
                val points = (data["award-all"] ?: 0) + (data[it.uuid] ?: 0)
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