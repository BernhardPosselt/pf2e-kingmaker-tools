# Kingmaker Tools (Unofficial)

This module ships macros, journal entries, roll tables, effect items, additional tooling for OGL/CUP licensed content
from the Kingmaker Adventure Path for Pathfinder 2nd Edition.

## Licensing

All Pathfinder content uses the [Open Gaming License](./OpenGameLicense.md) and [CUP](https://paizo.com/community/communityuse)

> This FoundryVTT module uses trademarks and/or copyrights owned by Paizo Inc., used under Paizo's Community Use
> Policy (paizo.com/communityuse). We are expressly prohibited from charging you to use or access this content. This
> FoundryVTT module is not published, endorsed, or specifically approved by Paizo. For more information about Paizo Inc.
> and Paizo products, visit paizo.com.

The source code is licensed under the AGPLv3 license.

All images in the [static/img/structures](static/img/structures) folder are licensed under [CC0 - Public Domain](https://creativecommons.org/publicdomain/zero/1.0/) by [Mark Pearce](https://github.com/MarkPearce). They were [generated and retouched using a MidJourney subscriber account](https://github.com/BernhardPosselt/pf2e-kingmaker-tools/issues/76). According to their [Terms of Service](https://docs.midjourney.com/docs/terms-of-service), subscribers to MidJourney that are not part of a company own all the generated images.

## Usage

This module ships a journal named **Kingmaker Tools Manual** which includes all of the instructions on how to use this module. It should automatically open once you enable the module for the first time. You can find it under the compendium tab in the **Kingmaker Tools** > **Journals** compendium.

If you have issues, consider dropping into our [Discord channel](https://discord.com/channels/880968862240239708/1079113556823396352)

## Installation

The package is available through the [module registry](https://foundryvtt.com/packages/pf2e-kingmaker-tools)

### Git

Clone this repository into your installation's module folder:

    cd ~/.local/share/FoundryVTT/Data/modules
    git clone https://github.com/BernhardPosselt/pf2e-kingmaker-tools.git 
    yarn install
    yarn run build

If your Foundry instance is running, you need to restart it to clear its module cache.

Run the following before packaging once and set your paths:

    ./node_modules/.bin/fvtt configure
    ./node_modules/.bin/fvtt configure set installPath /home/bernhard/dev/FoundryVTT-11.301
    ./node_modules/.bin/fvtt configure set dataPath /home/bernhard/.local/share/FoundryVTT/Data

### Functionality

This module ships with implementations for all the extra mechanics provided in the 2e Kingmaker Adventure Path, including optional and popular homebrew from Vance & Kerenshara:

* Camping Sheet
* Kingdom Sheet
* Weather
* Level 1 Statblocks for companions not included in the Companion Guide

Furthermore, this module provides:

* Various macros that I use in my own game
* Journals containing various rules and tips
* Combat Track integration

### House Rules & GM Tips

You can look up my tips and house rules [here](./docs/house-rules.md)

### Official Kingmaker Module Integration

If you've enabled the official module, the following things are automatically taken care of:

* Token Mappings for all companions and structures except for a few exceptions (see Manual journal)
* Weather Sound Effects for rain, rainstorms, blizzard and snow
* Default and Region combat tracks
* Rolltable integration for:
    * Kingdom Events
    * Random Encounters
    * Cult Events

### Screenshots

![kingdom-sheet.png](docs%2Fimages%2Fkingdom-sheet.png)

![settlements.png](docs%2Fimages%2Fsettlements.png)

![camping-sheet-1.png](docs%2Fimages%2Fcamping-sheet-1.png)
