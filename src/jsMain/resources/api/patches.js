globalThis.foundryvttKotlinPatches = {};

((exports) => {
    /**
     * There's some weird stuff going on in V2 APIs; this mixin seeks to solve 2 issues:
     * * Allow you to pass PARTS as constructor parameters instead of requiring statics
     * * Automatically sets a submit handler if a form property is present to an onSubmit() method
     */
    function SaneHandlebarsApplicationV2Mixin(clazz) {
        function copy(src, target) {
            Object.keys(src).forEach(key => {
                target[key] = src[key];
            });
        }

        // remove parts from super parameters and store them in a tmp variable instead
        const parts = {};

        function copyParts(config, parts) {
            if (config.parts) {
                copy(config.parts, parts)
                delete config['parts'];
            }
            return config
        }

        return class Hack extends foundry.applications.api.HandlebarsApplicationMixin(clazz) {

            constructor(config) {
                super(
                    copyParts({
                        ...config,
                        form: config.form !== undefined ?
                            {...config.form, handler: Hack._onSubmit}
                            : undefined,
                    }, parts)
                );
                this.constructor.PARTS = {}
                copy(parts, this.constructor.PARTS)
            }

            static async _onSubmit(event, form, formData) {
                await this.onSubmit(event, form, formData)
            }

            async onSubmit(event, form, formData) {
            }
        }
    }

    class SaneHandlebarsApplicationV2 extends SaneHandlebarsApplicationV2Mixin(foundry.applications.api.ApplicationV2) {
    }

    exports.SaneHandlebarsApplicationV2 = SaneHandlebarsApplicationV2;

    Hooks.on('init', () => {
        exports.rolls = {};
        CONFIG.Dice.rolls.forEach(mode => {
            exports.rolls[mode.name] = mode
        })
    });
})(globalThis.foundryvttKotlinPatches)