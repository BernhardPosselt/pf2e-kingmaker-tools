package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.kingdom.data.RawSkillRanks
import js.objects.JsPlainObject

@JsPlainObject
external interface SkillRanksContext {
    val agriculture: FormElementContext
    val arts: FormElementContext
    val boating: FormElementContext
    val defense: FormElementContext
    val engineering: FormElementContext
    val exploration: FormElementContext
    val folklore: FormElementContext
    val industry: FormElementContext
    val intrigue: FormElementContext
    val magic: FormElementContext
    val politics: FormElementContext
    val scholarship: FormElementContext
    val statecraft: FormElementContext
    val trade: FormElementContext
    val warfare: FormElementContext
    val wilderness: FormElementContext
}

fun RawSkillRanks.toContext() =
    SkillRanksContext(
        agriculture = NumberInput(
            label = "Agriculture",
            hideLabel = true,
            value = agriculture,
            name = "skillRanks.agriculture",
        ).toContext(),
        arts = NumberInput(
            label = "Arts",
            hideLabel = true,
            value = arts,
            name = "skillRanks.arts",
        ).toContext(),
        boating = NumberInput(
            label = "Boating",
            hideLabel = true,
            value = boating,
            name = "skillRanks.boating",
        ).toContext(),
        defense = NumberInput(
            label = "Defense",
            hideLabel = true,
            value = defense,
            name = "skillRanks.defense",
        ).toContext(),
        engineering = NumberInput(
            label = "Engineering",
            hideLabel = true,
            value = engineering,
            name = "skillRanks.engineering",
        ).toContext(),
        exploration = NumberInput(
            label = "Exploration",
            hideLabel = true,
            value = exploration,
            name = "skillRanks.exploration",
        ).toContext(),
        folklore = NumberInput(
            label = "Folklore",
            hideLabel = true,
            value = folklore,
            name = "skillRanks.folklore",
        ).toContext(),
        industry = NumberInput(
            label = "Industry",
            hideLabel = true,
            value = industry,
            name = "skillRanks.industry",
        ).toContext(),
        intrigue = NumberInput(
            label = "Intrigue",
            hideLabel = true,
            value = intrigue,
            name = "skillRanks.intrigue",
        ).toContext(),
        magic = NumberInput(
            label = "Magic",
            hideLabel = true,
            value = magic,
            name = "skillRanks.magic",
        ).toContext(),
        politics = NumberInput(
            label = "Politics",
            hideLabel = true,
            value = politics,
            name = "skillRanks.politics",
        ).toContext(),
        scholarship = NumberInput(
            label = "Scholarship",
            hideLabel = true,
            value = scholarship,
            name = "skillRanks.scholarship",
        ).toContext(),
        statecraft = NumberInput(
            label = "Statecraft",
            hideLabel = true,
            value = statecraft,
            name = "skillRanks.statecraft",
        ).toContext(),
        trade = NumberInput(
            label = "Trade",
            hideLabel = true,
            value = trade,
            name = "skillRanks.trade",
        ).toContext(),
        warfare = NumberInput(
            label = "Warfare",
            hideLabel = true,
            value = warfare,
            name = "skillRanks.warfare",
        ).toContext(),
        wilderness = NumberInput(
            label = "Wilderness",
            hideLabel = true,
            value = wilderness,
            name = "skillRanks.wilderness",
        ).toContext(),
    )