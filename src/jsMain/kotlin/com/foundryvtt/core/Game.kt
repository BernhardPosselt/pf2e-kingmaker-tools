package com.foundryvtt.core

import com.foundryvtt.core.canvas.Canvas
import com.foundryvtt.core.documents.User
import com.foundryvtt.core.documents.collections.Actors
import com.foundryvtt.core.documents.collections.CombatEncounters
import com.foundryvtt.core.documents.collections.CompendiumPacks
import com.foundryvtt.core.documents.collections.Folders
import com.foundryvtt.core.documents.collections.Items
import com.foundryvtt.core.documents.collections.Journal
import com.foundryvtt.core.documents.collections.Macros
import com.foundryvtt.core.documents.collections.Playlists
import com.foundryvtt.core.documents.collections.RollTables
import com.foundryvtt.core.documents.collections.Scenes
import com.foundryvtt.core.documents.collections.Users
import com.foundryvtt.core.helpers.GameTime
import com.foundryvtt.core.helpers.Localization
import com.foundryvtt.core.utils.Collection
import io.socket.Socket

external val game: Game

/**
 * Note: many if not all of these objects are only available after init
 */
external object Game {
    val settings: Settings
    val actors: Actors
    val playlists: Playlists
    val folders: Folders
    val users: Users
    val tables: RollTables
    val scenes: Scenes
    val journal: Journal
    val macros: Macros
    val combats: CombatEncounters
    val user: User
    val modules: Collection<Module>
    val packs: CompendiumPacks
    val time: GameTime
    val socket: Socket
    val canvas: Canvas
    val items: Items
    val i18n: Localization
}

