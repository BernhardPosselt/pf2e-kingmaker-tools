import {getBooleanSetting, getNumberSetting, getStringSetting, setSetting} from '../../settings';
import {updateKingdomArmyConsumption} from '../../armies/utils';
import {ResourceAutomationMode} from '../scene';
import {clamped, LabelAndValue, RollModeChoices, rollModeChoices} from '../../utils';
import {getKingdom, saveKingdom} from "../storage";
import {Kingdom} from "../data/kingdom";

type UntrainedSkillProficiency = 'level' | 'halfLevel' | 'none';

interface KingdomSettingData {
    expandMagicUse: boolean;
    vkXpRules: boolean;
    rpToXPConversionRate: number;
    rpToXpConversionLimit: number;
    doubleSkillIncreases: boolean;
    untrainedSkillProficiency: UntrainedSkillProficiency;
    allStructuresItemBonusesStack: boolean;
    automaticallyCalculateArmyConsumption: boolean;
    xpPerClaimedHex: number;
    cultOfTheBloomEvents: boolean;
    kingdomEventRollMode: string;
    kingdomEventsTable: string;
    kingdomCultTable: string;
    ignoreSkillRequirements: boolean;
    autoCalculateSettlementLevel: boolean;
    capitalInvestmentInCapital: boolean;
    reduceDCToBuildLumberStructures: boolean;
    automateResources: ResourceAutomationMode;
    rollModeChoices: RollModeChoices;
    untrainedSkillProficiencies: LabelAndValue[];
    automateResourcesChoices: LabelAndValue[];
    maxFamePoints: number;
}

interface KingdomSettingOptions {
    game: Game;
    sheetActor: Actor;
    onSave: () => void;
}

class KingdomSettings extends FormApplication<FormApplicationOptions & KingdomSettingOptions, object, null> {
    private game: Game;
    private onSave: () => void;
    private data: KingdomSettingData;
    private actor: Actor;
    private kingdom: Kingdom;

    constructor(options: KingdomSettingOptions) {
        super(null, options);
        this.game = options.game;
        this.onSave = options.onSave;
        this.actor = options.sheetActor;
        this.kingdom = getKingdom(this.actor);
        this.data = {
            expandMagicUse: this.kingdom.settings.expandMagicUse,
            allStructuresItemBonusesStack: getBooleanSetting(this.game, 'kingdomAllStructureItemBonusesStack'),
            ignoreSkillRequirements: getBooleanSetting(this.game, 'kingdomIgnoreSkillRequirements'),
            automaticallyCalculateArmyConsumption: getBooleanSetting(this.game, 'autoCalculateArmyConsumption'),
            doubleSkillIncreases: getBooleanSetting(this.game, 'kingdomSkillIncreaseEveryLevel'),
            rpToXpConversionLimit: getNumberSetting(this.game, 'rpToXpConversionLimit'),
            rpToXPConversionRate: getNumberSetting(this.game, 'rpToXpConversionRate'),
            untrainedSkillProficiency: this.getUntrainedProficiency(),
            vkXpRules: getBooleanSetting(this.game, 'vanceAndKerensharaXP'),
            xpPerClaimedHex: getNumberSetting(this.game, 'xpPerClaimedHex'),
            cultOfTheBloomEvents: getBooleanSetting(this.game, 'cultOfTheBloomEvents'),
            autoCalculateSettlementLevel: getBooleanSetting(this.game, 'autoCalculateSettlementLevel'),
            kingdomEventsTable: getStringSetting(this.game, 'kingdomEventsTable'),
            kingdomCultTable: getStringSetting(this.game, 'kingdomCultTable'),
            kingdomEventRollMode: getStringSetting(this.game, 'kingdomEventRollMode'),
            capitalInvestmentInCapital: getBooleanSetting(this.game, 'capitalInvestmentInCapital'),
            reduceDCToBuildLumberStructures: getBooleanSetting(this.game, 'reduceDCToBuildLumberStructures'),
            automateResources: getStringSetting(this.game, 'automateResources') as ResourceAutomationMode,
            rollModeChoices: rollModeChoices,
            untrainedSkillProficiencies: [
                {value: 'level', label: 'Full Level'},
                {value: 'halfLevel', label: 'Half Level'},
                {value: 'none', label: 'None'},
            ],
            automateResourcesChoices: [
                {'value': 'kingmaker', label: 'Official Module'},
                {'value': 'tileBased', label: 'Tile/Drawing Based'},
                {'value': 'manual', label: 'Manual'},
            ],
            maxFamePoints: this.kingdom.fame.max,
        };
    }

    static override get defaultOptions(): FormApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'kingdom-settings';
        options.title = 'Kingdom Settings';
        options.template = 'modules/pf2e-kingmaker-tools/templates/kingdom/settings.hbs';
        options.submitOnChange = true;
        options.closeOnSubmit = false;
        options.classes = [];
        options.height = 'auto';
        options.width = 600;
        return options;
    }

    override getData(): Promise<KingdomSettingData> | KingdomSettingData {
        return this.data;
    }

    override activateListeners(html: JQuery): void {
        super.activateListeners(html);
        const $html = html[0];
        $html.querySelector('.save')
            ?.addEventListener('click', async (): Promise<void> => {
                await this.save();
                await this.close();
                this.onSave();
            });
    }

    protected async _updateObject(event: Event, formData: object): Promise<void> {
        this.data = {
            ...this.data,
            ...foundry.utils.expandObject(formData) as KingdomSettingData,
        };
        this.render();
        console.log(this.data);
    }

    private getUntrainedProficiency(): UntrainedSkillProficiency {
        if (getBooleanSetting(this.game, 'kingdomAlwaysAddLevel')) {
            return 'level';
        } else if (getBooleanSetting(this.game, 'kingdomAlwaysAddHalfLevel')) {
            return 'halfLevel';
        } else {
            return 'none';
        }
    }

    private async save(): Promise<void> {
        await setSetting(this.game, 'kingdomAllStructureItemBonusesStack', this.data.allStructuresItemBonusesStack);
        await setSetting(this.game, 'autoCalculateArmyConsumption', this.data.automaticallyCalculateArmyConsumption);
        await setSetting(this.game, 'kingdomSkillIncreaseEveryLevel', this.data.doubleSkillIncreases);
        await setSetting(this.game, 'rpToXpConversionLimit', this.data.rpToXpConversionLimit);
        await setSetting(this.game, 'rpToXpConversionRate', this.data.rpToXPConversionRate);
        await setSetting(this.game, 'vanceAndKerensharaXP', this.data.vkXpRules);
        await setSetting(this.game, 'xpPerClaimedHex', this.data.xpPerClaimedHex);
        await setSetting(this.game, 'cultOfTheBloomEvents', this.data.cultOfTheBloomEvents);
        await setSetting(this.game, 'kingdomEventsTable', this.data.kingdomEventsTable);
        await setSetting(this.game, 'kingdomCultTable', this.data.kingdomCultTable);
        await setSetting(this.game, 'kingdomEventRollMode', this.data.kingdomEventRollMode);
        await setSetting(this.game, 'kingdomIgnoreSkillRequirements', this.data.ignoreSkillRequirements);
        await setSetting(this.game, 'autoCalculateSettlementLevel', this.data.autoCalculateSettlementLevel);
        await setSetting(this.game, 'capitalInvestmentInCapital', this.data.capitalInvestmentInCapital);
        await setSetting(this.game, 'reduceDCToBuildLumberStructures', this.data.reduceDCToBuildLumberStructures);
        await setSetting(this.game, 'automateResources', this.data.automateResources);
        await saveKingdom(this.actor, {settings: {expandMagicUse: this.data.expandMagicUse}});
        if (this.data.untrainedSkillProficiency === 'level') {
            await setSetting(this.game, 'kingdomAlwaysAddLevel', true);
            await setSetting(this.game, 'kingdomAlwaysAddHalfLevel', false);
        } else if (this.data.untrainedSkillProficiency === 'halfLevel') {
            await setSetting(this.game, 'kingdomAlwaysAddLevel', false);
            await setSetting(this.game, 'kingdomAlwaysAddHalfLevel', true);
        } else {
            await setSetting(this.game, 'kingdomAlwaysAddLevel', false);
            await setSetting(this.game, 'kingdomAlwaysAddHalfLevel', false);
        }
        if (this.data.automaticallyCalculateArmyConsumption) {
            await updateKingdomArmyConsumption({
                kingdomActor: this.actor,
                game: this.game,
                forceUpdate: true,
            });
        }
        await saveKingdom(this.actor, {
            fame: {
                ...this.kingdom.fame,
                now: clamped(this.kingdom.fame.now, 0, this.data.maxFamePoints),
                next: clamped(this.kingdom.fame.next, 0, this.data.maxFamePoints),
                max: Math.max(this.data.maxFamePoints, 0)
            }
        })
    }
}


export function showKingdomSettings(options: KingdomSettingOptions): void {
    new KingdomSettings(options).render(true);
}