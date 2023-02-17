import {Modifier, removeLowestModifiers, removePredicatedModifiers} from '../../src/kingdom/modifiers';

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

describe('removeLowestModifiers', () => {
    test('remove lowest modifiers if at least one is enabled per type', () => {
        const modifiers: Modifier[] = [{
            name: 'highest',
            enabled: true,
            value: 3,
            type: 'ability',
        }, {
            name: 'match',
            enabled: true,
            value: 2,
            type: 'ability',
        }];
        const result = removeLowestModifiers(modifiers);
        expect(result.length).toBe(1);
        expect(result[0].name).toBe('highest');
    });

    test('remove on modifiers if no one is enabled per type', () => {
        const modifiers: Modifier[] = [{
            name: 'highest',
            enabled: false,
            value: 3,
            type: 'ability',
        }, {
            name: 'match',
            enabled: false,
            value: 2,
            type: 'ability',
        }];
        const result = removeLowestModifiers(modifiers);
        expect(result.length).toBe(2);
    });

    test('if two modifiers have the same value and both are enabled, only keep the first one', () => {
        const modifiers: Modifier[] = [{
            name: 'first',
            enabled: true,
            value: 3,
            type: 'ability',
        }, {
            name: 'second',
            enabled: true,
            value: 3,
            type: 'ability',
        }, {
            name: 'third',
            enabled: true,
            value: -3,
            type: 'ability',
        }, {
            name: 'fourth',
            enabled: true,
            value: -3,
            type: 'ability',
        }];
        const result = removeLowestModifiers(modifiers);
        expect(result.length).toBe(2);
        expect(result[0].name).toBe('first');
        expect(result[1].name).toBe('third');
    });
});
