import {Structure} from '../data/structures';
import {Activity} from '../data/activities';
import {listenClick, unslugify} from '../../utils';

interface StructureBrowserOptions {
    game: Game;
    level: number;
    structures: Structure[];
}

interface ActivityFilter {
    name: string;
    enabled: boolean;
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
    structures: string[];
    activities: Record<Activity, ActivityFilter>;
    level: number;
    lots: number;
}

type StructureFilters = Omit<StructureBrowserData, 'structures'>;

class StructureBrowserApp extends Application<ApplicationOptions & StructureBrowserOptions> {
    private level: number;
    private structures: Structure[];

    static override get defaultOptions(): ApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'structure-browser-app';
        options.title = 'Structure Browser';
        options.template = 'modules/pf2e-kingmaker-tools/templates/kingdom/structure-browser.hbs';
        options.classes = ['kingmaker-tools-app', 'structure-browser-app'];
        options.width = 800;
        options.height = 600;
        options.height = 'auto';
        options.scrollY = ['#km-structure-browser-content', '#km-structure-browser-sidebar'];
        return options;
    }

    private readonly game: Game;
    private filters: StructureFilters;

    constructor(options: Partial<ApplicationOptions> & StructureBrowserOptions) {
        super(options);
        this.game = options.game;
        this.level = options.level;
        this.structures = options.structures;
        this.filters = this.resetFilters();
    }

    private resetFilters(): StructureFilters {
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
            lots: 4,
            activities: [],
            level: this.level,
        };
    }

    override getData(): StructureBrowserData {
        const structures = this.filterStructures(this.structures, this.filters);
        const activities = getAllStructureActivities(structures);
        return {
            ...this.filters,
            structures: this.structuresToLink(structures),
            activities: Array.from(activities).map((activity: Activity) => {
                return {
                    name: unslugify(activity),
                    id: activity,
                    enabled: this.filters.activities.includes(activity),
                };
            }),
        };
    }

    override activateListeners(html: JQuery): void {
        super.activateListeners(html);
        const $html = html[0];
        listenClick($html, '#km-structure-browser-clear', async (): Promise<void> => {
            this.filters = this.resetFilters();
            this.render();
        });
    }

    private filterStructures(structures: Structure[], filters: StructureFilters): Structure[] {
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
        enabledFilters.push((x) => hasActivities(x, Object.keys(filters.activities) as Activity[]));
        enabledFilters.push((x) => (x.level ?? 0) <= filters.level);
        enabledFilters.push((x) => (x.lots ?? 0) <= filters.lots);
        console.log(enabledFilters, filters, structures);
        return structures
            .filter(structure => enabledFilters.every(filter => filter(structure)))
            .sort((a, b) => a.name.localeCompare(b.name));
    }

    private structuresToLink(structures: Structure[]): string[] {
        return structures.map(s => s.name);
    }
}

function getStructureActivities(structure: Structure): Set<Activity> {
    const activityBonuses = structure.activityBonusRules?.map(r => r.activity) ?? [];
    const skillBonuses = (structure.skillBonusRules
        ?.filter(r => r.activity !== undefined)
        ?.map(r => r.activity) ?? []) as Activity[];
    return new Set([...activityBonuses, ...skillBonuses]);
}

function hasActivities(structure: Structure, activities: Activity[]): boolean {
    const allActivityBonuses = getStructureActivities(structure);
    return activities.some(a => allActivityBonuses.has(a));
}


function getAllStructureActivities(structures: Structure[]): Activity[] {
    return Array.from(new Set(structures.flatMap(structure => Array.from(getStructureActivities(structure)))));
}

export async function showStructureBrowser(game: Game, level: number, structures: Structure[]): Promise<void> {
    new StructureBrowserApp({game, level, structures}).render(true);
}
