import Joi from 'joi';
import {allSkills} from './data/skills';
import {allBuildingTraits, itemGroups, structuresByName} from './data/structures';

const builtInRefs = structuresByName.keys();
export const refSchema = Joi.object({
    ref: Joi.string()
        .valid(...builtInRefs),
});

export const ruleSchema = Joi.object({
    name: Joi.string().required(),
    notes: Joi.string().optional(),
    preventItemLevelPenalty: Joi.boolean().optional(),
    enableCapitalInvestment: Joi.boolean().optional(),
    increaseLeadershipActivities: Joi.boolean().optional(),
    consumptionReduction: Joi.number().optional(),
    activityBonusRules: Joi.array().items(Joi.object({
        value: Joi.number().required(),
        activity: Joi.string().required(),
    })).optional(),
    skillBonusRules: Joi.array().items(Joi.object({
        value: Joi.number().required(),
        skill: Joi.string().valid(...allSkills).required(),
        activity: Joi.string().optional(),
    })).optional(),
    availableItemsRules: Joi.array().items(Joi.object({
        value: Joi.number().required(),
        group: Joi.string().valid(...itemGroups).optional(),
        maximumStacks: Joi.number().optional(),
    })).optional(),
    settlementEventRules: Joi.array().items(Joi.object({
        value: Joi.number().required(),
    })).optional(),
    leadershipActivityRules: Joi.array().items(Joi.object({
        value: Joi.number().required(),
    })).optional(),
    storage: Joi.object({
        ore: Joi.number().optional(),
        food: Joi.number().optional(),
        lumber: Joi.number().optional(),
        stone: Joi.number().optional(),
        luxuries: Joi.number().optional(),
    }).optional(),
    unlockActivities: Joi.array().items(Joi.string()).optional(),
    traits: Joi.array().items(Joi.string().valid(...allBuildingTraits)).optional(),
    isBridge: Joi.boolean().optional(),
    stacksWith: Joi.string().optional(),
    lots: Joi.number().optional(),
    level: Joi.number().optional(),
    affectsEvents: Joi.boolean().optional(),
    upgradeFrom: Joi.array().items(Joi.string()).optional(),
    affectsDowntime: Joi.boolean().optional(),
    reducesUnrest: Joi.boolean().optional(),
    reducesRuin: Joi.boolean().optional(),
    construction: Joi.object({
        skills: Joi.array().items(Joi.object({
            skill: Joi.array().valid(...allSkills),
            proficiencyRank: Joi.number().valid(0, 1, 2, 3, 4).optional(),
        })),
        lumber: Joi.number().optional(),
        luxuries: Joi.number().optional(),
        ore: Joi.number().optional(),
        stone: Joi.number().optional(),
        rp: Joi.number(),
        dc: Joi.number(),
    }),
});
