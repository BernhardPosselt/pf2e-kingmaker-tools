import {calculateArmyData, CalculatedArmy} from './utils';
import {
    allAlignmentLabels,
    allAlignments,
    allArmyActions,
    allArmySaves,
    allArmyTypes,
    allGearByName,
    allLevels,
    allRarities,
    allTacticsByName,
    armiesByName,
    ArmyItem,
    ArmyTacticsName,
} from './data';
import {capitalize} from '../utils';

interface ArmyOptions {
    game: Game;
    actor: Actor;
    token: Token;
}

interface LabeledValue {
    label: string;
    value: string;
}

interface ArmyData extends Omit<CalculatedArmy, 'gear'> {
    rarities: LabeledValue[];
    saves: LabeledValue[];
    alignments: LabeledValue[];
    types: LabeledValue[];
    levels: LabeledValue[];
    tabs: Record<string, boolean>;
    actions: Actions[];
    gear: Gear[];
}

interface Actions {
    name: string;
    icon: string;
    traits: string[];
    requirements?: string;
    trigger?: string;
    description?: string;
    type?: LabeledValue;
    frequency?: string;
    effect?: string;
    results?: {
        criticalSuccess?: string;
        success?: string;
        failure?: string;
        criticalFailure?: string;
    }
}

interface Gear {
    name: string;
    level: number;
    traits: string[];
    quantity?: number;
    maximumQuantity?: number;
    description: string;
    price: number;
}

type ArmyTab = 'status' | 'gear' | 'conditions' | 'tactics' | 'actions' | 'effects';


class ArmySheet extends FormApplication<FormApplicationOptions & ArmyOptions, object, null> {
    static override get defaultOptions(): FormApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'army-app';
        options.title = 'Army';
        options.template = 'modules/pf2e-kingmaker-tools/templates/army/sheet.hbs';
        options.submitOnChange = true;
        options.closeOnSubmit = false;
        options.classes = ['kingmaker-tools-app', 'army-app'];
        options.width = 850;
        options.height = 'auto';
        options.scrollY = ['.km-content', '.km-sidebar'];
        return options;
    }

    private token: Token;

    private actor: Actor;
    private readonly game: Game;
    private nav: ArmyTab = 'status';


    constructor(object: null, options: Partial<FormApplicationOptions> & ArmyOptions) {
        super(object, options);
        this.game = options.game;
        this.actor = options.actor;
        this.token = options.token;
        this.actor.apps[this.appId] = this;
    }

    override async getData(): Promise<ArmyData> {
        const name = 'First World Army';
        const data = armiesByName.get(name)!;
        const army = calculateArmyData(data);
        console.log(army);
        // const gear = army.gear;
        const gear: ArmyItem[] = [{name: 'Healing Potion', quantity: 3}, {name: 'Magic Armor +1'}];
        return {
            ...army,
            alignments: allAlignments.map((alignment, index) => {
                return {value: alignment, label: allAlignmentLabels[index]};
            }),
            rarities: allRarities.map(rarity => {
                return {value: rarity, label: capitalize(rarity)};
            }),
            types: allArmyTypes.map(type => {
                return {value: type, label: capitalize(type)};
            }),
            saves: allArmySaves.map(save => {
                return {value: save, label: capitalize(save)};
            }),
            levels: allLevels.map(level => {
                return {value: `${level}`, label: `${level}`};
            }),
            tabs: this.getActiveTabs(),
            actions: this.buildActions(army.tactics),
            gear: this.buildGear(gear),
        };
    }

    private getActiveTabs(): Record<string, boolean> {
        return {
            statusTab: this.nav === 'status',
            actionsTab: this.nav === 'actions',
            gearTab: this.nav === 'gear',
            tacticsTab: this.nav === 'tactics',
            conditionsTab: this.nav === 'conditions',
            effectsTab: this.nav === 'effects',
        };
    }

    /* eslint-disable @typescript-eslint/no-explicit-any */
    override async _updateObject(event: Event, formData: any): Promise<void> {
        console.log(formData);
        return;
    }

    override activateListeners(html: JQuery): void {
        super.activateListeners(html);
        const $html = html[0];
        $html.querySelectorAll('.km-nav a')?.forEach(el => {
            el.addEventListener('click', (event) => {
                const tab = event.currentTarget as HTMLAnchorElement;
                this.nav = tab.dataset.tab as ArmyTab;
                this.render();
            });
        });
    }

    private buildActions(tactics: ArmyTacticsName[]): Actions[] {
        const unlockedActionNames = new Set(tactics.flatMap(tactic => allTacticsByName[tactic].grantsActions ?? []));
        const actions = allArmyActions.filter(action => !action.requiresUnlock || unlockedActionNames.has(action.name));
        return actions.map(action => {
            const traits: string[] = [
                ...(action.type ? [capitalize(action.type)] : []),
                ...(action.restrictedTypes ?? [])].map(value => capitalize(value)
            );
            const results = action.criticalSuccess || action.success || action.failure || action.criticalFailure
                ? {
                    criticalSuccess: action.criticalSuccess,
                    success: action.success,
                    failure: action.failure,
                    criticalFailure: action.criticalFailure,
                }
                : undefined;
            return {
                icon: action.actions,
                name: action.name,
                description: action.description,
                trigger: action.trigger,
                requirements: action.requirements,
                frequency: action.frequency,
                effect: action.effect,
                type: action.type ? {label: capitalize(action.type), value: action.type} : undefined,
                traits,
                results,
            };
        });
    }

    private buildGear(gear: ArmyItem[]): Gear[] {
        return gear.map(g => {
            const data = allGearByName[g.name];
            return {
                level: data.level,
                name: data.name,
                description: data.description,
                price: data.price,
                maximumQuantity: data.quantity,
                quantity: g.quantity,
                traits: ['Army', ...data.traits.map(trait => capitalize(trait))],
            };
        });
    }
}


export async function showArmy(game: Game, actor: Actor, token: Token): Promise<void> {
    if (actor) {
        new ArmySheet(null, {game, actor, token}).render(true);
    } else {
        ui.notifications?.error('Please select a token');
    }
}
