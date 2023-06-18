export type Rarity = 'common' | 'uncommon' | 'rare' | 'unique';

export interface CookingOutcome {
    message: string;
    effectUuid?: string;
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

const allRecipes: RecipeData[] = [
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
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.2DLTsWVdUnjewzhQ',
            'message': 'Your meal wreaks havoc on digestion. A character who partook of this meal becomes sickened 1 until after they rest and complete their daily preparations.',
        },
        'criticalSuccess': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.6IguL0ipdLTiVthS',
            'message': 'You prepare a delicious meal. A character who eats this meal recovers Hit Points equal to their Constitution modifier (minimum 1) multiplied by twice their level when they rest during this camping session instead of the normal amount, and they gain a +1 status bonus to all saving throws until they complete their daily preparations, or begin adventuring again.',
        },
        'success': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.gqy7D2LEWSak1pzM',
            'message': 'You prepare a meal. A character who eats it gains a +1 status bonus to all saving throws until they complete their daily preparations or begin adventuring again.',
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
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.7aBiVefiVDXS2XmV',
            'message': 'The meal grants the eater a +1 status bonus to the next 3 saving throws they attempt during the next 24 hours.',
        },
        'favoriteMeal': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.KeiNZDcAiGZjyzhn',
            'message': 'The eater recovers an additional amount of Hit Points equal to their level when they rest.',
        },
        'criticalFailure': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.LbW38wFmd1JSBPK6',
            'message': 'The hearty meal leaves the eater overstuffed. They suffer a –1 status penalty to initiative checks until they rest and begin their daily preparations',
        },
        'success': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.uI4ZsdIGx7Yoy2rI',
            'message': 'The meal grants the eater a +1 status bonus to the next saving throw they attempt during the next 24 hours.',
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
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.MGFPIxwSgRLvkV71',
            'message': 'The meal was a bit too light; you must eat another meal to stave off starvation.',
        },
        'criticalSuccess': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.OxEApkXu0hw34Ci5',
            'message': 'Twice during the next 24 hours when the eater takes the Step action, they carefully move 10 feet instead of five feet.',
        },
        'success': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.Sc5N6l1oCY1oiTfA',
            'message': 'Once during the next 24 hours when the eater takes the Step action, they carefully move 10 feet instead of five feet.',
        },
        'favoriteMeal': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.d8xr0i8GHY4195sa',
            'message': '+1 status bonus to Acrobatics checks to Escape for 24 hours.',
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
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.8eIGJPWFlg4L94wY',
            'message': 'For the next 24 hours, the eater can go for up to 17 hours without sleep before becoming fatigued.',
        },
        'criticalSuccess': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.L5sACVlUjYP6lJ7q',
            'message': 'For 2 days, the eater can go for up to 18 hours without sleep before becoming fatigued.',
        },
        'criticalFailure': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.c9qgXgiAJikyTM0p',
            'message': 'The fish is bad, making it tougher for the eater to get the full restorative effects of sleep. They must sleep 9 hours instead of 8 before they can make daily preparations.',
        },
        'favoriteMeal': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.dZTQ5rfedrpLUTxE',
            'message': 'The next time the eater rests within the next 8 hours, they need to sleep only 7 hours before they can begin daily preparations.',
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
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.1CIuG1XTo4R8fmeL',
            'message': 'The meal bolsters the eater against illness, granting a +1 status bonus to saving throws against effects that cause disease or the sickened condition for 24 hours.',
        },
        'success': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.ISeHA2sG6wwJyV6I',
            'message': 'The meal bolsters the eater against nausea, granting a +1 status bonus to effects that cause the sickened condition for 24 hours.',
        },
        'favoriteMeal': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.RttWd7ZJNbsFsQjE',
            'message': '+1 status bonus to Will saves against fear effects for 24 hours.',
        },
        'criticalFailure': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.k0BWf8aF5plRvhyk',
            'message': 'The meal makes the eater quite ill. They become sickened 1 until they get a full night’s rest. This is a poison effect.',
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
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.3Gj0AaUzqwN0CdTi',
            'message': 'The meal grants a +1 status bonus to Arcana checks made to Identify Magic and to Learn a Spell for the next 24 hours.',
        },
        'criticalFailure': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.AI1hYVSAMmKgEO9u',
            'message': 'The meal is filling, but its magical influence accidentally makes the eater more susceptible to certain magical effects which are drawn to the eater like iron filings to a magnet. The eater suffers a –1 status penalty to Reflex saves against spells for the next 24 hours. This is a curse effect.',
        },
        'success': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.GzZYeBB1UGMAykXc',
            'message': 'The meal grants a +1 status bonus to Arcana checks made to Identify Magic and to Learn a spell for the remainder of the camping session.',
        },
        'favoriteMeal': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.IRTWUl4HjRa82A1B',
            'message': 'Once during the next 24 hours, when the eater Refocuses to regain Focus Points, the restoration of magic infuses and invigorates them, restoring a number of Hit Points equal to 1d8 plus their level.',
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
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.3a5uIGu77TLQE9ZB',
            'message': 'The meal augments physical recovery. When the eater recovers Hit Points from resting during this camping session, they recover an additional 4d6 Hit Points. If the eater was drained, they reduce their drained condition by 2 points rather than by 1.',
        },
        'criticalFailure': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.84dIJjkOdLeHWkx2',
            'message': 'The meal doesn’t sit well. When the eater rests during this camping session, they regain only half the amount of Hit Points from resting that they normally would have.',
        },
        'success': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.COa19ug1vjTGHy04',
            'message': 'The meal augments the body’s ability to recover. When the eater recovers Hit Points from resting during this camping session, they recover an additional 2d6 Hit Points.',
        },
        'favoriteMeal': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.RL8zdU7YyoiduDva',
            'message': 'For the next 24 hours, the eater’s healing ability is enhanced. Whenever they recover Hit Points with a healing effect, they restore 1 additional Hit Point. If an effect would heal them more than once, such as fast healing, the additional Hit Point applies only the first time they’re healed',
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
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.57461QLkB8M3S0AS',
            'message': '+1 status bonus to saving throws against occult spells',
        },
        'success': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.BlKsZq6anCMSEshB',
            'message': 'The meal grants a +1 status bonus to Occultism checks made to Identify Magic, Learn a Spell, and Recall Knowledge until the eater begins daily preparations.',
        },
        'criticalFailure': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.X8sGtdz4yfVl2pKJ',
            'message': 'The poorly prepared oysters cause mild but distracting hallucinations, resulting in a –1 status penalty to Perception checks for 24 hours. This is a poison effect.',
        },
        'criticalSuccess': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.ebP1xWOh7vGZmmlz',
            'message': 'The meal grants a +1 status bonus to Occultism checks made to Identify Magic, Learn a Spell, and Recall Knowledge for 24 hours.',
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
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.EULsp9VffKbWpD0b',
            'message': 'The eater gains the following reaction, which they can use once during the next 24 hours. Careful Casting free action Frequency once per day; Trigger a reaction disrupts your spell; Effect You focus on your spellcasting in order to keep your spell from slipping away. Attempt a DC 15 Flat Check. On a success, the spell is not disrupted, and you cast it successfully.',
        },
        'success': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.G0qnixpCakGeHTp1',
            'message': 'The meal helps the eater recover from burns, bleeding, and other persistent damage, reducing the DC of flat checks made to end persistent damage by 4 until the eater begins their daily preparations.',
        },
        'criticalFailure': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.WDJQpYYx0N91mkO0',
            'message': 'The sausages leave the eater feeling bloated and uncomfortable. They become clumsy 1 until they get a full night’s rest. This is a poison effect.',
        },
        'criticalSuccess': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.jseoPPvbPg4Vr7MI',
            'message': 'The meal helps the eater recover from burns, bleeding, and other persistent damage, reducing the DC of flat checks made to end persistent damage by 4 for the next 24 hours.',
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
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.0QmbpFYAoyzUu1gi',
            'message': '+1 status bonus to all Lore checks made to Recall Knowledge.',
        },
        'criticalSuccess': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.9brr5UKBsZcboAOx',
            'message': 'The ice cream is perfect. The eater receives a +1 status bonus to Performance checks for 24 hours.',
        },
        'criticalFailure': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.KqktmooFbYLQQ2S1',
            'message': 'The ice cream is too cold, too sweet, or both. In addition to not gaining any special benefit from the meal, disappointment causes the eater to take a –1 status penalty to saving throws against emotion effects for 24 hours or until the character achieves a critical success on a saving throw against an emotion effect, whichever comes first.',
        },
        'success': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.L85BNpJEFAozA9kW',
            'message': 'The ice cream is perfect. The eater receives a +1 status bonus to Performance checks until they make their daily preparations.',
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
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.MADgm2doEOSlNe7u',
            'message': 'The eater receives a +1 status bonus to saving throws against effects that cause the clumsy condition or fatigue, and during the next 2 days, they can go for up to 18 hours without sleep before becoming fatigued.',
        },
        'favoriteMeal': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.Uh9fQWIE0Nc2hwLi',
            'message': '+1 status bonus to Acrobatics checks to Tumble Through.',
        },
        'criticalFailure': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.bH9QjKt6LybagxcR',
            'message': 'The spicy meal doesn’t sit well. The eater is sickened 1 until they get a full night’s rest.',
        },
        'success': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.x1lKNowlbhotDu0q',
            'message': 'During the next 24 hours, the eater can go for up to 18 hours without sleep before becoming fatigued.',
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
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.0faDj5jOYlPTwB21',
            'message': 'The meal is toxic and causes a –1 status penalty to Fortitude saving throws for 24 hours. This is a poison effect.',
        },
        'favoriteMeal': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.M2gTzsYSnmcq7Lho',
            'message': '+1 status bonus to Stealth checks.',
        },
        'criticalSuccess': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.MVrKXXHzxzyfy3mi',
            'message': 'The meal grants a +1 status bonus to Fortitude saving throws for 24 hours.',
        },
        'success': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.kmHHmbR283uOoDK9',
            'message': 'The meal grants a +1 status bonus to Fortitude saving throws against poison effects for 24 hours.',
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
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.1VchCdA1LMBhYENe',
            'message': 'For the next 24 hours, the eater can go for up to 18 hours without sleep before becoming fatigued.',
        },
        'favoriteMeal': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.2c2nrlkL2XJXXQIA',
            'message': '+1 status bonus to Religion checks',
        },
        'criticalFailure': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.EBa9BjdANCMLKMEy',
            'message': 'The heavy meal doesn’t sit well. The eater is sickened 1 until they finish a full night’s rest.',
        },
        'criticalSuccess': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.MI6MF3R5OswdGBUd',
            'message': 'The eater gains a +1 status bonus to saving throws against effects that cause the enfeebled condition or fatigue, and during the next 2 days, they can go for up to 20 hours without sleep before becoming fatigued.',
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
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.Dj9026jn8dI7oP0j',
            'message': 'The meal grants the following action usable once during the next 24 hours. \nActivate f envision; Frequency once per meal; Effect Your Speed increases by 5 feet for 1 minute. @UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.cFxiQsxvPP31qJhB]{Grilled Silver Eel: Speed}',
        },
        'criticalFailure': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.Vb39DIJ5OfWpN2ig',
            'message': 'The eel doesn’t sit well, and the distracting stomach cramps and shortness of breath make it more difficult to move. The eater’s Speed is reduced by 5 feet until they complete their daily preparations. This is a poison effect.',
        },
        'criticalSuccess': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.Zw8I0G3RqDfNHuCl',
            'message': 'The meal grants the eater a +1 status bonus to Lie and to Tumble Through for 24 hours.',
        },
        'success': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.ta81jwzhkNsN88EZ',
            'message': 'The meal grants the eater a +1 status bonus to Tumble Through for 24 hours.',
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
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.0un8LMe7iv59He25',
            'message': 'The eater gains a number of temporary Hit Points equal to their level, lasting until the eater begins their daily preparations.',
        },
        'criticalSuccess': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.LiXnKWPOkDBZRpLE',
            'message': 'The eater gains a number of temporary Hit Points equal to their level, lasting up to 24 hours.',
        },
        'criticalFailure': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.RzOGQxp3VHlpYtWJ',
            'message': 'The hunter’s roast is more than unpalatable—it’s poisonous! The eater takes [[/r {2d6}[poison]]]{2d6 Poison Damage} poison damage and is Sickened 1 until they get a full night’s rest. This is a poison effect.',
        },
        'favoriteMeal': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.zuPWMtCTzuD96Ykz',
            'message': '+1 status bonus to Nature checks.',
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
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.JY3TBLbVAbQHtmtu',
            'message': 'The meal grants a +1 status bonus to one-handed melee Strike damage rolls for 24 hours.',
        },
        'favoriteMeal': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.O4XVtCWn4Hq5QKUP',
            'message': '+1 status bonus to Nature checks.',
        },
        'success': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.RLJuUa9tzPrC6PqP',
            'message': 'The meal grants a +1 status bonus to one-handed melee Strike damage rolls for the remainder of the camping session.',
        },
        'criticalFailure': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.kyzTn0AUN4Q8G6TH',
            'message': 'The owlbear omelet continues to fight like a monster, even in the eater’s belly. They suffer a –1 status penalty to melee Strikes for the next 24 hours. This is a poison effect.',
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
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.BXTnX9xhnYvolrFk',
            'message': 'The meal grants a +1 status bonus to Reflex saves for the remainder of the camping session.',
        },
        'favoriteMeal': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.Lpo0XSUVmGDQaXtz',
            'message': 'When combat begins, the eater enjoys a quick boost of speed—on their first turn in each combat during the next 24 hours, they increase their Speed by 5 feet.',
        },
        'criticalFailure': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.ovbBMpOBtB1CrLOv',
            'message': 'The pancakes are simply too sweet! Jitters cause the eater to suffer a –1 status penalty to Reflex saves for 24 hours; this is a poison effect.',
        },
        'criticalSuccess': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.q5JHZeJeAIscA1ky',
            'message': 'The meal grants a +1 status bonus to Reflex saves for 24 hours.',
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
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.4d6roHW75xe52o7A',
            'message': 'For the next 24 hours, the eater treats light armor they’re at least trained in as if the armor had the comfort trait and gain a +1 status bonus to Fortitude saves.',
        },
        'criticalFailure': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.LBzsqdKb182lO5Fm',
            'message': 'The meal was prepared poorly, and as such the eater gains no nutritional value from it. Worse, the aches and pains they endure cause them to become Enfeebled 1 until they’ve had a full night of rest. This is a poison effect.',
        },
        'success': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.miTcZbTcQLJu2NKR',
            'message': 'Until the end of this camping session, the eater treats light armor they’re at least trained in as if the armor had the comfort trait and gain a +1 status bonus to Fortitude saves.',
        },
        'favoriteMeal': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.ve7NrzeHMwLsmbl3',
            'message': '+1 status bonus to Athletics checks for 24 hours.',
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
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.6Vu7Ts4p79O6JsD2',
            'message': 'The meal grants a +1 status bonus to Will saves for 24 hours.',
        },
        'success': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.8IzlPm2joulb5r0p',
            'message': 'The meal grants a +1 status bonus to Will saves for the remainder of the camping session.',
        },
        'criticalFailure': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.hHTMldJIDoqpCF7U',
            'message': 'The meal results in an obnoxious case of lingering halitosis, causing a –1 status penalty to Charisma-based skill checks for the next 24 hours. This is a poison effect.',
        },
        'favoriteMeal': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.pIgoUydEo8wU2Avm',
            'message': '+1 status bonus to Arcana checks.',
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
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.E3tZfxCJ5GQoIbei',
            'message': 'The meal infuses the eater’s impressions with additional power, spreading the benefits of the meal to other uses of Diplomacy and Intimidation. The eater gains a +2 status bonus to Intimidation checks to Demoralize for the remainder of the camping session and a +2 status bonus to Diplomacy checks to Make an Impression for 24 hours.',
        },
        'criticalSuccess': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.ZDDZz5ynEOR20LUC',
            'message': 'The meal grants a +2 status bonus to Diplomacy checks to Request and Intimidation checks to Coerce for the next 24 hours.',
        },
        'success': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.uw5M3YfEJs23RmX5',
            'message': 'The meal grants a +2 status bonus to Diplomacy checks to Request and Intimidation checks to Coerce for the remainder of the camping session.',
        },
        'criticalFailure': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.yMsjuwLmbF8h1jVt',
            'message': 'The oysters weren’t prepared properly. The eater becomes @UUID[Compendium.pf2e.conditionitems.4D2KBtexWXa6oUMR]{Drained} 1.',
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
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.elsXArG4GqGin6LB',
            'message': 'The meal restores 3d8 Hit Points when it’s eaten and an additional 3d8 Hit Points when the eater wakes',
        },
        'criticalFailure': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.lWy7BZYGab0uNf9H',
            'message': 'The meal is either far too sweet or far too sour. Worse, the unpleasant flavors linger for 8 hours and are distracting enough to cause the eater to become Stupefied 1 for that time.',
        },
        'criticalSuccess': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.n6ykp8NjXDFcvqXM',
            'message': 'The meal restores 6d8 Hit Points when it’s eaten and an additional 6d8 Hit Points when the eater wakes from a night of rest.',
        },
        'favoriteMeal': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.sQz8ugumjOMSHiYt',
            'message': '+1 status bonus to Religion checks for 24 hours.',
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
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.5GT0JeRjYJc8e5ZA',
            'message': 'The meal grants a +2 status bonus to damage rolls from melee Strikes against aberrations, beasts, and dragons for the remainder of the camping session.',
        },
        'criticalFailure': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.af6hFeUUfsF1GiX8',
            'message': 'The meal is unsettling to both the belly and the mind. The eater suffers a –2 status penalty to saving throws against fear effects for the next 24 hours or until they achieve a critical success on such a saving throw, whichever comes first.',
        },
        'criticalSuccess': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.gNGXKMmJdNQJZode',
            'message': 'The meal grants a +2 status bonus to damage rolls from melee Strikes against aberrations, beasts, and dragons for the next 24 hours.',
        },
        'favoriteMeal': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.qad6gIwEpQQIkZwQ',
            'message': '+1 status bonus to Athletics checks for the next 24 hours.',
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
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.uchgkN3hm1T51zfM',
            'message': 'The meal grants the eater resistance to fire 10 for the remainder of the camping session and resistance to fire 5 for the next 24 hours.',
        },
        'success': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.JqKhpMXiQCEsDSCw',
            'message': 'The meal grants the eater resistance to fire 10 for the remainder of the camping session.',
        },
        'criticalFailure': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.q3vnRHRBn3HmRhkV',
            'message': 'The wings and thighs are too hot! The eater is forced to spit them out and gains no nourishment from the meal.  Additionally, they are Sickened 2 until they get a full night’s rest, and they cannot reduce this condition’s value naturally, but it’s a poison effect.',
        },
        'favoriteMeal': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.xF0K40VzyjIbbRMA',
            'message': 'The eater gains the following reaction, which they can use once during the next 24 hours.\nIgnite Magic r (evocation, fire, primal) Frequency once per day; Trigger a creature rolls a critical failure against a single-target spell you cast on it; Effect You cause the magic affecting the target to ignite into flames. The creature takes {2d6}[persistent,fire]Persistent Fire Damage in addition to the normal effects of  critically failing.\n',
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
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.Q1Qg8K9G7R9wU4rI',
            'message': '+1 status bonus to damage rolls with melee Strikes with weapons that require 2 hands to use.',
        },
        'success': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.TLxckltEWbHKCXDi',
            'message': 'The meal grants the eater a +1 status bonus to saving throws for 24 hours.',
        },
        'criticalFailure': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.pTjrRRqxUJnbDRIB',
            'message': 'The meal is disappointing. The eater suffers a –2 status penalty to saving throws against emotion effects for 24 hours or until they critically succeed at such a saving throw, whichever comes first.',
        },
        'criticalSuccess': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.f2yKkelj374SNszb',
            'message': 'The meal grants the eater a +2 status bonus to saving throws for 24 hours',
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
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.0yH2V9UFRyGwXIRn',
            'message': 'The meal seems to take forever to digest. The eater becomes Clumsy 2. This condition is reduced to Clumsy 1 after the eater gets a night’s rest and does their daily preparations, and is removed entirely after 24 hours. This is a poison effect.',
        },
        'favoriteMeal': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.Ln9Q3cH6NzGZAJUs',
            'message': '+1 status bonus to Fortitude saves for the next 24 hours.',
        },
        'success': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.M6jcQsHsPEkigTTG',
            'message': 'The meal restores 4d8 Hit Points and reduces one of the eater’s clumsy, drained, enfeebled, or stupefied conditions by 2 (if the eater suffers from more than one, determine which one is reduced randomly).',
        },
        'criticalSuccess': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.xq4qNyrsL5wVYkcQ',
            'message': 'The meal restores 7d8 Hit Points and reduces the eater’s clumsy, drained, enfeebled, and stupefied conditions by 2.',
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
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.6RjaP1s58ITIVn6u',
            'message': 'The bonus against poison and disease effects increases to +4.',
        },
        'success': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.Zn0CB7bL7vHrMNTU',
            'message': 'The meal grants immunity to diseases and poisons of level 15 or lower. In addition, the meal grants a +3 status bonus to saving throws against poison and disease effects. Any saving throws attempted against cave worm poison rolled twice, using the  higher result as the actual result; this is a fortune effect.',
        },
        'criticalSuccess': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.hLGCjXajAYEcII9J',
            'message': 'The meal grants immunity to diseases and poisons of level 15 or lower. In addition, the meal grants a +3 status bonus to saving throws against poison and disease effects. Any saving throws attempted against cave worm poison or any poison of level 15 or lower are rolled twice, using the  higher result as the actual result; this is a fortune effect.',
        },
        'criticalFailure': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.o6F8QZbTvt9os8Ni',
            'message': 'Instead of protecting from poison, the soup is poison itself. The eater becomes Enfeebled 3; every 24 hours, this enfeebled condition diminishes by 1. This is a poison effect.',
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
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.I2LiwNjrowS5u4UG',
            'message': 'The meal grants a +3 status bonus to saves against curse and poison effects. In addition, the next time the eater is damaged, they gain fast healing 10; this fast healing lasts until the eater dies, is healed to its maximum hit points, or for 1 minute, whichever comes first, after which the fast healing effect ends.',
        },
        'success': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.m3FgRCWbVupkaORL',
            'message': 'The meal grants a +3 status bonus to saves against curse and poison effects.',
        },
        'criticalFailure': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.s1DfJpXFkJorHsuu',
            'message': 'The meal imparts a fragment of the linnorm death curse on the eater, who becomes @UUID[Compendium.pf2e.conditionitems.3uh1r86TzbQvosxv]{Doomed} 1 for 24 hours. This is a curse effect.',
        },
        'favoriteMeal': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.sxr0BsJJGeLyNBgq',
            'message': '+2 status bonus to Perception checks.',
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
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.1XbYyFCnlAZkhHhe',
            'message': 'Increase a random ability score by 2 or to 18, whichever results in the higher score. This effect persists as long as the eater remains in the First World or an associated demiplane, or until they gain an effect from another special meal, whichever comes first.',
        },
        'criticalFailure': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.72N0nZAAo2iuCGBd',
            'message': 'Something in the meal clashes with the eater’s body, mind, and soul. When they roll an initiative check at the start of any combat, attempt a DC 11 Flat Check. On a failure, they become Confused for 1 minute. This effect lasts as long as they remain in the First World (or an associated demiplane) or for 24 hours, whichever comes second. This is a curse effect.',
        },
        'success': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.GesHppaIt6v7WBzT',
            'message': 'Increase a random ability score by 2 or to 18, whichever results in the higher score. This effect persists for 24 hours if the eater remains in the First World or an associated demiplane, or until they gain an effect from another special meal, whichever comes first.',
        },
        'favoriteMeal': {
            'effectUuid': 'Compendium.pf2e-kingmaker-tools.kingmaker-tools-meal-effects.Item.Wfmt1NvfIqF3gFav',
            'message': '+3 status bonus to skill checks for all skills associated with the random ability score that increases.',
        },
    },
];

export function getRecipeData(): RecipeData[] {
    return allRecipes;
}

export const recipeEffectUuids = new Set(allRecipes.flatMap(a => {
    return [a.criticalSuccess?.effectUuid, a.criticalFailure?.effectUuid, a.favoriteMeal?.effectUuid, a.success?.effectUuid]
        .filter(a => a !== undefined) as string[];
}));

export function getRecipesKnownInZone(zoneLevel: number, recipes: RecipeData[]): RecipeData[] {
    return recipes.filter(recipe => recipe.rarity === 'common' && recipe.level <= zoneLevel);
}


