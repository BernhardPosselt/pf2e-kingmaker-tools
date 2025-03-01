package at.posselt.pfrpg2e.kingdom.modifiers.constructors

import at.posselt.pfrpg2e.data.kingdom.KingdomAbility
import at.posselt.pfrpg2e.data.kingdom.KingdomPhase
import at.posselt.pfrpg2e.data.kingdom.Leader
import at.posselt.pfrpg2e.data.kingdom.Vacancies
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.EqPredicate

fun createVacancyModifiers(
    vacancies: Vacancies,
): List<Modifier> {
    val rulerVacancyPenalty = if (vacancies.ruler) -1 else 0
    return Leader.entries.map {
        val (value, predicate) = when (it) {
            Leader.RULER -> 0 to null
            Leader.COUNSELOR -> -1 to EqPredicate("@ability", KingdomAbility.CULTURE.value)
            Leader.EMISSARY -> -1 to EqPredicate("@ability", KingdomAbility.LOYALTY.value)
            Leader.GENERAL, Leader.MAGISTER -> -4 to EqPredicate("@phase", KingdomPhase.ARMY.value)
            Leader.TREASURER -> -1 to EqPredicate("@ability", KingdomAbility.ECONOMY.value)
            Leader.VICEROY -> -1 to EqPredicate("@ability", KingdomAbility.STABILITY.value)
            Leader.WARDEN -> -4 to EqPredicate("@phase", KingdomPhase.REGION.value)
        }
        Modifier(
            value = value + rulerVacancyPenalty,
            id = "vacancy-${it.value}",
            name = "Vacancy (${it.label})",
            predicates = listOfNotNull(predicate),
            type = ModifierType.VACANCY,
        )
    }
}