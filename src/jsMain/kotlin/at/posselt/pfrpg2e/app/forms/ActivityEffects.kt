package at.posselt.pfrpg2e.app.forms

import at.posselt.pfrpg2e.camping.ActivityEffect
import at.posselt.pfrpg2e.camping.dialogs.ActivityEffectTarget
import at.posselt.pfrpg2e.toLabel
import com.foundryvtt.core.AnyObject
import com.foundryvtt.pf2e.item.PF2EEffect
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface ActivityEffectItem {
    val uuid: String
    val img: String
    val label: String
    val target: String
}

@JsPlainObject
external interface ActivityEffectsContext {
    val section: String
    val effects: Array<ActivityEffectItem>
}

class ActivityEffects(
    override val label: String = "Effects",
    val value: ActivityEffectsContext,
    val stacked: Boolean = true,
    override val name: String = "",
    override val help: String? = null,
    override val hideLabel: Boolean = false,
) : IntoFormElementContext {
    override fun toContext() =
        Component(
            label = label,
            templatePartial = "activityEffectsInput",
            value = value.unsafeCast<AnyObject>(),
            stacked = false,
            hideLabel = hideLabel,
            help = help,
            labelElement = "span",
        ).toContext()
}

fun toActivityEffectContext(
    allEffects: Array<PF2EEffect>,
    section: String,
    effects: Array<ActivityEffect>
) = ActivityEffectsContext(
    section = section,
    effects = effects.mapNotNull { effect ->
        allEffects.find { it.uuid == effect.uuid }?.let { effectItem ->
            ActivityEffectItem(
                uuid = effect.uuid,
                img = effectItem.img,
                label = effectItem.name!!,
                target = effect.target?.toLabel() ?: ActivityEffectTarget.ALL.toLabel(),
            )
        }
    }.toTypedArray()
)