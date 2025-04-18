package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.data.events.KingdomEvent
import at.posselt.pfrpg2e.data.events.KingdomEventStage
import at.posselt.pfrpg2e.data.kingdom.KingdomPhase
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import at.posselt.pfrpg2e.data.kingdom.structures.Structure
import at.posselt.pfrpg2e.kingdom.data.getChosenCharter
import at.posselt.pfrpg2e.kingdom.data.getChosenFeats
import at.posselt.pfrpg2e.kingdom.data.getChosenFeatures
import at.posselt.pfrpg2e.kingdom.data.getChosenGovernment
import at.posselt.pfrpg2e.kingdom.data.getChosenHeartland
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.Note
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.GlobalStructureBonuses
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.MergedSettlement
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.UntrainedProficiencyMode
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.createAllModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.All
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Case
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Eq
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Expression
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.ExpressionContext
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Gt
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Gte
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.HasRollOption
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.In
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Lt
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Lte
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Not
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Some
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.When
import at.posselt.pfrpg2e.kingdom.modifiers.penalties.ArmyConditionInfo
import at.posselt.pfrpg2e.kingdom.sheet.calculateAnarchy
import com.foundryvtt.core.AnyObject
import io.github.uuidjs.uuid.v4
import js.array.JsTuple2
import js.objects.Object
import kotlinx.js.JsPlainObject
import kotlinx.serialization.json.Json.Default.parseToJsonElement

sealed external interface RawExpression<T>

@JsPlainObject
external interface RawGte : RawExpression<Boolean> {
    val gte: JsTuple2<Any?, Any?>  // String, null or number
}

@JsPlainObject
external interface RawGt : RawExpression<Boolean> {
    val gt: JsTuple2<Any?, Any?>  // String, null or number
}

@JsPlainObject
external interface RawLte : RawExpression<Boolean> {
    val lte: JsTuple2<Any?, Any?>  // String, null or number
}

@JsPlainObject
external interface RawIn : RawExpression<Boolean> {
    val `in`: JsTuple2<Any?, Any>  // String, null or number in Array<Any?> or String
}

@JsPlainObject
external interface RawLt : RawExpression<Boolean> {
    val lt: JsTuple2<Any?, Any?>
}

@JsPlainObject
external interface RawEq : RawExpression<Boolean> {
    val eq: JsTuple2<Any?, Any?>
}

@JsPlainObject
external interface RawSome : RawExpression<Boolean> {
    val some: Array<RawExpression<Boolean>>
}

@JsPlainObject
external interface RawAll : RawExpression<Boolean> {
    val all: Array<RawExpression<Boolean>>
}

@JsPlainObject
external interface RawNot : RawExpression<Boolean> {
    val not: RawExpression<Boolean>
}

@JsPlainObject
external interface RawHasRollOption : RawExpression<Boolean> {
    val hasRollOption: String
}

@JsPlainObject
external interface RawCases {
    val cases: Array<RawCase>
    val default: Any
}

@JsPlainObject
external interface RawWhen : RawExpression<Any?> {
    val `when`: RawCases
}

@JsPlainObject
external interface RawCase {
    val case: JsTuple2<RawExpression<Boolean>, Any?>
}

@JsPlainObject
external interface RawNote {
    var degree: String?
    var note: String
}


@JsPlainObject
external interface RawModifier {
    var id: String?
    var type: String
    var buttonLabel: String?
    var value: Int
    var valueExpression: RawExpression<Any?>?
    var name: String
    var enabled: Boolean
    var turns: Int?
    var isConsumedAfterRoll: Boolean?
    var rollOptions: Array<String>?
    var applyIf: Array<RawExpression<Boolean>>?
    var fortune: Boolean?
    var rollTwiceKeepLowest: Boolean?
    var rollTwiceKeepHighest: Boolean?
    var upgradeResults: Array<RawUpgradeResult>?
    var downgradeResults: Array<RawDowngradeResult>?
    var notes: Array<RawNote>?
    var requiresTranslation: Boolean?
}

fun RawExpression<Boolean>.parse(): Expression<Boolean> {
    return if (Object.hasOwn(this, "all")) {
        val p = this.unsafeCast<RawAll>()
        All(p.all.map { it.parse() })
    } else if (Object.hasOwn(this, "some")) {
        val p = this.unsafeCast<RawSome>()
        Some(p.some.map { it.parse() })
    } else if (Object.hasOwn(this, "lt")) {
        val p = this.unsafeCast<RawLt>()
        Lt(p.lt.component1(), p.lt.component2())
    } else if (Object.hasOwn(this, "lte")) {
        val p = this.unsafeCast<RawLte>()
        Lte(p.lte.component1(), p.lte.component2())
    } else if (Object.hasOwn(this, "gt")) {
        val p = this.unsafeCast<RawGt>()
        Gt(p.gt.component1(), p.gt.component2())
    } else if (Object.hasOwn(this, "gte")) {
        val p = this.unsafeCast<RawGte>()
        Gte(p.gte.component1(), p.gte.component2())
    } else if (Object.hasOwn(this, "eq")) {
        val p = this.unsafeCast<RawEq>()
        Eq(p.eq.component1(), p.eq.component2())
    } else if (Object.hasOwn(this, "hasRollOption")) {
        val p = this.unsafeCast<RawHasRollOption>()
        HasRollOption(
            option = p.hasRollOption
        )
    } else if (Object.hasOwn(this, "in")) {
        val p = this.unsafeCast<RawIn>()
        val component2 = p.`in`.component2()
        In(
            needle = p.`in`.component1(),
            haystack = if(component2 is Array<*>) {
                component2.toSet()
            } else {
                component2
            },
        )
    } else if (Object.hasOwn(this, "not")) {
        val p = this.unsafeCast<RawNot>()
        Not(expression = p.not.parse())
    } else {
        throw IllegalArgumentException("Unknown Predicate Type " + JSON.stringify(this))
    }
}

fun RawExpression<Any?>.parse(): When {
    return if (Object.hasOwn(this, "when")) {
        val p = this.unsafeCast<RawWhen>()
        val w = p.`when`
        When(
            cases = w.cases.map { Case(it.case.component1().parse(), it.case.component2()) },
            default = w.default
        )
    } else {
        throw IllegalArgumentException("Unknown Predicate Type " + JSON.stringify(this))
    }
}

fun RawNote.parse(): Note =
    Note(degree = degree?.let(DegreeOfSuccess::fromString), note = note)

fun Note.serialize(): RawNote =
    RawNote(degree = degree?.value, note = note)

fun RawModifier.parse(): Modifier =
    Modifier(
        id = id ?: v4(),
        type = ModifierType.fromString(type) ?: ModifierType.UNTYPED,
        value = value,
        name = name,
        valueExpression = valueExpression?.parse(),
        enabled = enabled,
        isConsumedAfterRoll = isConsumedAfterRoll == true,
        turns = turns,
        rollOptions = rollOptions?.toSet() ?: emptySet(),
        applyIf = applyIf?.map { it.parse() } ?: emptyList(),
        rollTwiceKeepHighest = rollTwiceKeepHighest == true,
        rollTwiceKeepLowest = rollTwiceKeepLowest == true,
        fortune = fortune == true,
        upgradeResults = upgradeResults?.mapNotNull { it.parse() }.orEmpty(),
        downgradeResults = downgradeResults?.mapNotNull { it.parse() }.orEmpty(),
        notes = notes?.map { it.parse() }?.toSet().orEmpty(),
        requiresTranslation = requiresTranslation != false,
    )

suspend fun KingdomData.checkModifiers(
    globalBonuses: GlobalStructureBonuses,
    currentSettlement: MergedSettlement?,
    allSettlements: List<Settlement>,
    armyConditions: ArmyConditionInfo?,
): List<Modifier> {
    val chosenFeatures = getChosenFeatures(getExplodedFeatures())
    val chosenFeats = getChosenFeats(chosenFeatures)
    val chosenGovernment = getChosenGovernment()
    return createAllModifiers(
        kingdomLevel = level,
        globalBonuses = globalBonuses,
        currentSettlement = currentSettlement,
        abilityScores = parseAbilityScores(
            getChosenCharter(),
            getChosenHeartland(),
            chosenGovernment,
            chosenFeatures,
        ),
        leaderActors = parseLeaderActors(),
        leaderSkills = settings.leaderSkills.parse(),
        leaderKingdomSkills = settings.leaderKingdomSkills.parse(),
        kingdomSkillRanks = parseSkillRanks(
            chosenFeats = chosenFeats,
            government = chosenGovernment,
            chosenFeatures = chosenFeatures,
        ),
        allSettlements = allSettlements,
        ruins = parseRuins(chosenFeatures, settings.ruinThreshold, government),
        unrest = unrest,
        vacancies = vacancies(
            choices = chosenFeatures,
            bonusFeats = bonusFeats,
            government = government,
        ),
        targetedArmy = armyConditions,
        untrainedProficiencyMode = UntrainedProficiencyMode
            .fromString(settings.proficiencyMode) ?: UntrainedProficiencyMode.NONE,
        enableLeadershipBonuses = settings.enableLeadershipModifiers,
        featModifiers = chosenFeats
            .flatMap { it.feat.modifiers?.map { it.parse() } ?: emptyList() },
        featureModifiers = chosenFeatures
            .flatMap { it.feature.modifiers?.map { it.parse() } ?: emptyList() },
    ) + modifiers.map { it.parse() }
}

fun KingdomData.createExpressionContext(
    phase: KingdomPhase?,
    activity: RawActivity?,
    leader: Leader?,
    usedSkill: KingdomSkill,
    rollOptions: Set<String>,
    structure: Structure?,
    event: KingdomEvent?,
    eventStage: KingdomEventStage?,
    structureIds: Set<String>,
    waterBorders: Int,
): ExpressionContext {
    val chosenFeatures = getChosenFeatures(getExplodedFeatures())
    val chosenFeats = getChosenFeats(chosenFeatures)
    val settlementSceneId = activeSettlement
    return ExpressionContext(
        usedSkill = usedSkill,
        ranks = parseSkillRanks(
            chosenFeatures = chosenFeatures,
            chosenFeats = chosenFeats,
            government = getChosenGovernment()
        ),
        leader = leader,
        activity = activity?.id,
        phase = phase,
        level = level,
        unrest = unrest,
        rollOptions = chosenFeats
            .flatMap { it.feat.rollOptions?.toSet().orEmpty() }
            .toSet() + rollOptions,
        vacancies = vacancies(
            choices = chosenFeatures,
            bonusFeats = bonusFeats,
            government = government,
        ),
        structure = structure,
        anarchyAt = calculateAnarchy(chosenFeats),
        atWar = atWar,
        eventTraits = event?.traits?.map { it.value }?.toSet().orEmpty(),
        eventLeader = eventStage?.leader,
        event = event?.id,
        structures = structureIds,
        waterBorders = waterBorders,
        settlementEvents = if(settlementSceneId == null) {
            emptySet()
        } else {
            getOngoingEvents()
                .filter { it.settlementSceneId == settlementSceneId }
                .map { it.event.id }
                .toSet()
        },
    )
}

@JsModule("./schemas/modifier.json")
private external val modifierSchema: AnyObject

val parsedModifierSchema = parseToJsonElement(JSON.stringify(modifierSchema))