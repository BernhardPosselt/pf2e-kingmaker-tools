package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.data.kingdom.KingdomPhase
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.GlobalStructureBonuses
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.MergedSettlement
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.UntrainedProficiencyMode
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.createAllModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.createAssuranceModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.AndPredicate
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.EqPredicate
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.ExpressionContext
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.GtPredicate
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.GtePredicate
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.HasFlagPredicate
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.HasRollOptionPredicate
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.InPredicate
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.LtPredicate
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.LtePredicate
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.NotPredicate
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.OrPredicate
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Predicate
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.When
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.WhenBranch
import at.posselt.pfrpg2e.kingdom.modifiers.penalties.ArmyConditionInfo
import io.github.uuidjs.uuid.v4
import js.array.JsTuple2
import js.objects.Object
import kotlinx.js.JsPlainObject

sealed external interface RawPredicate

@JsPlainObject
external interface RawGtePredicate : RawPredicate {
    val gte: JsTuple2<String, String>
}

@JsPlainObject
external interface RawGtPredicate : RawPredicate {
    val gt: JsTuple2<String, String>
}

@JsPlainObject
external interface RawLtePredicate : RawPredicate {
    val lte: JsTuple2<String, String>
}

@JsPlainObject
external interface RawInPredicate : RawPredicate {
    val `in`: JsTuple2<String, Array<String>>
}

@JsPlainObject
external interface RawLtPredicate : RawPredicate {
    val lt: JsTuple2<String, String>
}

@JsPlainObject
external interface RawEqPredicate : RawPredicate {
    val eq: JsTuple2<String, String>
}

@JsPlainObject
external interface RawOrPredicate : RawPredicate {
    val or: JsTuple2<RawPredicate, RawPredicate>
}

@JsPlainObject
external interface RawAndPredicate : RawPredicate {
    val and: JsTuple2<RawPredicate, RawPredicate>
}

@JsPlainObject
external interface RawNotPredicate : RawPredicate {
    val not: RawPredicate
}

@JsPlainObject
external interface RawHasFlagPredicate : RawPredicate {
    val hasFlag: String
}

@JsPlainObject
external interface RawHasRollOptionPredicate : RawPredicate {
    val hasRollOption: String
}

@JsPlainObject
external interface RawWhenPredicate {
    val `when`: JsTuple2<RawPredicate, String>
}

@JsPlainObject
external interface RawModifier {
    var type: String
    var value: Int
    var predicatedValue: Array<RawWhenPredicate>?
    var name: String
    var enabled: Boolean
    var turns: Int?
    var isConsumedAfterRoll: Boolean?
    var rollOptions: Array<String>?
    var predicates: Array<RawPredicate>?
}


fun RawPredicate.parse(): Predicate {
    return if (Object.hasOwn(this, "and")) {
        val p = this.unsafeCast<RawAndPredicate>()
        AndPredicate(
            left = p.and.component1().parse(),
            right = p.and.component2().parse(),
        )
    } else if (Object.hasOwn(this, "or")) {
        val p = this.unsafeCast<RawOrPredicate>()
        OrPredicate(
            left = p.or.component1().parse(),
            right = p.or.component2().parse(),
        )
    } else if (Object.hasOwn(this, "lt")) {
        val p = this.unsafeCast<RawLtPredicate>()
        LtPredicate(p.lt.component1(), p.lt.component2())
    } else if (Object.hasOwn(this, "lte")) {
        val p = this.unsafeCast<RawLtePredicate>()
        LtePredicate(p.lte.component1(), p.lte.component2())
    } else if (Object.hasOwn(this, "gt")) {
        val p = this.unsafeCast<RawGtPredicate>()
        GtPredicate(p.gt.component1(), p.gt.component2())
    } else if (Object.hasOwn(this, "gte")) {
        val p = this.unsafeCast<RawGtePredicate>()
        GtePredicate(p.gte.component1(), p.gte.component2())
    } else if (Object.hasOwn(this, "eq")) {
        val p = this.unsafeCast<RawEqPredicate>()
        EqPredicate(p.eq.component1(), p.eq.component2())
    } else if (Object.hasOwn(this, "hasFlag")) {
        val p = this.unsafeCast<RawHasFlagPredicate>()
        HasFlagPredicate(
            flag = p.hasFlag
        )
    } else if (Object.hasOwn(this, "hasRollOption")) {
        val p = this.unsafeCast<RawHasRollOptionPredicate>()
        HasRollOptionPredicate(
            option = p.hasRollOption
        )
    } else if (Object.hasOwn(this, "in")) {
        val p = this.unsafeCast<RawInPredicate>()
        InPredicate(
            needle = p.`in`.component1(),
            haystack = p.`in`.component2().toSet(),
        )
    } else if (Object.hasOwn(this, "not")) {
        val p = this.unsafeCast<RawNotPredicate>()
        NotPredicate(predicate = p.not.parse())
    } else {
        throw IllegalArgumentException("Unknown Predicate Type " + JSON.stringify(this))
    }
}

fun Array<RawWhenPredicate>.parse(): When =
    When(map {
        WhenBranch(it.`when`.component1().parse(), it.`when`.component2())
    }, "")

fun RawModifier.parse(id: String): Modifier =
    Modifier(
        id = id,
        type = ModifierType.fromString(type) ?: ModifierType.UNTYPED,
        value = value,
        name = name,
        predicatedValue = predicatedValue?.parse(),
        enabled = enabled,
        isConsumedAfterRoll = isConsumedAfterRoll == true,
        turns = turns,
        rollOptions = rollOptions?.toSet() ?: emptySet(),
        predicates = predicates?.map { it.parse() } ?: emptyList()
    )


fun KingdomData.assuranceModifiers() = createAssuranceModifiers(
    kingdomSkillRanks = parseSkillRanks(),
    kingdomLevel = level,
    untrainedProficiencyMode = UntrainedProficiencyMode
        .fromString(settings.proficiencyMode) ?: UntrainedProficiencyMode.NONE,
)

suspend fun KingdomData.checkModifiers(
    globalBonuses: GlobalStructureBonuses,
    currentSettlement: MergedSettlement?,
    allSettlements: List<Settlement>,
    targetedArmy: ArmyConditionInfo?,
): List<Modifier> = createAllModifiers(
    kingdomLevel = level,
    globalBonuses = globalBonuses,
    currentSettlement = currentSettlement,
    abilityScores = parseAbilityScores(),
    leaderActors = parseLeaderActors(),
    leaderSkills = settings.leaderSkills.parse(),
    leaderKingdomSkills = settings.leaderKingdomSkills.parse(),
    kingdomSkillRanks = parseSkillRanks(),
    allSettlements = allSettlements,
    ruins = ruin.parse(),
    unrest = unrest,
    vacancies = vacancies(),
    targetedArmy = targetedArmy,
    untrainedProficiencyMode = UntrainedProficiencyMode
        .fromString(settings.proficiencyMode) ?: UntrainedProficiencyMode.NONE,
    enableLeadershipBonuses = settings.enableLeadershipModifiers,
    featModifiers = getChosenFeats()
        .flatMap { it.feat.modifiers?.map { it.parse(v4()) } ?: emptyList() },
    featureModifiers = getEnabledFeatures()
        .flatMap { it.modifiers?.map { it.parse(v4()) } ?: emptyList() },
)

fun KingdomData.createExpressionContext(
    phase: KingdomPhase?,
    activity: KingdomActivity?,
    leader: Leader,
    usedSkill: KingdomSkill,
    rollOptions: Set<String>,
) = ExpressionContext(
    usedSkill = usedSkill,
    ranks = parseSkillRanks(),
    leader = leader,
    activity = activity?.id,
    phase = phase,
    level = level,
    unrest = unrest,
    flags = getAllFeats()
        .flatMap { it.flags?.toSet() ?: emptySet() }
        .toSet(),
    rollOptions = rollOptions,
    isVacant = vacancies().resolveVacancy(leader)
)