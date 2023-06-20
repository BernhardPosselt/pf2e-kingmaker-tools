import {DateTime} from 'luxon';
import {RollTableDraw} from '@league-of-foundry-developers/foundry-vtt-types/src/foundry/client/data/documents/table';

declare global {
    interface Game {
        pf2eKingmakerTools: {
            macros: {
                toggleWeatherMacro: () => void,
                toTimeOfDayMacro: () => void,
                kingdomEventsMacro: () => void,
                rollKingmakerWeatherMacro: () => void,
                viewKingdomMacro: () => void,
                openCampingSheet: () => void,
                viewArmyMacro: (actor: Actor, token: Token) => void,
                /* eslint-disable @typescript-eslint/no-explicit-any */
                subsistMacro: (actor: any) => void,
                /* eslint-disable @typescript-eslint/no-explicit-any */
                editStructureMacro: (actor: any) => Promise<void>,
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

    // fix roll table types
    interface RollTable {
        draw(options?: Partial<RollTable.DrawOptions>): Promise<RollTableDraw>;
    }
}
