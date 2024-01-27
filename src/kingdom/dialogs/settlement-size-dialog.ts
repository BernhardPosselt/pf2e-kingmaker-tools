import {settlementTypeData} from '../structures';

class SettlementSizeDialog extends Application {

    /** @inheritdoc */
    static get defaultOptions(): ApplicationOptions {
        return foundry.utils.mergeObject(super.defaultOptions, {
            template: 'modules/pf2e-kingmaker-tools/templates/kingdom/settlement-size-help.hbs',
            classes: ['dialog'],
            width: 'auto',
            jQuery: false,
        });
    }

    getData(): object | Promise<object> {
        return {
            data: settlementTypeData.map(s => {
                return {
                    type: s.type,
                    blocks: s.maximumLots,
                    population: s.population,
                    level: s.levelFrom && s.levelTo ? (s.levelFrom !== s.levelTo ? s.levelFrom + '-' + s.levelTo : s.levelFrom) : s.levelFrom + '+',
                    consumption: s.consumption,
                    maxItemBonus: '+' + s.maxItemBonus,
                    influence: s.influence === 1 ? '1 hex' : s.influence + ' hexes',
                };
            }),
        };
    }
}


export function settlementSizeDialog(): void {
    new SettlementSizeDialog().render(true);
}