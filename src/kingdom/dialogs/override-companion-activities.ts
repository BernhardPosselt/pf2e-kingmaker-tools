import {getStringArraySetting, setSetting} from '../../settings';
import {isCompanionName} from '../data/companions';

interface CompanionData {
    companions: {
        amiri: boolean,
        ekunday: boolean,
        harrim: boolean,
        jaethal: boolean,
        jubilost: boolean,
        kalikke: boolean,
        kanerah: boolean,
        linzi: boolean,
        noknok: boolean,
        octavia: boolean,
        regongar: boolean,
        tristian: boolean,
        valerie: boolean,
    };
}

export class CompanionLeadershipBenefits extends FormApplication<FormApplicationOptions, object, null> {
    constructor() {
        super(null, {});
    }

    static override get defaultOptions(): FormApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'kingdom-override-companion-benefits';
        options.title = 'Override Companion Leadership Benefits';
        options.template = 'modules/pf2e-kingmaker-tools/templates/kingdom/override-companion-benefits.hbs';
        options.submitOnChange = true;
        options.closeOnSubmit = false;
        options.classes = [];
        options.height = 'auto';
        options.width = 200;
        return options;
    }

    override getData(options?: Partial<FormApplicationOptions>): Promise<CompanionData> | CompanionData {
        if (game instanceof Game) {
            const settings = new Set(getStringArraySetting(game, 'forceEnabledCompanionLeadershipBenefits')
                .filter(isCompanionName));
            return {
                companions: {
                    amiri: settings.has('Amiri'),
                    ekunday: settings.has('Ekundayo'),
                    harrim: settings.has('Harrim'),
                    jaethal: settings.has('Jaethal'),
                    jubilost: settings.has('Jubilost'),
                    kalikke: settings.has('Kalikke'),
                    kanerah: settings.has('Kanerah'),
                    linzi: settings.has('Linzi'),
                    noknok: settings.has('Nok-Nok'),
                    octavia: settings.has('Octavia'),
                    regongar: settings.has('Regongar'),
                    tristian: settings.has('Tristian'),
                    valerie: settings.has('Valerie'),
                },
            };
        } else {
            throw new Error('This should not happen');
        }
    }

    protected async _updateObject(event: Event, formData: object): Promise<void> {
        const result = expandObject(formData) as CompanionData;
        const overriden: string[] = [
            ...(result.companions.amiri ? ['Amiri'] : []),
            ...(result.companions.ekunday ? ['Ekundayo'] : []),
            ...(result.companions.harrim ? ['Harrim'] : []),
            ...(result.companions.jaethal ? ['Jaethal'] : []),
            ...(result.companions.jubilost ? ['Jubilost'] : []),
            ...(result.companions.kalikke ? ['Kalikke'] : []),
            ...(result.companions.kanerah ? ['Kanerah'] : []),
            ...(result.companions.linzi ? ['Linzi'] : []),
            ...(result.companions.noknok ? ['Nok-Nok'] : []),
            ...(result.companions.octavia ? ['Octavia'] : []),
            ...(result.companions.regongar ? ['Regongar'] : []),
            ...(result.companions.tristian ? ['Tristian'] : []),
            ...(result.companions.valerie ? ['Valerie'] : []),
        ];
        console.log(game, overriden, formData, result);
        if (game instanceof Game) {
            await setSetting(game, 'forceEnabledCompanionLeadershipBenefits', overriden);
        }
    }
}
