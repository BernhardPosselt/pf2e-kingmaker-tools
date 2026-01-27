package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.utils.fromUuidOfTypes
import at.posselt.pfrpg2e.utils.fromUuidsOfTypes
import at.posselt.pfrpg2e.utils.toMutableRecord
import com.foundryvtt.pf2e.actor.PF2EActor
import com.foundryvtt.pf2e.actor.PF2ECharacter
import com.foundryvtt.pf2e.actor.PF2ECreature
import com.foundryvtt.pf2e.actor.PF2ELoot
import com.foundryvtt.pf2e.actor.PF2ENpc
import com.foundryvtt.pf2e.actor.PF2EVehicle
import com.foundryvtt.pf2e.item.PF2EAction
import com.foundryvtt.pf2e.item.PF2EAffliction
import com.foundryvtt.pf2e.item.PF2EArmor
import com.foundryvtt.pf2e.item.PF2EBackpack
import com.foundryvtt.pf2e.item.PF2ECampaignFeature
import com.foundryvtt.pf2e.item.PF2ECondition
import com.foundryvtt.pf2e.item.PF2EConsumable
import com.foundryvtt.pf2e.item.PF2EEffect
import com.foundryvtt.pf2e.item.PF2EEquipment
import com.foundryvtt.pf2e.item.PF2EShield
import com.foundryvtt.pf2e.item.PF2ETreasure
import com.foundryvtt.pf2e.item.PF2EWeapon
import js.objects.recordOf
import kotlinx.coroutines.await
import kotlin.reflect.KClass

private val allowedCampingActorTypes = arrayOf(
    PF2ENpc::class,
    PF2ECharacter::class,
    PF2EVehicle::class,
    PF2ELoot::class,
)
private val allowedCampingActivityActorTypes: Array<KClass<out PF2EActor>> = arrayOf(
    PF2ECharacter::class,
)
val allowedDnDItems = arrayOf(
    PF2EAction::class,
    PF2ECampaignFeature::class,
    PF2ECondition::class,
    PF2EConsumable::class,
    PF2EEffect::class,
    PF2EEquipment::class,
    PF2EAffliction::class,
    PF2EWeapon::class,
    PF2EArmor::class,
    PF2EShield::class,
    PF2ETreasure::class,
    PF2EBackpack::class,
)

suspend fun getCampingActivityCreatureByUuid(actorUuid: String) =
    fromUuidOfTypes(actorUuid, *allowedCampingActivityActorTypes).unsafeCast<PF2ECreature?>()

suspend fun getCampingActivityActorByUuid(actorUuid: String) =
    getCampingActivityCreatureByUuid(actorUuid).unsafeCast<PF2EActor?>()

suspend fun getCampingActorByUuid(uuid: String) =
    fromUuidOfTypes(uuid, *allowedCampingActorTypes).unsafeCast<PF2EActor?>()

suspend fun getCampingActorsByUuid(uuids: Array<String>) =
    fromUuidsOfTypes(uuids, *allowedCampingActorTypes).unsafeCast<Array<PF2EActor>>()

/**
 * Old methods
 */
suspend fun CampingActor.deleteCampingActivityOld(id: String) {
    getCamping()?.let { camping ->
        camping.campingActivities = camping.campingActivities
            .filter { it.activityId != id }
            .toTypedArray()
        setCamping(camping)
    }
}

suspend fun CampingActor.deleteCampingActivitiesOld(ids: Set<String>) {
    getCamping()?.let { camping ->
        camping.campingActivities = camping.campingActivities
            .filter { it.activityId !in ids }
            .toTypedArray()
        setCamping(camping)
    }
}

suspend fun CampingActor.deleteCampingActorOld(actorUuid: String) {
    getCamping()?.let {
        it.actorUuids = it.actorUuids.filter { id -> id != actorUuid }.toTypedArray()
        it.campingActivities = it.campingActivities.filter { a -> a.actorUuid != actorUuid }.toTypedArray()
        it.cooking.actorMeals = it.cooking.actorMeals.filter { m -> m.actorUuid != actorUuid }.toTypedArray()
        setCamping(it)
    }
}

/**
 * New Replacements
 */
suspend fun CampingActor.deleteCampingActivity(id: String) {
    update(recordOf("$campingActivitiesPath.-=$id" to null)).await()
}

suspend fun CampingActor.deleteCampingActivities(ids: Set<String>) {
    update(ids.map { "$campingActivitiesPath.-=$it" to null }
        .toMutableRecord()).await()
}

suspend fun CampingActor.deleteCampingActor(actorUuid: String) {
    getCamping()?.let { camping ->
        val activitiesToRemove = camping.campingActivities
            .filter { it.actorUuid == actorUuid }
            .map { it.activityId }
            .toSet()
            .map { "$campingActivitiesPath.-=$it" to null }
        val updatePairs = listOf(
            "$campingPath.actorUuids" to camping.actorUuids.filter { id -> id != uuid }.toTypedArray(),
            "$campingPath.cooking.actorMeals" to camping.cooking.actorMeals.filter { m -> m.actorUuid != uuid }
                .toTypedArray(),
        ) + activitiesToRemove
        update(updatePairs.toMutableRecord()).await()
    }
}