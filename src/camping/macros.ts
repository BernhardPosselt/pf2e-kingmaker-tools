import {postDegreeOfSuccessMessage} from '../utils';
import {getRegionInfo} from './regions';


/* eslint-disable @typescript-eslint/no-explicit-any */
export async function subsist(game: Game, actor: any): Promise<void> {
    /* eslint-disable @typescript-eslint/no-explicit-any */
    const pf2e = game.pf2e as unknown as any;
    const {zoneDC} = getRegionInfo(game);
    if (actor) {
        const result = await actor.skills.survival.roll({
            modifiers: [new pf2e.Modifier({
                type: 'untyped',
                modifier: -5,
                label: 'Subist After Exploring',
            })],
            dc: zoneDC,
            extraRollOptions: ['action:subsist'],
        });
        await postDegreeOfSuccessMessage(result.degreeOfSuccess, {
            critSuccess: `${actor.name} provide a subsistence living for themselves and one additional creature`,
            success: `${actor.name} provide a subsistence living for themselves`,
            failure: `${actor.name} are exposed to the elements and don’t get enough food, becoming @UUID[Compendium.pf2e.conditionitems.HL2l2VRSaQHu9lUw]{Fatigued} until they attain sufficient food and shelter`,
            critFailure: `${actor.name} attract trouble, eat something they shouldn’t, or otherwise worsen their situation. They take a –2 circumstance penalty to checks to Subsist for 1 week @UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.tWDpGhZivvosT4QO]{Subsist: Critical Failure}. They don’t find any food at all; if they don’t have any stored up, they’re in danger of starving or dying of thirst if they continue failing`,
        });
    } else {
        ui.notifications?.error('Please select a character in your config');
    }
}


