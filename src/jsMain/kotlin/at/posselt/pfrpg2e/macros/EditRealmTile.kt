package at.posselt.pfrpg2e.macros

import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.app.prompt
import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.kingdom.getKingdomActors
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel
import at.posselt.pfrpg2e.utils.RealmTileData
import at.posselt.pfrpg2e.utils.getRealmTileData
import at.posselt.pfrpg2e.utils.setRealmTileData
import at.posselt.pfrpg2e.utils.unsetRealmTileData
import com.foundryvtt.core.Game
import com.foundryvtt.core.ui
import js.objects.recordOf
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface EditRealmTileData {
    val type: String?
    val kingdomActorUuid: String
}

enum class RealmTileCategory {
    CLAIMED,
    COMMODITY,
    WORKSITE;

    companion object {
        fun fromString(value: String) = fromCamelCase<RealmTileCategory>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}

enum class RealmTileType(val category: RealmTileCategory): ValueEnum, Translatable {
    MINE(RealmTileCategory.WORKSITE),
    ORE(RealmTileCategory.COMMODITY),
    LUMBER(RealmTileCategory.COMMODITY),
    LUMBER_CAMP(RealmTileCategory.WORKSITE),
    QUARRY(RealmTileCategory.WORKSITE),
    STONE(RealmTileCategory.COMMODITY),
    LUXURY(RealmTileCategory.COMMODITY),
    LUXURY_WORKSITE(RealmTileCategory.WORKSITE),
    FARMLAND(RealmTileCategory.WORKSITE),
    FOOD(RealmTileCategory.COMMODITY),
    CLAIMED(RealmTileCategory.CLAIMED);

    companion object {
        fun fromString(value: String) = fromCamelCase<RealmTileType>(value)
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "realmTypeType.$value"
}

suspend fun editRealmTileMacro(game: Game) {
    val drawings = game.canvas.drawings.controlled.map { it.document }
    val tiles = game.canvas.tiles.controlled.map { it.document }
    if (drawings.isEmpty() && tiles.isEmpty()) {
        ui.notifications.error("Please select drawings or tiles")
        return
    }
    val data = drawings.firstOrNull()?.getRealmTileData()
        ?: tiles.firstOrNull()?.getRealmTileData()
    val kingdoms = game.getKingdomActors()
        .map { SelectOption(label = it.name, value = it.uuid) }
    prompt<EditRealmTileData, Unit>(
        title = "Edit Selected Realm Tiles/Drawings",
        templatePath = "components/forms/form.hbs",
        templateContext = recordOf(
            "formRows" to formContext(
                Select.fromEnum<RealmTileType>(
                    name = "type",
                    label = "Realm Tile/Drawing Type",
                    value = data?.type?.let { RealmTileType.fromString(it) },
                    required = false,
                ),
                Select(
                    name = "kingdomActorUuid",
                    label = "Kingdom",
                    value = data?.kingdomActorUuid,
                    options = kingdoms,
                    required = false,
                ),
            )
        )
    ) {
        val type = it.type
        if (type == null) {
            drawings.forEach { drawing -> drawing.unsetRealmTileData() }
            tiles.forEach { drawing -> drawing.unsetRealmTileData() }
        } else {
            drawings.forEach { drawing ->
                drawing.setRealmTileData(
                    RealmTileData(
                        type = type,
                        kingdomActorUuid = it.kingdomActorUuid,
                    )
                )
            }
            tiles.forEach { tile ->
                tile.setRealmTileData(
                    RealmTileData(
                        type = type,
                        kingdomActorUuid = it.kingdomActorUuid,
                    )
                )
            }
        }
    }
}