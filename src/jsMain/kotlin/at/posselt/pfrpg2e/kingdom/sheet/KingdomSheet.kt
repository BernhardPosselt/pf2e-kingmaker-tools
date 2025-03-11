package at.posselt.pfrpg2e.kingdom.sheet

import at.posselt.pfrpg2e.actions.ActionDispatcher
import at.posselt.pfrpg2e.actions.ActionMessage
import at.posselt.pfrpg2e.actions.handlers.OpenKingdomSheetAction
import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.MenuControl
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.app.prompt
import at.posselt.pfrpg2e.data.kingdom.Relations
import at.posselt.pfrpg2e.data.kingdom.calculateControlDC
import at.posselt.pfrpg2e.data.kingdom.findKingdomSize
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.kingdom.KingdomActor
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.OngoingEvent
import at.posselt.pfrpg2e.kingdom.data.RawGroup
import at.posselt.pfrpg2e.kingdom.data.getChosenFeats
import at.posselt.pfrpg2e.kingdom.data.getChosenFeatures
import at.posselt.pfrpg2e.kingdom.data.getChosenGovernment
import at.posselt.pfrpg2e.kingdom.dialogs.ActivityManagement
import at.posselt.pfrpg2e.kingdom.dialogs.AddModifier
import at.posselt.pfrpg2e.kingdom.dialogs.CharterManagement
import at.posselt.pfrpg2e.kingdom.dialogs.FeatManagement
import at.posselt.pfrpg2e.kingdom.dialogs.GovernmentManagement
import at.posselt.pfrpg2e.kingdom.dialogs.HeartlandManagement
import at.posselt.pfrpg2e.kingdom.dialogs.KingdomSettingsApplication
import at.posselt.pfrpg2e.kingdom.dialogs.MilestoneManagement
import at.posselt.pfrpg2e.kingdom.getAllActivities
import at.posselt.pfrpg2e.kingdom.getAllSettlements
import at.posselt.pfrpg2e.kingdom.getCharters
import at.posselt.pfrpg2e.kingdom.getExplodedFeatures
import at.posselt.pfrpg2e.kingdom.getFeats
import at.posselt.pfrpg2e.kingdom.getGovernments
import at.posselt.pfrpg2e.kingdom.getHeartlands
import at.posselt.pfrpg2e.kingdom.getKingdom
import at.posselt.pfrpg2e.kingdom.getMilestones
import at.posselt.pfrpg2e.kingdom.getRealmData
import at.posselt.pfrpg2e.kingdom.hasLeaderUuid
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.getHighestLeadershipModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.penalties.calculateUnrestPenalty
import at.posselt.pfrpg2e.kingdom.parse
import at.posselt.pfrpg2e.kingdom.parseLeaderActors
import at.posselt.pfrpg2e.kingdom.resources.calculateStorage
import at.posselt.pfrpg2e.kingdom.setKingdom
import at.posselt.pfrpg2e.kingdom.sheet.contexts.KingdomSheetContext
import at.posselt.pfrpg2e.kingdom.sheet.contexts.NavEntryContext
import at.posselt.pfrpg2e.kingdom.sheet.contexts.NewKingdomContext
import at.posselt.pfrpg2e.kingdom.sheet.contexts.createBonusFeatContext
import at.posselt.pfrpg2e.kingdom.sheet.contexts.toContext
import at.posselt.pfrpg2e.kingdom.structures.RawSettlement
import at.posselt.pfrpg2e.kingdom.structures.importSettlementScene
import at.posselt.pfrpg2e.kingdom.structures.importStructures
import at.posselt.pfrpg2e.kingdom.vacancies
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.launch
import at.posselt.pfrpg2e.utils.openJournal
import at.posselt.pfrpg2e.utils.typeSafeUpdate
import com.foundryvtt.core.Actor
import com.foundryvtt.core.Game
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
import com.foundryvtt.core.onApplyTokenStatusEffect
import com.foundryvtt.core.onCanvasReady
import com.foundryvtt.core.onSightRefresh
import com.foundryvtt.core.onUpdateActor
import com.foundryvtt.core.ui
import com.foundryvtt.core.utils.deepClone
import com.foundryvtt.kingmaker.onCloseKingmakerHexEdit
import js.core.Void
import js.objects.recordOf
import kotlinx.coroutines.await
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise


private enum class NavEntry {
    TURN, CHARACTER_SHEET, SETTLEMENTS, TRADE_AGREEMENTS, MODIFIERS, NOTES;

    companion object {
        fun fromString(value: String) = fromCamelCase<NavEntry>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}

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
        MenuControl(label = "New Kingdom", action = "new-kingdom", gmOnly = true),
        MenuControl(label = "Settings", action = "settings", gmOnly = true),
        MenuControl(label = "Help", action = "help"),
    ),
    scrollable = arrayOf(".km-kingdom-sheet-sidebar", ".km-kingdom-sheet-sub-content"),
) {
    private var initialKingdomLevel = getKingdom().level
    private var noCharter = getKingdom().charter.type == null
    private var currentCharacterSheetNavEntry: String = if (noCharter) "Creation" else "$initialKingdomLevel"
    private var currentNavEntry: NavEntry = if (noCharter) NavEntry.CHARACTER_SHEET else NavEntry.TURN
    private var bonusFeat: String? = null
    private var ongoingEvent: String? = null

    init {
        actor.apps[id] = this
        appHook.onDeleteScene { _, _, _ -> render() }
        appHook.onCreateTile { _, _, _, _ -> render() }
        appHook.onUpdateTile { _, _, _, _ -> render() }
        appHook.onDeleteTile { _, _, _ -> render() }
        appHook.onCreateDrawing { _, _, _, _ -> render() }
        appHook.onUpdateDrawing { _, _, _, _ -> render() }
        appHook.onDeleteDrawing { _, _, _ -> render() }
        appHook.onDeleteToken { _, _, _ -> render() }
        appHook.onCreateToken { _, _, _, _ -> render() }
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

            "change-kingdom-section-nav" -> {
                event.preventDefault()
                event.stopPropagation()
                currentCharacterSheetNavEntry = target.dataset["link"] ?: "Creation"
                render()
            }

            "change-nav" -> {
                event.preventDefault()
                event.stopPropagation()
                currentNavEntry = target.dataset["link"]?.let { NavEntry.fromString(it) } ?: NavEntry.TURN
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
            "structures-import" -> buildPromise {
                importStructures()
            }

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

            "new-kingdom" -> buildPromise {
                prompt<NewKingdomContext, Unit>(
                    title = "Create a New Kingdom",
                    templatePath = "components/forms/form.hbs",
                    templateContext = recordOf(
                        "formRows" to formContext(
                            TextInput(
                                name = "name",
                                label = "Kingdom Name",
                                value = ""
                            )
                        )
                    ),
                ) {
                    KingdomSheet(game, newKingdom(it.name), dispatcher).launch()
                }
            }

            "help" -> buildPromise {
                openJournal("Compendium.pf2e-kingmaker-tools.kingmaker-tools-journals.JournalEntry.iAQCUYEAq4Dy8uCY.JournalEntryPage.ty6BS5eSI7ScfVBk")
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
        val allFeatures = kingdom.getExplodedFeatures()
        val chosenFeatures = kingdom.getChosenFeatures(allFeatures)
        val chosenFeats = kingdom.getChosenFeats(chosenFeatures)
        val leaderActors = kingdom.parseLeaderActors()
        val storage = calculateStorage(realm, settlements.allSettlements)
        val leaderSkills = kingdom.settings.leaderSkills.parse()
        val defaultLeadershipBonuses = getHighestLeadershipModifiers(
            leaderActors = leaderActors,
            leaderSkills = leaderSkills,
        )
        val kingdomNameInput = TextInput(
            name = "name",
            label = "Name",
            value = kingdom.name,
        )
        val settlementInput = Select(
            name = "activeSettlement",
            label = "Settlement",
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
        val ongoingEvent = TextInput(
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
            leadersContext = kingdom.leaders.toContext(leaderActors, defaultLeadershipBonuses),
            charter = kingdom.charter.toContext(enabledCharters),
            heartland = kingdom.heartland.toContext(enabledHeartlands),
            government = kingdom.government.toContext(enabledGovernments, feats),
            abilityBoosts = kingdom.abilityBoosts.toContext("", 2 + increaseScorePicksBy),
            currentNavEntry = currentNavEntry.value,
            hideCreation = currentCharacterSheetNavEntry != "Creation",
            hideBonus = currentCharacterSheetNavEntry != "Bonus",
            settings = kingdom.settings,
            featuresByLevel = kingdom.features.toContext(
                government = kingdom.getChosenGovernment(),
                features = allFeatures.toTypedArray(),
                feats = feats,
                increaseBoostsBy = increaseScorePicksBy,
                navigationEntry = currentCharacterSheetNavEntry,
                bonusFeats = kingdom.bonusFeats,
            )
                .sortedBy { it.level }
                .toTypedArray(),
            bonusFeat = createBonusFeatContext(
                government = kingdom.getChosenGovernment(),
                feats = feats,
                choices = kingdom.features,
                bonusFeats = kingdom.bonusFeats,
                value = bonusFeat,
            ),
            bonusFeats = kingdom.bonusFeats.toContext(
                kingdom.getFeats(),
            ),
            groups = kingdom.groups.toContext(),
            abilityScores = kingdom.abilityScores.toContext(),
            skillRanks = kingdom.skillRanks.toContext(),
            milestones = kingdom.milestones.toContext(kingdom.getMilestones()),
            ongoingEvent = ongoingEvent.toContext(),
            isGM = game.user.isGM,
            actor = actor,
            modifiers = kingdom.modifiers.toContext(),
            mainNav = createMainNav(kingdom),
        )
    }

    private fun createMainNav(kingdom: KingdomData): Array<NavEntryContext> {
        val tradeAgreements = kingdom.groups.count { it.relations == Relations.TRADE_AGREEMENT.value }
        return NavEntry.entries.map {
            NavEntryContext(
                label = if (it == NavEntry.TRADE_AGREEMENTS) "${it.label} ($tradeAgreements)" else it.label,
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
        console.log(value.notes)
        beforeKingdomUpdate(previousKingdom, kingdom)
        actor.setKingdom(kingdom)
        bonusFeat = value.bonusFeat
        ongoingEvent = value.ongoingEvent
        null
    }
}

suspend fun newKingdom(name: String): KingdomActor {
    val actor = KingdomActor.create(
        recordOf(
            "type" to "npc",
            "name" to name,
            "img" to "icons/environment/settlement/castle.webp",
            "ownership" to recordOf(
                "default" to 3
            )
        )
    ).await()
    actor.typeSafeUpdate {
        prototypeToken.actorLink = true
    }
    actor.setKingdom(createKingdomDefaults(name))
    return actor
}

suspend fun openOrCreateKingdomSheet(game: Game, dispatcher: ActionDispatcher, actor: KingdomActor?) {
    if (actor == null) {
        val actor = newKingdom("Kingdom")
        KingdomSheet(game, actor, dispatcher).launch()
        openJournal("Compendium.pf2e-kingmaker-tools.kingmaker-tools-journals.JournalEntry.FwcyYZARAnOHlKkE")
    } else {
        KingdomSheet(game, actor, dispatcher).launch()
    }
}