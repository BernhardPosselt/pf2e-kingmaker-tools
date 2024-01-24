import {Ability} from './abilities';

export const allSkills = [
    'agriculture',
    'arts',
    'boating',
    'defense',
    'engineering',
    'exploration',
    'folklore',
    'industry',
    'intrigue',
    'magic',
    'politics',
    'scholarship',
    'statecraft',
    'trade',
    'warfare',
    'wilderness',
] as const;

export type Skill = typeof allSkills[number];

export const skillAbilities: Record<Skill, Ability> = {
    agriculture: 'stability',
    arts: 'culture',
    boating: 'economy',
    defense: 'stability',
    engineering: 'stability',
    exploration: 'economy',
    folklore: 'culture',
    industry: 'economy',
    intrigue: 'loyalty',
    magic: 'culture',
    politics: 'loyalty',
    scholarship: 'culture',
    statecraft: 'loyalty',
    trade: 'economy',
    warfare: 'loyalty',
    wilderness: 'stability',
};

export const allSkillRanks = ['untrained', 'trained', 'expert', 'master', 'legendary'] as const;