import {DateTime} from 'luxon';
import {RollTableDraw} from '@league-of-foundry-developers/foundry-vtt-types/src/foundry/client/data/documents/table';
import {DegreeOfSuccess} from './degree-of-success';
import {WeatherEffectName} from './weather/data';
import {ModuleData} from '@league-of-foundry-developers/foundry-vtt-types/src/foundry/common/packages.mjs';
import {KingdomSettings} from "./kingdom/data/kingdom";
import {Structure} from "./kingdom/data/structures";
import {KingdomFeat} from "./kingdom/data/feats";
import {CombinedKingdomFeature} from "./kingdom/data/features";
import {KingdomActivity} from "./kingdom/data/activityData";
import {SkillStats} from "./kingdom/sheet";

declare global {
    declare class PF2EModifier {
        constructor({type: string, modifier: number, label: string})
    }

    interface Game {
        pf2eKingmakerTools: {
            macros: {
                structureTokenMappingMacro: () => void,
                kingdomEventsMacro: () => void,
                cultEventsMacro: () => void,
                viewKingdomMacro2: () => void,
            };
            migration: {
                kingdomSettings: (KingdomSettings, onSave: (settings: KingdomSettings) => void) => void,
                kingdomSizeHelp: () => void,
                settlementSizeHelp: () => void,
                structureXpDialog: (onSave: (xp: number) => void) => void,
                editSettlementDialog: (autoLevel: boolean,
                                 settlementName: string,
                                 settlement: Settlement,
                                 onOk: (Settlement) => void) => void,
                addOngoingEventDialog: (onSave: (event: string) => void) => void,
                data: {
                    structures: Structure[];
                    feats: KingdomFeat[];
                    features: CombinedKingdomFeature[];
                    activities: KingdomActivity[];
                }
                adjustUnrest: (kingdom: KingdomData) => Promise<number>
                collectResources: (kingdom: KingdomData) => Promise<{ rp: number, ore: number, lumber:number, luxuries: number, rd: number, stone: number }>
                checkDialog: (
                    game: Game,
                    kingdom: KingdomData,
                    kingdomActor: PF2ENpc,
                    activity?: KingdomActivity,
                    structure?: StructureData,
                    skill?: string,
                    afterRoll?: (DegreeOfSuccess) => Promise<string>
                ) => void
                armyBrowser: (
                    game: Game,
                    kingdom: KingdomData,
                    kingdomActor: PF2ENpc,
                ) => void
                tacticsBrowser: (
                    game: Game,
                    kingdom: KingdomData,
                    kingdomActor: PF2ENpc,
                ) => void
                addModifier: () => void
                tickDownModifiers: () => Promise<void>
                calculateSkillModifiers: (
                    game: Game,
                    kingdom: KingdomData,
                ) => Promise<SkillStats[]>
                calculateLeadershipBonuses: (
                    kingdom: KingdomData
                ) => Promise<Map<string, number>>
                calculateUnrestPenalty: (unrest: number) => number
                calculateHexXP: (
                    hexes: number,
                    xpPerClaimedHex: number,
                    kingdomSize: number,
                    useVK: boolean
                ) => number
                calculateRpXP: (
                    rp: number,
                    kingdomLevel: number,
                    rpToXpConversionRate: number,
                    rpToXpConversionLimit: number,
                    useVK: boolean,
                ) => number
                calculateEventXP: (modifier: Int) => number,
                findMaximumArmyTactics: (kingdomLevel) => number,
            }
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
        type: 'effect' | 'consumable' | 'melee' | 'weapon' | 'condition' | 'campaignFeature' | 'lore';
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
