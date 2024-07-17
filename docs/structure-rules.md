# Structure Rules

All structure rules are JSON and are persisted using actor flags. They can be edited using the **Edit Structure Rules**
Macro.

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
* Bank (V&K)
* Barracks
* Brewery
* Castle
* Castle (V&K)
* Cathedral
* Construction Yard
* Construction Yard (V&K)
* Dump
* Embassy
* Festival Hall
* Festival Hall (V&K)
* Foundry
* Garrison
* Garrison (V&K)
* General Store
* Gladiatorial Arena
* Granary
* Granary (V&K)
* Guildhall
* Herbalist
* Hospital
* Illicit Market
* Inn
* Inn (V&K)
* Jail
* Keep
* Library
* Library (V&K)
* Lumberyard
* Luxury Store
* Magic Shop
* Magic Shop (V&K)
* Mansion
* Marketplace
* Menagerie
* Military Academy
* Mill
* Mint
* Monument
* Monument (V&K)
* Museum
* Noble Villa
* Occult Shop
* Occult Shop (V&K)
* Opera House
* Palace
* Palace (V&K)
* Park
* Pier
* Printing House
* Sacred Grove
* Secure Warehouse
* Sewer System
* Shrine
* Smithy
* Smithy (V&K)
* Specialized Artisan
* Stable
* Stockyard
* Stonemason
* Tannery
* Tavern, Dive
* Tavern, Dive (V&K)
* Tavern, Luxury
* Tavern, Luxury (V&K)
* Tavern, Popular
* Tavern, Popular (V&K)
* Tavern, World-Class
* Tavern, World-Class (V&K)
* Temple
* Theater
* Thieves' Guild
* Town Hall
* Town Hall (V&K)
* Trade Shop
* University
* Watchtower
* Watchtower, Stone
* Waterfront
* Bridge
* Bridge, Stone
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

**Full rules are not yet finalized!**. If you are using them, be prepared for having to manually migrate them if
required!

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
  "lots": 2,
  "level": 2,
  "affectsEvents": false,
  "affectsDowntime": false,
  "reducesUnrest": false,
  "reducesRuin": false,
  "upgradeFrom": [
    "Pier"
  ],
  "construction": {
    "skills": [
      {
        "skill": "agriculture",
        "proficiencyRank": 2
      }
    ],
    "lumber": 2,
    "luxuries": 2,
    "ore": 2,
    "stone": 2,
    "rp": 2,
    "dc": 2
  },
  "stacksWith": "Slightly Different Magic School"
}
```

* **name**: optional, if absent taken from the actor name
* **notes**: optional, is shown at the bottom in the **Building Effects** section
* **preventItemLevelPenalty**: optional, if not at least one structure in your settlement has this set to true, it will
  reduce purchasable item level by 2 for this settlement
* **enableCapitalInvestment**: optional, if not at least one structure in your settlement has this set to true, *
  *Capital Investment** will be marked as not possible in this settlement
* **increaseLeadershipActivities**: optional, if one building in your capital has this set to true, your Leadership
  Activity number will increase from 2 to 3
* **consumptionReduction**: optional, is only used once for all structures with the same name, decreases its
  settlement's consumption by the amount
* **activityBonusRules**: optional, stack up to settlement item bonus and capital item bonuses
    * **value**: mandatory, bonus
    * **activity**: mandatory, kingdom activity id; all existing activities can [be found here](https://github.com/BernhardPosselt/pf2e-kingmaker-tools/blob/master/src/kingdom/data/activityData.ts), e.g. "abandon-hex" or "recover-army-damaged"
* **skillBonusRules**: optional, similar to **activityBonusRules** but allow you to add a flat skill bonus or limit an
  activity bonus to a certain skill
    * **value**: mandatory, bonus
    * **skill**: mandatory, Kingdom skill in lowercase
    * **activity**: optional, if provided, only applies to a single activity, formatting similar to activities of *
      *activityBonusRules**
* **availableItemsRules**: optional, if given increase the item level for purchasing items in this settlement
    * **value**: mandatory, level increase
    * **maximumStacks**: optional, up to how what maximum value buildings should stack
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
* **storage**: optional, marks up the current settlement's storage capacity, stacks with all values in the settlement.
  Each key is optional, so denoting only ore capacity increases would look something like:
  ```json
  {
    "storage": {
      "ore": 1
    }
  }
  ```
* **unlockActivities**: optional, contains a list of activity names that should be unlocked on the character sheet. The
  following activities are not enabled by default out of the box; most of them however are enabled by putting companions
  into certain leadership rules:
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
* **lots**: optional, if provided used to calculate how many lots the building takes up; otherwise the token size is
  used
* **level**: optional, if absent, uses the NPC actor's level
* **affectsEvents**: optional, used to filter for structures that affect events
* **affectsDowntime**: optional, used to filter for structures that have downtime bonuses
* **reducesUnrest**: optional, used to filter for structures that reduce unrest
* **reducesRuin**: optional, used to filter for structures that reduce ruin
* **upgradeFrom**: optional, includes a list of structure names that this structure can be upgraded from
* **construction**:
    * **skills**:
        * **skill**: a skill in lower case
        * **proficiencyRank**: optional, defaults to 0 (untrained); use 1 (trained), 2 (expert), 3 (master) or 4 (legendary)
    * **dc**: dc
    * **rp**: rp cost
    * **lumber**: optional, cost
    * **luxuries**: optional, cost
    * **ore**: optional, cost
    * **stone**: optional, cost
* **stacksWith**: optional, name of a structure that this structure should stack item bonuses with; useful when you've got the same building with 2 different construction costs