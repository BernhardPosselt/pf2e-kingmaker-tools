package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.Config
import at.posselt.pfrpg2e.actor.getEffectNames
import at.posselt.pfrpg2e.divideRoundingUp
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.utils.awaitAll
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.buildUpdate
import at.posselt.pfrpg2e.utils.fromUuidTypeSafe
import at.posselt.pfrpg2e.utils.fromUuidsTypeSafe
import at.posselt.pfrpg2e.utils.postChatTemplate
import at.posselt.pfrpg2e.utils.roll
import at.posselt.pfrpg2e.utils.typeSafeUpdate
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.documents.ChatMessage
import com.foundryvtt.core.documents.GetSpeakerOptions
import com.foundryvtt.pf2e.actor.PF2EActor
import com.foundryvtt.pf2e.actor.PF2ECharacter
import com.foundryvtt.pf2e.actor.PF2EParty
import com.foundryvtt.pf2e.item.PF2EConsumable
import com.foundryvtt.pf2e.item.PF2EConsumableData
import com.foundryvtt.pf2e.item.PF2EEffect
import com.foundryvtt.pf2e.rolls.DamageRoll
import js.array.push
import js.objects.recordOf
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.js.JsPlainObject
import kotlin.math.max
import kotlin.math.min

suspend fun PF2EActor.addConsumableToInventory(uuid: String, quantity: Int) {
    if (quantity > 0) {
        fromUuidTypeSafe<PF2EConsumable>(uuid)?.let { item ->
            val obj = item.toObject().unsafeCast<AnyObject>()
            val system = obj["system"].unsafeCast<PF2EConsumableData>()
            if (system.uses.max > 1) {
                val max = system.uses.max
                system.quantity = quantity.divideRoundingUp(max)
                system.uses.value = quantity % max
            } else {
                system.quantity = quantity
            }
            addToInventory(obj, undefined, false)
        }
    }
}

/**
 * Given a list of meal effects, get those that are have been applied to a player
 */
suspend fun PF2EActor.getAppliedMealEffects(mealEffects: List<MealEffect>): List<MealEffect> {
    val effectNames = getEffectNames()
    return mealEffects
        .map { buildPromise { fromUuidTypeSafe<PF2EEffect>(it.uuid)?.name to it } }
        .awaitAll()
        .filter { it.first != null && it.first in effectNames }
        .map { it.second }
}

fun mealEffectsChangingRestDuration(recipes: List<RecipeData>): List<MealEffect> =
    mealEffectsHaving(recipes) {
        it.changeRestDurationSeconds != null
    }

fun mealEffectsDoublingHealing(recipes: List<RecipeData>): List<MealEffect> =
    mealEffectsHaving(recipes) {
        it.doublesHealing == true
    }

fun mealEffectsHalvingHealing(recipes: List<RecipeData>): List<MealEffect> =
    mealEffectsHaving(recipes) {
        it.halvesHealing == true
    }


private fun mealEffectsHaving(recipes: List<RecipeData>, predicate: (MealEffect) -> Boolean): List<MealEffect> =
    recipes.asSequence()
        .flatMap {
            sequenceOf(
                it.criticalFailure.effects,
                it.success.effects,
                it.criticalSuccess.effects,
                it.favoriteMeal?.effects,
            ).filterNotNull()
                .flatMap(Array<MealEffect>::asSequence)
                .filter(predicate)
        }
        .toList()


private fun getAllOutcomeEffects(recipe: RecipeData): List<MealEffect> =
    listOfNotNull(
        recipe.criticalFailure.effects?.toList(),
        recipe.success.effects?.toList(),
        recipe.criticalSuccess.effects?.toList(),
        recipe.favoriteMeal?.effects?.toList(),
    ).flatten()

private suspend fun getMealEffectItems(
    recipe: RecipeData,
    onlyRemoveAfterRest: Boolean = false,
): List<PF2EEffect> = coroutineScope {
    getAllOutcomeEffects(recipe)
        .filter {
            if (onlyRemoveAfterRest) {
                it.removeAfterRest == true
            } else {
                true
            }
        }
        .map { it.uuid }
        .map { async { fromUuidTypeSafe<PF2EEffect>(it) } }
        .awaitAll()
        .filterNotNull()
}

suspend fun getMealEffectItems(
    recipes: List<RecipeData>,
    onlyRemoveAfterRest: Boolean = false,
): List<PF2EEffect> = coroutineScope {
    recipes
        .map { async { getMealEffectItems(it, onlyRemoveAfterRest) } }
        .awaitAll()
        .flatten()
}

suspend fun PF2EActor.removeConsumablesByName(names: Set<String>) {
    val idsToRemove = itemTypes.consumable
        .filter { it.name in names }
        .mapNotNull { it.id }
        .toTypedArray()
    deleteEmbeddedDocuments<PF2EEffect>("Item", idsToRemove).await()
}

suspend fun PF2EActor.removeEffectsByName(names: Set<String>) {
    val idsToRemove = itemTypes.effect
        .filter { it.name in names }
        .mapNotNull { it.id }
        .toTypedArray()
    deleteEmbeddedDocuments<PF2EEffect>("Item", idsToRemove).await()
}

suspend fun PF2EActor.clearEffects(effects: List<PF2EEffect>) {
    val effectNames = effects
        .mapNotNull { it.name }
        .toSet()
    removeEffectsByName(effectNames)
}

suspend fun removeMealEffects(
    recipes: List<RecipeData>,
    actors: List<PF2EActor>,
    onlyRemoveAfterRest: Boolean = false,
) = coroutineScope {
    val effects = getMealEffectItems(recipes, onlyRemoveAfterRest)
    actors
        .map { async { it.clearEffects(effects) } }
        .awaitAll()
}

suspend fun removeProvisions(
    actors: List<PF2EActor>,
) = coroutineScope {
    fromUuidTypeSafe<PF2EConsumable>(Config.items.provisionsUuid)
        ?.name
        ?.let { provisionsName ->
            actors
                .map { async { it.removeConsumablesByName(setOf(provisionsName)) } }
                .awaitAll()
        }
}

enum class MealEffectTrigger {
    CONSUMPTION,
    REST
}

private fun healModeApplies(healMode: String?, trigger: MealEffectTrigger) =
    when (healMode?.let { fromCamelCase<HealMode>(it) }) {
        HealMode.AFTER_CONSUMPTION -> trigger == MealEffectTrigger.CONSUMPTION
        HealMode.AFTER_REST -> trigger == MealEffectTrigger.REST
        HealMode.AFTER_CONSUMPTION_AND_REST -> true
        null -> trigger == MealEffectTrigger.CONSUMPTION
    }

data class MealNameAndEffect(
    val effectName: String,
    val effect: MealEffect,
)

suspend fun PF2ECharacter.applyConsumptionMealEffects(outcome: CookingOutcome) {
    val uuids = if (outcome.chooseRandomly == true) {
        listOfNotNull(outcome.effects?.randomOrNull()?.uuid)
    } else {
        outcome.effects?.map(MealEffect::uuid) ?: emptyList()
    }
    val effects = fromUuidsTypeSafe<PF2EEffect>(uuids.toTypedArray())
    val items = effects.map { it.toObject() }.toTypedArray()
    createEmbeddedDocuments<PF2EEffect>("Item", items).await()
    val effectsByUuid = effects.associateBy { it.uuid }
    val applicableHealEffects = outcome.effects
        ?.filter { healModeApplies(it.healMode, MealEffectTrigger.CONSUMPTION) }
        ?.mapNotNull { effectsByUuid[it.uuid]?.name?.let { name -> MealNameAndEffect(name, it) } }
        ?: emptyList()
    applyMealHealEffects(applicableHealEffects)
}

suspend fun applyConsumptionMealEffects(
    actors: List<PF2EActor>,
    outcome: CookingOutcome
) = coroutineScope {
    actors
        .filterIsInstance<PF2ECharacter>()
        .map { async { it.applyConsumptionMealEffects(outcome) } }
        .awaitAll()
}

suspend fun applyRestHealEffects(
    actors: List<PF2EActor>,
    recipes: List<RecipeData>,
    mealEffectItems: List<PF2EEffect>,
) = coroutineScope {
    actors
        .filterIsInstance<PF2ECharacter>()
        .map { async { it.applyRestHealEffects(recipes, mealEffectItems) } }
        .awaitAll()
}

suspend fun PF2ECharacter.applyRestHealEffects(
    recipes: List<RecipeData>,
    mealEffectItems: List<PF2EEffect>,
) {
    val effectsByUuid = mealEffectItems.associateBy { it.uuid }
    val appliedMealNames = itemTypes.effect.map { it.name }.toSet()
    val appliedEffectItemUuids = mealEffectItems
        .filter { it.name in appliedMealNames }
        .map { it.uuid }
        .toSet()
    val applicableHealEffects = recipes.asSequence()
        .flatMap { getAllOutcomeEffects(it) }
        .filter { it.uuid in appliedEffectItemUuids }
        .filter { healModeApplies(it.healMode, MealEffectTrigger.REST) }
        .mapNotNull { effectsByUuid[it.uuid]?.name?.let { name -> MealNameAndEffect(name, it) } }
        .toList()
    applyMealHealEffects(applicableHealEffects)
}

private sealed interface HealingValue

private data class Healing(
    val name: String,
    val formula: String,
) : HealingValue

private data class Damage(
    val name: String,
    val formula: String,
) : HealingValue

private data class Conditions(
    val name: String,
    val reduceConditions: ReduceConditions,
) : HealingValue

private fun parseMealNameAndEffects(effect: MealNameAndEffect): List<HealingValue> {
    val healFormula = effect.effect.healFormula
    val damageFormula = effect.effect.damageFormula
    val reduceConditions = effect.effect.reduceConditions
    val name = effect.effectName
    return listOfNotNull(
        healFormula?.let { Healing(name, it) },
        damageFormula?.let { Damage(name, it) },
        reduceConditions
            ?.takeIf { it.reducesAnyCondition() }
            ?.let { Conditions(name, it) }
    )
}


private suspend fun PF2ECharacter.applyMealHealEffects(
    mealEffectItems: List<MealNameAndEffect>,
) {
    val parsedEffects = mealEffectItems.flatMap(::parseMealNameAndEffects)
    val healingFormulas = parsedEffects.filterIsInstance<Healing>()
    val damageFormulas = parsedEffects.filterIsInstance<Damage>()
    val reduceConditions = parsedEffects.filterIsInstance<Conditions>()
    val totalHealing = healingFormulas.sumOf {
        roll(
            it.formula,
            flavor = "Automatically applying healing from ${it.name}",
            speaker = this,
        )
    }
    val hp = min(system.attributes.hp.max, system.attributes.hp.value + totalHealing)
    typeSafeUpdate { system.attributes.hp.value = hp }
    damageFormulas.forEach {
        DamageRoll(it.formula).toMessage(
            recordOf(
                "flavor" to "Rolling damage from ${it.name}, please apply manually",
                "speaker" to ChatMessage.getSpeaker(
                    GetSpeakerOptions(
                        actor = this
                    )
                )
            )
        )
    }
    reduceConditions.forEach { condition ->
        postChatTemplate(
            templatePath = "chatmessages/reduce-conditions.hbs",
            templateContext = recordOf(
                "effect" to condition.name,
                "heading" to if (condition.reduceConditions.mode == "random") {
                    "Manually lower one of the following conditions (or choose one at random if more than one applies)"
                } else {
                    "Manually lower all of the following conditions"
                },
                "values" to listOfNotNull(
                    condition.reduceConditions.clumsy?.takeIf { it > 0 }?.let { "Clumsy: $it" },
                    condition.reduceConditions.enfeebled?.takeIf { it > 0 }?.let { "Enfeebled: $it" },
                    condition.reduceConditions.drained?.takeIf { it > 0 }?.let { "Drained: $it" },
                    condition.reduceConditions.stupefied?.takeIf { it > 0 }?.let { "Stupefied: $it" },
                ).toTypedArray()
            ),
            speaker = this
        )
    }
}


@JsPlainObject
external interface FoodCost {
    val rations: String
    val basicIngredients: String
    val specialIngredients: String
    val rationImage: String?
    val basicImage: String?
    val specialImage: String?
    val totalRations: Int?
    val totalBasicIngredients: Int?
    val totalSpecialIngredients: Int?
    val missingRations: Boolean
    val missingBasic: Boolean
    val missingSpecial: Boolean
}

fun buildFoodCost(
    amount: FoodAmount,
    totalAmount: FoodAmount? = null,
    items: FoodItems,
    capAt: Int? = 99,
) = FoodCost(
    rations = capAt(amount.rations, capAt),
    basicIngredients = capAt(amount.basicIngredients, capAt),
    specialIngredients = capAt(amount.specialIngredients, capAt),
    totalRations = totalAmount?.rations,
    totalBasicIngredients = totalAmount?.basicIngredients,
    totalSpecialIngredients = totalAmount?.specialIngredients,
    rationImage = items.ration.img,
    basicImage = items.basic.img,
    specialImage = items.special.img,
    missingBasic = totalAmount?.let { it.basicIngredients < amount.basicIngredients } == true,
    missingSpecial = totalAmount?.let { it.specialIngredients < amount.specialIngredients } == true,
    missingRations = totalAmount?.let { it.rations < amount.rations } == true,
)

private fun capAt(number: Int, cap: Int? = null): String {
    return if (cap == null || number <= cap) {
        number.toString()
    } else {
        "$cap+"
    }
}

data class FoodAmount(
    val basicIngredients: Int = 0,
    val specialIngredients: Int = 0,
    val rations: Int = 0,
) {
    operator fun plus(other: FoodAmount): FoodAmount =
        FoodAmount(
            basicIngredients = basicIngredients + other.basicIngredients,
            specialIngredients = specialIngredients + other.specialIngredients,
            rations = rations + other.rations,
        )

    operator fun times(amount: Int) =
        FoodAmount(
            basicIngredients = basicIngredients * amount,
            specialIngredients = specialIngredients * amount,
            rations = rations * amount,
        )

    fun isEmpty() = basicIngredients == 0 && specialIngredients == 0 && rations == 0
}

fun List<FoodAmount>.sum() =
    fold(FoodAmount()) { a, b -> a + b }

suspend fun PF2EActor.addFoodToInventory(foodAmount: FoodAmount) = coroutineScope {
    listOf(
        async { addConsumableToInventory(Config.items.specialIngredientUuid, foodAmount.specialIngredients) },
        async { addConsumableToInventory(Config.items.basicIngredientUuid, foodAmount.basicIngredients) },
        async { addConsumableToInventory(Config.items.rationUuid, foodAmount.rations) },
    ).awaitAll()
}


suspend fun PF2EActor.removeConsumableFromInventory(name: String, quantity: Int): Int {
    val updates = arrayOf<AnyObject>()
    val deleteIds = arrayOf<String>()
    var leftOver = quantity
    consumablesByName(name).forEach { consumable ->
        val id = consumable.id
        if (id != null && leftOver > 0) {
            val totalQuantity = consumable.totalQuantity()
            val consume = min(leftOver, totalQuantity)
            leftOver -= consume
            if (totalQuantity <= consume) {
                deleteIds.push(id)
            } else {
                val chargeUpdates = calculateCharges(
                    removeQuantity = consume,
                    itemQuantity = consumable.system.quantity,
                    itemUses = consumable.system.uses.value,
                    itemMaxUses = consumable.system.uses.max,
                )
                updates.push(consumable.buildUpdate<PF2EConsumable> {
                    _id = id
                    system.quantity = chargeUpdates.quantity
                    system.uses.value = chargeUpdates.charges
                })
            }
        }
    }
    updateEmbeddedDocuments<PF2EConsumable>("Item", updates).await()
    deleteEmbeddedDocuments<PF2EConsumable>("Item", deleteIds).await()
    return leftOver
}

data class ChargeUpdate(val quantity: Int, val charges: Int)

fun calculateCharges(
    removeQuantity: Int,
    itemQuantity: Int,
    itemUses: Int,
    itemMaxUses: Int
): ChargeUpdate {
    val totalQuantity = calculateMaxQuantity(uses = itemUses, maxUses = itemMaxUses, quantity = itemQuantity)
    val leftOver = max(0, totalQuantity - removeQuantity)
    val charges = leftOver % itemMaxUses
    val quantity = leftOver.divideRoundingUp(itemMaxUses)
    return ChargeUpdate(
        charges = max(0, if (charges == 0 && quantity != 0) itemMaxUses else charges),
        quantity = max(0, quantity)
    )
}

suspend fun reduceFoodBy(
    actors: List<PF2EActor>,
    foodAmount: FoodAmount,
    foodItems: FoodItems,
    postMessage: Boolean = true,
): FoodAmount {
    var leftOver = foodAmount
    actors.forEach { actor ->
        leftOver = actor.reduceFoodBy(foodAmount = leftOver, foodItems = foodItems)
    }
    if (postMessage) {
        postChatTemplate(
            templatePath = "chatmessages/consume-food.hbs",
            templateContext = recordOf(
                "missing" to !leftOver.isEmpty(),
                "rations" to foodAmount.rations,
                "specialIngredients" to foodAmount.specialIngredients,
                "basicIngredients" to foodAmount.basicIngredients,
                "missingRations" to leftOver.rations,
                "missingSpecialIngredients" to leftOver.specialIngredients,
                "missingBasicIngredients" to leftOver.basicIngredients,
            ),
        )
    }
    return leftOver
}

suspend fun PF2EActor.reduceFoodBy(foodAmount: FoodAmount, foodItems: FoodItems): FoodAmount = coroutineScope {
    val leftOverSpecial =
        async { removeConsumableFromInventory(foodItems.special.name!!, foodAmount.specialIngredients) }
    val leftOverBasic = async { removeConsumableFromInventory(foodItems.basic.name!!, foodAmount.basicIngredients) }
    val leftOverProvisions = async { removeConsumableFromInventory(foodItems.provisions.name!!, foodAmount.rations) }
    val leftOverRation = async { removeConsumableFromInventory(foodItems.ration.name!!, leftOverProvisions.await()) }
    FoodAmount(
        basicIngredients = leftOverBasic.await(),
        specialIngredients = leftOverSpecial.await(),
        rations = leftOverRation.await(),
    )
}

private fun calculateMaxQuantity(uses: Int, quantity: Int, maxUses: Int) =
    uses + max(0, quantity - 1) * maxUses

private fun PF2EConsumable.totalQuantity() =
    calculateMaxQuantity(uses = system.uses.value, quantity = system.quantity, maxUses = system.uses.max)

private fun List<PF2EConsumable>.sumQuantity() =
    fold(0) { a, b -> a + b.totalQuantity() }

private fun PF2EActor.consumableQuantityByName(name: String): Int =
    consumablesByName(name).sumQuantity()

private fun PF2EActor.consumablesByName(name: String) =
    itemTypes.consumable.filter { it.name == name }


data class FoodItems(
    val basic: PF2EConsumable,
    val special: PF2EConsumable,
    val ration: PF2EConsumable,
    val provisions: PF2EConsumable,
)

suspend fun getCompendiumFoodItems() = coroutineScope {
    val b = async { fromUuidTypeSafe<PF2EConsumable>(Config.items.basicIngredientUuid) }
    val s = async { fromUuidTypeSafe<PF2EConsumable>(Config.items.specialIngredientUuid) }
    val r = async { fromUuidTypeSafe<PF2EConsumable>(Config.items.rationUuid) }
    val p = async { fromUuidTypeSafe<PF2EConsumable>(Config.items.provisionsUuid) }
    val basic = b.await()
    val special = s.await()
    val ration = r.await()
    val provisions = p.await()
    checkNotNull(basic) { "Basic Ingredient UUID changed, something is very wrong" }
    checkNotNull(special) { "Basic Ingredient UUID changed, something is very wrong" }
    checkNotNull(ration) { "Basic Ingredient UUID changed, something is very wrong" }
    checkNotNull(provisions) { "Basic Ingredient UUID changed, something is very wrong" }
    FoodItems(basic = basic, special = special, ration = ration, provisions = provisions)
}

fun PF2EActor.getTotalCarriedFood(
    foodItems: FoodItems,
): FoodAmount {
    return FoodAmount(
        basicIngredients = consumableQuantityByName(foodItems.basic.name!!),
        specialIngredients = consumableQuantityByName(foodItems.special.name!!),
        rations = consumableQuantityByName(foodItems.ration.name!!) +
                consumableQuantityByName(foodItems.provisions.name!!),
    )
}

suspend fun CampingData.getTotalCarriedFood(
    party: PF2EParty?,
    foodItems: FoodItems,
): FoodAmount = coroutineScope {
    val actors = getActorsInCamp() + (party?.let { listOf(it) } ?: emptyList())
    actors.map {
        it.getTotalCarriedFood(foodItems = foodItems)
    }.sum()
}
