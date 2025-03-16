package at.posselt.pfrpg2e

import at.posselt.pfrpg2e.actions.ActionDispatcher
import at.posselt.pfrpg2e.actions.handlers.AddHuntAndGatherResultHandler
import at.posselt.pfrpg2e.actions.handlers.ApplyMealEffectsHandler
import at.posselt.pfrpg2e.actions.handlers.ClearMealEffectsHandler
import at.posselt.pfrpg2e.actions.handlers.GainProvisionsHandler
import at.posselt.pfrpg2e.actions.handlers.LearnSpecialRecipeHandler
import at.posselt.pfrpg2e.actions.handlers.OpenCampingSheetHandler
import at.posselt.pfrpg2e.actions.handlers.OpenKingdomSheetHandler
import at.posselt.pfrpg2e.actions.handlers.SyncActivitiesHandler
import at.posselt.pfrpg2e.actor.partyMembers
import at.posselt.pfrpg2e.camping.bindCampingChatEventListeners
import at.posselt.pfrpg2e.camping.createCampingIcon
import at.posselt.pfrpg2e.camping.getCampingActors
import at.posselt.pfrpg2e.camping.newCampingActor
import at.posselt.pfrpg2e.camping.openCampingSheet
import at.posselt.pfrpg2e.camping.registerActivityDiffingHooks
import at.posselt.pfrpg2e.camping.registerMealDiffingHooks
import at.posselt.pfrpg2e.combattracks.registerCombatTrackHooks
import at.posselt.pfrpg2e.firstrun.showFirstRunMessage
import at.posselt.pfrpg2e.kingdom.armies.registerArmyConsumptionHooks
import at.posselt.pfrpg2e.kingdom.bindChatButtons
import at.posselt.pfrpg2e.kingdom.createKingmakerIcon
import at.posselt.pfrpg2e.kingdom.getKingdomActors
import at.posselt.pfrpg2e.kingdom.registerContextMenus
import at.posselt.pfrpg2e.kingdom.sheet.newKingdomActor
import at.posselt.pfrpg2e.kingdom.sheet.openOrCreateKingdomSheet
import at.posselt.pfrpg2e.kingdom.structures.validateStructures
import at.posselt.pfrpg2e.macros.awardHeroPointsMacro
import at.posselt.pfrpg2e.macros.awardXPMacro
import at.posselt.pfrpg2e.macros.chooseParty
import at.posselt.pfrpg2e.macros.combatTrackMacro
import at.posselt.pfrpg2e.macros.createFoodMacro
import at.posselt.pfrpg2e.macros.editRealmTileMacro
import at.posselt.pfrpg2e.macros.editStructureMacro
import at.posselt.pfrpg2e.macros.resetHeroPointsMacro
import at.posselt.pfrpg2e.macros.rollExplorationSkillCheckMacro
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
import at.posselt.pfrpg2e.utils.Pfrpg2eKingdomCampingWeather
import at.posselt.pfrpg2e.utils.ToolsMacros
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.fixVisibility
import at.posselt.pfrpg2e.utils.loadTpls
import at.posselt.pfrpg2e.utils.pf2eKingmakerTools
import at.posselt.pfrpg2e.weather.registerWeatherHooks
import at.posselt.pfrpg2e.weather.rollWeather
import com.foundryvtt.core.Hooks
import com.foundryvtt.core.directories.onRenderActorDirectory
import com.foundryvtt.core.game
import com.foundryvtt.core.onInit
import com.foundryvtt.core.onReady
import com.foundryvtt.core.onRenderChatLog
import com.foundryvtt.core.onRenderChatMessage
import io.kvision.jquery.get
import js.objects.recordOf
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.get

fun main() {
    registerContextMenus()
    Hooks.onInit {
        val actionDispatcher = ActionDispatcher(
            game = game,
            handlers = listOf(
                AddHuntAndGatherResultHandler(game = game),
                OpenCampingSheetHandler(game = game),
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

        bindChatButtons(game)

        Hooks.onRenderActorDirectory { _, html, _ ->
            html[0]?.querySelectorAll(".party-header")
                ?.asList()
                ?.filterIsInstance<HTMLElement>()
                ?.forEach {
                    val uuid = it.dataset["documentId"]
                    val kingdomLink = createKingmakerIcon(uuid, actionDispatcher)
                    val campingSheetLink = createCampingIcon(uuid, actionDispatcher)
                    it.querySelector("&> a")?.insertAdjacentElement("beforeBegin", campingSheetLink)
                    it.querySelector("&> a")?.insertAdjacentElement("beforeBegin", kingdomLink)
                }

            buildPromise {
                // register partials
                loadTpls(
                    arrayOf(
                        "kingdom-activities" to "applications/kingdom/activities.hbs",
                        "kingdom-trade-agreements" to "applications/kingdom/sections/trade-agreements/page.hbs",
                        "kingdom-settlements" to "applications/kingdom/sections/settlements/page.hbs",
                        "kingdom-turn" to "applications/kingdom/sections/turn/page.hbs",
                        "kingdom-modifiers" to "applications/kingdom/sections/modifiers/page.hbs",
                        "kingdom-notes" to "applications/kingdom/sections/notes/page.hbs",
                        "kingdom-character-sheet" to "applications/kingdom/sections/character-sheet/page.hbs",
                        "kingdom-character-sheet-creation" to "applications/kingdom/sections/character-sheet/creation.hbs",
                        "kingdom-character-sheet-bonus" to "applications/kingdom/sections/character-sheet/bonus.hbs",
                        "kingdom-character-sheet-levels" to "applications/kingdom/sections/character-sheet/levels.hbs",
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
                    rollKingmakerWeatherMacro = { buildPromise { rollWeather(game) } },
                    awardXpMacro = { buildPromise { awardXPMacro(game) } },
                    resetHeroPointsMacro = {
                        buildPromise {
                            val players = chooseParty(game).partyMembers()
                            resetHeroPointsMacro(players)
                        }
                    },
                    awardHeroPointsMacro = {
                        buildPromise {
                            val players = chooseParty(game).partyMembers()
                            awardHeroPointsMacro(players)
                        }
                    },
                    rollExplorationSkillCheck = { skill, effect ->
                        buildPromise {
                            rollExplorationSkillCheckMacro(
                                game,
                                attributeName = skill,
                                explorationEffectName = effect,
                            )
                        }
                    },
                    rollSkillDialog = {
                        buildPromise {
                            rollPartyCheckMacro(chooseParty(game).partyMembers())
                        }
                    },
                    setSceneCombatPlaylistDialogMacro = { actor -> buildPromise { combatTrackMacro(game, actor) } },
                    toTimeOfDayMacro = { buildPromise { setTimeOfDayMacro(game) } },
                    toggleCombatTracksMacro = { buildPromise { toggleCombatTracksMacro(game) } },
                    realmTileDialogMacro = { buildPromise { editRealmTileMacro(game) } },
                    editStructureMacro = { actor -> buildPromise { editStructureMacro(actor) } },
                    openCampingSheet = {
                        buildPromise {
                            // TODO: let player choose which camping sheet to open
                            val actor = game.getCampingActors().firstOrNull() ?: newCampingActor()
                            openCampingSheet(game, actionDispatcher, actor)
                        }
                    },
                    subsistMacro = { actor -> buildPromise { subsistMacro(game, actor) } },
                    createFoodMacro = { buildPromise { createFoodMacro(game, actionDispatcher) } },
                    viewKingdomMacro = {
                        buildPromise {
                            // TODO: let player choose which kingdom to open
                            val actor = game.getKingdomActors().firstOrNull() ?: newKingdomActor()
                            openOrCreateKingdomSheet(game, actionDispatcher, actor)
                        }
                    }
                ),
            )

            Hooks.onReady {
                buildPromise {
                    game.migratePfrpg2eKingdomCampingWeather()
                    showFirstRunMessage(game)
                    validateStructures(game)
                    openOrCreateKingdomSheet(game, actionDispatcher, game.getKingdomActors().first())
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

}