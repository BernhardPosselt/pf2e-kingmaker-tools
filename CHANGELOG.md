# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

* Regions and Climate can now be configured to use your own settings. This allows you to have custom regions with custom
  weather, combat tracks and random encounter tables
* Added a custom Subsist macro that works off the current region and allows you to add Provisions items to your
  inventory. Provisions are used up before rations and removed after resting. Automation is included for Coyote Cloak
  and Forager
* Recipe healing, damaging and condition reducing effects are implemented, automating all remaining recipes
* There is now a way to increase the number of fame points
* Campsite Positions are now remembered on each scene so you can reuse their last result if it wasn't a critical failure

### Removed

* Remove token mappings from GMG npcs to not conflict with NPC Core
* This release marks a departure from Paizo IP to OGL only content due improve licensing compatibility. This primarily
  means that some names and setting information had to be removed or changed (
  e.g. the First World has been replaced with "A Supernatural Place" or "Galt Ragout" is now just "Ragout")
* Removed additional Companion actors: ultimately too difficult to maintain due to Remaster changes and new content
  releases in addition to Paizo IP
* Removed various journal entries which are all covered on existing sheets
* Removed Companion Influence and Discover camping activities. You have to add them on your own now
* Removed the ability to override combat tracks by region, e.g. by creating a playlist called **Kingmaker.Greenbelt**.
  Instead, simply configure these on the camping sheet

### Changed

* Meal Effects are now removed right before Prepare Campsite RAW
* You can have more than one meal effect active at a time which at the same time means that eating a meal won't override
  the previous meal anymore.
* Instead of a single meal, players can now choose from the entire array of available recipes RAW
* If you don't use the official module, you will need to:
    * Set your combat tracks and random encounters manually in camping sheet
* All companion names have been removed from camping activities, kingdom events and kingdom activities
* Companion Kingdom Activities are now handled like all other activities, meaning: you don't have to force override them
  to enable them without companion leaders. However, they are not migrated and don't automatically enable invested and
  benefits anymore, when you assign a companion to a leadership position. Enable/Disable them like normally in your turn
  tab. Regarding Octavia, there's a kingdom sheet setting now to enable that benefit
* The following camping activities have been renamed:
    * Dawnflower's Blessing -> Healer's Blessing
* The following recipes have been renamed
    * Broiled Tuskwater Oysters -> Broiled Oysters
    * First World Mince Pie -> Supernatural Mince Pie
    * Galt Ragout -> Ragout
    * Giant Scrambled Egg With Shambletus -> Giant Scrambled Egg
    * Kameberry Pie -> Berry Pie
    * Whiterose Oysters -> Oysters
    * Owlbear Omelet -> Omelet
* The following Kingdom Activities have been renamed:
    * Restore the Temple of the Elk -> Restore the Temple
    * Harvest Azure Lily Pollen -> Harvest Lily Pollen
* Migrations are not run anymore if you've updated from a version prior to 0.12.2 (released in October 2023). If you've
  upgraded from such a version, download to the latest version for your release that still ships these (1.1.1 for V12 or
  0.46.2 for V11), then upgrade to this release
* If you have multiple kingdom sheets, migrations will be run for all of them; you can still only have 1 camping sheet
* Combat Tracks can now select a track on a playlist in addition to selecting a playlist
* Camping Sheet and almost all macros are now based on ApplicationV2 which support a dark and light theme out of the box
* Changing the degree of success dropdown for activities as well as recipes posts all associated messages to chat now

## Fixed

* Lots are not calculated from token size anymore allowing you to use a finer grained grid
* Private camping rolls are now hidden from players again

## [1.1.1] - 2024-07-17

### Fixed

* When upgrading from V11, correctly migrate modifiers to Recover Army to separated activities to not break the kingdom
  sheet

## [1.1.0] - 2024-07-09

### Changed

* Mark Pearce [contributed CC licensed](https://github.com/BernhardPosselt/pf2e-kingmaker-tools/issues/76) images for
  the missing structures. You will need to reimport these structures to make use of the new images. Keep in mind that
  you also need to replace all existing tokens after deleting their actors.

## [1.0.4] - 2024-07-07

### Fixed

* Only add Inspiring Entertainment bonus if kingdom has at least 1 unrest

## [1.0.3] - 2024-07-05

### Changed

* When activating settlement scenes through the kingdom sheet using ctrl + click, change the active settlement in the
  sheet to the scene

### Fixed

* Do not show ruin and consumption structure information once per player

## [1.0.2] - 2024-06-30

### Fixed

* Garrison now prints unrest reduction to chat

## [1.0.1] - 2024-06-30

### Added

* Add min lots filter to structure browser
* Manage Trade Agreements and Trade Commodities activities buttons now ask how many times they should be performed
* Show hints and buttons when creating a structure token on a sheet that reduces or increases unrest or ruin

### Fixed

* Do not show missing message when reducing more ruin or unrest than present

## [1.0.0-beta7] - 2024-06-19

### Changed

* When no actor selected the cook activity in the camping sheet, show a hint

### Fixed

* Make chat buttons work without refreshing the browser window when no camping nor kingdom actor is present
* When no settlements exist, make first added settlement the currently selected one

## [1.0.0-beta6] - 2024-06-16

### Fixed

* Automatically remove Tell Campfire Story effects after daily preparations
* Automatically toggle Tell Campfire Story effects during combat
* Sum up rations, special ingredients and basic ingredients correctly if more than one item is in the player's inventory
* Correctly update consumed rations and ingredients on the sheet when an actor is removed or added

## [1.0.0-beta5] - 2024-06-02

### Fixed

* Fix camping activities not letting you unselect results

## [1.0.0-beta4] - 2024-06-02

### Fixed

* Fix recover army structure bonuses; you need to manually migrate **recover-army** rule element targets to:
    * **recover-army-damaged**
    * **recover-army-defeated**
    * **recover-army-lost**
    * **recover-army-mired-pinned**
    * **recover-army-shaken**
    * **recover-army-weary**

## [1.0.0-beta3] - 2024-06-02

### Fixed

* Do not list already known tactics in army tactic browser
* Disable roll buttons for camping activities if no actor is set
* Improve Camping Sheet UI to be easier to use on first run

## [1.0.0-beta2] - 2024-05-22

### Changed

* Split Recover Army into separate instances to avoid confusing skill lookups
* Add button to import structures in structure browser if none are present

### Added

* Add Kingdom Sheet automation for Mired, Weary and Scouting DC by selecting tokens
* Added a Tactics Browser for Train Army
* Added Army Browser to Recruit Army

### Fixed

* Do not break roll popup when modifiers have non ASCII names

## [1.0.0-beta1] - 2024-05-18

### Added

* Support for Foundry V12

### Changed

* Make structure browser prettier

### Fixed

* Ignore unlinked armies when calculating army consumption
* disable army consumption input when auto calculating army consumption

### Removed

* Remove army implementation

## [0.46.2] - 2024-05-17

### Fixed

* Fix bonus feat modifiers not being used

## [0.46.1] - 2024-05-17

### Changed

* Remove house icon from housing

## [0.46.0] - 2024-05-17

### Changed

* List residential lots in settlements list
* Display Secondary Territory issues in list
* Replace issues with icons

## [0.45.0] - 2024-05-13

### Added

* This release achieves 100% compatibility with V&K rules except for turning a feat's status bonuses into circumstance
  bonuses instead of letting them stack with other status bonuses
* Added the V&K version of Practical Magic
* Added the V&K version of Request Foreign Aid; in either case you need to manually enable/disable either activity in
  the **Manage Activities** button at the top of the **Turn** section

## [0.44.0] - 2024-05-04

### Added

* Add a button to reset camping activities
* Also display building from ruin cost when building structures

### Fixed

* When clicking Gain 1 Fame button on a critical success, a message is posted to chat as usual
* Re-Rolling Build Structure actions now correctly re-posts the structure build result message

## [0.43.0] - 2024-03-16

### Fixed

* Do not hide camping settings button if no actors are on the sheet
* Correctly link V&K structures in upgrades

### Changed

* Remove the sheet actor compendium and instead create the actor programmatically when clicking on the camping/kingdom
  macro

## [0.42.0] - 2024-03-08

### Added

* Kingdom Sheet check popup allows you to enter the roll mode
* Structure browser allows you to select the active settlement
* Add tabs for upgradable and free (slowed) structures

## [0.41.0] - 2024-02-27

### Added

* Add button to gain unrest or reduce RP when not having enough food during upkeep
* Add water borders to settlement info popup

### Changed

* Do not show Rubble in structure browser
* Improve docs on how the module integrates with the official module

## [0.40.0] - 2024-02-23

### Added

* Use wild card images for houses, mill, cemetery and opera house tokens
* Show structure images in structure browser

### Fixed

* Fix Structure Token Mapping macro to use correct cemetery and waterfront images
* Fix Cemetery token typo

## [0.39.0] - 2024-02-23

### Fixed

* Fix Cemetery structure name
* Fix Cemetery and Waterfront images that were renamed with the latest Kingmaker update. You will have to either fix
  these manually in your worlds or reimport and replace them on your scenes

### Changed

* Move random encounter settings into camping sheet

## [0.38.0] - 2024-02-17

### Changed

* Remove average party level setting, instead take the highest level of a character on the party actor

## [0.37.0] - 2024-02-16

### Added

* Display roll modifiers in chat
* You can now play a playlist when clicking the Begin Rest button on the Camping Sheet
* You can now move gathered ingredients to the party sheet
* You can now consume ingredients and rations from the party actor as well

## [0.36.0] - 2024-02-15

### Added

* Re-roll using supernatural solution or creative solution
* Automate Cooperative Leadership

## [0.35.0] - 2024-02-13

### Changed

* Do not parse hidden tiles or drawings when using tile/drawing based worksite, size & farmland automation
* **Roll Perception Exploration** and **Roll Stealth Exploration** now use all characters owned by players in a world
  except for all characters in the active scene and add the appropriate action roll options

## [0.34.0] - 2024-02-11

### Changed

* Do not count armies with linked actor tokens multiple times for calculating army consumption
* Do not count armies that are not owned by players for army consumption

### Fixed

* Fixed perception scaling for armies

## [0.33.0] - 2024-02-09

### Added

* Award Hero Point macro

### Changed

* Ask for confirmation before running reset hero point macro

## [0.32.0] - 2024-02-09

### Added

* Army consumption is now also calculated from visible built in army tokens on scenes

## [0.31.0] - 2024-02-09

### Added

* You can now also edit homebrew camping activities

### Fixed

* Prevent homebrew kingdom activities from being overriden

## [0.30.0] - 2024-02-09

### Added

* Added a way to track Food in addition to Farmlands; Food reduces consumption by 1 in claimed hexes similar to
  Farmlands
* Camping activities can now be overridden by adding a new activity with the same name as an existing one

### Changed

* You can now override existing camping activities by adding a new activity with the same name

## [0.29.1] - 2024-02-09

### Fixed

* Remove double none dc setting for kingdom activities and add **custom** which sets the dc to 0

## [0.29.0] - 2024-02-08

### Added

* Show consumption surplus for farms in Status tab and for settlements in settlement detail view
* Homebrew Camping activities can now add more than 1 effect

### Fixed

* Fixed Hearty Meal also applying to saving DC values

### Changed

* Split up Relaxed into 2 effects so that modifier can be removed after first use
* Simplify skill selects in kingdom activity and camping activity dialogs

## [0.28.4] - 2024-02-06

### Fixed

* Fix edit structure rules macro; you will have to reimport it

## [0.28.3] - 2024-02-03

### Fixed

* Move add stolen lands scene button to bottom to prevent accidentally clicking on it
* Properly hide said button for non GMs

## [0.28.2] - 2024-02-03

### Fixed

* Really run migration

## [0.28.1] - 2024-02-03

### Fixed

* More details in journal on how to set up a capital
* Run migration to set real scene id to null if not present

## [0.28.0] - 2024-02-03

### Changed

* Re-organize kingdom sheet
* Worksites, farmlands and kingdom size can now be tracked in 3 different ways in the kingdom sheet settings (**READ THE
  MANUAL JOURNAL!**):
    * Official Module: Use the official module's hex map
    * Tile/Drawing Based: Use tiles/drawings as worksites, claimed hexes and farmlands
    * Manual: Manage everything manually
* Remove gain/lose size buttons from chat

## [0.27.1] - 2024-02-02

### Added

* Testing automated foundry release API

## [0.27.0] - 2024-02-02

### Added

* Setting to automate Ekundayo's -2 to lumber buildings reduction
* Also post description of activity to chat to display resource buttons
* Add buttons to gain commodities in Purchase Commodities result

### Fixed

* Make changing degree of success work again on kingdom activity results

## [0.26.1] - 2024-01-28

* Fix title of Take Charge activity

## [0.26.0] - 2024-01-27

### Changed

* Move Features tab into Feats

### Added

* Kingdom Sheet Notes tab
* Settlement Size help dialog
* Kingdom Size help dialog

## [0.25.0] - 2024-01-26

### Added

* Add setting to always enable capital investment in the capital despite not having a bank

## [0.24.0] - 2024-01-25

### Changed

* Obsolete the token mappings module and integrate functionality into the core module
* Configure token mappings if kingmaker module is not activated or installed by default
* Use the Bridge.webp image for Bridge, Stone structure as well

### Added

* Added support for homebrew kingdom activities
* Include macro to change token images of all imported structures

## [0.23.4] - 2024-01-23

### Changed

* Make negotiation dc and group name editable
* Fix wording in settlement view

## [0.23.3] - 2024-01-21

### Fixed

* Fix vertical scroll bars in camping and kingdom sheet

### Changed

* Style proficiency based on rank

## [0.23.2] - 2024-01-21

### Fixed

* Fix possible Forge cache issues

## [0.23.1] - 2024-01-21

### Fixed

* Fix typo in settlement navigation

### Changed

* Sort buildings in settlement view alphabetically

### Added

* Pressing the control key while clicking on a settlement scene activates it instead of viewing it

## [0.23.0] - 2024-01-21

### Added

* When inspecting a settlement, show a list of all built structures

### Fixed

* Wooden Watchtowers stack with Stone Watchtowers again
* Re-render kingdom sheet when a scene is deleted to fix issues with settlement list getting out of sync
* Use more precise wording for help in the civic phase section

### Changed

* Set structure name visibility to Hover by Anyone
* Improve block tile graphics
* Ship 1x4 and 4x1 block graphics
* Settlement dialog now uses tabs for navigation

## [0.22.0] - 2024-01-20

### Added

* Add upgrade to and from filters in structure browser

### Changed

* Reduce settlement scene size by 80%

## [0.21.5] - 2024-01-20

### Fixed

* Use smaller margin of error for placing structure on a block (10% instead of 20%)
* Do not add grid size again when calculating block positions preventing structures being counted as inside the block
  when placing them near the block's edges

## [0.21.4] - 2024-01-20

### Fixed

* Fix settlement name typos
* Fix settlement level calculation in blocks other than the top left one

## [Unreleased]

## [0.21.3] - 2024-01-20

### Fixed

* Disable token vision in settlement scenes

## [0.21.2] - 2024-01-20

### Fixed

* Package image folder

## [0.21.1] - 2024-01-20

### Fixed

* Package image folder

## [0.21.0] - 2024-01-20

### Added

* Ship a Settlement Scenes compendium
* Add settlement level automation. You will need to manually migrate your settlement scenes to make use of this
  automation or turn it off in the kingdom sheet settings. Consult the Kingmaker Tools Manual journal (Help button in
  the title bar of your kingdom sheet) for more information on how to use it.

### Changed

* Order compendium folders by name

### Fixed

* Fix prototype tokens for structures to show no HP bars and display their name for V&K buildings, Waterfront and
  Monument

## [0.20.2] - 2024-01-19

### Fixed

* Added inline help in civic activities section describing how to use structures

## [0.20.1] - 2024-01-19

### Fixed

* Create Chatmessage when clicking gain 1 fame button to mirror other upkeep activity buttons
* Replace Full Costs with better naming: Build Directly
* New Leadership - Critical Success effect now correctly lasts until the end of your next turn

## [0.20.0] - 2024-01-18

### Added

* Ignore structures with the slowed condition from available structures

## [0.19.0] - 2024-01-17

### Added

* Added support for calculating building upgrade costs

### Changed

* Remove pay button from sheet and instead post pay buttons to chat
* Updated journal to include structure browser changes

## [0.18.1] - 2024-01-15

### Fixed

* Fixed edit structures macro not persisting data on the base actor

## [0.18.0] - 2024-01-15

### Added

* Structure Browser which lets you filter structures, roll checks to build and pay costs replaces the **Build Structure
  ** activity popup. Structures are gathered from all imported structures, so you might need to import them from the
  compendium
* Separate structures for Watchtowers and Bridges built out of stone

### Changed

* Add the following [additional required attributes to structures](docs/structure-rules.md) (you may need to edit your
  custom structures to make things work again):
    * **construction**
* Add the following additional required attributes to structures:
    * **affectsEvents**
    * **affectsDowntime**
    * **reducesUnrest**
    * **reducesRuin**
    * **upgradesFrom**
* Use simple NPC sheet for structures
* Add more attributes onto the structures; you might need to edit your custom structures to adjust them
* Move kingdom and camping sheet npc actors to separate compendium

### Fixed

* Allow optional **isBridge** attribute for custom structures as documented

## [0.17.1] - 2024-01-14

### Fixed

* Clicking on the settlement link will not open a new browser tab anymore

## [0.17.0] - 2024-01-14

### Added

* Make it possible to view settlement scenes by clicking on their name in the kingdom sheet settlement list
* Added a setting to disable activity skill proficiency requirements

### Fixed

* Fame/Infamy points are now correctly reset to 0 at the end of the turn

## [0.16.0] - 2023-12-15

### Changed

* Require 5.11 as minimum 2e version
* Correctly play Dunsward combat track

### Fixed

* Fix camping sheet; 2e system data model renamed charges to uses

## [0.15.1] - 2023-12-01

### Fixed

* Play Glenebon Uplands combat track in western parts of the map

## [0.15.0] - 2023-11-24

### Fixed

* Link Pinned condition in Mired condition

### Added

* Added Manual journal that includes help on how to use armies, camping and kingdom building
* Open Manual journal on first launch

### Removed

* Remove army help macro

## [0.14.0] - 2023-11-12

### Changed

* Errataed the Feint Army action to be a Maneuver and remove the Attack trait
* Migrate to Remaster
* Require 5.9.1 as minimum pf2e version

## [0.13.1] - 2023-11-12

### Fixed

* Add Fortified Critical Success for Garrison Army

## [0.13.0] - 2023-11-12

### Added

* Trouble in Tatzlford: Army Effects for defenses

## [0.12.4] - 2023-11-10

### Fixed

* Rename **Tusk Riders** to **Tusker Riders**
* Add full text on Defeated army condition
* Added an **Army** ability in the **Army Actions** compendium to make armies unflankable and add additional notes. You
  need to manually add this ability to imported actors

## [0.12.3] - 2023-11-03

### Fixed

* Make Statecraft selectable as a skill to recruit an army
* Group available item levels again

## [0.12.2] - 2023-11-02

### Fixed

* Tell Campfire story does not include the storyteller but only their allies

## [0.12.1] - 2023-10-30

### Fixed

* Do not display weather result to players when set to blind gm roll

## [0.12.0] - 2023-10-30

### Fixed

* When set to public roll, display weather roll result publicly as well

### Added

* Effect to gain temporary HP from Maintain Armor and post the effect into chat when clicking the **Combat Effects to
  Chat** button

## [0.11.0] - 2023-10-28

### Fixed

* Monument structure description
* Edit Structure Rules macro
* Structure item availability rules now limit to maximum buildings occurrences, not maximum modifiers

### Added

* Vance & Kerenshara Structures

## [0.10.2] - 2023-10-28

### Fixed

* Only show reset adventuring time button to GMs

## [0.10.1] - 2023-10-27

### Changed

* Turn kingdom feat status bonuses into circumstance bonuses since about half of them provide circumstance bonuses
  anyway

## [0.10.0] - 2023-10-27

### Added

* Improve kingdom feat automation notes by displaying what is not automated on hovering over the cell
* Add a reset button to camping adventuring time

### Fixed

* Do not fail rolling weather if calendar does not use Golarion mode
* Add Magic to usable skills when Engineering is allowed and Practical Magic is chosen
* Fortified Fiefs now gives you a +1 status bonus to dangerous events
* Available item calculation does not cap out at the maximum item level bonus anymore and buildings stack with more
  specific ones now enabling a maximum bonus of +13
* Marketplace structure description uses the correct one
* Enable combat tracks without having to import the camping sheet first
* Sharpshooter scaling after level 8 to 14 now properly adds a -1 to melee checks; you need to re-import the feat on
  imported actors
* Deploy Army skill selection now includes boating and magic when master

## [0.9.0] - 2023-10-14

### Added

* Setting to configure rp to xp conversion rate
* Setting to configure xp per claimed hex
* Setting to configure rp to xp conversion limit
* Move kingdom settings into kingdom sheet

### Changed

* Improve visual design for marking up farmlands and resource income
* Kingdom Settings have been moved to the Kingdom Sheet header

## [0.8.1] - 2023-10-14

### Fixed

* Previously only the number of Residential buildings was used to calculate Overcrowded; this is now the sum of all
  Residential buildings' token size (width * height); this can be overridden via a new **lots** building rules element

## [0.8.0] - 2023-10-12

### Added

* Leadership activities Reconnoiter Hex & Take Charge from Vance & Kerenshara; these have to be enabled in the activity
  blacklist
* You can now enable companion leadership activities without adding them as leaders

## [0.7.2] - 2023-10-10

### Fixed

* Live off the land tactic

## [0.7.1] - 2023-10-07

### Fixed

* Apply Hold the Line after bumping hp via Toughened Soldiers

## [0.7.0] - 2023-10-07

### Added

* Armies

### Fixed

* Fix broken ingredient links in Hunt & Gather journal
* Repair reputation now correctly increases control DC by 2

## [0.6.0] - 2023-09-29

### Changed

* Roll from Kingmaker Cult & Kingdom events if Kingmaker module is enabled
* Set Token paths for buildings and companions to Kingmaker module; the previous behavior is now available by installing
  the https://github.com/BernhardPosselt/pf2e-kingmaker-tools-token-mapping module
* Autoconfigure region and default combat tracks to point to Kingmaker playlists and tracks
* Autoconfigure snow, rain, rain storm and blizzard to point to Kingmaker SFX tracks
* Add a setting to disable weather effects

## [0.5.0] - 2023-09-20

### Changed

* Pull out token mappings into the https://github.com/BernhardPosselt/pf2e-kingmaker-tools-token-mapping module
  so that you can use both the Kingmaker and Kingmaker Tools module at the same time
* Roll from Kingmaker module roll tables when Kingmaker module is enabled

### Fixed

* Fix Elk reference in Rostland Hinterlands roll table

## [0.4.1] - 2023-09-04

### Fixed

* Capital investment failure now correctly gives you 1d4 RP
* Fix Subsist button that suffered from the same API break issue as other skill check buttons

## [0.4.0] - 2023-09-02

### Added

* Implemented Vance & Kerenshara Structure Item Bonus Stack rules that allow all item bonuses of all structures to stack

### Changed

* Search and Avoid Notice check macros now use the party sheet instead of the exploration macros module

## [0.3.6] - 2023-09-01

### Fixed

* Fix API breaks in PF2E 5.4.1 that prevented you from rolling checks from your kingdom sheet

### Removed

* Drop compatibility with versions lower than 5.4.1

## [0.3.5] - 2023-08-04

### Added

* Also warn when user accidentally created a camping actor manually
* Kalikke and Kanerah companion actors

## [0.3.4] - 2023-07-26

### Fixed

* Automate succulent sausage success and critical success
* Only sync weather effect when activating a scene or via macros to prevent inherent Foundry race conditions from
  occurring

## [0.3.3] - 2023-07-21

### Changed

* Add **Scene Weather Settings** macro which allows you to disable syncing weather based on the global weather settings
  on a per-scene basis.

## [0.3.2] - 2023-07-21

### Fixed

* Properly handle sheltered. If you are upgrading from 0.3.1 or 0.3.0, you need to retoggle the sheltered macro

## [0.3.1] - 2023-07-21

### Fixed

* Fixed set weather dialog button label

## [0.3.0] - 2023-07-21

### Added

* Added a macro to manually set weather
* Expanded weather playlists to leaves, rainStorm, fog and blizzard

### Changed

* Disabling weather in settings does not override scene's weather effects anymore
* The **Toggle Weather** macro's job is being replaced by the **Toggle Sheltered from Weather** macro which will now
  also stop all weather playlists instead of playing the **weather.sunny** one
* The **weather.snowfall** playlist has been changed to **weather.snow** to match the Foundry weather effect value. You
  will need to manually override the value via the **Set Current Weather** macro if its snowing

## [0.2.23] - 2023-07-21

### Added

* Added migrations

### Fixed

* Fix settlement levels randomly turning into strings

## [0.2.22] - 2023-07-14

### Fixed

* Fix activity naming issues, relaxed naming restrictions

## [0.2.21] - 2023-07-14

### Fixed

* Adding Activities: Relax validation for activity names, also validate skills and skill requirements

## [0.2.20] - 2023-07-13

### Fixed

* Whitelist characters for camping activities explicitly to not run errors when creating activities with special
  characters

## [0.2.19] - 2023-07-13

### Fixed

* Fix First World Mince Pie

## [0.2.18] - 2023-07-12

### Fixed

* Trim all text inputs to prevent errors due to trailing spaces, see #25

## [0.2.17] - 2023-07-12

### Added

* Added prebuilt companions for Jaethal, Harrim, Octavia and Regongar. Waiting for Rage of Elements to add the
  Kineticist sisters

### Fixed

* Fix macro typo

## [0.2.16] - 2023-07-12

### Added

* Added a way to play combat tracks based on current region, scene or actors in combat

## [0.2.15] - 2023-07-11

### Fixed

* Do not display cult event section and milestones to players

## [0.2.14] - 2023-07-11

### Fixed

* Kingdom Assurance doesn't add penalties nor bonuses

## [0.2.13] - 2023-07-09

### Changed

* Skip Rest for the Night dialog, requires PF2 system v5.1.2

## [0.2.12] - 2023-07-05

### Added

* Add setting to add half proficiency to untrained kingdom skill

### Fixed

* Remove combat effects after rest as well
* Prevent loot and vehicle actors from being dropped onto camping activities

## [0.2.11] - 2023-07-04

### Fixed

* Don't sync effects for loot and vehicle actors

## [0.2.10] - 2023-07-04

### Changed

* Allow npcs, loot and vehicle actors in addition to characters in the camping sheet

## [0.2.9] - 2023-06-30

### Fixed

* Support Dorako UI Dark Mode

## [0.2.8] - 2023-06-30

### Added

* Make it possible to disable activity skill requirement checks

## [0.2.7] - 2023-06-30

### Fixed

* Remove config too new for The Forge to parse aka "Cannot read property 'replace' of undefined"

## [0.2.6] - 2023-06-30

### Fixed

* Do not display basic nor special ingredient costs if Prepare Campsite was a Critical Failure or has not been set or if
  no actor selected the cook meal activity

## [0.2.5] - 2023-06-30

### Added

* Setting to configure the actor to add Hund and Gather Ingredients to; this is useful if you are using a party loot
  actor to store ingredients in

### Changed

* Hunt and Gather and Discover Special Meal buttons in chat can now be pressed by all actors
* Improved docs and add label onto sidebar actor silhouette to clearly communicate that actors can be dropped there

## [0.2.4] - 2023-06-29

### Changed

* Actors can now be blacklisted for the random encounter perception roll during rest. You can configure that in the rest
  settings by deselecting the actor from having watch. This is useful if you want to except companions from rolling
  perception checks during watch or if you have a party actor that includes ingredients/rations that should not have an
  impact on rest duration
* Number of actors can now be increased by setting the **Increase actors keeping watch** rest setting. This is useful if
  you use a single companion actor but want all companions to have an effect on the watch duration

## [0.2.3] - 2023-06-28

### Fixed

* Remove labels from all meal and camping effects

## [0.2.2] - 2023-06-28

### Added

* Note on how to implement Relax if desired in the help menu
* Combat effects no mention if they target enemies or allies

### Removed

* Relax camping activity
* Unused camping effects

### Fixed

* Fully automate **Relaxed** effect
* Fix effect rendering by renaming all effects containing a colon to use parenthesis. Existing effects are synced by
  name, so you will need to remove them manually
* Adjust fortify camp to only apply to watches

## [0.2.1] - 2023-06-27

### Added

* Macros to Award XP
* Macro to reset Hero Points to 1

### Fixed

* Also ship static folder

## [0.2.0] - 2023-06-26

### Added

* Camping sheet implementation
* Button to open kingdom sheet for players

### Changed

* Add support for rolling cult events with descending DC
* Add support for tracking and gaining XP from cult events that have already occurred
* Use built in V11 weather effects instead of FXMaster; you need to manually clear existing weather in FXMaster
* Fully reviewed, fixed and updated meal and camping effects

### Removed

* Removed Special and Basic Ingredient items; these are now shipped in the 2e system
* Removed the following macros (these are now available through the camping sheet):
    * Hunt and Gather
    * Camp Management
    * Prepare Camp
    * Camouflage Campsite
    * Organize Watch
    * Cook Recipe
    * Tell Campfire Story
    * Recipes
    * Learn from a Companion
    * Random Encounter
    * Stopwatch
    * Companion Effects to Chat
    * Subsist

## [0.1.0] - 2023-06-11

### Fixed

* Store and migrate recipe data to actor instead of using client settings: client settings were unknowingly stored in
  local storage; local storage is local to a browser and is wiped when deleting browser data; this also caused recipes
  form one world to be present in another world. We now store the settings on the actor and migrate existing data once
  you first run the recipes macro again

### Added

* Foundry 11 support

### Removed

* Foundry 10 support

### Changed

* Show unrest penalty in sidebar
* Group compendia in new compendia folders
* Move Settlement config into separate dialog

## [0.0.35] - 2023-04-27

### Fixed

* Ruler Vacancy Penalty correctly increases Control DC by 2 and causes you to gain 1d4 Unrest during Upkeep

### Added

* Add a way to be able to track Creative and Supernatural Solutions
* Added a Button to gain XP from unused Creative and Supernatural Solutions

## [0.0.34] - 2023-03-19

### Added

* Display errors when actors with name Kingdom Sheet exist that haven't been imported with the View Kingdom Macro

## [0.0.33] - 2023-03-16

### Fixed

* Do not pre-fill control dc for request foreign aid

## [0.0.32] - 2023-03-10

### Fixed

* @zarmstrong: Fixed prepare campsite to use region DC instead of actor level dc
* @zarmstrong: Fixed grammatical issues in regard to camping activities
* @rectulo: fix typo in Abandon Hex activity
* Default to Basic Meal if home brew recipe data can not be found

## [0.0.31] - 2023-03-08

### Added

* Add buttons to roll Flat Check to reduce ruin

## [0.0.30] - 2023-03-08

### Added

* Add gain 1 fame button to chat on critical success skill check

## [0.0.29] - 2023-03-05

### Added

* Automate claiming refuges and landmarks

## [0.0.28] - 2023-03-04

### Added

* Automate Settlements without Land Borders penalty

## [0.0.27] - 2023-03-04

### Fixed

* Farmlands now reduce consumption

## [0.0.26] - 2023-02-28

### Fixed

* Palace and Town Hall now correctly increase leadership activity number to 3 in turn tab

## [0.0.25] - 2023-02-26

### Added

* You can now right-click on a rolled kingdom event in chat and add it to your ongoing events list

### Fixed

* Fix flat checks in help popup

## [0.0.24] - 2023-02-25

### Fixed

* Fix auto-rolling weather for users with a name other than **Gamemaster**

## [0.0.23] - 2023-02-24

### Fixed

* Collecting resources now adds values to the current ones instead of replacing them
* Distribute kingdom sheet changes across clients

### Changed

* Kalikke's Deliberate Planning activity now provides a circumstance rather than a status bonus. See reasoning in
  the [README](README.md)

## [0.0.22] - 2023-02-22

### Fixed

* Heartland is now correctly rendered again in the turn tab

### Added

* Bring back overcrowded in settlement overview
* Added clarification about building terrain features in the turn's region section

## [0.0.21] - 2023-02-21

### Changed

* Settlement data is now no longer saved on the scene itself, allowing players to edit their values as well. You need to
  re-add existing scenes
* Rather than configuring an overcrowded flag, you configure a "lots" value now which is compared to the amount of
  residential buildings
* Improve modifier handling

## [0.0.20] - 2023-02-20

### Added

* There is now a Fame Next column
* Added buttons to easily subtract/add resources, unrest and ruin from activities

### Fixed

* Do not throw error on left click in chat if no Kingdom Sheet actor is present
* Double check and fixed various activities

## [0.0.19] - 2023-02-20

### Fixed

* Fixed Heartland dropdown to include correct values
* Consumption can no longer go into negatives

## [0.0.18] - 2023-02-20

### Added

* You can now add homebrew recipes to the Learn Recipe macro

## [0.0.17] - 2023-02-19

### Fixed

* Set initial ruin threshold values to 10
* Updating dice in now column correctly adjust dice in dice column

## [0.0.16] - 2023-02-19

### Fixed

* Re-Render kingdom sheet on relevant config changes

## [0.0.15] - 2023-02-19

### Fixed

* Disable Capital Investment in active settlement when no Bank is built

## [0.0.14] - 2023-02-19

### Fixed

* A 1 or 20 on a d20 should now correctly upgrade/downgrade a roll

### Added

* Rolls from kingdom sheets can now be re-rolled, re-rolled and keep higher/lower and re-rolled using fame points with a
  right click on the roll chat message
* Activity results can now be upgraded/downgrade with a right click on the chat message

## [0.0.13] - 2023-02-18

### Added

* Added automation for Warfare Exercises and Cooperative's Leadership bonus to work for Focused Attention

## [0.0.12] - 2023-02-18

### Fixed

* Make a copy of the modifier consumeId to not update data

## [0.0.11] - 2023-02-18

### Added

* Added effects to events that can be applied to the kingdom when rolled in chat

## [0.0.10] - 2023-02-18

### Added

* Added an effects tab allowing you to add custom modifiers that expire and get removed at the end of a kingdom turn

## [0.0.9] - 2023-02-18

### Added

* Added Structure traits to automatically calculate residential buildings. All the buildings should now have data
  references. You need to re-import the following structures:
    * Bridge
    * Cemetery
    * Houses
    * Magical Streetlamps
    * Orphanage
    * Paved Streets
    * Rubble
    * Tavern, Dive
    * Tenement
    * Wall, Stone
    * Wall, Wooden

### Fixed

* Fixed Ekundayo's and Nok-Nok's activities not being blacklisted

## [0.0.8] - 2023-02-17

### Changed

* Automate milestone XP gain

### Fixed

* Ensure that modifier penalties and modifiers are always positive

## [0.0.7] - 2023-02-17

### Fixed

* Disable settlement inputs for users because these are persisted on scenes where they don't have permissions

## [0.0.6] - 2023-02-17

### Changed

* Don't render tables when there's no data

### Fixed

* Palace now gives a +3 item bonus during leadership activities
* Better feedback when no settlement exists yet and players open kingdom sheet

### Added

* Moved Settlement Macro into Kingdom Sheet
* Implement QoL feat

### Removed

* Remove view settlement macro and moved functionality into kingdom sheet

## [0.0.5] - 2023-02-17

### Added

* Add Alpha version of the Kingdom Sheet

### Changed

* All mentions of **action** in structure rules have been replaced with **activity**

## [0.0.4] - 2023-02-12

### Added

* Include missing Leadership Activities
* Include missing Events
* Added structure rules. Existing structures need to be replaced upon update to make use of it
* Added a settlement overview that computes all settlement item bonuses, storages, notes and types; bonuses stack with
  the capital automatically
* Added a macro to edit structure rules

### Fixed

* Also include familiars in exploration macros
* Change Marketplace Lots to 2
* Changed Special Ingredients to 0 gp

## [0.0.3] - 2023-01-13

### Added

* Added an input for setting servings numbers for the cooking popup
* Weather Event Maximum Level is now configurable. By RAW, it's 4, meaning no weather events that are more than 4 levels
  higher than the party's can occur, but you may want to turn that down to 2 to not TPK your party.

### Changed

* The Stopwatch app has been reworked into a watch and daily preparations tracker that automatically calculates watch
  time and daily preps time

### Fixed

* Ingredients gathered via Hunt and Gather now stack correctly in your inventory

## [0.0.2] - 2023-01-11

### Added

* Added a macro to roll weather and a setting to disable automatically rolling weather when a new day begins
* Added a macro to learn or buy recipes
* Added a macro for Learn from a Companion
* Added a macro for Camp Management
* Added a stopwatch macro to track time elapsed after daily preparations
* Added Kingdom Events journals as well since these are now available on AoN
* Added Cult Events random event table

### Changed

* Only learned recipes are showing in the Cook Recipe macro

### Fixed

* Cooking macro not being able to store values in settings when a player used it; these settings are now stored per
  player

## [0.0.1] - 2023-01-05

### Added

* First Release
