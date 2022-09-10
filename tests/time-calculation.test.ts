import {TimeChangeMode, TimeOfDay, TimeOfYear, TimeUnit} from '../src/time/calculation';
import {DateTime} from 'luxon';
import {formatWorldTime} from '../src/time/format';

const noon = DateTime.fromObject({
    day: 1,
    month: 1,
    year: 2022,
    hour: 12,
    minute: 0,
    second: 0,
});

describe('time of day calculation', () => {
    test('advancing/rewinding to same time of day should skip/rewind a day', () => {
        const time = new TimeOfDay({hour: 12, minute: 0, second: 0});

        expect(time.diffSeconds(noon, TimeChangeMode.ADVANCE)).toBe(24 * 60 * 60);
        expect(time.diffSeconds(noon, TimeChangeMode.RETRACT)).toBe(-24 * 60 * 60);
    });

    test('advancing from 12:00:00 to 12:59:59', () => {
        const time = new TimeOfDay({hour: 12, minute: 59, second: 59});

        expect(time.diffSeconds(noon, TimeChangeMode.ADVANCE)).toBe(59 * 60 + 59);
    });

    test('retracting from 12:00:00 to 11:00:01', () => {
        const time = new TimeOfDay({hour: 11, minute: 0, second: 1});

        expect(time.diffSeconds(noon, TimeChangeMode.RETRACT)).toBe((59 * 60 + 59) * -1);
    });

    test('advancing from 12:00:00 to 11:59:59', () => {
        const time = new TimeOfDay({hour: 11, minute: 59, second: 59});

        expect(time.diffSeconds(noon, TimeChangeMode.ADVANCE)).toBe(23 * 60 * 60 + 59 * 60 + 59);
    });

    test('retracting from 12:00:00 to 12:00:01', () => {
        const time = new TimeOfDay({hour: 12, minute: 0, second: 1});

        expect(time.diffSeconds(noon, TimeChangeMode.RETRACT)).toBe((23 * 60 * 60 + 59 * 60 + 59) * -1);
    });
});

describe('time unit', () => {
    it('test hours advance', () => {
        const hour = new TimeUnit({
            hour: 1,
        });
        const result = hour.diffSeconds(noon, TimeChangeMode.ADVANCE);
        expect(result).toBe(3600);
    });

    it('test hours retract', () => {
        const hour = new TimeUnit({
            hour: 1,
        });
        const seconds = hour.diffSeconds(noon, TimeChangeMode.RETRACT);
        expect(seconds).toBe(-3600);
    });
});

describe('time of year', () => {
    it('advance one day', () => {
        const timeOfYear = new TimeOfYear({
            day: 2,
            month: 1,
            year: 2022,
            hour: 12,
            minute: 0,
            second: 0,
        });
        const seconds = timeOfYear.diffSeconds(noon);
        expect(seconds).toBe(24 * 3600);
    });

    it('retract one day', () => {
        const timeOfYear = new TimeOfYear({
            day: 31,
            month: 12,
            year: 2021,
            hour: 12,
            minute: 0,
            second: 0,
        });
        const seconds = timeOfYear.diffSeconds(noon);
        expect(seconds).toBe(-24 * 3600);
    });
});

describe('time formatting', () => {
    it('should format a date', () => {
        const result = formatWorldTime(noon, 'AR');
        expect(result).toBe('Starday, 1st of Abadius, 4722 AR (12:00:00)');
    });

});
