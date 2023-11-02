import {Camping} from '../camping/camping';
import {Migration} from './migration';
import {ActivityEffect} from '../camping/activities';

export class Migration5 extends Migration {
    constructor() {
        super(5);
    }

    override async migrateCamping(game: Game, camping: Camping): Promise<void> {
        camping.homebrewCampingActivities.forEach(activity => {
            if (activity.criticalSuccess && activity.criticalSuccess.effectUuids) {
                this.migrateActivity(activity.criticalSuccess.effectUuids);
            }
            if (activity.success && activity.success.effectUuids) {
                this.migrateActivity(activity.success.effectUuids);
            }
            if (activity.failure && activity.failure.effectUuids) {
                this.migrateActivity(activity.failure.effectUuids);
            }
            if (activity.criticalFailure && activity.criticalFailure.effectUuids) {
                this.migrateActivity(activity.criticalFailure.effectUuids);
            }
            if (activity.effectUuids) {
                this.migrateActivity(activity.effectUuids);
            }
        });
    }

    private migrateActivity(effectUuids: ActivityEffect[]): void {
        effectUuids.forEach(activity => {
            if ('targetAll' in activity) {
                if (activity.targetAll === false) {
                    activity.target = 'self';
                } else {
                    activity.target = 'all';
                }
                delete activity.targetAll;
            }
        });
    }
}