class Hooks {
    static on(key) {
    }
}

const foundry = {
    ux: {},
    abstract: {
        DataModel: class {

        }
    },
    utils: {
        expandObject: () => {
        }
    },
    data: {
        fields: {}
    },
    helpers: {},
    applications: {
        sidebar: {
            ActorDirectory: class {}
        },
        ui: {
            Hotbar: class {}
        },
        ux: {
            TextEditor: {
                implementation: class {
                }
            }
        },
        handlebars: {},
        api: {
            HandlebarsApplicationMixin: (klass) => {
                return class extends klass {
                }
            },
            ApplicationV2: class {
            },
            DocumentSheetV2: class {
            }
        }
    }
}