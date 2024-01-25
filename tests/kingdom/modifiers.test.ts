import {ModifierWithId, removeLowestModifiers, removePredicatedModifiers} from '../../src/kingdom/modifiers';
import {getKingdomActivitiesById} from '../../src/kingdom/data/activityData';

describe('predicate modifiers', () => {
    test('only keep very specific one', () => {
        const modifiers: ModifierWithId[] = [{
            id: '1',
            name: 'match',
            enabled: true,
            abilities: ['culture'],
            value: 3,
            skills: ['arts'],
            type: 'ability',
            phases: ['leadership'],
            activities: ['create-a-masterpiece'],
        }, {
            id: '2',
            name: 'no-match1',
            enabled: true,
            abilities: ['economy'],
            value: 3,
            skills: ['arts'],
            type: 'ability',
            phases: ['leadership'],
            activities: ['create-a-masterpiece'],
        }, {
            id: '3',
            name: 'no-match2',
            enabled: true,
            abilities: ['culture'],
            value: 3,
            skills: ['agriculture'],
            type: 'ability',
            phases: ['leadership'],
            activities: ['create-a-masterpiece'],
        }, {
            id: '4',
            name: 'no-match3',
            enabled: true,
            abilities: ['culture'],
            value: 3,
            skills: ['arts'],
            type: 'ability',
            phases: ['event'],
            activities: ['create-a-masterpiece'],
        }, {
            id: '5',
            name: 'no-match4',
            enabled: true,
            abilities: ['culture'],
            value: 3,
            skills: ['arts'],
            type: 'ability',
            phases: ['leadership'],
            activities: ['spread-the-legend'],
        }];
        const activities = getKingdomActivitiesById([]);
        const result = removePredicatedModifiers(modifiers, 'leadership', 'create-a-masterpiece', 'arts', 1, activities);
        expect(result.length).toBe(1);
        expect(result[0].name).toBe('match');
    });

    test('do not keep modifier if rank is not possible', () => {
        const modifiers: ModifierWithId[] = [{
            id: '1',
            name: 'match',
            enabled: true,
            abilities: ['culture'],
            value: 3,
            skills: ['arts'],
            type: 'ability',
            phases: ['leadership'],
            activities: ['create-a-masterpiece'],
        }];
        const activities = getKingdomActivitiesById([]);
        const result = removePredicatedModifiers(modifiers, 'event', 'create-a-masterpiece', 'arts', 0, activities);
        expect(result.length).toBe(0);
    });

    test('should add activity in leadership phase', () => {
        const modifiers: ModifierWithId[] = [{
            id: '1',
            name: 'match',
            enabled: true,
            value: 3,
            type: 'ability',
            activities: ['new-leadership'],
        }];
        const activities = getKingdomActivitiesById([]);
        const result = removePredicatedModifiers(modifiers, 'upkeep', 'new-leadership', 'intrigue', 0, activities);
        expect(result.length).toBe(1);
    });
});

describe('removeLowestModifiers', () => {
    test('remove lowest modifiers if at least one is enabled per type', () => {
        const modifiers: ModifierWithId[] = [{
            id: '1',
            name: 'highest',
            enabled: true,
            value: 3,
            type: 'ability',
        }, {
            id: '2',
            name: 'match',
            enabled: true,
            value: 2,
            type: 'ability',
        }];
        const result = removeLowestModifiers(modifiers);
        expect(result.length).toBe(2);
        expect(result[0].name).toBe('highest');
        expect(result[0].enabled).toBe(true);
        expect(result[1].enabled).toBe(false);
    });

    test('remove no modifiers if no one is enabled per type', () => {
        const modifiers: ModifierWithId[] = [{
            id: '1',
            name: 'highest',
            enabled: false,
            value: 3,
            type: 'ability',
        }, {
            id: '2',
            name: 'match',
            enabled: false,
            value: 2,
            type: 'ability',
        }];
        const result = removeLowestModifiers(modifiers);
        expect(result.length).toBe(2);
    });

    test('remove no modifiers if no one is enabled per type and both are the same', () => {
        const modifiers: ModifierWithId[] = [{
            id: '1',
            name: 'highest',
            enabled: false,
            value: 1,
            type: 'circumstance',
        }, {
            id: '2',
            name: 'match',
            enabled: true,
            value: 1,
            type: 'circumstance',
        }];
        const result = removeLowestModifiers(modifiers);
        expect(result.length).toBe(2);
        expect(result[0].name).toBe('highest');
        expect(result[0].enabled).toBe(false);
        expect(result[1].name).toBe('match');
        expect(result[1].enabled).toBe(true);
    });

    test('if two modifiers have the same value and both are enabled, only enable the first one', () => {
        const modifiers: ModifierWithId[] = [{
            id: '1',
            name: 'first',
            enabled: true,
            value: 3,
            type: 'ability',
        }, {
            id: '2',
            name: 'second',
            enabled: true,
            value: 3,
            type: 'ability',
        }, {
            id: '3',
            name: 'third',
            enabled: true,
            value: -3,
            type: 'ability',
        }, {
            id: '4',
            name: 'fourth',
            enabled: true,
            value: -3,
            type: 'ability',
        }];
        const result = removeLowestModifiers(modifiers);
        expect(result.length).toBe(4);
        expect(result[0].name).toBe('first');
        expect(result[0].enabled).toBe(true);
        expect(result[1].name).toBe('second');
        expect(result[1].enabled).toBe(false);
        expect(result[2].name).toBe('third');
        expect(result[2].enabled).toBe(true);
        expect(result[3].name).toBe('fourth');
        expect(result[3].enabled).toBe(false);
    });
});
