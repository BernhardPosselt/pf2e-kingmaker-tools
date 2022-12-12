import {getNumberSetting, getStringSetting, setSetting} from "./settings";
import {findRollTableUuidWithFallback, findWorldTableUuid, rollRollTable} from "./roll-tables";
import {DegreeOfSuccess, determineDegreeOfSuccess} from "./degree-of-success";

interface ZoneData {
    zoneDC: number;
    encounterDC: number;
    level: number;
}

const regions = new Map<string, ZoneData>();
regions.set("Brevoy", {zoneDC: 14, encounterDC: 12, level: 0})
regions.set("Rostland Hinterlands", {zoneDC: 15, encounterDC: 12, level: 1})
regions.set("Greenbelt", {zoneDC: 16, encounterDC: 14, level: 2})
regions.set("Tuskwater", {zoneDC: 18, encounterDC: 12, level: 3})
regions.set("Kamelands", {zoneDC: 19, encounterDC: 12, level: 4})
regions.set("Narlmarches", {zoneDC: 20, encounterDC: 14, level: 5})
regions.set("Sellen Hills", {zoneDC: 20, encounterDC: 12, level: 6})
regions.set("Dunsward", {zoneDC: 18, encounterDC: 12, level: 7})
regions.set("Nomen Heights", {zoneDC: 24, encounterDC: 12, level: 8})
regions.set("Tors of Levenies", {zoneDC: 28, encounterDC: 16, level: 9})
regions.set("Hooktongue", {zoneDC: 32, encounterDC: 14, level: 10})
regions.set("Drelev", {zoneDC: 28, encounterDC: 12, level: 11})
regions.set("Tiger Lords", {zoneDC: 28, encounterDC: 12, level: 12})
regions.set("Rushlight", {zoneDC: 26, encounterDC: 12, level: 13})
regions.set("Glenebon Lowlands", {zoneDC: 30, encounterDC: 12, level: 14})
regions.set("Pitax", {zoneDC: 29, encounterDC: 12, level: 15})
regions.set("Glenebon Uplands", {zoneDC: 35, encounterDC: 12, level: 16})
regions.set("Numeria", {zoneDC: 36, encounterDC: 12, level: 17})
regions.set("Thousand Voices", {zoneDC: 43, encounterDC: 14, level: 18})
regions.set("Branthlend Mountains", {zoneDC: 41, encounterDC: 16, level: 19})

async function rollHasEncounterFlatCheck(region: string, modifier: number): Promise<boolean> {
    const data = regions.get(region)!;
    const dc = data.encounterDC + modifier;
    const flavor = `Rolling Random Encounter for terrain ${region} with Flat DC ${dc}`;
    const dieRoll = await new Roll('1d20').evaluate();
    await dieRoll.toMessage({flavor}, {rollMode: 'gmroll'});
    const degreeOfSuccess = determineDegreeOfSuccess(dieRoll.total, dieRoll.total, dc);
    return degreeOfSuccess === DegreeOfSuccess.CRITICAL_SUCCESS || degreeOfSuccess === DegreeOfSuccess.SUCCESS;
}

async function rollRandomEncounter(game: Game, region: string, modifier: number, forgoFlatCheck: boolean = false): Promise<void> {
    const creatureRollTable = (await findRollTableUuidWithFallback(game, region, 'pf2e-kingmaker-tools.kingmaker-random-encounters'))!;
    const harmlessRollTable = findWorldTableUuid(game, 'Harmless Encounters')!;
    const encounterTypeRollTable = findWorldTableUuid(game, 'Random Encounter Types')!;
    const rollOptions: Partial<RollTable.DrawOptions> = {rollMode: 'gmroll'};

    if (forgoFlatCheck || await rollHasEncounterFlatCheck(region, modifier)) {
        const typeResult = await rollRollTable(game, encounterTypeRollTable, rollOptions);
        const tableResult = typeResult.draw.results[0] as any;
        if (tableResult.text?.trim() === 'Creature') {
            console.log('Rolling on creature table ' + creatureRollTable)
            await rollRollTable(game, creatureRollTable, rollOptions);
        } else {
            await rollRollTable(game, harmlessRollTable, rollOptions);
        }
    }
}

interface RandomEncounterFormData {
    modifier: number;
    region: string;
}

interface RandomEncounterOptions {
    game: Game;
}

class RandomEncounterApp extends FormApplication<RandomEncounterOptions & FormApplicationOptions, RandomEncounterFormData, Game> {
    static override get defaultOptions() {
        const options = super.defaultOptions;
        options.id = 'random-encounter-app';
        options.title = 'Random Encounters';
        options.template = "modules/pf2e-kingmaker-tools/templates/random-encounter.html";
        options.submitOnChange = true;
        options.closeOnSubmit = false;
        return options;
    }

    private readonly game: Game;

    constructor(object: any, options: Partial<FormApplicationOptions> & RandomEncounterOptions) {
        super(object, options);
        this.game = options.game;
    }

    override getData(options?: Partial<FormApplicationOptions>) {
        const currentRegionData = regions.get(getStringSetting(this.game, "currentRegion") || 'Rostland');
        const currentEncounterDCModifier = getNumberSetting(this.game, "currentEncounterDCModifier");
        return {
            ...super.getData(options),
            regions: Array.from(regions.keys()),
            ...currentRegionData,
            currentRegion: getStringSetting(this.game, "currentRegion") || 'Rostland',
            currentEncounterDCModifier,
            encounterDC: currentEncounterDCModifier + (currentRegionData?.encounterDC ?? 0),
        };
    }

    override async _updateObject(event: Event, formData?: RandomEncounterFormData): Promise<void> {
        const modifier = formData?.modifier ?? 0;
        const region = formData?.region ?? 'Rostland';
        await setSetting(this.game, 'currentRegion', region);
        await setSetting(this.game, 'currentEncounterDCModifier', modifier);
        this.render();
    }

    override activateListeners(html: JQuery) {
        super.activateListeners(html);
        const resetDCButton = html[0].querySelector('#reset-dc') as HTMLButtonElement;
        const rollButton = html[0].querySelector('#roll-encounter') as HTMLButtonElement;
        const rollButtonNoCheck = html[0].querySelector('#roll-encounter-no-check') as HTMLButtonElement;
        rollButton?.addEventListener('click', async () => {
            const region = getStringSetting(this.game, "currentRegion") || 'Rostland';
            const modifier = getNumberSetting(this.game, "currentEncounterDCModifier");
            await rollRandomEncounter(this.game, region, modifier);
        });
        rollButtonNoCheck?.addEventListener('click', async () => {
            const region = getStringSetting(this.game, "currentRegion") || 'Rostland';
            const modifier = getNumberSetting(this.game, "currentEncounterDCModifier");
            await rollRandomEncounter(this.game, region, modifier, true);
        });
        resetDCButton?.addEventListener('click', async () => {
            await setSetting(this.game, 'currentEncounterDCModifier', 0);
            this.render();
        })
    }
}

export async function randomEncounterDialog(game: Game): Promise<void> {
    new RandomEncounterApp(null, {game}).render(true);
}
