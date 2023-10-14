import {DateTime} from 'luxon';
import {RollTableDraw} from '@league-of-foundry-developers/foundry-vtt-types/src/foundry/client/data/documents/table';
import {DegreeOfSuccess} from './degree-of-success';
import {WeatherEffectName} from './weather/data';

declare global {
    declare class PF2EModifier {
        constructor({type: string, modifier: number, label: string})
    }

    interface Game {
        pf2eKingmakerTools: {
            macros: {
                toggleCombatTracksMacro: () => void,
                armyHelpMacro: () => void,
                toggleWeatherMacro: () => void,
                toggleShelteredMacro: () => void,
                setCurrentWeatherMacro: () => void,
                sceneWeatherSettingsMacro: () => void,
                toTimeOfDayMacro: () => void,
                kingdomEventsMacro: () => void,
                rollKingmakerWeatherMacro: () => void,
                viewKingdomMacro: () => void,
                openCampingSheet: () => void,
                editArmyStatisticsMacro: (actor: Actor) => void,
                /* eslint-disable @typescript-eslint/no-explicit-any */
                editStructureMacro: (actor: any) => Promise<void>,
                rollExplorationSkillCheck: (skill: string, effect: string) => Promise<void>,
                rollSkillDialog: () => Promise<void>,
                awardXpMacro: () => Promise<void>,
                resetHeroPointsMacro: () => Promise<void>,
                /* eslint-disable @typescript-eslint/no-explicit-any */
                setSceneCombatPlaylistDialogMacro: (actor: Actor | undefined) => Promise<void>,
            };
        };
        pf2e: {
            worldClock: {
                worldTime: DateTime;
                month: string;
            }
            actions: {
                restForTheNight: (options: { actors: Actor[], skipDialog?: boolean }) => Promise<void>;
                subsist: (options: { actors: Actor[], skill: string, difficultyClass?: { value: number }; }) => void;
            }
            Modifier: typeof PF2EModifier
        };
    }

    // fix roll table types
    interface RollTable {
        draw(options?: Partial<RollTable.DrawOptions>): Promise<RollTableDraw>;
    }

    interface RollResult {
        degreeOfSuccess: DegreeOfSuccess;
    }

    interface RollOptions {
        dc?: { value: number };
        extraRollOptions?: string[];
        rollMode?: 'publicroll' | 'roll' | 'gmroll' | 'blindroll' | 'selfroll';
    }

    interface ActorSkill {
        rank: number;
        roll: (data: RollData) => Promise<null | RollResult>;
    }

    interface Actor {
        id: string;
        perception: ActorSkill;
        level: number;
        itemTypes: {
            consumable: Item[];
            effect: Item[];
            equipment: Item[];
            action: Item[];
        };

        addToInventory(value: object, container?: Item, newStack: false): Promise<Item | null>;

        createEmbeddedDocuments(type: 'Item', data: object[]): Promise<void>;

        skills: Record<string, ActorSkill>;
        attributes: {
            hp: { value: number, max: number },
        };
        abilities: {
            con: { mod: number }
        };
        system: {
            details: {
                xp: { value: number, max: number },
                level: { value: number }
            };
            attributes: {
                ac: { value: number },
                perception: { value: number },
                hp: { value: number, max: number },
            }
            exploration?: string[];
            saves: {
                fortitude: { value: number },
                reflex: { value: number },
                will: { value: number },
            },
        };
    }

    class ItemSheet {
        render: (force: true, args?: Record<string, string>) => void;
    }

    class JournalEntryPage {
        id: string;
        parent?: {
            sheet?: ItemSheet
        };
    }

    interface Item {
        id: string;
        name: string;
        sourceId: string;
        sheet: ItemSheet;
        type: 'effect' | 'consumable' | 'melee' | 'weapon';
        system: {
            traits: {
                value: string[]
            }
            bonus: {
                value: number
            };
            weaponType: {
                value: string
            }
            damageRolls: Record<string, { damage: number }>
        };
    }

    interface EffectItem {
        isExpired: boolean;
    }

    interface ConsumableItem {
        system: {
            charges: {
                value: number;
                max: number;
            }
            quantity: number;
        };
        quantity: number;
    }

    interface Playlist {
        _source: {
            _id: string;
        };
    }

    interface TokenDocument {
        height: number;
        width: number;
    }

    interface Scene {
        weather: WeatherEffectName;
    }
}
