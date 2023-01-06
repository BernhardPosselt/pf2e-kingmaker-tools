import {DateTime} from 'luxon';
import {RollTableDraw} from '@league-of-foundry-developers/foundry-vtt-types/src/foundry/client/data/documents/table';
import {FxMaster, WeatherEffects} from './fxmaster';

declare global {
    interface Game {
        pf2eKingmakerTools: {
            macros: {
                toggleWeatherMacro: () => void,
                toTimeOfDayMacro: () => void,
                randomEncounterMacro: () => void,
                kingdomEventsMacro: () => void,
                postCompanionEffectsMacro: () => void,
                rollKingmakerWeatherMacro: () => void,
                /* eslint-disable @typescript-eslint/no-explicit-any */
                subsistMacro: (actor: any) => void,
                /* eslint-disable @typescript-eslint/no-explicit-any */
                huntAndGatherMacro: (actor: any) => Promise<void>,
                /* eslint-disable @typescript-eslint/no-explicit-any */
                camouflageCampsiteMacro: (actor: any) => Promise<void>,
                /* eslint-disable @typescript-eslint/no-explicit-any */
                organizeWatchMacro: (actor: any) => Promise<void>,
                /* eslint-disable @typescript-eslint/no-explicit-any */
                tellCampfireStoryMacro: (actor: any) => Promise<void>,
                /* eslint-disable @typescript-eslint/no-explicit-any */
                prepareCampsiteMacro: (actor: any) => Promise<void>,
                /* eslint-disable @typescript-eslint/no-explicit-any */
                cookRecipeMacro: (actor: any) => Promise<void>,
                /* eslint-disable @typescript-eslint/no-explicit-any */
                discoverSpecialMealMacro: (actor: any) => Promise<void>,
                rollExplorationSkillCheck: (skill: string, effect: string) => Promise<void>,
                rollSkillDialog: () => Promise<void>,
            };
        };
        pf2e: {
            worldClock: {
                worldTime: DateTime;
                month: string;
            }
        }
    }

    interface Window {
        FXMASTER: FxMaster
    }

    // fix roll table types
    interface RollTable {
        draw(options?: Partial<RollTable.DrawOptions>): Promise<RollTableDraw>;
    }

    interface Hooks {
        call<T extends WeatherEffects>(hook: 'fxmaster.switchParticleEffects', parameter: {
            type: T['type'],
            name: string,
            options: T['options'],
        }): boolean;
        call(hook: 'fxmaster.updateParticleEffects', parameter: WeatherEffects[]): boolean;
    }
}
