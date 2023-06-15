interface CampingOptions extends ApplicationOptions {
    game: Game;
}
export class CampingSheet extends Application<CampingOptions> {
    static override get defaultOptions(): ApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'camping-app';
        options.title = 'Camping Sheet';
        options.template = 'modules/pf2e-kingmaker-tools/templates/camping/sheet.hbs';
        options.classes = ['kingmaker-tools-app', 'camping-app'];
        options.width = 850;
        options.height = 'auto';
        return options;
    }

    override getData(options?: Partial<ApplicationOptions>): object | Promise<object> {
        return super.getData(options);
    }

    override activateListeners(html: JQuery): void {
        super.activateListeners(html);
    }
}

export function openCampingSheet(game: Game): void {
    new CampingSheet({game}).render(true);
}
