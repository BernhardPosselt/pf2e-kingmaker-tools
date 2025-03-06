import {getKingdom, saveKingdom} from './storage';


export async function addOngoingEvent(actor: Actor, uuid: string, label: string): Promise<void> {
    const kingdom = getKingdom(actor);
    const name = `@UUID[${uuid}]{${label}}`;
    await saveKingdom(actor, {
        ongoingEvents: [...kingdom.ongoingEvents, {name}],
    });
}
