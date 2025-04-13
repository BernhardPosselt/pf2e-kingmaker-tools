# PFRPG 2e: Kingdom Building, Camping & Weather

This module ships all OGL licensed rules to run the Kingdom, Camping and Weather rule systems for the most popular adventure for PFRPG 2e.

**Documentation is included in a journal inside FoundryVTT!**

## Licensing

All PFRPG 2e content uses the [Open Gaming License](./OpenGameLicense.md). If you find non OGL content, please file an
issue and I'll get it removed ASAP. I've tried my best to scrub existing content, but there might still be leftovers.

The source code is licensed under the AGPLv3 license, except for
the [src/jsMain/kotlin/com/foundryvtt](./src/jsMain/kotlin/com/foundryvtt) folder which is licensed
under [Apache License 2.0](./src/jsMain/kotlin/com/foundryvtt/LICENSE).

All images in the [img/structures](./img/structures),  [img/kingdom/backgrounds](./img/kingdom/backgrounds) and [img/camping/backgrounds](./img/camping/backgrounds) are
licensed under [CC0 - Public Domain](https://creativecommons.org/publicdomain/zero/1.0/)
by [Mark Pearce](https://github.com/MarkPearce). They
were [generated and retouched using a MidJourney subscriber account](https://github.com/BernhardPosselt/pf2e-kingmaker-tools/issues/76).
According to their [Terms of Service](https://docs.midjourney.com/docs/terms-of-service), subscribers to MidJourney that
are not part of a company own all the generated images.

All images in [img/settlements/backgrounds](./img/settlements/backgrounds) are
licensed under [CC0 - Public Domain](https://creativecommons.org/publicdomain/zero/1.0/)
by dbavirt on Discord.

## Installation

The package is available through the [module registry](https://foundryvtt.com/packages/pf2e-kingmaker-tools)

## Functionality

This module ships with implementations for all the extra mechanics provided in the best sandbox PFRPG 2e adventure,
including optional and popular homebrew from Vance & Kerenshara:

* Camping Sheet
* Kingdom Sheet
* Weather
* Combat Tracks

### House Rules & GM Tips

You can look up my tips and house rules [here](./docs/house-rules.md)

### Official Module Integration

If you've enabled the official module, the following things are automatically taken care of:

* Token Mappings for all companions and structures
* Weather Sound Effects for rain, rainstorms, blizzard and snow
* Default and Region combat tracks
* Rolltable integration for:
    * Kingdom Events
    * Random Encounters
    * Cult Events

### Screenshots

![Kingdom Sheet](./img/kingdom/docs/kingdom-sheet.webp)

![Settlements](./img/kingdom/docs/settlement.webp)

![camping sheet](./img/camping/docs/camping-activities.webp)

## Development

If you are interested in hacking on the code base, take a look at the [Kotlin JS Primer](./docs/Kotlin%20JS%20Primer.md)
for a quick intro on how to interact with the js api.

### Setup

Install the following things:

* JDK 21
* git
* node
* yarn

First, clone the repository into a local folder, e.g. **/home/bernhard/dev**:

    cd /home/bernhard/dev
    git clone https://github.com/BernhardPosselt/pf2e-kingmaker-tools.git 

Then link this directory to your foundry data folder:

    ln -s /home/bernhard/dev/pf2e-kingmaker-tools/ /home/bernhard/.local/share/FoundryVTT/Data/modules/pf2e-kingmaker-tools/

Pull all language files from Transifex:

    ./gradlew txPull

Run the package task to build everything from scratch:

    ./gradlew build

Then, you can keep building the project using:

    ./gradlew assemble

or if you want to both build it and run its tests:

    ./gradlew build

To execute tests run:

    ./gradlew jsTest

Finally, start foundry

    cd dev/FoundryVTT-12.330/
    ./foundryvtt

You can release a new version by changing the version in **build.gradle.kts** and then executing:

    GITHUB_TOKEN="token_here" FOUNDRY_TOKEN="token_here" ./gradlew release

### Enable Schema Autocompletion Support in IntelliJ

Some files are in JSON rather than actual code. To get autocompletion for these in IntelliJ, you need to enable custom
schemas.

In settings, go to **Languages & Frameworks > Schemas and DTDs > JSON Schema Mappings**.

Click on the + to add a new mapping for each schema. Then add the following (see a list of values further down below):

* **Name**: Name of the Schema
* **Schema file or URL**: Path to the Schema file in [./schemas/](./schemas/)
* **Schema Version**: Always **JSON Schema version 7**
* Then click on the **+** below and **Directory**

Example:

* : Recipes:
    * **Name**: Recipes
    * **Schema file or URL**: schemas/recipes.json
    * **Directory**: data/recipes

## Translations

### Help out as a Translator

If you want to help translating this module as a Translator open an issue on GitHub with your email and language or contact me on Discord, so I can send you an invite to the project on Transifex.

Don't edit the files in **lang/** directly. Every file except for **en.json** will be overridden by changes from Transifex. Instead, edit the translations in Transifex, then pull and build the project to get a preview:

    ./gradlew txPull
    ./gradlew assemble

Note that if you want to preview changes locally, you need to follow the **Setup** mentioned above.

When you pull new code and there is a conflict in a file **other than en.json**, always opt to throw away your local changes. 

### Help out as a Developer

This module skips the built-in Foundry translation system since it's broken and unusable. Nonetheless, you need to link your json files in the module.json file for each language, otherwise Foundry will not let you change your language in the settings.

#### Making Changes in Code

Translations are persisted in **lang/en.json**. **DO NOT EDIT other translation files directly since they will be overridden by changes from Transifex**. You can arbitrarily nest JSON values and reference them using the path. For instance: 

```json
{
  "key": {
    "something": "value"
  }
}
```

would be referenced using **key.something**. Translations can be parameterized:

```json
{
  "key": {
    "something": "{greeting} to you"
  }
}
```

Plurals can be translated using [ICU](https://unicode-org.github.io/icu/userguide/format_parse/messages/) (with **coins** being passed as an int parameter):

```json
{
  "key": {
    "something": "He paid {coins, plural, =0 {nothing} =1 {one coin} =other{# coins}} for his groceries"
  }
}
```

It's also possible to use different translations based on a parameter, e.g. gender (with **gender** being passed as either "male" or "female"; other is the catchall case):

```json
{
  "key": {
    "something": "{gender, select, female {She} male {He} other {They}} went to the party"
  }
}
```

There are 2 places where you can translate strings:

In Kotlin code:

```kt
// single value
t("key.something")
// values with context
t("key.something", recordOf("greeting" to "hello"))
```

In Handlebars templates:

```handlebars
{{localizeKM "key.something"}}
{{localizeKM "key.something" greeting="hello"}}
```

#### Pushing Changes

Strings are not edited in the repository. Instead, they are pushed to Transifex first, edited and then pulled.

In order to pull/push, [you first need to set up a token on transifex](https://app.transifex.com/user/settings/api/) by clicking on Generate a token.

Then create a **~/.transifexrc** in your home directory (or **C:\\Users\\USERNAME\\.transifexrc** on Windows) with the token:
```ini
[https://app.transifex.com]
rest_hostname = https://rest.api.transifex.com
token         = TOKEN_HERE
```

You can also persist it in the **TX_TOKEN** environment variable.

Then you can edit **lang/en.json** as normal. Once you are done with your changes, you need to push it to your translators using:

    ./gradlew txPush

To download the latest translations, use

    ./gradlew txPull