package at.posselt.pfrpg2e

import at.posselt.pfrpg2e.actions.ActionDispatcher
import at.posselt.pfrpg2e.actions.handlers.AddHuntAndGatherResultHandler
import at.posselt.pfrpg2e.actions.handlers.ApplyMealEffectsHandler
import at.posselt.pfrpg2e.actions.handlers.ClearActivitiesHandler
import at.posselt.pfrpg2e.actions.handlers.ClearMealEffectsHandler
import at.posselt.pfrpg2e.actions.handlers.GainProvisionsHandler
import at.posselt.pfrpg2e.actions.handlers.LearnSpecialRecipeHandler
import at.posselt.pfrpg2e.actions.handlers.OpenCampingSheetHandler
import at.posselt.pfrpg2e.actions.handlers.OpenKingdomSheetHandler
import at.posselt.pfrpg2e.actions.handlers.SyncActivitiesHandler
import at.posselt.pfrpg2e.actor.partyMembers
import at.posselt.pfrpg2e.camping.bindCampingChatEventListeners
import at.posselt.pfrpg2e.camping.openCampingSheet
import at.posselt.pfrpg2e.camping.registerActivityDiffingHooks
import at.posselt.pfrpg2e.camping.registerMealDiffingHooks
import at.posselt.pfrpg2e.combattracks.registerCombatTrackHooks
import at.posselt.pfrpg2e.data.armies.findMaximumArmyTactics
import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.calculateEventXP
import at.posselt.pfrpg2e.data.kingdom.calculateHexXP
import at.posselt.pfrpg2e.data.kingdom.calculateRpXP
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.firstrun.showFirstRunMessage
import at.posselt.pfrpg2e.kingdom.armies.registerArmyConsumptionHooks
import at.posselt.pfrpg2e.kingdom.data.getChosenFeats
import at.posselt.pfrpg2e.kingdom.dialogs.CheckType
import at.posselt.pfrpg2e.kingdom.dialogs.KingdomSettingsApplication
import at.posselt.pfrpg2e.kingdom.dialogs.addModifier
import at.posselt.pfrpg2e.kingdom.dialogs.addOngoingEvent
import at.posselt.pfrpg2e.kingdom.dialogs.armyBrowser
import at.posselt.pfrpg2e.kingdom.dialogs.armyTacticsBrowser
import at.posselt.pfrpg2e.kingdom.dialogs.editSettlement
import at.posselt.pfrpg2e.kingdom.dialogs.kingdomCheckDialog
import at.posselt.pfrpg2e.kingdom.dialogs.kingdomSizeHelp
import at.posselt.pfrpg2e.kingdom.dialogs.settlementSizeHelp
import at.posselt.pfrpg2e.kingdom.dialogs.structureXpDialog
import at.posselt.pfrpg2e.kingdom.getAllActivities
import at.posselt.pfrpg2e.kingdom.getAllSettlements
import at.posselt.pfrpg2e.kingdom.getKingdom
import at.posselt.pfrpg2e.kingdom.getKingdomActor
import at.posselt.pfrpg2e.kingdom.getRealmData
import at.posselt.pfrpg2e.kingdom.kingdomActivities
import at.posselt.pfrpg2e.kingdom.kingdomFeats
import at.posselt.pfrpg2e.kingdom.kingdomFeatures
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.getHighestLeadershipModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.penalties.calculateUnrestPenalty
import at.posselt.pfrpg2e.kingdom.parse
import at.posselt.pfrpg2e.kingdom.parseLeaderActors
import at.posselt.pfrpg2e.kingdom.setKingdom
import at.posselt.pfrpg2e.kingdom.sheet.adjustUnrest
import at.posselt.pfrpg2e.kingdom.sheet.calculateSkillModifierBreakdown
import at.posselt.pfrpg2e.kingdom.sheet.collectResources
import at.posselt.pfrpg2e.kingdom.sheet.openKingdomSheet
import at.posselt.pfrpg2e.kingdom.sheet.tickDownModifiers
import at.posselt.pfrpg2e.kingdom.structures.parseStructure
import at.posselt.pfrpg2e.kingdom.structures.structures
import at.posselt.pfrpg2e.kingdom.structures.validateStructures
import at.posselt.pfrpg2e.macros.awardHeroPointsMacro
import at.posselt.pfrpg2e.macros.awardXPMacro
import at.posselt.pfrpg2e.macros.combatTrackMacro
import at.posselt.pfrpg2e.macros.createFoodMacro
import at.posselt.pfrpg2e.macros.editRealmTileMacro
import at.posselt.pfrpg2e.macros.editStructureMacro
import at.posselt.pfrpg2e.macros.resetHeroPointsMacro
import at.posselt.pfrpg2e.macros.rollCultEventMacro
import at.posselt.pfrpg2e.macros.rollExplorationSkillCheckMacro
import at.posselt.pfrpg2e.macros.rollKingdomEventMacro
import at.posselt.pfrpg2e.macros.rollPartyCheckMacro
import at.posselt.pfrpg2e.macros.sceneWeatherSettingsMacro
import at.posselt.pfrpg2e.macros.setTimeOfDayMacro
import at.posselt.pfrpg2e.macros.setWeatherMacro
import at.posselt.pfrpg2e.macros.subsistMacro
import at.posselt.pfrpg2e.macros.toggleCombatTracksMacro
import at.posselt.pfrpg2e.macros.toggleShelteredMacro
import at.posselt.pfrpg2e.macros.toggleWeatherMacro
import at.posselt.pfrpg2e.migrations.migratePfrpg2eKingdomCampingWeather
import at.posselt.pfrpg2e.settings.pfrpg2eKingdomCampingWeather
import at.posselt.pfrpg2e.utils.KtMigration
import at.posselt.pfrpg2e.utils.KtMigrationData
import at.posselt.pfrpg2e.utils.Pfrpg2eKingdomCampingWeather
import at.posselt.pfrpg2e.utils.ToolsMacros
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.fixVisibility
import at.posselt.pfrpg2e.utils.launch
import at.posselt.pfrpg2e.utils.loadTpls
import at.posselt.pfrpg2e.utils.pf2eKingmakerTools
import at.posselt.pfrpg2e.weather.registerWeatherHooks
import at.posselt.pfrpg2e.weather.rollWeather
import com.foundryvtt.core.Hooks
import com.foundryvtt.core.game
import com.foundryvtt.core.onInit
import com.foundryvtt.core.onReady
import com.foundryvtt.core.onRenderChatLog
import com.foundryvtt.core.onRenderChatMessage
import io.kvision.jquery.get
import js.collections.JsMap
import js.objects.recordOf
import kotlinx.coroutines.await
import org.w3c.dom.HTMLElement

fun main() {
    Hooks.onInit {
        val actionDispatcher = ActionDispatcher(
            game = game,
            handlers = listOf(
                AddHuntAndGatherResultHandler(game = game),
                OpenCampingSheetHandler(game = game),
                ClearActivitiesHandler(game = game),
                SyncActivitiesHandler(game = game),
                ClearMealEffectsHandler(game = game),
                LearnSpecialRecipeHandler(game = game),
                ApplyMealEffectsHandler(game = game),
                GainProvisionsHandler(game = game),
                OpenKingdomSheetHandler(game = game),
            )
        ).apply {
            listen()
        }

        buildPromise {
            // register partials
            loadTpls(
                arrayOf(
                    "campingTile" to "applications/kingdom/kingdom-sheet-kingdom.hbs",
                    "campingTile" to "applications/camping/camping-tile.hbs",
                    "recipeTile" to "applications/camping/recipe-tile.hbs",
                    "formElement" to "components/forms/form-element.hbs",
                    "tabs" to "components/tabs/tabs.hbs",
                    "foodCost" to "components/food-cost/food-cost.hbs",
                    "skillPickerInput" to "components/skill-picker/skill-picker-input.hbs",
                    "activityEffectsInput" to "components/activity-effects/activity-effects-input.hbs",
                )
            )
            game.settings.pfrpg2eKingdomCampingWeather.register()

            // load custom token mappings if kingmaker module isn't installed
            if (game.modules.get("pf2e-kingmaker")?.active != true
                && game.settings.pfrpg2eKingdomCampingWeather.getEnableTokenMapping()
            ) {
                val data = recordOf(
                    "flags" to recordOf(
                        Config.moduleId to recordOf(
                            "pf2e-art" to "modules/${Config.moduleId}/token-map.json"
                        )
                    )
                )
                game.modules.get(Config.moduleId)
                    ?.updateSource(data)
            }
            registerWeatherHooks(game)
            registerCombatTrackHooks(game)
            registerActivityDiffingHooks(game, actionDispatcher)
            registerMealDiffingHooks()
            registerArmyConsumptionHooks(game)
        }

        game.pf2eKingmakerTools = Pfrpg2eKingdomCampingWeather(
            macros = ToolsMacros(
                toggleWeatherMacro = { buildPromise { toggleWeatherMacro(game) } },
                toggleShelteredMacro = { buildPromise { toggleShelteredMacro(game) } },
                setCurrentWeatherMacro = { buildPromise { setWeatherMacro(game) } },
                sceneWeatherSettingsMacro = {
                    buildPromise<Unit> {
                        game.scenes.active?.let {
                            sceneWeatherSettingsMacro(it)
                        }
                    }
                },
                kingdomEventsMacro = { buildPromise { rollKingdomEventMacro(game) } },
                cultEventsMacro = { buildPromise { rollCultEventMacro(game) } },
                rollKingmakerWeatherMacro = { buildPromise { rollWeather(game) } },
                awardXpMacro = { buildPromise { awardXPMacro(game.partyMembers()) } },
                resetHeroPointsMacro = { buildPromise { resetHeroPointsMacro(game.partyMembers()) } },
                awardHeroPointsMacro = { buildPromise { awardHeroPointsMacro(game.partyMembers()) } },
                rollExplorationSkillCheck = { skill, effect ->
                    buildPromise {
                        rollExplorationSkillCheckMacro(
                            game,
                            attributeName = skill,
                            explorationEffectName = effect,
                        )
                    }
                },
                rollSkillDialog = { buildPromise { rollPartyCheckMacro(game.partyMembers()) } },
                setSceneCombatPlaylistDialogMacro = { actor -> buildPromise { combatTrackMacro(game, actor) } },
                toTimeOfDayMacro = { buildPromise { setTimeOfDayMacro(game) } },
                toggleCombatTracksMacro = { buildPromise { toggleCombatTracksMacro(game) } },
                realmTileDialogMacro = { buildPromise { editRealmTileMacro(game) } },
                editStructureMacro = { actor -> buildPromise { editStructureMacro(actor) } },
                openCampingSheet = { buildPromise { openCampingSheet(game, actionDispatcher) } },
                subsistMacro = { actor -> buildPromise { subsistMacro(game, actor) } },
                createFoodMacro = { buildPromise { createFoodMacro(game, actionDispatcher) } },
                viewKingdomMacro = { buildPromise {
                    // TODO: let player choose which kingdom to open
                    val actor = game.getKingdomActor()
                    openKingdomSheet(game, actionDispatcher, actor)
                }}
            ),
            migration = KtMigration(
                kingdomSettings = { settings, onSave ->
                    KingdomSettingsApplication(
                        game,
                        kingdomSettings = settings,
                        onSave = onSave
                    ).launch()
                },
                kingdomSizeHelp = { buildPromise { kingdomSizeHelp() } },
                settlementSizeHelp = { buildPromise { settlementSizeHelp() } },
                structureXpDialog = { onOk -> buildPromise { structureXpDialog(game, onOk) } },
                editSettlementDialog = ::editSettlement,
                addOngoingEventDialog = { onOk -> buildPromise { addOngoingEvent(onOk) } },
                data = KtMigrationData(
                    structures = structures,
                    feats = kingdomFeats,
                    features = kingdomFeatures,
                    activities = kingdomActivities,
                ),
                armyBrowser = { game, actor, kingdom ->
                    buildPromise {
                        armyBrowser(game, actor, kingdom)
                    }
                },
                adjustUnrest = { kingdom ->
                    buildPromise {
                        val settlements = kingdom.getAllSettlements(game)
                        val chosenFeats = kingdom.getChosenFeats()
                        adjustUnrest(kingdom, settlements.allSettlements, chosenFeats)
                    }
                },
                collectResources = { kingdom ->
                    buildPromise {
                        val result = collectResources(
                            kingdomData = kingdom,
                            realmData = game.getRealmData(kingdom),
                            allFeats = kingdom.getChosenFeats(),
                            settlements = kingdom.getAllSettlements(game).allSettlements
                        )
                        recordOf(
                            "ore" to result.ore,
                            "lumber" to result.lumber,
                            "luxuries" to result.luxuries,
                            "stone" to result.stone,
                            "rp" to result.resourcePoints,
                            "rd" to result.resourceDice,
                        )
                    }
                },
                tacticsBrowser = { game, actor, kingdom ->
                    buildPromise {
                        armyTacticsBrowser(game, actor, kingdom)
                    }
                },
                findMaximumArmyTactics = ::findMaximumArmyTactics,
                addModifier = {
                    buildPromise {
                        val kingdomActor = game.getKingdomActor()
                        val kingdom = kingdomActor?.getKingdom()
                        kingdom?.getAllActivities()?.let {
                            addModifier(it) { mod ->
                                kingdom.modifiers = kingdom.modifiers + mod
                                kingdomActor.setKingdom(kingdom)
                            }
                        }
                    }
                },
                calculateLeadershipBonuses = { kingdom ->
                    buildPromise {
                        val mods = getHighestLeadershipModifiers(
                            leaderActors = kingdom.parseLeaderActors(),
                            leaderSkills = kingdom.settings.leaderSkills.parse(),
                        )
                        val map = JsMap<String, Int>()
                        Leader.entries.forEach { leader -> map[leader.value] = mods.resolve(leader) }
                        map
                    }
                },
                calculateUnrestPenalty = {unrest -> calculateUnrestPenalty(unrest) },
                calculateSkillModifiers = { game, kingdom ->
                    val settlementResult = kingdom.getAllSettlements(game)
                    buildPromise {
                        calculateSkillModifierBreakdown(kingdom, settlementResult)
                    }
                },
                tickDownModifiers = {
                    buildPromise {
                        val kingdomActor = game.getKingdomActor()
                        kingdomActor?.getKingdom()?.let {
                            tickDownModifiers(kingdomActor, it)
                        }
                    }
                },
                calculateHexXP = ::calculateHexXP,
                calculateRpXP = ::calculateRpXP,
                calculateEventXP = ::calculateEventXP,
                checkDialog = { game, kingdom, kingdomActor, activity, structure, skill, afterRoll ->
                    buildPromise {
                        val wrapper: suspend (degree: DegreeOfSuccess) -> String = { degree ->
                            afterRoll(degree).await()
                        }
                        val structure = structure?.parseStructure(false)
                        kingdomCheckDialog(
                            game = game,
                            kingdom = kingdom,
                            kingdomActor = kingdomActor,
                            check = if (structure != null) {
                                CheckType.BuildStructure(structure)
                            } else if (activity != null) {
                                CheckType.PerformActivity(activity)
                            } else {
                                checkNotNull(skill) {
                                    "Skill must be provided"
                                }
                                val kingdomSkill = KingdomSkill.fromString(skill)
                                checkNotNull(kingdomSkill) {
                                    "Invalid skill $skill"
                                }
                                CheckType.RollSkill(kingdomSkill)
                            },
                            afterRoll = wrapper
                        )
                    }
                }
            )
        )

        Hooks.onReady {
            buildPromise {
                game.migratePfrpg2eKingdomCampingWeather()
                showFirstRunMessage(game)
                validateStructures(game)
                openKingdomSheet(game, actionDispatcher, game.getKingdomActor())
            }
        }

        Hooks.onRenderChatMessage { message, html, _ ->
            val elem = html[0] as HTMLElement
            fixVisibility(game, elem, message)
        }

        Hooks.onRenderChatLog { _, _, _ ->
            bindCampingChatEventListeners(game, actionDispatcher)
        }
    }
}
