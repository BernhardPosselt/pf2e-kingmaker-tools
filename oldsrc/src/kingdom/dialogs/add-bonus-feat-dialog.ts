import {KingdomFeat} from '../data/feats';
import {BonusFeat} from '../data/kingdom';


type AddBonusFeatOptions = {
    feats: KingdomFeat[]
    onOk: (feat: BonusFeat) => void;
};

export class AddBonusFeatDialog extends FormApplication<FormApplicationOptions & AddBonusFeatOptions, object, null> {
    private feats: KingdomFeat[];
    private onOk: (feat: BonusFeat) => void;
    private selected = '-';

    static override get defaultOptions(): FormApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'kingdom-add-bonus-feat';
        options.title = 'Kingdom';
        options.template = 'modules/pf2e-kingmaker-tools/templates/kingdom/add-bonus-feat.hbs';
        options.submitOnChange = true;
        options.closeOnSubmit = false;
        options.classes = [];
        options.height = 'auto';
        return options;
    }

    constructor(object: null, options: Partial<FormApplicationOptions> & AddBonusFeatOptions) {
        super(object, options);
        this.feats = options.feats;
        this.onOk = options.onOk;
    }

    override getData(options?: Partial<FormApplicationOptions & { feats: KingdomFeat[] }>): Promise<object> | object {
        return {
            feats: this.feats,
            selected: this.selected,
            feat: this.feats.find(feat => feat.name === this.selected),
            ...super.getData(options),
        };
    }

    protected async _updateObject(event: Event, formData: { selected: string }): Promise<void> {
        this.selected = formData.selected;
        this.render();
    }

    override activateListeners(html: JQuery): void {
        super.activateListeners(html);
        const $html = html[0];
        $html.querySelector('button')?.addEventListener('click', async () => {
            const selectedFeat = this.feats.find(feat => feat.name === this.selected)!;
            this.onOk({
                id: selectedFeat.name,
            });
            await this.close();
        });
    }
}
