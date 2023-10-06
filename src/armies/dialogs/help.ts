class ArmyHelpApplication extends Application<ApplicationOptions> {

    static override get defaultOptions(): ApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'help-army-app';
        options.title = 'Help';
        options.template = 'modules/pf2e-kingmaker-tools/templates/army/help.hbs';
        options.classes = ['kingmaker-tools-app', 'help-army-app'];
        options.width = 500;
        options.height = 'auto';
        return options;
    }

    constructor(options: Partial<ApplicationOptions>) {
        super(options);
    }
}

export function showArmyHelpDialog(): void {
    new ArmyHelpApplication({}).render(true);
}
