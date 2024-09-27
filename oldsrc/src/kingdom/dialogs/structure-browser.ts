import {Structure} from '../data/structures';
import {
    capitalize,
    createUUIDLink,
    escapeHtml,
    groupBy,
    isBlank,
    isGm,
    isNonNullable,
    LabelAndValue,
    listenClick,
    unslugify,
} from '../../utils';
import {rankToLabel} from '../modifiers';
import {Kingdom} from '../data/kingdom';
import {CheckDialog} from './check-dialog';
import {getBooleanSetting} from '../../settings';
import {getKingdom, saveKingdom} from '../storage';
import {DegreeOfSuccess} from '../../degree-of-success';
import {
    ActorStructure,
    getScene,
    getSceneActorStructures,
    getStructuresFromActors,
    isStructureActorActive,
} from '../scene';

interface StructureBrowserOptions {
    game: Game;
    kingdom: Kingdom;
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

interface StructureFilters {
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
    activities: Partial<Record<string, ActivityFilter>>;
    level: number;
    lots: number;
    minLots: number;
    ignoreStructureCost: boolean;
    search: string;
}

interface StructureBrowserData extends StructureFilters {
    structures: StructureData[];
    noStructures: boolean;
    isGM: boolean;
    buildableTab: boolean;
    upgradableTab: boolean;
    freeTab: boolean;
    activeSettlement?: string;
    settlements: LabelAndValue[];
    constructableBuildings: {
        free: number;
        upgradable: number;
        buildable: number;
    };
}

function checkProficiency(structure: Structure, kingdom: Kingdom): boolean {
    return structure.construction?.skills === undefined ||
        structure.construction.skills.length === 0 ||
        structure.construction.skills.some(requirement => {
            return kingdom.skillRanks[requirement.skill] >= (requirement.proficiencyRank ?? 0);
        });
}

function checkRpCost(structure: Structure, kingdom: Kingdom, discounts?: BuildDiscounts): boolean {
    return ((structure.construction?.rp ?? 0) - (discounts?.rp ?? 0)) <= kingdom.resourcePoints.now;
}

function checkLumberCost(structure: Structure, kingdom: Kingdom, discounts?: BuildDiscounts): boolean {
    return ((structure.construction?.lumber ?? 0) - (discounts?.lumber ?? 0)) <= kingdom.commodities.now.lumber;
}

function checkOreCost(structure: Structure, kingdom: Kingdom, discounts?: BuildDiscounts): boolean {
    return ((structure.construction?.ore ?? 0) - (discounts?.ore ?? 0)) <= kingdom.commodities.now.ore;
}

function checkStoneCost(structure: Structure, kingdom: Kingdom, discounts?: BuildDiscounts): boolean {
    return ((structure.construction?.stone ?? 0) - (discounts?.stone ?? 0)) <= kingdom.commodities.now.stone;
}

function checkLuxuriesCost(structure: Structure, kingdom: Kingdom, discounts?: BuildDiscounts): boolean {
    return ((structure.construction?.luxuries ?? 0) - (discounts?.luxuries ?? 0)) <= kingdom.commodities.now.luxuries;
}


interface BuildDiscounts {
    rp: number;
    lumber: number;
    luxuries: number;
    ore: number;
    stone: number;
}

function checkBuildingCost(
    structure: Structure,
    sceneStructures: Map<string, Structure[]>,
    kingdom: Kingdom,
    mode: StructureBrowserTab,
): boolean {
    if (mode === 'free') {
        return true;
    } else if (mode === 'upgradable') {
        return structure.upgradeFrom
            ?.flatMap(name => sceneStructures.get(name) ?? [])
            ?.some(upgradableStructure => {
                const discount: BuildDiscounts = {
                    rp: upgradableStructure?.construction?.rp ?? 0,
                    lumber: upgradableStructure?.construction?.lumber ?? 0,
                    luxuries: upgradableStructure?.construction?.luxuries ?? 0,
                    ore: upgradableStructure?.construction?.ore ?? 0,
                    stone: upgradableStructure?.construction?.stone ?? 0,
                };
                return checkLumberCost(structure, kingdom, discount)
                    && checkRpCost(structure, kingdom, discount)
                    && checkLuxuriesCost(structure, kingdom, discount)
                    && checkOreCost(structure, kingdom, discount)
                    && checkStoneCost(structure, kingdom, discount);
            }) ?? false;
    } else {
        return checkLumberCost(structure, kingdom)
            && checkRpCost(structure, kingdom)
            && checkLuxuriesCost(structure, kingdom)
            && checkOreCost(structure, kingdom)
            && checkStoneCost(structure, kingdom);
    }
}

type StructureBrowserTab = 'free' | 'upgradable' | 'buildable';

class StructureBrowserApp extends FormApplication<
    FormApplicationOptions & StructureBrowserOptions,
    object,
    null
> {
    private level: number;
    private structureActors: Actor[];
    private kingdom: Kingdom;
    private currentTab: StructureBrowserTab = 'buildable';

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
        this.structureActors = this.getActors();
        this.kingdom = options.kingdom;
        this.sheetActor = options.sheetActor;
        this.onRoll = options.onRoll;
    }

    private getActors(): Actor[] {
        return this.game.actors
                ?.filter(a => a.type === 'npc'
                    && isNonNullable(a.getFlag('pf2e-kingmaker-tools', 'structureData')))
            ?? [];
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
            minLots: 0,
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
        const {
            buildableStructures,
            freeStructures,
            upgradableStructures,
            sceneStructures,
        } = this.getStructureActors();
        const buildableViewStructures = await this.toViewStructures(this.filterStructures(buildableStructures, sceneStructures, this.filters, 'buildable'));
        const upgradableViewStructures = await this.toViewStructures(this.filterStructures(upgradableStructures, sceneStructures, this.filters, 'upgradable'));
        const freeViewStructures = await this.toViewStructures(this.filterStructures(freeStructures, sceneStructures, this.filters, 'free'));
        let viewStructures: StructureData[];
        if (this.currentTab === 'buildable') {
            viewStructures = buildableViewStructures;
        } else if (this.currentTab === 'upgradable') {
            viewStructures = upgradableViewStructures;
        } else {
            viewStructures = freeViewStructures;
        }
        return {
            ...this.filters,
            ...this.getActiveTabs(),
            constructableBuildings: {
                buildable: buildableViewStructures.length,
                free: freeViewStructures.length,
                upgradable: upgradableViewStructures.length,
            },
            isGM: isGm(this.game),
            structures: viewStructures,
            activities: this.filters.activities,
            noStructures: buildableStructures.length === 0,
            activeSettlement: getScene(this.game, this.kingdom.activeSettlement)?.id ?? undefined,
            settlements: this.kingdom.settlements
                .map(s => getScene(this.game, s.sceneId))
                .filter(isNonNullable)
                .map(s => ({label: s.name ?? '', value: s.id ?? ''})),
        };
    }

    private getStructureActors(): {
        buildableStructures: ActorStructure[];
        upgradableStructures: ActorStructure[];
        freeStructures: ActorStructure[];
        sceneStructures: ActorStructure[];
    } {
        const buildableStructures = getStructuresFromActors(this.structureActors)
            .filter(a => a.name !== 'Rubble');
        const activeSettlement = getScene(this.game, this.kingdom.activeSettlement);

        if (activeSettlement) {
            const sceneStructures = getSceneActorStructures(activeSettlement);
            const sceneStructuresByName = groupBy(sceneStructures, s => s.name);
            const freeStructures = buildableStructures
                .filter(s => sceneStructuresByName.get(s.name)
                    ?.some(a => !isStructureActorActive(a.actor)));
            const upgradableStructures = buildableStructures
                .filter(as => as.upgradeFrom
                    ?.some(name => sceneStructuresByName.get(name)
                        ?.some(a => isStructureActorActive(a.actor))));
            return {
                buildableStructures,
                upgradableStructures,
                freeStructures,
                sceneStructures,
            };
        } else {
            return {
                buildableStructures,
                upgradableStructures: [],
                freeStructures: [],
                sceneStructures: [],
            };
        }
    }

    override activateListeners(html: JQuery): void {
        super.activateListeners(html);
        const $html = html[0];
        listenClick($html, '#km-structure-browser-clear', async (): Promise<void> => {
            this.filters = undefined;
            this.render();
        });
        listenClick($html, '.km-import-structures', async (): Promise<void> => {
            await this.game.packs.get('pf2e-kingmaker-tools.kingmaker-tools-structures')
                ?.importAll({folderName: 'Structures'});
            this.structureActors = this.getActors();
            this.render();
        });
        $html.querySelectorAll('.km-structure-link')
            .forEach(el => el.addEventListener('click', async (ev): Promise<void> => {
                ev.preventDefault();
                ev.stopPropagation();
                const target = ev.currentTarget as HTMLElement;
                const uuid = target.dataset.uuid!;
                const actor = await fromUuid(uuid) as Actor | null;
                actor?.sheet?.render(true);
            }));
        $html.querySelectorAll('.km-nav a')?.forEach(el => {
            el.addEventListener('click', (event) => {
                const tab = event.currentTarget as HTMLAnchorElement;
                this.currentTab = tab.dataset.tab as StructureBrowserTab;
                this.render();
            });
        });
        $html.querySelectorAll('.km-build-structure-dialog')
            .forEach(el => el.addEventListener('click', (ev) => this.buildStructure(ev)));
    }

    private filterStructures(
        structures: ActorStructure[],
        sceneStructures: ActorStructure[],
        filters: StructureFilters,
        mode: StructureBrowserTab,
    ): ActorStructure[] {
        const groupedSceneStructures = groupBy(sceneStructures, (s) => s.name);
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
        if (!filters.ignoreStructureCost) enabledFilters.push(x => checkBuildingCost(x, groupedSceneStructures, this.kingdom, mode));
        if (!isBlank(filters.search)) enabledFilters.push(x => x.name.toLowerCase().includes(filters.search.trim().toLowerCase()));
        enabledFilters.push((x) => hasActivities(x, filters.activities));
        enabledFilters.push((x) => (x.level ?? 0) <= filters.level);
        enabledFilters.push((x) => (x.lots) <= filters.lots);
        enabledFilters.push((x) => (x.lots) >= filters.minLots);
        return structures
            .filter(structure => enabledFilters.every(filter => filter(structure)))
            .sort((a, b) => a.name.localeCompare(b.name));
    }

    private async toViewStructures(structures: ActorStructure[]): Promise<StructureData[]> {
        return await Promise.all(structures.map(async (structure) => {
            const link = await TextEditor.enrichHTML(createUUIDLink(structure.actor.uuid, structure.name));
            const lacksProficiency = !checkProficiency(structure, this.kingdom);
            const skills = structure.construction?.skills.map(s => {
                const rank = s.proficiencyRank ? ` (${rankToLabel(s.proficiencyRank)})` : '';
                const label = unslugify(s.skill);
                return label + rank;
            }) ?? [];
            return {
                link: link,
                name: structure.name,
                uuid: structure.actor.uuid,
                dc: this.getStructureDC(structure),
                skills,
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
                img: structure.actor.img,
                residential: structure.traits?.includes('residential') ?? false,
                infrastructure: structure.traits?.includes('infrastructure') ?? false,
                proficiencyRequirements: skills.join(' or '),
            };
        }));
    }

    private getStructureDC(structure: Structure): number | undefined {
        const adjustment = getBooleanSetting(this.game, 'reduceDCToBuildLumberStructures')
        && (structure.construction?.lumber ?? 0) > 0 ? -2 : 0;
        const dc = structure.construction?.dc;
        return dc === undefined ? undefined : dc + adjustment;
    }

    private getActiveTabs(): Record<'buildableTab' | 'freeTab' | 'upgradableTab', boolean> {
        return {
            buildableTab: this.currentTab === 'buildable',
            upgradableTab: this.currentTab === 'upgradable',
            freeTab: this.currentTab === 'free',
        };
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
            minLots: formData.minLots,
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
        await saveKingdom(this.sheetActor, {
            activeSettlement: formData.activeSettlement,
        });
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
                afterRoll: async (): Promise<void> => {
                    await this.close();
                },
                additionalChatMessages: [{
                    [DegreeOfSuccess.CRITICAL_SUCCESS]: await this.payStructure(structure, DegreeOfSuccess.CRITICAL_SUCCESS),
                    [DegreeOfSuccess.SUCCESS]: await this.payStructure(structure, DegreeOfSuccess.SUCCESS),
                    [DegreeOfSuccess.FAILURE]: await this.payStructure(structure, DegreeOfSuccess.FAILURE),
                    [DegreeOfSuccess.CRITICAL_FAILURE]: await this.payStructure(structure, DegreeOfSuccess.CRITICAL_FAILURE),
                }],
            }).render(true);
        }
    }

    private async getPaymentChatData(structure: ActorStructure, degree: DegreeOfSuccess): Promise<{
        upgradeCosts: { name: string, costs: Costs }[],
        ruinCosts: Costs,
        costs: Costs,
        structureLink?: string,
    }> {
        const structureActors = getStructuresFromActors(this.structureActors);
        const upgradeFromStructures = (structure.upgradeFrom
                ?.map(name => structureActors.find(a => a.actor.name === name))
                ?.filter(structure => isNonNullable(structure))
            ?? []) as ActorStructure[];
        const costMode = degree === DegreeOfSuccess.CRITICAL_SUCCESS ? 'criticalSuccess' : 'success';
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
        const ruinCostMode = degree === DegreeOfSuccess.CRITICAL_SUCCESS ? 'ruinCriticalSuccess' : 'ruinSuccess';
        const ruinCosts = calculateCosts(structure.construction ?? {}, ruinCostMode);
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
            ruinCosts,
        };
    }

    private async payStructure(structure: ActorStructure, degree: DegreeOfSuccess): Promise<string> {
        const {
            structureLink,
            costs,
            upgradeCosts,
            ruinCosts,
        } = await this.getPaymentChatData(structure, degree);
        const upgradeButtons = upgradeCosts.map(costs => {
            const title = `<p><b>Upgrade from ${escapeHtml(costs.name)}:</b></p>`;
            return title + createPayButton(costs.costs);
        }).join('');
        const header = `<h3>Constructing ${escapeHtml(structure.name)}</h3>`;
        const ruinTitle = '<p><b>Build from Ruin:</b></p>';
        const ruinButton = ruinTitle + createPayButton(ruinCosts);
        const title = '<p><b>Build Directly:</b></p>';
        const payButton = title + createPayButton(costs);
        const linkNote = degree === DegreeOfSuccess.FAILURE
            ? ' and apply the @UUID[Compendium.pf2e.conditionitems.Item.xYTAsEpcJE1Ccni3]{Slowed} condition to signal that the structure is under construction' : '';
        const link = structureLink ? `<p><b>Drag onto scene to build${linkNote}:</b></p><p>${structureLink}</p>` : '';
        return header + upgradeButtons + ruinButton + payButton + link;
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

function calculateCosts(costs: Partial<Costs>, mode: 'ruinSuccess' | 'ruinCriticalSuccess' | 'success' | 'criticalSuccess'): Costs {
    const result: Costs = {
        rp: costs.rp ?? 0,
        ore: costs.ore ?? 0,
        lumber: costs.lumber ?? 0,
        luxuries: costs.luxuries ?? 0,
        stone: costs.stone ?? 0,
    };

    if (mode === 'success') {
        return result;
    } else if (mode === 'criticalSuccess') {
        // critical successes only halve commodity cost
        return {
            rp: result.rp,
            ore: Math.ceil(result.ore / 2),
            lumber: Math.ceil(result.lumber / 2),
            luxuries: Math.ceil(result.luxuries / 2),
            stone: Math.ceil(result.stone / 2),
        };
    } else if (mode === 'ruinSuccess') {
        return {
            rp: Math.ceil(result.rp / 2),
            ore: Math.ceil(result.ore / 2),
            lumber: Math.ceil(result.lumber / 2),
            luxuries: Math.ceil(result.luxuries / 2),
            stone: Math.ceil(result.stone / 2),
        };
    } else {
        // critical successes only halve commodity cost
        return {
            rp: Math.ceil(result.rp / 2),
            ore: Math.ceil(result.ore / 4),
            lumber: Math.ceil(result.lumber / 4),
            luxuries: Math.ceil(result.luxuries / 4),
            stone: Math.ceil(result.stone / 4),
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
    kingdom: Kingdom,
    sheetActor: Actor,
    onRoll: (consumeModifiers: Set<string>) => Promise<void>,
): Promise<void> {
    new StructureBrowserApp({game, kingdom, sheetActor, onRoll}).render(true);
}
