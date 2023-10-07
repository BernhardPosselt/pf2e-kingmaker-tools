# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.7.0] - 2023-10-07

### Added

* Armies

### Fixed

* Fix broken ingredient links in Hunt & Gather journal
* Repair reputation now correctly increases control DC by 2

## [0.6.0] - 2023-09-29

### Changed

* Roll from Kingmaker Cult & Kingdom events if Kingmaker module is enabled
* Set Token paths for buildings and companions to Kingmaker module; the previous behavior is now available by installing the https://github.com/BernhardPosselt/pf2e-kingmaker-tools-token-mapping module
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
