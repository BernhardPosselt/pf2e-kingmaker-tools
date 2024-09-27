export interface ArmyStatistic {
    level: number;
    scouting: number;
    standardDC: number;
    ac: number;
    highSave: number;
    lowSave: number;
    attack: number;
    maximumTactics: number;
}

const allArmyStatistics: ArmyStatistic[] = [
    {level: 1, scouting: 7, standardDC: 15, ac: 16, highSave: 10, lowSave: 4, attack: 9, maximumTactics: 1},
    {level: 2, scouting: 8, standardDC: 16, ac: 18, highSave: 11, lowSave: 5, attack: 11, maximumTactics: 1},
    {level: 3, scouting: 9, standardDC: 18, ac: 19, highSave: 12, lowSave: 6, attack: 12, maximumTactics: 1},
    {level: 4, scouting: 11, standardDC: 19, ac: 21, highSave: 14, lowSave: 8, attack: 14, maximumTactics: 2},
    {level: 5, scouting: 12, standardDC: 20, ac: 22, highSave: 15, lowSave: 9, attack: 15, maximumTactics: 2},
    {level: 6, scouting: 14, standardDC: 22, ac: 24, highSave: 17, lowSave: 11, attack: 17, maximumTactics: 2},
    {level: 7, scouting: 15, standardDC: 23, ac: 25, highSave: 18, lowSave: 12, attack: 18, maximumTactics: 2},
    {level: 8, scouting: 16, standardDC: 24, ac: 27, highSave: 19, lowSave: 13, attack: 20, maximumTactics: 3},
    {level: 9, scouting: 18, standardDC: 26, ac: 28, highSave: 21, lowSave: 15, attack: 21, maximumTactics: 3},
    {level: 10, scouting: 19, standardDC: 27, ac: 30, highSave: 22, lowSave: 16, attack: 23, maximumTactics: 3},
    {level: 11, scouting: 21, standardDC: 28, ac: 31, highSave: 24, lowSave: 18, attack: 24, maximumTactics: 3},
    {level: 12, scouting: 22, standardDC: 30, ac: 33, highSave: 25, lowSave: 19, attack: 26, maximumTactics: 4},
    {level: 13, scouting: 23, standardDC: 31, ac: 34, highSave: 26, lowSave: 20, attack: 27, maximumTactics: 4},
    {level: 14, scouting: 25, standardDC: 32, ac: 36, highSave: 28, lowSave: 22, attack: 29, maximumTactics: 4},
    {level: 15, scouting: 26, standardDC: 34, ac: 37, highSave: 29, lowSave: 23, attack: 30, maximumTactics: 4},
    {level: 16, scouting: 28, standardDC: 35, ac: 39, highSave: 30, lowSave: 25, attack: 32, maximumTactics: 5},
    {level: 17, scouting: 29, standardDC: 36, ac: 40, highSave: 32, lowSave: 26, attack: 33, maximumTactics: 5},
    {level: 18, scouting: 30, standardDC: 38, ac: 42, highSave: 33, lowSave: 27, attack: 35, maximumTactics: 5},
    {level: 19, scouting: 32, standardDC: 39, ac: 43, highSave: 35, lowSave: 29, attack: 36, maximumTactics: 5},
    {level: 20, scouting: 33, standardDC: 40, ac: 45, highSave: 36, lowSave: 30, attack: 38, maximumTactics: 6},
];

export const armyStatisticsByLevel: Map<number, ArmyStatistic> = new Map<number, ArmyStatistic>();
allArmyStatistics.forEach(army => armyStatisticsByLevel.set(army.level, army));
