import {Modifier} from "../modifiers";
import {Kingdom} from "./kingdom";
import {range} from "../../utils";

export interface KingdomFeature {
    level: number;
    name: string;
    description: string;
    flags?: string[];
    modifiers?: Modifier[];
}

export interface CombinedKingdomFeature {
    levels: number[];
    name: string;
    description: string;
    flags?: string[];
    modifiers?: Modifier[];
}

export function getAllFeatures(game: Game, kingdom: Kingdom): KingdomFeature[] {
    const features = deepClone(game.pf2eKingmakerTools.migration.data.features);
    if (kingdom.settings.kingdomSkillIncreaseEveryLevel) {
        const increase = features.find(f => f.name === 'Skill Increase');
        if (increase) {
            increase.levels = range(2, 21)
        }
    }
    return features.flatMap(feature => {
        return feature.levels.map(level => {
            const explodedFeature: KingdomFeature = {
                name: feature.name,
                modifiers: feature.modifiers,
                flags: feature.flags,
                description: feature.description,
                level,
            };
            return explodedFeature
        })
    });
}