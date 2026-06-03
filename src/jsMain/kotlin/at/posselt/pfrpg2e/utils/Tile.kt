package at.posselt.pfrpg2e.utils

import com.foundryvtt.core.documents.TileDocument
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RealmTileData {
    val type: String
    val kingdomActorUuid: String?
    val settlementId: String?

    // Identifies which native Kingmaker hex a per-hex overlay (claimed/explored/cleared)
    // belongs to. We match overlays on this stamped key rather than on geometry, so a single
    // hex can carry several overlay types at once and each is tracked deterministically.
    val hexKey: String?
}

fun TileDocument.getRealmTileData(): RealmTileData? =
    getAppFlag("realmTile")

suspend fun TileDocument.setRealmTileData(data: RealmTileData) {
    setAppFlag("realmTile", data)
}

suspend fun TileDocument.unsetRealmTileData() {
    unsetAppFlag("realmTile")
}