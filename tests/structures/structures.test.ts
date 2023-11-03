import {evaluateStructures, groupAvailableItems} from '../../src/kingdom/structures';
import {ItemLevelBonuses} from '../../src/kingdom/data/structures';

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
            magical: 1,
            other: 1,
            luxuryMagical: 1,
            luxuryArcane: 1,
            luxuryPrimal: 1,
            luxuryDivine: 1,
            luxuryOccult: 1,
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
            luxuryMagical: -1,
            luxuryPrimal: -1,
            luxuryDivine: -1,
            luxuryArcane: -1,
            luxuryOccult: -1,
            magical: -1,
            other: -1,
        });
    });

    test('categories do not stack across different structures', () => {
        const result = evaluateStructures([{
            name: 'a',
            preventItemLevelPenalty: true,
        }, {
            name: 'b',
            availableItemsRules: [{
                value: 1,
                group: 'divine',
            }],
        }, {
            name: 'c',
            availableItemsRules: [{
                value: 1,
                group: 'divine',
            }],
        }], 15, 'same-structures-stack');
        expect(result.itemLevelBonuses).toEqual({
            divine: 1,
            alchemical: 0,
            primal: 0,
            occult: 0,
            arcane: 0,
            luxuryOccult: 0,
            luxuryArcane: 0,
            luxuryDivine: 1,
            luxuryMagical: 0,
            luxuryPrimal: 0,
            magical: 0,
            other: 0,
        });
    });

    test('other item level bonuses always stack', () => {
        const result = evaluateStructures([{
            name: 'a',
            preventItemLevelPenalty: true,
        }, {
            name: 'b',
            availableItemsRules: [{
                value: 1,
            }],
        }, {
            name: 'c',
            availableItemsRules: [{
                value: 1,
            }],
        }], 15, 'same-structures-stack');
        expect(result.itemLevelBonuses).toEqual({
            divine: 2,
            alchemical: 2,
            primal: 2,
            occult: 2,
            arcane: 2,
            luxuryOccult: 2,
            luxuryArcane: 2,
            luxuryDivine: 2,
            luxuryMagical: 2,
            luxuryPrimal: 2,
            magical: 2,
            other: 2,
        });
    });

    test('magic item should stack with more specific ones', () => {
        const result = evaluateStructures([{
            name: 'a',
            preventItemLevelPenalty: true,
        }, {
            name: 'b',
            availableItemsRules: [{
                value: 1,
                group: 'divine',
            }],
        }, {
            name: 'b',
            availableItemsRules: [{
                value: 1,
                group: 'divine',
            }],
        }, {
            name: 'b',
            availableItemsRules: [{
                value: 1,
                group: 'divine',
            }],
        }, {
            name: 'c',
            availableItemsRules: [{
                value: 1,
                group: 'magical',
            }],
        }, {
            name: 'c',
            availableItemsRules: [{
                value: 1,
                group: 'magical',
            }],
        }, {
            name: 'c',
            availableItemsRules: [{
                value: 1,
                group: 'magical',
            }],
        }], 15, 'same-structures-stack');
        expect(result.itemLevelBonuses).toEqual({
            divine: 6,
            alchemical: 0,
            primal: 3,
            occult: 3,
            arcane: 3,
            luxuryOccult: 3,
            luxuryArcane: 3,
            luxuryDivine: 6,
            luxuryMagical: 3,
            luxuryPrimal: 3,
            magical: 3,
            other: 0,
        });
    });

    test('ungrouped should stack with magical and specific ones', () => {
        const result = evaluateStructures([{
            name: 'a',
            preventItemLevelPenalty: true,
        }, {
            name: 'b',
            availableItemsRules: [{
                value: 1,
                group: 'divine',
            }],
        }, {
            name: 'b',
            availableItemsRules: [{
                value: 1,
                group: 'divine',
            }],
        }, {
            name: 'b',
            availableItemsRules: [{
                value: 1,
                group: 'divine',
            }],
        }, {
            name: 'd',
            availableItemsRules: [{
                value: 1,
                maximumStacks: 3,
            }],
        }, {
            name: 'd',
            availableItemsRules: [{
                value: 1,
                maximumStacks: 3,
            }],
        }, {
            name: 'd',
            availableItemsRules: [{
                value: 1,
                maximumStacks: 3,
            }],
        }, {
            name: 'd',
            availableItemsRules: [{
                value: 1,
                maximumStacks: 3,
            }],
        }, {
            name: 'e',
            availableItemsRules: [{
                value: 1,
                group: 'luxury',
            }],
        }], 15, 'same-structures-stack');
        expect(result.itemLevelBonuses).toEqual({
            divine: 6,
            alchemical: 3,
            primal: 3,
            occult: 3,
            arcane: 3,
            magical: 3,
            luxuryOccult: 4,
            luxuryArcane: 4,
            luxuryDivine: 7,
            luxuryMagical: 4,
            luxuryPrimal: 4,
            other: 3,
        });
    });

    test('should limit max stacks', () => {
        const result = evaluateStructures([{
            name: 'a',
            preventItemLevelPenalty: true,
        }, {
            name: 'b',
            availableItemsRules: [{
                value: 2,
                group: 'divine',
                maximumStacks: 2,
            }],
        }, {
            name: 'b',
            availableItemsRules: [{
                value: 2,
                group: 'divine',
                maximumStacks: 2,
            }],
        }, {
            name: 'b',
            availableItemsRules: [{
                value: 2,
                group: 'divine',
                maximumStacks: 2,
            }],
        }], 15, 'same-structures-stack');
        expect(result.itemLevelBonuses).toEqual({
            divine: 4,
            alchemical: 0,
            primal: 0,
            occult: 0,
            arcane: 0,
            magical: 0,
            luxuryOccult: 0,
            luxuryArcane: 0,
            luxuryDivine: 4,
            luxuryMagical: 0,
            luxuryPrimal: 0,
            other: 0,
        });
    });

    test('max stacks should be respected', () => {
        const result = evaluateStructures([{
            name: 'a',
            preventItemLevelPenalty: true,
        }, {
            name: 'b',
            availableItemsRules: [{
                value: 1,
                group: 'divine',
                maximumStacks: 2,
            }],
        }, {
            name: 'b',
            availableItemsRules: [{
                value: 1,
                group: 'divine',
                maximumStacks: 2,
            }],
        }, {
            name: 'b',
            availableItemsRules: [{
                value: 1,
                group: 'divine',
                maximumStacks: 2,
            }],
        }, {
            name: 'b',
            availableItemsRules: [{
                value: 1,
                group: 'divine',
                maximumStacks: 2,
            }],
        }], 15, 'same-structures-stack');
        expect(result.itemLevelBonuses).toEqual({
            divine: 2,
            alchemical: 0,
            primal: 0,
            occult: 0,
            arcane: 0,
            luxuryOccult: 0,
            luxuryArcane: 0,
            luxuryDivine: 2,
            luxuryMagical: 0,
            luxuryPrimal: 0,
            magical: 0,
            other: 0,
        });
    });
});

describe('group available items', () => {

    it('should group luxury bonuses', () => {
        const actual = groupAvailableItems(createBonuses({
            luxuryMagical: 3,
            luxuryArcane: 4,
        }));
        expect(actual).toEqual({
            other: 0,
            luxuryMagical: 3,
            luxuryArcane: 4,
            luxuryDivine: 0,
            luxuryPrimal: 0,
            luxuryOccult: 0,
        });
    });

    it('should group magical bonuses', () => {
        const actual = groupAvailableItems(createBonuses({
            magical: 3,
            luxuryMagical: 3,
            luxuryArcane: 3,
            luxuryDivine: 3,
            luxuryPrimal: 3,
            luxuryOccult: 3,
            arcane: 4,
            divine: 3,
            occult: 3,
            primal: 3,
        }));
        expect(actual).toEqual({
            other: 0,
            magical: 3,
            arcane: 4,
        });
    });

});

function createBonuses(overrides: Partial<ItemLevelBonuses>): ItemLevelBonuses {
    return {
        divine: 0,
        alchemical: 0,
        primal: 0,
        occult: 0,
        arcane: 0,
        luxuryOccult: 0,
        luxuryArcane: 0,
        luxuryDivine: 0,
        luxuryMagical: 0,
        luxuryPrimal: 0,
        magical: 0,
        other: 0,
        ...overrides,
    };
}