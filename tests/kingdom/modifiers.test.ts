import {Modifier, removePredicatedModifiers} from '../../src/kingdom/modifiers';

describe('predicate modifiers', () => {
    test('only keep very specific one', () => {
        const modifiers: Modifier[] = [{
            name: 'match',
            enabled: true,
            abilities: ['culture'],
            value: 3,
            skills: ['arts'],
            type: 'ability',
            phases: ['leadership'],
            activities: ['create-a-masterpiece'],
        }, {
            name: 'no-match1',
            enabled: true,
            abilities: ['economy'],
            value: 3,
            skills: ['arts'],
            type: 'ability',
            phases: ['leadership'],
            activities: ['create-a-masterpiece'],
        }, {
            name: 'no-match2',
            enabled: true,
            abilities: ['culture'],
            value: 3,
            skills: ['agriculture'],
            type: 'ability',
            phases: ['leadership'],
            activities: ['create-a-masterpiece'],
        }, {
            name: 'no-match3',
            enabled: true,
            abilities: ['culture'],
            value: 3,
            skills: ['arts'],
            type: 'ability',
            phases: ['event'],
            activities: ['create-a-masterpiece'],
        }, {
            name: 'no-match4',
            enabled: true,
            abilities: ['culture'],
            value: 3,
            skills: ['arts'],
            type: 'ability',
            phases: ['leadership'],
            activities: ['spread-the-legend'],
        }];
        const result = removePredicatedModifiers(modifiers, 'leadership', 'create-a-masterpiece', 'arts', 1);
        expect(result.length).toBe(1);
        expect(result[0].name).toBe('match');
    });

    test('do not keep modifier if rank is not possible', () => {
        const modifiers: Modifier[] = [{
            name: 'match',
            enabled: true,
            abilities: ['culture'],
            value: 3,
            skills: ['arts'],
            type: 'ability',
            phases: ['leadership'],
            activities: ['create-a-masterpiece'],
        }];
        const result = removePredicatedModifiers(modifiers, 'event', 'create-a-masterpiece', 'arts', 0);
        expect(result.length).toBe(0);
    });
});
