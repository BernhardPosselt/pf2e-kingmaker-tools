package at.posselt.pfrpg2e.utils

import at.posselt.pfrpg2e.Config
import at.posselt.pfrpg2e.data.checks.RollMode
import at.posselt.pfrpg2e.toCamelCase
import com.foundryvtt.core.Game
import com.foundryvtt.core.documents.DrawOptions
import com.foundryvtt.core.documents.RollTable
import com.foundryvtt.core.documents.RollTableDraw
import kotlinx.coroutines.await

data class TableAndDraw(val table: RollTable, val draw: RollTableDraw)

suspend fun Game.rollWithCompendiumFallback(
    rollMode: RollMode,
    uuid: String? = null,
    compendiumUuid: String,
    displayChat: Boolean? = true,
    compendium: String = Config.rollTables.compendium,
): TableAndDraw? {
    val table = findRollTableWithCompendiumFallback(
        compendiumUuid = compendiumUuid,
        compendium = compendium,
        uuid = uuid,
    )
    return table?.rollWithDraw(rollMode = rollMode, displayChat = displayChat)
}


suspend fun RollTable.rollWithDraw(
    rollMode: RollMode,
    displayChat: Boolean? = true,
): TableAndDraw {
    val roll = draw(DrawOptions(rollMode = rollMode.toCamelCase(), displayChat = displayChat)).await()
    return TableAndDraw(this, roll)
}

suspend fun Game.findRollTableWithCompendiumFallback(
    uuid: String? = null,
    compendiumUuid: String,
    compendium: String = Config.rollTables.compendium,
) =
    if (uuid == null) {
        packs.get(compendium)
            ?.getDocuments()
            ?.await()
            ?.filterIsInstance<RollTable>()
            ?.find { it.uuid == compendiumUuid }
    } else {
        tables.contents.find { it.uuid == uuid }
            ?: packs.get(compendium)
                ?.getDocuments()
                ?.await()
                ?.filterIsInstance<RollTable>()
                ?.find { it.uuid == compendiumUuid }
    }