import {DateTime} from 'luxon';
import {RollTableDraw} from '@league-of-foundry-developers/foundry-vtt-types/src/foundry/client/data/documents/table';
import {DegreeOfSuccess} from './degree-of-success';
import {WeatherEffectName} from './weather/data';
import {ModuleData} from '@league-of-foundry-developers/foundry-vtt-types/src/foundry/common/packages.mjs';

declare global {
    declare class PF2EModifier {
        constructor({type: string, modifier: number, label: string})
    }

    interface Game {
        pf2eKingmakerTools: {
            macros: {
                structureTokenMappingMacro: () => void,
                viewKingdomMacro: () => void,
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
            } & Collection<PF2EAction>
            Modifier: typeof PF2EModifier
        };
    }

    interface PF2EAction {
        use(options: { actors: Actor[] }): Promise<unknown>;
    }

    // fix roll table types
    interface RollTable {
        draw(options?: Partial<RollTable.DrawOptions>): Promise<RollTableDraw>;
    }

    interface RollResult {
        degreeOfSuccess: DegreeOfSuccess;
    }

    export type RollMode = 'publicroll' | 'gmroll' | 'blindroll' | 'selfroll';

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
            condition: Item[];
        };
        prototypeToken: TokenDocument;

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
            traits: {
                rarity: 'common' | 'uncommon' | 'rare' | 'unique';
            }
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
            resources: {
                heroPoints: {
                    value: number;
                }
            }
        };
    }

    interface ArmyActor {
        system: {
            recruitmentDC: number;
            consumption: number;
            scouting: number;
            traits: {
                type: 'skirmisher' | 'cavalry' | 'siege' | 'infantry';
            };
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

    interface ItemSystem {
        traits: {
            value: string[]
        };
        bonus: {
            value: number
        };
        weaponType: {
            value: string
        };
        damageRolls: Record<string, { damage: number }>;
    }

    interface Item<S = ItemSystem> {
        id: string;
        name: string;
        sourceId: string;
        sheet: ItemSheet;
        type: 'effect' | 'consumable' | 'melee' | 'weapon' | 'condition' | 'campaignFeature';
        system: S;
    }

    interface Scene {
        grid: {
            type: number;
            size: number;
        };
    }

    interface EffectItem {
        isExpired: boolean;
        slug: string;
        badge: {
            value: number;
        };
    }

    interface Playlist {
        _source: {
            _id: string;
        };
    }

    interface TokenDocument {
        height: number;
        width: number;
        x: number;
        y: number;
        texture: {
            src: string;
        };
    }

    interface TileDocument {
        width: number;
        height: number;
        x: number;
        y: number;
    }

    interface DrawingDocument {
        shape: {
            width: number;
            height: number;
        };
        x: number;
        y: number;
    }

    interface Scene {
        weather: WeatherEffectName;
    }

    const kingmaker: Kingmaker;

    interface HexFeature {
        type: 'landmark' | 'refuge' | 'ruin' | 'structure' | 'farmland' | 'road' | 'bridge' | 'ford' | 'waterfall' | 'hazard' | 'bloom' | 'freehold' | 'village' | 'town' | 'city' | 'metropolis';
    }

    type CommodityType = 'ore' | 'lumber' | 'stone' | 'food' | 'luxuries';

    type CampType = 'quarry' | 'mine' | 'lumber';

    interface HexState {
        commodity?: CommodityType;
        camp?: CampType;
        features?: HexFeature[];
        claimed?: boolean;
    }

    interface KingmakerState {
        hexes: Record<number, HexState>;
    }

    interface Kingmaker extends ModuleData {
        state: KingmakerState;
    }

    interface CampaignSystem {
        campaign: string;
        category: string;
        traits: {
            value: string[];
        };
        level: {
            value: number;
        };
    }

    interface CampaignFeaturePF2E extends Item<CampaignSystem> {
    }
}
