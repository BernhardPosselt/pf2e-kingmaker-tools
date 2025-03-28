package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.HiddenInput
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderActor
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderActors
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderType
import at.posselt.pfrpg2e.kingdom.data.RawLeaderValues
import at.posselt.pfrpg2e.kingdom.data.RawLeaders
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.LeaderBonuses
import at.posselt.pfrpg2e.utils.formatAsModifier
import kotlinx.browser.document
import kotlinx.html.b
import kotlinx.html.classes
import kotlinx.html.dom.create
import kotlinx.html.js.span
import kotlinx.js.JsPlainObject

@Suppress("unused")
@JsPlainObject
external interface ActorLeaderContext {
    val name: String
    val uuid: String
    val uuidInput: FormElementContext
    val img: String?
    val level: Int
    val bonus: String
}

@JsPlainObject
external interface LeaderValuesContext {
    val label: String
    val leader: String
    val actor: ActorLeaderContext?
    val invested: FormElementContext
    val type: FormElementContext
    val vacant: FormElementContext
}

fun RawLeaderValues.toContext(
    leaderActor: LeaderActor?,
    leader: Leader,
    bonus: Int,
): LeaderValuesContext {
    val leaderActorName = leaderActor?.name
    val label = document.create.span {
        classes = setOf("km-leader-vacant-label")
        b {
            +leader.label
        }
        +(leaderActorName?.let { ": $it" } ?: "")
    }
    return LeaderValuesContext(
        label = leader.label,
        actor = leaderActor?.let { actor ->
            ActorLeaderContext(
                uuid = actor.uuid,
                img = actor.img,
                level = actor.level,
                bonus = bonus.formatAsModifier(),
                name = actor.name,
                uuidInput = HiddenInput(
                    value = actor.uuid,
                    name = "leaders.${leader.value}.uuid"
                ).toContext()
            )
        },
        invested = CheckboxInput(
            value = invested && uuid != null,
            name = "leaders.${leader.value}.invested",
            label = "Invested",
        ).toContext(),
        vacant = CheckboxInput(
            escapeLabel = false,
            value = vacant || uuid == null,
            name = "leaders.${leader.value}.vacant",
            label = label.outerHTML,
        ).toContext(),
        type = Select.fromEnum<LeaderType>(
            name = "leaders.${leader.value}.type",
            label = "Type",
            hideLabel = true,
            value = LeaderType.fromString(type) ?: LeaderType.PC,
        ).toContext(),
        leader= leader.value,
    )
}

fun RawLeaders.toContext(
    leaderActors: LeaderActors,
    bonuses: LeaderBonuses,
): Array<LeaderValuesContext> = arrayOf(
    ruler.toContext(
        leaderActors.resolve(Leader.RULER),
        Leader.RULER,
        bonuses.resolve(Leader.RULER)
    ),
    counselor.toContext(
        leaderActors.resolve(Leader.COUNSELOR),
        Leader.COUNSELOR,
        bonuses.resolve(Leader.COUNSELOR)
    ),
    emissary.toContext(
        leaderActors.resolve(Leader.EMISSARY),
        Leader.EMISSARY,
        bonuses.resolve(Leader.EMISSARY)
    ),
    general.toContext(
        leaderActors.resolve(Leader.GENERAL),
        Leader.GENERAL,
        bonuses.resolve(Leader.GENERAL)
    ),
    magister.toContext(
        leaderActors.resolve(Leader.MAGISTER),
        Leader.MAGISTER,
        bonuses.resolve(Leader.MAGISTER)
    ),
    treasurer.toContext(
        leaderActors.resolve(Leader.TREASURER),
        Leader.TREASURER,
        bonuses.resolve(Leader.TREASURER)
    ),
    viceroy.toContext(
        leaderActors.resolve(Leader.VICEROY),
        Leader.VICEROY,
        bonuses.resolve(Leader.VICEROY)
    ),
    warden.toContext(
        leaderActors.resolve(Leader.WARDEN),
        Leader.WARDEN,
        bonuses.resolve(Leader.WARDEN)
    ),
)