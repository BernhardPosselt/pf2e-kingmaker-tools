import {calculateArmyData, CalculatedArmy, createDefaultArmy} from '../../src/armies/utils';

describe('Calculate Army', () => {
    test('createArmyData', () => {
        const army = createDefaultArmy();
        army.level = 10;
        army.highSave = 'maneuver';
        army.adjustments = {
            morale: 1,
            scouting: 2,
            ac: 3,
            recruitmentDC: 4,
            maneuver: 5,
            melee: -6,
            ranged: 7,
        };
        const calculated = calculateArmyData(army);
        const expected: CalculatedArmy = {
            ...army,
            calculated: {
                ac: 33,
                meleeModifier: 17,
                rangedModifier: 30,
                scouting: 21,
                recruitmentDC: 31,
                morale: 17,
                maneuver: 27,
                maximumTactics: 3,
                initiative: 21,
                consumption: 0,
                hp: 0,
                routeThreshold: 0,
            },
        };
        expect(calculated).toEqual(expected);
    });
});
