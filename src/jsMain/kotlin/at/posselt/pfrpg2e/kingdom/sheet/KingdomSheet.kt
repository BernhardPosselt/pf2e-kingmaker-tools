package at.posselt.pfrpg2e.kingdom.sheet

import at.posselt.pfrpg2e.actions.ActionDispatcher
import at.posselt.pfrpg2e.actions.ActionMessage
import at.posselt.pfrpg2e.actions.handlers.OpenKingdomSheetAction
import at.posselt.pfrpg2e.actor.openActor
import at.posselt.pfrpg2e.app.ActorRef
import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.MenuControl
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.HiddenInput
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.OverrideType
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.data.checks.RollMode
import at.posselt.pfrpg2e.data.events.KingdomEventTrait
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.Relations
import at.posselt.pfrpg2e.data.kingdom.calculateControlDC
import at.posselt.pfrpg2e.data.kingdom.calculateHexXP
import at.posselt.pfrpg2e.data.kingdom.calculateRpXP
import at.posselt.pfrpg2e.data.kingdom.findKingdomSize
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.data.kingdom.settlements.SettlementType
import at.posselt.pfrpg2e.kingdom.AutomateResources
import at.posselt.pfrpg2e.kingdom.KingdomActor
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.RawEq
import at.posselt.pfrpg2e.kingdom.RawModifier
import at.posselt.pfrpg2e.kingdom.RawOngoingKingdomEvent
import at.posselt.pfrpg2e.kingdom.RawSome
import at.posselt.pfrpg2e.kingdom.SettlementTerrain
import at.posselt.pfrpg2e.kingdom.armies.updateArmyConsumption
import at.posselt.pfrpg2e.kingdom.createModifiers
import at.posselt.pfrpg2e.kingdom.createSimpleContext
import at.posselt.pfrpg2e.kingdom.data.RawBonusFeat
import at.posselt.pfrpg2e.kingdom.data.RawConsumption
import at.posselt.pfrpg2e.kingdom.data.RawGroup
import at.posselt.pfrpg2e.kingdom.data.endTurn
import at.posselt.pfrpg2e.kingdom.data.getChosenCharter
import at.posselt.pfrpg2e.kingdom.data.getChosenFeats
import at.posselt.pfrpg2e.kingdom.data.getChosenFeatures
import at.posselt.pfrpg2e.kingdom.data.getChosenGovernment
import at.posselt.pfrpg2e.kingdom.data.getChosenHeartland
import at.posselt.pfrpg2e.kingdom.dialogs.ActivityManagement
import at.posselt.pfrpg2e.kingdom.dialogs.AddEvent
import at.posselt.pfrpg2e.kingdom.dialogs.AddModifier
import at.posselt.pfrpg2e.kingdom.dialogs.CharterManagement
import at.posselt.pfrpg2e.kingdom.dialogs.CheckType
import at.posselt.pfrpg2e.kingdom.dialogs.FeatManagement
import at.posselt.pfrpg2e.kingdom.dialogs.GovernmentManagement
import at.posselt.pfrpg2e.kingdom.dialogs.HeartlandManagement
import at.posselt.pfrpg2e.kingdom.dialogs.InspectSettlement
import at.posselt.pfrpg2e.kingdom.dialogs.KingdomEventManagement
import at.posselt.pfrpg2e.kingdom.dialogs.KingdomSettingsApplication
import at.posselt.pfrpg2e.kingdom.dialogs.MilestoneManagement
import at.posselt.pfrpg2e.kingdom.dialogs.StructureBrowser
import at.posselt.pfrpg2e.kingdom.dialogs.armyBrowser
import at.posselt.pfrpg2e.kingdom.dialogs.armyTacticsBrowser
import at.posselt.pfrpg2e.kingdom.dialogs.configureLeaderKingdomSkills
import at.posselt.pfrpg2e.kingdom.dialogs.configureLeaderSkills
import at.posselt.pfrpg2e.kingdom.dialogs.consumptionBreakdown
import at.posselt.pfrpg2e.kingdom.dialogs.kingdomCheckDialog
import at.posselt.pfrpg2e.kingdom.dialogs.kingdomSizeHelp
import at.posselt.pfrpg2e.kingdom.dialogs.newSettlementChoices
import at.posselt.pfrpg2e.kingdom.dialogs.settlementSizeHelp
import at.posselt.pfrpg2e.kingdom.dialogs.structureXpDialog
import at.posselt.pfrpg2e.kingdom.getActiveLeader
import at.posselt.pfrpg2e.kingdom.getActivity
import at.posselt.pfrpg2e.kingdom.getAllActivities
import at.posselt.pfrpg2e.kingdom.getAllSettlements
import at.posselt.pfrpg2e.kingdom.getCharters
import at.posselt.pfrpg2e.kingdom.getEvent
import at.posselt.pfrpg2e.kingdom.getExplodedFeatures
import at.posselt.pfrpg2e.kingdom.getFeats
import at.posselt.pfrpg2e.kingdom.getGovernments
import at.posselt.pfrpg2e.kingdom.getHeartlands
import at.posselt.pfrpg2e.kingdom.getKingdom
import at.posselt.pfrpg2e.kingdom.getMilestones
import at.posselt.pfrpg2e.kingdom.getOngoingEvents
import at.posselt.pfrpg2e.kingdom.getRealmData
import at.posselt.pfrpg2e.kingdom.getTrainedSkills
import at.posselt.pfrpg2e.kingdom.hasLeaderUuid
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.calculateInvestedBonus
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.getHighestLeadershipModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.evaluateGlobalBonuses
import at.posselt.pfrpg2e.kingdom.modifiers.penalties.calculateUnrestPenalty
import at.posselt.pfrpg2e.kingdom.parse
import at.posselt.pfrpg2e.kingdom.parseAbilityScores
import at.posselt.pfrpg2e.kingdom.parseLeaderActors
import at.posselt.pfrpg2e.kingdom.parseRuins
import at.posselt.pfrpg2e.kingdom.parseSkillRanks
import at.posselt.pfrpg2e.kingdom.resources.calculateConsumption
import at.posselt.pfrpg2e.kingdom.resources.calculateStorage
import at.posselt.pfrpg2e.kingdom.setKingdom
import at.posselt.pfrpg2e.kingdom.sheet.contexts.KingdomSheetContext
import at.posselt.pfrpg2e.kingdom.sheet.contexts.NavEntryContext
import at.posselt.pfrpg2e.kingdom.sheet.contexts.createBonusFeatContext
import at.posselt.pfrpg2e.kingdom.sheet.contexts.createTabs
import at.posselt.pfrpg2e.kingdom.sheet.contexts.skillChecks
import at.posselt.pfrpg2e.kingdom.sheet.contexts.toActivitiesContext
import at.posselt.pfrpg2e.kingdom.sheet.contexts.toContext
import at.posselt.pfrpg2e.kingdom.sheet.navigation.MainNavEntry
import at.posselt.pfrpg2e.kingdom.sheet.navigation.TurnNavEntry
import at.posselt.pfrpg2e.kingdom.structures.RawSettlement
import at.posselt.pfrpg2e.kingdom.structures.getImportedStructures
import at.posselt.pfrpg2e.kingdom.structures.importSettlementScene
import at.posselt.pfrpg2e.kingdom.structures.importStructures
import at.posselt.pfrpg2e.kingdom.structures.isStructure
import at.posselt.pfrpg2e.kingdom.vacancies
import at.posselt.pfrpg2e.settings.pfrpg2eKingdomCampingWeather
import at.posselt.pfrpg2e.takeIfInstance
import at.posselt.pfrpg2e.utils.TableAndDraw
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.d20Check
import at.posselt.pfrpg2e.utils.formatAsModifier
import at.posselt.pfrpg2e.utils.launch
import at.posselt.pfrpg2e.utils.openJournal
import at.posselt.pfrpg2e.utils.postChatMessage
import at.posselt.pfrpg2e.utils.postChatTemplate
import at.posselt.pfrpg2e.utils.roll
import at.posselt.pfrpg2e.utils.rollWithCompendiumFallback
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.Game
import com.foundryvtt.core.applications.api.ApplicationRenderOptions
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.applications.ux.TextEditor.enrichHtml
import com.foundryvtt.core.documents.Actor
import com.foundryvtt.core.documents.onCreateDrawing
import com.foundryvtt.core.documents.onCreateTile
import com.foundryvtt.core.documents.onCreateToken
import com.foundryvtt.core.documents.onDeleteDrawing
import com.foundryvtt.core.documents.onDeleteScene
import com.foundryvtt.core.documents.onDeleteTile
import com.foundryvtt.core.documents.onDeleteToken
import com.foundryvtt.core.documents.onUpdateActor
import com.foundryvtt.core.documents.onUpdateDrawing
import com.foundryvtt.core.documents.onUpdateItem
import com.foundryvtt.core.documents.onUpdateTile
import com.foundryvtt.core.documents.onUpdateToken
import com.foundryvtt.core.helpers.onApplyTokenStatusEffect
import com.foundryvtt.core.helpers.onCanvasReady
import com.foundryvtt.core.ui
import com.foundryvtt.core.utils.deepClone
import com.foundryvtt.kingmaker.onCloseKingmakerHexEdit
import io.github.uuidjs.uuid.v4
import js.array.toTypedArray
import js.array.tupleOf
import js.core.Void
import js.objects.recordOf
import kotlinx.browser.document
import kotlinx.coroutines.await
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.collections.contains
import kotlin.collections.count
import kotlin.collections.emptyList
import kotlin.collections.filter
import kotlin.collections.filterIndexed
import kotlin.collections.filterIsInstance
import kotlin.collections.filterNot
import kotlin.collections.find
import kotlin.collections.firstNotNullOf
import kotlin.collections.forEach
import kotlin.collections.getOrNull
import kotlin.collections.isNotEmpty
import kotlin.collections.listOf
import kotlin.collections.map
import kotlin.collections.mapIndexed
import kotlin.collections.mapNotNull
import kotlin.collections.mutableSetOf
import kotlin.collections.plus
import kotlin.collections.setOf
import kotlin.collections.sortedBy
import kotlin.collections.sumOf
import kotlin.collections.toSet
import kotlin.collections.toTypedArray
import kotlin.js.Promise
import kotlin.math.max

class KingdomSheet(
    private val game: Game,
    private val actor: KingdomActor,
    private val dispatcher: ActionDispatcher,
) : FormApp<KingdomSheetContext, KingdomSheetData>(
    title = "Manage Kingdom",
    template = "applications/kingdom/kingdom-sheet.hbs",
    debug = true,
    dataModel = KingdomSheetDataModel::class.js,
    classes = setOf("km-kingdom-sheet"),
    id = "kmKingdomSheet-${actor.uuid}",
    width = 1000,
    syncedDocument = actor,
    controls = arrayOf(
        MenuControl(label = t("kingdom.showPlayers"), action = "show-players", gmOnly = true),
        MenuControl(label = t("kingdom.activities"), action = "configure-activities", gmOnly = true),
        MenuControl(label = t("kingdom.charters"), action = "configure-charters", gmOnly = true),
        MenuControl(label = t("kingdom.events"), action = "configure-events", gmOnly = true),
        MenuControl(label = t("kingdom.feats"), action = "configure-feats", gmOnly = true),
        MenuControl(label = t("kingdom.governments"), action = "configure-governments", gmOnly = true),
        MenuControl(label = t("kingdom.heartlands"), action = "configure-heartlands", gmOnly = true),
        MenuControl(label = t("kingdom.milestones"), action = "configure-milestones", gmOnly = true),
        MenuControl(label = t("applications.settings"), action = "settings", gmOnly = true),
        MenuControl(label = t("applications.quickstart"), action = "quickstart", gmOnly = true),
        MenuControl(label = t("applications.help"), action = "help"),
    ),
    scrollable = setOf(
        ".km-kingdom-sheet-sidebar-kingdom",
        ".km-kingdom-sheet-turn",
        ".km-kingdom-sheet-kingdom",
        ".km-kingdom-sheet-modifiers",
        ".km-kingdom-sheet-notes",
        ".km-kingdom-sheet-settlements",
        ".km-kingdom-sheet-trade-agreements",
        ".km-kingdom-sheet-sidebar-turn"
    ),
) {
    private var initialKingdomLevel = getKingdom().level
    private var noCharter = getKingdom().charter.type == null
    private var currentCharacterSheetNavEntry: String = if (noCharter) "Creation" else "$initialKingdomLevel"
    private var currentNavEntry: MainNavEntry = if (noCharter) MainNavEntry.KINGDOM else MainNavEntry.TURN
    private var bonusFeat: String? = null
    private val openedDetails = mutableSetOf<String>()

    init {
        appHook.onDeleteScene { _, _, _ -> render() }
        appHook.onCreateTile { _, _, _, _ -> render() }
        appHook.onUpdateTile { _, _, _, _ -> render() }
        appHook.onDeleteTile { _, _, _ -> render() }
        appHook.onCreateDrawing { _, _, _, _ -> render() }
        appHook.onUpdateDrawing { _, _, _, _ -> render() }
        appHook.onDeleteDrawing { _, _, _ -> render() }
        appHook.onDeleteToken { token, _, _ ->
            if (token.isStructure()) {
                render()
            }
        }
        appHook.onUpdateToken { token, _, _, _ ->
            if (token.isStructure()) {
                render()
            }
        }
        appHook.onCreateToken { token, _, _, _ ->
            if (token.isStructure()) {
                render()
            }
        }
        appHook.onCanvasReady { _ -> render() }
        appHook.onApplyTokenStatusEffect { _, _, _ -> render() }
        appHook.onCloseKingmakerHexEdit { _, _ -> render() }
        appHook.onUpdateActor { actor, _, _, _ -> checkUpdateActorReRenders(actor) }
        appHook.onUpdateItem { item, _, _, _ ->
            val actor = item.actor
            if (item.type == "lore" && actor != null) {
                checkUpdateActorReRenders(actor)
            }
        }
        onDocumentRefDrop(
            ".km-choose-leaders li",
            { it.type == "Actor" }
        ) { event, documentRef ->
            buildPromise {
                val target = event.currentTarget as HTMLElement
                val leader = target.dataset["leader"]?.let { Leader.fromString(it) }
                if (leader != null && documentRef is ActorRef) {
                    val kingdom = getKingdom()
                    when (leader) {
                        Leader.RULER -> kingdom.leaders.ruler.uuid = documentRef.uuid
                        Leader.COUNSELOR -> kingdom.leaders.counselor.uuid = documentRef.uuid
                        Leader.EMISSARY -> kingdom.leaders.emissary.uuid = documentRef.uuid
                        Leader.GENERAL -> kingdom.leaders.general.uuid = documentRef.uuid
                        Leader.MAGISTER -> kingdom.leaders.magister.uuid = documentRef.uuid
                        Leader.TREASURER -> kingdom.leaders.treasurer.uuid = documentRef.uuid
                        Leader.VICEROY -> kingdom.leaders.viceroy.uuid = documentRef.uuid
                        Leader.WARDEN -> kingdom.leaders.warden.uuid = documentRef.uuid
                    }
                    actor.setKingdom(kingdom)
                }
            }
        }
    }

    private fun checkUpdateActorReRenders(actor: Actor) {
        val kingdom = getKingdom()
        if (kingdom.hasLeaderUuid(actor.uuid)) {
            render()
        }
    }

    private fun getKingdom(): KingdomData {
        val kingdom = actor.getKingdom()
        checkNotNull(kingdom) {
            "Actor ${actor.name} is not a kingdom actor"
        }
        return kingdom
    }

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "show-players" -> buildPromise {
                val action = ActionMessage(
                    action = "openKingdomSheet",
                    data = OpenKingdomSheetAction(actorUuid = actor.uuid)
                )
                dispatcher.dispatch(action)
            }

            "clear-leader" -> buildPromise {
                val target = event.target as HTMLElement
                val leader = target.dataset["leader"]?.let { Leader.fromString(it) }
                if (leader != null) {
                    val kingdom = getKingdom()
                    when (leader) {
                        Leader.RULER -> kingdom.leaders.ruler.uuid = null
                        Leader.COUNSELOR -> kingdom.leaders.counselor.uuid = null
                        Leader.EMISSARY -> kingdom.leaders.emissary.uuid = null
                        Leader.GENERAL -> kingdom.leaders.general.uuid = null
                        Leader.MAGISTER -> kingdom.leaders.magister.uuid = null
                        Leader.TREASURER -> kingdom.leaders.treasurer.uuid = null
                        Leader.VICEROY -> kingdom.leaders.viceroy.uuid = null
                        Leader.WARDEN -> kingdom.leaders.warden.uuid = null
                    }
                    actor.setKingdom(kingdom)
                }
            }

            "open-leader" -> buildPromise {
                val target = event.target as HTMLElement
                val leader = target.dataset["leader"]?.let { Leader.fromString(it) }
                if (leader != null) {
                    val kingdom = getKingdom()
                    when (leader) {
                        Leader.RULER -> kingdom.leaders.ruler.uuid?.let { openActor(it) }
                        Leader.COUNSELOR -> kingdom.leaders.counselor.uuid?.let { openActor(it) }
                        Leader.EMISSARY -> kingdom.leaders.emissary.uuid?.let { openActor(it) }
                        Leader.GENERAL -> kingdom.leaders.general.uuid?.let { openActor(it) }
                        Leader.MAGISTER -> kingdom.leaders.magister.uuid?.let { openActor(it) }
                        Leader.TREASURER -> kingdom.leaders.treasurer.uuid?.let { openActor(it) }
                        Leader.VICEROY -> kingdom.leaders.viceroy.uuid?.let { openActor(it) }
                        Leader.WARDEN -> kingdom.leaders.warden.uuid?.let { openActor(it) }
                    }
                    actor.setKingdom(kingdom)
                }
            }

            "change-kingdom-section-nav" -> {
                event.preventDefault()
                event.stopPropagation()
                currentCharacterSheetNavEntry = target.dataset["link"] ?: "Creation"
                render()
            }

            "change-nav" -> {
                event.preventDefault()
                event.stopPropagation()
                currentNavEntry = target.dataset["link"]?.let { MainNavEntry.fromString(it) } ?: MainNavEntry.TURN
                render()
            }

            "add-bonus-feat" -> buildPromise {
                val featId = bonusFeat
                if (featId != null) {
                    val kingdom = getKingdom()
                    kingdom.bonusFeats = kingdom.bonusFeats + RawBonusFeat(
                        id = featId,
                        ruinThresholdIncreases = emptyArray(),
                    )
                    bonusFeat = null
                    actor.setKingdom(kingdom)
                }
            }

            "delete-bonus-feat" -> buildPromise {
                val featId = target.dataset["id"]
                if (featId != null) {
                    val kingdom = getKingdom()
                    kingdom.bonusFeats = kingdom.bonusFeats.filter { it.id != featId }.toTypedArray()
                    actor.setKingdom(kingdom)
                }
            }

            "add-modifier" -> buildPromise {
                val kingdom = getKingdom()
                AddModifier(activities = kingdom.getAllActivities().toTypedArray()) {
                    val current = getKingdom()
                    current.modifiers = current.modifiers + it
                    actor.setKingdom(current)
                }.launch()
            }

            "delete-modifier" -> buildPromise {
                val index = target.dataset["index"]?.toInt() ?: 0
                val kingdom = getKingdom()
                kingdom.modifiers = kingdom.modifiers.filterIndexed { idx, _ -> idx != index }.toTypedArray()
                actor.setKingdom(kingdom)
            }

            "add-group" -> buildPromise {
                val kingdom = getKingdom()
                val realm = game.getRealmData(actor, kingdom)
                kingdom.groups = kingdom.groups + RawGroup(
                    name = t("kingdom.groupName"),
                    negotiationDC = 10 + findKingdomSize(realm.size).controlDCModifier,
                    atWar = false,
                    relations = "none",
                    preventPledgeOfFealty = false,
                )
                actor.setKingdom(kingdom)
            }

            "delete-group" -> buildPromise {
                val index = target.dataset["index"]?.toInt() ?: 0
                val kingdom = getKingdom()
                kingdom.groups = kingdom.groups.filterIndexed { idx, _ -> idx != index }.toTypedArray()
                actor.setKingdom(kingdom)
            }

            "configure-activities" -> ActivityManagement(kingdomActor = actor).launch()
            "configure-events" -> KingdomEventManagement(kingdomActor = actor).launch()
            "configure-milestones" -> MilestoneManagement(kingdomActor = actor).launch()
            "configure-charters" -> CharterManagement(kingdomActor = actor).launch()
            "configure-governments" -> GovernmentManagement(kingdomActor = actor).launch()
            "configure-heartlands" -> HeartlandManagement(kingdomActor = actor).launch()
            "configure-feats" -> FeatManagement(kingdomActor = actor).launch()
            "structures-import" -> buildPromise { importStructures() }

            "create-settlement" -> {
                buildPromise {
                    val result = newSettlementChoices()
                    importSettlement(
                        sceneName = result.name,
                        terrain = result.terrain,
                        waterBorders = result.waterBorders,
                        type = SettlementType.SETTLEMENT,
                    )
                }
            }

            "create-capital" -> {
                buildPromise {
                    val heartland = getKingdom().getChosenHeartland()
                    val terrain = when (heartland?.id) {
                        "forest-or-swamp" -> SettlementTerrain.FOREST
                        "hill-or-plain" -> SettlementTerrain.PLAINS
                        "lake-or-river" -> SettlementTerrain.SWAMP
                        "mountain-or-ruins" -> SettlementTerrain.MOUNTAINS
                        else -> null
                    }
                    val result = newSettlementChoices(terrain)
                    importSettlement(
                        sceneName = result.name,
                        terrain = result.terrain,
                        waterBorders = result.waterBorders,
                        type = SettlementType.CAPITAL,
                    )
                }
            }

            "add-settlement" -> buildPromise {
                game.scenes.current?.id?.let { id ->
                    val kingdom = getKingdom()
                    kingdom.settlements = kingdom.settlements + RawSettlement(
                        sceneId = id,
                        lots = 1,
                        level = 1,
                        type = "settlement",
                        secondaryTerritory = false,
                        manualSettlementLevel = false,
                        waterBorders = 0,
                    )
                    actor.setKingdom(kingdom)
                }
            }

            "delete-settlement" -> buildPromise {
                target.dataset["id"]?.let { id ->
                    val kingdom = getKingdom()
                    kingdom.ongoingEvents = kingdom.ongoingEvents.filter { it.settlementSceneId != id }.toTypedArray()
                    kingdom.settlements = kingdom.settlements.filter { it.sceneId != id }.toTypedArray()
                    actor.setKingdom(kingdom)
                }
            }

            "view-settlement" -> buildPromise {
                target.dataset["id"]?.let { id ->
                    game.scenes.get(id)?.view()?.await()
                }
            }

            "activate-settlement" -> buildPromise {
                target.dataset["id"]?.let { id ->
                    game.scenes.get(id)?.activate()?.await()
                }
            }

            "inspect-settlement" -> buildPromise {
                target.dataset["id"]?.let { id ->
                    val kingdom = getKingdom()
                    val settlement = kingdom.settlements.find { it.sceneId == id }
                    checkNotNull(settlement) {
                        "Could not find raw settlement with id $id"
                    }
                    val autoCalculateSettlementLevel = kingdom.settings.autoCalculateSettlementLevel
                    val allStructuresStack = kingdom.settings.kingdomAllStructureItemBonusesStack
                    val title = game.scenes.get(id)?.name
                    checkNotNull(title) {
                        "Scene with id $id not found"
                    }
                    InspectSettlement(
                        game = game,
                        title = title,
                        autoCalculateSettlementLevel = autoCalculateSettlementLevel,
                        allStructuresStack = allStructuresStack,
                        allowCapitalInvestmentInCapitalWithoutBank = kingdom.settings.capitalInvestmentInCapital,
                        settlement = settlement,
                        feats = kingdom.getChosenFeats(kingdom.getChosenFeatures(kingdom.getExplodedFeatures())),
                        kingdom = kingdom,
                    ) { data ->
                        val kingdom = getKingdom()
                        kingdom.settlements = kingdom.settlements
                            .filter { it.sceneId != data.sceneId }
                            .toTypedArray() + data
                        actor.setKingdom(kingdom)
                    }.launch()
                }
            }

            "settings" -> {
                val kingdom = getKingdom()
                buildPromise {
                    KingdomSettingsApplication(
                        game = game,
                        onSave = {
                            val previous = deepClone(kingdom)
                            kingdom.settings = it
                            kingdom.fame.now = kingdom.fame.now.coerceIn(0, kingdom.settings.maximumFamePoints)
                            beforeKingdomUpdate(previous, kingdom)
                            actor.setKingdom(kingdom)
                            val armyFolderIdChanged =
                                previous.settings.recruitableArmiesFolderId != kingdom.settings.recruitableArmiesFolderId
                            if (kingdom.settings.autoCalculateArmyConsumption && (!previous.settings.autoCalculateArmyConsumption || armyFolderIdChanged)) {
                                updateArmyConsumption(game)
                            }
                        },
                        kingdomSettings = kingdom.settings
                    ).launch()
                }
            }

            "quickstart" -> buildPromise {
                openJournal("Compendium.pf2e-kingmaker-tools.kingmaker-tools-journals.JournalEntry.FwcyYZARAnOHlKkE")
            }

            "help" -> buildPromise {
                openJournal("Compendium.pf2e-kingmaker-tools.kingmaker-tools-journals.JournalEntry.iAQCUYEAq4Dy8uCY.JournalEntryPage.ty6BS5eSI7ScfVBk")
            }

            "gain-xp" -> buildPromise {
                target.dataset["xp"]?.toInt()?.let {
                    actor.gainXp(it)
                }
            }

            "hex-xp" -> buildPromise {
                target.dataset["hexes"]?.toInt()?.let { hexes ->
                    actor.getKingdom()?.let { kingdom ->
                        val xp = calculateHexXP(
                            hexes = hexes,
                            xpPerClaimedHex = kingdom.settings.xpPerClaimedHex,
                            kingdomSize = game.getRealmData(actor, kingdom).size,
                            useVK = kingdom.settings.vanceAndKerensharaXP,
                        )
                        actor.gainXp(xp)
                    }
                }
            }

            "structure-xp" -> buildPromise {
                structureXpDialog(game) {
                    actor.gainXp(it)
                }
            }

            "rp-xp" -> buildPromise {
                actor.getKingdom()?.let { kingdom ->
                    val xp = calculateRpXP(
                        rp = kingdom.resourcePoints.now,
                        kingdomLevel = kingdom.level,
                        rpToXpConversionRate = kingdom.settings.rpToXpConversionRate,
                        rpToXpConversionLimit = kingdom.settings.rpToXpConversionLimit,
                        useVK = kingdom.settings.vanceAndKerensharaXP,
                    )
                    actor.gainXp(xp)
                }
            }

            "solution-xp" -> buildPromise {
                actor.getKingdom()?.let { kingdom ->
                    val xp = (kingdom.supernaturalSolutions + kingdom.creativeSolutions) * 10
                    actor.gainXp(xp)
                }
            }

            "level-up" -> buildPromise {
                actor.levelUp()
            }

            "scroll-to" -> {
                event.stopPropagation()
                event.preventDefault()
                target.dataset["id"]?.let {
                    document.getElementById(it)?.scrollIntoView()
                }
            }

            "toggle-continuous" -> buildPromise {
                val eventIndex = target.dataset["eventIndex"]?.toInt()
                checkNotNull(eventIndex) { "event index is null" }
                actor.getKingdom()?.let { kingdom ->
                    kingdom.ongoingEvents = kingdom.ongoingEvents
                        .mapIndexed { index, event ->
                            if (index == eventIndex) {
                                val isContinuous = event.becameContinuous == true
                                RawOngoingKingdomEvent.copy(event, becameContinuous = !isContinuous)
                            } else {
                                event
                            }
                        }
                        .toTypedArray()
                    actor.setKingdom(kingdom)
                }
            }

            "check-cult-event" -> buildPromise {
                actor.getKingdom()?.let { kingdom ->
                    val dc = getCultEventDC(kingdom)
                    val rollMode = RollMode.fromString(kingdom.settings.kingdomEventRollMode)
                    val succeeded = d20Check(
                        dc = dc,
                        flavor = t("kingdom.checkingForCultEvent", recordOf("dc" to dc)),
                        rollMode = rollMode,
                    ).degreeOfSuccess.succeeded()
                    if (succeeded) {
                        kingdom.turnsWithoutCultEvent = 0
                        postChatMessage(t("kingdom.cultEventOccurs"), rollMode = rollMode)
                    } else {
                        kingdom.turnsWithoutCultEvent += 1
                    }
                    actor.setKingdom(kingdom)
                }
            }

            "check-event" -> buildPromise {
                actor.getKingdom()?.let { kingdom ->
                    val dc = getEventDC(kingdom)
                    val rollMode = RollMode.fromString(kingdom.settings.kingdomEventRollMode)
                    val succeeded = d20Check(
                        dc = dc,
                        flavor = t("kingdom.checkingForKingdomEvent", recordOf("dc" to dc)),
                        rollMode = rollMode,
                    ).degreeOfSuccess.succeeded()
                    if (succeeded) {
                        kingdom.turnsWithoutEvent = 0
                        postChatMessage(t("kingdom.kingdomEventOccurs"), rollMode = rollMode)
                    } else {
                        kingdom.turnsWithoutEvent += 1
                    }
                    actor.setKingdom(kingdom)
                }
            }

            "roll-cult-event" -> buildPromise {
                val kingdom = getKingdom()
                val uuid = kingdom.settings.kingdomCultTable
                val rollMode = kingdom.settings.kingdomEventRollMode
                    .let { RollMode.fromString(it) } ?: RollMode.GMROLL
                val result = game.rollWithCompendiumFallback(
                    rollMode = rollMode,
                    uuid = uuid,
                    compendiumUuid = "Compendium.pf2e-kingmaker-tools.kingmaker-tools-rolltables.RollTable.aYQXu2GwIf5gdyQa",
                )
                postAddToOngoingEvents(result, rollMode, kingdom)
            }

            "roll-event" -> buildPromise {
                val kingdom = getKingdom()
                val uuid = kingdom.settings.kingdomEventsTable
                val rollMode = kingdom.settings.kingdomEventRollMode
                    .let { RollMode.fromString(it) } ?: RollMode.GMROLL
                val result = game.rollWithCompendiumFallback(
                    rollMode = rollMode,
                    uuid = uuid,
                    compendiumUuid = "Compendium.pf2e-kingmaker-tools.kingmaker-tools-rolltables.RollTable.ZXk2yVZH7JMswXbD",
                )
                postAddToOngoingEvents(result, rollMode, kingdom)
            }

            "delete-event" -> buildPromise {
                actor.getKingdom()?.let { kingdom ->
                    target.dataset["index"]?.toInt()?.let { eventIndex ->
                        kingdom.ongoingEvents = kingdom.ongoingEvents
                            .filterIndexed { index, _ -> index != eventIndex }
                            .toTypedArray()
                        actor.setKingdom(kingdom)
                    }
                }
            }

            "add-event" -> {
                val kingdom = getKingdom()
                buildPromise {
                    val settlements = kingdom.getAllSettlements(game)
                    AddEvent(
                        game = game,
                        kingdomActor = actor,
                        kingdom = kingdom,
                        settlements = settlements.allSettlements,
                        onSave = {
                            val k = getKingdom()
                            k.ongoingEvents = k.ongoingEvents + it
                            actor.setKingdom(k)
                        }
                    ).launch()
                }
            }

            "change-event-stage" -> buildPromise {
                val stage = target.dataset["stage"]?.toInt()
                val eventIndex = target.dataset["eventIndex"]?.toInt()
                checkNotNull(stage) { "index is null" }
                checkNotNull(eventIndex) { "event index is null" }
                actor.getKingdom()?.let { kingdom ->
                    kingdom.ongoingEvents = kingdom.ongoingEvents
                        .mapIndexed { index, event ->
                            if (index == eventIndex) {
                                RawOngoingKingdomEvent.copy(event, stage = stage)
                            } else {
                                event
                            }
                        }
                        .toTypedArray()
                    actor.setKingdom(kingdom)
                }
            }

            "handle-event" -> buildPromise {
                val index = target.dataset["index"]?.toInt()
                checkNotNull(index)
                val kingdom = getKingdom()
                val event = kingdom.getOngoingEvents().getOrNull(index)
                checkNotNull(event)
                actor.getKingdom()?.let { kingdom ->
                    kingdomCheckDialog(
                        game = game,
                        kingdom = kingdom,
                        kingdomActor = actor,
                        check = CheckType.HandleEvent(event),
                        selectedLeader = game.getActiveLeader(),
                        groups = emptyArray(),
                        events = emptyList(),
                    )
                }
            }

            "claimed-refuge" -> buildPromise {
                actor.getKingdom()?.let { kingdom ->
                    kingdom.modifiers = kingdom.modifiers + RawModifier(
                        id = v4(),
                        turns = 2,
                        name = "kingdom.claimedRefuge",
                        type = ModifierType.CIRCUMSTANCE.value,
                        value = 2,
                        enabled = true,
                        applyIf = arrayOf(
                            RawSome(
                                some = arrayOf(
                                    RawEq(eq = tupleOf("@ability", "culture")),
                                    RawEq(eq = tupleOf("@ability", "economy")),
                                )
                            )
                        )
                    )
                    val unrest = roll("1d4", flavor = "Losing Unrest")
                    val chosenFeatures = kingdom.getChosenFeatures(kingdom.getExplodedFeatures())
                    kingdom.unrest = kingdom.addUnrest(-unrest, kingdom.getChosenFeats(chosenFeatures))
                    actor.setKingdom(kingdom)
                }
            }

            "claimed-landmark" -> buildPromise {
                actor.getKingdom()?.let { kingdom ->
                    kingdom.modifiers = kingdom.modifiers + RawModifier(
                        id = v4(),
                        turns = 2,
                        name = "kingdom.claimedLandmark",
                        type = ModifierType.CIRCUMSTANCE.value,
                        value = 2,
                        enabled = true,
                        applyIf = arrayOf(
                            RawSome(
                                some = arrayOf(
                                    RawEq(eq = tupleOf("@ability", "loyalty")),
                                    RawEq(eq = tupleOf("@ability", "stability")),
                                )
                            )
                        )
                    )
                    val ruinButtons = sequenceOf(Resource.CRIME, Resource.DECAY, Resource.CORRUPTION, Resource.STRIFE)
                        .map {
                            ResourceButton(
                                value = "1",
                                resource = it,
                                mode = ResourceMode.LOSE
                            ).toHtml(emptyArray())
                        }
                        .toTypedArray()
                    postChatTemplate(
                        templatePath = "chatmessages/landmark.hbs",
                        templateContext = recordOf(
                            "buttons" to ruinButtons,
                            "actorUuid" to actor.uuid
                        )
                    )
                    actor.setKingdom(kingdom)
                }
            }

            "gain-fame" -> buildPromise {
                actor.getKingdom()?.let { kingdom ->
                    kingdom.fame.now = (kingdom.fame.now + 1).coerceIn(0, kingdom.settings.maximumFamePoints)
                    postChatMessage(t("kingdom.gaining1Fame"))
                    actor.setKingdom(kingdom)
                }
            }

            "adjust-unrest" -> buildPromise {
                actor.getKingdom()?.let { kingdom ->
                    val settlements = kingdom.getAllSettlements(game)
                    val allFeatures = kingdom.getExplodedFeatures()
                    val chosenFeatures = kingdom.getChosenFeatures(allFeatures)
                    val chosenFeats = kingdom.getChosenFeats(chosenFeatures)
                    kingdom.unrest = adjustUnrest(
                        kingdom = kingdom,
                        settlements = settlements.allSettlements,
                        chosenFeats = chosenFeats,
                    )
                    actor.setKingdom(kingdom)
                }
            }

            "collect-resources" -> buildPromise {
                actor.getKingdom()?.let { kingdom ->
                    val realm = game.getRealmData(actor, kingdom)
                    val settlements = kingdom.getAllSettlements(game)
                    val allFeatures = kingdom.getExplodedFeatures()
                    val chosenFeatures = kingdom.getChosenFeatures(allFeatures)
                    val chosenFeats = kingdom.getChosenFeats(chosenFeatures)
                    val resources = collectResources(
                        kingdomData = kingdom,
                        realmData = realm,
                        resourceDice = kingdom.getResourceDiceAmount(
                            chosenFeats,
                            settlements.allSettlements,
                            kingdomLevel = kingdom.level,
                        ),
                        increaseGainedLuxuries = chosenFeats.sumOf { it.feat.increaseGainedLuxuriesOncePerTurnBy ?: 0 },
                        settlements = settlements.allSettlements,
                        expressionContext = kingdom.createSimpleContext(settlements),
                        modifiers = kingdom.createModifiers(settlements),
                    )
                    kingdom.resourcePoints.now = resources.resourcePoints
                    kingdom.resourceDice.now = resources.resourceDice
                    kingdom.commodities.now.lumber = resources.lumber
                    kingdom.commodities.now.luxuries = resources.luxuries
                    kingdom.commodities.now.stone = resources.stone
                    kingdom.commodities.now.ore = resources.ore
                    actor.setKingdom(kingdom)
                }
            }

            "pay-consumption" -> buildPromise {
                actor.getKingdom()?.let { kingdom ->
                    val realm = game.getRealmData(actor, kingdom)
                    val settlements = kingdom.getAllSettlements(game)
                    kingdom.commodities.now.food = payConsumption(
                        kingdomActor = actor,
                        settlements = settlements.allSettlements,
                        realmData = realm,
                        armyConsumption = kingdom.consumption.armies,
                        availableFood = kingdom.commodities.now.food,
                        now = kingdom.consumption.now,
                        expressionContext = kingdom.createSimpleContext(settlements),
                        modifiers = kingdom.createModifiers(settlements),
                    )
                    actor.setKingdom(kingdom)
                }
            }

            "end-turn" -> buildPromise {
                actor.getKingdom()?.let { kingdom ->
                    val realm = game.getRealmData(actor, kingdom)
                    val settlements = kingdom.getAllSettlements(game)
                    val storage = calculateStorage(realm = realm, settlements = settlements.allSettlements)
                    kingdom.supernaturalSolutions = 0
                    kingdom.creativeSolutions = 0
                    kingdom.fame.now = kingdom.fame.next
                    kingdom.fame.next = 0
                    kingdom.resourcePoints = kingdom.resourcePoints.endTurn()
                    kingdom.resourceDice = kingdom.resourceDice.endTurn()
                    kingdom.consumption = kingdom.consumption.endTurn()
                    kingdom.commodities = kingdom.commodities.endTurn(storage)
                    // tick down modifiers
                    kingdom.modifiers = kingdom.modifiers.mapNotNull {
                        val turns = it.turns
                        if (turns == 0 || turns == null) {
                            it
                        } else if (turns == 1) {
                            null
                        } else {
                            RawModifier.copy(it, turns = turns - 1)
                        }
                    }.toTypedArray()
                    actor.setKingdom(kingdom)
                }
                postChatTemplate(templatePath = "chatmessages/end-turn.hbs")
            }

            "settlement-size-info" -> buildPromise {
                settlementSizeHelp()
            }

            "kingdom-size-info" -> buildPromise {
                kingdomSizeHelp()
            }

            "consumption-breakdown" -> buildPromise {
                actor.getKingdom()?.let { kingdom ->
                    val realm = game.getRealmData(actor, kingdom)
                    val settlements = kingdom.getAllSettlements(game)
                    val consumption = calculateConsumption(
                        settlements = settlements.allSettlements,
                        realmData = realm,
                        armyConsumption = kingdom.consumption.armies,
                        now = kingdom.consumption.now,
                        expressionContext = kingdom.createSimpleContext(settlements),
                        modifiers = kingdom.createModifiers(settlements),
                    )
                    consumptionBreakdown(consumption.toContext())
                }
            }

            "skip-collect-taxes" -> buildPromise {
                actor.getKingdom()?.let { kingdom ->
                    val succeeded = d20Check(
                        dc = 11,
                        flavor = t("kingdom.skipCollectTaxesCheck"),
                    ).degreeOfSuccess.succeeded()
                    if (succeeded && kingdom.unrest > 0) {
                        postChatMessage(t("kingdom.reducing1Unrest"))
                        kingdom.unrest = (kingdom.unrest - 1).coerceIn(0, Int.MAX_VALUE)
                    }
                    actor.setKingdom(kingdom)
                }
            }

            "roll-skill-check" -> buildPromise {
                val skill = target.dataset["skill"]?.let { KingdomSkill.fromString(it) }
                checkNotNull(skill)
                val kingdom = actor.getKingdom()
                checkNotNull(kingdom)
                kingdomCheckDialog(
                    game = game,
                    kingdom = kingdom,
                    kingdomActor = actor,
                    check = CheckType.RollSkill(skill),
                    selectedLeader = game.getActiveLeader(),
                    groups = emptyArray(),
                    events = emptyList(),
                )
            }

            "inspect-leader-skills" -> {
                val kingdom = actor.getKingdom() ?: return
                configureLeaderSkills(kingdom.settings.leaderSkills, true) {}
            }

            "inspect-kingdom-skills" -> {
                val kingdom = actor.getKingdom() ?: return
                configureLeaderKingdomSkills(kingdom.settings.leaderKingdomSkills, true) {}
            }

            "perform-activity" -> buildPromise {
                val activityId = target.dataset["activity"]
                checkNotNull(activityId)
                val kingdom = actor.getKingdom()
                checkNotNull(kingdom)
                val activity = kingdom.getActivity(activityId)
                checkNotNull(activity)
                actor.getKingdom()?.let { kingdom ->
                    val allFeatures = kingdom.getExplodedFeatures()
                    val chosenFeatures = kingdom.getChosenFeatures(allFeatures)
                    val chosenFeats = kingdom.getChosenFeats(chosenFeatures)
                    val government = kingdom.getChosenGovernment()
                    val kingdomRanks = kingdom.parseSkillRanks(
                        chosenFeatures = chosenFeatures,
                        chosenFeats = chosenFeats,
                        government = government,
                    )
                    when (activityId) {
                        "build-structure" -> {
                            StructureBrowser(
                                actor = actor,
                                kingdom = kingdom,
                                worldStructures = game.getImportedStructures(),
                                game = game,
                                kingdomRanks = kingdomRanks,
                                chosenFeats = chosenFeats,
                            ).launch()
                        }

                        "recruit-army" -> armyBrowser(
                            game = game,
                            kingdomActor = actor,
                            kingdom = kingdom
                        )

                        "train-army" -> armyTacticsBrowser(
                            game = game,
                            kingdomActor = actor,
                            kingdom = kingdom
                        )

                        else -> {
                            val groups = when (activity.id) {
                                "request-foreign-aid",
                                "request-foreign-aid-vk",
                                    -> kingdom.groups.filter {
                                    it.relations != Relations.NONE.value
                                }.toTypedArray()

                                "send-diplomatic-envoy" -> kingdom.groups.filter {
                                    it.relations == Relations.NONE.value
                                }.toTypedArray()

                                "establish-trade-agreement" -> kingdom.groups.filter {
                                    it.relations == Relations.DIPLOMATIC_RELATIONS.value
                                }.toTypedArray()

                                "pledge-of-fealty" -> kingdom.groups.filterNot {
                                    it.preventPledgeOfFealty
                                }.toTypedArray()

                                else -> kingdom.groups
                            }
                            val events = when (activity.id) {
                                else -> kingdom.getOngoingEvents()
                            }
                            kingdomCheckDialog(
                                game = game,
                                kingdom = kingdom,
                                kingdomActor = actor,
                                check = CheckType.PerformActivity(activity),
                                selectedLeader = game.getActiveLeader(),
                                groups = groups,
                                events = events,
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun postAddToOngoingEvents(
        result: TableAndDraw?,
        rollMode: RollMode,
        kingdom: KingdomData,
    ) {
        result
            ?.draw
            ?.results
            ?.firstNotNullOf { it.text }
            ?.let { kingdom.getEvent(it) }
            ?.let { event ->
                val modifier = event.modifier
                val traits = event.traits.mapNotNull { KingdomEventTrait.fromString(it) }
                val stages = event.stages.map {
                    val leader = Leader.fromString(it.leader) ?: Leader.RULER
                    val criticalSuccess = enrichHtml(it.criticalSuccess?.msg ?: "")
                    val success = enrichHtml(it.success?.msg ?: "")
                    val failure = enrichHtml(it.failure?.msg ?: "")
                    val criticalFailure = enrichHtml(it.criticalFailure?.msg ?: "")
                    recordOf(
                        "leader" to t(leader),
                        "skills" to it.skills
                            .mapNotNull { KingdomSkill.fromString(it) }
                            .map { t(it) }
                            .toTypedArray(),
                        "criticalSuccess" to criticalSuccess,
                        "success" to success,
                        "failure" to failure,
                        "criticalFailure" to criticalFailure,
                    )
                }.toTypedArray()
                val description = enrichHtml(event.description)
                postChatTemplate(
                    templatePath = "chatmessages/event.hbs",
                    templateContext = recordOf(
                        "actorUuid" to actor.uuid,
                        "eventId" to event.id,
                        "label" to event.name + if (modifier != null && modifier != 0) " (${modifier.formatAsModifier()})" else "",
                        "traits" to traits.map { t(it) }.toTypedArray(),
                        "location" to event.location,
                        "special" to event.special,
                        "description" to description,
                        "resolution" to event.resolution,
                        "stages" to stages
                    ),
                    rollMode = rollMode,
                )
            }
    }

    suspend fun importStructures() {
        if (game.importStructures().isNotEmpty()) {
            ui.notifications.info(t("kingdom.importedStructures"))
        }
    }

    suspend fun importSettlement(
        sceneName: String,
        terrain: SettlementTerrain,
        waterBorders: Int,
        type: SettlementType,
    ) {
        val scene = game.importSettlementScene(
            uuid = "Compendium.pf2e-kingmaker-tools.kingmaker-tools-settlements.Scene.XmDdx6ufrqqjhkJE",
            sceneName = sceneName,
            terrain = terrain,
            waterBorders = waterBorders,
        )
        scene?.id?.let {
            val kingdom = getKingdom()
            kingdom.settlements = kingdom.settlements + RawSettlement(
                sceneId = it,
                lots = 1,
                level = 1,
                type = type.value,
                secondaryTerritory = false,
                manualSettlementLevel = false,
                waterBorders = waterBorders,
            )
            kingdom.activeSettlement = it
            actor.setKingdom(kingdom)
            scene.activate().await()
        }
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<KingdomSheetContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val kingdom = getKingdom()
        val allFeatures = kingdom.getExplodedFeatures()
        val chosenFeatures = kingdom.getChosenFeatures(allFeatures)
        val vacancies = kingdom.vacancies(
            choices = chosenFeatures,
            bonusFeats = kingdom.bonusFeats,
            government = kingdom.government,
        )
        val realm = game.getRealmData(actor, kingdom)
        val settlements = kingdom.getAllSettlements(game)
        val controlDc = calculateControlDC(kingdom.level, realm, vacancies.ruler)
        val globalBonuses = evaluateGlobalBonuses(settlements.allSettlements)
        val chosenFeats = kingdom.getChosenFeats(chosenFeatures)
        val leaderActors = kingdom.parseLeaderActors()
        val storage = calculateStorage(realm, settlements.allSettlements)
        val leaderSkills = kingdom.settings.leaderSkills.parse()
        val defaultLeaderBonuses = if (kingdom.settings.enableLeadershipModifiers) {
            getHighestLeadershipModifiers(
                leaderActors = leaderActors,
                leaderSkills = leaderSkills,
            )
        } else {
            calculateInvestedBonus(kingdom.level, leaderActors)
        }
        val consumption = calculateConsumption(
            settlements = settlements.allSettlements,
            realmData = realm,
            armyConsumption = kingdom.consumption.armies,
            now = kingdom.consumption.now,
            expressionContext = kingdom.createSimpleContext(settlements),
            modifiers = kingdom.createModifiers(settlements),
        )
        val kingdomNameInput = TextInput(
            name = "name",
            label = t("applications.kingdom"),
            value = kingdom.name,
            elementClasses = listOf("km-width-medium"),
            labelClasses = listOf("km-slim-inputs"),
            required = false,
            stacked = false,
        )
        val settlementInput = Select(
            name = "activeSettlement",
            label = t("kingdom.activeSettlement"),
            value = kingdom.activeSettlement,
            options = settlements.allSettlements.map { SelectOption(it.name, it.id) },
            required = false,
            labelClasses = listOf("km-slim-inputs"),
        )
        val xpInput = NumberInput(
            name = "xp",
            label = t("applications.xp"),
            hideLabel = true,
            elementClasses = listOf("km-width-small", "km-slim-inputs"),
            value = kingdom.xp,
            stacked = false,
        )
        val xpThresholdInput = NumberInput(
            name = "xpThreshold",
            label = t("kingdom.xpThreshold"),
            hideLabel = true,
            elementClasses = listOf("km-width-small", "km-slim-inputs"),
            value = kingdom.xpThreshold,
            stacked = false,
        )
        val levelInput = Select.range(
            name = "level",
            label = t("applications.level"),
            value = kingdom.level,
            elementClasses = listOf("km-width-small"),
            labelClasses = listOf("km-slim-inputs"),
            from = 1,
            to = 20,
            stacked = false,
        )
        val atWarInput = CheckboxInput(
            name = "atWar",
            value = kingdom.atWar,
            label = t("kingdom.atWar")
        )
        val anarchyAt = calculateAnarchy(chosenFeats)
        val unrestInput = Select.range(
            name = "unrest",
            label = t("kingdom.unrest"),
            value = kingdom.unrest,
            from = 0,
            to = anarchyAt,
            stacked = false,
            elementClasses = listOf("km-width-small"),
            labelClasses = listOf("km-slim-inputs"),
        )
        val sizeInput = if (kingdom.settings.automateResources == AutomateResources.MANUAL.value) {
            NumberInput(
                name = "size",
                label = t("kingdom.size"),
                value = kingdom.size,
                elementClasses = listOf("km-slim-inputs", "km-width-small"),
                hideLabel = true,
                stacked = false,
            )
        } else {
            HiddenInput(
                name = "size",
                value = kingdom.size.toString(),
                overrideType = OverrideType.NUMBER,
            )
        }
        val supernaturalSolutionsInput = NumberInput(
            name = "supernaturalSolutions",
            value = kingdom.supernaturalSolutions,
            label = t("kingdom.supernaturalSolutions"),
            stacked = false,
            elementClasses = listOf("km-width-small"),
            labelClasses = listOf("km-slim-inputs"),
        )
        val creativeSolutionsInput = NumberInput(
            name = "creativeSolutions",
            value = kingdom.creativeSolutions,
            label = t("kingdom.creativeSolutions"),
            stacked = false,
            elementClasses = listOf("km-width-small"),
            labelClasses = listOf("km-slim-inputs"),
        )
        val unrestPenalty = calculateUnrestPenalty(kingdom.unrest)
        val feats = kingdom.getFeats()
            .filter { it.id !in kingdom.featBlacklist }
            .toTypedArray()
        val increaseScorePicksBy = kingdom.settings.increaseScorePicksBy
        val kingdomSectionNav = createKingdomSectionNav()
        val heartlandBlacklist = kingdom.heartlandBlacklist.toSet()
        val charterBlacklist = kingdom.charterBlacklist.toSet()
        val governmentBlacklist = kingdom.governmentBlacklist.toSet()
        val enabledHeartlands = kingdom.getHeartlands().filter { it.id !in heartlandBlacklist }
        val enabledCharters = kingdom.getCharters().filter { it.id !in charterBlacklist }
        val enabledGovernments = kingdom.getGovernments().filter { it.id !in governmentBlacklist }
        val notesContext = kingdom.notes.toContext()
        val government = kingdom.getChosenGovernment()
        val heartland = kingdom.getChosenHeartland()
        val charter = kingdom.getChosenCharter()
        val trainedSkills = kingdom.getTrainedSkills(chosenFeats, government)
        val resourceDiceNum = kingdom.getResourceDiceAmount(
            chosenFeats,
            settlements.allSettlements,
            kingdomLevel = kingdom.level,
        )
        val initialProficiencies = (0..3).map { index ->
            val proficiency = kingdom.initialProficiencies.getOrNull(index)
                ?.let { KingdomSkill.fromString(it) }
            val result = Select(
                name = "initialProficiencies.$index",
                label = t("kingdom.skillTraining"),
                value = proficiency?.value,
                options = KingdomSkill.entries.filter { it == proficiency || it !in trainedSkills }
                    .map { SelectOption(value = it.value, label = t(it)) },
                required = false,
                hideLabel = true,
            ).toContext()
            result
        }.toTypedArray()
        val currentSceneId = game.scenes.current?.id
        val allSettlementSceneIds = kingdom.settlements.map { it.sceneId }.toSet()
        val canAddCurrentScene = currentSceneId != null && currentSceneId !in allSettlementSceneIds
        val kingdomSkillRanks = kingdom.parseSkillRanks(
            chosenFeatures = chosenFeatures,
            chosenFeats = chosenFeats,
            government = government,
        )
        val activities = toActivitiesContext(
            activities = kingdom.getAllActivities(),
            activityBlacklist = kingdom.activityBlacklist.toSet(),
            unlockedActivities = globalBonuses.unlockedActivities,
            allowCapitalInvestment = settlements.current?.allowCapitalInvestment == true,
            kingdomSkillRanks = kingdomSkillRanks,
            chosenFeatures = chosenFeatures,
            openedDetails = openedDetails,
            kingdom = kingdom,
            chosenFeats = chosenFeats,
            activeLeader = game.getActiveLeader(),
        )
        val leadersContext = kingdom.leaders.toContext(
            leaderActors = leaderActors,
            bonuses = defaultLeaderBonuses,
            vacancies = vacancies,
        )
        val automateStats = kingdom.settings.automateStats
        val checks = skillChecks(
            kingdom = kingdom,
            settlements = settlements,
            skillRanks = kingdomSkillRanks,
        )

        val abilityScores = kingdom.parseAbilityScores(
            chosenCharter = charter,
            chosenHeartland = heartland,
            chosenGovernment = government,
            chosenFeatures = chosenFeatures,
        )
        val automateResources = kingdom.settings.automateResources != AutomateResources.MANUAL.value
        val isGM = game.user.isGM
        val ongoingEvents = kingdom.getOngoingEvents().toContext(
            openedDetails = openedDetails,
            isGM = isGM,
            settlements = settlements
        )
        val activeLeaderContext = Select.fromEnum<Leader>(
            name = "activeLeader",
            label = t("kingdom.activeLeader"),
            required = false,
            value = game.getActiveLeader(),
            labelClasses = listOf("km-slim-inputs"),
        ).toContext()
        KingdomSheetContext(
            partId = parent.partId,
            isFormValid = true,
            kingdomSectionNav = kingdomSectionNav,
            kingdomNameInput = kingdomNameInput.toContext(),
            settlementInput = settlementInput.toContext(),
            xpInput = xpInput.toContext(),
            xpThresholdInput = xpThresholdInput.toContext(),
            levelInput = levelInput.toContext(),
            fameContext = kingdom.fame.toContext(kingdom.settings.maximumFamePoints),
            atWarInput = atWarInput.toContext(),
            unrestInput = unrestInput.toContext(),
            controlDc = controlDc,
            unrestPenalty = unrestPenalty,
            anarchyAt = anarchyAt,
            ruinContext = kingdom.ruin.toContext(
                automateStats,
                kingdom.parseRuins(
                    choices = chosenFeatures,
                    baseThreshold = kingdom.settings.ruinThreshold,
                    government = kingdom.government,
                )
            ),
            commoditiesContext = kingdom.commodities.toContext(storage),
            worksitesContext = kingdom.workSites.toContext(realm.worksites, automateResources),
            sizeInput = sizeInput.toContext(),
            size = realm.size,
            kingdomSize = t(realm.sizeInfo.type),
            resourcePointsContext = kingdom.resourcePoints.toContext("resourcePoints", t("kingdom.resourcePoints")),
            resourceDiceContext = kingdom.resourceDice.toContext("resourceDice", t("kingdom.resourceDice")),
            consumptionContext = kingdom.consumption.toContext(kingdom.settings.autoCalculateArmyConsumption),
            supernaturalSolutionsInput = supernaturalSolutionsInput.toContext(),
            creativeSolutionsInput = creativeSolutionsInput.toContext(),
            notesContext = notesContext,
            leadersContext = leadersContext,
            charter = kingdom.charter.toContext(enabledCharters),
            heartland = kingdom.heartland.toContext(enabledHeartlands),
            government = kingdom.government.toContext(enabledGovernments, feats),
            abilityBoosts = kingdom.abilityBoosts.toContext("", 2 + increaseScorePicksBy),
            currentNavEntry = currentNavEntry.value,
            hideCreation = currentCharacterSheetNavEntry != "Creation",
            hideBonus = currentCharacterSheetNavEntry != "Bonus",
            settings = kingdom.settings,
            featuresByLevel = kingdom.features.toContext(
                government = government,
                features = allFeatures.toTypedArray(),
                feats = feats,
                increaseBoostsBy = increaseScorePicksBy,
                navigationEntry = currentCharacterSheetNavEntry,
                bonusFeats = kingdom.bonusFeats,
                trainedSkills = trainedSkills,
                chosenFeats = chosenFeats,
                abilityScores = abilityScores,
                skillRanks = kingdomSkillRanks,
            )
                .sortedBy { it.level }
                .toTypedArray(),
            bonusFeat = createBonusFeatContext(
                government = government,
                feats = feats,
                choices = kingdom.features,
                bonusFeats = kingdom.bonusFeats,
                value = bonusFeat,
                trainedSkills = trainedSkills,
                chosenFeats = chosenFeats,
                abilityScores = abilityScores,
                skillRanks = kingdomSkillRanks,
            ),
            bonusFeats = kingdom.bonusFeats.toContext(
                kingdom.getFeats(),
                chosenFeats = chosenFeats,
                abilityScores = abilityScores,
                skillRanks = kingdomSkillRanks,
            ),
            groups = kingdom.groups.toContext(),
            abilityScores = kingdom.abilityScores.toContext(abilityScores, automateStats),
            skillRanks = kingdom.skillRanks.toContext(),
            milestones = kingdom.milestones.toContext(
                kingdom.getMilestones(),
                isGM,
                kingdom.settings.cultOfTheBloomEvents
            ),
            isGM = isGM,
            actor = actor,
            modifiers = kingdom.modifiers.toContext(),
            mainNav = createMainNav(kingdom),
            initialProficiencies = initialProficiencies,
            enableLeadershipModifiers = kingdom.settings.enableLeadershipModifiers,
            settlements = kingdom.settlements.toContext(
                game,
                kingdom.settings.autoCalculateSettlementLevel,
                kingdom.settings.kingdomAllStructureItemBonusesStack,
                kingdom.settings.capitalInvestmentInCapital,
            ),
            canAddCurrentSceneAsSettlement = canAddCurrentScene,
            turnSectionNav = createTabs<TurnNavEntry>("scroll-to"),
            canLevelUp = kingdom.canLevelUp(),
            vkXp = kingdom.settings.vanceAndKerensharaXP,
            activities = activities,
            cultOfTheBloomEvents = kingdom.settings.cultOfTheBloomEvents,
            ongoingEvents = ongoingEvents,
            eventDC = getEventDC(kingdom),
            cultEventDC = getCultEventDC(kingdom),
            civicPlanning = kingdom.level >= 12,
            heartlandLabel = heartland?.name,
            leadershipActivities = if (globalBonuses.increaseLeadershipActivities) 3 else 2,
            collectTaxesReduceUnrestDisabled = kingdom.unrest <= 0,
            consumption = consumption.total,
            automateStats = automateStats,
            resourceDiceIncome = "$resourceDiceNum${realm.sizeInfo.resourceDieSize.value}",
            skillChecks = checks,
            automateResources = automateResources,
            useLeadershipModifiers = kingdom.settings.enableLeadershipModifiers,
            activeSettlementType = settlements.current?.size?.type?.value ?: "none",
            actorUuid = actor.uuid,
            activeLeader = activeLeaderContext,
        )
    }

    private fun getCultEventDC(kingdom: KingdomData): Int =
        max(1, kingdom.settings.cultEventDc - kingdom.turnsWithoutCultEvent * kingdom.settings.cultEventDcStep)

    private fun getEventDC(kingdom: KingdomData): Int =
        max(1, kingdom.settings.eventDc - kingdom.turnsWithoutEvent * kingdom.settings.eventDcStep)

    private fun createMainNav(kingdom: KingdomData): Array<NavEntryContext> {
        val tradeAgreements = kingdom.groups.count { it.relations == Relations.TRADE_AGREEMENT.value }
        return MainNavEntry.entries.map {
            val postfix = when (it) {
                MainNavEntry.TRADE_AGREEMENTS -> " ($tradeAgreements)"
                MainNavEntry.SETTLEMENTS -> {
                    val size = kingdom.settlements.mapNotNull { game.scenes.get(it.sceneId) }.size
                    " ($size)"
                }

                MainNavEntry.MODIFIERS -> " (${kingdom.modifiers.size})"
                else -> ""
            }
            NavEntryContext(
                label = "${t(it)}$postfix",
                active = currentNavEntry == it,
                link = it.value,
                title = t(it),
                action = "change-nav",
            )
        }
            .toTypedArray()
    }

    private fun createKingdomSectionNav(): Array<NavEntryContext> {
        return (1..20).map { it.toString() }
            .map {
                NavEntryContext(
                    label = it,
                    active = currentCharacterSheetNavEntry == it,
                    link = it,
                    title = "Level: $it",
                    action = "change-kingdom-section-nav",
                )
            }
            .toTypedArray()
    }

    override fun _attachPartListeners(partId: String, htmlElement: HTMLElement, options: ApplicationRenderOptions) {
        super._attachPartListeners(partId, htmlElement, options)
        htmlElement.querySelectorAll(".km-gain-lose").asList()
            .filterIsInstance<HTMLElement>()
            .forEach { elem ->
                elem.addEventListener("click", {
                    actor.getKingdom()?.let { kingdom ->
                        buildPromise {
                            val activityId = elem.closest(".km-kingdom-activity")
                                ?.takeIfInstance<HTMLElement>()
                                ?.dataset["activityId"]
                            executeResourceButton(
                                game = game,
                                actor = actor,
                                kingdom = kingdom,
                                elem = elem,
                                activityId = activityId,
                            )
                        }
                    }
                })
            }
        // need to manually keep track of opened details because fucking foundry
        // re-renders the entire dom when you change anything, thereby closing all details
        htmlElement.querySelectorAll(".km-kingdom-details > details > summary").asList()
            .filterIsInstance<HTMLElement>()
            .forEach { elem ->
                elem.addEventListener("click", {
                    if (it.target.unsafeCast<HTMLElement>().classList.contains("km-detail-label")) {
                        it.preventDefault()
                        it.stopPropagation()
                        val id = (it.currentTarget as HTMLElement).dataset["id"] ?: ""
                        if (id in openedDetails) {
                            openedDetails.remove(id)
                        } else {
                            openedDetails.add(id)
                        }
                        render()
                    }
                })
            }
    }

    override fun onParsedSubmit(value: KingdomSheetData): Promise<Void> = buildPromise {
        if (isFormValid) {
            val previousKingdom = getKingdom()
            val kingdom = deepClone(previousKingdom)
            kingdom.name = value.name
            kingdom.atWar = value.atWar
            kingdom.fame = value.fame
            kingdom.level = value.level
            kingdom.xp = value.xp
            kingdom.xpThreshold = value.xpThreshold
            kingdom.unrest = value.unrest
            kingdom.ruin = value.ruin
            kingdom.commodities = value.commodities
            kingdom.workSites = value.workSites
            kingdom.size = value.size
            kingdom.resourcePoints = value.resourcePoints
            kingdom.resourceDice = value.resourceDice
            kingdom.activeSettlement = value.activeSettlement
            kingdom.supernaturalSolutions = value.supernaturalSolutions
            kingdom.creativeSolutions = value.creativeSolutions
            kingdom.consumption = if (kingdom.settings.autoCalculateArmyConsumption) {
                RawConsumption.copy(value.consumption, armies = kingdom.consumption.armies)
            } else {
                value.consumption
            }
            kingdom.charter = value.charter
            kingdom.heartland = value.heartland
            kingdom.government = value.government
            kingdom.abilityBoosts = value.abilityBoosts
            kingdom.features = value.features
            kingdom.bonusFeats = value.bonusFeats
            kingdom.leaders = value.leaders
            kingdom.groups = value.groups
            kingdom.skillRanks = value.skillRanks
            kingdom.abilityScores = value.abilityScores
            kingdom.milestones = value.milestones
            kingdom.notes = value.notes
            kingdom.initialProficiencies = value.initialProficiencies
            beforeKingdomUpdate(previousKingdom, kingdom)
            actor.setKingdom(kingdom)
            bonusFeat = value.bonusFeat
            game.settings.pfrpg2eKingdomCampingWeather.setKingdomActiveLeader(value.activeLeader)
        }
        null
    }
}

suspend fun openOrCreateKingdomSheet(game: Game, dispatcher: ActionDispatcher, actor: KingdomActor) {
    if (actor.getKingdom() == null) {
        actor.setKingdom(createKingdomDefaults(t("kingdom.newKingdom")))
        openJournal("Compendium.pf2e-kingmaker-tools.kingmaker-tools-journals.JournalEntry.FwcyYZARAnOHlKkE")
    }
    KingdomSheet(game, actor, dispatcher).launch()
}