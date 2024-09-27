package at.posselt.pfrpg2e.utils

import com.foundryvtt.core.documents.TileDocument
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RealmTileData {
    val type: String
}

fun TileDocument.getRealmTileData(): RealmTileData? =
    getAppFlag("realmTile")

suspend fun TileDocument.setRealmTileData(data: RealmTileData) {
    setAppFlag("realmTile", data)
}

suspend fun TileDocument.unsetRealmTileData() {
    unsetAppFlag("realmTile")
}