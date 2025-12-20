# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

* When using manual settlement management, always use occupied blocks when figuring out settlement level; remove unused settlement level configuration


## [5.7.5] - 2025-10-31

### Fixed

* Autocalculate total loss in food rather than requiring users to insert it manually


## [5.7.4] - 2025-10-31

### Fixed

* Fixed Brazilian language tag

## [5.7.3] - 2025-10-31

### Fixed

* Added note to skip Adjusting Unrest and Paying Consumption on the first turn
* Fixed empty labels in resource consumption breakdown
* Fix "Lose 5 Resource Points per Missing Commodity" button not subtracting RP
* Properly add Portuguese and Chinese (Simplified) to available languages 

## [5.7.2] - 2025-09-20

### Fixed

* Fix API break in 2e system 7.5.0

### Changed

* @MarkPearce improved kingdom turn styling

## [5.7.1] - 2025-08-15

### Fixed

* Update Polish translations

### Changed

* Update library dependencies


## [5.7.0] - 2025-08-13

### Added

* After combat, the GM will get a popup to award XP and hero points

### Fixed

* Removed automation notes from establish worksite

## [5.6.0] - 2025-07-27

### Added

* Also include ration cost when consuming rations in cost sidebar
* Allow non Character actors to consume rations

### Fixed

* Do not render cult activity event section for players

### Changed

* Update toolchain and dependencies

## [5.5.4] - 2025-06-16

### Fixed

* Improve labelling of vacancies and add available actions to phases
* Ask DC before performing Clear Hex instead of simply setting 0

## [5.5.3] - 2025-06-16

### Fixed

* Rerender sheet when changing active leader drop down
* Cleanup weather effects and post precipitation effects to chat
* When using partial construction, display the amount paid in addition to the total amount on the structure browser section if it is different
* When using partial construction, mark structures as buildable if their RP cost per structure or total cost, whichever is lower, is lower or equal than your total funds
* Always allow spending RP in Under Construction tab if RP are available
* Limit spending RP for structures under construction to current RP amount
* Disable Spend RP button if kingdom has 0 RP available

## [5.5.2] - 2025-06-16

### Fixed

* Rerender sheet upon changing bonus feat drop downs
* Strip HTML from new Rolltable entries which is generated in V13


## [5.5.1] - 2025-06-15

### Fixed

* Do not display slowed structures in Under Construction tab

## [5.5.0] - 2025-06-15

### Added 

* When placing V&K Civic buildings, the structure browser now shows the amount of additional settlement actions that you can take for each settlement
* Added a setting to enable structure rp cost tracking. RP is tracked using the structure actors HP

### Changed

* Changes to V&K 1.1 rules changed how resource dice per settlements are calculated. This required hard coding the RD values; instead of configuring it per settlement type, you now need to toggle a new setting (Settlements Generate Resource Dice) to enable this behavior.

### Fixed

* Allow configuring all hex scenes to store party actor location in camping settings


## [5.4.1] - 2025-06-06

### Fixed

* Fixed translations for realm tile type macro

## [5.4.0] - 2025-06-04

### Changed

* Require PF2E 7.1.0
* Instead of activating parties from the camping sheet menu, use the new checkbox on the party actor in the actor sidebar 

### Fixed

* Remove Leadership activity increase text from V&K Palace, Town Hall & Castle
* Fix Read All About It requirement text
* Fix Hospital downtime description
* Make Seasoned Wings and Thighs effect critical success descriptions more apparent in what each effect does
* Fixed Camping Sheet not opening due to API breaks in PF2 7.1.0

## [5.3.0] - 2025-05-26

### Added

* Allow to open Settlement Inspect dialog in structure browser 

### Fixed

* Ruin Resistance correctly asks you to pick 1 ruin to increase instead of 2


## [5.2.0] - 2025-05-16

### Added

* Party actor icons can now be hidden if you only rely on macros
* Schema version is now visible and changeable through settings to make debugging/support easier

## [5.1.10] - 2025-05-14

### Fixed

* Also update army consumption when changing the army folder in kingdom settings


## [5.1.9] - 2025-05-14

### Fixed

* When changing from manual army consumption to automatic, immediately update army consumption on the sheet

## [5.1.8] - 2025-05-11

### Fixed

* Fix activity syncing when always performed activities are changed in settings

## [5.1.7] - 2025-05-10

### Fixed

* Prevent users from upgrading from versions that haven't been properly migrated on V12 yet to prevent potential bugs during data migration
* Fix migration successful popup

## [5.1.6] - 2025-05-04

### Fixed

* Fix xp macro translations


## [5.1.5] - 2025-05-03

### Fixed

* Fix structure name migrations when updating from Foundry V12 without having upgraded to the latest version
* Abort migrations and show a user error if structures with a broken ref are encountered

## [5.1.4] - 2025-05-03

### Fixed

* Fix structure XP dialog

## [5.1.3] - 2025-04-30

### Fixed

* If more than one user has playlist permissions, only start the track once

## [5.1.2] - 2025-04-29

### Added

* Added Polish translation, thanks @Lioheart


## [5.1.1] - 2025-04-26

### Fixed

* Teleporter Macro can now select a different scene
* Fix translations in migration notification

## [5.1.0] - 2025-04-25

### Added

* Added a macro that changes all NPCs' Health Bars on all scenes to a configured value
* Added a macro that creates 2 regions used for teleporting tokens between each other

## [5.0.0] - 2025-04-21

### Added

* The module can now be translated into different languages
* Add modifiers that increase consumption
* Automate Squatters event consumption increase
* Allow dragging actor images from a recipe to another recipe
* Allow users to re-enable the first run message
* Add all structure traits to Structure Browser filters
* Add Gain Fame button if built famous/infamous trait type matches kingdom fame type
* Add buttons to add and resolve kingdom events
* Allow tracking of blocked Pledge of Fealty
* Pledge of Fealty, Establish Trade Agreement, Send Diplomatic Envoy and Request Foreign Aid now allow you to pick a group when performing the activity
* Automate income increase when critically succeeding at building a worksite
* Deliberate Planning and Hire Adventurers now allow you to pick an Event
* Add button to mark an event as continuous

### Changed
* Require FoundryVTT 13
* Apply Plague event civic penalty automatically without having to add it first
* Migrate recipe cost to parsed values
* Indicate activity ordering in Upkeep and Commerce phase
* Various activity modifiers that give a penalty when performing the activity during consecutive turns now use modifiers to track rather than requiring a toggle

### Fixed

* Filter out lower bonuses in inspect settlement
* Fix Political Calm bonus
* Correctly exclude all V&K activities when first creating a kingdom sheet
* Automatically enable Civil Service bonus
* Fix Fame & Fortune
* Fix leadership lores not working when containing special characters
* Display kingdom activity automation notes

## [4.8.5] - 2025-06-16

### Fixed

* Fix Kingdom Sheet not re-rendering when choosing bonus feats in the Bonus tab

## [4.8.4] - 2025-05-18

### Fixed

* Fix pre update hooks breaking preventing meal updates and combat tracks from working properly

## [4.8.3] - 2025-05-11

### Fixed

* Fix activity syncing when always performed activities are changed in settings

## [4.8.2] - 2025-05-03

### Fixed

* Fix structure XP dialog
* If more than one user has playlist permissions, only start the track once


## [4.8.1] - 2025-04-21

### Fixed

* Fix Deploy Army skills
* Only re-render kingdom sheet once
* Try to prevent possible null values being migrated when upgrading from versions prior to 4.0.0

## [4.8.0] - 2025-04-09

### Added

* Make Event DC and Step configurable

### Changed

* Collapsed modifier flags and roll options into one

## [4.7.0] - 2025-04-08

### Added

* Automated Free and Fair rerolls

## [4.6.0] - 2025-04-08

### Added

* Each logged-in user can now select an active leader. Doing so will update the action cost in the turn tab when using V&K Leadership Modifiers and preselect the leader in all check popups 
* When using V&K Leadership Modifiers, the check popup now includes action cost based on the selected leader

### Fixed

* Fix issue that prevented you from building structures
* Hide Kingdom/Camping export buttons for non GM users

## [4.5.0] - 2025-04-07

### Added

* Automate additional unrest reduction of Endure Anarchy
* Automate Civil Service supported leader choice
* Mark feat prerequisites as red if they are not satisfied

### Changed

* Migrate structure name lookups to ids. If you have custom structures, you will need to set an id field
* Add ids to recipes and camping activities
* Show ids in homebrew dialogs to allow you to override them easier

### Fixed

* Fix issue that caused wrong resting playlist sound to be set if you changed the playlist but not the track
* Display Feat Prerequisites
* Prevent adding the same bonus feat twice
* Do not mark more than 1 tab as active in the Kingdom section
* Disable dragging for sheet tabs
* Display automation notes for features
* Fix data migration bug back to version 2.0.0
* Show ruin threshold increase checkboxes for government feats

## [4.4.1] - 2025-04-05

### Fixed
* Improve event automation
* Rename Terrain to Map in add settlement dialog

## [4.4.0] - 2025-03-31

### Added

* Add a 3 dot menu to all party actors to export/delete/import camping/kingdom data


## [4.3.2] - 2025-03-31

### Fixed

* Fixed Schema migrations for 4.3.0. Earlier versions need manual intervention as described in the Upgrading Notices for 4.0.0

## [4.3.1] - 2025-03-30

### Fixed

* Added searchbar to filter kingdom events

## [4.3.0] - 2025-03-30

### Added

* Events are now completely automated and can be managed like every other homebrew item
* Compendium links to the official Kingmaker module will be migrated to the new built-in events, everything else has to be migrated manually; you will receive a chat message with events that couldn't be migrated

### Changed

* Inspect Settlement dialog now picks up changes on the canvas allowing you to see added and deleted structures without reopening the dialog

### Fixed

* Fix copy-paste mistake that caused all primal, occult and arcane shopping bonuses to register under divine
* Fixed Compendium Weather Events


## [4.2.5] - 2025-03-28

### Fixed

* Fix invested wording

## [4.2.4] - 2025-03-28

### Fixed

* Improve heartland guidance


## [4.2.3] - 2025-03-28

### Fixed

* Improve kingdom creation experience by including relevant notes and guidance


## [4.2.2] - 2025-03-28

### Fixed

* Properly escape leader names when not using escaped labels

## [4.2.1] - 2025-03-28

### Fixed

* Display leader actor name in vacancies 

## [4.2.0] - 2025-03-28

### Changed

* Always expand Bonus Feat description
* Display automation notes for feats during selection
* Add note about Hire Adventurers about Practical Magical price reduction
* Increase luxuries gained during collect resources activity by 1 if Quality of Life is taken
* Display Gain 1 Resource Dice Next Turn button when Fame and Fortune feat is taken

## [4.1.3] - 2025-03-28

### Fixed

* Do not render leader types when leadership modifiers are disabled
* Remove wink to contribute settlement scenes since those have been contributed
* If no settlement is selected, swap in a non settlement bg image
* Improve usage documentation in the creation tab


## [4.1.2] - 2025-03-24

### Changed

* Improve upgrading notices to make breaking changes clearer

## [4.1.1] - 2025-03-24

### Fixed

* Prevent users from entering empty number fields

## [4.1.0] - 2025-03-24

### Added

* You can now create new settlement scenes in the Settlements tab. Those will pull in backgrounds kindly provided by dbavirt from Discord if you don't have the official module installed; otherwise, the official module's settlement scenes will be used


## [4.0.10] - 2025-03-22

### Fixed

* Do not hide camping icon anymore if setting to hide built in sheet is set to on and no built-in kingdom sheet was created


## [4.0.9] - 2025-03-22

### Fixed

* Make camping icons on party actor visible for players even if you haven't created a built-in kingdom sheet 


## [4.0.8] - 2025-03-21

### Fixed

* Fix kingdom leader delete button background color using light theme
* Improve Dorako UI compatibility


## [4.0.7] - 2025-03-21

### Fixed

* Remove blacklisted feats from options in kingdom section


## [4.0.6] - 2025-03-21

### Fixed

* Do not clear all events when remove one ongoing event


## [4.0.5] - 2025-03-21

### Fixed

* Fixed trained skills chocking on null values

### Added 

* Change kingdom sheet backgrounds based on active settlement types

## [4.0.4] - 2025-03-20

### Fixed

* Fix turn tab


## [4.0.3] - 2025-03-20

### Fixed

* Always include skill proficiency drop downs to prevent setting them to null


## [4.0.2] - 2025-03-20

### Fixed

* Correctly parse farmlands when using official module as resource mode


## [4.0.1] - 2025-03-20

### Fixed

* Fixed Kingdom Sheet not opening after migration when one of the kingdom's skills was not set to at least trained

## [4.0.0] - 2025-03-20

### Added

* Kingdom Character Sheet builder with automated ability scores, skill trainings, skill increases and ruin thresholds
* You can now have more than one kingdom/camping sheet
* Kingdom Sheet homebrew options for charters, heartlands, governments and milestones

### Removed

* Structure Token Mapping Macro
* Removed Roll Kingdom Event macro since those depend on the actual kingdom sheet now
* Removed Camping and Kingdom macros; those are now accessed through the party actor and can be drag and dropped onto your macro bar
* Removed background for settlement scenes due to unclear commercial licensing

### Changed
* Armies don't need to be targeted anymore but simply selected
* Many Feats, Activities and Features have received increased automation, especially ones that upgrade or downgrade degrees of successes
* Supernatural Solution now correctly rolls before knowing the result
* Reset/Award Hero Points, Award XP and Roll Stealth/Perception Exploration macros are now party aware. If more than one party exists, you will need to choose the current party in a popup
* All check dialogs now support selecting a leader by default; this automatically enables the +3 bonus from the Palace to Leadership activities for the Ruler
* Recruitable Armies are now looked up in a folder that is configured in the kingdom sheet settings
* For developers only: all code has now been ported from TypeScript to Kotlin so oldsrc/TypeScript setup is not necessary anymore

## [3.1.0] - 2025-02-26

### Fixed

* Fix settlement dice granted by settlement type if using automatic settlement level

### Changed

* Migrated various dialogs to app v2
* On startup, all custom structures are validated against the current schema instead of each time when querying them
* **API BREAK**: Require name, lots and level for custom structures and no longer fall back to actor data

## [3.0.0] - 2025-02-25

### Changed

* Overhauled Kingdom Settings and made them local per kingdom sheet
* All Kingdom Leaders must now refer to an NPC or Character actor; a best effort migration is in place which tries to find an NPC or Character actor by the previously set name. This might not work if your leaders don't have actors or if the names don't line up; to change existing leaders, simply drag and drop an actor from the sideebar onto the leader position in the sheet
* Kingdom settings are now local to your kingdom sheet. This means that if you are running multiple instances, changes won't propagate anymore to other sheets anymore
* Roll Table UUIDs are now used for kingdom and cult events. This means that roll table names are not required to be unique anymore

### Added

* Added V&K 1.1 homebrew except:
  * Construction Time
  * Renown/Fame/Infamy split

### Fixed

* Edit Structure macro should now edit the base actor and not the unlinked token actor
* Fix Economic Surge journal link for built-in kingdom events

## [2.1.2] - 2025-01-29

### Fixed

* Do not fail when worldtime returns a fraction

## [2.1.1] - 2025-01-26

### Fixed

* Also add the possiblity to disable advancing daily preps and checking for random encounters when resting
* Disabling watch in rest popup does not disable random encounter checks anymore
* Do not show rest config popup when clicking the "Continue" button

## [2.1.0] - 2025-01-26

### Changed

* Clicking on the Rest button on the camping sheet now asks you to confirm the rest, shows you the duration, and allows you to skip the watch, reducing time spent resting and disabling the random encounter check

### Fixed

* Evangelize the Dead has been corrected to Evangelize the End. You might need to adjust your bonuses in existing structures.
* UUIDs for consumable modifiers on the kingdom sheet are now generated using a fallback that will work when running foundry over HTTP instead of HTTPS only


## [2.0.7] - 2024-11-17

### Fixed

* Document required scene permission in the manual when using Tile/Drawing based kingdom resources
* Fix accidental mentions of the Lands tab in kingdom settings (should have been Status)

### Added

* Make it possible to disable the automatic token mapping in settings if the official module isn't installed

## [2.0.6] - 2024-11-15

### Fixed

* Instead of advancing the random encounter point in time twice during resting, advance the remaining rest period

## [2.0.5] - 2024-11-02

### Fixed

* Closing the DC popup now defaults to a DC of 0 for camping activities
* Closing the DC popup when resting skips the perception check altogether
* Properly apply roll mode to weather events table draw

## [2.0.4] - 2024-10-13

### Fixed

* Show recipes up to region level instead of recipes lower than region level

## [2.0.3] - 2024-10-13

### Fixed

* Cap XP gain from XP macro at level 20
* Do not show total food cost for meals with a cooking result
* Better hover titles for available ingredients

## [2.0.2] - 2024-10-02

### Fixed

* Correctly limit width of wide character portraits to camping activity tiles

## [2.0.1] - 2024-09-27

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
