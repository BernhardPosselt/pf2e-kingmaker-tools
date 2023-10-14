# Structure Rules

All structure rules are JSON and are persisted using actor flags. They can be edited using the **Edit Structure Rules** Macro.

Rules can be one of the following types:

* References to built ins
* Full rule

### References

These take the form of

```json
{
  "ref": "Herbalist"
}
```

and reference to built-in rules that get maintained and updated with the module. The following refs are available:

* Academy
* Alchemy Laboratory
* Arcanist's Tower
* Arena
* Bank
* Barracks
* Brewery
* Castle
* Cathedral
* Construction Yard
* Dump
* Embassy
* Festival Hall
* Foundry
* Garrison
* General Store
* Gladiatorial Arena
* Granary
* Guildhall
* Herbalist
* Hospital
* Illicit Market
* Inn
* Jail
* Keep
* Library
* Lumberyard
* Luxury Store
* Magic Shop
* Mansion
* Marketplace
* Menagerie
* Military Academy
* Mill
* Mint
* Museum
* Noble Villa
* Occult Shop
* Opera House
* Palace
* Park
* Pier
* Printing House
* Sacred Grove
* Secure Warehouse
* Sewer System
* Shrine
* Smithy
* Specialized Artisan
* Stable
* Stockyard
* Stonemason
* Tannery
* Tavern, Luxury
* Tavern, Popular
* Tavern, World-Class
* Temple
* Theater
* Thieves' Guild
* Town Hall
* Trade Shop
* University
* Watchtower
* Waterfront
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

## Full Rules

**Full rules are not yet finalized!**. If you are using them, be prepared for having to manually migrate them if required!

A full structure rule would look something like this:

```json
{
  "name": "Magic School",
  "notes": "Allows you to retrain your grades",
  "preventItemLevelPenalty": false,
  "enableCapitalInvestment": false,
  "increaseLeadershipActivities": false,
  "consumptionReduction": 1,
  "activityBonusRules": [
    {
      "value": 1,
      "activity": "create-a-masterpiece"
    }
  ],
  "skillBonusRules": [
    {
      "value": 2,
      "skill": "warfare",
      "activity": "pledge-of-fealty"
    }
  ],
  "availableItemsRules": [
    {
      "value": 1,
      "group": "luxury"
    }
  ],
  "settlementEventRules": [
    {
      "value": 1
    }
  ],
  "leadershipActivityRules": [
    {
      "value": 1
    }
  ],
  "storage": {
    "ore": 1,
    "food": 1,
    "lumber": 1,
    "stone": 1,
    "luxuries": 1
  },
  "isBridge": false,
  "unlockActivities": [
    "read-all-about-it"
  ],
  "traits": [
    "edifice"
  ],
  "lots": 2
}
```

* **name**: optional, if absent taken from the actor name
* **notes**: optional, is shown at the bottom in the **Building Effects** section
* **preventItemLevelPenalty**: optional, if not at least one structure in your settlement has this set to true, it will reduce purchasable item level by 2 for this settlement
* **enableCapitalInvestment**: optional, if not at least one structure in your settlement has this set to true, **Capital Investment** will be marked as not possible in this settlement
* **increaseLeadershipActivities**: optional, if one building in your capital has this set to true, your Leadership Activity number will increase from 2 to 3
* **consumptionReduction**: optional, is only used once for all structures with the same name, decreases its settlement's consumption by the amount
* **activityBonusRules**: optional, stack up to settlement item bonus and capital item bonuses
    * **value**: mandatory, bonus
    * **activity**: mandatory, kingdom activity in lowercase with spaces converted to dashes (e.g. **Rest and Relax** -> **rest-and-relax**)
* **skillBonusRules**: optional, similar to **activityBonusRules** but allow you to add a flat skill bonus or limit an activity bonus to a certain skill
    * **value**: mandatory, bonus
    * **skill**: mandatory, Kingdom skill in lowercase
    * **activity**: optional, if provided, only applies to a single activity, formatting similar to activities of **activityBonusRules**
* **availableItemsRules**: optional, if given increase the item level for purchasing items in this settlement
    * **value**: mandatory, level increase
    * **group**: optional, if absent **stacks with everything else** up to 3 times, otherwise one of:
        * other
        * alchemical
        * primal
        * divine
        * occult
        * arcane
        * luxury
        * magical
* **settlementEventRules**: optional, if given increase the settlements event item bonus for this settlement
    * **value**: mandatory, bonus
* **settlementEventRules**: optional, if given increase the leadership activity item bonus
    * **value**: mandatory, bonus
* **isBridge**: optional, if true, gets rid of the trade penalty when a settlement has 4 water borders
* **storage**: optional, marks up the current settlement's storage capacity, stacks with all values in the settlement. Each key is optional, so denoting only ore capacity increases would look something like:
  ```json
  {
    "storage": {
      "ore": 1
    }
  }
  ```
* **unlockActivities**: optional, contains a list of activity names that should be unlocked on the character sheet. The following activities are not enabled by default out of the box; most of them however are enabled by putting companions into certain leadership rules:
    * read-all-about-it
    * evangelize-the-dead
    * decadent-feasts
    * deliberate-planning
    * false-victory
    * show-of-force
    * warfare-exercises
    * preventative-measures
    * spread-the-legend
    * read-all-about-it
    * recruit-monsters
    * process-hidden-fees
    * supplementary-hunting
* **traits**: optional, may include:
    * edifice
    * yard
    * building
    * famous
    * infamous
    * residential
    * infrastructure
* **lots**: optional, if provided used to calculate how many lots the building takes up; otherwise the token size is used 