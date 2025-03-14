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
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.data.checks.RollMode
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.Relations
import at.posselt.pfrpg2e.data.kingdom.calculateControlDC
import at.posselt.pfrpg2e.data.kingdom.calculateHexXP
import at.posselt.pfrpg2e.data.kingdom.calculateRpXP
import at.posselt.pfrpg2e.data.kingdom.findKingdomSize
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.kingdom.KingdomActor
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.OngoingEvent
import at.posselt.pfrpg2e.kingdom.RawEq
import at.posselt.pfrpg2e.kingdom.RawModifier
import at.posselt.pfrpg2e.kingdom.RawSome
import at.posselt.pfrpg2e.kingdom.data.RawGroup
import at.posselt.pfrpg2e.kingdom.data.endTurn
import at.posselt.pfrpg2e.kingdom.data.getChosenFeats
import at.posselt.pfrpg2e.kingdom.data.getChosenFeatures
import at.posselt.pfrpg2e.kingdom.data.getChosenGovernment
import at.posselt.pfrpg2e.kingdom.data.getChosenHeartland
import at.posselt.pfrpg2e.kingdom.dialogs.ActivityManagement
import at.posselt.pfrpg2e.kingdom.dialogs.AddModifier
import at.posselt.pfrpg2e.kingdom.dialogs.CharterManagement
import at.posselt.pfrpg2e.kingdom.dialogs.CheckType
import at.posselt.pfrpg2e.kingdom.dialogs.FeatManagement
import at.posselt.pfrpg2e.kingdom.dialogs.GovernmentManagement
import at.posselt.pfrpg2e.kingdom.dialogs.HeartlandManagement
import at.posselt.pfrpg2e.kingdom.dialogs.InspectSettlement
import at.posselt.pfrpg2e.kingdom.dialogs.KingdomSettingsApplication
import at.posselt.pfrpg2e.kingdom.dialogs.MilestoneManagement
import at.posselt.pfrpg2e.kingdom.dialogs.StructureBrowser
import at.posselt.pfrpg2e.kingdom.dialogs.armyBrowser
import at.posselt.pfrpg2e.kingdom.dialogs.armyTacticsBrowser
import at.posselt.pfrpg2e.kingdom.dialogs.kingdomCheckDialog
import at.posselt.pfrpg2e.kingdom.dialogs.structureXpDialog
import at.posselt.pfrpg2e.kingdom.getActivity
import at.posselt.pfrpg2e.kingdom.getAllActivities
import at.posselt.pfrpg2e.kingdom.getAllSettlements
import at.posselt.pfrpg2e.kingdom.getCharters
import at.posselt.pfrpg2e.kingdom.getEnabledFeatures
import at.posselt.pfrpg2e.kingdom.getExplodedFeatures
import at.posselt.pfrpg2e.kingdom.getFeats
import at.posselt.pfrpg2e.kingdom.getGovernments
import at.posselt.pfrpg2e.kingdom.getHeartlands
import at.posselt.pfrpg2e.kingdom.getKingdom
import at.posselt.pfrpg2e.kingdom.getMilestones
import at.posselt.pfrpg2e.kingdom.getRealmData
import at.posselt.pfrpg2e.kingdom.getTrainedSkills
import at.posselt.pfrpg2e.kingdom.hasLeaderUuid
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.calculateInvestedBonus
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.getHighestLeadershipModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.evaluateGlobalBonuses
import at.posselt.pfrpg2e.kingdom.modifiers.penalties.calculateUnrestPenalty
import at.posselt.pfrpg2e.kingdom.parse
import at.posselt.pfrpg2e.kingdom.parseLeaderActors
import at.posselt.pfrpg2e.kingdom.parseSkillRanks
import at.posselt.pfrpg2e.kingdom.resources.calculateConsumption
import at.posselt.pfrpg2e.kingdom.resources.calculateStorage
import at.posselt.pfrpg2e.kingdom.setKingdom
import at.posselt.pfrpg2e.kingdom.sheet.contexts.KingdomSheetContext
import at.posselt.pfrpg2e.kingdom.sheet.contexts.NavEntryContext
import at.posselt.pfrpg2e.kingdom.sheet.contexts.createBonusFeatContext
import at.posselt.pfrpg2e.kingdom.sheet.contexts.createNavEntries
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
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.d20Check
import at.posselt.pfrpg2e.utils.launch
import at.posselt.pfrpg2e.utils.openJournal
import at.posselt.pfrpg2e.utils.postChatMessage
import at.posselt.pfrpg2e.utils.postChatTemplate
import at.posselt.pfrpg2e.utils.roll
import at.posselt.pfrpg2e.utils.rollWithCompendiumFallback
import com.foundryvtt.core.Actor
import com.foundryvtt.core.Game
import com.foundryvtt.core.applications.api.ApplicationRenderOptions
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.documents.onCreateDrawing
import com.foundryvtt.core.documents.onCreateTile
import com.foundryvtt.core.documents.onCreateToken
import com.foundryvtt.core.documents.onDeleteDrawing
import com.foundryvtt.core.documents.onDeleteScene
import com.foundryvtt.core.documents.onDeleteTile
import com.foundryvtt.core.documents.onDeleteToken
import com.foundryvtt.core.documents.onUpdateDrawing
import com.foundryvtt.core.documents.onUpdateItem
import com.foundryvtt.core.documents.onUpdateTile
import com.foundryvtt.core.documents.onUpdateToken
import com.foundryvtt.core.onApplyTokenStatusEffect
import com.foundryvtt.core.onCanvasReady
import com.foundryvtt.core.onSightRefresh
import com.foundryvtt.core.onUpdateActor
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
    classes = arrayOf("km-kingdom-sheet"),
    id = "kmKingdomSheet-${actor.uuid}",
    width = 970,
    controls = arrayOf(
        MenuControl(label = "Show Players", action = "show-players", gmOnly = true),
        MenuControl(label = "Activities", action = "configure-activities", gmOnly = true),
        MenuControl(label = "Charters", action = "configure-charters", gmOnly = true),
        MenuControl(label = "Governments", action = "configure-governments", gmOnly = true),
        MenuControl(label = "Feats", action = "configure-feats", gmOnly = true),
        MenuControl(label = "Heartlands", action = "configure-heartlands", gmOnly = true),
        MenuControl(label = "Milestones", action = "configure-milestones", gmOnly = true),
        MenuControl(label = "Settings", action = "settings", gmOnly = true),
        MenuControl(label = "Help", action = "help"),
    ),
    scrollable = arrayOf(
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
    private var ongoingEvent: String? = null
    private val openedActivityDetails = mutableSetOf<String>()

    init {
        actor.apps[id] = this
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
        appHook.onSightRefresh { _ -> render() } // end of drag movement
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
                val realm = game.getRealmData(kingdom)
                kingdom.groups = kingdom.groups + RawGroup(
                    name = "Group Name",
                    negotiationDC = 10 + findKingdomSize(realm.size).controlDCModifier,
                    atWar = false,
                    relations = "none",
                )
                actor.setKingdom(kingdom)
            }

            "delete-group" -> buildPromise {
                val index = target.dataset["index"]?.toInt() ?: 0
                val kingdom = getKingdom()
                kingdom.groups = kingdom.groups.filterIndexed { idx, _ -> idx != index }.toTypedArray()
                actor.setKingdom(kingdom)
            }

            "add-ongoing-event" -> {
                val kingdom = getKingdom()
                buildPromise {
                    val event = ongoingEvent
                    if (event != null) {
                        kingdom.ongoingEvents = kingdom.ongoingEvents + OngoingEvent(name = event)
                        ongoingEvent = null
                        actor.setKingdom(kingdom)
                    }
                }
            }

            "configure-activities" -> ActivityManagement(kingdomActor = actor).launch()
            "configure-milestones" -> MilestoneManagement(kingdomActor = actor).launch()
            "configure-charters" -> CharterManagement(kingdomActor = actor).launch()
            "configure-governments" -> GovernmentManagement(kingdomActor = actor).launch()
            "configure-heartlands" -> HeartlandManagement(kingdomActor = actor).launch()
            "configure-feats" -> FeatManagement(kingdomActor = actor).launch()
            "structures-import" -> buildPromise { importStructures() }

            "settlement-import" -> {
                buildPromise {
                    when (target.dataset["waterBorders"]) {
                        "1" -> importSettlement("Settlement - 1 Water Border", 1)
                        "2" -> importSettlement("Settlement - 2 Water Borders", 2)
                        "3" -> importSettlement("Settlement - 3 Water Borders", 3)
                        "4" -> importSettlement("Settlement - 4 Water Borders", 4)
                        else -> importSettlement("Settlement - No Water Borders", 0)
                    }
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
                        feats = kingdom.getChosenFeats(kingdom.getChosenFeatures(kingdom.getExplodedFeatures()))
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
                            kingdom.settings = it
                            actor.setKingdom(kingdom)
                        },
                        kingdomSettings = kingdom.settings
                    ).launch()
                }
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
                            kingdomSize = game.getRealmData(kingdom).size,
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

            "remove-ongoing-event" -> buildPromise {
                actor.getKingdom()?.let { kingdom ->
                    target.dataset["index"]?.toInt()?.let { index ->
                        kingdom.ongoingEvents = kingdom.ongoingEvents
                            .filterIndexed { index, _ -> index != index }
                            .toTypedArray()
                        actor.setKingdom(kingdom)
                    }
                }
            }

            "check-cult-event" -> buildPromise {
                actor.getKingdom()?.let { kingdom ->
                    val dc = getCultEventDC(kingdom)
                    val rollMode = RollMode.fromString(kingdom.settings.kingdomEventRollMode)
                    val succeeded = d20Check(
                        dc = dc,
                        flavor = "Checking for Cult Event with Flat DC $dc",
                        rollMode = rollMode,
                    ).degreeOfSuccess.succeeded()
                    if (succeeded) {
                        kingdom.turnsWithoutCultEvent = 0
                        postChatMessage("Cult Event occurs, roll a Cult Event", rollMode = rollMode)
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
                        flavor = "Checking for Kingdom Event with Flat DC $dc",
                        rollMode = rollMode,
                    ).degreeOfSuccess.succeeded()
                    if (succeeded) {
                        kingdom.turnsWithoutEvent = 0
                        postChatMessage("Kingdom Event occurs, roll a Kingdom Event", rollMode = rollMode)
                    } else {
                        kingdom.turnsWithoutEvent += 1
                    }
                    actor.setKingdom(kingdom)
                }
            }

            "roll-cult-event" -> buildPromise {
                val uuid = actor.getKingdom()
                    ?.settings
                    ?.kingdomCultTable
                val rollMode = game.settings.pfrpg2eKingdomCampingWeather.getKingdomEventRollMode()
                game.rollWithCompendiumFallback(
                    tableName = "Random Cult Events",
                    rollMode = rollMode,
                    uuid = uuid,
                )
            }

            "roll-event" -> buildPromise {
                val uuid = actor.getKingdom()
                    ?.settings
                    ?.kingdomEventsTable
                val rollMode = game.settings.pfrpg2eKingdomCampingWeather.getKingdomEventRollMode()
                game.rollWithCompendiumFallback(
                    tableName = "Random Kingdom Events",
                    rollMode = rollMode,
                    uuid = uuid,
                )
            }

            "claimed-refuge" -> buildPromise {
                actor.getKingdom()?.let { kingdom ->
                    kingdom.modifiers = kingdom.modifiers + RawModifier(
                        id = v4(),
                        turns = 2,
                        name = "Claimed Refuge",
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
                        name = "Claimed Landmark",
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
                        .map { ResourceButton(value = "1", resource = it, mode = ResourceMode.LOSE).toHtml() }
                        .toTypedArray()
                    postChatTemplate(
                        templatePath = "chatmessages/landmark.hbs",
                        templateContext = recordOf("buttons" to ruinButtons)
                    )
                    actor.setKingdom(kingdom)
                }
            }

            "gain-fame" -> buildPromise {
                actor.getKingdom()?.let { kingdom ->
                    kingdom.fame.now = (kingdom.fame.now + 1).coerceIn(0, kingdom.settings.maximumFamePoints)
                    postChatMessage("Gaining 1 Fame")
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
                    val realm = game.getRealmData(kingdom)
                    val settlements = kingdom.getAllSettlements(game)
                    val allFeatures = kingdom.getExplodedFeatures()
                    val chosenFeatures = kingdom.getChosenFeatures(allFeatures)
                    val chosenFeats = kingdom.getChosenFeats(chosenFeatures)
                    val resources = collectResources(
                        kingdomData = kingdom,
                        realmData = realm,
                        allFeats = chosenFeats,
                        settlements = settlements.allSettlements,
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
                    val realm = game.getRealmData(kingdom)
                    val settlements = kingdom.getAllSettlements(game)
                    kingdom.commodities.now.food = payConsumption(
                        availableFood = kingdom.commodities.now.food,
                        settlements = settlements.allSettlements,
                        realmData = realm,
                        armyConsumption = kingdom.consumption.armies,
                    )
                    actor.setKingdom(kingdom)
                }
            }

            "end-turn" -> buildPromise {
                actor.getKingdom()?.let { kingdom ->
                    val realm = game.getRealmData(kingdom)
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
                            it.copy(turns = turns - 1)
                        }
                    }.toTypedArray()
                    actor.setKingdom(kingdom)
                }
                postChatTemplate(templatePath = "chatmessages/end-turn.hbs")
            }

            "skip-collect-taxes" -> buildPromise {
                actor.getKingdom()?.let { kingdom ->
                    val succeeded = d20Check(
                        dc = 11,
                        flavor = "Trying to reduce unrest on a Flat Check 11",
                    ).degreeOfSuccess.succeeded()
                    if (succeeded && kingdom.unrest > 0) {
                        postChatMessage("Reducing Unrest by 1")
                        kingdom.unrest = (kingdom.unrest - 1).coerceIn(0, Int.MAX_VALUE)
                    }
                    actor.setKingdom(kingdom)
                }
            }

            "perform-activity" -> buildPromise {
                val activityId = target.dataset["activity"]
                checkNotNull(activityId)
                val kingdom = actor.getKingdom()
                checkNotNull(kingdom)
                val activity = kingdom.getActivity(activityId)
                checkNotNull(activity)
                actor.getKingdom()?.let { kingdom ->
                    when (activityId) {
                        "build-structure" -> {
                            StructureBrowser(
                                actor = actor,
                                kingdom = kingdom,
                                structures = game.getImportedStructures(),
                                game = game,
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
                        else -> kingdomCheckDialog(
                            game = game,
                            kingdom = kingdom,
                            kingdomActor = actor,
                            check = CheckType.PerformActivity(activity),
                        )
                    }
                }
            }
        }
    }

    suspend fun importStructures() {
        if (game.importStructures().isNotEmpty()) {
            ui.notifications.info("Imported Structures into Structures folder")
        }
    }

    suspend fun importSettlement(sceneName: String, waterBorders: Int) {
        game.importSettlementScene(sceneName, waterBorders)?.id?.let {
            val kingdom = getKingdom()
            kingdom.settlements = kingdom.settlements + RawSettlement(
                sceneId = it,
                lots = 1,
                level = 1,
                type = "capital",
                secondaryTerritory = false,
                manualSettlementLevel = false,
                waterBorders = waterBorders,
            )
            actor.setKingdom(kingdom)
            ui.notifications.info("Imported a predefined scene as Capital")
        }
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<KingdomSheetContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val kingdom = getKingdom()
        val vacancies = kingdom.vacancies()
        val realm = game.getRealmData(kingdom)
        val controlDc = calculateControlDC(kingdom.level, realm, vacancies.ruler)
        val settlements = kingdom.getAllSettlements(game)
        val globalBonuses = evaluateGlobalBonuses(settlements.allSettlements)
        val allFeatures = kingdom.getExplodedFeatures()
        val chosenFeatures = kingdom.getChosenFeatures(allFeatures)
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
        ).total
        val kingdomNameInput = TextInput(
            name = "name",
            label = "Name",
            value = kingdom.name,
        )
        val settlementInput = Select(
            name = "activeSettlement",
            label = "Active Settlement",
            value = kingdom.activeSettlement,
            options = settlements.allSettlements.map { SelectOption(it.name, it.id) },
            required = false
        )
        val xpInput = NumberInput(
            name = "xp",
            label = "XP",
            hideLabel = true,
            value = kingdom.xp,
        )
        val xpThresholdInput = NumberInput(
            name = "xpThreshold",
            label = "XP Threshold",
            hideLabel = true,
            value = kingdom.xpThreshold,
        )
        val levelInput = Select.range(
            name = "level",
            label = "Level",
            value = kingdom.level,
            from = 1,
            to = 20,
        )
        val atWarInput = CheckboxInput(
            name = "atWar",
            value = kingdom.atWar,
            label = "At War"
        )
        val anarchyAt = calculateAnarchy(chosenFeats)
        val unrestInput = Select.range(
            name = "unrest",
            label = "Unrest",
            value = kingdom.unrest,
            from = 0,
            to = anarchyAt
        )
        val sizeInput = NumberInput(
            name = "size",
            label = "Size",
            value = kingdom.size,
        )
        val supernaturalSolutionsInput = NumberInput(
            name = "supernaturalSolutions",
            value = kingdom.supernaturalSolutions,
            label = "Supernatural Solutions"
        )
        val creativeSolutionsInput = NumberInput(
            name = "creativeSolutions",
            value = kingdom.creativeSolutions,
            label = "Creative Solutions"
        )
        val ongoingEventInput = TextInput(
            name = "ongoingEvent",
            label = "Ongoing Event",
            value = ongoingEvent ?: "",
            required = false,
        )
        val unrestPenalty = calculateUnrestPenalty(kingdom.unrest)
        val feats = kingdom.getFeats()
        val increaseScorePicksBy = kingdom.settings.increaseScorePicksBy
        val kingdomSectionNav = createKingdomSectionNav(kingdom)
        val heartlandBlacklist = kingdom.heartlandBlacklist.toSet()
        val charterBlacklist = kingdom.charterBlacklist.toSet()
        val governmentBlacklist = kingdom.governmentBlacklist.toSet()
        val enabledHeartlands = kingdom.getHeartlands().filter { it.id !in heartlandBlacklist }
        val enabledCharters = kingdom.getCharters().filter { it.id !in charterBlacklist }
        val enabledGovernments = kingdom.getGovernments().filter { it.id !in governmentBlacklist }
        val notesContext = kingdom.notes.toContext()
        val government = kingdom.getChosenGovernment()
        val heartland = kingdom.getChosenHeartland()
        val trainedSkills = kingdom.getTrainedSkills(chosenFeats, government)
        val initialProficiencies = (0..3).map { index ->
            val proficiency = kingdom.initialProficiencies.getOrNull(index)
                ?.let { KingdomSkill.fromString(it) }
            val result = Select(
                name = "initialProficiencies.$index",
                label = "Skill Training",
                value = proficiency?.value,
                options = KingdomSkill.entries.filter { it == proficiency || it !in trainedSkills }
                    .map { SelectOption(value = it.value, label = it.label) },
                required = false,
                hideLabel = true,
            ).toContext()
            result
        }.toTypedArray()
        val currentSceneId = game.scenes.current?.id
        val allSettlementSceneIds = kingdom.settlements.map { it.sceneId }.toSet()
        val canAddCurrentScene = currentSceneId != null && currentSceneId !in allSettlementSceneIds
        val activities = toActivitiesContext(
            activities = kingdom.getAllActivities(),
            activityBlacklist = kingdom.activityBlacklist.toSet(),
            unlockedActivities = globalBonuses.unlockedActivities,
            kingdomLevel = kingdom.level,
            allowCapitalInvestment = settlements.current?.allowCapitalInvestment == true,
            kingdomSkillRanks = kingdom.parseSkillRanks(
                chosenFeatures = chosenFeatures,
                chosenFeats = chosenFeats,
                government = government,
            ),
            ignoreSkillRequirements = kingdom.settings.kingdomIgnoreSkillRequirements,
            enabledFeatures = kingdom.getEnabledFeatures(),
            openedActivityDetails = openedActivityDetails,
        )
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
            ruinContext = kingdom.ruin.toContext(),
            commoditiesContext = kingdom.commodities.toContext(storage),
            worksitesContext = kingdom.workSites.toContext(realm.worksites),
            sizeInput = sizeInput.toContext(),
            resourcePointsContext = kingdom.resourcePoints.toContext("resourcePoints"),
            resourceDiceContext = kingdom.resourceDice.toContext("resourceDice"),
            consumptionContext = kingdom.consumption.toContext(kingdom.settings.autoCalculateArmyConsumption),
            supernaturalSolutionsInput = supernaturalSolutionsInput.toContext(),
            creativeSolutionsInput = creativeSolutionsInput.toContext(),
            notesContext = notesContext,
            leadersContext = kingdom.leaders.toContext(leaderActors, defaultLeaderBonuses),
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
            ),
            bonusFeats = kingdom.bonusFeats.toContext(
                kingdom.getFeats(),
            ),
            groups = kingdom.groups.toContext(),
            abilityScores = kingdom.abilityScores.toContext(),
            skillRanks = kingdom.skillRanks.toContext(),
            milestones = kingdom.milestones.toContext(kingdom.getMilestones(), game.user.isGM),
            ongoingEvent = ongoingEventInput.toContext(),
            isGM = game.user.isGM,
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
            turnSectionNav = createNavEntries<TurnNavEntry>(),
            canLevelUp = kingdom.canLevelUp(),
            vkXp = kingdom.settings.vanceAndKerensharaXP,
            activities = activities,
            cultOfTheBloomEvents = kingdom.settings.cultOfTheBloomEvents,
            ongoingEvents = kingdom.ongoingEvents.map { it.name }.toTypedArray(),
            eventDC = getEventDC(kingdom),
            cultEventDC = getCultEventDC(kingdom),
            civicPlanning = kingdom.level >= 12,
            heartlandLabel = heartland?.name,
            leadershipActivities = if (globalBonuses.increaseLeadershipActivities) 3 else 2,
            ongoingEventButtonDisabled = ongoingEvent.isNullOrEmpty(),
            collectTaxesReduceUnrestDisabled = kingdom.unrest <= 0,
            consumption = consumption,
        )
    }

    private fun getCultEventDC(kingdom: KingdomData): Int = max(1, 20 - kingdom.turnsWithoutCultEvent * 2)

    private fun getEventDC(kingdom: KingdomData): Int = max(1, 16 - kingdom.turnsWithoutEvent * 5)

    private fun createMainNav(kingdom: KingdomData): Array<NavEntryContext> {
        val tradeAgreements = kingdom.groups.count { it.relations == Relations.TRADE_AGREEMENT.value }
        return MainNavEntry.entries.map {
            NavEntryContext(
                label = if (it == MainNavEntry.TRADE_AGREEMENTS) "${it.label} ($tradeAgreements)" else it.label,
                active = currentNavEntry == it,
                link = it.value,
                title = it.label,
            )
        }
            .toTypedArray()
    }

    private fun createKingdomSectionNav(kingdom: KingdomData): Array<NavEntryContext> {
        val selectLv1 = currentCharacterSheetNavEntry != "Creation"
                && currentCharacterSheetNavEntry != "Bonus"
                && currentCharacterSheetNavEntry.toInt() > kingdom.level
        return (1..20).map { it.toString() }
            .map {
                NavEntryContext(
                    label = it,
                    active = (selectLv1 && it == "1") || currentCharacterSheetNavEntry == it,
                    link = it,
                    title = "Level: $it",
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
                            executeResourceButton(
                                game = game,
                                actor = actor,
                                kingdom = kingdom,
                                elem = elem,
                            )
                        }
                    }
                })
            }
        // need to manually keep track of opened details because fucking foundry
        // re-renders the entire dom when you change anything, thereby closing all details
        htmlElement.querySelectorAll(".km-kingdom-activity > details > summary").asList()
            .filterIsInstance<HTMLElement>()
            .forEach { elem ->
                elem.addEventListener("click", {
                    it.preventDefault()
                    it.stopPropagation()
                    val activityId = (it.currentTarget as HTMLElement).dataset["activity"] ?: ""
                    console.log(activityId)
                    if (activityId in openedActivityDetails) {
                        openedActivityDetails.remove(activityId)
                    } else {
                        openedActivityDetails.add(activityId)
                    }
                    render()
                })
            }
    }

    override fun onParsedSubmit(value: KingdomSheetData): Promise<Void> = buildPromise {
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
            value.consumption.copy(armies = kingdom.consumption.armies)
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
        ongoingEvent = value.ongoingEvent
        null
    }
}

suspend fun newKingdomActor() =
    KingdomActor.create(
        recordOf(
            "type" to "npc",
            "name" to "Kingdom Sheet",
            "img" to "icons/environment/settlement/castle.webp",
            "ownership" to recordOf(
                "default" to 3
            )
        )
    ).await()

suspend fun openOrCreateKingdomSheet(game: Game, dispatcher: ActionDispatcher, actor: KingdomActor) {
    if (actor.getKingdom() == null) {
        actor.setKingdom(createKingdomDefaults("New Kingdom"))
        openJournal("Compendium.pf2e-kingmaker-tools.kingmaker-tools-journals.JournalEntry.FwcyYZARAnOHlKkE")
    }
    KingdomSheet(game, actor, dispatcher).launch()
}