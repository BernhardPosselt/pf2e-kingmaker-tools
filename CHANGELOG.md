# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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
* Kalikke's Deliberate Planning activity now provides a circumstance rather than a status bonus. See reasoning in the [README](README.md)

## [0.0.22] - 2023-02-22

### Fixed

* Heartland is now correctly rendered again in the turn tab

### Added

* Bring back overcrowded in settlement overview
* Added clarification about building terrain features in the turn's region section

## [0.0.21] - 2023-02-21

### Changed
* Settlement data is now no longer saved on the scene itself, allowing players to edit their values as well. You need to re-add existing scenes
* Rather than configuring an overcrowded flag, you configure a "lots" value now which is compared to the amount of residential buildings
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

* Rolls from kingdom sheets can now be re-rolled, re-rolled and keep higher/lower and re-rolled using fame points with a right click on the roll chat message
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
* Added Structure traits to automatically calculate residential buildings. All the buildings should now have data references. You need to re-import the following structures:
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
* Added a settlement overview that computes all settlement item bonuses, storages, notes and types; bonuses stack with the capital automatically
* Added a macro to edit structure rules

### Fixed

* Also include familiars in exploration macros
* Change Marketplace Lots to 2
* Changed Special Ingredients to 0 gp

## [0.0.3] - 2023-01-13

### Added

* Added an input for setting servings numbers for the cooking popup
* Weather Event Maximum Level is now configurable. By RAW, it's 4, meaning no weather events that are more than 4 levels higher than the party's can occur, but you may want to turn that down to 2 to not TPK your party.

### Changed

* The Stopwatch app has been reworked into a watch and daily preparations tracker that automatically calculates watch time and daily preps time

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

* Cooking macro not being able to store values in settings when a player used it; these settings are now stored per player

## [0.0.1] - 2023-01-05

### Added

* First Release
