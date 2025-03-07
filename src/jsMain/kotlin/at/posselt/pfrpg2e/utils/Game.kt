package at.posselt.pfrpg2e.utils

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.kingdom.KingdomActivity
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.KingdomFeature
import at.posselt.pfrpg2e.kingdom.KingdomSettings
import at.posselt.pfrpg2e.kingdom.RawKingdomFeat
import at.posselt.pfrpg2e.kingdom.structures.RawSettlement
import at.posselt.pfrpg2e.kingdom.structures.RawStructureData
import com.foundryvtt.core.Actor
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game
import com.foundryvtt.pf2e.actor.PF2EActor
import com.foundryvtt.pf2e.actor.PF2EArmy
import com.foundryvtt.pf2e.actor.PF2ENpc
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface ToolsMacros {
    val toggleWeatherMacro: () -> Unit
    val toggleShelteredMacro: () -> Unit
    val setCurrentWeatherMacro: () -> Unit
    val sceneWeatherSettingsMacro: () -> Unit
    val kingdomEventsMacro: () -> Unit
    val cultEventsMacro: () -> Unit
    val rollKingmakerWeatherMacro: () -> Unit
    val awardXpMacro: () -> Unit
    val resetHeroPointsMacro: () -> Unit
    val awardHeroPointsMacro: () -> Unit
    val rollExplorationSkillCheck: (String, String) -> Unit
    val rollSkillDialog: () -> Unit
    val setSceneCombatPlaylistDialogMacro: (PF2EActor?) -> Unit
    val toTimeOfDayMacro: () -> Unit
    val toggleCombatTracksMacro: () -> Unit
    val realmTileDialogMacro: () -> Unit
    val editStructureMacro: (Actor?) -> Unit
    val openCampingSheet: () -> Unit
    val createFoodMacro: () -> Unit
    val subsistMacro: (Actor?) -> Unit
//    val structureTokenMappingMacro: () -> Unit
//    val viewKingdomMacro: () -> Unit
}

@JsPlainObject
external interface KtMigrationData {
    val structures: Array<RawStructureData>
    val feats: Array<RawKingdomFeat>
    val features: Array<KingdomFeature>
    val activities: Array<KingdomActivity>
}

@JsPlainObject
external interface KtMigration {
    val kingdomSettings: (settings: KingdomSettings, onSave: (settings: KingdomSettings) -> Unit) -> Unit
    val kingdomSizeHelp: () -> Unit
    val settlementSizeHelp: () -> Unit
    val structureXpDialog: (onSave: (xp: Int) -> Unit) -> Unit
    val editSettlementDialog: (
        autoLevel: Boolean,
        settlementName: String,
        settlement: RawSettlement,
        onOk: (RawSettlement) -> Unit
    ) -> Unit
    val addOngoingEventDialog: (onSave: (String) -> Unit) -> Unit
    val checkDialog: (
        game: Game,
        kingdom: KingdomData,
        kingdomActor: PF2ENpc,
        activity: KingdomActivity?,
        structure: RawStructureData?,
        skill: String?,
        afterRoll: (degree: DegreeOfSuccess) -> Promise<String>
    ) -> Unit
    val data: KtMigrationData
    val armyBrowser: (game: Game, actor: PF2ENpc, kingdom: KingdomData) -> Unit
    val tacticsBrowser: (game: Game, actor: PF2ENpc, kingdom: KingdomData, army: PF2EArmy) -> Unit
    val adjustUnrest: (kingdom: KingdomData) -> Promise<Int>
    val collectResources: (kingdom: KingdomData) -> Promise<AnyObject>
    val addModifier: () -> Unit
}

@JsPlainObject
external interface Pfrpg2eKingdomCampingWeather {
    val macros: ToolsMacros
    val migration: KtMigration
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
inline var Game.pf2eKingmakerTools: Pfrpg2eKingdomCampingWeather
    get() = asDynamic().pf2eKingmakerTools as Pfrpg2eKingdomCampingWeather
    set(value) {
        asDynamic().pf2eKingmakerTools = value
    }
