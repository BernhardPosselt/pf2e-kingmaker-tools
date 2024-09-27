package com.foundryvtt.core

import com.foundryvtt.core.collections.*
import com.foundryvtt.core.documents.User
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
}

