package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.kingdom.data.RawSkillRanks
import at.posselt.pfrpg2e.utils.t
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
            label = t(KingdomSkill.AGRICULTURE),
            hideLabel = true,
            value = agriculture,
            name = "skillRanks.agriculture",
        ).toContext(),
        arts = NumberInput(
            label = t(KingdomSkill.ARTS),
            hideLabel = true,
            value = arts,
            name = "skillRanks.arts",
        ).toContext(),
        boating = NumberInput(
            label = t(KingdomSkill.BOATING),
            hideLabel = true,
            value = boating,
            name = "skillRanks.boating",
        ).toContext(),
        defense = NumberInput(
            label = t(KingdomSkill.DEFENSE),
            hideLabel = true,
            value = defense,
            name = "skillRanks.defense",
        ).toContext(),
        engineering = NumberInput(
            label = t(KingdomSkill.ENGINEERING),
            hideLabel = true,
            value = engineering,
            name = "skillRanks.engineering",
        ).toContext(),
        exploration = NumberInput(
            label = t(KingdomSkill.EXPLORATION),
            hideLabel = true,
            value = exploration,
            name = "skillRanks.exploration",
        ).toContext(),
        folklore = NumberInput(
            label = t(KingdomSkill.FOLKLORE),
            hideLabel = true,
            value = folklore,
            name = "skillRanks.folklore",
        ).toContext(),
        industry = NumberInput(
            label = t(KingdomSkill.INDUSTRY),
            hideLabel = true,
            value = industry,
            name = "skillRanks.industry",
        ).toContext(),
        intrigue = NumberInput(
            label = t(KingdomSkill.INTRIGUE),
            hideLabel = true,
            value = intrigue,
            name = "skillRanks.intrigue",
        ).toContext(),
        magic = NumberInput(
            label = t(KingdomSkill.MAGIC),
            hideLabel = true,
            value = magic,
            name = "skillRanks.magic",
        ).toContext(),
        politics = NumberInput(
            label = t(KingdomSkill.POLITICS),
            hideLabel = true,
            value = politics,
            name = "skillRanks.politics",
        ).toContext(),
        scholarship = NumberInput(
            label = t(KingdomSkill.SCHOLARSHIP),
            hideLabel = true,
            value = scholarship,
            name = "skillRanks.scholarship",
        ).toContext(),
        statecraft = NumberInput(
            label = t(KingdomSkill.STATECRAFT),
            hideLabel = true,
            value = statecraft,
            name = "skillRanks.statecraft",
        ).toContext(),
        trade = NumberInput(
            label = t(KingdomSkill.TRADE),
            hideLabel = true,
            value = trade,
            name = "skillRanks.trade",
        ).toContext(),
        warfare = NumberInput(
            label = t(KingdomSkill.WARFARE),
            hideLabel = true,
            value = warfare,
            name = "skillRanks.warfare",
        ).toContext(),
        wilderness = NumberInput(
            label = t(KingdomSkill.WILDERNESS),
            hideLabel = true,
            value = wilderness,
            name = "skillRanks.wilderness",
        ).toContext(),
    )