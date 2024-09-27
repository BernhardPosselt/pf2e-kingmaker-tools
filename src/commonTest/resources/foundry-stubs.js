class Hooks {
    static on(key) {
    }
}

const foundry = {
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
    applications: {
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