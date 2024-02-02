import {Structure} from '../data/structures';
import {capitalize, createUUIDLink, escapeHtml, isBlank, isNonNullable, listenClick, unslugify} from '../../utils';
import {rankToLabel} from '../modifiers';
import {Kingdom} from '../data/kingdom';
import {CheckDialog} from './check-dialog';
import {getBooleanSetting} from '../../settings';
import {getKingdom, saveKingdom} from '../storage';
import {DegreeOfSuccess} from '../../degree-of-success';
import {ActorStructure, getStructuresFromActors} from '../scene';

interface StructureBrowserOptions {
    game: Game;
    kingdom: Kingdom;
    structureActors: Actor[];
    sheetActor: Actor;
    onRoll: (consumeModifiers: Set<string>) => Promise<void>,
}

interface ActivityFilter {
    name: string;
    enabled: boolean;
}

interface StructureData {
    lots?: number;
    name: string;
    skills: string[];
    dc?: number;
    rp?: number;
    lumber?: number;
    luxuries?: number;
    ore?: number;
    stone?: number;
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
    upgradeFrom: boolean;
    upgradeTo: boolean;
    ignoreProficiencyRequirements: boolean;
    structures: StructureData[];
    activities: Partial<Record<string, ActivityFilter>>;
    level: number;
    lots: number;
    noStructures: boolean;
    ignoreStructureCost: boolean;
    search: string;
}

type StructureFilters = Omit<StructureBrowserData, 'structures' | 'noStructures'>;

function checkProficiency(structure: Structure, kingdom: Kingdom): boolean {
    return structure.construction?.skills === undefined ||
        structure.construction.skills.length === 0 ||
        structure.construction.skills.some(requirement => {
            return kingdom.skillRanks[requirement.skill] >= (requirement.proficiencyRank ?? 0);
        });
}

function checkRpCost(structure: Structure, kingdom: Kingdom): boolean {
    return (structure.construction?.rp ?? 0) <= kingdom.resourcePoints.now;
}

function checkLumberCost(structure: Structure, kingdom: Kingdom): boolean {
    return (structure.construction?.lumber ?? 0) <= kingdom.commodities.now.lumber;
}

function checkOreCost(structure: Structure, kingdom: Kingdom): boolean {
    return (structure.construction?.ore ?? 0) <= kingdom.commodities.now.ore;
}

function checkStoneCost(structure: Structure, kingdom: Kingdom): boolean {
    return (structure.construction?.stone ?? 0) <= kingdom.commodities.now.stone;
}

function checkLuxuriesCost(structure: Structure, kingdom: Kingdom): boolean {
    return (structure.construction?.luxuries ?? 0) <= kingdom.commodities.now.luxuries;
}


function checkBuildingCost(structure: Structure, kingdom: Kingdom): boolean {
    return checkLumberCost(structure, kingdom)
        && checkRpCost(structure, kingdom)
        && checkLuxuriesCost(structure, kingdom)
        && checkOreCost(structure, kingdom)
        && checkStoneCost(structure, kingdom);
}

class StructureBrowserApp extends FormApplication<
    FormApplicationOptions & StructureBrowserOptions,
    object,
    null
> {
    private level: number;
    private structureActors: Actor[];
    private kingdom: Kingdom;

    static override get defaultOptions(): FormApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'structure-browser-app';
        options.title = 'Structure Browser';
        options.template = 'modules/pf2e-kingmaker-tools/templates/kingdom/structure-browser.hbs';
        options.classes = ['kingmaker-tools-app', 'structure-browser-app'];
        options.width = 960;
        options.height = 600;
        options.height = 'auto';
        options.submitOnChange = true;
        options.closeOnSubmit = false;
        options.scrollY = ['#km-structure-browser-content', '#km-structure-browser-sidebar'];
        return options;
    }

    private readonly game: Game;
    private filters?: StructureFilters;
    private sheetActor: Actor;
    private onRoll: (consumeModifiers: Set<string>) => Promise<void>;

    constructor(options: Partial<ApplicationOptions> & StructureBrowserOptions) {
        super(null, options);
        this.game = options.game;
        this.level = options.kingdom.level;
        this.structureActors = options.structureActors;
        this.kingdom = options.kingdom;
        this.sheetActor = options.sheetActor;
        this.onRoll = options.onRoll;
    }

    private async resetFilters(): Promise<StructureFilters> {
        const structures = getStructuresFromActors(this.structureActors);
        const activities = getAllStructureActivities(structures);
        return {
            search: '',
            reducesUnrest: false,
            housing: false,
            affectsDowntime: false,
            affectsEvents: false,
            items: false,
            storage: false,
            consumption: false,
            reducesRuin: false,
            upgradeFrom: false,
            upgradeTo: false,
            infrastructure: false,
            ignoreProficiencyRequirements: false,
            ignoreStructureCost: false,
            lots: 4,
            activities: Object.fromEntries(activities.map(activity => {
                return [activity, {name: unslugify(activity), enabled: false}];
            })),
            level: this.level,
        };
    }

    override async getData(): Promise<StructureBrowserData> {
        this.kingdom = getKingdom(this.sheetActor);
        if (this.filters === undefined) {
            this.filters = await this.resetFilters();
        }
        const structureActors = getStructuresFromActors(this.structureActors);
        const structures = this.filterStructures(structureActors, this.filters);
        const viewStructures = await this.toViewStructures(structures);
        return {
            ...this.filters,
            structures: viewStructures,
            activities: this.filters.activities,
            noStructures: structureActors.length === 0,
        };
    }

    override activateListeners(html: JQuery): void {
        super.activateListeners(html);
        const $html = html[0];
        listenClick($html, '#km-structure-browser-clear', async (): Promise<void> => {
            this.filters = undefined;
            this.render();
        });
        $html.querySelectorAll('.km-build-structure-dialog')
            .forEach(el => el.addEventListener('click', (ev) => this.buildStructure(ev)));
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
        if (filters.upgradeFrom) enabledFilters.push((x) => x.upgradeFrom !== undefined && x.upgradeFrom.length > 0);
        if (filters.upgradeTo) enabledFilters.push((x) => this.hasUpgradeTo(x, structures));
        if (!filters.ignoreProficiencyRequirements) enabledFilters.push(x => checkProficiency(x, this.kingdom));
        if (!filters.ignoreStructureCost) enabledFilters.push(x => checkBuildingCost(x, this.kingdom));
        if (!isBlank(filters.search)) enabledFilters.push(x => x.name.toLowerCase().includes(filters.search.trim().toLowerCase()));
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
            const lacksProficiency = !checkProficiency(structure, this.kingdom);
            return {
                name: name,
                dc: this.getStructureDC(structure),
                skills: structure.construction?.skills.map(s => {
                    const rank = s.proficiencyRank ? ' (' + rankToLabel(s.proficiencyRank) + ')' : '';
                    const label = unslugify(s.skill);
                    return label + rank;
                }) ?? [],
                lacksProficiency,
                disableBuild: lacksProficiency && !getBooleanSetting(this.game, 'kingdomIgnoreSkillRequirements'),
                lumber: structure.construction?.lumber,
                ore: structure.construction?.ore,
                stone: structure.construction?.stone,
                luxuries: structure.construction?.luxuries,
                rp: structure.construction?.rp,
                insufficientStone: !checkStoneCost(structure, this.kingdom),
                insufficientLumber: !checkLumberCost(structure, this.kingdom),
                insufficientRp: !checkRpCost(structure, this.kingdom),
                insufficientLuxuries: !checkLuxuriesCost(structure, this.kingdom),
                insufficientOre: !checkOreCost(structure, this.kingdom),
                lots: structure.lots === 0 ? undefined : structure.lots,
                id: structure.actor.id,
            };
        }));
    }

    private getStructureDC(structure: Structure): number | undefined {
        const adjustment = getBooleanSetting(this.game, 'reduceDCToBuildLumberStructures')
        && (structure.construction?.lumber ?? 0) > 0 ? -2 : 0;
        const dc = structure.construction?.dc;
        return dc === undefined ? undefined : dc + adjustment;
    }

    /* eslint-disable @typescript-eslint/no-explicit-any */
    protected async _updateObject(event: Event, formData: any): Promise<void> {
        console.log(formData);
        this.filters = {
            search: formData.search,
            housing: formData.housing,
            affectsDowntime: formData.affectsDowntime,
            affectsEvents: formData.affectsEvents,
            items: formData.items,
            storage: formData.storage,
            consumption: formData.consumption,
            reducesRuin: formData.reducesRuin,
            reducesUnrest: formData.reducesUnrest,
            infrastructure: formData.infrastructure,
            upgradeFrom: formData.upgradeFrom,
            upgradeTo: formData.upgradeTo,
            ignoreStructureCost: formData.ignoreStructureCost,
            ignoreProficiencyRequirements: formData.ignoreProficiencyRequirements,
            level: formData.level,
            lots: formData.lots,
            activities: Object.fromEntries(
                Object.keys(formData)
                    .filter(d => d.startsWith('activity-'))
                    .map(d => d.replace('activity-', ''))
                    .map(activity => {
                        return [activity, {
                            name: unslugify(activity),
                            enabled: formData['activity-' + activity],
                        }];
                    }),
            ),
        };
        this.render();
    }

    private async buildStructure(ev: Event): Promise<void> {
        const button = ev.currentTarget as HTMLElement;
        const id = button.dataset.id!;
        const structureActors = getStructuresFromActors(this.structureActors);
        const structure = structureActors.find(a => a.actor.id === id);
        if (structure) {
            const applicableSkills = structure.construction?.skills?.map(s => {
                return [s.skill, s.proficiencyRank ?? 0];
            });
            new CheckDialog(null, {
                activity: 'build-structure',
                kingdom: this.kingdom,
                dc: this.getStructureDC(structure),
                overrideSkills: applicableSkills === undefined ? undefined : Object.fromEntries(applicableSkills),
                game: this.game,
                type: 'activity',
                onRoll: this.onRoll,
                actor: this.sheetActor,
                afterRoll: async (degree): Promise<void> => {
                    await this.payStructure(structure, degree);
                    await this.close();
                },
            }).render(true);
        }
    }

    private async getPaymentChatData(structure: ActorStructure, degree: DegreeOfSuccess): Promise<{
        upgradeCosts: { name: string, costs: Costs }[],
        costs: Costs,
        structureLink?: string,
    }> {
        const structureActors = getStructuresFromActors(this.structureActors);
        const upgradeFromStructures = (structure.upgradeFrom
                ?.map(name => structureActors.find(a => a.actor.name === name))
                ?.filter(structure => isNonNullable(structure))
            ?? []) as ActorStructure[];
        const costMode = degree === DegreeOfSuccess.CRITICAL_SUCCESS ? 'half' : 'full';
        const upgradeCosts: { name: string, costs: Costs }[] = upgradeFromStructures.map(upgradeFrom => {
            const costs = {
                rp: Math.max(0, (structure.construction?.rp ?? 0) - (upgradeFrom.construction?.rp ?? 0)),
                lumber: Math.max(0, (structure.construction?.lumber ?? 0) - (upgradeFrom.construction?.lumber ?? 0)),
                stone: Math.max(0, (structure.construction?.stone ?? 0) - (upgradeFrom.construction?.stone ?? 0)),
                ore: Math.max(0, (structure.construction?.ore ?? 0) - (upgradeFrom.construction?.ore ?? 0)),
                luxuries: Math.max(0, (structure.construction?.luxuries ?? 0) - (upgradeFrom.construction?.luxuries ?? 0)),
            };
            return {
                name: upgradeFrom.name,
                costs: calculateCosts(costs, costMode),
            };
        });
        const costs = calculateCosts(structure.construction ?? {}, costMode);
        const structureToLink = degree === DegreeOfSuccess.CRITICAL_FAILURE
            ? structureActors.find(a => a.actor.name === 'Rubble')?.actor
            : structure?.actor;
        const structureLink = structureToLink
            ? await TextEditor.enrichHTML(createUUIDLink(structureToLink.uuid, structureToLink.name!))
            : undefined;
        return {
            structureLink,
            costs,
            upgradeCosts,
        };
    }

    private async payStructure(structure: ActorStructure, degree: DegreeOfSuccess): Promise<void> {
        const {
            structureLink,
            costs,
            upgradeCosts,
        } = await this.getPaymentChatData(structure, degree);
        const upgradeButtons = upgradeCosts.map(costs => {
            const title = `<p><b>Upgrade from ${escapeHtml(costs.name)}:</b></p>`;
            return title + createPayButton(costs.costs);
        }).join('');
        const header = `<h3>Constructing ${escapeHtml(structure.name)}</h3>`;
        const title = '<p><b>Build Directly:</b></p>';
        const payButton = title + createPayButton(costs);
        const linkNote = degree === DegreeOfSuccess.FAILURE
            ? ' and apply the @UUID[Compendium.pf2e.conditionitems.Item.xYTAsEpcJE1Ccni3]{Slowed} condition to signal that the structure is under construction' : '';
        const link = structureLink ? `<p><b>Drag onto scene to build${linkNote}:</b></p><p>${structureLink}</p>` : '';
        await ChatMessage.create({
            content: header + upgradeButtons + payButton + link,
        });
    }

    private hasUpgradeTo(structure: Structure, structures: ActorStructure[]): boolean {
        return structures.some(s => s.upgradeFrom?.includes(structure.name) ?? false);
    }
}

function getStructureActivities(structure: Structure): Set<string> {
    const activityBonuses = structure.activityBonusRules?.map(r => r.activity) ?? [];
    const skillBonuses = (structure.skillBonusRules
        ?.filter(r => r.activity !== undefined)
        ?.map(r => r.activity) ?? []) as string[];
    return new Set([...activityBonuses, ...skillBonuses]);
}

function hasActivities(structure: Structure, activities: Partial<Record<string, ActivityFilter>>): boolean {
    const allActivityBonuses = getStructureActivities(structure);
    const enabledActivities = Array.from(Object.entries(activities))
        .filter(([, filter]) => filter?.enabled)
        .map(([activity]) => activity) as string[];
    return enabledActivities.length === 0 || enabledActivities.every(a => allActivityBonuses.has(a));
}


function getAllStructureActivities(structures: Structure[]): string[] {
    return Array.from(new Set(structures.flatMap(structure => Array.from(getStructureActivities(structure)))))
        .sort((a, b) => a.localeCompare(b));
}


export interface Costs {
    rp: number;
    ore: number;
    stone: number;
    lumber: number;
    luxuries: number;
}

function createPayButton(costs: Costs): string {
    return `<button type="button" 
                data-rp="${costs.rp}" 
                data-lumber="${costs.lumber}"
                data-luxuries="${costs.luxuries}"
                data-stone="${costs.stone}"
                data-ore="${costs.ore}"
                class="km-pay-structure"><b>Pay</b>: ${formatCosts(costs)}</button>`;
}

export function parsePayButton(el: HTMLElement): Costs {
    return {
        rp: parseInt(el.dataset.rp ?? '0', 10),
        lumber: parseInt(el.dataset.lumber ?? '0', 10),
        luxuries: parseInt(el.dataset.luxuries ?? '0', 10),
        stone: parseInt(el.dataset.stone ?? '0', 10),
        ore: parseInt(el.dataset.ore ?? '0', 10),
    };
}

export async function payStructure(sheetActor: Actor, costs: Costs): Promise<void> {
    const kingdom = getKingdom(sheetActor);
    await saveKingdom(sheetActor, {
        commodities: {
            ...kingdom.commodities,
            now: {
                ...kingdom.commodities.now,
                ore: Math.max(0, kingdom.commodities.now.ore - costs.ore),
                lumber: Math.max(0, kingdom.commodities.now.lumber - costs.lumber),
                luxuries: Math.max(0, kingdom.commodities.now.luxuries - costs.luxuries),
                stone: Math.max(0, kingdom.commodities.now.stone - costs.stone),
            },
        },
        resourcePoints: {
            ...kingdom.resourcePoints,
            now: Math.max(0, kingdom.resourcePoints.now - costs.rp),
        },
    });
    await ChatMessage.create({
        content: `<b>Paid:</b> ${formatCosts(costs)}`,
    });
}

function calculateCosts(costs: Partial<Costs>, mode: 'half' | 'full'): Costs {
    const zeroedCosts: Costs = {
        rp: costs.rp ?? 0,
        ore: costs.ore ?? 0,
        lumber: costs.lumber ?? 0,
        luxuries: costs.luxuries ?? 0,
        stone: costs.stone ?? 0,
    };
    if (mode === 'full') {
        return zeroedCosts;
    } else {
        return {
            rp: zeroedCosts.rp,
            ore: Math.ceil(zeroedCosts.ore / 2),
            lumber: Math.ceil(zeroedCosts.lumber / 2),
            luxuries: Math.ceil(zeroedCosts.luxuries / 2),
            stone: Math.ceil(zeroedCosts.stone / 2),
        };
    }
}

function formatCosts(costs: Costs): string {
    return (Object.entries(costs) as [string, number][])
        .filter(([, value]) => value > 0)
        .map(([key, value]) => {
            return `${capitalize(key)}: ${value}`;
        })
        .join(', ');
}

export async function showStructureBrowser(
    game: Game,
    structureActors: Actor[],
    kingdom: Kingdom,
    sheetActor: Actor,
    onRoll: (consumeModifiers: Set<string>) => Promise<void>,
): Promise<void> {
    new StructureBrowserApp({game, structureActors, kingdom, sheetActor, onRoll}).render(true);
}
