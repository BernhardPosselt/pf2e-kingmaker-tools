export const allLeaderTypes = ['pc', 'regularNpc', 'highlyMotivatedNpc', 'nonPathfinderNpc'] as const;

export type LeadershipLeaderType = typeof allLeaderTypes[number];
