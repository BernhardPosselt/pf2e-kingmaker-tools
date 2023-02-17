import Joi from 'joi';
import {allSkills} from './data/skills';
import {allActivities} from './data/activities';
import {itemGroups, structuresByName} from './data/structures';

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
        activity: Joi.string().valid(...allActivities).required(),
    })).optional(),
    skillBonusRules: Joi.array().items(Joi.object({
        value: Joi.number().required(),
        skill: Joi.string().valid(...allSkills).required(),
        activity: Joi.string().valid(...allActivities).optional(),
    })).optional(),
    availableItemsRules: Joi.array().items(Joi.object({
        value: Joi.number().required(),
        group: Joi.string().valid(...itemGroups).optional(),
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
    unlockActivities: Joi.array().items(Joi.string().valid(...allActivities)).optional(),
});
