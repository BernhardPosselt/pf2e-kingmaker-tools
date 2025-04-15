package at.posselt.pfrpg2e.app.forms

import at.posselt.pfrpg2e.app.SkillInputArrayContext
import at.posselt.pfrpg2e.camping.CampingSkill
import at.posselt.pfrpg2e.data.actor.Attribute
import at.posselt.pfrpg2e.data.actor.Proficiency
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.AnyObject
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface SkillInputContext {
    val hideProficiency: Boolean
    val skills: Array<SkillInputArrayContext>
}

class SkillPicker(
    override val label: String = "",
    val context: SkillInputContext,
    val stacked: Boolean = true,
    override val name: String = "",
    override val help: String? = null,
    override val hideLabel: Boolean = false,
) : IntoFormElementContext {
    override fun toContext() =
        Component(
            label = label.takeIf { it.isNotBlank() } ?: t("applications.skills"),
            templatePartial = "skillPickerInput",
            value = context.unsafeCast<AnyObject>(),
            stacked = false,
            hideLabel = hideLabel,
            help = help,
        ).toContext()
}

fun toSkillContext(skills: Array<CampingSkill>): SkillInputContext {
    val anySkill = skills.find { it.name == "any" }
    return if (anySkill == null) {
        SkillInputContext(
            hideProficiency = false,
            skills = skills
                .filter { it.validateOnly != true }
                .map {
                    SkillInputArrayContext(
                        label = t(Attribute.fromString(it.name)),
                        proficiency = t(Proficiency.fromString(it.proficiency) ?: Proficiency.UNTRAINED),
                    )
                }
                .toTypedArray()
        )
    } else {
        SkillInputContext(
            hideProficiency = false,
            skills = arrayOf(
                SkillInputArrayContext(
                    label = t("applications.any"),
                    proficiency = t(Proficiency.fromString(anySkill.proficiency) ?: Proficiency.UNTRAINED),
                )
            )
        )
    }
}