import {dayHasChanged, syncWeather, toggleWeather} from './weather';
import {getSelectedCharacter, isGm} from './utils';
import {toTimeOfDayMacro} from './time/app';
import {getBooleanSetting, getStringSetting} from './settings';
import {rollKingmakerWeather} from './kingmaker-weather';
import {randomEncounterDialog} from './random-encounters';
import {rollExplorationSkillCheck, rollSkillDialog} from './skill-checks';
import {rollKingdomEvent} from './kingdom-events';
import {subsist} from './subsist';
import {
    camouflageCampsite, campManagement, cookRecipe, discoverSpecialMeal,
    huntAndGather, learnFromCompanion,
    organizeWatch,
    postCompanionEffects,
    prepareCampsite,
    tellCampfireStory,
} from './camping';

Hooks.on('ready', async () => {
    if (game instanceof Game) {
        const gameInstance = game;
        gameInstance.pf2eKingmakerTools = {
            macros: {
                toggleWeatherMacro: toggleWeather.bind(null, game),
                toTimeOfDayMacro: toTimeOfDayMacro.bind(null, game),
                randomEncounterMacro: randomEncounterDialog.bind(null, game),
                kingdomEventsMacro: rollKingdomEvent.bind(null, game),
                postCompanionEffectsMacro: postCompanionEffects.bind(null, game),
                rollKingmakerWeatherMacro: rollKingmakerWeather.bind(null, game),
                /* eslint-disable @typescript-eslint/no-explicit-any */
                subsistMacro: async (actor: any): Promise<void> => {
                    const selectedActor = getBooleanSetting(gameInstance, 'useSelectedCharacter')
                        ? getSelectedCharacter(gameInstance) : actor;
                    await subsist(gameInstance, selectedActor);
                },
                /* eslint-disable @typescript-eslint/no-explicit-any */
                huntAndGatherMacro: (actor: any): Promise<void> => huntAndGather(gameInstance, actor),
                /* eslint-disable @typescript-eslint/no-explicit-any */
                camouflageCampsiteMacro: (actor: any): Promise<void> => camouflageCampsite(gameInstance, actor),
                /* eslint-disable @typescript-eslint/no-explicit-any */
                organizeWatchMacro: (actor: any): Promise<void> => organizeWatch(gameInstance, actor),
                /* eslint-disable @typescript-eslint/no-explicit-any */
                tellCampfireStoryMacro: (actor: any): Promise<void> => tellCampfireStory(gameInstance, actor),
                /* eslint-disable @typescript-eslint/no-explicit-any */
                prepareCampsiteMacro: (actor: any): Promise<void> => prepareCampsite(gameInstance, actor),
                /* eslint-disable @typescript-eslint/no-explicit-any */
                discoverSpecialMealMacro: (actor: any): Promise<void> => discoverSpecialMeal(gameInstance, actor),
                /* eslint-disable @typescript-eslint/no-explicit-any */
                cookRecipeMacro: (actor: any): Promise<void> => cookRecipe(gameInstance, actor),
                /* eslint-disable @typescript-eslint/no-explicit-any */
                learnFromCompanionMacro: (actor: any): Promise<void> => learnFromCompanion(gameInstance, actor),
                /* eslint-disable @typescript-eslint/no-explicit-any */
                campManagementMacro: (actor: any): Promise<void> => campManagement(gameInstance, actor),
                rollExplorationSkillCheck: async (skill: string, effect: string): Promise<void> => {
                    const actors = canvas?.scene?.tokens
                        ?.filter(t => t !== null && t.actor !== null && t.actor.type === 'character')
                        ?.map(t => t.actor!) ?? [];
                    await rollExplorationSkillCheck(actors, skill, effect);
                },
                rollSkillDialog: async (): Promise<void> => {
                    const configuredActors = getStringSetting(gameInstance, 'skillCheckMacroCharacterNames')
                        ?.split('||')
                        ?.map(s => s.trim())
                        ?.filter(s => s !== '') ?? [];
                    if (configuredActors.length === 0) {
                        ui?.notifications?.error('No actor names configured! Configure actor names first in settings');
                    } else {
                        const actorNames = new Set(configuredActors);
                        const actors = gameInstance?.actors
                            ?.filter(actor => actor.type === 'character'
                                && actor.name !== null
                                && actorNames.has(actor.name)) ?? [];
                        await rollSkillDialog(actors);
                    }
                },
            },
        };
        const rollModeChoices = {
            publicroll: 'Public Roll',
            gmroll: 'Private GM Roll',
            blindroll: 'Blind GM Roll',
            selfroll: 'Self Roll',
        };
        gameInstance.settings.register<string, string, number>('pf2e-kingmaker-tools', 'averagePartyLevel', {
            name: 'Average Party Level',
            default: 1,
            config: true,
            type: Number,
            scope: 'world',
        });
        gameInstance.settings.register<string, string, boolean>('pf2e-kingmaker-tools', 'enableWeather', {
            name: 'Enable Weather',
            default: true,
            config: true,
            type: Boolean,
            scope: 'world',
        });
        gameInstance.settings.register<string, string, boolean>('pf2e-kingmaker-tools', 'autoRollWeather', {
            name: 'Automatically roll Weather',
            hint: 'When a new day begins (00:00), automatically roll weather',
            default: true,
            config: true,
            type: Boolean,
            scope: 'world',
        });
        gameInstance.settings.register<string, string, string>('pf2e-kingmaker-tools', 'weatherRollMode', {
            name: 'Weather Roll Mode',
            scope: 'world',
            config: true,
            default: 'gmroll',
            type: String,
            choices: rollModeChoices,
        });
        gameInstance.settings.register('pf2e-kingmaker-tools', 'currentWeatherFx', {
            name: 'Current Weather FX',
            hint: 'Based on the current value of the roll table',
            scope: 'world',
            config: false,
            default: 'sunny',
            type: String,
        });
        gameInstance.settings.register('pf2e-kingmaker-tools', 'currentRegion', {
            name: 'Current Region',
            hint: 'Region used for random encounters',
            scope: 'world',
            config: false,
            default: 'Rostland',
            type: String,
        });
        gameInstance.settings.register('pf2e-kingmaker-tools', 'currentEncounterDCModifier', {
            name: 'Current Encounter DC Modifier',
            scope: 'world',
            config: false,
            default: 0,
            type: Number,
        });
        gameInstance.settings.register('pf2e-kingmaker-tools', 'proxyEncounterTable', {
            name: 'Proxy Random Encounter Table',
            hint: 'Name of the in world roll table that is rolled first to check what kind of encounter is rolled. Use the string "Creature" to roll on the region roll table in the proxy roll table or link another roll table of your choice. Leave blank to always roll on the region random encounter tables.',
            scope: 'world',
            config: true,
            default: '',
            type: String,
        });
        gameInstance.settings.register<string, string, string>('pf2e-kingmaker-tools', 'randomEncounterRollMode', {
            name: 'Random Encounter Roll Mode',
            scope: 'world',
            config: true,
            default: 'gmroll',
            type: String,
            choices: rollModeChoices,
        });
        gameInstance.settings.register('pf2e-kingmaker-tools', 'skillCheckMacroCharacterNames', {
            name: 'Skill Check Macro Character Names',
            hint: 'A string of character names separated by ||, e.g. Jake||John||The Undertaker',
            scope: 'world',
            config: true,
            default: '',
            type: String,
        });
        gameInstance.settings.register<string, string, string>('pf2e-kingmaker-tools', 'kingdomEventRollMode', {
            name: 'Kingdom Events Roll Mode',
            scope: 'world',
            config: true,
            default: 'gmroll',
            type: String,
            choices: rollModeChoices,
        });
        gameInstance.settings.register('pf2e-kingmaker-tools', 'kingdomEventsTable', {
            name: 'Kingdom Events Table Name',
            scope: 'world',
            config: true,
            default: 'Kingdom Events',
            type: String,
        });
        gameInstance.settings.register('pf2e-kingmaker-tools', 'selectedCompanions', {
            name: 'Selected Companions',
            scope: 'client',
            config: false,
            default: '',
            type: String,
        });
        gameInstance.settings.register('pf2e-kingmaker-tools', 'lastCookedMeal', {
            name: 'Last Cooked Meal',
            scope: 'client',
            config: false,
            default: '',
            type: String,
        });
        gameInstance.settings.register('pf2e-kingmaker-tools', 'lastCookingSkill', {
            name: 'Last Cooking Skill',
            scope: 'client',
            config: false,
            default: '',
            type: String,
        });
        gameInstance.settings.register('pf2e-kingmaker-tools', 'knownRecipes', {
            name: 'Known Recipes',
            scope: 'client',
            config: false,
            default: '["Basic Meal", "Hearty Meal"]',
            type: String,
        });
        gameInstance.settings.register<string, string, boolean>('pf2e-kingmaker-tools', 'useSelectedCharacter', {
            name: 'Subsist: Use user\'s character instead of token',
            hint: 'If true, a player can use the Subsist macro on the overland map without having the token to be present. Set to false if you always want to roll this for a selected token.',
            default: true,
            config: true,
            type: Boolean,
            scope: 'world',
        });
        Hooks.on('updateWorldTime', async (_, delta) => {
            if (getBooleanSetting(gameInstance, 'autoRollWeather')
                && isGm(gameInstance)
                && dayHasChanged(gameInstance, delta)
            ) {
                await rollKingmakerWeather(gameInstance);
            }
        });
        Hooks.on('canvasReady', async () => {
            if (isGm(gameInstance)) {
                await syncWeather(gameInstance);
            }
        });
    }
});
