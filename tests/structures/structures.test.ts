import {evaluateStructures} from '../../src/kingdom/structures';

describe('structures', () => {
    test('evaluate no buildings', () => {
        const result = evaluateStructures([], 3, 'same-structures-stack');
        expect(result.notes.length).toBe(0);
        expect(result.allowCapitalInvestment).toBe(false);
    });

    test('item bonuses should stack if from same building', () => {
        const result = evaluateStructures([{
            name: 'a',
            skillBonusRules: [{
                value: 1,
                skill: 'defense',
                activity: 'provide-care',
            }],
        }, {
            name: 'b',
            skillBonusRules: [{
                value: 1,
                skill: 'defense',
                activity: 'provide-care',
            }],
        }, {
            name: 'a',
            skillBonusRules: [{
                value: 1,
                skill: 'defense',
                activity: 'provide-care',
            }],
        }], 10, 'same-structures-stack');
        expect(result.skillBonuses.defense.value).toBe(0);
        expect(result.skillBonuses.defense?.activities?.['provide-care']).toBe(2);
    });

    test('vance & kerenshara: item bonuses should stack', () => {
        const result = evaluateStructures([{
            name: 'a',
            skillBonusRules: [{
                value: 1,
                skill: 'defense',
                activity: 'provide-care',
            }],
        }, {
            name: 'b',
            skillBonusRules: [{
                value: 1,
                skill: 'defense',
                activity: 'provide-care',
            }],
        }, {
            name: 'a',
            skillBonusRules: [{
                value: 1,
                skill: 'defense',
                activity: 'provide-care',
            }],
        }], 10, 'all-structures-stack');
        expect(result.skillBonuses.defense.value).toBe(0);
        expect(result.skillBonuses.defense?.activities?.['provide-care']).toBe(3);
    });

    test('vance & kerenshara: item bonuses should stack with skill only bonuses', () => {
        const result = evaluateStructures([{
            name: 'a',
            skillBonusRules: [{
                value: 1,
                skill: 'defense',
                activity: 'provide-care',
            }],
        }, {
            name: 'b',
            skillBonusRules: [{
                value: 1,
                skill: 'defense',
                activity: 'provide-care',
            }],
        }, {
            name: 'c',
            skillBonusRules: [{
                value: 1,
                skill: 'defense',
            }],
        }], 10, 'all-structures-stack');
        expect(result.skillBonuses.defense.value).toBe(1);
        expect(result.skillBonuses.defense?.activities?.['provide-care']).toBe(3);
    });

    test('item bonuses from skills should override action bonuses', () => {
        const result = evaluateStructures([{
            name: 'a',
            skillBonusRules: [{
                value: 1,
                skill: 'defense',
                activity: 'provide-care',
            }],
        }, {
            name: 'b',
            skillBonusRules: [{
                value: 2,
                skill: 'defense',
            }],
        }], 10, 'same-structures-stack');
        expect(result.skillBonuses.defense.value).toBe(2);
        expect(Object.keys(result.skillBonuses.defense?.activities)).toStrictEqual([]);
        expect(result.skillBonuses.defense?.activities?.['provide-care']).toBe(undefined);
    });

    test('max item bonuses should override skill bonuses', () => {
        const result = evaluateStructures([{
            name: 'b',
            skillBonusRules: [{
                value: 2,
                skill: 'defense',
            }],
        }], 1, 'same-structures-stack');
        expect(result.skillBonuses.defense.value).toBe(1);
    });

    test('should calculate item level bonuses', () => {
        const result = evaluateStructures([{
            name: 'b',
            availableItemsRules: [{
                value: 1,
            }],
            preventItemLevelPenalty: true,
        }], 15, 'same-structures-stack');
        expect(result.itemLevelBonuses).toEqual({
            divine: 1,
            alchemical: 1,
            primal: 1,
            occult: 1,
            arcane: 1,
            luxury: 1,
            magical: 1,
            other: 1,
        });
    });

    test('should apply item level penalty', () => {
        const result = evaluateStructures([{
            name: 'b',
            availableItemsRules: [{
                value: 1,
            }],
        }], 15, 'same-structures-stack');
        expect(result.itemLevelBonuses).toEqual({
            divine: -1,
            alchemical: -1,
            primal: -1,
            occult: -1,
            arcane: -1,
            luxury: -1,
            magical: -1,
            other: -1,
        });
    });

    test('should calculate complex example', () => {
        const result = evaluateStructures([{
            name: 'b',
            availableItemsRules: [{
                value: 1,
            }, {
                value: 1,
            }, {
                value: 1,
                group: 'magical',
            }, {
                value: 2,
                group: 'arcane',
            }],
        }], 15, 'same-structures-stack');
        expect(result.itemLevelBonuses).toEqual({
            divine: 1,
            alchemical: 0,
            primal: 1,
            occult: 1,
            arcane: 2,
            luxury: 0,
            magical: 1,
            other: 0,
        });
    });
});
