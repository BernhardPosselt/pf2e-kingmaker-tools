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

export const allTrainedSkillRanks = ['trained', 'expert', 'master', 'legendary'] as const;