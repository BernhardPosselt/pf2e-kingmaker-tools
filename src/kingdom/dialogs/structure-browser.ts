import {Structure} from '../data/structures';
import {Activity} from '../data/activities';
import {createUUIDLink, listenClick, unslugify} from '../../utils';
import {rankToLabel} from '../modifiers';
import {Kingdom} from '../data/kingdom';
import {parseStructureData} from '../scene';

interface StructureBrowserOptions {
    game: Game;
    kingdom: Kingdom;
    structureUuids: string[];
}

interface ActivityFilter {
    name: string;
    enabled: boolean;
}

interface StructureData {
    lots: number;
    name: string;
    skills: string[];
    dc: number;
    rp: number;
    lumber: number;
    luxuries: number;
    ore: number;
    stone: number;
}

interface StructureBrowserData {
    housing: boolean;
    affectsDowntime: boolean;
    affectsEvents: boolean;
    items: boolean;
    storage: boolean;
    consumption: boolean;
    reducesRuin: boolean;
    reducesUnrest: boolean;
    infrastructure: boolean;
    ignoreProficiencyRequirements: boolean;
    structures: StructureData[];
    activities: Partial<Record<Activity, ActivityFilter>>;
    level: number;
    lots: number;
}

type StructureFilters = Omit<StructureBrowserData, 'structures'>;
type ActorStructure = Structure & { actor: Actor };

function checkProficiency(structure: Structure, kingdom: Kingdom): boolean {
    return structure.construction?.skills === undefined ||
        structure.construction.skills.length === 0 ||
        structure.construction.skills.some(requirement => {
            return kingdom.skillRanks[requirement.skill] >= (requirement.proficiencyRank ?? 0);
        });
}

async function getStructuresByUuid(structureUuids: string[]): Promise<ActorStructure[]> {
    return (await Promise.all(structureUuids.map(async (uuid) => {
        const actor = await fromUuid(uuid) as Actor | null;
        if (actor) {
            console.log(actor);
            const width = actor.token?.width ?? actor.prototypeToken?.width ?? 0;
            const height = actor.token?.height ?? actor.prototypeToken?.height ?? 0;
            const data = parseStructureData(actor!.name, actor!.getFlag('pf2e-kingmaker-tools', 'structureData'), width, height);
            if (data) {
                return {
                    ...data,
                    actor,
                };
            }
        }
        return null;
    }))).filter(actor => actor !== null)! as ActorStructure[];
}

class StructureBrowserApp extends FormApplication<
    FormApplicationOptions & StructureBrowserOptions,
    object,
    null
> {
    private level: number;
    private structureUuids: string[];
    private kingdom: Kingdom;

    static override get defaultOptions(): FormApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'structure-browser-app';
        options.title = 'Structure Browser';
        options.template = 'modules/pf2e-kingmaker-tools/templates/kingdom/structure-browser.hbs';
        options.classes = ['kingmaker-tools-app', 'structure-browser-app'];
        options.width = 800;
        options.height = 600;
        options.height = 'auto';
        options.submitOnChange = true;
        options.closeOnSubmit = false;
        options.scrollY = ['#km-structure-browser-content', '#km-structure-browser-sidebar'];
        return options;
    }

    private readonly game: Game;
    private filters?: StructureFilters;

    constructor(options: Partial<ApplicationOptions> & StructureBrowserOptions) {
        super(null, options);
        this.game = options.game;
        this.level = options.kingdom.level;
        this.structureUuids = options.structureUuids;
        this.kingdom = options.kingdom;
    }

    private async resetFilters(): Promise<StructureFilters> {
        const structures = await getStructuresByUuid(this.structureUuids);
        const activities = getAllStructureActivities(structures);
        return {
            reducesUnrest: false,
            housing: false,
            affectsDowntime: false,
            affectsEvents: false,
            items: false,
            storage: false,
            consumption: false,
            reducesRuin: false,
            infrastructure: false,
            ignoreProficiencyRequirements: false,
            lots: 4,
            activities: Object.fromEntries(activities.map(activity => {
                return [activity, {name: unslugify(activity), enabled: false}];
            })),
            level: this.level,
        };
    }

    override async getData(): Promise<StructureBrowserData> {
        if (this.filters === undefined) {
            this.filters = await this.resetFilters();
        }
        const structuresByUuid = await getStructuresByUuid(this.structureUuids);
        const structures = this.filterStructures(structuresByUuid, this.filters);
        return {
            ...this.filters,
            structures: await this.toViewStructures(structures),
            activities: this.filters.activities,
        };
    }

    override activateListeners(html: JQuery): void {
        super.activateListeners(html);
        const $html = html[0];
        listenClick($html, '#km-structure-browser-clear', async (): Promise<void> => {
            this.filters = undefined;
            this.render();
        });
    }

    private filterStructures(structures: ActorStructure[], filters: StructureFilters): ActorStructure[] {
        const enabledFilters: ((structure: Structure) => boolean)[] = [];
        if (filters.storage) enabledFilters.push((x) => x.storage !== undefined);
        if (filters.affectsEvents) enabledFilters.push((x) => x.affectsEvents === true);
        if (filters.affectsDowntime) enabledFilters.push((x) => x.affectsDowntime === true);
        if (filters.housing) enabledFilters.push((x) => x.traits?.includes('residential') === true);
        if (filters.infrastructure) enabledFilters.push((x) => x.traits?.includes('infrastructure') === true);
        if (filters.reducesUnrest) enabledFilters.push((x) => x.reducesUnrest === true);
        if (filters.reducesRuin) enabledFilters.push((x) => x.reducesRuin === true);
        if (filters.consumption) enabledFilters.push((x) => x.consumptionReduction !== undefined && x.consumptionReduction > 0);
        if (filters.items) enabledFilters.push((x) => x.availableItemsRules !== undefined && x.availableItemsRules.length > 0);
        if (!filters.ignoreProficiencyRequirements) enabledFilters.push(x => checkProficiency(x, this.kingdom));
        enabledFilters.push((x) => hasActivities(x, filters.activities));
        enabledFilters.push((x) => (x.level ?? 0) <= filters.level);
        enabledFilters.push((x) => (x.lots ?? 0) <= filters.lots);
        return structures
            .filter(structure => enabledFilters.every(filter => filter(structure)))
            .sort((a, b) => a.name.localeCompare(b.name));
    }

    private async toViewStructures(structures: ActorStructure[]): Promise<StructureData[]> {
        return await Promise.all(structures.map(async (structure) => {
            const name = await TextEditor.enrichHTML(createUUIDLink(structure.actor.uuid, structure.name));
            return {
                name: name,
                dc: structure.construction?.dc ?? 0,
                skills: structure.construction?.skills.map(s => {
                    const rank = s.proficiencyRank ? ' (' + rankToLabel(s.proficiencyRank) + ')' : '';
                    const label = unslugify(s.skill);
                    return label + rank;
                }) ?? [],
                lacksProficiency: !checkProficiency(structure, this.kingdom),
                lumber: structure.construction?.lumber ?? 0,
                ore: structure.construction?.ore ?? 0,
                stone: structure.construction?.stone ?? 0,
                luxuries: structure.construction?.luxuries ?? 0,
                rp: structure.construction?.rp ?? 0,
                lots: structure.lots ?? 0,
            };
        }));
    }

    /* eslint-disable @typescript-eslint/no-explicit-any */
    protected async _updateObject(event: Event, formData: any): Promise<void> {
        console.log(formData);
        this.filters = {
            housing: formData.housing,
            affectsDowntime: formData.affectsDowntime,
            affectsEvents: formData.affectsEvents,
            items: formData.items,
            storage: formData.storage,
            consumption: formData.consumption,
            reducesRuin: formData.reducesRuin,
            reducesUnrest: formData.reducesUnrest,
            infrastructure: formData.infrastructure,
            ignoreProficiencyRequirements: formData.ignoreProficiencyRequirements,
            level: formData.level,
            lots: formData.lots,
            activities: Object.fromEntries(
                Object.keys(formData)
                    .filter(d => d.startsWith('activity-'))
                    .map(d => d.replace('activity-', ''))
                    .map(activity => {
                        return [activity as Activity, {
                            name: unslugify(activity),
                            enabled: formData['activity-' + activity],
                        }];
                    }),
            ),
        };


        this.render();
    }
}

function getStructureActivities(structure: Structure): Set<Activity> {
    const activityBonuses = structure.activityBonusRules?.map(r => r.activity) ?? [];
    const skillBonuses = (structure.skillBonusRules
        ?.filter(r => r.activity !== undefined)
        ?.map(r => r.activity) ?? []) as Activity[];
    return new Set([...activityBonuses, ...skillBonuses]);
}

function hasActivities(structure: Structure, activities: Partial<Record<Activity, ActivityFilter>>): boolean {
    const allActivityBonuses = getStructureActivities(structure);
    const enabledActivities = Array.from(Object.entries(activities))
        .filter(([, filter]) => filter.enabled)
        .map(([activity]) => activity) as Activity[];
    return enabledActivities.length === 0 || enabledActivities.every(a => allActivityBonuses.has(a));
}


function getAllStructureActivities(structures: Structure[]): Activity[] {
    return Array.from(new Set(structures.flatMap(structure => Array.from(getStructureActivities(structure)))))
        .sort((a, b) => a.localeCompare(b));
}

export async function showStructureBrowser(game: Game, structureUuids: string[], kingdom: Kingdom): Promise<void> {
    new StructureBrowserApp({game, structureUuids, kingdom}).render(true);
}
