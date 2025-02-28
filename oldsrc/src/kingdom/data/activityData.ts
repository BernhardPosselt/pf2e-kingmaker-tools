import {KingdomPhase} from './activities';
import {Skill} from './skills';
import {Modifier, Predicate} from '../modifiers';
import {Ruin} from './ruin';
import {unslugify} from '../../utils';
import {ResourceMode, ResourceTurn, RolledResources} from '../resources';

export type SkillRanks = Partial<Record<Skill, number>>;

export interface ChatModifier extends Modifier {
    renderPredicate?: Predicate[];
}

export interface ActivityResult {
    msg: string;
    modifiers?: ChatModifier[];
}

export interface ActivityResults {
    criticalSuccess?: ActivityResult;
    success?: ActivityResult;
    failure?: ActivityResult;
    criticalFailure?: ActivityResult;
}

export interface ActivityContent extends ActivityResults {
    title: string;
    description: string;
    requirement?: string;
    special?: string;
    skills: SkillRanks;
    phase: KingdomPhase;
    dc: 'control' | 'custom' | 'none' | 'scouting' | number;
    dcAdjustment?: number;
    enabled: boolean;
    fortune: boolean;
    oncePerRound: boolean;
    hint?: string;
}

export interface KingdomActivity extends ActivityContent {
    id: string;
}

export type KingdomActivityById = Record<string, KingdomActivity>;

interface CreateResourceButton {
    type: RolledResources,
    mode?: ResourceMode,
    turn?: ResourceTurn,
    value: string,
    hints?: string;
    multiple?: boolean;
}

export function gainRuin(type: Ruin, value: number | string): string {
    return createResourceButton({value: `${value}`, type});
}

export function loseRuin(type: Ruin, value: number | string): string {
    return createResourceButton({value: `${value}`, type, mode: 'lose'});
}

export function loseRP(value: number | string, multiple = false): string {
    return createResourceButton({value: `${value}`, type: 'resource-points', mode: 'lose', multiple});
}

export function gainUnrest(value: number | string): string {
    return createResourceButton({value: `${value}`, type: 'unrest'});
}

export function loseUnrest(value: number | string): string {
    return createResourceButton({value: `${value}`, type: 'unrest', mode: 'lose'});
}

export function createResourceButton({
                                         turn = 'now',
                                         value,
                                         mode = 'gain',
                                         type,
                                         hints,
                                         multiple = false,
                                     }: CreateResourceButton): string {
    const turnLabel = turn === 'now' ? '' : ' Next Turn';
    const label = `${mode === 'gain' ? 'Gain' : 'Lose'} ${value} ${unslugify(type)}${turnLabel}`;
    return `<button type="button" class="km-gain-lose" data-type="${type}" data-mode="${mode}" data-turn="${turn}" data-multiple="${multiple}" ${value !== undefined ? `data-value="${value}"` : ''}>${label}${hints !== undefined ? `(${hints})` : ''}</button>`;
}

export function getKingdomActivitiesById(game: Game,additionalActivities: KingdomActivity[]): KingdomActivityById {
    return <KingdomActivityById>Object.fromEntries(getKingdomActivities(game, additionalActivities)
        .map(activity => [activity.id, activity]));
}

export function getKingdomActivities(game: Game, additionalActivities: KingdomActivity[]): KingdomActivity[] {
    const homebrewIds = new Set(additionalActivities.map(a => a.id));
    return game.pf2eKingmakerTools.migration.data.activities
        .filter(activity => !homebrewIds.has(activity.id))
        .concat(additionalActivities);
}