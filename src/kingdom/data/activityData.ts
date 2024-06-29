import {KingdomPhase} from './activities';
import {allSkills, Skill} from './skills';
import {Companion} from './companions';
import {Modifier} from '../modifiers';
import {Commodities, hasFeat, Kingdom} from './kingdom';
import {Ruin} from './ruin';
import {unslugify} from '../../utils';
import {ResourceMode, ResourceTurn, RolledResources} from '../resources';

export type SkillRanks = Partial<Record<Skill, number>>;

function simpleRank(skills: Skill[], rank = 0): SkillRanks {
    return skills
        .map(skill => {
            return {[skill]: rank};
        })
        .reduce((a, b) => Object.assign(a, b), {});
}

export interface ActivityResult {
    msg: string;
    modifiers?: (kingdom: Kingdom) => Modifier[];
}

export interface ActivityResults {
    criticalSuccess?: ActivityResult;
    success?: ActivityResult;
    failure?: ActivityResult;
    criticalFailure?: ActivityResult;
}

export interface ActivityContent extends ActivityResults {
    title: string;
    description: string;
    requirement?: string;
    special?: string;
    skills: SkillRanks;
    phase: KingdomPhase;
    dc: 'control' | 'custom' | 'none' | 'scouting' | number;
    dcAdjustment?: number;
    enabled: boolean;
    companion?: Companion;
    fortune: boolean;
    oncePerRound: boolean;
    hint?: string;
}

export interface KingdomActivity extends ActivityContent {
    id: string;
}

export type KingdomActivityById = Record<string, KingdomActivity>;

const activityData: Record<string, ActivityContent> = {
    'abandon-hex': {
        title: 'Abandon Hex',
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'region',
        dc: 'control',
        description: 'After careful consideration, you decide that you would rather not hold onto a particular hex as part of your claimed territory. You renounce your claim to it and pull back any settlers or explorers. Attempt a basic Exploration or Wilderness check. You can abandon more than one hex at a time, but each additional hex you abandon increases the DC of this check by 1.',
        requirement: 'The hex to be abandoned must be controlled.',
        skills: simpleRank(['exploration', 'wilderness']),
        criticalSuccess: {
            msg: `You abandon the hex or hexes, decreasing your kingdom's Size by 1 per hex abandoned (this affects all statistics determined by Size; see page 532). Settlers and explorers return and resettle elsewhere in your kingdom, bringing with them bits of salvage from the abandoned hexes. ${gainRP(1)} per abandoned hex.`,
        },
        success: {
            msg: `You abandon the hex or hexes, decreasing your kingdom's Size by 1 per hex abandoned (this affects all statistics determined by Size; see page 532). Settlers and explorers return and resettle elsewhere in your kingdom, bringing with them bits of salvage from the abandoned hexes. ${gainUnrest(1)}`,
        },
        failure: {
            msg: `You abandon the hex or hexes, decreasing your kingdom's Size by 1 per hex abandoned (this affects all statistics determined by Size; see page 532). Some citizens become disgruntled refugees who refuse to leave the hex. ${gainUnrest(2)} and then attempt a @Check[type:flat|dc:6]. If you fail, the refugees become bandits, and during your next Event phase, your kingdom experiences a Squatters kingdom event automatically in addition to any other event that might occur.`,
        },
        criticalFailure: {
            msg: `You abandon the hex or hexes, decreasing your kingdom's Size by 1 per hex abandoned (this affects all statistics determined by Size; see page 532). Some citizens become disgruntled refugees who refuse to leave the hex. ${gainUnrest(3)} and automatically experience a Bandit Activity kingdom event.`,
        },
        special: 'The Unrest gained from abandoning a hex doubles if it includes a settlement. A settlement in an abandoned hex becomes a Freehold (page 536).',
    },
    'build-roads': {
        title: 'Build Roads',
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'region',
        dc: 'control',
        description: `<p>You order your kingdom’s engineers to construct a network of robust roads through the hex. Travel along roads uses a terrain type one step better than the surrounding terrain; for example, roads through forest hexes—normally difficult terrain—allow travel as if it were open terrain.</p>
<p>Spend RP as determined by the hex’s most inhospitable terrain (if the hex includes any rivers that cross the hex from one hex side to any other, you must spend double the normal RP cost to also build bridges; this adds the Bridge structure to that hex):</p>
<ul>
<li><b>Mountains</b>: ${loseRP(12)}</li>
<li><b>Swamps</b>: ${loseRP(8)}</li>
<li><b>Forests</b>: ${loseRP(4)}</li>
<li><b>Hills</b>: ${loseRP(2)}</li>
<li><b>Plains</b>: ${loseRP(1)}</li>
</ul>
<p>Then attempt a basic check. Work with the GM to determine where your roads appear on the map.</p>`,
        requirement: 'The hex in which you seek to build roads must be claimed by your kingdom.',
        skills: simpleRank(['engineering']),
        criticalSuccess: {
            msg: 'You build roads into the target hex and, if possible, one adjacent claimed hex that doesn’t yet have roads and whose terrain features are at least as hospitable as those of the target hex',
        },
        success: {
            msg: 'You build roads in the hex.',
        },
        failure: {
            msg: 'You fail to build roads in the hex.',
        },
        criticalFailure: {
            msg: 'Your attempt to build roads ends in disaster. Not only do you fail to build roads, but you lose several workers to an accident, banditry, a vicious monster, or some other unforeseen occurrence. ' + gainUnrest(1),
        },
    },
    'build-structure': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'civic',
        dc: 'custom',
        title: 'Build Structure',
        description: `You attempt to build a structure in the settlement that’s granting the Civic activity. You may choose any structure for which you meet the requirements. Select the appropriate number of contiguous buildable lots in a single block as specified by the structure’s entry and spend the specified RP and Commodity cost. Then attempt the structure’s skill check.

You can also use this activity to attempt to repair a structure that was damaged as the result of an event but hasn’t been replaced by Rubble. To do this, first spend half the structure’s listed RP and Commodity cost, and then attempt the specified check. The existing structure gives you a +2 item bonus to the check.

On a success, record the new construction on the Urban Grid. Unless the structure’s entry states otherwise, its effects are immediate; if the structure adjusts a Ruin’s point total, adjust it upon construction.
`,
        skills: simpleRank([...allSkills]),
        criticalSuccess: {
            msg: 'You construct or repair the structure with great efficiency and get back half of the Commodities spent in construction or repair.',
        },
        success: {
            msg: 'You construct or repair the structure.',
        },
        failure: {
            msg: 'You fail to construct or repair the structure. You can try to complete it next Kingdom turn; if you do so, you do not need to re-pay the RP and Commodity cost.',
        },
        criticalFailure: {
            msg: 'You fail to construct the structure; if you were attempting to repair a damaged structure, it is reduced to Rubble. In either event, Rubble now fills the structure’s lots, which must be cleared with the Demolish activity before you can attempt to Build a Structure in them again.',
        },
    },
    'capital-investment': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'leadership',
        dc: 'control',
        title: 'Capital Investment',
        description: `You contribute funds from your personal wealth for the good of the kingdom, including coinage, gems, jewelry, weapons and armor salvaged from enemies, magical or alchemical items, and so on. Your contribution generates economic activity in the form of RP that can be used during your current Kingdom turn or on the next Kingdom turn (your choice).

You can use Capital Investment to repay funds from Tap Treasury (page 528). In this case, no roll is needed and you simply deduct the appropriate amount of funds from your personal wealth to pay back that which was borrowed. When you use Capital Investment to generate RP, the amount of gp required to make an investment is set by your kingdom’s level. Investments below this amount cause your attempt at Capital Investment to suffer an automatic critical failure, while investments above this amount are lost. The investment required is equal to the value listed on Table 10–9: Party Treasure by Level @UUID[Compendium.pf2e.journals.S55aqwWIzpQRFhcq.JournalEntryPage.Ly8l2GT6dbvuY3A2]{Treasure}; use the value for your kingdom’s level under the “Currency per Additional PC” as the required investment value. This is a basic check.`,
        requirement: 'You must be within the influence of a settlement that contains at least one Bank.',
        skills: simpleRank(['trade']),
        criticalSuccess: {
            msg: `Success Your kingdom reaps the benefits of your investment. ${gainRolledRD(4)}`,
        },
        success: {
            msg: `Your investment helps the economy. ${gainRolledRD(2)}`,
        },
        failure: {
            msg: `Your investment ends up being used to shore up shortfalls elsewhere. ${gainRP('1d4')}`,
        },
        criticalFailure: {
            // TODO: increase crime by an equal amount
            msg: `Your investment is embezzled, lost, or otherwise misappropriated. Choose one of the following: either ${gainRolledRD(1)} and also increase your Crime by an equal amount, or gain 0 RP and ${gainRuin('crime', 1)}`,
        },
    },
    'celebrate-holiday': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'leadership',
        dc: 'control',
        title: 'Celebrate Holiday',
        description: 'You declare a day of celebration. Holidays may be religious, historical, martial, or simply festive, but all relieve your citizens from their labors and give them a chance to make merry at the kingdom’s expense. Attempt a basic check, but if your kingdom Celebrated a Holiday the previous turn, the DC increases by 4, as your kingdom hasn’t had a chance to recover from the previous gala.',
        skills: simpleRank(['folklore']),
        criticalSuccess: {
            msg: 'Your holidays are a delight to your people. The event is expensive, but incidental income from the celebrants covers the cost. You gain a +2 circumstance bonus to Loyalty-based checks until the end of your next Kingdom turn.',
            modifiers: () => [{
                turns: 2,
                enabled: true,
                abilities: ['loyalty'],
                value: 2,
                name: 'Celebrate Holiday: Critical Success',
                type: 'circumstance',
            }],
        },
        success: {
            msg: `Your holidays are a success, but they’re also expensive. You gain a +1 circumstance bonus to Loyalty-based checks until the end of your next Kingdom turn. ${loseRolledRD(1)}. If you can’t afford this cost, treat this result as a Critical Failure instead.`,
            modifiers: () => [{
                turns: 2,
                enabled: true,
                abilities: ['loyalty'],
                value: 1,
                name: 'Celebrate Holiday: Success',
                type: 'circumstance',
            }],
        },
        failure: {
            msg: `The holiday passes with little enthusiasm, but is still expensive. ${loseRolledRD(1)}. If you can’t afford this cost, treat this result as a Critical Failure instead.`,
        },
        criticalFailure: {
            msg: `Your festival days are poorly organized, and the citizens actively mock your failed attempt to celebrate. ${createResourceButton({
                turn: 'next',
                value: '4',
                mode: 'lose',
                type: 'resource-dice',
            })}. The failure also causes you to take a –1 circumstance penalty to Loyalty-based checks until the end of the next Kingdom turn.`,
            modifiers: () => [{
                turns: 2,
                enabled: true,
                abilities: ['loyalty'],
                value: -1,
                name: 'Celebrate Holiday: Critical Failure',
                type: 'circumstance',
            }],
        },
    },
    'claim-hex': {
        title: 'Claim Hex',
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'region',
        dc: 'control',
        requirement: 'You have Reconnoitered the hex to be claimed during hexploration. This hex must be adjacent to at least one hex that’s already part of your kingdom. If the hex to be claimed contains dangerous hazards or monsters, they must first be cleared out—either via standard adventuring or the Clear Hex activity.',
        description: `Your surveyors fully explore the hex and attempt to add it into your kingdom’s domain. ${loseRP(1)} and then attempt a basic Exploration, Intrigue, Magic, or Wilderness check.`,
        skills: simpleRank(['exploration', 'intrigue', 'magic', 'wilderness']),
        criticalSuccess: {
            msg: 'You claim the hex and immediately add it to your territory, increasing your kingdom\'s Size by 1 (this affects all statistics determined by Size; see page 532). Your occupation of the hex goes so smoothly that you can immediately attempt another Region activity.',
        },
        success: {
            msg: 'You claim the hex and add it to your territory, increasing your kingdom\'s Size by 1 (this affects all statistics determined by Size; see page 532).',
        },
        failure: {
            msg: 'You fail to claim the hex.',
        },
        criticalFailure: {
            msg: 'You fail to claim the hex, and a number of early settlers and explorers are lost, causing you to take a –1 circumstance penalty to Stability-based checks until the end of your next Kingdom turn.',
            modifiers: () => [{
                turns: 2,
                enabled: true,
                abilities: ['stability'],
                value: -1,
                name: 'Claim Hex: Critical Failure',
                type: 'circumstance',
            }],
        },
        special: 'At 1st level, when selecting the three activities you take during the Region Activities step of the Activity phase of the Kingdom turn, you may select this activity no more than once. Once your kingdom reaches 4th level, you may select it up to twice per turn, and after reaching 9th level you may select it up to three times per turn. When you successfully claim a hex, gain kingdom XP (see page 540). Many hexes have terrain features that grant benefits to your kingdom when claimed; see Terrain Features on page 535.',
    },
    'clandestine-business': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'leadership',
        dc: 'control',
        title: 'Clandestine Business',
        description: 'You know there are criminals in your kingdom, and they know you know. You encourage them to send kickbacks in the form of resources and Commodities to the government, but the common citizens will be more than upset if they find out! This starts as a basic check against your Control DC, but every subsequent Kingdom turn you pursue Clandestine Business, the DC increases by 2. Every Kingdom turn that passes without Clandestine Business reduces the DC by 1 (until you reach your Control DC).',
        skills: simpleRank(['intrigue'], 1),
        criticalSuccess: {
            msg: `${gainRolledRD(2)}. In addition, you ${gainCommodities('luxuries', '1d4')}. The public is none the wiser.`,
        },
        success: {
            msg: `Either ${gainRolledRD(2)}, or gain ${gainCommodities('luxuries', '1d4')}. Regardless of your choice, rumors spread about where the government is getting these “gifts.” ${gainUnrest(1)}.`,
        },
        failure: {
            msg: `${gainRolledRD(1)}. Rumors are backed up with eyewitness accounts. ${gainUnrest(1)} and ${gainRuin('corruption', 1)}.`,
        },
        criticalFailure: {
            msg: `You gain nothing from the Clandestine Business but angry citizens. ${gainUnrest('1d6')}, ${gainRuin('corruption', 2)}, and one other Ruin of your choice by 1.`,
        },
    },
    'clear-hex': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        title: 'Clear Hex',
        phase: 'region',
        dc: 'custom',
        description: `<p>Engineers and mercenaries attempt to prepare a hex to serve as the site for a settlement, or they work to remove an existing improvement, a dangerous hazard, or an encounter.</p>
<p>If you’re trying to prepare a hex for a settlement or demolish an improvement you previously built (or that was already present in the hex), spend RP as determined by the hex’s most inhospitable terrain feature:</p>
<ul>
<li><b>Mountains</b>: ${loseRP(12)}</li>
<li><b>Swamps</b>: ${loseRP(8)}</li>
<li><b>Forests</b>: ${loseRP(4)}</li>
<li><b>Hills</b>: ${loseRP(2)}</li>
<li><b>Plains</b>: ${loseRP(1)}</li>
</ul>
<p>Then attempt a basic Engineering check. If you’re trying to remove a hazard or encounter, instead attempt an Exploration check. The DC of this check is set by the highest level creature or hazard in the hex (as set by Table 10–5: DCs by Level, on page 503 of the Pathfinder Core Rulebook).</p>

<p>If the hex you’re attempting to Clear has existing Ruins or an existing Structure, your action doesn’t physically remove the buildings from the area and you can later incorporate these buildings (or repair ruined ones) into a Settlement you build here later (see page 542). Regardless of the skill used, increase the basic DC by 2 if the hex to be cleared is not yet part of your kingdom.</p>`,
        skills: simpleRank(['engineering', 'exploration']),
        criticalSuccess: {
            msg: `You successfully clear the hex. If you spent RP to attempt this activity, you’re refunded half of the RP cost. If you were removing dangerous creatures (but not hazards) from the hex, your explorers and mercenaries recover 2 Luxury Commodities as treasure. ${gainCommodities('luxuries', 2)}`,
        },
        success: {
            msg: 'You successfully clear the hex.',
        },
        failure: {
            msg: 'You fail to clear the hex.',
        },
        criticalFailure: {
            msg: `You catastrophically fail to clear the hex and several workers lose their lives. ${gainUnrest(1)}`,
        },
    },
    'collect-taxes': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'commerce',
        dc: 'control',
        title: 'Collect Taxes',
        description: 'Tax collectors travel through the lands to collect funds for the betterment of the kingdom. Attempt a basic check.',
        skills: simpleRank(['trade'], 1),
        criticalSuccess: {
            msg: 'Your tax collectors are wildly successful! For the remainder of the Kingdom turn, gain a +2 circumstance bonus to Economy-based checks.',
            modifiers: () => [{
                turns: 1,
                enabled: true,
                abilities: ['economy'],
                value: 2,
                name: 'Collect Taxes: Critical Success',
                type: 'circumstance',
            }],
        },
        success: {
            msg: `Your tax collectors gather enough to grant you a +1 circumstance bonus to Economy-based checks for the remainder of the Kingdom turn. If you attempted to Collect Taxes during the previous turn, ${gainUnrest(1)}`,
            modifiers: () => [{
                turns: 1,
                enabled: true,
                abilities: ['economy'],
                value: 1,
                name: 'Collect Taxes: Success',
                type: 'circumstance',
            }],
        },
        failure: {
            msg: `Your tax collectors gather enough to grant you a +1 circumstance bonus to Economy-based checks for the remainder of the Kingdom turn. In addition, people are unhappy about taxes—${gainUnrest(1)} (or ${gainUnrest(2)} if you attempted to Collect Taxes the previous turn).`,
            modifiers: () => [{
                turns: 1,
                enabled: true,
                abilities: ['economy'],
                value: 1,
                name: 'Collect Taxes: Failure',
                type: 'circumstance',
            }],
        },
        criticalFailure: {
            msg: `Your tax collectors encounter resistance from the citizens and their attempts to gather taxes are rebuffed. While the tax collectors still manage to gather enough taxes to support essential government, they have angered the kingdom's citizens and encouraged rebellious acts. ${gainUnrest(2)}, and choose one Ruin to increase by 1.`,
        },
    },
    'craft-luxuries': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'leadership',
        dc: 'control',
        title: 'Craft Luxuries',
        description: `You encourage your artisans to craft luxury goods and may even aid them in this pursuit. ${loseRolledRD(1)}. Then attempt a basic check.`,
        skills: simpleRank(['arts']),
        criticalSuccess: {
            msg: `Your artisans exceed expectations and craft extravagant goods. ${gainCommodities('luxuries', '1d4')}`,
        },
        success: {
            msg: `Your artisans produce some delightful goods. ${gainCommodities('luxuries', 1)}`,
        },
        failure: {
            msg: 'Your artisans fail to produce anything noteworthy.',
        },
        criticalFailure: {
            // TODO: increase ruins by 1
            msg: 'Your artisans not only fail to produce anything noteworthy, but some took advantage of the opportunity to push their own agendas or earn more for themselves by selling to underground markets. Increase one of your Ruins by 1.',
        },
    },
    'create-a-masterpiece': {
        oncePerRound: true,
        fortune: false,
        enabled: true,
        phase: 'leadership',
        dc: 'control',
        title: 'Create a Masterpiece',
        description: 'You encourage your kingdom’s artists to create and display a masterful work of art to bolster your kingdom’s reputation. Attempt a basic check; the result affects either Fame or Infamy (depending on the type of kingdom you’re running). Create a Masterpiece may be attempted only once per Kingdom turn regardless of the number of leaders pursuing activities.',
        skills: simpleRank(['arts'], 1),
        criticalSuccess: {
            msg: `${gainFame(1)}, and ${createResourceButton({
                turn: 'next',
                type: 'fame',
                value: '1',
            })}. ${gainRolledRD(2)}`,
        },
        success: {
            msg: gainFame(1),
        },
        failure: {
            msg: 'Your attempt to create a masterpiece fails.',
        },
        criticalFailure: {
            msg: `Not only does your attempt to create a masterpiece fail, it does so in a dramatic and humiliating way. ${loseFame(1)}; if you have no Fame or Infamy points to lose, instead ${gainUnrest('1d4')}`,
        },
    },
    'creative-solution': {
        enabled: true,
        oncePerRound: false,
        fortune: true,
        phase: 'leadership',
        dc: 'control',
        title: 'Creative Solution',
        description: 'You work with your kingdom’s scholars, thinkers, and practitioners of magical and mundane experimentation to come up with new ways to resolve issues when business as usual is just not working. Attempt a basic check.',
        skills: simpleRank(['scholarship']),
        criticalSuccess: {
            msg: `You can call upon the solution to aid in resolving any Kingdom skill check made during the remainder of this Kingdom turn ${gainSolution('creative-solution')}. Do so when a Kingdom skill check is rolled, but before you learn the result. Immediately reroll that check with a +2 circumstance bonus; you must take the new result. If you don’t use your Creative Solution by the end of this turn, you lose this benefit and gain 10XP instead.`,
            modifiers: () => [{
                turns: 1,
                enabled: false,
                value: 2,
                name: 'Creative Solution: Critical Success',
                type: 'circumstance',
                consumeId: '',
            }],
        },
        success: {
            msg: `You can call upon the solution to aid in resolving any Kingdom skill check made during the remainder of this Kingdom turn ${gainSolution('creative-solution')}. Do so when a Kingdom skill check is rolled, but before you learn the result. Immediately reroll that check with a +2 circumstance bonus; you must take the new result. If you don’t use your Creative Solution by the end of this turn, you lose this benefit and gain 10XP instead. In addition, ${loseRP('1d4')} to research the solution. This cost is paid now, whether or not you use your Creative Solution.`,
            modifiers: () => [{
                turns: 1,
                enabled: false,
                value: 2,
                name: 'Creative Solution: Success',
                type: 'circumstance',
                consumeId: '',
            }],
        },
        failure: {
            msg: `Your attempt at researching is a failure and you ${loseRP('2d6')}. It provides no advantage.`,
        },
        criticalFailure: {
            msg: `Your attempt at researching is a failure and you ${loseRP('2d6')}. It provides no advantage. In addition, your scholars and thinkers are so frustrated that you take a –1 circumstance penalty to Culture-based checks until the end of the next Kingdom turn.`,
            modifiers: () => [{
                turns: 2,
                enabled: true,
                value: -1,
                name: 'Creative Solution: Critical Failure',
                type: 'circumstance',
                abilities: ['culture'],
            }],
        },
        special: 'You cannot influence a check with Supernatural Solution and Creative Solution simultaneously.',
    },
    'decadent-feasts': {
        companion: 'Jaethal',
        oncePerRound: false,
        fortune: false,
        enabled: false,
        phase: 'leadership',
        dc: 'control',
        title: 'Decadent Feasts',
        description: `Urgathoa is more than just the goddess of undeath— she’s also the goddess of gluttony. And Jaethal is no fool; she understands the fear her goddess inspires in the living and knows that focusing on other aspects of her worship are more likely to result in positive growth for the nation. In order to distract the populace, she arranges for decadent feasts for the people, simultaneously feeding the hungry while camouflaging some of her faith’s more sinister aspects. ${loseCommodities('food', '1d8')} and ${loseCommodities('luxuries', 1)}, then attempt a basic Agriculture or Trade check to determine how effective the feasts are.`,
        skills: simpleRank(['agriculture']),
        criticalSuccess: {
            msg: `The people rejoice and glut themselves on the repast! ${loseUnrest('1d6')}, and the next time this Kingdom turn you suffer an effect that increases Unrest, do not increase your Unrest.`,
        },
        success: {
            msg: `The people enjoy the meal, but no longer than it takes to gulp it down. ${loseUnrest('1d3')}`,
        },
        failure: {
            msg: `The meal is appreciated, but the people remain suspicious. ${loseUnrest(1)}.`,
        },
        criticalFailure: {
            msg: `The people are too suspicious of the feast to enjoy it. Worse, rumors that Jaethal is trying to distract the citizens from an evil ritual spread. ${gainUnrest('1d4')} and ${gainRuin('corruption', 1)}`,
        },
    },
    'deliberate-planning': {
        companion: 'Kalikke',
        oncePerRound: false,
        fortune: true,
        enabled: false,
        phase: 'leadership',
        dc: 'custom',
        title: 'Deliberate Planning',
        description: 'Kalikke takes time and weighs all options when faced with decisions, regardless of their importance. While this can sometimes lead her to taking too long to make choices, her theoretical analysis can be quite helpful in navigating continuous events. Choose a single continuous event that will affect your kingdom on this turn’s Event Phase, then attempt a Scholarship check against that event’s DC.',
        skills: simpleRank([...allSkills]),
        criticalSuccess: {
            msg: 'Kalikke’s aid has been monumentally helpful. When you roll to resolve the continuous event you chose, you can roll twice and choose which result to apply. You gain a +1 circumstance bonus to each roll. This is a Fortune effect.',
            modifiers: () => [{
                turns: 1,
                enabled: false,
                value: 1,
                name: 'Deliberate Planning: Critical Success',
                type: 'circumstance',
                phases: ['event'],
            }],
        },
        success: {
            msg: 'Kalikke’s suggestions are useful, granting you a +1 circumstance bonus to rolls to resolve the chosen continuous event.',
            modifiers: () => [{
                turns: 1,
                enabled: false,
                value: 1,
                name: 'Deliberate Planning: Success',
                type: 'circumstance',
                phases: ['event'],
            }],
        },
        failure: {
            msg: 'Kalikke’s advice isn’t helpful, but neither does it hinder your ability to handle the event.',
        },
        criticalFailure: {
            msg: 'You got too caught up in Kalikke’s theoretical analysis and spent too much time preparing. When you roll to resolve the continuous event you tried to plan for, roll twice and take the worse result.',
        },
    },
    'demolish': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'civic',
        dc: 'control',
        title: 'Demolish',
        description: 'Choose a single occupied lot in one of your settlements and attempt a basic check to reduce it to Rubble and then clear the Rubble away to make ready for a new structure. For multiple-lot structures, you’ll need to perform multiple Demolish activities (or critically succeed at the activity) to fully clear all of the lots. As soon as you begin Demolishing a multiple-lot structure, all of the lots occupied by that structure no longer function.',
        skills: simpleRank(['engineering']),
        criticalSuccess: {
            msg: 'Choose one of the following effects: you demolish an entire multiple-lot structure all at once and clear all of the lots it occupied, or you recover [[/r 1d6#Commodities]] Commodities (chosen from lumber, stone, and ore) from the Rubble of a single-lot demolition.',
        },
        success: {
            msg: 'You demolish the lot successfully.',
        },
        failure: {
            msg: 'You fail to demolish the lot. It remains in Rubble and cannot be used for further construction until you successfully Demolish it.',
        },
        criticalFailure: {
            msg: `You fail to demolish the lot. It remains in Rubble and cannot be used for further construction until you successfully Demolish it. In addition, accidents during the demolition cost you the lives of some of your workers. ${gainUnrest(1)}`,
        },
    },
    'deploy-army': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'army',
        dc: 'control',
        title: 'Deploy Army',
        description: `The army moves through your kingdom or beyond. Since this travel occurs over the course of the entire month that preceded the Kingdom turn, the ground an army covers when it deploys can be quite extensive. You can Deploy an Army with an Exploration, Boating, or Magic check.

When you use an Exploration check, choose a location within 20 hexes of the army’s current hex. If the army’s starting point and ending point are connected by a road, increase the result one degree of success. Count roadless hexes that contain swamps or mountains, or each hex where you must cross a river or lake without the aid of a bridge, as two hexes. You can issue orders to force march. Doing so grants a +4 circumstance bonus on the check, but causes the army to increase its weary condition by 1 (or by 2, if you fail the check).

When you use a Boating check, the army’s starting point and ending point must be connected by a body of water; choose any location within 20 hexes along this route.

You must be at least master in Magic to attempt a Magic check. When you do so, choose any location within 30 hexes of the army’s current hex, then roll your check. If the army’s deployment causes it to cross your kingdom’s border, the DC increases by 5. If the army’s deployment causes it to cross an enemy kingdom’s border, the DC instead increases by 10.`,
        skills: {
            warfare: 0,
            boating: 0,
            magic: 3,
        },
        criticalSuccess: {
            msg: 'The army arrives much more quickly than you anticipated; it arrives at its destination and then becomes efficient.',
        },
        success: {
            msg: 'The army arrives at its destination. ',
        },
        failure: {
            msg: 'The army arrives at its destination, but ran into some sort of trouble along the way. Increase the army’s weary condition by 1 and attempt a @Check[type:flat|dc:6]; on a failure, reduce the army’s HP by 1.',
        },
        criticalFailure: {
            msg: `Rather than arriving at its destination, the army becomes lost until it recovers from this condition. ${gainUnrest('1d4')}, and attempt a @Check[type:flat|dc:11]; on a failure, reduce the army’s HP by 1.`,
        },
    },
    'disband-army': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'army',
        dc: 'none',
        title: 'Disband Army',
        description: 'You can choose to disband an army with no check needed. If the army consisted of conscripts from your kingdom, the soldiers revert to being citizens. If the army was recruited from creatures encountered in the wilds, they return to their homes. A disbanded army no longer contributes to your kingdom’s Consumption.',
        skills: simpleRank([...allSkills]),
    },
    'establish-farmland': {
        title: 'Establish Farmland',
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'region',
        dc: 'control',
        description: `You plant crops and establish livestock in permanent farms, ranches, and other growing operations to create Farmland (page 535). If you’re attempting to Establish Farmland in a hex that is predominantly plains, you must ${loseRP(1)} and the check is against your Control DC. If you’re targeting a hex that is predominantly hills, you must spend ${loseRP(2)} and the check is against your Control DC + 5.`,
        requirement: 'Plains or hills are the predominant terrain feature in the hex; the hex is in the influence of one of your settlements.',
        skills: simpleRank(['agriculture']),
        criticalSuccess: {
            msg: 'You establish two adjacent Farmland hexes instead of one. If your target hex was a hills hex, the additional hex may be a hills hex or a plains hex; otherwise, the additional hex must be a plains hex. If no appropriate hex is available, treat this result as a regular success instead.',
        },
        success: {
            msg: 'You establish one Farmland hex.',
        },
        failure: {
            msg: 'You fail to establish a Farmland hex.',
        },
        criticalFailure: {
            msg: 'You fail to establish a Farmland hex, and your attempt potentially causes the spread of a blight. At the start of each of the next two Event phases, attempt a @Check[type:flat|dc:6|showDC:all]; on a failure, your kingdom experiences a Crop Failure event in this and all adjacent hexes.',
        },
    },
    'establish-settlement': {
        title: 'Establish Settlement',
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'region',
        dc: 'control',
        description: 'You draw up plans, gather resources, entice citizens, and establish boundaries to found a brand new settlement in the hex. Attempt a basic Engineering, Industry, Politics, or Scholarship check. If you cannot pay the RP required by the result of this check, treat your result as a critical failure. A settlement always starts as a village. See page 540 for further details about building settlements.',
        requirement: 'The hex in which you’re establishing the settlement has been Cleared and doesn’t currently have a settlement (including a Freehold) in it.',
        skills: simpleRank(['engineering', 'industry', 'politics', 'scholarship']),
        criticalSuccess: {
            msg: `You establish the settlement largely with the aid of enthusiastic volunteers. ${loseRP('1d6')}`,
        },
        success: {
            msg: `You establish the settlement. ${loseRP('3d6')}`,
        },
        failure: {
            msg: `You establish the settlement, but inefficiently and at great expense. ${loseRP('6d6')}`,
        },
        criticalFailure: {
            msg: 'You fail to establish the settlement.',
        },
    },
    'establish-trade-agreement': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'leadership',
        dc: 'custom',
        title: 'Establish Trade Agreement',
        description: `You send a band of merchants out to establish a trade agreement between your kingdom and a group with whom you’ve established diplomatic relations. If a navigable river connects your kingdom with the other group’s territory, you can attempt a Boating check to Establish the Trade Agreement. If your kingdom’s proficiency rank in Magic is Master or higher, you can attempt a Magic check. Otherwise, attempt a Trade check.

The check’s DC is either the group’s Negotiation DC (see sidebar) or your kingdom’s Control DC, whichever is higher.`,
        requirement: 'You have diplomatic relations with the group you wish to establish an agreement with.',
        skills: {
            boating: 0,
            trade: 0,
            magic: 3,
        },
        criticalSuccess: {
            msg: `You successfully establish a trade agreement with your target, and your merchants return with gifts! ${gainRolledRD(2)}`,
        },
        success: {
            msg: 'You successfully establish a trade agreement.',
        },
        failure: {
            msg: `Your traders reach their destination but need to sweeten the deal to secure the trade agreement. ${loseRolledRD(2)}. If you do so, you successfully establish a trade agreement, otherwise the attempt fails.`,
        },
        criticalFailure: {
            msg: `Your trade agreement is a total loss and your traders do not return. ${gainUnrest(1)}, and until the end of the next Kingdom turn, take a –1 circumstance penalty to all Economy-related checks.`,
            modifiers: () => [{
                turns: 2,
                enabled: false,
                value: -1,
                name: 'Establish Trade Agreement: Critical Failure',
                type: 'circumstance',
                abilities: ['economy'],
            }],
        },
    },
    'establish-work-site-lumber': {
        title: 'Establish Work Site Lumber',
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'region',
        dc: 'control',
        description: `<p>Your hire a crew of workers to travel to a hex that contains Lumber, Ore, or Stone to be harvested. Spend RP as determined by the hex’s most inhospitable terrain:</p>
<ul>
<li><b>Mountains</b>: ${loseRP(12)}</li>
<li><b>Swamps</b>: ${loseRP(8)}</li>
<li><b>Forests</b>: ${loseRP(4)}</li>
<li><b>Hills</b>: ${loseRP(2)}</li>
<li><b>Plains</b>: ${loseRP(1)}</li>
</ul>
<p>Then attempt a basic check. Lumber camps can be established in any hex that contains a significant amount of forest terrain. Mines and quarries can be established in any hex that contains a significant amount of hill or mountain terrain.</p>`,
        skills: simpleRank(['engineering']),
        criticalSuccess: {
            msg: 'You establish a Work Site in the hex and proceed to discover an unexpectedly rich supply of high quality Commodities. All Commodity yields granted by this site are doubled until the end of the next Kingdom turn.',
        },
        success: {
            msg: 'You establish a Work Site in the hex.',
        },
        failure: {
            msg:
                'You fail to establish a Work Site in the hex.',
        },
        criticalFailure: {
            msg: `Not only do you fail to establish a Work Site, but you lose several workers to an accident, banditry, a vicious monster, or some other unforeseen occurrence. ${gainUnrest(1)}`,
        },
    },
    'establish-work-site-mine': {
        title: 'Establish Work Site Mine',
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'region',
        dc: 'control',
        description: `<p>Your hire a crew of workers to travel to a hex that contains Lumber, Ore, or Stone to be harvested. Spend RP as determined by the hex’s most inhospitable terrain:</p>
<ul>
<li><b>Mountains</b>: ${loseRP(12)}</li>
<li><b>Swamps</b>: ${loseRP(8)}</li>
<li><b>Forests</b>: ${loseRP(4)}</li>
<li><b>Hills</b>: ${loseRP(2)}</li>
<li><b>Plains</b>: ${loseRP(1)}</li>
</ul>
<p>Then attempt a basic check. Lumber camps can be established in any hex that contains a significant amount of forest terrain. Mines and quarries can be established in any hex that contains a significant amount of hill or mountain terrain.</p>`,
        skills: simpleRank(['engineering']),
        criticalSuccess: {
            msg: 'You establish a Work Site in the hex and proceed to discover an unexpectedly rich supply of high quality Commodities. All Commodity yields granted by this site are doubled until the end of the next Kingdom turn.',
        },
        success: {
            msg: 'You establish a Work Site in the hex.',
        },
        failure: {
            msg: 'You fail to establish a Work Site in the hex.',
        },
        criticalFailure: {
            msg: `Not only do you fail to establish a Work Site, but you lose several workers to an accident, banditry, a vicious monster, or some other unforeseen occurrence. ${gainUnrest(1)}`,
        },
    },
    'establish-work-site-quarry': {
        title: 'Establish Work Site Quarry',
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'region',
        dc: 'control',
        description: `<p>Your hire a crew of workers to travel to a hex that contains Lumber, Ore, or Stone to be harvested. Spend RP as determined by the hex’s most inhospitable terrain:</p>
<ul>
<li><b>Mountains</b>: ${loseRP(12)}</li>
<li><b>Swamps</b>: ${loseRP(8)}</li>
<li><b>Forests</b>: ${loseRP(4)}</li>
<li><b>Hills</b>: ${loseRP(2)}</li>
<li><b>Plains</b>: ${loseRP(1)}</li>
</ul>
<p>Then attempt a basic check. Lumber camps can be established in any hex that contains a significant amount of forest terrain. Mines and quarries can be established in any hex that contains a significant amount of hill or mountain terrain.</p>`,
        skills: simpleRank(['engineering']),
        criticalSuccess: {
            msg: 'You establish a Work Site in the hex and proceed to discover an unexpectedly rich supply of high quality Commodities. All Commodity yields granted by this site are doubled until the end of the next Kingdom turn.',
        },
        success: {
            msg: 'You establish a Work Site in the hex.',
        },
        failure: {
            msg: 'You fail to establish a Work Site in the hex.',
        },
        criticalFailure: {
            msg: `Not only do you fail to establish a Work Site, but you lose several workers to an accident, banditry, a vicious monster, or some other unforeseen occurrence. ${gainUnrest(1)}`,
        },
    },
    'evangelize-the-dead': {
        companion: 'Harrim',
        oncePerRound: false,
        fortune: false,
        enabled: false,
        phase: 'leadership',
        dc: 'control',
        title: 'Evangelize the Dead',
        description: 'Harrim spends time preaching the End Times. While his sermons certainly aren’t for everyone, his methods avoid deliberately antagonizing those who see hope in the world while simultaneously providing ease and calm to the more desperate among the kingdom’s citizens. Attempt a basic Folklore check to determine how effective his sermons are.',
        skills: simpleRank(['folklore']),
        criticalSuccess: {
            msg: `Harrim’s prayers soothe and calm the more criminal-minded citizens for the time being. ${loseUnrest('1d4')}, and either ${loseRuin('crime', 2)} or ${loseRuin('corruption', 1)} or ${loseRuin('strife', 1)}.`,
        },
        success: {
            msg: `Harrim’s prayers serve to redirect and calm discord. ${loseUnrest('1d3')}`,
        },
        failure: {
            msg: `Harrim’s prayers serve to redirect and calm discord. ${loseUnrest('1')}`,
        },
        criticalFailure: {
            msg: `Harrim’s prayers have unsettled some of your citizens. ${gainUnrest('1d4')} and ${gainRuin('decay', 1)} `,
        },
    },
    'false-victory': {
        companion: 'Kanerah',
        oncePerRound: false,
        fortune: false,
        enabled: false,
        phase: 'leadership',
        dc: 'control',
        title: 'False Victory',
        description: 'Kanerah’s contacts with the criminal underworld and her knack for dodging punishment and claiming responsibility for victories she had no direct role in can be harnessed to engineer false victories to trick the kingdom’s citizens into thinking their leaders are doing more than they actually are to create a safe place to live. Such attempts are not without risks, though, for if things backfire, you can cause problems where none existed in the first place. When setting up a false victory, attempt a basic Intrigue check.',
        skills: simpleRank(['intrigue']),
        criticalSuccess: {
            // TODO: ruin of your choice
            msg: `At the end of this Kingdom turn’s Event Phase, roll again on the random kingdom events table. Rumors of this event being resolved spread throughout your kingdom. You don’t gain any of the benefits of resolving this false victory, but instead ${loseUnrest('1d6')} and one Ruin of your choice by 1. If you randomly roll that same random kingdom event at any time during the next four kingdom turns, you can attempt an Intrigue check with a +1 circumstance bonus to resolve it rather than the normal check to resolve it.`,
            modifiers: () => [{
                turns: 5,
                enabled: false,
                value: 1,
                name: 'False Victory: Critical Success',
                type: 'circumstance',
                phases: ['event'],
                skills: ['intrigue'],
            }],
        },
        success: {
            msg: `Vague rumors of the kingdom’s leaders attaining victories over vague threats spread through the kingdom. ${loseUnrest('1d3')}`,
        },
        failure: {
            msg: `The false event fails to manifest, and rumors of the truth spread throughout the kingdom. ${gainUnrest(1)}. You cannot attempt False Victory on your next Kingdom turn.`,
        },
        criticalFailure: {
            msg: 'The truth comes out, and the citizens revolt against this attempt to manipulate them. A Public Scandal event takes place during this kingdom’s event phase, in addition to any other events that would normally take place. Attempt a DC @Check[type:flat|dc:11|showDC:all] check. On a success, the Public Scandal involves a randomly determined leader, but on a failure, the blame falls on Kanerah. Regardless of how the Public Scandal plays out, you cannot attempt False Victory again for 6 Kingdom turns.',
        },
    },
    'focused-attention': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'leadership',
        dc: 20,
        title: 'Focused Attention',
        description: `You set aside time to focus attention on aiding another leader in an activity. Choose another leader and a Kingdom skill, then attempt a DC 20 check using the chosen skill. On a success, you grant that leader a +2 circumstance bonus to one kingdom check using that skill, provided that leader attempts the skill check during the same Kingdom turn.

The Cooperative Leadership Kingdom feat (page 531) increases the efficiency of this activity.`,
        skills: simpleRank([...allSkills]),
        criticalSuccess: {
            msg: 'You grant that leader a +2 circumstance bonus to one kingdom check using that skill, provided that leader attempts the skill check during the same Kingdom turn',
            modifiers: (kingdom) => [{
                turns: 1,
                enabled: false,
                consumeId: '',
                value: hasFeat(kingdom, 'Cooperative Leadership') ? 3 : 2,
                name: 'Focused Attention: Critical Success',
                type: 'circumstance',
                rollOptions: ['focused-attention'],
            }],
        },
        success: {
            msg: 'You grant that leader a +2 circumstance bonus to one kingdom check using that skill, provided that leader attempts the skill check during the same Kingdom turn',
            modifiers: (kingdom) => [{
                turns: 1,
                enabled: false,
                consumeId: '',
                value: hasFeat(kingdom, 'Cooperative Leadership') ? 3 : 2,
                name: 'Focused Attention: Success',
                type: 'circumstance',
                rollOptions: ['focused-attention'],
            }],
        },
    },
    'fortify-hex': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'region',
        dc: 'control',
        title: 'Fortify Hex',
        description: `<p>Your command your engineers to construct a protected encampment, such as a fort or barbican, to serve as a defensive post in the hex. Spend RP as determined by the hex’s most inhospitable terrain:</p>
<ul>
<li><b>Mountains</b>: ${loseRP(12)}</li>
<li><b>Swamps</b>: ${loseRP(8)}</li>
<li><b>Forests</b>: ${loseRP(4)}</li>
<li><b>Hills</b>: ${loseRP(2)}</li>
<li><b>Plains</b>: ${loseRP(1)}</li>
</ul>
<p>Then attempt a basic check. A fortified hex grants an additional bonus in warfare (see Appendix 3), but also gives traveling PCs a place to rest that prevents wandering monsters from interrupting their rest.</p>`,
        requirement: 'The target hex must be claimed by your kingdom and must not have a settlement in it.',
        skills: simpleRank(['defense']),
        criticalSuccess: {
            msg: `You find a defensible position for your fortification and finish construction efficiently. Gain a refund of half the RP you spent to build in the hex, then ${loseUnrest(1)}`,
        },
        success: {
            msg:
                `You establish your fortification in the hex. ${loseUnrest(1)}`,
        },
        failure: {
            msg: 'You fail to fortify the hex.',
        },
        criticalFailure: {
            msg: `Your attempt ends in disaster. Not only do you fail to build a structure, but you lose several workers to an accident, banditry, a vicious monster, or some other unforeseen occurrence. ${gainUnrest(1)}`,
        },
    },
    'garrison-army': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'army',
        dc: 'control',
        title: 'Garrison Army',
        description: 'You move an army into a fortification and assign them to guard it. In order to garrison, the army must be located in a hex that contains a Refuge, Settlement, or Work Site. If you’re garrisoning the army in a Refuge hex, attempt a basic Defense check. If you’re garrisoning the army in a settlement, attempt a basic Politics check. If you’re garrisoning the army in a Work Site hex, attempt a basic Engineering check. This check’s DC increases by 5 if the hex is not part of your kingdom, or by 10 if the location is part of an enemy kingdom.',
        requirement: 'The army is in the same hex as a Refuge, Settlement, or Work Site.',
        skills: simpleRank(['defense', 'politics', 'engineering']),
        criticalSuccess: {
            msg: 'The army becomes fortified until it is deployed. Additionally, the efficiency of the garrisoning reduces this army’s Consumption by 2 (to a minimum of 1) until it is deployed.',
        },
        success: {
            msg: 'The army becomes fortified until it is deployed.',
        },
        failure: {
            msg: 'The army becomes fortified until the next Kingdom turn begins, at which point you must use this activity again to maintain the fortified condition.',
        },
        criticalFailure: {
            msg: `Your army clashes with local citizens, abuses their authority, lets their watchful readiness slack, and/or provokes confrontations where they are not needed. It does not become fortified, and you cannot attempt to garrison that army at this location again for 4 Kingdom turns. ${gainUnrest(1)}`,
        },
    },
    'gather-livestock': {
        title: 'Gather Livestock',
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'region',
        dc: 'control',
        description: 'Attempt a basic check to gather excess livestock from local wildlife, ranches, and farms. This generates a number of Food commodities.',
        skills: simpleRank(['wilderness']),
        criticalSuccess: {
            msg: gainCommodities('food', '1d4'),
        },
        success: {
            msg: gainCommodities('food', 1),
        },
        failure: {
            msg: 'Gain no Food commodities.',
        },
        criticalFailure: {
            msg: `${loseCommodities('food', '1d4')} to spoilage. If you have no Food to lose, you instead ${gainUnrest(1)}`,
        },
    },
    'go-fishing': {
        title: 'Go Fishing',
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'region',
        dc: 'control',
        description: 'Attempt a basic check to fish for food from the rivers and lakes in your kingdom.',
        requirement: 'Must have at least one claimed hex that includes river or lake terrain.',
        skills: simpleRank(['boating']),
        criticalSuccess: {
            msg: gainCommodities('food', '1d4'),
        },
        success: {
            msg: gainCommodities('food', 1),
        },
        failure: {
            msg: 'Gain no Food commodities.',
        },
        criticalFailure: {
            msg: `You lose some fishers to tragic accidents; ${gainUnrest(1)}`,
        },
    },
    'harvest-azure-lily-pollen': {
        oncePerRound: false,
        fortune: false,
        enabled: false,
        phase: 'region',
        dc: 'control',
        title: 'Harvest Azure Lily Pollen',
        description: 'You or a small group of trained herbalists or naturalists work to harvest and process azure lily pollen in hopes of creating a few doses of the poison (detailed on page 584). Attempt a DC 30 Agriculture check. You must then wait 1 Kingdom turn before attempting this activity again, to give the lilies time to recover.',
        skills: simpleRank(['agriculture'], 3),
        criticalSuccess: {
            msg: 'You create 2 doses of azure lily pollen.',
        },
        success: {
            msg: `You create 1 dose of azure lily pollen but must also attempt a @Check[type:basic|dc:11|showDC:all]. On a failure, some of the poison makes its way into criminal hands; ${gainRuin('crime', 1)}`,
        },
        failure: {
            msg:
                `You fail to harvest any azure lily pollen, in part because many of the resources make their way into the kingdom’s criminal underworld. ${gainRuin('crime', 1)}`,
        },
        criticalFailure: {
            msg: `Not only do you fail to harvest any pollen, and not only do resources make their way into the hands of criminals, but whispers and rumors that you allowed this to happen on purpose spread through the kingdom. ${gainUnrest('1d4')}, ${gainRuin('corruption', 1)}, and ${gainRuin('crime', 2)}`,
        },
    },
    'harvest-crops': {
        title: 'Harvest Crops',
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'region',
        dc: 'control',
        description: 'Attempt a basic check to forage for wild edibles or gather excess crops from farms.',
        skills: simpleRank(['agriculture']),
        criticalSuccess: {
            msg: gainCommodities('food', '1d4'),
        },
        success: {
            msg: gainCommodities('food', 1),
        },
        failure: {
            msg: 'Gain no Food commodities.',
        },
        criticalFailure: {
            msg: `${loseCommodities('food', '1d4')} to spoilage; if you have no Food to lose, you instead ${gainUnrest(1)}`,
        },
    },
    'hire-adventurers': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'leadership',
        dc: 'custom',
        title: 'Hire Adventurers',
        description: `While the PCs can strike out themselves to deal with ongoing events, it’s often more efficient to Hire Adventurers. When you Hire Adventurers to help end an ongoing event, the DC is equal to your Control DC adjusted by the event’s level modifier. ${loseRolledRD(1)} each time you attempt this activity.`,
        skills: simpleRank(['exploration']),
        criticalSuccess: {
            msg: 'You end the continuous event.',
        },
        success: {
            msg: 'The continuous event doesn’t end, but you gain a +2 circumstance bonus to resolve the event during the next Event phase.',
            modifiers: () => [{
                turns: 2,
                enabled: false,
                phases: ['event'],
                value: 2,
                name: 'Hire Adventurers: Success',
                type: 'circumstance',
            }],
        },
        failure: {
            msg: 'You fail to end the continuous event. If you try to end the continuous event again, the cost in RP increases to 2 Resource Dice.',
        },
        criticalFailure: {
            msg: 'You fail to end the continuous event. If you try to end the continuous event again, the cost in RP increases to 2 Resource Dice. In addition, word spreads quickly through the region—you can no longer attempt to end this continuous event by Hiring Adventurers.',
        },
    },
    'improve-lifestyle': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'commerce',
        dc: 'control',
        title: 'Improve Lifestyle',
        description: 'Attempt a basic check to draw upon your kingdom’s treasury to enhance the quality of life for your citizens. This activity can be taken only during the Commerce phase of a Kingdom turn',
        skills: simpleRank(['politics']),
        criticalSuccess: {
            msg: 'Your push to Improve Lifestyles affords your citizens significant free time to pursue recreational activities. For the remainder of the Kingdom turn, you gain a +2 circumstance bonus to Culture-based checks.',
            modifiers: () => [{
                turns: 1,
                enabled: true,
                abilities: ['culture'],
                value: 2,
                name: 'Improve Lifestyle: Critical Success',
                type: 'circumstance',
            }],
        },
        success: {
            msg: 'Your push to Improve Lifestyles helps your citizens enjoy life. For the remainder of the Kingdom turn, you gain a +1 circumstance bonus to Culture-based checks.',
            modifiers: () => [{
                turns: 1,
                enabled: true,
                abilities: ['culture'],
                value: 1,
                name: 'Improve Lifestyle: Success',
                type: 'circumstance',
            }],
        },
        failure: {
            msg: 'Your push to Improve Lifestyles helps your citizens enjoy life. For the remainder of the Kingdom turn, you gain a +1 circumstance bonus to Culture-based checks. In addition, you’ve strained your treasury. Take a –1 circumstance penalty to Economy-based checks for the remainder of this Kingdom turn.',
            modifiers: () => [{
                turns: 1,
                enabled: true,
                abilities: ['economy'],
                value: -1,
                name: 'Improve Lifestyle: Failure',
                type: 'circumstance',
            }, {
                turns: 1,
                enabled: true,
                abilities: ['culture'],
                value: 1,
                name: 'Improve Lifestyle: Failure',
                type: 'circumstance',
            }],
        },
        criticalFailure: {
            // TODO: ruin of choice
            msg: `Your attempt to Improve Lifestyles backfires horribly as criminal elements in your kingdom abuse your generosity. You take a –1 circumstance penalty to Economy-based checks for the remainder of the Kingdom turn, ${gainUnrest(1)}, and add 1 to a Ruin of your choice.`,
            modifiers: () => [{
                turns: 1,
                enabled: true,
                abilities: ['economy'],
                value: -1,
                name: 'Improve Lifestyle: Critical Failure',
                type: 'circumstance',
            }],
        },
    },
    'infiltration': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'leadership',
        dc: 'control',
        title: 'Infiltration',
        description: 'You send spies out to gather intelligence on a neighboring nation, a cult or thieves’ guild within your borders, an unclaimed Freehold, or even an unexplored adventure site. Alternately, you can simply send your spies out to investigate the current health of your kingdom. Attempt a basic check.',
        skills: simpleRank(['intrigue']),
        criticalSuccess: {
            msg: `You learn something valuable or helpful. If you were infiltrating a specific target, the GM decides what is learned, but the information is exact and precise. For example, if you were infiltrating an unexplored ruin, you might learn that the site is infested with web lurkers and spider swarms. If you were investigating your kingdom’s health, your spies reveal easy methods to address citizen dissatisfaction, allowing you to choose one of the following: ${loseUnrest('1d4')} or reduce a Ruin of your choice by 1.`,
        },
        success: {
            msg: `You learn something helpful about the target, but the information is vague and imprecise. For example, if you were infiltrating the same ruin mentioned in the critical success above, you might learn that some sort of aberration uses the ruins as its lair. If you were investigating your kingdom’s health, your spies learn enough that you can take action. ${loseUnrest(1)}`,
        },
        failure: {
            msg: 'Your spies fail to learn anything of import, but they are not themselves compromised.',
        },
        criticalFailure: {
            msg: 'You never hear from your spies again, but someone certainly does! You take a –2 circumstance penalty on all kingdom checks until the end of the next Kingdom turn as counter-infiltration from an unknown enemy tampers with your kingdom’s inner workings.',
            modifiers: () => [{
                turns: 2,
                enabled: true,
                value: -2,
                name: 'Infiltration: Critical Failure',
                type: 'circumstance',
            }],
        },
    },
    'irrigation': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'region',
        dc: 'control',
        title: 'Irrigation',
        description: `<p>You send excavators to build waterways, canals, or drainage systems to convey water from areas that have natural access to a river or lake. Spend RP as determined by the hex’s most inhospitable terrain feature:</p>
<ul>
<li><b>Mountains</b>: ${loseRP(12)}</li>
<li><b>Swamps</b>: ${loseRP(8)}</li>
<li><b>Forests</b>: ${loseRP(4)}</li>
<li><b>Hills</b>: ${loseRP(2)}</li>
<li><b>Plains</b>: ${loseRP(1)}</li>
</ul>
<p>Then attempt a basic check.</p>`,
        requirement: 'You control a hex adjacent to a river or lake that itself does not contain a river or lake.',
        skills: simpleRank(['engineering'], 1),
        criticalSuccess: {
            msg: 'The hex gains a river or lake terrain feature (or you change the effects of a previous critical failure at Irrigation in this hex into a failure); work with your GM to determine where these features appear in the hex. In addition, your workers were efficient and quick, and you regain half the RP you spent building the waterways.',
        },
        success: {
            msg: 'The hex gains a river or lake terrain feature (or you change the effects of a previous critical failure at Irrigation in this hex into a failure); work with your GM to determine where these features appear in the hex.',
        },
        failure: {
            msg: 'You fail to build workable systems or to restore a previous critical failure, and the hex does not gain the river or lake terrain feature.',
        },
        criticalFailure: {
            msg: `You fail to build workable systems or to restore a previous critical failure, and the hex does not gain the river or lake terrain feature. Your attempts at Irrigation are so completely useless that they become breeding grounds for disease. ${gainUnrest(1)}. From this point onward, at the start of your Kingdom turn’s Event phase, attempt a @Check[type:flat|dc:4]. This flat check’s DC increases by 1 for each hex in your kingdom that contains a critically failed attempt at Irrigation. If you fail this flat check, your kingdom suffers a Plague event in addition to any other event it might have. You can attempt this activity again in a later Kingdom turn to undo a critically failed Irrigation attempt.`,
        },
    },
    'manage-trade-agreements': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'commerce',
        dc: 'control',
        title: 'Manage Trade Agreements',
        description: `You send agents out to attend to established trade agreements. ${loseRP(2, true)} per Trade Agreement you wish to manage. Then attempt a basic check. If you Managed Trade Agreements on the previous turn, increase this DC by 5.`,
        skills: simpleRank(['trade']),
        criticalSuccess: {
            msg: `${createResourceButton({
                value: '1',
                turn: 'next',
                type: 'resource-dice',
                multiple: true,
            })} per trade agreement, and 1 Commodity of your choice per trade agreement (no more than half of these Commodities may be Luxuries).`,
        },
        success: {
            msg: `${createResourceButton({
                value: '1',
                turn: 'next',
                type: 'resource-dice',
                multiple: true,
            })} per trade agreement, or 1 Commodity of your choice per trade agreement (no more than half of these Commodities may be Luxuries).`,
        },
        failure: {
            msg: `${createResourceButton({
                value: '1',
                turn: 'next',
                type: 'resource-points',
                multiple: true,
            })} per trade agreement`,
        },
        criticalFailure: {
            msg: 'You gain no benefit, as your traders and merchants met with bad luck on the road. You can’t Manage Trade Agreements for 1 Kingdom turn.',
        },
    },
    'new-leadership': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'upkeep',
        dc: 'control',
        title: 'New Leadership',
        description: `<p>You announce the promotion of a character into a leadership role, whether they’re a newly appointed leader or just shifting from one leadership role to another. You normally perform this activity at the start of a Kingdom turn, but if unexpected events (such as the death of the character) remove a leader from a leadership role, you may immediately use the New Leadership activity to attempt to assign a new leader to that role, even outside of a Kingdom turn (applying the vacancy penalty for that role as appropriate). Attempt a basic Intrigue, Politics, Statecraft, or Warfare skill check—while any of these skills can be used, each skill is particularly suited to assigning two specific leadership roles.</p>
<ul>
<li><b>Intrigue</b>: Grants a +2 circumstance bonus to checks to assign Emissaries and Treasurers.</li>
<li><b>Politics</b>: Grants a +2 circumstance bonus to checks to assign Counselors and Rulers.</li>
<li><b>Statecraft</b>: Grants a +2 circumstance bonus to checks to assign Magisters and Viceroys.</li>
<li><b>Warfare</b>: Grants a +2 circumstance bonus to checks to assign Generals and Wardens.</li>
</ul>

<p>Rulers are particularly difficult to assign; when you take this activity to assign a new Ruler, you take a –4 circumstance penalty to the skill check, and unless you achieve a critical success, you ${gainUnrest(1)}. Whether or not you are simultaneously assigning a leader, you may also use this activity to attempt to reselect the four leadership roles that you have invested. Any result other than a critical failure allows this.</p>`,
        skills: simpleRank(['intrigue', 'politics', 'statecraft', 'warfare']),
        criticalSuccess: {
            msg: 'The people love the new leader. The leader immediately provides the benefits tied to occupying the new role and gains a +1 circumstance bonus to all Kingdom skill checks they attempt before the end of the next Kingdom turn.',
            modifiers: () => [{
                turns: 2,
                enabled: false,
                value: 1,
                name: 'New Leadership: Critical Success',
                type: 'circumstance',
            }],
        },
        success: {
            msg: `The people accept the new leader. The leader immediately provides the benefits tied to occupying the new role. ${gainUnrest(1)}`,
        },
        failure: {
            msg: `The people are unsure about the new leader. The leader takes a –1 circumstance penalty to all checks they attempt as part of their activities during the Activity phase of each Kingdom turn. At the end of the next Kingdom turn, the leader can attempt any Loyalty-based basic skill check to ingratiate themselves with the populace. The leader may attempt this check at the end of each Kingdom turn until they succeed. Success removes this penalty, but a critical failure results in the development detailed in Critical Failure below. ${gainUnrest(1)}`,
            modifiers: () => [{
                enabled: false,
                value: -1,
                name: 'New Leadership: Failure',
                type: 'circumstance',
            }],
        },
        criticalFailure: {
            msg: `The people reject the new leader. The leadership role is treated as vacant and you must attempt to reassign it using the New Leadership activity at the start of the next Kingdom turn. ${gainUnrest(1)}`,
        },
    },
    'offensive-gambit': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'army',
        dc: 'scouting',
        title: 'Offensive Gambit',
        description: 'You order an attack against an enemy army, causing a war encounter to begin after this Kingdom turn ends. No check is necessary if you wish to engage the enemy without attempting to gain an advantage in initiative. If you want to gain an advantage by surprising the enemy, attempt an Intrigue check. If you want to gain an advantage by intimidating the enemy, attempt a Warfare check. In either case, the DC is equal to the enemy army’s Scouting DC.',
        requirement: 'You have at least one army in the same hex as an enemy army.',
        skills: simpleRank(['intrigue']),
        criticalSuccess: {
            msg: 'Your approach surprises or intimidates the enemy. Your armies in this hex gain a +2 circumstance bonus on their initiative checks, and one enemy army of the party’s choice in this hex becomes shaken 1.',
        },
        success: {
            msg: 'Your approach gives you an advantage. Your armies in this hex gain a +2 circumstance bonus on their initiative checks.',
        },
        failure: {
            msg: 'You gain no advantage in the battle.',
        },
        criticalFailure: {
            msg: 'Not only do you fail to gain advantage, but the enemy forces have anticipated the attack. Enemy armies in this hex at the time of the Offensive',
        },
    },
    'outfit-army': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'army',
        dc: 'control',
        title: 'Outfit Army',
        description: `You provide your army with better gear. Choose what sort of gear you wish to provide your army with from the list beginning on page 67. The level of the gear chosen must be equal to or less than the army’s level. If you’re crafting or purchasing gear, the level of the gear chosen must be equal to or less than your kingdom level. If you’re distributing resources gained from battle, the level of the gear chosen must be equal to or less than the highest level of an enemy army defeated in that battle. 

If you’re purchasing the gear, this activity requires a basic Trade check and costs the standard amount of RP for the gear; you cannot purchase magic gear unless your kingdom is at least expert rank in Magic. 

If you’re distributing gear gained from battle, this activity requires a basic Warfare check and does not cost RP.`,
        skills: simpleRank(['trade', 'warfare']),
        criticalSuccess: {
            msg: 'The gear proved particularly easy to outfit, and the army becomes efficient.',
        },
        success: {
            msg: 'The gear is sufficient, and your army becomes outfitted with it immediately.',
        },
        failure: {
            msg: 'The gear proves to be unusable and the attempt to outfit the army fails. If you spent RP on the check, it is refunded.',
        },
        criticalFailure: {
            msg: 'The gear proves to be unusable and the attempt to outfit the army fails.',
        },
    },
    'pledge-of-fealty': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'leadership',
        dc: 'control',
        title: 'Pledge of Fealty',
        description: `When your representatives encounter freeholders, refugees, independent groups, or other bands of individuals gathered in the wilderness who aren’t already part of a nation, you can offer them a place in your kingdom, granting them the benefits of protection, security, and prosperity in exchange for their fealty. The benefits granted to your kingdom can vary wildly, but often manifest as one-time boons to your commodities or unique bonuses against certain types of events. The adventure text in this campaign offers numerous examples of groups who could accept a Pledge of Fealty.

You can attempt this skill check with Intrigue, Statecraft, or Warfare; however, certain groups will respond better (or worse) to specific skills. The DC is the group’s Negotiation DC (see the sidebar on page 519).`,
        skills: simpleRank(['intrigue', 'statecraft', 'warfare'], 1),
        criticalSuccess: {
            msg: 'The group becomes part of your kingdom, granting the specific boon or advantage listed in that group’s entry. If you haven’t already claimed the hex in which the group dwells, you immediately do so, gain 10XP and increasing your kingdom\'s Size by 1 (this affects all statistics determined by Size; see page 532). If the hex doesn’t share a border with your kingdom, it becomes a secondary territory and checks involving this location take a Control penalty.',
        },
        success: {
            msg: `The group becomes part of your kingdom, granting the specific boon or advantage listed in that group’s entry. If the hex doesn’t share a border with your kingdom, it becomes a secondary territory and checks involving this location take a Control penalty. ${loseRolledRD(1)} to the result to integrate the group into your kingdom.`,
        },
        failure: {
            msg:
                `The group refuses to pledge to you at this time. You can attempt to get them to Pledge Fealty next turn. ${gainUnrest(1)}`,
        },
        criticalFailure: {
            msg: `The group refuses to pledge to you—furthermore, it will never Pledge Fealty to your kingdom, barring significant in-play changes or actions by the PCs (subject to the GM’s approval). The group’s potentially violent rebuff of your offer ${gainUnrest(2)} and increases a Ruin of your choice by 1.`,
        },
    },
    'preventative-measures': {
        companion: 'Tristian',
        oncePerRound: false,
        fortune: false,
        enabled: false,
        phase: 'leadership',
        dc: 'control',
        title: 'Preventative Measures',
        description: 'Tristian helps to organize magical defenses and resources to combat potential upcoming disasters or dangers to the kingdom. Attempt a basic Magic check to determine how effective the magical preparations are.',
        skills: simpleRank(['magic']),
        criticalSuccess: {
            msg: 'The next time during this Kingdom turn that you attempt a Kingdom skill check to resolve a dangerous event, you gain a +2 circumstance bonus to the check and, unless you roll a critical failure, the result is improved one degree. If you reach the end of this Kingdom turn and haven’t had a dangerous event, you may decrease one Ruin of your choice by 1.',
            modifiers: () => [{
                turns: 1,
                consumeId: '',
                phases: ['event'],
                enabled: false,
                value: 2,
                name: 'Preventative Measures: Critical Success',
                type: 'circumstance',
            }],
        },
        success: {
            msg: 'The next time during this Kingdom turn that you attempt a Kingdom skill check to resolve a dangerous event, you gain a +2 circumstance bonus to the check.',
            modifiers: () => [{
                turns: 1,
                consumeId: '',
                phases: ['event'],
                enabled: false,
                value: 2,
                name: 'Preventative Measures: Success',
                type: 'circumstance',
            }],
        },
        criticalFailure: {
            msg: `The attempt to put preventative measures in place has resulted in significant waste of resources. You can’t use Preventative Measures again on your next Kingdom turn, and ${createResourceButton({
                turn: 'next',
                value: '2',
                type: 'resource-dice',
                mode: 'lose',
            })}`,
        },
    },
    'process-hidden-fees': {
        companion: 'Jubilost',
        oncePerRound: false,
        fortune: false,
        enabled: false,
        phase: 'leadership',
        dc: 'control',
        title: 'Process Hidden Fees',
        description: 'With Jubilost’s aid, you can process additional taxes, fees, and payments. Attempt a basic Trade check to determine what sorts of additional resources you gather.',
        skills: simpleRank(['trade']),
        criticalSuccess: {
            msg: createResourceButton({
                turn: 'next',
                value: '2',
                type: 'resource-dice',
            }),
        },
        success: {
            msg: `${createResourceButton({
                turn: 'next',
                value: '1',
                type: 'resource-dice',
            })}, but the citizens suspect something is going on: if you attempt to Process Hidden Fees on the next Kingdom turn, the result is worsened one degree.`,
        },
        failure: {
            msg: `${createResourceButton({
                turn: 'next',
                value: '1',
                type: 'resource-dice',
            })}, but the citizens catch wind of the fees and grow unhappy. ${gainUnrest(1)}, and you cannot Process Hidden Fees on your next Kingdom turn.`,
        },
        criticalFailure: {
            msg: `${createResourceButton({
                turn: 'next',
                value: '1',
                type: 'resource-dice',
            })}, but the citizens catch wind of the fees and grow unhappy. ${gainUnrest('1d6')}, and you cannot Process Hidden Fees on your next Kingdom turn.`,
        },
    },
    'prognostication': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'leadership',
        dc: 'control',
        title: 'Prognostication',
        description: 'Your kingdom’s spellcasters read the omens and provide advice on how best to prepare for near-future events. Attempt a basic check.',
        skills: simpleRank(['magic'], 1),
        criticalSuccess: {
            msg: 'If you have a random kingdom event this turn, roll twice to determine the event that takes place. The players choose which of the two results occurs, and the kingdom gains a +2 circumstance bonus to the check to resolve the event.',
            modifiers: () => [{
                turns: 1,
                consumeId: '',
                phases: ['event'],
                enabled: true,
                value: 2,
                name: 'Prognostication: Critical Success',
                type: 'circumstance',
            }],
        },
        success: {
            msg: 'Gain a +1 circumstance bonus to checks made to resolve random kingdom events this turn.',
            modifiers: () => [{
                turns: 1,
                consumeId: '',
                phases: ['event'],
                enabled: true,
                value: 1,
                name: 'Prognostication: Critical Success',
                type: 'circumstance',
            }],
        },
        failure: {
            msg: 'Your spellcasters divine no aid.',
        },
        criticalFailure: {
            msg: 'Your spellcasters provide inaccurate readings of the future. You automatically have a random kingdom event this turn. Roll twice to determine the event that takes place; the GM decides which of the two results occurs.',
        },
    },
    'provide-care': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'leadership',
        dc: 'control',
        title: 'Provide Care',
        description: 'Attempt a basic check to organize and encourage your settlements’ healers, apothecaries, medics, and other caregivers to provide care and support for citizens in need.',
        skills: simpleRank(['defense']),
        criticalSuccess: {
            msg: `You provide unexpectedly compassionate support for the people. ${loseUnrest(1)} and reduce one Ruin of your choice by 1.`,
        },
        success: {
            msg: `Your care soothes the worries and fears of the populace; ${loseUnrest(1)}.`,
        },
        failure: {
            msg: 'You don’t provide any notable care for the citizens, but at least you don’t make things worse.',
        },
        criticalFailure: {
            msg: `Your attempt to provide care backfires. ${gainUnrest(1)} or a Ruin of your choice by 1.`,
        },
    },
    'purchase-commodities': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'leadership',
        dc: 'control',
        title: 'Purchase Commodities',
        description: `You can spend RP to Purchase Commodities, but doing so is more expensive than gathering them or relying upon trade agreements. When you Purchase Commodities, select the Commodity you wish to purchase (Food, Lumber, Luxuries, Ore, or Stone). ${loseRP(8)} if you’re purchasing Luxuries or ${loseRP(4)} if you’re purchasing any other Commodity. Then attempt a basic check.`,
        skills: simpleRank(['trade']),
        criticalSuccess: {
            msg: `<p>You immediately gain 4 Commodities of the chosen type:</p> <ul>
<li>${gainCommodities('food', '4')}</li>
<li>${gainCommodities('ore', '4')}</li>
<li>${gainCommodities('lumber', '4')}</li>
<li>${gainCommodities('stone', '4')}</li>
<li>${gainCommodities('luxuries', '4')}</li>
</ul><p>and 2 Commodities of any other type (except Luxuries):</p> <ul>
<li>${gainCommodities('food', '2')}</li>
<li>${gainCommodities('ore', '2')}</li>
<li>${gainCommodities('lumber', '2')}</li>
<li>${gainCommodities('stone', '2')}</li>
</ul>`,
        },
        success: {
            msg: `<p>You gain 2 Commodities of the chosen type.<p> <ul>
<li>${gainCommodities('food', '2')}</li>
<li>${gainCommodities('ore', '2')}</li>
<li>${gainCommodities('lumber', '2')}</li>
<li>${gainCommodities('stone', '2')}</li>
<li>${gainCommodities('luxuries', '2')}</li>
</ul>`,
        },
        failure: {
            msg: `<p>You gain 1 Commodity of the chosen type.</p> <ul>
<li>${gainCommodities('food', '1')}</li>
<li>${gainCommodities('ore', '1')}</li>
<li>${gainCommodities('lumber', '1')}</li>
<li>${gainCommodities('stone', '1')}</li>
<li>${gainCommodities('luxuries', '1')}</li>
</ul>`,
        },
        criticalFailure: {
            msg: 'Failure You gain no Commodities.',
        },
    },
    'quell-unrest': {
        oncePerRound: true,
        fortune: false,
        enabled: true,
        phase: 'leadership',
        dc: 'control',
        title: 'Quell Unrest',
        description: 'You send your agents among the citizenry with the charge of suppressing dissent and calming unrest. You can attempt a basic Arts, Folklore, Intrigue, Magic, Politics, or Warfare check to Quell Unrest, but you can never use the same skill for this activity in consecutive Kingdom turns. This activity cannot be attempted more than once per Kingdom turn.',
        skills: simpleRank(['arts', 'folklore', 'intrigue', 'magic', 'politics', 'warfare']),
        criticalSuccess: {
            msg: loseUnrest('1d6'),
        },
        success: {
            msg: loseUnrest(1),
        },
        failure: {
            msg: 'You fail to reduce Unrest.',
        },
        criticalFailure: {
            msg: `You not only fail to reduce Unrest, but actually incite further anger among the citizenry. Choose one of the following: ${gainUnrest('1d4')} or increase two Ruins of your choice by 1.`,
        },
    },
    'read-all-about-it': {
        companion: 'Linzi',
        oncePerRound: false,
        fortune: false,
        enabled: false,
        phase: 'leadership',
        dc: 'control',
        title: 'Read All About It',
        description: 'You take advantage of your nation’s paper to print an extra edition, a bonus-sized issue, or something to spread news to the citizens of the nation so that they are more prepared for upcoming events or more informed on how to deal with ongoing events. Attempt a basic Scholarship check to determine how helpful the information proves to be.',
        requirement: 'You have built a Printing Press',
        skills: simpleRank(['scholarship']),
        criticalSuccess: {
            msg: 'Your kingdom becomes particularly prepared. The next time you attempt a skill check to resolve any event during this Kingdom turn, you gain a +4 circumstance bonus to the roll.',
            modifiers: () => [{
                turns: 1,
                consumeId: '',
                phases: ['event'],
                enabled: true,
                value: 4,
                name: 'Read All About It: Critical Success',
                type: 'circumstance',
            }],
        },
        success: {
            msg: 'The information is helpful, but only against ongoing events. The next time you attempt a skill check to resolve an ongoing event during this Kingdom turn, you gain a +2 circumstance bonus to the roll.',
            modifiers: () => [{
                turns: 1,
                consumeId: '',
                phases: ['event'],
                enabled: false,
                value: 2,
                name: 'Read All About It: Success',
                type: 'circumstance',
            }],
        },
        failure: {
            msg: 'You fail to prepare your people for the worst.',
        },
        criticalFailure: {
            msg: 'Critical parts of the information you published are false. You take a –2 circumstance penalty to all skill checks made to resolve Kingdom events for the remainder of this Kingdom turn',
            modifiers: () => [{
                turns: 1,
                phases: ['event'],
                enabled: true,
                value: -2,
                name: 'Read All About It: Critical Failure',
                type: 'circumstance',
            }],
        },
    },
    'recover-army-damaged': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'army',
        dc: 'control',
        title: 'Recover Army: Damaged',
        description: `
        When an army endures ill fortune, it can become afflicted by negative conditions. You can use the Recover Army activity to work at removing an affliction with a basic skill check.`,
        skills: {
            'defense': 0,
            'folklore': 2,
        },
        criticalSuccess: {
            msg: 'You increase its HP by 2 up to its maximum.',
        },
        success: {
            msg: 'You increase its HP by 1 up to its maximum.',
        },
        failure: {
            msg: 'You fail to remove the affliction. ',
        },
        criticalFailure: {
            msg: `You fail to remove the affliction and your soldier’s lowered morale spreads discontent; ${gainUnrest(1)}.`,
        },
    },
    'recover-army-defeated': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'army',
        dc: 'control',
        dcAdjustment: 5,
        title: 'Recover Army: Defeated',
        description: `
        When an army endures ill fortune, it can become afflicted by negative conditions. You can use the Recover Army activity to work at removing an affliction with a basic skill check. The DC is increased by 5.`,
        skills: {
            'politics': 3,
            'warfare': 2,
        },
        criticalSuccess: {
            msg: 'You increase its HP by 2 up to its maximum and remove the affliction.',
        },
        success: {
            msg: 'You increase its HP by 1 up to its maximum and remove the affliction.',
        },
        failure: {
            msg: 'You fail to remove the affliction.',
        },
        criticalFailure: {
            msg: `You fail to remove the affliction and your soldier’s lowered morale spreads discontent; ${gainUnrest(1)}. The army is destroyed.`,
        },
    },
    'recover-army-lost': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'army',
        dc: 'control',
        title: 'Recover Army: Lost',
        description: `
        When an army endures ill fortune, it can become afflicted by negative conditions. You can use the Recover Army activity to work at removing an affliction with a basic skill check.`,
        skills: {
            'exploration': 0,
            'wilderness': 2,
        },
        criticalSuccess: {
            msg: 'You remove the affliction.',
        },
        success: {
            msg: 'You remove the affliction.',
        },
        failure: {
            msg: 'You fail to remove the affliction.',
        },
        criticalFailure: {
            msg: `You fail to remove the affliction and your soldier’s lowered morale spreads discontent; ${gainUnrest(1)}.`,
        },
    },
    'recover-army-mired-pinned': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'army',
        dc: 'control',
        title: 'Recover Army: Mired or Pinned',
        description: `
        When an army endures ill fortune, it can become afflicted by negative conditions. You can use the Recover Army activity to work at removing an affliction with a basic skill check.`,
        skills: {
            'engineering': 0,
            'magic': 2,
        },
        criticalSuccess: {
            msg: 'You reduce the affliction’s value by 2. If the affliction does not have a value, it is removed.',
        },
        success: {
            msg: 'As critical success but you reduce the affliction’s value by 1.',
        },
        failure: {
            msg: 'You fail to remove the affliction.',
        },
        criticalFailure: {
            msg: `You fail to remove the affliction and your soldier’s lowered morale spreads discontent; ${gainUnrest(1)}.`,
        },
    },
    'recover-army-shaken': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'army',
        dc: 'control',
        title: 'Recover Army: Shaken',
        description: `
        When an army endures ill fortune, it can become afflicted by negative conditions. You can use the Recover Army activity to work at removing an affliction with a basic skill check.`,
        skills: {
            'arts': 0,
            'warfare': 2,
        },
        criticalSuccess: {
            msg: 'You reduce the affliction’s value by 2.',
        },
        success: {
            msg: 'You reduce the affliction’s value by 1.',
        },
        failure: {
            msg: 'You fail to remove the affliction.',
        },
        criticalFailure: {
            msg: `You fail to remove the affliction and your soldier’s lowered morale spreads discontent; ${gainUnrest(1)}.`,
        },
    },
    'recover-army-weary': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'army',
        dc: 'control',
        title: 'Recover Army: Weary',
        description: `
        When an army endures ill fortune, it can become afflicted by negative conditions. You can use the Recover Army activity to work at removing an affliction with a basic skill check.`,
        skills: {
            'defense': 0,
            'arts': 2,
        },
        criticalSuccess: {
            msg: 'You reduce the affliction’s value by 2.',
        },
        success: {
            msg: 'You reduce the affliction’s value by 1.',
        },
        failure: {
            msg: 'You fail to remove the affliction.',
        },
        criticalFailure: {
            msg: `You fail to remove the affliction and your soldier’s lowered morale spreads discontent; ${gainUnrest(1)}.`,
        },
    },
    'recruit-army': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'leadership',
        dc: 'custom',
        title: 'Recruit Army',
        description: 'Either you recruit an army from your kingdom’s citizens, or you secure the allegiance of a specialized army you encountered in the Stolen Lands. If you’re recruiting an army from your kingdom’s citizens, choose one of the basic armies listed at the start of page 570 and attempt a Warfare check against the army’s Recruitment DC. If you’re securing a specialized army, you must attempt a Statecraft check against the Recruitment DC;',
        skills: simpleRank(['warfare', 'statecraft']),
        criticalSuccess: {
            msg: 'You recruit the army; it becomes efficient.',
        },
        success: {
            msg: 'You recruit the army.',
        },
        failure: {
            msg: 'You fail to recruit the army.',
        },
        criticalFailure: {
            msg: `Many of the individuals in the army you attempted to recruit took offense at the attempt. ${gainUnrest(1)}, and you cannot attempt to recruit an army again until the next Kingdom turn.`,
        },
    },
    'recruit-monsters': {
        companion: 'Nok-Nok',
        oncePerRound: false,
        fortune: false,
        enabled: false,
        phase: 'region',
        dc: 'control',
        title: 'Recruit Monsters',
        description: 'While Nok-Nok is quick to suggest that most of the monsters the party encounters during their adventures deserve killing, he also understands that there can be exceptions. Some can be bribed or allied with, while others can be trusted to act on their instincts—a canny person can capitalize on these instincts or alliances to bolster a kingdom’s defenses. Attempt a basic Intrigue check.',
        skills: simpleRank(['intrigue']),
        criticalSuccess: {
            msg: `You manage to locate a monster’s lair and take steps to incorporate it into your kingdom’s defense. The next time your kingdom suffers a Bandit Activity, Monster Activity, Sacrifices, or Undead Uprising random event, you can use your recruited monster to help resolve the event. Doing so removes the Recruited Monster from your kingdom (you can attempt to recruit a new monster on a future kingdom turn though) but allows you to roll a skill check twice when resolving the Dangerous Hex event, taking the better of the two results as your actual result. This is a fortune effect.

Your kingdom can support 1 Recruited Monster at a time. If your kingdom is master in Intrigue, you can support up to 2 Recruited Monsters at a time, and if your kingdom is legendary in Intrigue, you can support up to 3 Recruited Monsters at a time.`,
        },
        success: {
            msg: 'You locate a monster’s lair but can’t recruit it into your kingdom’s defense just yet. If you attempt this activity on your next kingdom turn; the result of that check is improved one degree as you continue to build a rapport with the monster.',
        },
        failure: {
            msg: 'You fail to locate a monster, or if you were recruiting a monster you didn’t succeed at recruiting on the previous turn, that monster moves on and you must start the recruitment procedure from scratch in the future.',
        },
        criticalFailure: {
            msg: 'You found a monster, but it proves impossible to recruit. Worse, you’ve attracted its attention. A Monster Activity event occurs during the kingdom’s next Event Phase, in addition to any other potential random events.',
        },
    },
    'relocate-capital': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'leadership',
        dc: 'control',
        title: 'Relocate Capital',
        description: 'One of your settlements that is not your current capital must contain a Castle, Palace, or Town Hall. All leaders must spend all of their leadership activities during the Activity phase of a Kingdom turn on this activity. The kingdom leaders announce that they are uprooting the seat of government from its current home and reestablishing it in another settlement. Attempt a check with a DC equal to the kingdom’s Control DC + 5. You cannot Relocate your Capital again for at least 3 Kingdom turns.',
        skills: simpleRank(['industry'], 1),
        criticalSuccess: {
            msg: 'The move goes off splendidly, with people excited about the new capital and celebrating the leadership’s wisdom.',
        },
        success: {
            msg: `The move goes smoothly and with minimal disruption, but some folks are upset or homesick. ${gainUnrest(1)}`,
        },
        failure: {
            msg: `The move causes unhappiness. ${gainUnrest(1)} and increase two Ruins of your choice by 1.`,
        },
        criticalFailure: {
            msg: `The people reject the idea of the new capital and demand you move it back. The move is unsuccessful, and your capital remains unchanged. ${gainUnrest('1d4')}. Increase three Ruins of your choice by 1 and the fourth Ruin by 3.`,
        },
    },
    'repair-reputation-corruption': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'leadership',
        dc: 'control',
        dcAdjustment: 2,
        title: 'Repair Reputation Corruption',
        description: `When things have gotten out of hand in the kingdom and the nation’s reputation has become damaged, you can focus efforts on a campaign to reassure the citizens and bring them closer together, stamp down crime, organize repairs and maintenance of public structures, or strive to adjust poor public opinions.

The skill used to Repair Reputation depends on which Ruin total you wish to reduce. If you wish to reduce your Corruption, you attempt an Arts check. If you wish to reduce your Crime, you attempt a Trade check. If you wish to reduce your Decay, you attempt an Engineering check. If you wish to reduce your Strife, you attempt an Intrigue check. In all cases, the DC is your Control DC + 2.`,
        skills: simpleRank(['arts'], 1),
        criticalSuccess: {
            msg: `${loseRuin('corruption', 2)} and reduce its current ruin penalty by 1 to a minimum of 0.`,
        },
        success: {
            msg: loseRuin('corruption', 1),
        },
        failure: {
            msg: 'You fail to reduce the targeted Ruin. You cannot attempt to Repair Reputation on this Ruin for 1 Kingdom turn.',
        },
        criticalFailure: {
            msg: `You fail to reduce the targeted Ruin in a particularly public and embarrassing way. ${gainUnrest('1d4')}, and you cannot attempt to Repair Reputation for 3 Kingdom turns.`,
        },
    },
    'repair-reputation-crime': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'leadership',
        dc: 'control',
        dcAdjustment: 2,
        title: 'Repair Reputation Crime',
        description: `When things have gotten out of hand in the kingdom and the nation’s reputation has become damaged, you can focus efforts on a campaign to reassure the citizens and bring them closer together, stamp down crime, organize repairs and maintenance of public structures, or strive to adjust poor public opinions.

The skill used to Repair Reputation depends on which Ruin total you wish to reduce. If you wish to reduce your Corruption, you attempt an Arts check. If you wish to reduce your Crime, you attempt a Trade check. If you wish to reduce your Decay, you attempt an Engineering check. If you wish to reduce your Strife, you attempt an Intrigue check. In all cases, the DC is your Control DC + 2.`,
        skills: simpleRank(['trade'], 1),
        criticalSuccess: {
            msg: `${loseRuin('crime', 2)} and reduce its current ruin penalty by 1 to a minimum of 0.`,
        },
        success: {
            msg: loseRuin('crime', 1),
        },
        failure: {
            msg: 'You fail to reduce the targeted Ruin. You cannot attempt to Repair Reputation on this Ruin for 1 Kingdom turn.',
        },
        criticalFailure: {
            msg: `You fail to reduce the targeted Ruin in a particularly public and embarrassing way. ${gainUnrest('1d4')}, and you cannot attempt to Repair Reputation for 3 Kingdom turns.`,
        },
    },
    'repair-reputation-decay': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'leadership',
        dc: 'control',
        dcAdjustment: 2,
        title: 'Repair Reputation Decay',
        description: `When things have gotten out of hand in the kingdom and the nation’s reputation has become damaged, you can focus efforts on a campaign to reassure the citizens and bring them closer together, stamp down crime, organize repairs and maintenance of public structures, or strive to adjust poor public opinions.

The skill used to Repair Reputation depends on which Ruin total you wish to reduce. If you wish to reduce your Corruption, you attempt an Arts check. If you wish to reduce your Crime, you attempt a Trade check. If you wish to reduce your Decay, you attempt an Engineering check. If you wish to reduce your Strife, you attempt an Intrigue check. In all cases, the DC is your Control DC + 2.`,
        skills: simpleRank(['engineering'], 1),
        criticalSuccess: {
            msg: `${loseRuin('decay', 2)} and reduce its current ruin penalty by 1 to a minimum of 0.`,
        },
        success: {
            msg: loseRuin('decay', 1),
        },
        failure: {
            msg: 'You fail to reduce the targeted Ruin. You cannot attempt to Repair Reputation on this Ruin for 1 Kingdom turn.',
        },
        criticalFailure: {
            msg: `You fail to reduce the targeted Ruin in a particularly public and embarrassing way. ${gainUnrest('1d4')}, and you cannot attempt to Repair Reputation for 3 Kingdom turns.`,
        },
    },
    'repair-reputation-strife': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'leadership',
        dc: 'control',
        dcAdjustment: 2,
        title: 'Repair Reputation Strife',
        description: `When things have gotten out of hand in the kingdom and the nation’s reputation has become damaged, you can focus efforts on a campaign to reassure the citizens and bring them closer together, stamp down crime, organize repairs and maintenance of public structures, or strive to adjust poor public opinions.

The skill used to Repair Reputation depends on which Ruin total you wish to reduce. If you wish to reduce your Corruption, you attempt an Arts check. If you wish to reduce your Crime, you attempt a Trade check. If you wish to reduce your Decay, you attempt an Engineering check. If you wish to reduce your Strife, you attempt an Intrigue check. In all cases, the DC is your Control DC + 2.`,
        skills: simpleRank(['intrigue'], 1),
        criticalSuccess: {
            msg: `${loseRuin('strife', 2)} and reduce its current ruin penalty by 1 to a minimum of 0.`,
        },
        success: {
            msg: loseRuin('strife', 1),
        },
        failure: {
            msg: 'You fail to reduce the targeted Ruin. You cannot attempt to Repair Reputation on this Ruin for 1 Kingdom turn.',
        },
        criticalFailure: {
            msg: `You fail to reduce the targeted Ruin in a particularly public and embarrassing way. ${gainUnrest('1d4')}, and you cannot attempt to Repair Reputation for 3 Kingdom turns.`,
        },
    },
    'repair-the-flooded-mine': {
        oncePerRound: false,
        fortune: false,
        enabled: false,
        phase: 'leadership',
        dc: 32,
        title: 'Repair the Flooded Mine',
        description: `A group of engineers work to drain the flooded mine, shore up its damaged section, and establish the mine as a functional Work Site. ${loseRP(20)}, then attempt a DC 32 Engineering check.`,
        skills: simpleRank(['engineering'], 3),
        criticalSuccess: {
            msg: 'The mine is repaired, and as a bonus, your workers Establish a Work Site at the mine immediately. This mine produces 2 Ore Commodities per Kingdom turn.',
        },
        success: {
            msg: 'The mine is repaired. This mine produces 2 Ore Commodities per Kingdom turn.',
        },
        failure: {
            msg: 'Work on repairing the mine proceeds, but at a much slower (and more expensive) pace than you’d hoped. The mine remains flooded, but you can attempt this check again on the next Kingdom turn.',
        },
        criticalFailure: {
            msg: `Work on repairing the mine proceeds, but at a much slower (and more expensive) pace than you’d hoped. The mine remains flooded, but you can attempt this check again on the next Kingdom turn. In addition, a catastrophic failure costs the lives of several workers. ${gainUnrest(2)} and ${gainRuin('decay', 1)}. The next time you attempt to Repair the Flooded Mine, it costs 40 RP.`,
        },
    },
    'request-foreign-aid': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'leadership',
        dc: 'custom',
        title: 'Request Foreign Aid',
        description: 'When disaster strikes, you send out a call for help to another nation with whom you have diplomatic relations. The DC of this check is equal to the other group’s Negotiation DC +2 (see the sidebar on page 519).',
        requirement: 'You have diplomatic relations with the group you are requesting aid from.',
        skills: simpleRank(['statecraft'], 1),
        criticalSuccess: {
            msg: `Your ally’s aid grants a +4 circumstance bonus to any one Kingdom skill check attempted during the remainder of this Kingdom turn. You can choose to apply this bonus to any Kingdom skill check after the die is rolled, but must do so before the result is known. In addition, ${gainRolledRD(2)}; this RP does not accrue into XP at the end of the turn if you don’t spend it.`,
            modifiers: () => [{
                turns: 1,
                consumeId: '',
                enabled: false,
                value: 4,
                name: 'Request Foreign Aid: Critical Success',
                type: 'circumstance',
            }],
        },
        success: {
            msg: `Your ally’s aid grants you either a +2 circumstance bonus to any one Kingdom skill check attempted during the remainder of this Kingdom turn or ${gainRolledRD(1)}. This RP does not accrue into XP at the end of the turn if you don’t spend it. You can choose to apply the bonus to any Kingdom skill check after the die is rolled, but must do so before the result is known.`,
            modifiers: () => [{
                turns: 1,
                consumeId: '',
                enabled: false,
                value: 2,
                name: 'Request Foreign Aid: Success',
                type: 'circumstance',
            }],
        },
        failure: {
            msg: `Your ally marshals its resources but cannot get aid to you in time to deal with your current situation. ${createResourceButton({
                turn: 'next',
                value: '1d4',
                type: 'resource-points',
            })}`,
        },
        criticalFailure: {
            msg: `Your ally is tangled up in its own problems and is unable to assist you, is insulted by your request for aid, or might even have an interest in seeing your kingdom struggle against one of your ongoing events. Whatever the case, your pleas for aid make your kingdom look desperate. You gain no aid, but you do ${gainUnrest('1d4')}.`,
        },
    },
    'request-foreign-aid-vk': {
        oncePerRound: false,
        fortune: false,
        enabled: false,
        phase: 'leadership',
        dc: 'custom',
        title: 'Request Foreign Aid (V&K)',
        description: 'When disaster strikes, you send out a call for help to another nation with whom you have diplomatic relations. The DC of this check starts at the other group’s Negotiation DC +2, but every subsequent Kingdom turn you Request Foreign Aid from the same group, the DC increases by 2. Every Kingdom turn that passes without Requesting Foreign Aid from that Group reduces the DC by 1 (until you reach the other group’s Negotiation DC +2). You may only attempt to request Foreign Aid with a given group once per Kingdom turn regardless of the number of leaders pursuing activities.',
        requirement: 'You have diplomatic relations with the group you are requesting aid from.',
        skills: simpleRank(['statecraft'], 1),
        criticalSuccess: {
            msg: `Your ally’s aid grants a +4 circumstance bonus to any one Kingdom skill check attempted during the remainder of this Kingdom turn. You can choose to apply this bonus to any Kingdom skill check after the die is rolled, but must do so before the result is known. In addition, ${gainRolledRD(2)}; this RP does not accrue into XP at the end of the turn if you don’t spend it.`,
            modifiers: () => [{
                turns: 1,
                consumeId: '',
                enabled: false,
                value: 4,
                name: 'Request Foreign Aid: Critical Success',
                type: 'circumstance',
            }],
        },
        success: {
            msg: `Your ally’s aid grants you either a +2 circumstance bonus to any one Kingdom skill check attempted during the remainder of this Kingdom turn or ${gainRolledRD(1)}. This RP does not accrue into XP at the end of the turn if you don’t spend it. You can choose to apply the bonus to any Kingdom skill check after the die is rolled, but must do so before the result is known.`,
            modifiers: () => [{
                turns: 1,
                consumeId: '',
                enabled: false,
                value: 2,
                name: 'Request Foreign Aid: Success',
                type: 'circumstance',
            }],
        },
        failure: {
            msg: `Your ally marshals its resources but cannot get aid to you in time to deal with your current situation. ${createResourceButton({
                turn: 'next',
                value: '1d4',
                type: 'resource-points',
            })}`,
        },
        criticalFailure: {
            msg: `Your ally is tangled up in its own problems and is unable to assist you, is insulted by your request for aid, or might even have an interest in seeing your kingdom struggle against one of your ongoing events. Whatever the case, your pleas for aid make your kingdom look desperate. You gain no aid, but you do ${gainUnrest('1d4')}.`,
        },
    },
    'rest-and-relax': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'leadership',
        dc: 'control',
        title: 'Rest and Relax',
        description: `Working non-stop can burn out even the most devoted and dedicated individual. As such, it’s important to take time for yourself, and thus set a good example for the nation.

You take time to relax, and you extend the chance to unwind to your citizens as well. The Kingdom skill you use to determine the effectiveness of your time off depends on how you want to spend it: Use a basic Arts check to spend the time engaged in entertainment or the pursuit of a hobby. Use a basic Boating check to enjoy trips on the lakes and rivers of your kingdom. Use a basic Scholarship check to spend the time reading or studying a topic of personal interest beyond your daily duties. Use a basic Trade check to spend your time shopping or feasting. Use a basic Wilderness check to get away from the bustle and relax in the countryside. If your kingdom Rested and Relaxed the previous Kingdom turn, the DC increases by 4, as your kingdom’s production and output hasn’t had a chance to catch up to all those vacation days.`,
        skills: simpleRank(['arts', 'boating', 'scholarship', 'trade', 'wilderness']),
        criticalSuccess: {
            msg: 'The citizens enjoy the time off and are ready to get back to work. ' + loseUnrest(1) + ', and the next Leadership activity you take gains a +2 circumstance bonus.',
            modifiers: () => [{
                turns: 1,
                consumeId: '',
                phases: ['leadership'],
                enabled: true,
                value: 2,
                name: 'Rest and Relax: Success',
                type: 'circumstance',
            }],
        },
        success: {
            msg: `The time spent relaxing has calmed nerves; ${loseUnrest(1)}.`,
        },
        failure: {
            msg: 'The rest is welcome, but not particularly beneficial in the long term.',
        },
        criticalFailure: {
            msg: 'The time is wasted, and when you get back to work, you have to spend extra time catching up. Take a –2 circumstance penalty to your next skill check made as a Leadership activity.',
            modifiers: () => [{
                turns: 1,
                consumeId: '',
                phases: ['leadership'],
                enabled: true,
                value: -2,
                name: 'Rest and Relax: Critical Failure',
                type: 'circumstance',
            }],
        },
    },
    'restore-the-temple-of-the-elk': {
        oncePerRound: false,
        fortune: false,
        enabled: false,
        phase: 'leadership',
        dc: 25,
        title: 'Restore the Temple of the Elk',
        description: `You work with several worshippers of Erastil, gifted masons, and skilled laborers to restore the temple and once more consecrate it as a sacred place devoted to the worship of Erastil. ${loseRP('1d6')}, then attempt a DC 25 Folklore check.`,
        skills: simpleRank(['folklore']),
        criticalSuccess: {
            msg: 'The temple is restored and can now serve as a Refuge terrain feature. If you later build a settlement here, the temple instead functions as a free Shrine in the settlement. In addition, your work was so excellent that you’ve attracted Erastil’s attention! The PC who rolled the Folklore check is granted Erastil’s minor boon: whenever that PC critically fails a check to Subsist in the wild, they gain a failure instead. @UUID[Compendium.pf2e.boons-and-curses.hrTl9kfSNrOQeNze]{Erastil - Minor Boon}',
        },
        success: {
            msg: 'The temple is restored and can now serve as a Refuge terrain feature. If you later build a settlement here, the temple instead functions as a free Shrine in the settlement.',
        },
        failure: {
            msg: 'Work proceeds but is not yet complete; you can attempt to restore the temple again on the next Kingdom turn.',
        },
        criticalFailure: {
            msg: `Disaster strikes as the temple’s cavern collapses and rubble spills out to bury and destroy much of the temple’s plaza. ${gainRuin('decay', 1)} and ${gainUnrest('1d4')}. You can still attempt to Restore the Temple, but the DC for success increases by 4. This increase is cumulative with successive critical failures.`,
        },
    },
    'send-diplomatic-envoy': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'leadership',
        dc: 'custom',
        title: 'Send Diplomatic Envoy',
        description: 'You send emissaries to another group to foster positive relations and communication. The DC of this check is the group’s Negotiation DC (see the sidebar on page 519). Attempts to Send a Diplomatic Envoy to a nation with which your kingdom is at war take a –4 circumstance penalty to the check and have the result worsened one degree. At the GM’s option, some wars might be so heated that this activity has no chance of success.',
        skills: simpleRank(['statecraft'], 1),
        criticalSuccess: {
            msg: 'Your envoys are received quite warmly and make a good first impression. You establish diplomatic relations with the group (see page 534 for more information) and gain a +2 circumstance bonus to all checks made with that group until the next Kingdom turn.',
            modifiers: () => [{
                turns: 1,
                enabled: false,
                value: 2,
                name: 'Send Diplomatic Envoy: Critical Success',
                type: 'circumstance',
            }],
        },
        success: {
            msg: 'You establish diplomatic relations.',
        },
        failure: {
            msg: 'Your envoys are received, but the target organization isn’t ready to engage in diplomatic relations. If you attempt to Send a Diplomatic Envoy to the group next Kingdom turn, you gain a +2 circumstance bonus to that check.',
            modifiers: () => [{
                turns: 2,
                enabled: false,
                value: 2,
                activities: ['send-diplomatic-envoy'],
                name: 'Send Diplomatic Envoy: Success',
                type: 'circumstance',
            }],
        },
        criticalFailure: {
            msg: `Disaster! Your envoy fails to reach their destination, is turned back at the border, or is taken prisoner or executed, at the GM’s discretion. The repercussions on your kingdom’s morale and reputation are significant. Choose one of the following results: ${gainUnrest('1d4')}, add 1 to a Ruin of your choice, or ${loseRolledRD(2)}. In any event, you cannot attempt to Send a Diplomatic Envoy to this same target for the next 3 Kingdom Turns.`,
        },
    },
    'show-of-force': {
        companion: 'Regongar',
        oncePerRound: false,
        fortune: false,
        enabled: false,
        phase: 'leadership',
        dc: 'control',
        title: 'Show of Force',
        description: 'Using Regongar’s advice, a public show of force is performed in an attempt to curtail criminal activity or subversive activity in the kingdom. Attempt a basic Warfare check to determine how effective the Show of Force is.',
        skills: simpleRank(['warfare']),
        criticalSuccess: {
            msg: `The kingdom’s criminals are cowed by the show of force. ${loseRuin('crime', 2)} and reduce one other Ruin of your choice by 1.`,
        },
        success: {
            msg: `The kingdom’s criminals take note of the show of force. ${loseRuin('crime', 1)}`,
        },
        failure: {
            msg: `The show of force fails to impress criminals but unsettles the rest of the citizens. ${gainUnrest(1)}.`,
        },
        criticalFailure: {
            msg: `The criminals are emboldened by the failed show of force. ${gainUnrest('1d4')} and ${gainRuin('crime', 1)}.`,
        },
    },
    'spread-the-legend': {
        companion: 'Linzi',
        oncePerRound: false,
        fortune: false,
        enabled: false,
        phase: 'leadership',
        dc: 'control',
        title: 'Spread the Legend',
        description: 'Linzi works to spread the word of the party’s heroics and achievements, both through word of mouth and by distributing chapbooks or one-sheets detailing their exploits. Attempt a basic Arts check to determine the success of Linzi’s efforts. If she has secured a printing press for the kingdom after the PCs help with her quest (see To Ask for Forgiveness, below), the Arts check gains a +2 item bonus.',
        skills: simpleRank(['arts']),
        criticalSuccess: {
            msg: `Not only do Linzi’s stories bring pride and patriotism to the nation, but they also help increase its glory. ${loseUnrest('1d6')}, and ${createResourceButton({
                type: 'fame',
                turn: 'next',
                value: '1',
            })}. In addition, if the kingdom experiences a dangerous random event during this turn’s Event Phase, reduce that event’s level modifier by 1.`,
        },
        success: {
            msg: `The rousing and inspiring stories Linzi spreads about the PCs helps to bring the nation together. ${loseUnrest('1d6')}`,
        },
        failure: {
            msg: `Linzi avoids spreading unfortunate news, but only just barely. The citizens are only slightly entertained by their leaders’ exploits. ${loseUnrest(1)}.`,
        },
        criticalFailure: {
            msg: `Linzi accidentally spreads news of a humiliating or embarrassing nature, causing the people of the kingdom to lose respect for their leaders. ${gainUnrest('1d4')}.`,
        },
    },
    'supernatural-solution': {
        enabled: true,
        phase: 'leadership',
        dc: 'control',
        title: 'Supernatural Solution',
        description: 'Your spellcasters try to resolve issues when mundane solutions just aren’t enough. Attempt a basic check.',
        oncePerRound: false,
        fortune: true,
        skills: simpleRank(['magic']),
        criticalSuccess: {
            msg: `You can call upon your spellcasters’ supernatural solution to aid in resolving any Kingdom skill check made during the remainder of this Kingdom turn ${gainSolution('supernatural-solution')}. Do so just before a Kingdom skill check is rolled (by yourself or any other PC). Attempt a Magic check against the same DC in addition to the Kingdom skill check, and take whichever of the two results you prefer. If you don’t use your Supernatural Solution by the end of this Kingdom turn, this benefit ends and you gain 10XP instead.`,
        },
        success: {
            msg: `You can call upon your spellcasters’ supernatural solution to aid in resolving any Kingdom skill check made during the remainder of this Kingdom turn ${gainSolution('supernatural-solution')}. Do so just before a Kingdom skill check is rolled (by yourself or any other PC). Attempt a Magic check against the same DC in addition to the Kingdom skill check, and take whichever of the two results you prefer. If you don’t use your Supernatural Solution by the end of this Kingdom turn, this benefit ends and you gain 10XP instead. However, you ${loseRP('1d4')} to research the solution. This cost is paid now, whether or not you use your supernatural solution.`,
        },
        failure: {
            msg: `Your attempt at researching a supernatural solution costs additional RP to research, but is ultimately a failure, providing no advantage. ${loseRP('2d6')}`,
        },
        criticalFailure: {
            msg: `Your attempt at researching a supernatural solution costs additional RP to research, but is ultimately a failure, providing no advantage. ${loseRP('2d6')}. In addition, your spellcasters’ resources and morale are impacted such that you cannot attempt a Supernatural Solution again for 2 Kingdom turns.`,
        },
        special: 'You cannot influence a check with Supernatural Solution and Creative Solution simultaneously.',
    },
    'supplementary-hunting': {
        companion: 'Ekundayo',
        oncePerRound: false,
        fortune: false,
        enabled: false,
        phase: 'region',
        dc: 'control',
        title: 'Supplementary Hunting',
        description: 'Following Ekundayo’s advice, rural-dwelling citizens work to supplement stores of food and resources through hunting and trapping. Attempt a basic Wilderness check to gather excess livestock from the local wildlife, ranches, and farms to generate food commodities.',
        skills: simpleRank(['wilderness']),
        criticalSuccess: {
            msg: `${gainCommodities('food', '1d4')}, ${gainCommodities('luxuries', 1)}, and ${createResourceButton({
                type: 'resource-dice',
                turn: 'next',
                value: '1',
            })}`,
        },
        success: {
            msg: `Choose one: ${gainCommodities('food', '1d4')}, ${gainCommodities('luxuries', 1)}, or ${createResourceButton({
                type: 'resource-dice',
                turn: 'next',
                value: '1',
            })}`,
        },
        failure: {
            msg: 'Your hunters and trappers fail to supplement your stores and must spend time resupplying and setting new traps; you cannot attempt Supplementary Hunting on the next Kingdom turn.',
        },
        criticalFailure: {
            msg: `Your hunters and trappers fail to supplement your stores and must spend time resupplying and setting new traps; you cannot attempt Supplementary Hunting on the next Kingdom turn. In addition, your hunters and trappers have accidentally attracted the attention of dangerous wildlife. Either ${gainUnrest('1d4')} or increase a Ruin of your choice by 1.`,
        },
    },
    'tap-treasury': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'commerce',
        dc: 'control',
        title: 'Tap Treasury',
        description: 'You tap into the cash reserves of your kingdom for the PCs’ personal use or to provide emergency funding for an event. This is a basic check, but after you succeed or critically succeed at this activity, all future attempts to Tap Treasury have their results worsened two degrees. This penalty persists until funds equal to those taken from the treasury are repaid via Capital Investment',
        skills: simpleRank(['statecraft']),
        criticalSuccess: {
            msg: 'You withdraw funds equal to the Currency per Additional PC column on Table 10–9: Party Treasure By Level @UUID[Compendium.pf2e.journals.S55aqwWIzpQRFhcq.JournalEntryPage.Ly8l2GT6dbvuY3A2]{Treasure}, or you successfully fund the unexpected event that required you to Tap your Treasury.',
        },
        success: {
            msg: 'You withdraw funds equal to the Currency per Additional PC column on Table 10–9: Party Treasure By Level @UUID[Compendium.pf2e.journals.S55aqwWIzpQRFhcq.JournalEntryPage.Ly8l2GT6dbvuY3A2]{Treasure}, or you successfully fund the unexpected event that required you to Tap your Treasury. In addition, you overdraw your treasury in the attempt. You take a –1 circumstance penalty to all Economy-based checks until the end of your next Kingdom turn.',
            modifiers: () => [{
                value: -1,
                name: 'Tap Treasury: Success',
                abilities: ['economy'],
                turns: 2,
                enabled: true,
                type: 'circumstance',
            }],
        },
        failure: {
            msg: 'You fail to secure the funds you need, and rumors about the kingdom’s potential shortfall of cash cause you to take a –1 circumstance penalty to all Loyalty- and Economy-based checks until the end of your next Kingdom turn.',
            modifiers: () => [{
                value: -1,
                name: 'Tap Treasury: Success',
                abilities: ['economy', 'loyalty'],
                turns: 2,
                enabled: true,
                type: 'circumstance',
            }],
        },
        criticalFailure: {
            msg: `You fail to secure the funds you need, and rumors about the kingdom’s potential shortfall of cash cause you to take a –1 circumstance penalty to all Loyalty- and Economy-based checks until the end of your next Kingdom turn. In addition, rumors spiral out of control. ${gainUnrest(1)} and add 1 to a Ruin of your choice.`,
            modifiers: () => [{
                value: -1,
                name: 'Tap Treasury: Success',
                abilities: ['economy', 'loyalty'],
                turns: 2,
                enabled: true,
                type: 'circumstance',
            }],
        },
    },
    'trade-commodities': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'commerce',
        dc: 'control',
        title: 'Trade Commodities',
        description: 'There are five different categories of Commodities: Food, Lumber, Luxuries, Ore, and Stone. When you Trade Commodities, select one Commodity that your kingdom currently stockpiles and reduce that Commodity’s stockpile by up to 4. Then attempt a basic check. If you trade with a group that you’ve established diplomatic relations with, you gain a +1 circumstance bonus to the check.',
        skills: simpleRank(['industry']),
        criticalSuccess: {
            msg: `${createResourceButton({
                type: 'resource-dice',
                turn: 'next',
                value: '2',
                multiple: true,
            })} per point of stockpile expended from your Commodity now.`,
        },
        success: {
            msg: `${createResourceButton({
                type: 'resource-dice',
                turn: 'next',
                value: '1',
                multiple: true,
            })} per point of stockpile expended from your Commodity now.`,
        },
        failure: {
            msg: createResourceButton({type: 'resource-dice', turn: 'next', value: '1'}),
        },
        criticalFailure: {
            msg: `You gain no bonus Resource Dice (though the Commodity remains depleted). If you Traded Commodities the previous turn, ${gainUnrest(1)}`,
        },
    },
    'train-army': {
        oncePerRound: false,
        fortune: false,
        enabled: true,
        phase: 'army',
        dc: 'control',
        title: 'Train Army',
        description: 'You train an army in the use of a tactic. Choose one of the tactics from those listed starting on page 68, then attempt a Scholarship or Warfare check against the tactic’s Training DC. If your army has already learned its maximum number of tactics, the newly learned tactic replaces a previously learned tactic of your choice. ',
        skills: simpleRank(['scholarship', 'warfare']),
        criticalSuccess: {
            msg: 'The army learns the tactic and then becomes efficient.',
        },
        success: {
            msg: 'The army learns the tactic.',
        },
        failure: {
            msg: 'The army fails to learn the tactic.',
        },
        criticalFailure: {
            msg: 'The army not only fails to learn the tactic but becomes frustrated and exhausted from the training; increase the army’s weary condition by 1.',
        },
    },
    'warfare-exercises': {
        companion: 'Valerie',
        oncePerRound: false,
        fortune: false,
        enabled: false,
        phase: 'leadership',
        dc: 'control',
        title: 'Warfare Exercises',
        description: 'Valerie spends time studying the nation’s armies, speaking with its commanders, researching historical records of battles, and running simulations in war rooms to help predict the best ways to prepare for upcoming conflicts. Attempt a basic Warfare check to determine the success of these exercises.',
        skills: simpleRank(['warfare']),
        criticalSuccess: {
            msg: 'The exercises reveal a wide range of suggestions for the PCs to use during that month’s military exercises. All Army activities taken during this Kingdom turn’s Activity Phase gain a +1 circumstance bonus. This bonus increases to +2 at Kingdom level 9 and +3 at Kingdom level 15. In addition, the next time this Kingdom turn that you roll a critical failure on an Army activity, the result is improved to a regular failure instead.',
            modifiers: (kingdom: Kingdom) => [{
                turns: 1,
                consumeId: '',
                enabled: true,
                phases: ['army'],
                value: kingdom.level < 9 ? 1 : (kingdom.level < 15 ? 2 : 3),
                name: 'Warfare Exercises: Critical Success',
                type: 'circumstance',
            }],
        },
        success: {
            msg: 'The exercises grant a +1 circumstance bonus to your first Army activity taken during the Kingdom turn’s Activity Phase. This bonus increases to +2 at Kingdom level 9 and +3 at Kingdom level 15.',
            modifiers: (kingdom: Kingdom) => [{
                turns: 1,
                consumeId: '',
                enabled: true,
                phases: ['army'],
                value: kingdom.level < 9 ? 1 : (kingdom.level < 15 ? 2 : 3),
                name: 'Warfare Exercises: Critical Success',
                type: 'circumstance',
            }],
        },
        failure: {
            msg: 'The warfare exercises provide no insight this turn.',
        },
        criticalFailure: {
            msg: 'You accidentally form incorrect assumptions about your military tactics. The next time you roll a failure on an Army activity this Kingdom turn, it becomes a critical failure instead.',
        },
    },
    'reconnoiter-hex': {
        oncePerRound: false,
        fortune: false,
        enabled: false,
        phase: 'leadership',
        dc: 'control',
        title: 'Reconnoiter Hex (V&K)',
        description: `You send a team to spend time surveying and exploring a specific hex, getting the lay of the land and looking for unusual features and specific sites. ${loseRP(1)} and then attempt a Basic check.`,
        skills: simpleRank(['wilderness']),
        criticalSuccess: {
            msg: 'Your team successfully explores the hex and it is now Reconnoitered for the purpose of Claim Hex. Your team automatically finds one Special or Hidden feature if the hex contains one. If the hex contains multiple Special or Hidden Features the GM chooses one. If the hex contains an Encounter or Hazard, the team avoids it and reports back useful and detailed information on it. In addition, your team\'s reconnaissance of the hex goes so smoothly you may immediately attempt an additional Reconnoiter Hex activity on an adjacent hex. Treat a Critical Success on this additional check as a Success instead.',
        },
        success: {
            msg: 'Your team successfully explores the hex and it is now Reconnoitered for the purpose of Claim Hex. If the hex contains a Special feature your team may find it if your GM wishes. If the hex contains an Encounter or Hazard, the team avoids it and reports basic information on it.',
        },
        failure: {
            msg: 'Your team fails to explore the hex sufficiently. If the hex contains an Encounter or Hazard, the team escapes it and reports basic information on it.',
        },
        criticalFailure: {
            msg: 'Your team fails to explore the hex sufficiently and a number of the team are lost, causing you to take a -1 circumstance penalty to Loyalty-based checks until the end of your next Kingdom turn. If the hex contains an Encounter or Hazard, the team members were lost to it and the survivors can report back basic information on it.',
            modifiers: () => [{
                turns: 2,
                enabled: true,
                abilities: ['loyalty'],
                value: -1,
                name: 'Reconnoiter Hex: Critical Failure',
                type: 'circumstance',
            }],
        },
    },
    'take-charge': {
        oncePerRound: false,
        fortune: false,
        enabled: false,
        phase: 'leadership',
        dc: 'control',
        title: 'Take Charge (V&K)',
        description: 'You spend some time getting directly involved in helping your kingdom. Choose a skill that your Kingdom is at least Trained in, then attempt a basic check. You can never use the same skill for this activity twice in the same Kingdom turn.',
        skills: simpleRank([...allSkills], 1),
        criticalSuccess: {
            msg: `${gainRP(1)}. In addition you get a +1 Circumstance Bonus to the next Check you make this turn with the chosen skill. `,
            modifiers: () => [{
                turns: 1,
                consumeId: '',
                enabled: false,
                value: 1,
                name: 'Take Charge: Critical Success',
                type: 'circumstance',
            }],
        },
        success: {
            msg: gainRP(1),
        },
        failure: {
            msg: 'You fail to generate RP.',
        },
        criticalFailure: {
            msg: 'You take a -1 Circumstance Penalty to the next Check you make this turn with the chosen skill.',
            modifiers: () => [{
                turns: 1,
                consumeId: '',
                enabled: false,
                value: -1,
                name: 'Take Charge: Critical Failure',
                type: 'circumstance',
            }],
        },
    },
};

interface CreateResourceButton {
    type: RolledResources,
    mode?: ResourceMode,
    turn?: ResourceTurn,
    value: string,
    hints?: string;
    multiple?: boolean;
}

export function gainXp(value: number | string): string {
    return createResourceButton({value: `${value}`, type: 'xp'});
}

export function gainFame(value: number | string): string {
    return createResourceButton({value: `${value}`, type: 'fame'});
}

export function loseFame(value: number | string): string {
    return createResourceButton({value: `${value}`, type: 'fame', mode: 'lose'});
}

export function gainCommodities(type: keyof Commodities, value: number | string): string {
    return createResourceButton({value: `${value}`, type});
}

export function loseCommodities(type: keyof Commodities, value: number | string): string {
    return createResourceButton({value: `${value}`, type, mode: 'lose'});
}

export function gainRuin(type: Ruin, value: number | string): string {
    return createResourceButton({value: `${value}`, type});
}

export function loseRuin(type: Ruin, value: number | string): string {
    return createResourceButton({value: `${value}`, type, mode: 'lose'});
}

export function gainRolledRD(value: number | string): string {
    return createResourceButton({value: `${value}`, type: 'rolled-resource-dice'});
}

export function loseRolledRD(value: number | string): string {
    return createResourceButton({value: `${value}`, type: 'rolled-resource-dice', mode: 'lose'});
}

export function gainRP(value: number | string): string {
    return createResourceButton({value: `${value}`, type: 'resource-points'});
}

export function loseRP(value: number | string, multiple = false): string {
    return createResourceButton({value: `${value}`, type: 'resource-points', mode: 'lose', multiple});
}

export function gainUnrest(value: number | string): string {
    return createResourceButton({value: `${value}`, type: 'unrest'});
}

export function loseUnrest(value: number | string): string {
    return createResourceButton({value: `${value}`, type: 'unrest', mode: 'lose'});
}

export function gainSolution(type: 'creative-solution' | 'supernatural-solution'): string {
    return createResourceButton({value: '1', type, mode: 'gain'});
}

export function createResourceButton({
                                         turn = 'now',
                                         value,
                                         mode = 'gain',
                                         type,
                                         hints,
                                         multiple = false,
                                     }: CreateResourceButton): string {
    const turnLabel = turn === 'now' ? '' : ' Next Turn';
    const label = `${mode === 'gain' ? 'Gain' : 'Lose'} ${value} ${unslugify(type)}${turnLabel}`;
    return `<button type="button" class="km-gain-lose" 
        data-type="${type}"
        data-mode="${mode}"
        data-turn="${turn}"
        data-multiple="${multiple}"
        ${value !== undefined ? `data-value="${value}"` : ''}
        >${label}${hints !== undefined ? `(${hints})` : ''}</button>`;
}

export function getKingdomActivitiesById(additionalActivities: KingdomActivity[]): KingdomActivityById {
    return <KingdomActivityById>Object.fromEntries(getKingdomActivities(additionalActivities)
        .map(activity => [activity.id, activity]));
}

export function getKingdomActivities(additionalActivities: KingdomActivity[]): KingdomActivity[] {
    const homebrewIds = new Set(additionalActivities.map(a => a.id));
    return Array.from(Object.entries(activityData))
        .filter(([id]) => !homebrewIds.has(id))
        .map(([id, activity]) => {
            return {
                id,
                ...activity,
            };
        })
        .concat(additionalActivities);
}