import {evaluate} from '../src/structures';

describe('structures', () => {
    test('evaluate no buildings', () => {
        const result = evaluate([], 3);
        expect(result.notes.length).toBe(0);
        expect(result.allowCapitalInvestment).toBe(false);
    });

    test('item bonuses should stack if from same building', () => {
        const result = evaluate([{
            name: 'a',
            kingdomSkillRules: [{
                value: 1,
                skill: 'defense',
                predicate: ['action:provide-care'],
            }],
        }, {
            name: 'b',
            kingdomSkillRules: [{
                value: 1,
                skill: 'defense',
                predicate: ['action:provide-care'],
            }],
        }, {
            name: 'a',
            kingdomSkillRules: [{
                value: 1,
                skill: 'defense',
                predicate: ['action:provide-care'],
            }],
        }], 2);
        expect(result.skillBonuses.defense.value).toBe(0);
        expect(result.skillBonuses.defense?.actions?.['provide-care']).toBe(2);
    });

    test('item bonuses from skills should override action bonuses', () => {
        const result = evaluate([{
            name: 'a',
            kingdomSkillRules: [{
                value: 1,
                skill: 'defense',
                predicate: ['action:provide-care'],
            }],
        }, {
            name: 'b',
            kingdomSkillRules: [{
                value: 2,
                skill: 'defense',
            }],
        }], 2);
        expect(result.skillBonuses.defense.value).toBe(2);
        expect(Object.keys(result.skillBonuses.defense?.actions)).toStrictEqual([]);
        expect(result.skillBonuses.defense?.actions?.['provide-care']).toBe(undefined);
    });

    test('max item bonuses should override skill bonuses', () => {
        const result = evaluate([{
            name: 'b',
            kingdomSkillRules: [{
                value: 2,
                skill: 'defense',
            }],
        }], 1);
        expect(result.skillBonuses.defense.value).toBe(1);
    });

    test('should calculate item level bonuses', () => {
        const result = evaluate([{
            name: 'b',
            availableItemsRules: [{
                value: 1,
            }],
            preventItemLevelPenalty: true,
        }], 3);
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
        const result = evaluate([{
            name: 'b',
            availableItemsRules: [{
                value: 1,
            }],
        }], 3);
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

    test('should calcualte complex example', () => {
        const result = evaluate([{
            name: 'b',
            availableItemsRules: [{
                value: 1,
            }, {
                value: 1,
            }, {
                value: 1,
                predicate: ['item:trait:magical'],
            }, {
                value: 2,
                predicate: ['item:trait:arcane'],
            }],
        }], 3);
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

    // dedup notes
});
