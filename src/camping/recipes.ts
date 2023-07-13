import {getRegionInfo} from './regions';
import {StringDegreeOfSuccess} from '../degree-of-success';
import {Camping} from './camping';
import {getRecipeData} from './eating';

export type Rarity = 'common' | 'uncommon' | 'rare' | 'unique';

export interface MealEffect {
    uuid: string;
    removeAfterRest?: boolean;
}

export interface CookingOutcome {
    effects?: MealEffect[];
    chooseRandomly?: boolean;
}

export interface RecipeData {
    name: string,
    basicIngredients: number,
    specialIngredients: number,
    cookingLoreDC: number,
    survivalDC: number,
    uuid: string,
    level: number,
    cost: string,
    rarity: Rarity,
    isHomebrew?: boolean;
    criticalSuccess: CookingOutcome;
    success: CookingOutcome;
    criticalFailure: CookingOutcome;
    favoriteMeal?: CookingOutcome;
}

const firstWorldMincePieEffects = {
    strength: {
        criticalSuccessUuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.69VWFB5UNLau1Rzt',
        successUuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.iV22sORhjIhWZMKw',
    },
    dexterity: {
        criticalSuccessUuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.OAxcELiLh04BLVvP',
        successUuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.CQGJi8WiBq0T8Kc6',
    },
    constitution: {
        criticalSuccessUuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.WVRQf9SLX0iRh3Ur',
        successUuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.L8HdDXKrfa3gDakt',
    },
    wisdom: {
        criticalSuccessUuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.69MQw1WMMlN4hEzb',
        successUuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.6CjFExnLSe9qij9z',
    },
    intelligence: {
        criticalSuccessUuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.yLf2n65zVocqsl6r',
        successUuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.Af25wacbdYDnFKwM',
    },
    charisma: {
        criticalSuccessUuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.sd45BQ4P7BtsYEuP',
        successUuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.VhzUKWFVrZ7tgcEQ',
    },
};

export const allRecipes: RecipeData[] = [
    {
        'name': 'Basic Meal',
        'basicIngredients': 2,
        'specialIngredients': 0,
        'cookingLoreDC': 18,
        'survivalDC': 22,
        'uuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.J5nci1DS7i1wDph4',
        'level': 0,
        'cost': '0 gp',
        'rarity': 'common',
        'criticalFailure': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.2DLTsWVdUnjewzhQ',
                removeAfterRest: true,
            }],
        },
        'criticalSuccess': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.6IguL0ipdLTiVthS',
                removeAfterRest: true,
            }],
        },
        'success': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.gqy7D2LEWSak1pzM',
                removeAfterRest: true,
            }],
        },
    },
    {
        'name': 'Hearty Meal',
        'basicIngredients': 4,
        'specialIngredients': 0,
        'cookingLoreDC': 14,
        'survivalDC': 16,
        'uuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.K5l6QZci2mCofOBg',
        'level': 0,
        'cost': '0 gp',
        'rarity': 'common',
        'criticalSuccess': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.7aBiVefiVDXS2XmV'}],
        },
        'favoriteMeal': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.KeiNZDcAiGZjyzhn',
                removeAfterRest: true,
            }],
        },
        'criticalFailure': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.LbW38wFmd1JSBPK6',
                removeAfterRest: true,
            }],
        },
        'success': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.uI4ZsdIGx7Yoy2rI'}],
        },
    },
    {
        'name': 'Jeweled Rice',
        'basicIngredients': 1,
        'specialIngredients': 0,
        'cookingLoreDC': 14,
        'survivalDC': 16,
        'uuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.N3k7OGUKHn4kTu59',
        'level': 0,
        'cost': '5 sp',
        'rarity': 'common',
        'criticalFailure': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.MGFPIxwSgRLvkV71'}],
        },
        'criticalSuccess': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.OxEApkXu0hw34Ci5'}],
        },
        'success': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.Sc5N6l1oCY1oiTfA'}],
        },
        'favoriteMeal': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.d8xr0i8GHY4195sa'}],
        },
    },
    {
        'name': 'Fish-On-A-Stick',
        'basicIngredients': 2,
        'specialIngredients': 0,
        'cookingLoreDC': 17,
        'survivalDC': 19,
        'uuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.b59ro2GHvRvBOko8',
        'level': 1,
        'cost': '1 gp',
        'rarity': 'common',
        'success': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.8eIGJPWFlg4L94wY'}],
        },
        'criticalSuccess': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.L5sACVlUjYP6lJ7q'}],
        },
        'criticalFailure': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.c9qgXgiAJikyTM0p',
            }],
        },
        'favoriteMeal': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.dZTQ5rfedrpLUTxE',
            }],
        },
    },
    {
        'name': 'Haggis',
        'basicIngredients': 2,
        'specialIngredients': 0,
        'cookingLoreDC': 15,
        'survivalDC': 17,
        'uuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.tKcKHaxGW48w5UMj',
        'level': 1,
        'cost': '1 gp',
        'rarity': 'common',
        'criticalSuccess': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.1CIuG1XTo4R8fmeL'}],
        },
        'success': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.ISeHA2sG6wwJyV6I'}],
        },
        'favoriteMeal': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.RttWd7ZJNbsFsQjE'}],
        },
        'criticalFailure': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.k0BWf8aF5plRvhyk',
                removeAfterRest: true,
            }],
        },
    },
    {
        'name': 'Rice-N-Nut Pudding',
        'basicIngredients': 2,
        'specialIngredients': 1,
        'cookingLoreDC': 16,
        'survivalDC': 18,
        'uuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.IrrRh2yttdTDU1A5',
        'level': 2,
        'cost': '2 gp',
        'rarity': 'common',
        'criticalSuccess': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.3Gj0AaUzqwN0CdTi'}],
        },
        'criticalFailure': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.AI1hYVSAMmKgEO9u'}],
        },
        'success': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.GzZYeBB1UGMAykXc',
                removeAfterRest: true,
            }],
        },
        'favoriteMeal': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.IRTWUl4HjRa82A1B'}],
        },
    },
    {
        'name': 'Shepherd\'s Pie',
        'basicIngredients': 4,
        'specialIngredients': 0,
        'cookingLoreDC': 18,
        'survivalDC': 20,
        'uuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.GsPVSA1GNm4tvmcR',
        'level': 2,
        'cost': '2 gp',
        'rarity': 'uncommon',
        'criticalSuccess': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.3a5uIGu77TLQE9ZB'}],
        },
        'criticalFailure': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.84dIJjkOdLeHWkx2'}],
        },
        'success': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.COa19ug1vjTGHy04'}],
        },
        'favoriteMeal': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.RL8zdU7YyoiduDva'}],
        },
    },
    {
        'name': 'Broiled Tuskwater Oysters',
        'basicIngredients': 2,
        'specialIngredients': 1,
        'cookingLoreDC': 20,
        'survivalDC': 22,
        'uuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.aXmbLuQMQOMAlls3',
        'level': 3,
        'cost': '3 gp',
        'rarity': 'uncommon',
        'favoriteMeal': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.57461QLkB8M3S0AS'}],
        },
        'success': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.BlKsZq6anCMSEshB',
                removeAfterRest: true,
            }],
        },
        'criticalFailure': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.X8sGtdz4yfVl2pKJ'}],
        },
        'criticalSuccess': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.ebP1xWOh7vGZmmlz'}],
        },
    },
    {
        'name': 'Succulent Sausages',
        'basicIngredients': 3,
        'specialIngredients': 1,
        'cookingLoreDC': 18,
        'survivalDC': 20,
        'uuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.IZoPx6QH5NRY1HBi',
        'level': 3,
        'cost': '3 gp',
        'rarity': 'common',
        'favoriteMeal': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.EULsp9VffKbWpD0b'}],
        },
        'success': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.G0qnixpCakGeHTp1',
                removeAfterRest: true,
            }],
        },
        'criticalFailure': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.WDJQpYYx0N91mkO0',
                removeAfterRest: true,
            }],
        },
        'criticalSuccess': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.jseoPPvbPg4Vr7MI'}],
        },
    },
    {
        'name': 'Chocolate Ice Cream',
        'basicIngredients': 2,
        'specialIngredients': 1,
        'cookingLoreDC': 19,
        'survivalDC': 21,
        'uuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.xfIwKyG1pNFp8JbN',
        'level': 4,
        'cost': '5 gp',
        'rarity': 'common',
        'favoriteMeal': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.0QmbpFYAoyzUu1gi'}],
        },
        'criticalSuccess': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.9brr5UKBsZcboAOx'}],
        },
        'criticalFailure': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.KqktmooFbYLQQ2S1'}],
        },
        'success': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.L85BNpJEFAozA9kW',
                removeAfterRest: true,
            }],
        },
    },
    {
        'name': 'Galt Ragout',
        'basicIngredients': 4,
        'specialIngredients': 0,
        'cookingLoreDC': 20,
        'survivalDC': 22,
        'uuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.agI35cH4peI3aIX5',
        'level': 4,
        'cost': '5 gp',
        'rarity': 'uncommon',
        'criticalSuccess': {
            'effects': [
                {uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.MADgm2doEOSlNe7u'},
                {uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.JqxNxAVZzAdCAoCZ'},
            ],
        },
        'favoriteMeal': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.Uh9fQWIE0Nc2hwLi'}],
        },
        'criticalFailure': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.bH9QjKt6LybagxcR',
                removeAfterRest: true,
            }],
        },
        'success': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.x1lKNowlbhotDu0q'}],
        },
    },
    {
        'name': 'Baked Spider Legs',
        'basicIngredients': 4,
        'specialIngredients': 1,
        'survivalDC': 22,
        'cookingLoreDC': 20,
        'uuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.OXKHrajfyBjEBLTM',
        'level': 5,
        'cost': '8 gp',
        'rarity': 'common',
        'criticalFailure': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.0faDj5jOYlPTwB21'}],
        },
        'favoriteMeal': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.M2gTzsYSnmcq7Lho'}],
        },
        'criticalSuccess': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.MVrKXXHzxzyfy3mi'}],
        },
        'success': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.kmHHmbR283uOoDK9'}],
        },
    },
    {
        'name': 'Cheese Crostata',
        'basicIngredients': 4,
        'specialIngredients': 0,
        'cookingLoreDC': 22,
        'survivalDC': 24,
        'uuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.GAf8W01ppdseIGbq',
        'level': 5,
        'cost': '8 gp',
        'rarity': 'uncommon',
        'success': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.1VchCdA1LMBhYENe'}],
        },
        'favoriteMeal': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.2c2nrlkL2XJXXQIA'}],
        },
        'criticalFailure': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.EBa9BjdANCMLKMEy',
                removeAfterRest: true,
            }],
        },
        'criticalSuccess': {
            'effects': [
                {uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.MI6MF3R5OswdGBUd'},
                {uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.X7fTfenq4TukJJM4'},
            ],
        },
    },
    {
        'name': 'Grilled Silver Eel',
        'basicIngredients': 4,
        'specialIngredients': 1,
        'cookingLoreDC': 24,
        'survivalDC': 26,
        'uuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.UMgvKT6nNTPvp28R',
        'level': 6,
        'cost': '13 gp',
        'rarity': 'uncommon',
        'favoriteMeal': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.Dj9026jn8dI7oP0j'}],
        },
        'criticalFailure': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.Vb39DIJ5OfWpN2ig',
                removeAfterRest: true,
            }],
        },
        'criticalSuccess': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.Zw8I0G3RqDfNHuCl'}],
        },
        'success': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.ta81jwzhkNsN88EZ'}],
        },
    },
    {
        'name': 'Hunter\'s Roast',
        'basicIngredients': 4,
        'specialIngredients': 0,
        'cookingLoreDC': 22,
        'survivalDC': 24,
        'uuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.5QyW0eFSJRgmyRzU',
        'level': 6,
        'cost': '13 gp',
        'rarity': 'common',
        'success': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.0un8LMe7iv59He25',
                removeAfterRest: true,
            }],
        },
        'criticalSuccess': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.LiXnKWPOkDBZRpLE'}],
        },
        'criticalFailure': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.RzOGQxp3VHlpYtWJ',
                removeAfterRest: true,
            }],
        },
        'favoriteMeal': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.zuPWMtCTzuD96Ykz'}],
        },
    },
    {
        'name': 'Owlbear Omelet',
        'basicIngredients': 4,
        'specialIngredients': 1,
        'cookingLoreDC': 25,
        'survivalDC': 27,
        'uuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.3ak0XG0Xf66q2ApV',
        'level': 7,
        'cost': '18 gp',
        'rarity': 'uncommon',
        'criticalSuccess': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.JY3TBLbVAbQHtmtu'}],
        },
        'favoriteMeal': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.O4XVtCWn4Hq5QKUP'}],
        },
        'success': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.RLJuUa9tzPrC6PqP',
                removeAfterRest: true,
            }],
        },
        'criticalFailure': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.kyzTn0AUN4Q8G6TH'}],
        },
    },
    {
        'name': 'Sweet Pancakes',
        'basicIngredients': 2,
        'specialIngredients': 2,
        'cookingLoreDC': 23,
        'survivalDC': 25,
        'uuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.WrDLx0FVkaglg1hh',
        'level': 7,
        'cost': '18 gp',
        'rarity': 'common',
        'success': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.BXTnX9xhnYvolrFk'}],
        },
        'favoriteMeal': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.Lpo0XSUVmGDQaXtz'}],
        },
        'criticalFailure': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.ovbBMpOBtB1CrLOv'}],
        },
        'criticalSuccess': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.q5JHZeJeAIscA1ky'}],
        },
    },
    {
        'name': 'Smoked Trout And Hydra Pate',
        'basicIngredients': 6,
        'specialIngredients': 2,
        'cookingLoreDC': 26,
        'survivalDC': 28,
        'uuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.t5pXd39gRgNkPoiu',
        'level': 8,
        'cost': '25 gp',
        'rarity': 'uncommon',
        'criticalSuccess': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.4d6roHW75xe52o7A'}],
        },
        'criticalFailure': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.LBzsqdKb182lO5Fm',
                removeAfterRest: true,
            }],
        },
        'success': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.miTcZbTcQLJu2NKR',
                removeAfterRest: true,
            }],
        },
        'favoriteMeal': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.ve7NrzeHMwLsmbl3'}],
        },
    },
    {
        'name': 'Onion Soup',
        'basicIngredients': 2,
        'specialIngredients': 1,
        'cookingLoreDC': 24,
        'survivalDC': 26,
        'uuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.laM0IfTV8eviU1vm',
        'level': 8,
        'cost': '25 gp',
        'rarity': 'common',
        'criticalSuccess': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.6Vu7Ts4p79O6JsD2'}],
        },
        'success': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.8IzlPm2joulb5r0p',
                removeAfterRest: true,
            }],
        },
        'criticalFailure': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.hHTMldJIDoqpCF7U'}],
        },
        'favoriteMeal': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.pIgoUydEo8wU2Avm'}],
        },
    },
    {
        'name': 'Whiterose Oysters',
        'basicIngredients': 3,
        'specialIngredients': 2,
        'cookingLoreDC': 26,
        'survivalDC': 28,
        'uuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.p08orFcjqQzA7KdE',
        'level': 9,
        'cost': '35 gp',
        'rarity': 'common',
        'favoriteMeal': {
            'effects': [
                {uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.E3tZfxCJ5GQoIbei'},
                {
                    uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.cCKgpxIsFevgENf2',
                    removeAfterRest: true,
                },
            ],
        },
        'criticalSuccess': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.ZDDZz5ynEOR20LUC'}],
        },
        'success': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.uw5M3YfEJs23RmX5',
                removeAfterRest: true,
            }],
        },
        'criticalFailure': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.yMsjuwLmbF8h1jVt'}],
        },
    },
    {
        'name': 'Kameberry Pie',
        'basicIngredients': 3,
        'specialIngredients': 2,
        'cookingLoreDC': 27,
        'survivalDC': 29,
        'uuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.zX4S7wsOxB8CcVWw',
        'level': 10,
        'cost': '50 gp',
        'rarity': 'common',
        'success': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.elsXArG4GqGin6LB',
                removeAfterRest: true,
            }],
        },
        'criticalFailure': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.lWy7BZYGab0uNf9H'}],
        },
        'criticalSuccess': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.n6ykp8NjXDFcvqXM',
                removeAfterRest: true,
            }],
        },
        'favoriteMeal': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.sQz8ugumjOMSHiYt'}],
        },
    },
    {
        'name': 'Monster Casserole',
        'basicIngredients': 7,
        'specialIngredients': 2,
        'cookingLoreDC': 28,
        'survivalDC': 30,
        'uuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.gqT2VQGYgDQIMYsW',
        'level': 11,
        'cost': '70 gp',
        'rarity': 'common',
        'success': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.5GT0JeRjYJc8e5ZA',
                removeAfterRest: true,
            }],
        },
        'criticalFailure': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.af6hFeUUfsF1GiX8'}],
        },
        'criticalSuccess': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.gNGXKMmJdNQJZode'}],
        },
        'favoriteMeal': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.qad6gIwEpQQIkZwQ'}],
        },
    },
    {
        'name': 'Seasoned Wings And Thighs',
        'basicIngredients': 4,
        'specialIngredients': 2,
        'cookingLoreDC': 30,
        'survivalDC': 32,
        'uuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.BTKN1IgANoof0tfB',
        'level': 12,
        'cost': '100 gp',
        'rarity': 'common',
        'criticalSuccess': {
            'effects': [
                {
                    uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.uchgkN3hm1T51zfM',
                    removeAfterRest: true,
                },
                {uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.JqKhpMXiQCEsDSCw'},
            ],
        },
        'success': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.JqKhpMXiQCEsDSCw'}],
        },
        'criticalFailure': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.q3vnRHRBn3HmRhkV',
                removeAfterRest: true,
            }],
        },
        'favoriteMeal': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.xF0K40VzyjIbbRMA'}],
        },
    },
    {
        'name': 'Giant Scrambled Egg With Shambletus',
        'basicIngredients': 6,
        'specialIngredients': 2,
        'cookingLoreDC': 33,
        'survivalDC': 35,
        'uuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.IiV8wHXKZS1HbLLW',
        'level': 13,
        'cost': '150 gp',
        'rarity': 'uncommon',
        'favoriteMeal': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.Q1Qg8K9G7R9wU4rI'}],
        },
        'success': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.TLxckltEWbHKCXDi'}],
        },
        'criticalFailure': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.pTjrRRqxUJnbDRIB'}],
        },
        'criticalSuccess': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.f2yKkelj374SNszb'}],
        },
    },
    {
        'name': 'Mastodon Steak',
        'basicIngredients': 4,
        'specialIngredients': 3,
        'cookingLoreDC': 34,
        'survivalDC': 36,
        'uuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.SfNeVcYxBD4uCfrT',
        'level': 14,
        'cost': '225 gp',
        'rarity': 'uncommon',
        'criticalFailure': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.0yH2V9UFRyGwXIRn'}],
        },
        'favoriteMeal': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.Ln9Q3cH6NzGZAJUs'}],
        },
        'success': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.M6jcQsHsPEkigTTG',
                removeAfterRest: true,
            }],
        },
        'criticalSuccess': {
            'effects': [{
                uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.xq4qNyrsL5wVYkcQ',
                removeAfterRest: true,
            }],
        },
    },
    {
        'name': 'Hearty Purple Soup',
        'basicIngredients': 6,
        'specialIngredients': 3,
        'cookingLoreDC': 40,
        'survivalDC': 42,
        'uuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.yAB0s5JgzCZpf0dT',
        'level': 16,
        'cost': '500 gp',
        'rarity': 'rare',
        'favoriteMeal': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.6RjaP1s58ITIVn6u'}],
        },
        'success': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.Zn0CB7bL7vHrMNTU'}],
        },
        'criticalSuccess': {
            'effects': [
                {uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.hLGCjXajAYEcII9J'},
            ],
        },
        'criticalFailure': {
            'effects': [
                {uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.o6F8QZbTvt9os8Ni'},
                {uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.TQXXVzD1ZZHnouTI'},
                {uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.SJvEGeMryIekwIKk'},
            ],
        },
    },
    {
        'name': 'Black Linnorm Stew',
        'basicIngredients': 8,
        'specialIngredients': 3,
        'cookingLoreDC': 43,
        'survivalDC': 45,
        'uuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.V6uIWe6BVFV9Ewo3',
        'level': 18,
        'cost': '1200 gp',
        'rarity': 'rare',
        'criticalSuccess': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.I2LiwNjrowS5u4UG'}],
        },
        'success': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.m3FgRCWbVupkaORL'}],
        },
        'criticalFailure': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.s1DfJpXFkJorHsuu'}],
        },
        'favoriteMeal': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.sxr0BsJJGeLyNBgq'}],
        },
    },
    {
        'name': 'First World Mince Pie',
        'basicIngredients': 8,
        'specialIngredients': 4,
        'cookingLoreDC': 45,
        'survivalDC': 47,
        'uuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.RmMZWrzgd8Wh8z5H',
        'level': 20,
        'cost': '3500 gp',
        'rarity': 'rare',
        'criticalSuccess': {
            'effects': Object.values(firstWorldMincePieEffects).map(v => {
                return {uuid: v.criticalSuccessUuid};
            }),
            chooseRandomly: true,
        },
        'criticalFailure': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.72N0nZAAo2iuCGBd'}],
        },
        'success': {
            'effects': Object.values(firstWorldMincePieEffects).map(v => {
                return {uuid: v.successUuid};
            }),
            chooseRandomly: true,
        },
        'favoriteMeal': {
            'effects': [{uuid: 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.Wfmt1NvfIqF3gFav'}],
        },
    },
];


export function getRecipesKnownInRegion(game: Game, region: string, recipes: RecipeData[]): RecipeData[] {
    const {zoneLevel} = getRegionInfo(game, region);
    return recipes.filter(recipe => recipe.rarity === 'common' && recipe.level <= zoneLevel);
}

export function getOutcomeEffects(outcome: CookingOutcome | undefined, includeAll: boolean): string[] {
    const effects = outcome?.effects?.flatMap(e => e.uuid) ?? [];
    if (!includeAll && outcome?.chooseRandomly && effects.length > 0) {
        const randomIndex = Math.floor(Math.random() * (effects.length - 1));
        return [effects[randomIndex]];
    } else {
        return effects;
    }
}

export function getExpiringOutcomeEffects(outcome: CookingOutcome | undefined): string[] {
    return outcome?.effects
        ?.filter(e => e.removeAfterRest)
        ?.flatMap(e => e.uuid) ?? [];
}

export function getAllMealEffectUuids(recipes: RecipeData[]): string[] {
    return recipes.flatMap(r => [
        ...getOutcomeEffects(r.criticalFailure, true),
        ...getOutcomeEffects(r.success, true),
        ...getOutcomeEffects(r.criticalSuccess, true),
        ...getOutcomeEffects(r.favoriteMeal, true),
    ]);
}

export function getAllExpiringMealEffectUuids(recipes: RecipeData[]): string[] {
    return recipes.flatMap(r => [
        ...getExpiringOutcomeEffects(r.criticalFailure),
        ...getExpiringOutcomeEffects(r.success),
        ...getExpiringOutcomeEffects(r.criticalSuccess),
        ...getExpiringOutcomeEffects(r.favoriteMeal),
    ]);
}

export function getMealEffectUuids(recipe: RecipeData, favoriteMeal: string | undefined, degree: StringDegreeOfSuccess): string[] {
    const favoriteMealEffects = recipe.name === favoriteMeal ? getOutcomeEffects(recipe.favoriteMeal, false) : [];
    const effects = degree === 'criticalSuccess'
    || degree === 'success'
    || degree === 'criticalFailure' ? getOutcomeEffects(recipe[degree], false) : [];
    return degree === 'criticalSuccess' || degree === 'success' ? [...effects, ...favoriteMealEffects] : effects;
}

export function getKnownRecipes(camping: Camping): string[] {
    const recipes = new Set(getRecipeData(camping).map(r => r.name));
    return camping.cooking.knownRecipes.filter(r => recipes.has(r));
}
