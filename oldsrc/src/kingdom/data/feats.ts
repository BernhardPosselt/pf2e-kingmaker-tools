import {isNonNullable} from '../../utils';
import {Skill} from './skills';
import {Modifier, Predicate} from '../modifiers';
import {Kingdom} from "./kingdom";
import {StringDegreeOfSuccess} from "../../degree-of-success";

export interface UpgradeResult {
    upgrade: StringDegreeOfSuccess
    predicate?: Predicate[];
}

export interface KingdomFeat {
    name: string;
    level: number;
    text: string;
    prerequisites?: string;
    automationNotes?: string;
    modifiers?: Modifier[];
    resourceDice?: number;
    settlementItemLevelIncrease?: number;
    trainSkill?: Skill;
    assuranceForSkill?: Skill;
    increaseUsableSkills?: Partial<Record<Skill, Skill[]>>;
    flags?: string[];
    upgradeResults?: UpgradeResult[];
}

export function getAllFeats(game: Game, kingdom: Kingdom): KingdomFeat[] {
    return game.pf2eKingmakerTools.migration.data.feats;
}

export function getAllSelectedFeats(game: Game, kingdom: Kingdom): KingdomFeat[] {
    const feats = getAllFeats(game, kingdom);
    const allFeatsByName = new Map<string, KingdomFeat>();
    feats.forEach(f => allFeatsByName.set(f.name, f));
    const featIds = new Set([...kingdom.feats.map(f => f.id), ...kingdom.bonusFeats.map(f => f.id)])
    return Array.from(featIds)
        .map(id => allFeatsByName.get(id))
        .filter(feat => isNonNullable(feat)) as KingdomFeat[];
}