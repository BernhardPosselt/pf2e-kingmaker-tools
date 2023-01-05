import {getStringSetting, RollMode, setSetting} from './settings';
import {getRegionInfo} from './random-encounters';
import {DegreeOfSuccess} from './degree-of-success';
import {createUUIDLink, getLevelBasedDC, postDegreeOfSuccessMessage, roll} from './utils';

async function getQuantities(
    degreeOfSuccess: DegreeOfSuccess,
    zoneDc: number,
    zoneLevel: number,
): Promise<{ basic: number, special: number }> {
    if (degreeOfSuccess === DegreeOfSuccess.CRITICAL_SUCCESS) {
        return {
            basic: 2 * zoneDc,
            special: zoneLevel >= 14 ? 12 : (zoneLevel >= 7 ? 8 : 4),
        };
    } else if (degreeOfSuccess === DegreeOfSuccess.SUCCESS) {
        return {
            basic: zoneDc,
            special: await roll((zoneLevel >= 14 ? 3 : (zoneLevel >= 7 ? 2 : 1)) + 'd4', 'Special Ingredients'),
        };
    } else if (degreeOfSuccess === DegreeOfSuccess.FAILURE) {
        return {
            basic: zoneDc,
            special: 0,
        };
    } else {
        return {
            basic: await roll('1d4', 'Basic Ingredients'),
            special: 0,
        };
    }
}

/* eslint-disable @typescript-eslint/no-explicit-any */
export async function huntAndGather(game: Game, actor: any): Promise<void> {
    const {zoneDC, zoneLevel} = getRegionInfo(game);
    const compendium = 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects';
    const basicIngredient = (await fromUuid(`${compendium}.R2Q3OsZv8F4QCxTg`))?.toObject();
    const specialIngredient = (await fromUuid(`${compendium}.2b5UCIo9qVuOwjxe`))?.toObject();

    if (actor) {
        const result = await actor.skills.survival.roll({
            dc: zoneDC,
        });
        const quantities = await getQuantities(result.degreeOfSuccess, zoneDC, zoneLevel);
        if (quantities.special > 0) {
            specialIngredient.system.quantity = quantities.special;
            await actor.addToInventory(specialIngredient);
        }
        basicIngredient.system.quantity = quantities.basic;
        await actor.addToInventory(basicIngredient, undefined, false);
        const content = `${actor.name} gathered ${quantities.basic} basic and ${quantities.special} special ingredients`;
        await ChatMessage.create({content});
    } else {
        ui.notifications?.error('Please select a token');
    }
}

/* eslint-disable @typescript-eslint/no-explicit-any */
export async function camouflageCampsite(game: Game, actor: any): Promise<void> {
    const {zoneDC} = getRegionInfo(game);
    if (actor) {
        const result = await actor.skills.stealth.roll({
            dc: zoneDC,
            rollMode: 'blindroll',
        });
        await postDegreeOfSuccessMessage(result.degreeOfSuccess, {
            critSuccess: `Camouflage Camp: ${actor.name}'s camouflage attempt exceeds expectation. Increase the Encounter DC for your camp by 2. The first time a flat check would result in an encounter during this camping session, instead treat that result as a failure with no encounter`,
            success: `Camouflage Camp: ${actor.name}'s work helps hide your camp from detection. Increase the Encounter DC for your camp by 1.`,
            failure: `Camouflage Camp: ${actor.name}'s work does not help in particular`,
            critFailure: `Camouflage Camp: ${actor.name} believe they’ve done well at your camouflage attempt but have actually forgotten something important or accidentally did something to make the campsite more noticeable. Decrease the Encounter DC for your camp by 2, and flat checks made to determine encounters result in a critical success on a roll of 19 or 20.`,
            isPrivate: true,
        });
    } else {
        ui.notifications?.error('Please select a token');
    }
}

/* eslint-disable @typescript-eslint/no-explicit-any */
export async function organizeWatch(game: Game, actor: any): Promise<void> {
    const {zoneDC} = getRegionInfo(game);

    if (actor) {
        const result = await actor.perception.roll({
            dc: zoneDC,
        });
        await postDegreeOfSuccessMessage(result.degreeOfSuccess, {
            critSuccess: 'Organize Watch: The camp’s watch is efficient. Treat the total time required for rest as if the party size were 1 more, and all characters gain a +2 status bonus to Perception checks made during their shift on watch. @UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.010XlkzzjX38BHsO]{Organize Watch: Critical Success}',
            success: 'Organize Watch: Characters gain a +1 status bonus to Perception checks made during their shift on watch. @UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.MOXWga7l8dsypUPy]{Organize Watch: Success}',
            failure: `Organize Watch: ${actor.name}'s attempt to organize the watch doesn’t grant any additional benefits.`,
            critFailure: `Organize Watch: ${actor.name}'s attempt to organize the watch doesn’t grant any additional benefits, but they may have attracted unwanted attention. Attempt a flat check against the zone’s Encounter DC to determine if a random encounter occurs.`,
        });
    } else {
        ui.notifications?.error('Please select a token');
    }
}

/* eslint-disable @typescript-eslint/no-explicit-any */
export async function tellCampfireStory(game: Game, actor: any): Promise<void> {
    const dc = getLevelBasedDC(actor.level);

    if (actor) {
        const result = await actor.skills.performance.roll({
            dc,
        });
        await postDegreeOfSuccessMessage(result.degreeOfSuccess, {
            critSuccess: `Tell Campfire Story: ${actor.name} inspire your allies dramatically. For the remainder of the camping session, their allies gain a +2 status bonus to attack rolls, saving throws, and skill checks during combat at the campsite. The bonuses end as soon as daily preparations begin after resting is concluded. If an ally spent the hour Relaxing, they can also choose to reroll a failed roll at any time once during the remainder of the camping session while the status bonus persists; this is a fortune effect. @UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.2vdskbqd0VrWKR9Y]{Campfire Story: Critical Success}`,
            success: `Tell Campfire Story: ${actor.name} inspire their allies. For the remainder of the camping session, their allies gain a +1 status bonus to attack rolls, saving throws, and skill checks during combat at the campsite. The bonuses end as soon as daily preparations begin after resting is concluded. @UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.wT2NzrfgmWmCMRtv]{Campfire Story: Success}`,
            failure: `Tell Campfire Story: ${actor.name}'s allies are unmoved and receive no benefits.`,
            critFailure: `Tell Campfire Story: ${actor.name}'s story distracts or unsettles your allies. They each take a –1 status penalty to skill checks until they Relax or until they begin daily preparations. @UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.y5Jqw40SWNOgcxvC]{Campfire Story: Critical Failure}`,
        });
    } else {
        ui.notifications?.error('Please select a token');
    }
}

/* eslint-disable @typescript-eslint/no-explicit-any */
export async function prepareCampsite(game: Game, actor: any): Promise<void> {
    const dc = getLevelBasedDC(actor.level);

    if (actor) {
        const result = await actor.skills.survival.roll({
            dc,
        });
        await postDegreeOfSuccessMessage(result.degreeOfSuccess, {
            critSuccess: `Prepare Campsite: ${actor.name} find the perfect spot for a camp. Flat checks to determine encounters at the campsite for the next 24 hours have a DC 2 higher than normal, and the first 2 hours spent performing Camping activities does not incur the usual flat check for random encounters.`,
            success: `Prepare Campsite: ${actor.name} find a serviceable spot for a camp and for Camping activities.`,
            failure: `Prepare Campsite: ${actor.name}'s campsite will work, but it’s not the best. Campsite activities that require checks take a –2 penalty.`,
            critFailure: `Prepare Campsite: ${actor.name}'s campsite is a mess. You can use it to rest and to perform daily preparations, but it isn’t good enough to allow for Campsite activities at all. Worse, your attempt to secure a campsite has possibly attracted unwanted attention—attempt a flat check against the zone’s Encounter DC. If successful, a random encounter automatically occurs.`,
        });
    } else {
        ui.notifications?.error('Please select a token');
    }
}


function companionTpl(allCompanions: string[], preselectedCompanions: string[]): string {
    return `<form>
        ${allCompanions.map(name => {
        const label = name === 'Jubilost' ? 'Jubilost: Critical Failure' : name;
        const checked = preselectedCompanions.includes(name) ? 'checked="checked"' : '';
        return `<div><input name="${name}" type="checkbox" value="${name}" ${checked}>${label}</div>`;
    }).join('\n')}
    </form>`;
}

export async function postCompanionEffects(game: Game): Promise<void> {
    const companionConfig = {
        'Linzi': '@UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.mAEvnTqFuLIASQLB]{Bolster Confidence}',
        'Jubilost': '@UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.vpH13xgAqX4zsU6o]{Organize Camp: Critical Failure}',
        'Kanerah': '@UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.NFPQHxvpJd2vsKMe]{Enhance Campfire}',
        'Amiri': '@UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.ZKJlIqyFgbKDACnG]{Enhance Weapons}',
        'Octavia': '@UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.ptjs54eoEX8EAQNq]{Set Alarms}',
        'Nok-Nok': '@UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.PSBOS7ZEl9RGWBqD]{Set Traps}',
        'Jaethal': '@UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.KysTaC245mOnSnmE]{Undead Guardians}',
        'Kalikke': '@UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.LN6mH7Muj4hgvStt]{Water Hazards}',
        'Ekundayo': '@UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.v9i9W49PeJ9NkfXX]{Wilderness Survival}',
    };
    const preselectedCompanions = JSON.parse(getStringSetting(game, 'selectedCompanions') || '[]');
    new Dialog({
        title: 'Advance/Retract to Time of Day',
        content: companionTpl(Object.keys(companionConfig), preselectedCompanions),
        buttons: {
            post: {
                label: 'Post Effects',
                callback: async (html): Promise<void> => {
                    const $html = html as HTMLInputElement;
                    const companionsAndLabels = Object.entries(companionConfig)
                        .map(([name, link]) => {
                            const checkbox = $html.querySelector(`input[name=${name}]`) as HTMLInputElement;
                            if (checkbox.checked) {
                                return {[name]: `${name}: ${link}`};
                            } else {
                                return null;
                            }
                        })
                        .filter(v => v !== null)
                        .reduce((a: object, b) => Object.assign(a, b), {});
                    await setSetting(game, 'selectedCompanions', JSON.stringify(Object.keys(companionsAndLabels)));
                    await ChatMessage.create({content: Object.values(companionsAndLabels).join('<br>')});
                },
            },
        },
        default: 'post',
    }, {
        jQuery: false,
    }).render(true);
}

// TODO macro to record start of day
interface CookingOptions {
    game: Game;
    actor: any;
}

interface CookingFormData {
    selectedRecipe: string;
    selectedSkill: string;
}

const recipes = [
    {
        name: 'Basic Meal',
        basicIngredients: 2,
        specialIngredients: 0,
        cookingLoreDC: 18,
        survivalDC: 22,
        uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.J5nci1DS7i1wDph4',
    },
    {
        name: 'Hearty Meal',
        basicIngredients: 4,
        specialIngredients: 0,
        cookingLoreDC: 14,
        survivalDC: 16,
        uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.K5l6QZci2mCofOBg',
    },
    {
        name: 'Jeweled Rice',
        basicIngredients: 1,
        specialIngredients: 0,
        cookingLoreDC: 14,
        survivalDC: 16,
        uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.N3k7OGUKHn4kTu59',
    },
    {
        name: 'Fish-On-A-Stick',
        basicIngredients: 2,
        specialIngredients: 0,
        cookingLoreDC: 17,
        survivalDC: 19,
        uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.b59ro2GHvRvBOko8',
    },
    {
        name: 'Haggis',
        basicIngredients: 2,
        specialIngredients:0 ,
        cookingLoreDC: 15,
        survivalDC: 17,
        uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.tKcKHaxGW48w5UMj',
    },
    {
        name: 'Rice-N-Nut Pudding',
        basicIngredients: 2,
        specialIngredients: 1,
        cookingLoreDC: 16,
        survivalDC: 18,
        uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.IrrRh2yttdTDU1A5',
    },
    {
        name: 'Shepherd\'s Pie',
        basicIngredients: 4,
        specialIngredients: 0,
        cookingLoreDC: 18,
        survivalDC: 20,
        uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.GsPVSA1GNm4tvmcR',
    },
    {
        name: 'Broiled Tuskwater Oysters',
        basicIngredients: 2,
        specialIngredients: 1,
        cookingLoreDC: 20,
        survivalDC: 22,
        uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.aXmbLuQMQOMAlls3',
    },
    {
        name: 'Succulent Sausages',
        basicIngredients: 3,
        specialIngredients: 1,
        cookingLoreDC: 18,
        survivalDC: 20,
        uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.IZoPx6QH5NRY1HBi',
    },
    {
        name: 'Chocolate Ice Cream',
        basicIngredients: 2,
        specialIngredients: 1,
        cookingLoreDC: 19,
        survivalDC: 21,
        uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.xfIwKyG1pNFp8JbN',
    },
    {
        name: 'Galt Ragout',
        basicIngredients: 4,
        specialIngredients: 0,
        cookingLoreDC: 20,
        survivalDC: 22,
        uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.agI35cH4peI3aIX5',
    },
    {
        name: 'Baked Spider Legs',
        basicIngredients: 4,
        specialIngredients: 1,
        survivalDC: 22,
        cookingLoreDC: 20,
        uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.OXKHrajfyBjEBLTM',
    },
    {
        name: 'Cheese Crostata',
        basicIngredients: 4,
        specialIngredients: 0,
        cookingLoreDC: 22,
        survivalDC: 24,
        uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.GAf8W01ppdseIGbq',
    },
    {
        name: 'Grilled Silver Eel',
        basicIngredients: 4,
        specialIngredients: 1,
        cookingLoreDC: 24,
        survivalDC: 26,
        uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.UMgvKT6nNTPvp28R',
    },
    {
        name: 'Hunter\'s Roast',
        basicIngredients: 4,
        specialIngredients: 0,
        cookingLoreDC: 22,
        survivalDC: 24,
        uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.5QyW0eFSJRgmyRzU',
    },
    {
        name: 'Owlbear Omelet',
        basicIngredients: 4,
        specialIngredients: 1,
        cookingLoreDC: 25,
        survivalDC: 27,
        uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.3ak0XG0Xf66q2ApV',
    },
    {
        name: 'Sweet Pancakes',
        basicIngredients: 2,
        specialIngredients: 2,
        cookingLoreDC: 23,
        survivalDC: 25,
        uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.WrDLx0FVkaglg1hh',
    },
    {
        name: 'Smoked Trout And Hydra Pate',
        basicIngredients: 6,
        specialIngredients: 2,
        cookingLoreDC: 26,
        survivalDC: 28,
        uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.t5pXd39gRgNkPoiu',
    },
    {
        name: 'Onion Soup',
        basicIngredients: 2,
        specialIngredients: 1,
        cookingLoreDC: 24,
        survivalDC: 26,
        uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.laM0IfTV8eviU1vm',
    },
    {
        name: 'Whiterose Oysters',
        basicIngredients: 3,
        specialIngredients: 2,
        cookingLoreDC: 26,
        survivalDC: 28,
        uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.p08orFcjqQzA7KdE',
    },
    {
        name: 'Kameberry Pie',
        basicIngredients: 3,
        specialIngredients: 2,
        cookingLoreDC: 27,
        survivalDC: 29,
        uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.zX4S7wsOxB8CcVWw',
    },
    {
        name: 'Monster Casserole',
        basicIngredients: 7,
        specialIngredients: 2,
        cookingLoreDC: 28,
        survivalDC: 30,
        uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.gqT2VQGYgDQIMYsW',
    },
    {
        name: 'Seasoned Wings And Thighs',
        basicIngredients: 4,
        specialIngredients: 2,
        cookingLoreDC: 30,
        survivalDC: 32,
        uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.BTKN1IgANoof0tfB',
    },
    {
        name: 'Giant Scrambled Egg With Shambletus',
        basicIngredients: 6,
        specialIngredients: 2,
        cookingLoreDC: 33,
        survivalDC: 35,
        uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.IiV8wHXKZS1HbLLW',
    },
    {
        name: 'Mastodon Steak',
        basicIngredients: 4,
        specialIngredients: 3,
        cookingLoreDC: 34,
        survivalDC: 36,
        uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.SfNeVcYxBD4uCfrT',
    },
    {
        name: 'Hearty Purple Soup',
        basicIngredients: 6,
        specialIngredients: 3,
        cookingLoreDC: 40,
        survivalDC: 42,
        uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.yAB0s5JgzCZpf0dT',
    },
    {
        name: 'Black Linnorm Stew',
        basicIngredients: 8,
        specialIngredients: 3,
        cookingLoreDC: 43,
        survivalDC: 45,
        uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.V6uIWe6BVFV9Ewo3',
    },
    {
        name: 'First World Mince Pie',
        basicIngredients: 8,
        specialIngredients: 4,
        cookingLoreDC: 45,
        survivalDC: 47,
        uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.RmMZWrzgd8Wh8z5H',
    },
];


class CookApp extends FormApplication<CookingOptions & FormApplicationOptions, object, null> {
    static override get defaultOptions(): FormApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'cooking-app';
        options.title = 'Cooking';
        options.template = 'modules/pf2e-kingmaker-tools/templates/cooking.html';
        options.submitOnChange = true;
        options.closeOnSubmit = false;
        options.classes = ['kingmaker-tools-app'];
        options.width = 500;
        return options;
    }

    private readonly game: Game;
    private readonly actor: any;

    constructor(object: null, options: Partial<FormApplicationOptions> & CookingOptions) {
        super(object, options);
        this.actor = options.actor;
        this.game = options.game;
    }

    override getData(options?: Partial<FormApplicationOptions>): object {
        const selectedRecipe = this.getSelectedRecipe();
        const selectedRecipeData = recipes.find(r => r.name === selectedRecipe)!;
        return {
            ...super.getData(options),
            selectedRecipeName: selectedRecipe,
            selectedRecipe: selectedRecipeData,
            recipeLink: TextEditor.enrichHTML(createUUIDLink(selectedRecipeData.uuid, selectedRecipeData.name)),
            recipes,
            skills: this.getSkills(),
            selectedSkill: this.getSelectedSkill(),
        };
    }

    private getSkills(): { attribute: string, label: string }[] {
        return Object.entries(this.actor.skills)
            .filter(([attr]) => attr === 'survival' || attr === 'cooking-lore')
            .map(([attr, stat]) => {
                return {attribute: attr, label: (stat as any).label};
            });
    }

    private getSelectedRecipe(): string {
        return getStringSetting(this.game, 'lastCookedMeal') || 'Basic Meal';
    }

    private getSelectedSkill(): string {
        return getStringSetting(this.game, 'lastCookingSkill') || 'survival';
    }

    override async _updateObject(event: Event, formData?: CookingFormData): Promise<void> {
        await setSetting(this.game, 'lastCookedMeal', formData?.selectedRecipe ?? 'Basic Meal');
        await setSetting(this.game, 'lastCookingSkill', formData?.selectedSkill ?? 'survival');
        console.log(formData);
        this.render();
    }

    override activateListeners(html: JQuery): void {
        super.activateListeners(html);
        const cookButton = html[0].querySelector('#cook-button') as HTMLButtonElement;
        cookButton?.addEventListener('click', async () => {
            const selectedSkill = this.getSelectedSkill();
            const dcKey = selectedSkill === 'cooking-lore' ? 'cookingLoreDC' : 'survivalDC';
            const selectedRecipe = recipes.find(r => r.name === this.getSelectedRecipe())!;
            const result = await this.actor.skills[selectedSkill].roll({
                dc: selectedRecipe?.[dcKey] ?? 0,
            });
            await postDegreeOfSuccessMessage(result.degreeOfSuccess, {
                critSuccess: `Critical Success: ${createUUIDLink(selectedRecipe.uuid, selectedRecipe.name)}`,
                success: `Success: ${createUUIDLink(selectedRecipe.uuid, selectedRecipe.name)}`,
                critFailure: `Critical Failure: ${createUUIDLink(selectedRecipe.uuid, selectedRecipe.name)}`,
            });
            await this.close();
        });
    }
}

export async function cookRecipe(game: Game, actor: any): Promise<void> {
    if (actor) {
        new CookApp(null, {game, actor}).render(true);
    } else {
        ui.notifications?.error('Please select a token');
    }
}
