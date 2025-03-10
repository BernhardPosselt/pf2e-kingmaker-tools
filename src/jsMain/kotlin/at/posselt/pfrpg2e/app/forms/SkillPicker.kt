package at.posselt.pfrpg2e.app.forms

import at.posselt.pfrpg2e.app.SkillInputArrayContext
import at.posselt.pfrpg2e.camping.CampingSkill
import at.posselt.pfrpg2e.data.actor.Proficiency
import at.posselt.pfrpg2e.toLabel
import at.posselt.pfrpg2e.utils.asSequence
import com.foundryvtt.core.AnyObject
import js.objects.Record
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface SkillInputContext {
    val skills: Array<SkillInputArrayContext>
}

class SkillPicker(
    override val label: String = "Skills",
    val context: SkillInputContext,
    val stacked: Boolean = true,
    override val name: String = "",
    override val help: String? = null,
    override val hideLabel: Boolean = false,
) : IntoFormElementContext {
    override fun toContext() =
        Component(
            label = label,
            templatePartial = "skillPickerInput",
            value = context.unsafeCast<AnyObject>(),
            stacked = false,
            hideLabel = hideLabel,
            help = help,
        ).toContext()
}

fun toSkillContext(skills: Record<String, Int>): SkillInputContext {
    return SkillInputContext(
        skills = skills.asSequence()
            .map { (skill, rank) ->
                SkillInputArrayContext(
                    label = skill.toLabel(),
                    proficiency = Proficiency.fromRank(rank).value,
                )
            }
            .toList()
            .toTypedArray()
    )
}

fun toSkillContext(skills: Array<CampingSkill>): SkillInputContext {
    val anySkill = skills.find { it.name == "any" }
    return if (anySkill == null) {
        SkillInputContext(
            skills = skills
                .filter { it.validateOnly != true }
                .map {
                    SkillInputArrayContext(
                        label = it.name.toLabel(),
                        proficiency = it.proficiency.toLabel(),
                    )
                }
                .toTypedArray()
        )
    } else {
        SkillInputContext(
            skills = arrayOf(
                SkillInputArrayContext(
                    label = "Any",
                    proficiency = anySkill.proficiency.toLabel(),
                )
            )
        )
    }
}