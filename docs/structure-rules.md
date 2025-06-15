# Structure Rules

All structure rules are JSON and are persisted using actor flags. They can be edited using the **Edit Structure Rules**
Macro.

If you need inspiration, take a look at [existing structures](../data/structures)

Rules can be one of the following types:

* References to built ins
* Full rule

### References

These take the form of

```json
{
  "ref": "herbalist"
}
```

and reference to built-in rules that get maintained and updated with the module. The following refs are available:

* academy
* alchemy-laboratory
* arcanists-tower
* arena
* bank
* barracks
* brewery
* bridge
* bridge-stone
* castle
* cathedral
* cemetery
* construction-yard
* dump
* embassy
* festival-hall
* fishing-fleets-vk
* foundry
* garrison
* general-store
* gladiatorial-arena
* houses
* granary
* guildhall
* herbalist
* hospital
* illicit-market
* inn
* jail
* keep
* library
* lumberyard
* luxury-store
* magic-shop
* magical-streetlamps
* mansion
* marketplace
* menagerie
* military-academy
* mill
* mint
* monument
* museum
* noble-villa
* occult-shop
* opera-house
* orphanage
* palace
* park
* paved-streets
* pier
* rubble
* printing-house
* sacred-grove
* secure-warehouse
* sewer-system
* shrine
* smithy
* specialized-artisan
* stable
* stockyard
* stonemason
* tannery
* tavern-dive
* tavern-luxury
* tavern-popular
* tavern-world-class
* temple
* tenement
* theater
* thieves-guild
* town-hall
* trade-shop
* university
* wall-stone
* wall-wooden
* watchtower
* watchtower-stone
* bank-vk
* castle-vk
* construction-yard-vk
* festival-hall-vk
* garrison-vk
* granary-vk
* inn-vk
* library-vk
* magic-shop-vk
* monument-vk
* occult-shop-vk
* palace-vk
* smithy-vk
* tavern-dive-vk
* town-hall-vk
* pier-vk
* tavern-luxury-vk
* tavern-popular-vk
* tavern-world-class-vk
* waterfront-vk
* waterfront

## Full Rules

**Full rules are not yet finalized!**. If you are using them, be prepared for having to manually migrate them if
required!

A full structure rule would look something like this:

```json
{
  "id": "magic-school",
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
      "group": "luxury",
      "alwaysStacks": false,
      "maximumStacks": 3
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
  "maximumCivicRdLimit": 0,
  "upgradeFrom": [
    "pier"
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
  "stacksWith": "other-magic-school-id",
  "reduceUnrestBy": {
    "value": 1,
    "moreThanOncePerTurn": false,
    "note": "as long as it's the first wall in a settlement"
  },
  "reduceRuinBy": {
    "value": 2,
    "ruin": "any"
  },
  "gainRuin": {
    "value": 1,
    "ruin": "decay"
  },
  "increaseResourceDice": {
    "town": 1,
    "metropolis": 3
  },
  "ignoreConsumptionReductionOf": ["pier"]
}
```

* **id**: building id
* **name**: building name
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
    * **maximumStacks**: optional, defaults to 3; maximum number of allowed buildings to consider
    * **alwaysStacks**: optional, false by default; if true, is not subjected to maximum stack rules and will always be added to existing value of its group
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
    * evangelize-the-end
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
* **lots**: used to calculate how many lots the building takes up
* **level**: building level
* **affectsEvents**: optional, used to filter for structures that affect events
* **affectsDowntime**: optional, used to filter for structures that have downtime bonuses
* **reducesUnrest**: optional, used to filter for structures that reduce unrest
* **reducesRuin**: optional, used to filter for structures that reduce ruin
* **upgradeFrom**: optional, includes a list of structure ids that this structure can be upgraded from
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

* **reduceUnrestBy**: optional
  * **value**: how much unrest is reduced
  * **moreThanOncePerTurn**: optional, default true; if false, shows a message that it can only be reduced once
  * **note**: extra restriction notes that are written to chat
* **reduceRuinBy**: optional
  * **value**: how much ruin is reduced
  * **ruin**: one of either **decay**, **crime**, **strife**, **corruption** or **any**
* **gainRuin**: optional, exactly the same as **reduceRuinBy**
* **increaseResourceDice**: optional, key is either **village**, **town**, **city** or **metropolis** and value is a number that increases resource dice gained each turn
* **ignoreConsumptionReductionOf**: optional, a structure id that is removed from consumption reduction in a settlement
* **consumptionReductionStacks**: optional, if true, all structures with the same name in a settlement add up their consumption reduction
* **maximumCivicRdLimit**: optional, increases the limit of RD gained by Town, City and Metropolis RD settings
* **increaseMinimumSettlementActions** optional, increase V&K Settlement Actions viewable in the structure browser