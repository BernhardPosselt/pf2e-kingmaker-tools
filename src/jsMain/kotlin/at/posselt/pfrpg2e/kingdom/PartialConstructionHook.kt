package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.kingdom.structures.StructureActor
import at.posselt.pfrpg2e.kingdom.structures.parseStructure
import at.posselt.pfrpg2e.takeIfInstance
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.typeSafeUpdate
import com.foundryvtt.core.Game
import com.foundryvtt.core.documents.onCreateToken
import com.foundryvtt.core.helpers.TypedHooks
import kotlin.math.min

fun registerPartialConstructionHooks(game: Game) {
    TypedHooks.onCreateToken { document, options, userId ->
        if (userId == game.user.id) {
            document.actor
                ?.takeIfInstance<StructureActor>()
                ?.let { actor ->
                    val structure = actor.parseStructure()
                    if (structure != null) {
                        val rpCost = structure.construction.rp
                        val sceneId = document.scene.id
                        val kingdomActor = game.getKingdomActors()
                            .find { actor -> actor.getKingdom()?.settlements?.any { it.sceneId == sceneId } == true }
                        val kingdom = kingdomActor?.getKingdom()
                        val realmData = kingdom?.let { game.getRealmData(kingdomActor, it) }
                        val partialStructureConstruction = kingdom?.settings?.partialStructureConstruction == true
                        if (partialStructureConstruction && rpCost > 0 && realmData != null) {
                            val hpValue = min(actor.hitPoints.max, realmData.sizeInfo.maximumStructureRpPerTurn)
                            buildPromise {
                                actor.typeSafeUpdate { system.attributes.hp.value = hpValue }
                            }
                        }
                    }
                }
        }
    }
}