import {dayHasChanged, syncWeather, toggleWeather} from './weather';
import {isGm} from './utils';
import {toTimeOfDayMacro} from './time/app';
import {getBooleanSetting, getStringSetting} from './settings';
import {rollKingmakerWeather} from './kingmaker-weather';
import {rollExplorationSkillCheck, rollSkillDialog} from './skill-checks';
import {rollKingdomEvent} from './kingdom-events';
import {showKingdom} from './kingdom/sheet';
import {showStructureEditDialog} from './kingdom/dialogs/edit-structure-rules';
import {getKingdom} from './kingdom/storage';
import {addOngoingEvent, changeDegree, parseUpgradeMeta, reRoll} from './kingdom/rolls';
import {kingdomChatButtons} from './kingdom/chat-buttons';
import {StringDegreeOfSuccess} from './degree-of-success';
import {showArmy} from './armies/sheet';
import {openCampingSheet} from './camping/sheet';
import {bindCampingChatEventListeners} from './camping/chat';
import {getDiffListeners} from './camping/effect-syncing';
import {getCamping, getCampingActor} from './camping/storage';
import {resetHeroPoints, showAwardXPDialog} from './macros';

Hooks.on('ready', async () => {
    if (game instanceof Game) {
        const gameInstance = game;
        gameInstance.pf2eKingmakerTools = {
            macros: {
                toggleWeatherMacro: toggleWeather.bind(null, game),
                toTimeOfDayMacro: toTimeOfDayMacro.bind(null, game),
                kingdomEventsMacro: rollKingdomEvent.bind(null, game),
                rollKingmakerWeatherMacro: rollKingmakerWeather.bind(null, game),
                viewKingdomMacro: showKingdom.bind(null, game),
                awardXpMacro: showAwardXPDialog.bind(null, game),
                resetHeroPointsMacro: resetHeroPoints.bind(null, game),
                viewArmyMacro: (actor: Actor, token: Token): Promise<void> => showArmy(gameInstance, actor, token),
                /* eslint-disable @typescript-eslint/no-explicit-any */
                openCampingSheet: (): void => openCampingSheet(gameInstance),
                rollExplorationSkillCheck: async (skill: string, effect: string): Promise<void> => {
                    const actors = canvas?.scene?.tokens
                        ?.filter(t => t !== null
                            && t.actor !== null
                            && (t.actor.type === 'character' || t.actor.type === 'familiar'))
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
                            ?.filter(actor => (actor.type === 'character' || actor.type === 'familiar')
                                && actor.name !== null
                                && actorNames.has(actor.name)) ?? [];
                        await rollSkillDialog(actors);
                    }
                },
                editStructureMacro: async (actor: any): Promise<void> => {
                    if (actor === undefined) {
                        ui.notifications?.error('Please select an actor');
                    } else {
                        await showStructureEditDialog(gameInstance, actor);
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
        gameInstance.settings.register<string, string, number>('pf2e-kingmaker-tools', 'weatherHazardRange', {
            name: 'Weather Hazard Range',
            hint: 'Maximum Level of Weather Event that can occur. Added to Average Party Level.',
            default: 4,
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
        gameInstance.settings.register('pf2e-kingmaker-tools', 'kingdomCultTable', {
            name: 'Kingdom Cult Events Table Name',
            scope: 'world',
            config: true,
            default: 'Random Cult Events',
            type: String,
        });
        gameInstance.settings.register('pf2e-kingmaker-tools', 'vanceAndKerensharaXP', {
            name: 'Enable Vance and Kerenshara XP rules',
            hint: 'Adds additional Milestone Events, more XP for claiming hexes and RP',
            scope: 'world',
            config: true,
            default: false,
            requiresReload: true,
            type: Boolean,
        } as any);
        gameInstance.settings.register('pf2e-kingmaker-tools', 'kingdomAlwaysAddLevel', {
            name: 'Always add Level to Skill',
            hint: 'If enabled, always adds the kingdom\'s level to a skill, even if it is untrained',
            scope: 'world',
            config: true,
            default: false,
            requiresReload: true,
            type: Boolean,
        } as any);
        gameInstance.settings.register('pf2e-kingmaker-tools', 'kingdomSkillIncreaseEveryLevel', {
            name: 'Double Skill Increases',
            hint: 'If enabled, adds Skill Increases for all even levels from level 2 onwards',
            scope: 'world',
            config: true,
            default: false,
            requiresReload: true,
            type: Boolean,
        } as any);
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
        checkKingdomErrors(gameInstance);

        // listen for camping sheet open
        const listeners = getDiffListeners(gameInstance);
        gameInstance.socket!.on('module.pf2e-kingmaker-tools', (data: { action: string }) => {
            if (data.action === 'openCampingSheet') {
                openCampingSheet(gameInstance);
            } else if (data.action === 'openKingdomSheet') {
                showKingdom(gameInstance);
            } else {
                // players changed data and GM needs to sync effects
                const sheetActor = getCampingActor(gameInstance);
                if (sheetActor && isGm(gameInstance)) {
                    const camping = getCamping(sheetActor);
                    for (const listener of listeners) {
                        if (listener.canHandle(data.action)) {
                            listener.onReceive(camping);
                        }
                    }
                }
            }
        });
    }
});

Hooks.on('init', async () => {
    await loadTemplates([
        'modules/pf2e-kingmaker-tools/templates/camping/activity.partial.hbs',
        'modules/pf2e-kingmaker-tools/templates/camping/eating.partial.hbs',
        'modules/pf2e-kingmaker-tools/templates/army/sidebar.hbs',
        'modules/pf2e-kingmaker-tools/templates/army/status.hbs',
        'modules/pf2e-kingmaker-tools/templates/army/gear.hbs',
        'modules/pf2e-kingmaker-tools/templates/army/effects.hbs',
        'modules/pf2e-kingmaker-tools/templates/army/actions.hbs',
        'modules/pf2e-kingmaker-tools/templates/army/conditions.hbs',
        'modules/pf2e-kingmaker-tools/templates/army/tactics.hbs',
        'modules/pf2e-kingmaker-tools/templates/kingdom/sidebar.hbs',
        'modules/pf2e-kingmaker-tools/templates/kingdom/status.hbs',
        'modules/pf2e-kingmaker-tools/templates/kingdom/skills.hbs',
        'modules/pf2e-kingmaker-tools/templates/kingdom/turn.hbs',
        'modules/pf2e-kingmaker-tools/templates/kingdom/groups.hbs',
        'modules/pf2e-kingmaker-tools/templates/kingdom/feats.hbs',
        'modules/pf2e-kingmaker-tools/templates/kingdom/features.hbs',
        'modules/pf2e-kingmaker-tools/templates/kingdom/settlements.hbs',
        'modules/pf2e-kingmaker-tools/templates/kingdom/settlement.hbs',
        'modules/pf2e-kingmaker-tools/templates/kingdom/effects.hbs',
    ]);
});

Hooks.on('renderChatLog', () => {
    if (game instanceof Game) {
        const gameInstance = game;
        const actor = getKingdomSheetActor(gameInstance);
        if (actor) {
            const chatLog = $('#chat-log');
            for (const button of kingdomChatButtons) {
                chatLog.on('click', button.selector, (event: Event) => button.callback(gameInstance, actor, event));
            }
        }
        bindCampingChatEventListeners(gameInstance);
    }
});

type LogEntry = {
    name: string,
    icon?: string,
    condition?: (html: JQuery) => boolean,
    callback: (html: JQuery) => void,
};

function getKingdomSheetActor(game: Game): Actor | undefined {
    return game?.actors?.find(a => a.name === 'Kingdom Sheet');
}

function ifKingdomActorExists(game: Game, el: HTMLElement, callback: (actor: Actor) => void): void {
    // TODO: replace this with a data-actor-id lookup in the el
    const actor = getKingdomSheetActor(game);
    if (actor) {
        callback(actor);
    } else {
        ui.notifications?.error('Could not find actor with name Kingdom Sheet');
    }
}

Hooks.on('getChatLogEntryContext', (html: HTMLElement, items: LogEntry[]) => {
    if (game instanceof Game) {
        const gameInstance = game;
        const hasActor = (): boolean => getKingdomSheetActor(gameInstance) !== undefined;
        const hasContentLink = (el: JQuery): boolean => hasActor() && el[0].querySelector('.content-link') !== null;
        const hasMeta = (el: JQuery): boolean => hasActor() && el[0].querySelector('.km-roll-meta') !== null;
        const canChangeDegree = (chatMessage: HTMLElement, direction: 'upgrade' | 'downgrade'): boolean => {
            const cantChangeDegreeUnless: StringDegreeOfSuccess = direction === 'upgrade' ? 'criticalSuccess' : 'criticalFailure';
            const meta = chatMessage.querySelector('.km-upgrade-result');
            return hasActor() && meta !== null && parseUpgradeMeta(chatMessage).degree !== cantChangeDegreeUnless;
        };
        const canReRollUsingFame = (el: JQuery): boolean => {
            const actor = getKingdomSheetActor(gameInstance);
            return actor ? getKingdom(actor).fame.now > 0 && hasMeta(el) : false;
        };
        items.push({
            name: 'Re-Roll Using Fame/Infamy',
            icon: '<i class="fa-solid fa-dice-d20"></i>',
            condition: canReRollUsingFame,
            callback: el => ifKingdomActorExists(gameInstance, el[0], (actor) => reRoll(actor, el[0], 'fame')),
        }, {
            name: 'Re-Roll',
            condition: hasMeta,
            icon: '<i class="fa-solid fa-dice-d20"></i>',
            callback: el => ifKingdomActorExists(gameInstance, el[0], (actor) => reRoll(actor, el[0], 're-roll')),
        }, {
            name: 'Re-Roll Keep Higher',
            condition: hasMeta,
            icon: '<i class="fa-solid fa-dice-d20"></i>',
            callback: el => ifKingdomActorExists(gameInstance, el[0], (actor) => reRoll(actor, el[0], 'keep-higher')),
        }, {
            name: 'Re-Roll Keep Lower',
            condition: hasMeta,
            icon: '<i class="fa-solid fa-dice-d20"></i>',
            callback: el => ifKingdomActorExists(gameInstance, el[0], (actor) => reRoll(actor, el[0], 'keep-lower')),
        }, {
            name: 'Upgrade Degree of Success',
            condition: (el: JQuery) => canChangeDegree(el[0], 'upgrade'),
            icon: '<i class="fa-solid fa-arrow-up"></i>',
            callback: el => ifKingdomActorExists(gameInstance, el[0], (actor) => changeDegree(actor, el[0], 'upgrade')),
        }, {
            name: 'Downgrade Degree of Success',
            condition: (el: JQuery) => canChangeDegree(el[0], 'downgrade'),
            icon: '<i class="fa-solid fa-arrow-down"></i>',
            callback: el => ifKingdomActorExists(gameInstance, el[0], (actor) => changeDegree(actor, el[0], 'downgrade')),
        }, {
            name: 'Add to Ongoing Events',
            icon: '<i class="fa-solid fa-plus"></i>',
            condition: hasContentLink,
            callback: async (el) => {
                ifKingdomActorExists(gameInstance, el[0], async (actor) => {
                    const link = el[0].querySelector('.content-link') as HTMLElement | null;
                    const uuid = link?.dataset?.uuid;
                    const text = link?.innerText;
                    if (uuid && text) {
                        await addOngoingEvent(actor, uuid, text);
                    }
                });
            },
        });
    }
});

function checkKingdomErrors(game: Game): void {
    const actor = getKingdomSheetActor(game);
    if (actor && actor.getFlag('pf2e-kingmaker-tools', 'kingdom-sheet') === undefined) {
        ui.notifications?.error('Found an Actor with name "Kingdom Sheet" that has not been imported using the "View Kingdom" Macro! Please delete the actor and re-import it.');
    }
}
