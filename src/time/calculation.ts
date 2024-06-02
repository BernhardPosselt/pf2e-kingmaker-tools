import {DateTime, DayNumbers, DurationLikeObject, HourNumbers, MinuteNumbers, MonthNumbers, SecondNumbers} from 'luxon';


export enum TimeChangeMode {
    ADVANCE,
    RETRACT,
}

export function getWorldTime(game: Game): DateTime {
    const value = game.settings.get('pf2e', 'worldClock.worldCreatedOn') as string;
    return DateTime.fromISO(value)
        .toUTC()
        .plus({seconds: game.time.worldTime});
}

export interface TimeDiff {
    /**
     * Returns positive or negative number of seconds to add to current
     * game time advance function https://foundryvtt.com/api/GameTime.html#advance
     * @param worldTime the current time as luxon DateTime
     * @param mode whether to go back to that point in time or to advance
     */
    diffSeconds(worldTime: DateTime, mode: TimeChangeMode): number;
}

/**
 * Use this to represent a time unit that you want to advance or retract, e.g. 1 day or 10 minutes
 */
export class TimeUnit implements TimeDiff {
    constructor(public readonly unit: DurationLikeObject) {
    }

    diffSeconds(worldTime: DateTime, mode: TimeChangeMode): number {
        const targetTime = mode == TimeChangeMode.ADVANCE ? worldTime.plus(this.unit) : worldTime.minus(this.unit);
        return targetTime.diff(worldTime, 'seconds').seconds;
    }
}

/**
 * Use this to represent a date. Golarion calendar not included since
 * that is only used for rendering
 */
export class TimeOfYear implements TimeDiff {
    public readonly year: number;
    public readonly month: MonthNumbers;
    public readonly day: DayNumbers;
    public readonly hour: HourNumbers;
    public readonly minute: MinuteNumbers;
    public readonly second: SecondNumbers;

    constructor(
        {
            year,
            month,
            day,
            hour = 0,
            minute = 0,
            second = 0,
        }: {
            year: number,
            month: MonthNumbers,
            day: DayNumbers,
            hour: HourNumbers,
            minute: MinuteNumbers,
            second: SecondNumbers,
        },
    ) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    public diffSeconds(worldTime: DateTime): number {
        const targetTime = DateTime.fromObject({
            year: this.year,
            month: this.month,
            day: this.day,
            hour: this.hour,
            minute: this.minute,
            second: this.second,
        });

        return targetTime.diff(worldTime, 'seconds').seconds;
    }
}

/**
 * Use this if you want to advance or retract to a time of day, e.g. advance to 16:00.
 * Automatically figures out if you need to advance or retract a day
 */
export class TimeOfDay implements TimeDiff {
    public readonly hour: HourNumbers;
    public readonly minute: MinuteNumbers;
    public readonly second: SecondNumbers;

    constructor(
        {
            hour = 0,
            minute = 0,
            second = 0,
        }: {
            hour: HourNumbers,
            minute: MinuteNumbers,
            second: SecondNumbers,
        },
    ) {
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    /** Point in morning twilight where dim light begins */
    static DAWN = new TimeOfDay({hour: 4, minute: 58, second: 54});

    static NOON = new TimeOfDay({hour: 12, minute: 0, second: 0});

    /** Point in evening twilight where dim light begins */
    static DUSK = new TimeOfDay({hour: 18, minute: 34, second: 6});

    static MIDNIGHT = new TimeOfDay({hour: 0, minute: 0, second: 0});

    public diffSeconds(worldTime: DateTime, mode: TimeChangeMode): number {
        const targetTime = worldTime.set({
            hour: this.hour,
            minute: this.minute,
            second: this.second,
        });
        const targetDayDifference = TimeOfDay.diffDays(worldTime, targetTime, mode);
        const targetDay = worldTime.plus({day: targetDayDifference});

        return targetDay.set(this).diff(worldTime, 'seconds').seconds;
    }

    private static diffDays(currentTime: DateTime, targetTime: DateTime, mode: TimeChangeMode): -1 | 0 | 1 {
        // if we have the same point in time, we always want to either skip or rewind a full day
        if (currentTime >= targetTime && mode === TimeChangeMode.ADVANCE) {
            // case: now: 12:01 and advance to 12:00 -> we need to add 1 day to calculate the difference
            return 1;
        } else if (currentTime <= targetTime && mode === TimeChangeMode.RETRACT) {
            // case: now: 12:00 and retract to 12:01 -> we need to subtract 1 day to calculate the difference
            return -1;
        } else {
            return 0;
        }
    }
}

export function getTimeOfDayPercent(time: DateTime): number {
    const elapsedSeconds = time.second + time.minute * 60 + time.hour * 3600;
    return elapsedSeconds / (36 * 24);
}

export function isDayOrNight(time: DateTime): 'day' | 'night' {
    if (time.hour >= 6 && time.hour < 18) {
        return 'day';
    } else {
        return 'night';
    }
}
